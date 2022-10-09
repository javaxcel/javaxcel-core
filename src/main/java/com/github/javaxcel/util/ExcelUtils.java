/*
 * Copyright 2020 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.util;

import com.github.javaxcel.exception.UnsupportedWorkbookException;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import com.github.javaxcel.styler.config.Configurer;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.FilenameUtils;
import jakarta.validation.constraints.Null;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Utilities for spreadsheet Excel with Apache POI.
 *
 * @see Workbook
 * @see Sheet
 * @see Row
 * @see Cell
 * @see CellStyle
 * @see Font
 */
public final class ExcelUtils {

    public static final String EXCEL_97_EXTENSION = "xls";
    public static final String EXCEL_2007_EXTENSION = "xlsx";

    @ExcludeFromGeneratedJacocoReport
    private ExcelUtils() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    /**
     * Returns the instance of {@link Workbook} by reading file.
     *
     * @param file Excel file
     * @return Excel workbook instance
     * @throws IllegalArgumentException unless file extension is equal to 'xls' or 'xlsx'
     */
    public static Workbook getWorkbook(File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        Asserts.that(extension)
                .describedAs("Extension of Excel file must be '{0}' or '{1}'",
                        EXCEL_97_EXTENSION, EXCEL_2007_EXTENSION)
                .matches(EXCEL_97_EXTENSION + '|' + EXCEL_2007_EXTENSION);

        Workbook workbook;
        try {
            if (extension.equals(EXCEL_97_EXTENSION)) {
                InputStream in = new FileInputStream(file);
                workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(file);
            }
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }

        return workbook;
    }

    /**
     * Returns all sheets in a workbook.
     *
     * @param workbook Excel workbook
     * @return all sheets
     */
    public static List<Sheet> getSheets(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();

        List<Sheet> sheets = new ArrayList<>(numberOfSheets);
        for (int i = 0; i < numberOfSheets; i++) {
            if (workbook.isSheetHidden(i)) continue;

            Sheet sheet = workbook.getSheetAt(i);
            sheets.add(sheet);
        }

        return sheets;
    }

    /**
     * Returns the number of rows in a sheet.
     *
     * @param sheet sheet
     * @return the number of rows
     * @throws UnsupportedWorkbookException if instance of sheet is {@link SXSSFSheet}
     */
    public static int getNumOfRows(Sheet sheet) {
        Asserts.that(sheet)
                .isNotNull()
                .describedAs("SXSSFWorkbook is not supported workbook when read")
                .thrownBy(UnsupportedWorkbookException::new)
                .isNotInstanceOf(SXSSFSheet.class);

        return Math.max(0, sheet.getPhysicalNumberOfRows());
    }

    /**
     * Returns the number of rows in all sheets.
     *
     * @param workbook Excel workbook
     * @return the number of rows
     * @throws UnsupportedWorkbookException if instance of workbook is {@link SXSSFWorkbook}
     */
    public static long getNumOfRows(Workbook workbook) {
        Asserts.that(workbook)
                .isNotNull()
                .describedAs("SXSSFWorkbook is not supported workbook when read")
                .thrownBy(UnsupportedWorkbookException::new)
                .isNotInstanceOf(SXSSFWorkbook.class);

        long numOfRows = 0;
        for (Sheet sheet : getSheets(workbook)) {
            numOfRows += getNumOfRows(sheet);
        }

        return numOfRows;
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
        Asserts.that(sheet)
                .isNotNull()
                .describedAs("SXSSFWorkbook is not supported workbook when read")
                .thrownBy(UnsupportedWorkbookException::new)
                .isNotInstanceOf(SXSSFSheet.class);

        return Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
    }

    /**
     * Returns the number of models in all sheets.
     *
     * <p> This excludes header row. In other words,
     * this returns the total number of rows minus number of all headers.
     *
     * @param workbook Excel workbook
     * @return the number of models
     * @throws UnsupportedWorkbookException if instance of workbook is {@link SXSSFWorkbook}
     */
    public static long getNumOfModels(Workbook workbook) {
        Asserts.that(workbook)
                .isNotNull()
                .describedAs("SXSSFWorkbook is not supported workbook when read")
                .thrownBy(UnsupportedWorkbookException::new)
                .isNotInstanceOf(SXSSFWorkbook.class);

        long numOfModels = 0;
        for (Sheet sheet : getSheets(workbook)) {
            numOfModels += getNumOfModels(sheet);
        }

        return numOfModels;
    }

    /**
     * Returns the number of models in all sheets.
     *
     * <p> This excludes header row. In other words,
     * this returns the total number of rows minus number of all headers.
     *
     * @param file Excel file
     * @return the number of models
     */
    public static long getNumOfModels(File file) {
        Workbook workbook = getWorkbook(file);
        return getNumOfModels(workbook);
    }

