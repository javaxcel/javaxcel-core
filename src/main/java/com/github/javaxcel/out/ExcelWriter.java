package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.converter.impl.BasicWritingConverter;
import com.github.javaxcel.converter.impl.ExpressiveWritingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.exception.WritingExcelException;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.StringUtils;
import com.github.javaxcel.util.TriConsumer;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
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
public final class ExcelWriter<W extends Workbook, T> {

    private final BasicWritingConverter<T> basicConverter = new BasicWritingConverter<>();

    private final ExpressiveWritingConverter<T> expConverter = new ExpressiveWritingConverter<>();

    /**
     * Apache POI workbook.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
     * @see org.apache.poi.xssf.streaming.SXSSFWorkbook
     */
    private final W workbook;

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    /**
     * Name of columns in header.
     */
    private String[] headerNames;

    /**
     * Name of excel sheet.
     */
    private String sheetName;

    //////////////////////////////////////// Style ////////////////////////////////////////

    private TriConsumer<Sheet, Integer, Integer> adjustSheet;

    private CellStyle headerStyle;

    private CellStyle[] columnStyles;

    ///////////////////////////////////////////////////////////////////////////////////////

    private ExcelWriter(W workbook, Class<T> type) {
        this.workbook = workbook;
        this.type = type;
        this.fields = FieldUtils.getTargetedFields(type);

        if (this.fields.isEmpty()) throw new NoTargetedFieldException(this.type);

        expConverter.cacheExpressions(this.fields);
    }

    /**
     * Initializes excel writer.
     *
     * @param workbook excel workbook
     * @param type     class type
     * @param <W>      instance that implements {@link Workbook}
     * @param <E>      type of the element
     * @return excel writer
     */
    public static <W extends Workbook, E> ExcelWriter<W, E> init(W workbook, Class<E> type) {
        return new ExcelWriter<>(workbook, type);
    }

    public ExcelWriter<W, T> headerNames(String... headerNames) {
        if (headerNames == null) throw new IllegalArgumentException("Header names cannot be null");
        if (headerNames.length != this.fields.size()) {
            throw new IllegalArgumentException(String.format(
                    "The number of header names is not equal to the number of targeted fields in the class %s", this.type.getName()));
        }

        this.headerNames = headerNames;
        return this;
    }

    /**
     * Sets up default value.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link ExcelWriter}
     */
    public ExcelWriter<W, T> defaultValue(String defaultValue) {
        if (defaultValue == null) throw new IllegalArgumentException("Default value cannot be null");

        this.basicConverter.setDefaultValue(defaultValue);
        this.expConverter.setDefaultValue(defaultValue);
        return this;
    }

    public ExcelWriter<W, T> sheetName(String sheetName) {
        if (StringUtils.isNullOrEmpty(sheetName)) {
            throw new IllegalArgumentException("Sheet name cannot be null or empty");
        }

        this.sheetName = sheetName;
        return this;
    }

    /**
     * @param triConsumer sheet, numOfRows, numOfColumns
     * @return {@code ExcelWriter}
     */
    public ExcelWriter<W, T> adjustSheet(TriConsumer<Sheet, Integer, Integer> triConsumer) {
        if (triConsumer == null) throw new IllegalArgumentException("Tri-consumer cannot be null");

        this.adjustSheet = triConsumer;
        return this;
    }

    public ExcelWriter<W, T> headerStyle(BiFunction<CellStyle, Font, CellStyle> biFunction) {
        if (biFunction == null) throw new IllegalArgumentException("Bi-function for header style cannot be null");

        CellStyle headerStyle = biFunction.apply(this.workbook.createCellStyle(), this.workbook.createFont());
        if (headerStyle == null) throw new IllegalArgumentException("Header style cannot be null");

        this.headerStyle = headerStyle;
        return this;
    }

    @SafeVarargs
    public final ExcelWriter<W, T> columnStyles(BiFunction<CellStyle, Font, CellStyle>... biFunctions) {
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
     * Writes the data in a sheet.
     *
     * @param out  output stream for writing excel workbook
     * @param list data list
     */
    public void write(OutputStream out, List<T> list) {
        if (list == null) throw new IllegalArgumentException("Data list cannot be null");

        // Creates a sheet.
        this.sheetName = StringUtils.ifNullOrEmpty(sheetName, "Sheet");
        Sheet sheet = this.workbook.createSheet(this.sheetName);

        // Creates the first row that is header.
        Row row = sheet.createRow(0);

        // If the header name is not set through ExcelWriter, brings values from @ExcelColumn.
        if (this.headerNames == null) this.headerNames = FieldUtils.toHeaderNames(this.fields);

        // If the header name doesn't exist, ExcelWriter doesn't write.
        if (this.headerNames.length == 0) return;

        // Names the header given values.
        for (int i = 0; i < this.headerNames.length; i++) {
            String name = this.headerNames[i];

            Cell cell = row.createCell(i);
            cell.setCellValue(name);

            // Sets up style of the header.
            if (this.headerStyle != null) cell.setCellStyle(this.headerStyle);
        }

        // Sets data into a sheet.
        if (!list.isEmpty()) listToSheet(sheet, list);

        // Adjusts a sheet, rows and columns.
        if (this.adjustSheet != null) this.adjustSheet.accept(sheet, list.size() + 1, this.fields.size());

        // Writes data to the excel sheet.
        try {
            this.workbook.write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

    /**
     * Sets values of the list into a sheet.
     *
     * @param sheet sheet
     * @param list  list
     */
    private void listToSheet(Sheet sheet, List<T> list) {
        final int listSize = list.size();
        final int fieldsSize = this.fields.size();

        for (int i = 0; i < listSize; i++) {
            T model = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < fieldsSize; j++) {
                Field field = this.fields.get(j);
                Cell cell = row.createCell(j);

                // Converts field value to the string.
                String value;
                ExcelWriterExpression conversion = field.getAnnotation(ExcelWriterExpression.class);
                if (conversion == null) {
                    // When the field is not annotated with @ExcelWriterExpression.
                    value = basicConverter.convert(model, field);
                } else {
                    // When the field is annotated with @ExcelWriterExpression.
                    Map<String, Object> simulatedModel = FieldUtils.toMap(model, this.fields);
                    expConverter.setVariables(simulatedModel);
                    value = expConverter.convert(model, field);
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

}
