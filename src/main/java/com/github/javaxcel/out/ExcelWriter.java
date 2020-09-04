package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * Initializes excel writer.
     *
     * @param workbook excel workbook
     * @param type class type
     * @param <W> instance that extends {@code Workbook}
     * @param <E> type of the element
     * @return excel writer
     */
    public static <W extends Workbook, E> ExcelWriter<W, E> init(W workbook, Class<E> type) {
        return new ExcelWriter<>(workbook, type);
    }

    private ExcelWriter(W workbook, Class<T> type) {
        this.workbook = workbook;
        this.type = type;

        // @ExcelModel의 타깃 필드 정책에 따라 가져오는 필드가 다르다
        ExcelModel annotation = this.type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(this.type.getDeclaredFields())
                : ExcelUtils.getInheritedFields(this.type).stream();

        // Excludes the fields annotated @ExcelIgnore.
        this.fields = stream.filter(field -> field.getAnnotation(ExcelIgnore.class) == null)
                .collect(Collectors.toList());
    }

    public ExcelWriter<W, T> headerNames(String... headerNames) {
        if (headerNames.length != this.fields.size()) {
            throw new IllegalArgumentException("The number of header names is not equal to the number of targeted fields in the class " + this.type.getName());
        }

        this.headerNames = headerNames;
        return this;
    }

    public ExcelWriter<W, T> defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ExcelWriter<W, T> sheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * 엑셀 파일을 생성한다, 값이 null이거나 empty string인 경우 지정된 문자열로 치환한다.
     *
     * @param out output stream for writing excel workbook
     * @param list data list
     * @throws IOException
     * @throws IllegalAccessException
     */
    public void write(OutputStream out, List<T> list) throws IOException, IllegalAccessException {
        this.sheetName = StringUtils.ifNullOrEmpty(sheetName, "Sheet");
        Sheet sheet = this.workbook.createSheet(this.sheetName);

        Row row = sheet.createRow(0);

        // Sets up style of the header.
        CellStyle style = this.workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = this.workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        // 헤더명을 설정하지 않은 경우, 우선순위: @ExcelColumn에 지정한 헤더명 > 필드명
        if (this.headerNames == null) {
            this.headerNames = this.fields.stream()
                    .map(field -> {
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        return annotation == null || StringUtils.isNullOrEmpty(annotation.value()) ? field.getName() : annotation.value();
                    }).toArray(String[]::new);
        }

        // 빈 헤더명을 설정한 경우, 종료한다
        if (this.headerNames.length == 0) return;

        // Creates the header.
        for (int i = 0; i < this.headerNames.length; i++) {
            String name = this.headerNames[i];

            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(name);
        }

        // Writes the data.
        if (list != null && !list.isEmpty()) setValueToCellFromFields(sheet, list);
        this.workbook.write(out);
    }

    /**
     * 상속받은 필드는 포함하지 않으나, 필드의 순서가 일정하다.
     */
    private void setValueToCellFromFields(Sheet sheet, List<T> list) throws IllegalAccessException {
        final int listSize = list.size();
        final int fieldsSize = this.fields.size();

        for (int i = 0; i < listSize; i++) {
            T element = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < fieldsSize; j++) {
                Field field = this.fields.get(j);

                // Creates the cell and sets up data to it.
                Cell cell = row.createCell(j);
                String value = ExcelUtils.stringifyValue(element, field);
                cell.setCellValue(StringUtils.ifNullOrEmpty(value, () -> {
                    // 기본값 우선순위: ExcelWriter.write에 넘겨준 기본값 > @ExcelColumn에 지정한 기본값
                    if (this.defaultValue != null) return this.defaultValue;
                    ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                    return annotation != null ? annotation.defaultValue() : null;
                }));
            }
        }
    }

}