    /**
     * Returns maximum number of rows in a spreadsheet.
     *
     * @param workbook Excel workbook
     * @return maximum number of rows in a spreadsheet
     */
    public static int getMaxRows(Workbook workbook) {
        return isExcel97(workbook)
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
    }

    /**
     * Returns maximum number of rows in a spreadsheet.
     *
     * @param sheet sheet
     * @return maximum number of rows in a spreadsheet
     */
    public static int getMaxRows(Sheet sheet) {
        return isExcel97(sheet)
                ? SpreadsheetVersion.EXCEL97.getMaxRows()
                : SpreadsheetVersion.EXCEL2007.getMaxRows();
    }

    /**
     * Returns maximum number of columns in a spreadsheet.
     *
     * @param workbook Excel workbook
     * @return maximum number of columns in a spreadsheet
     */
    public static int getMaxColumns(Workbook workbook) {
        return isExcel97(workbook)
                ? SpreadsheetVersion.EXCEL97.getMaxColumns()
                : SpreadsheetVersion.EXCEL2007.getMaxColumns();
    }

    /**
     * Returns maximum number of columns in a spreadsheet.
     *
     * @param sheet sheet
     * @return maximum number of columns in a spreadsheet
     */
    public static int getMaxColumns(Sheet sheet) {
        return isExcel97(sheet)
                ? SpreadsheetVersion.EXCEL97.getMaxColumns()
                : SpreadsheetVersion.EXCEL2007.getMaxColumns();
    }

    /**
     * Checks if spreadsheet's version is 97.
     *
     * @param workbook Excel workbook
     * @return whether spreadsheet's version is 97 or not
     */
    public static boolean isExcel97(Workbook workbook) {
        return workbook instanceof HSSFWorkbook;
    }

    /**
     * Checks if spreadsheet's version is 97.
     *
     * @param sheet Excel sheet
     * @return whether spreadsheet's version is 97 or not
     */
    public static boolean isExcel97(Sheet sheet) {
        return sheet instanceof HSSFSheet;
    }

    /**
     * Checks if spreadsheet's version is 97.
     *
     * @param row row
     * @return whether spreadsheet's version is 97 or not
     */
    public static boolean isExcel97(Row row) {
        return row instanceof HSSFRow;
    }

    /**
     * Checks if spreadsheet's version is 97.
     *
     * @param cell cell
     * @return whether spreadsheet's version is 97 or not
     */
    public static boolean isExcel97(Cell cell) {
        return cell instanceof HSSFCell;
    }

