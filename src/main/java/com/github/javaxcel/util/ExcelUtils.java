package com.github.javaxcel.util;

import com.github.javaxcel.exception.UnsupportedWorkbookException;
import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.styler.config.ExcelStyleConfig;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * Returns range of the sheets.
     *
     * @param workbook excel workbook
     * @return range that from 0 to (the number of sheets - 1)
     * @see Workbook#getNumberOfSheets()
     */
    public static int[] getSheetRange(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets()).toArray();
    }

    /**
     * Returns all sheets in a workbook.
     *
     * @param workbook excel workbook
     * @return all sheets
     */
    public static List<Sheet> getSheets(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets())
                .mapToObj(workbook::getSheetAt)
                .collect(toList());
    }

    /**
     * Returns the number of rows in a sheet.
     *
     * @param sheet sheet
     * @return the number of rows
     * @throws UnsupportedWorkbookException if instance of sheet is {@link SXSSFSheet}
     */
    public static int getNumOfRows(Sheet sheet) {
        if (sheet instanceof SXSSFSheet) throw new UnsupportedWorkbookException();
        return Math.max(0, sheet.getPhysicalNumberOfRows());
    }

    /**
     * Returns the number of rows in all sheets.
     *
     * @param workbook excel workbook
     * @return the number of rows
     * @throws UnsupportedWorkbookException if instance of workbook is {@link SXSSFWorkbook}
     */
    public static long getNumOfRows(Workbook workbook) {
        if (workbook instanceof SXSSFWorkbook) throw new UnsupportedWorkbookException();
        return getSheets(workbook).stream().mapToInt(ExcelUtils::getNumOfRows).sum();
    }

    /**
     * Returns the number of models in a sheet.
     *
     * <p> This excludes header row. In other words,
     * this returns the total number of rows minus 1.
     *
     * @param sheet sheet
     * @return the number of models
     * @throws UnsupportedWorkbookException if instance of sheet is {@link SXSSFSheet}
     */
    public static int getNumOfModels(Sheet sheet) {
        if (sheet instanceof SXSSFSheet) throw new UnsupportedWorkbookException();
        return Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
    }

    /**
     * Returns the number of models in all sheets.
     *
     * <p> This excludes header row. In other words,
     * this returns the total number of rows minus number of all headers.
     *
     * @param workbook excel workbook
     * @return the number of models
     * @throws UnsupportedWorkbookException if instance of workbook is {@link SXSSFWorkbook}
     */
    public static long getNumOfModels(Workbook workbook) {
        if (workbook instanceof SXSSFWorkbook) throw new UnsupportedWorkbookException();
        return getSheets(workbook).stream().mapToInt(ExcelUtils::getNumOfModels).sum();
    }

    public static int getMaxRows(Workbook workbook) {
        return isExcel97(workbook)
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
    }

    public static int getMaxRows(Sheet sheet) {
        return isExcel97(sheet)
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
    }

    public static int getMaxColumns(Workbook workbook) {
        return isExcel97(workbook)
                ? SpreadsheetVersion.EXCEL97.getMaxColumns()
                : SpreadsheetVersion.EXCEL2007.getMaxColumns();
    }

    public static int getMaxColumns(Sheet sheet) {
        return isExcel97(sheet)
                ? SpreadsheetVersion.EXCEL97.getMaxColumns()
                : SpreadsheetVersion.EXCEL2007.getMaxColumns();
    }

    public static boolean isExcel97(Workbook workbook) {
        return workbook instanceof HSSFWorkbook;
    }

    public static boolean isExcel97(Sheet sheet) {
        return sheet instanceof HSSFSheet;
    }

    public static boolean isExcel97(Row row) {
        return row instanceof HSSFRow;
    }

    public static boolean isExcel97(Cell cell) {
        return cell instanceof HSSFCell;
    }

    /**
     * Adjusts width of columns to fit the contents.
     *
     * <p> This can be affected by font size and font family.
     * If you want this process well, set up the same font family into all cells.
     * This process will be perform in parallel.
     *
     * @param sheet        excel sheet
     * @param numOfColumns number of the columns that wanted to make fit contents.
     * @throws UnsupportedWorkbookException if instance of sheet is {@link SXSSFSheet}
     * @see Sheet#autoSizeColumn(int)
     */
    public static void autoResizeColumns(Sheet sheet, int numOfColumns) {
        if (sheet instanceof SXSSFSheet) throw new UnsupportedWorkbookException();
        IntStream.range(0, numOfColumns).parallel().forEach(sheet::autoSizeColumn);
    }

    /**
     * Hides extraneous rows.
     *
     * <p> This process must not be performed in parallel.
     * If try it, this will throw {@link java.util.ConcurrentModificationException}.
     *
     * @param sheet     excel sheet
     * @param numOfRows number of the rows that have contents.
     * @see Row#setZeroHeight(boolean)
     */
    public static void hideExtraRows(Sheet sheet, int numOfRows) {
        final int maxRows = getMaxRows(sheet);

        for (int i = numOfRows; i < maxRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) row = sheet.createRow(i);

            row.setZeroHeight(true);
        }
    }

    /**
     * Hides extraneous columns.
     *
     * <p> This process shouldn't be performed in parallel.
     * If try it, this is about 46% slower when handled in parallel
     * than when handled in sequential.
     *
     * <pre>{@code
     *     +------------+----------+
     *     | sequential | parallel |
     *     +------------+----------+
     *     | 15s        | 22s      |
     *     +------------+----------+
     * }</pre>
     *
     * @param sheet        excel sheet
     * @param numOfColumns number of the columns that have contents.
     * @see Sheet#setColumnHidden(int, boolean)
     */
    public static void hideExtraColumns(Sheet sheet, int numOfColumns) {
        final int maxColumns = getMaxColumns(sheet);

        for (int i = numOfColumns; i < maxColumns; i++) {
            sheet.setColumnHidden(i, true);
        }
    }

    public static CellStyle[] toCellStyles(Workbook workbook, ExcelStyleConfig... configs) {
        if (configs == null || configs.length == 0) {
            throw new IllegalArgumentException("Configurations for style cannot be null or empty");
        }

        CellStyle[] cellStyles = new CellStyle[configs.length];
        for (int i = 0; i < configs.length; i++) {
            ExcelStyleConfig config = configs[i];

            CellStyle cellStyle = workbook.createCellStyle();
            Configurer configurer = new Configurer(cellStyle, workbook.createFont());
            config.configure(configurer);

            cellStyles[i] = cellStyle;
        }

        return cellStyles;
    }

}
