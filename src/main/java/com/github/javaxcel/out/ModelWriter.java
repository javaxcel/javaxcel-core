package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.converter.impl.BasicWritingConverter;
import com.github.javaxcel.converter.impl.ExpressiveWritingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.TriConsumer;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Excel writer
 *
 * @param <W> workbook
 * @param <T> the type of model
 */
public final class ModelWriter<W extends Workbook, T> extends AbstractExcelWriter<W, T> {

    private final BasicWritingConverter<T> basicConverter = new BasicWritingConverter<>();

    private final ExpressiveWritingConverter<T> expConverter;

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    //////////////////////////////////////// Style ////////////////////////////////////////

    private TriConsumer<Sheet, Integer, Integer> adjustSheet;

    private CellStyle headerStyle;

    private CellStyle[] columnStyles;

    ///////////////////////////////////////////////////////////////////////////////////////

    private ModelWriter(W workbook, Class<T> type) {
        super(workbook);

        if (type == null) throw new IllegalArgumentException("Type cannot be null");
        this.type = type;

        // Finds targeted fields.
        this.fields = FieldUtils.getTargetedFields(type);
        if (this.fields.isEmpty()) throw new NoTargetedFieldException(type);

        // Caches expressions for each fields to improve performance.
        this.expConverter = new ExpressiveWritingConverter<>(this.fields);

    }

    @Override
    public ModelWriter<W, T> headerNames(List<String> headerNames) {
        super.headerNames(headerNames);
        if (headerNames.size() != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "The number of header names is not equal to the number of targeted fields in the class %s", this.type.getName()));
        }

        return this;
    }

    /**
     * Sets up default value.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.basicConverter.setDefaultValue(defaultValue);
        this.expConverter.setDefaultValue(defaultValue);
        return this;
    }

    @Override
    public ModelWriter<W, T> sheetName(String sheetName) {
        super.sheetName(sheetName);
        return this;
    }

    /**
     * @param triConsumer sheet, numOfRows, numOfColumns
     * @return {@code ExcelWriter}
     */
    public ModelWriter<W, T> adjustSheet(TriConsumer<Sheet, Integer, Integer> triConsumer) {
        if (triConsumer == null) throw new IllegalArgumentException("Tri-consumer cannot be null");

        this.adjustSheet = triConsumer;
        return this;
    }

    public ModelWriter<W, T> headerStyle(BiFunction<CellStyle, Font, CellStyle> biFunction) {
        if (biFunction == null) throw new IllegalArgumentException("Bi-function for header style cannot be null");

        CellStyle headerStyle = biFunction.apply(this.workbook.createCellStyle(), this.workbook.createFont());
        if (headerStyle == null) throw new IllegalArgumentException("Header style cannot be null");

        this.headerStyle = headerStyle;
        return this;
    }

    @SafeVarargs
    public final ModelWriter<W, T> columnStyles(BiFunction<CellStyle, Font, CellStyle>... biFunctions) {
        if (biFunctions == null) throw new IllegalArgumentException("Bi-functions for column styles cannot be null");

        CellStyle[] columnStyles = Arrays.stream(biFunctions)
                .map(func -> func.apply(this.workbook.createCellStyle(), this.workbook.createFont()))
                .toArray(CellStyle[]::new);
        if (columnStyles.length != 1 && columnStyles.length != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "The number of column styles is not equal to the number of targeted fields in the class %s (the number of column styles can be 1 for common style)",
                    this.type.getName()));
        }

        this.columnStyles = columnStyles;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p> If the header names are not set through {@link ExcelWriter#headerNames},
     * this method brings the values from {@link ExcelColumn#name()}.
     *
     * @see FieldUtils#toHeaderNames(List)
     */
    @Override
    protected void ifHeaderNamesAreEmpty(List<String> headerNames) {
        headerNames.addAll(FieldUtils.toHeaderNames(this.fields));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeToSheet(Sheet sheet, List<T> list) {
        final int numOfModels = list.size();
        final int numOfFields = this.fields.size();

        for (int i = 0; i < numOfModels; i++) {
            T model = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfFields; j++) {
                Field field = this.fields.get(j);
                Cell cell = row.createCell(j);

                // Converts field value to the string.
                String value;
                ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
                if (annotation == null) {
                    // When the field is not annotated with @ExcelWriterExpression.
                    value = this.basicConverter.convert(model, field);
                } else {
                    // When the field is annotated with @ExcelWriterExpression.
                    Map<String, Object> simulatedModel = FieldUtils.toMap(model, this.fields);
                    this.expConverter.setVariables(simulatedModel);
                    value = this.expConverter.convert(model, field);
                }

                if (value != null) cell.setCellValue(value);

                // Sets up style to the column.
                if (this.columnStyles != null) {
                    CellStyle columnStyle = this.columnStyles.length == 1
                            ? this.columnStyles[0] // common style
                            : this.columnStyles[j]; // each columns's style
                    cell.setCellStyle(columnStyle);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p> This method adjusts the sheet, rows and columns.
     */
    @Override
    protected void decorate(Sheet sheet, int numOfModels) {
        if (this.adjustSheet != null) this.adjustSheet.accept(sheet, numOfModels + 1, this.fields.size());
    }

}