    /**
     * Adjusts width of columns to fit the contents.
     *
     * <p> This can be affected by font size and font family.
     * If you want this process well, set up the same font family into all cells.
     * This process will be performed in parallel, but if {@link SXSSFSheet} is, in single.
     *
     * <p> If instance of sheet is {@link SXSSFSheet}, the columns may be
     * inaccurately auto-resized compared to {@link HSSFSheet} and {@link org.apache.poi.xssf.usermodel.XSSFSheet}.
     *
     * @param sheet        Excel sheet
     * @param numOfColumns number of the columns that wanted to make fit contents.
     * @see Sheet#autoSizeColumn(int)
     */
    public static void autoResizeColumns(Sheet sheet, int numOfColumns) {
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            /*
            If use parallel stream, you will see the following error logs.

            java.lang.NullPointerException
                at jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
                at jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
                at jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
                at java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
                at java.lang.reflect.Constructor.newInstance(Constructor.java:481)
                at java.util.concurrent.ForkJoinTask.getThrowableException(ForkJoinTask.java:603)
                at java.util.concurrent.ForkJoinTask.reportException(ForkJoinTask.java:678)
                at java.util.concurrent.ForkJoinTask.invoke(ForkJoinTask.java:737)
                at java.util.stream.ForEachOps$ForEachOp.evaluateParallel(ForEachOps.java:159)
                at java.util.stream.ForEachOps$ForEachOp$OfInt.evaluateParallel(ForEachOps.java:188)
                at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:233)
                at java.util.stream.IntPipeline.forEach(IntPipeline.java:439)
                at java.util.stream.IntPipeline$Head.forEach(IntPipeline.java:596)
             */
            for (int i = 0; i < numOfColumns; i++) {
                sheet.autoSizeColumn(i);
            }
        } else {
            IntStream.range(0, numOfColumns).parallel().forEach(sheet::autoSizeColumn);
        }
    }

    /**
     * Hides extraneous rows.
     *
     * <p> This process will be performed in single-thread.
     * If change this code to be in parallel, this will throw {@link java.util.ConcurrentModificationException}.
     *
     * @param sheet     Excel sheet
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
     * <pre><code>
     *     +------------+----------+
     *     | sequential | parallel |
     *     +------------+----------+
     *     | 15s        | 22s      |
     *     +------------+----------+
     * </code></pre>
     *
     * @param sheet        Excel sheet
     * @param numOfColumns number of the columns that have contents.
     * @see Sheet#setColumnHidden(int, boolean)
     */
    public static void hideExtraColumns(Sheet sheet, int numOfColumns) {
        final int maxColumns = getMaxColumns(sheet);

        for (int i = numOfColumns; i < maxColumns; i++) {
            sheet.setColumnHidden(i, true);
        }
    }

    /**
     * Converts configuration to cell style.
     *
     * @param workbook Excel workbook
     * @param config   configuration of cell style
     * @return cell style | null if config type is {@link NoStyleConfig}
     */
    @Null
    public static CellStyle toCellStyle(Workbook workbook, ExcelStyleConfig config) {
        // To save memory and prevent the number of cell styles in a workbook from increasing,
        // replaces redundant cell style with null.
        if (config.getClass() == NoStyleConfig.class) return null;

        CellStyle cellStyle = workbook.createCellStyle();
        Configurer configurer = new Configurer(cellStyle, workbook.createFont());
        config.configure(configurer);

        return cellStyle;
    }

    /**
     * Converts configurations to cell styles.
     *
     * @param workbook Excel workbook
     * @param configs  configurations of cell style
     * @return cell styles, or null if all instance types of configs are {@link NoStyleConfig}
     * @throws IllegalArgumentException if configs are null, their length is 0, or they contain null
     */
    public static CellStyle[] toCellStyles(Workbook workbook, ExcelStyleConfig... configs) {
        Asserts.that(configs).thrownBy(IllegalArgumentException::new)
                .describedAs("configs is not allowed to be null or empty: {0}", (Object) configs)
                .isNotNull().isNotEmpty()
                .describedAs("configs is not allowed to contain null: {0}", (Object) configs)
                .doesNotContainNull();

        // CellStyle is reusable class, so we use cache
        // to restrain excessive instantiation of that class.
        Map<Class<? extends ExcelStyleConfig>, CellStyle> cache = new HashMap<>();

        CellStyle[] cellStyles = new CellStyle[configs.length];
        for (int i = 0; i < configs.length; i++) {
            ExcelStyleConfig config = configs[i];
            Class<? extends ExcelStyleConfig> configType = config.getClass();

            // When cache hit.
            if (cache.containsKey(configType)) {
                cellStyles[i] = cache.get(configType);
                continue;
            }

            cellStyles[i] = toCellStyle(workbook, config);
            cache.put(configType, cellStyles[i]);
        }

        return cellStyles;
    }

    /**
     * Sets alias for range.
     *
     * <pre><code>
     *     Workbook workbook = new XSSFWorkbook();
     *     Sheet sheet = workbook.createSheet("mySheet");
     *     String ref = sheet.getSheetName() + "!$A$1:$A$2";
     *
     *     setRangeAlias(workbook, "MY_RANGE", ref);
     * </code></pre>
     *
     * @param workbook Excel workbook
     * @param alias    alias for cell range address
     * @param ref      reference for cell range address
     */
    public static void setRangeAlias(Workbook workbook, String alias, String ref) {
        Name name = workbook.createName();
        name.setNameName(alias);
        name.setRefersToFormula(ref);
    }

    /**
     * Converts a reference for cell range address.
     *
     * <pre><code>
     *     Workbook workbook = new XSSFWorkbook();
     *     Sheet sheet = workbook.createSheet("mySheet");
     *     Cell startCell = sheet.createRow(0).createCell(0);
     *     Cell endCell = sheet.createRow(1).createCell(0);
     *
     *     toRangeReference(sheet, startCell, endCell); // mySheet!$A$1:$A$2
     * </code></pre>
     *
     * @param sheet     Excel sheet
     * @param startCell first cell in cell range address
     * @param endCell   last cell in cell range address
     * @return reference for cell range address
     * @throws IllegalArgumentException if end cell precedes start cell.
     */
    public static String toRangeReference(Sheet sheet, Cell startCell, Cell endCell) {
        String startAlphabet = CellReference.convertNumToColString(startCell.getColumnIndex());
        String endAlphabet = CellReference.convertNumToColString(endCell.getColumnIndex());

        int startRownum = startCell.getRowIndex() + 1;
        int endRownum = endCell.getRowIndex() + 1;

        if (startAlphabet.compareTo(endAlphabet) > 0 || startRownum > endRownum) {
            String startCellAddress = startCell.getAddress().formatAsString();
            String endCellAddress = endCell.getAddress().formatAsString();
            throw new IllegalArgumentException("endCell precedes startCell: " + endCellAddress + " > " + startCellAddress);
        }

        return sheet.getSheetName() + "!$" + startAlphabet + '$' + startRownum + ":$" + endAlphabet + '$' + endRownum;
    }

    /**
     * Converts a reference for cell range address.
     *
     * <pre><code>
     *     Workbook workbook = new XSSFWorkbook();
     *     Sheet sheet = workbook.createSheet("mySheet");
     *
     *     toRangeReference(sheet, 0, 0, 0, 1); // mySheet!$A$1:$A$2
     *     toRangeReference(sheet, 2, 1, 5, 4); // mySheet!$C$2:$F$5
     * </code></pre>
     *
     * @param sheet            Excel sheet
     * @param startColumnIndex column index of first cell in cell range address
     * @param startRowIndex    row index of first cell in cell range address
     * @param endColumnIndex   column index of end cell in cell range address
     * @param endRowIndex      row index of end cell in cell range address
     * @return reference for cell range address
     * @throws IllegalArgumentException if end cell's column/row index precedes start cell's
     */
    public static String toRangeReference(Sheet sheet, int startColumnIndex, int startRowIndex, int endColumnIndex, int endRowIndex) {
        String startAlphabet = CellReference.convertNumToColString(startColumnIndex);
        String endAlphabet = CellReference.convertNumToColString(endColumnIndex);

        int startRownum = startRowIndex + 1;
        int endRownum = endRowIndex + 1;

        if (startColumnIndex > endColumnIndex || startRowIndex > endRowIndex) {
            String startCellAddress = startAlphabet + startRownum;
            String endCellAddress = endAlphabet + endRownum;
            throw new IllegalArgumentException("Invalid column/row index for cell range reference: " + endCellAddress + " > " + startCellAddress);
        }

        return sheet.getSheetName() + "!$" + startAlphabet + '$' + startRownum + ":$" + endAlphabet + '$' + endRownum;
    }

    /**
     * Converts a reference for column range address except first row.
     *
     * <pre><code>
     *     Workbook hssfWorkbook = new HSSFWorkbook();
     *     Sheet hssfSheet = hssfWorkbook.createSheet("mySheet");
     *     toRangeReference(hssfSheet, 0); // mySheet!$A$2:$A$65536
     *
     *     Workbook xssfWorkbook = new XSSFWorkbook();
     *     Sheet xssfSheet = xssfWorkbook.createSheet("mySheet");
     *     toRangeReference(xssfSheet, 2); // mySheet!$C$2:$A$1048576
     * </code></pre>
     *
     * @param sheet       Excel sheet
     * @param columnIndex column index for cell range address
     * @return reference for column range address except first row
     * @throws IllegalArgumentException if column index is greater than max column index of the sheet
     */
    public static String toColumnRangeReference(Sheet sheet, int columnIndex) {
        int maxColumnIndex = getMaxColumns(sheet) - 1;
        if (columnIndex > maxColumnIndex) {
            throw new IllegalArgumentException("Column index exceeds max column index: " + columnIndex + " > " + maxColumnIndex);
        }

        int maxRowIndex = getMaxRows(sheet) - 1;
        return toRangeReference(sheet, columnIndex, 1, columnIndex, maxRowIndex);
    }

    /**
     * Sets a validation to the cells on the reference.
     *
     * <pre><code>
     *     Workbook workbook = new XSSFWorkbook();
     *     Sheet sheet = workbook.createSheet("mySheet");
     *     DataValidationHelper helper = sheet.getDataValidationHelper();
     *     String ref = toRangeReference(sheet, 2);
     *
     *     setValidation(sheet, helper, ref, "RED", "GREEN", "BLUE");
     * </code></pre>
     *
     * @param sheet  Excel sheet
     * @param helper data validation helper
     * @param ref    reference for cell range address
     * @param values constraint values
     * @see Sheet#getDataValidationHelper()
     */
    public static void setValidation(Sheet sheet, DataValidationHelper helper, String ref, String... values) {
        // Creates a reference.
        CellRangeAddressList ranges = new CellRangeAddressList();
        CellRangeAddress range = CellRangeAddress.valueOf(ref);
        ranges.addCellRangeAddress(range);

        // Value of cell on the reference is restricted to the given values only.
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        DataValidation validation = helper.createValidation(constraint, ranges);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    /**
     * Returns the number of declared cell styles.
     *
     * @param workbook Excel workbook
     * @return number of declared cell styles
     */
    public static int getNumOfDeclaredCellStyles(Workbook workbook) {
        int numCellStyles = workbook.getNumCellStyles();
        return numCellStyles - getNumOfInitialCellStyles(workbook);
    }

    /**
     * Returns the number of initial cell styles.
     *
     * @param workbook Excel workbook
     * @return the number of initial cell styles
     */
    public static int getNumOfInitialCellStyles(Workbook workbook) {
        return isExcel97(workbook) ? 21 : 1;
    }

    /**
     * Returns the declared cell styles.
     *
     * @param workbook Excel workbook
     * @return the declared cell styles
     */
    public static List<CellStyle> getDeclaredCellStyles(Workbook workbook) {
        int initial = getNumOfInitialCellStyles(workbook);
        int declared = getNumOfDeclaredCellStyles(workbook);

        List<CellStyle> cellStyles = new ArrayList<>(declared);
        for (int i = initial; i < initial + declared; i++) {
            CellStyle cellStyle = workbook.getCellStyleAt(i);
            cellStyles.add(cellStyle);
        }

        return cellStyles;
    }

    /**
     * Returns a font on cell style in workbook.
     *
     * @param workbook Excel workbook
     * @param style    cell style
     * @return font on cell style
     */
    public static Font getFontFromCellStyle(Workbook workbook, CellStyle style) {
        if (isExcel97(workbook)) {
            return ((HSSFCellStyle) style).getFont(workbook);
        } else {
            return ((XSSFCellStyle) style).getFont();
        }
    }

    /**
     * Returns whether one cell style/font is equal to the other.
     *
     * @param workbook      Excel workbook
     * @param style         cell style
     * @param otherWorkbook other Excel workbook
     * @param otherStyle    other cell style
     * @return whether one cell style/font is equal to the other
     */
    public static boolean equalsCellStyleAndFont(Workbook workbook, CellStyle style,
                                                 Workbook otherWorkbook, CellStyle otherStyle) {
        Font font = getFontFromCellStyle(workbook, style);
        Font otherFont = getFontFromCellStyle(otherWorkbook, otherStyle);
        return equalsCellStyle(style, otherStyle) && equalsFont(font, otherFont);
    }

    /**
     * Returns whether one cell style is equal to the other.
     *
     * @param style cell style
     * @param other other cell style
     * @return whether one cell style is equal to the other
     */
    public static boolean equalsCellStyle(CellStyle style, CellStyle other) {
        if (style == null || other == null) return false;
        if (style == other) return true;

        boolean alignment = style.getAlignment() == other.getAlignment();
        boolean background = style.getFillForegroundColor() == other.getFillForegroundColor();
        boolean pattern = style.getFillPattern() == other.getFillPattern();

        boolean borderTop = style.getBorderTop() == other.getBorderTop();
        boolean borderRight = style.getBorderRight() == other.getBorderRight();
        boolean borderBottom = style.getBorderBottom() == other.getBorderBottom();
        boolean borderLeft = style.getBorderLeft() == other.getBorderLeft();
        boolean borderStyle = borderTop && borderRight && borderBottom && borderLeft;

        boolean topBorderColor = style.getTopBorderColor() == other.getTopBorderColor();
        boolean rightBorderColor = style.getRightBorderColor() == other.getRightBorderColor();
        boolean bottomBorderColor = style.getBottomBorderColor() == other.getBottomBorderColor();
        boolean leftBorderColor = style.getLeftBorderColor() == other.getLeftBorderColor();
        boolean borderColor = topBorderColor && rightBorderColor && bottomBorderColor && leftBorderColor;

        return alignment && background && pattern && borderStyle && borderColor;
    }

    /**
     * Returns whether one font is equal to the other.
     *
     * @param font  font
     * @param other other font
     * @return whether one font is equal to the other
     */
    public static boolean equalsFont(Font font, Font other) {
        if (font == null || other == null) return false;
        if (font == other) return true;

        boolean name = Objects.equals(font.getFontName(), other.getFontName());
        boolean size = font.getFontHeightInPoints() == other.getFontHeightInPoints();
        boolean color = font.getColor() == other.getColor();
        boolean bold = font.getBold() == other.getBold();
        boolean italic = font.getItalic() == other.getItalic();
        boolean strikeout = font.getStrikeout() == other.getStrikeout();
        boolean underline = font.getUnderline() == other.getUnderline();
        boolean offset = font.getTypeOffset() == other.getTypeOffset();

        return name && size && color && bold && italic && strikeout && underline && offset;
    }

}
