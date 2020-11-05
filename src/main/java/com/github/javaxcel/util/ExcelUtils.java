package com.github.javaxcel.util;

import com.github.javaxcel.exception.UnsupportedWorkbookException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * Gets range of the sheets.
     *
     * @param workbook excel workbook
     * @return range that from 0 to (the number of sheets - 1)
     * @see Workbook#getNumberOfSheets()
     */
    public static int[] getSheetRange(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets()).toArray();
    }

    public static List<Sheet> getSheets(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets())
                .mapToObj(workbook::getSheetAt)
                .collect(toList());
    }

    /**
     * Returns the number of models in a sheet.
     *
     * <p> This excludes header row.
     * In other words, this returns the total number of rows minus 1.
     *
     * @param sheet sheet
     * @return the number of models
     */
    public static int getNumOfModels(Sheet sheet) {
        if (sheet instanceof SXSSFSheet) throw new UnsupportedWorkbookException();
        return Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
    }

    public static long getNumOfModels(Workbook workbook) {
        if (workbook instanceof SXSSFWorkbook) throw new UnsupportedWorkbookException();
        return getSheets(workbook).stream().mapToInt(ExcelUtils::getNumOfModels).sum();
    }

    public static int getMaxRows(Workbook workbook) {
        return isExcel97(workbook)
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
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

}
