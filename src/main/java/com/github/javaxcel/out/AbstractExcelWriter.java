package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.WritingExcelException;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractExcelWriter<W extends Workbook, T> implements ExcelWriter<T> {

    /**
     * Apache POI workbook.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
     * @see org.apache.poi.xssf.streaming.SXSSFWorkbook
     */
    protected final W workbook;

    /**
     * Header's names.
     *
     * @see #headerNames(List)
     */
    protected final List<String> headerNames = new ArrayList<>();

    /**
     * Prefix of all the sheets' names.
     *
     * @see #sheetName(String)
     */
    protected String sheetName;

    /**
     * When the number of rows to be written is greater than maximum rows of sheet,
     * whether to write excess rows to the next sheet.
     *
     * <p> Default is {@code true}.
     *
     * @see #disableRolling()
     */
    protected boolean rolling = true;

    //////////////////////////////////////// Style ////////////////////////////////////////

    /**
     * @see #headerStyles(ExcelStyleConfig...)
     * @see ExcelColumn#headerStyle()
     * @see ExcelModel#headerStyle()
     */
    protected CellStyle[] headerStyles;

    /**
     * @see #bodyStyles(ExcelStyleConfig...)
     * @see ExcelColumn#bodyStyle()
     * @see ExcelModel#bodyStyle()
     */
    protected CellStyle[] bodyStyles;

    /**
     * @see #autoResizeCols()
     */
    protected boolean willAutoResize;

    /**
     * @see #hideExtraRows()
     */
    protected boolean willHideRows;

    /**
     * @see #hideExtraCols()
     */
    protected boolean willHideCols;

    ///////////////////////////////////////////////////////////////////////////////////////

    protected AbstractExcelWriter(W workbook) {
        if (workbook == null) throw new IllegalArgumentException("Workbook cannot be null");
        this.workbook = workbook;
    }

    /**
     * Sets default value when value to be written is null or empty.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link AbstractExcelWriter}
     */
    public AbstractExcelWriter<W, T> defaultValue(String defaultValue) {
        if (StringUtils.isNullOrEmpty(defaultValue)) {
            throw new IllegalArgumentException("Default value cannot be null or empty");
        }

        return this;
    }

    /**
     * Sets sheet name.
     *
     * <p> Prefix for each sheet name. For example, if you set 'SHEET' to this,
     * the names you can see are <span>SHEET0, SHEET1, SHEET2, ...</span>
     *
     * <p> If you invoke {@link #disableRolling()}, the sheet name has no suffix.
     * You can see the sheet name like this <span>SHEET</span>
     *
     * @param sheetName sheet name
     * @return {@link AbstractExcelWriter}
     * @see #disableRolling()
     */
    public AbstractExcelWriter<W, T> sheetName(String sheetName) {
        if (StringUtils.isNullOrEmpty(sheetName)) {
            throw new IllegalArgumentException("Sheet name cannot be null or empty");
        }

        this.sheetName = sheetName;
        return this;
    }

    /**
     * Sets header names.
     *
     * @param headerNames header name
     * @return {@link AbstractExcelWriter}
     */
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
     * Disables rolling excess rows.
     *
     * <p> If this is invoked, excel file has only one sheet.
     *
     * @return {@link AbstractExcelWriter}
     */
    public AbstractExcelWriter<W, T> disableRolling() {
        this.rolling = false;
        return this;
    }

    //////////////////////////////////////// Style ////////////////////////////////////////

    public AbstractExcelWriter<W, T> headerStyles(ExcelStyleConfig... configs) {
        this.headerStyles = ExcelUtils.toCellStyles(this.workbook, configs);
        return this;
    }

    public AbstractExcelWriter<W, T> bodyStyles(ExcelStyleConfig... configs) {
        this.bodyStyles = ExcelUtils.toCellStyles(this.workbook, configs);
        return this;
    }

    public AbstractExcelWriter<W, T> autoResizeCols() {
        this.willAutoResize = true;
        return this;
    }

    public AbstractExcelWriter<W, T> hideExtraRows() {
        this.willHideRows = true;
        return this;
    }

    public AbstractExcelWriter<W, T> hideExtraCols() {
        this.willHideCols = true;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public final void write(OutputStream out, List<T> list) {
        if (list == null) throw new IllegalArgumentException("List of models cannot be null");

        beforeWrite(out, list);

        final int maxModels = ExcelUtils.getMaxRows(this.workbook) - 1;
        List<List<T>> lists = this.rolling
                ? CollectionUtils.partitionBySize(list, maxModels)
                : Collections.singletonList(list.subList(0, Math.min(list.size(), maxModels)));

        // Writes each sheet.
        final int numOfSheets = lists.size();
        for (int i = 0; i < numOfSheets; i++) {
            // Names a sheet.
            Sheet sheet = this.sheetName == null
                    ? this.workbook.createSheet()
                    : this.workbook.createSheet(this.rolling ? this.sheetName + i : this.sheetName);

            // Writes header.
            createHeader(sheet);

            List<T> those = lists.get(i);
            writeToSheet(sheet, those);

            // Adjusts rows and columns.
            if (this.willAutoResize) ExcelUtils.autoResizeColumns(sheet, getNumOfColumns());
            if (this.willHideRows) ExcelUtils.hideExtraRows(sheet, those.size() + 1);
            if (this.willHideCols) ExcelUtils.hideExtraColumns(sheet, getNumOfColumns());
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
        final int numOfHeaders = this.headerNames.size();
        for (int i = 0; i < numOfHeaders; i++) {
            String name = this.headerNames.get(i);

            Cell cell = row.createCell(i);
            cell.setCellValue(name);

            if (this.headerStyles == null) continue;

            // Sets common style to all header cells or each style to each header cell.
            CellStyle headerStyle = this.headerStyles.length == 1
                    ? this.headerStyles[0] : this.headerStyles[i];

            // When configure styles with annotations, there is possibility that 'headerStyles' has null elements.
            if (headerStyle != null) cell.setCellStyle(headerStyle);
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
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    protected abstract int getNumOfColumns();

}
