package com.github.javaxcel.out;

import com.github.javaxcel.exception.WritingExcelException;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractExcelWriter<W extends Workbook, T> implements ExcelWriter<W, T> {

    /**
     * Apache POI workbook.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
     * @see org.apache.poi.xssf.streaming.SXSSFWorkbook
     */
    protected final W workbook;

    /**
     * Headers' names.
     */
    protected final List<String> headerNames = new ArrayList<>();

    /**
     * Prefix of all the sheets' names.
     */
    protected String sheetName;

    protected AbstractExcelWriter(W workbook) {
        if (workbook == null) throw new IllegalArgumentException("Workbook cannot be null");

        this.workbook = workbook;
    }

    /**
     * Sets up default value.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link AbstractExcelWriter}
     */
    @Override
    public AbstractExcelWriter<W, T> defaultValue(String defaultValue) {
        if (StringUtils.isNullOrEmpty(defaultValue)) {
            throw new IllegalArgumentException("Default value cannot be null or empty");
        }

        return this;
    }

    @Override
    public AbstractExcelWriter<W, T> sheetName(String sheetName) {
        if (StringUtils.isNullOrEmpty(sheetName)) {
            throw new IllegalArgumentException("Sheet name cannot be null or empty");
        }

        this.sheetName = sheetName;
        return this;
    }

    @Override
    public AbstractExcelWriter<W, T> headerNames(List<String> headerNames) {
        if (CollectionUtils.isNullOrEmpty(headerNames)) {
            throw new IllegalArgumentException("Header names cannot be null or empty");
        }

        // Replaces current header names with the new things.
        if (!this.headerNames.isEmpty()) this.headerNames.clear();

        this.headerNames.addAll(headerNames);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void write(OutputStream out, List<T> list) {
        if (list == null) throw new IllegalArgumentException("List of models cannot be null");

        beforeWrite(out, list);

        Function<Integer, Sheet> createSheet = this.sheetName == null
                ? i -> this.workbook.createSheet()
                : i -> this.workbook.createSheet(this.sheetName + i);

        int maxModels = ExcelUtils.getMaxRows(this.workbook) - 1;
        List<List<T>> lists = CollectionUtils.partitionBySize(list, maxModels);

        // Writes each sheets.
        int numOfSheets = lists.size();
        for (int i = 0; i < numOfSheets; i++) {
            // Writes header.
            Sheet sheet = createSheet.apply(i);
            createHeader(sheet);

            List<T> those = lists.get(i);
            writeToSheet(sheet, those);
            decorate(sheet, those.size());
        }

        // Saves the data.
        save(out);

        afterWrite(out, list);
    }

    protected void beforeWrite(OutputStream out, List<T> list) {
    }

    protected void afterWrite(OutputStream out, List<T> list) {
    }

    private void createHeader(Sheet sheet) {
        // Creates the first row that is header.
        Row row = sheet.createRow(0);

        if (this.headerNames.isEmpty()) ifHeaderNamesAreEmpty(this.headerNames);

        // Names the header given values.
        int numOfHeaders = this.headerNames.size();
        for (int i = 0; i < numOfHeaders; i++) {
            String name = this.headerNames.get(i);

            Cell cell = row.createCell(i);
            cell.setCellValue(name);

            // Sets up style of the header.
//            if (this.headerStyle != null) cell.setCellStyle(this.headerStyle); TODO: Support custom styling
        }
    }

    /**
     * Saves the data into a excel file.
     *
     * @param out output stream
     */
    private void save(OutputStream out) {
        try {
            this.workbook.write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

    /**
     * Handles header names, if they are empty.
     *
     * <p> You have to implement how to do, if the header names are empty.
     * For examples, you can throw exception or set default header names.
     * This process will be executed before {@link ExcelWriter} writes header.
     *
     * @param headerNames header names
     */
    protected abstract void ifHeaderNamesAreEmpty(List<String> headerNames);

    /**
     * Writes list of models to the sheet.
     *
     * @param sheet sheet
     * @param list  list of models
     */
    protected abstract void writeToSheet(Sheet sheet, List<T> list);

    /**
     * Decorates the sheet.
     *
     * @param sheet       sheet
     * @param numOfModels the number of models
     *                    (it is equal to the number of rows in the sheet)
     */
    protected abstract void decorate(Sheet sheet, int numOfModels);

}
