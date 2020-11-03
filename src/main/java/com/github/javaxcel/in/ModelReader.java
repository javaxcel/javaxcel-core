package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.converter.impl.ExpressiveReadingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.exception.UnsupportedWorkbookException;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.expression.Expression;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader
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

    /**
     * Sheet's indexes that {@link ModelReader} will read.
     * <br>
     * Default value is {@code {0}} (it means index of the first sheet).
     */
    private int[] sheetIndexes = {0};

    /**
     * Row's index that {@link ModelReader} will start to read.
     */
    private int startIndex;

    /**
     * Row's index that {@link ModelReader} will end to read.
     * <br>
     * Default value is {@code -1} (it means index of the last row).
     */
    private int endIndex = -1;

    private ModelReader(W workbook, Class<T> type) {
        super(workbook);

        this.type = type;
        this.fields = FieldUtils.getTargetedFields(type);

        if (this.fields.isEmpty()) throw new NoTargetedFieldException(this.type);

        this.cache = ExpressiveReadingConverter.createCache(this.fields);
    }

    @Deprecated
    public static <W extends Workbook, E> ModelReader<W, E> init(W workbook, Class<E> type) {
        if (workbook instanceof SXSSFWorkbook) throw new UnsupportedWorkbookException();
        return new ModelReader<>(workbook, type);
    }

    @Deprecated
    public ModelReader<W, T> sheetIndexes(int... sheetIndexes) {
        if (sheetIndexes == null || sheetIndexes.length == 0 || IntStream.of(sheetIndexes).anyMatch(i -> i < 0)) {
            throw new IllegalArgumentException("Sheet indexes cannot be null, empty or less than 0.");
        }

        this.sheetIndexes = sheetIndexes;
        return this;
    }

    @Deprecated
    public ModelReader<W, T> startIndex(int startIndex) {
        if (startIndex < 0) throw new IllegalArgumentException("Start index cannot be less than 0.");

        this.startIndex = startIndex;
        return this;
    }

    @Deprecated
    public ModelReader<W, T> endIndex(int endIndex) {
        if (endIndex < 0) throw new IllegalArgumentException("End index cannot be less than 0.");

        this.endIndex = endIndex;
        return this;
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
     * {@inheritDoc}
     *
     * @return {@link ModelReader}
     */
    @Override
    public ModelReader<W, T> parallel() {
        super.parallel();
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
        final int numOfRows = this.limit < 0 ? ExcelUtils.getNumOfModels(sheet) : this.limit;

        // 인덱스 유효성을 체크한다
        if (this.endIndex == -1 || this.endIndex > numOfRows) this.endIndex = numOfRows;

        // Reads rows.
        List<Map<String, Object>> simulatedModels = new ArrayList<>();
        for (int i = this.startIndex; i < this.endIndex; i++) {
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
        T model = ExcelUtils.instantiate(this.type);

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
