package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelWriterConversion;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.exception.WritingExcelException;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.TriConsumer;
import io.github.imsejin.util.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * ExcelWriter
 *
 * <pre>
 * 1. VO의 필드가 오직 `기초형`, `Wrapper Class` 또는 `String`이어야 한다.
 *    이외의 타입을 갖는 필드(컬럼)는 계산하지 않는다, 즉 해당 필드는 순서(엑셀 파일)에서 제외된다.
 *
 * 2. 상속받은 필드는 제외된다, 즉 해당 VO에서 정의된 필드만 계산한다.
 *
 * 3. `headerNames`와 VO의 필드 순서가 일치해야 한다.
 * </pre>
 */
public final class ExcelWriter<W extends Workbook, T> {

    /**
     * @see Workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
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
     * Replacement of the value when the value is null or empty string.
     */
    private String defaultValue;

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

    public ExcelWriter<W, T> defaultValue(String defaultValue) {
        if (defaultValue == null) throw new IllegalArgumentException("Default value cannot be null");

        this.defaultValue = defaultValue;
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
            throw new IllegalArgumentException(String.format("The number of column styles is not equal to the number of targeted fields in the class %s (the number of column styles can be 1 for common style)", this.type.getName()));
        }

        this.columnStyles = columnStyles;
        return this;
    }

    /**
     * 엑셀 파일을 생성한다, 값이 null이거나 empty string인 경우 지정된 문자열로 치환한다.
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

        // 헤더명을 설정하지 않은 경우, 우선순위: @ExcelColumn에 지정한 헤더명 > 필드명
        if (this.headerNames == null) this.headerNames = FieldUtils.toHeaderNames(this.fields);

        // 빈 헤더명을 설정한 경우, 종료한다
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
            T element = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < fieldsSize; j++) {
                Field field = this.fields.get(j);
                Cell cell = row.createCell(j);

                // Computes the data.
                String value;
                ExcelWriterConversion conversion = field.getAnnotation(ExcelWriterConversion.class);
                if (conversion == null) {
                    // When the field is not annotated with @ExcelWriterConversion.
                    value = StringUtils.ifNullOrEmpty(ExcelUtils.stringifyValue(element, field), () -> {
                        // 기본값 우선순위: ExcelWriter.write에 넘겨준 기본값 > @ExcelColumn에 지정한 기본값
                        if (this.defaultValue != null) return this.defaultValue;
                        ExcelColumn column = field.getAnnotation(ExcelColumn.class);
                        return column != null ? column.defaultValue() : null;
                    });
                } else {
                    // When the field is annotated with @ExcelWriterConversion.
                    Map<String, Object> map = FieldUtils.toMap(element, this.fields);
                    value = ExcelUtils.parseExpression(element, field, map);
                }

                cell.setCellValue(value);

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
