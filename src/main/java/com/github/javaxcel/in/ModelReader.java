package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.converter.impl.ExpressiveReadingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.expression.Expression;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader for model.
 *
 * @param <W> excel workbook
 * @param <T> type of model
 */
public final class ModelReader<W extends Workbook, T> extends AbstractExcelReader<W, T> {

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    private final Map<String, Expression> cache;

    private boolean parallel;

    private ModelReader(W workbook, Class<T> type) {
        super(workbook);

        this.type = type;
        this.fields = FieldUtils.getTargetedFields(type);

        if (this.fields.isEmpty()) throw new NoTargetedFieldException(this.type);

        this.cache = ExpressiveReadingConverter.createCache(this.fields);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelReader}
     */
    @Override
    public ModelReader<W, T> limit(int limit) {
        super.limit(limit);
        return this;
    }

    /**
     * Makes the conversion from simulated model into real model parallel.
     *
     * <p> We recommend processing in parallel only when
     * dealing with large data. The following table is a benchmark.
     *
     * <pre>{@code
     *     +------------+------------+----------+
     *     | row \ type | sequential | parallel |
     *     +------------+------------+----------+
     *     | 10,000     | 16s        | 13s      |
     *     +------------+------------+----------+
     *     | 25,000     | 31s        | 21s      |
     *     +------------+------------+----------+
     *     | 100,000    | 2m 7s      | 1m 31s   |
     *     +------------+------------+----------+
     *     | 150,000    | 3m 28s     | 2m 1s    |
     *     +------------+------------+----------+
     * }</pre>
     *
     * @return {@link ModelReader}
     */
    public ModelReader<W, T> parallel() {
        if (this.parallel) return this;

        this.parallel = true;
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * Reads a sheet and Adds rows of the data into list.
     *
     * @param sheet sheet to be read
     */
    @Override
    protected List<T> readSheet(Sheet sheet) {
        List<Map<String, Object>> simulatedModels = getSimulatedModels(sheet);
        Stream<Map<String, Object>> stream = this.parallel
                ? simulatedModels.parallelStream() : simulatedModels.stream();

        return stream.map(this::toRealModel).collect(toList());
    }

    @Override
    protected int getNumOfColumns(Row row) {
        return this.fields.size();
    }

    @Override
    protected String getColumnName(Cell cell, int columnIndex) {
        return this.fields.get(columnIndex).getName();
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets simulated models from a sheet.
     *
     * @param sheet excel sheet
     * @return simulated models
     */
    private List<Map<String, Object>> getSimulatedModels(Sheet sheet) {
        int numOfRows = ExcelUtils.getNumOfModels(sheet);
        if (this.limit >= 0) numOfRows = Math.min(this.limit, numOfRows);

        // Reads rows.
        List<Map<String, Object>> simulatedModels = new ArrayList<>();
        for (int i = 0; i < numOfRows; i++) {
            if (this.numOfRowsRead == this.limit) break;

            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            // Adds a row data of the sheet.
            simulatedModels.add(readRow(row));
        }

        return simulatedModels;
    }

    /**
     * Converts a simulated model to the real model.
     *
     * @param sModel simulated model
     * @return real model
     */
    private T toRealModel(Map<String, Object> sModel) {
        T model = FieldUtils.instantiate(this.type);

        for (Field field : this.fields) {
            String cellValue = (String) sModel.get(field.getName());

            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            Object fieldValue;
            if (annotation == null) {
                // When the field is not annotated with @ExcelReaderExpression.
                fieldValue = basicConverter.convert(cellValue, field);
            } else {
                // When the field is annotated with @ExcelReaderExpression.
                ExpressiveReadingConverter<T> expConverter = new ExpressiveReadingConverter<>(this.type);
                expConverter.setVariables(sModel);
                Expression expression = this.cache.get(field.getName());
                fieldValue = expConverter.convert(cellValue, field, expression);
            }

            FieldUtils.setFieldValue(model, field, fieldValue);
        }

        return model;
    }

}
