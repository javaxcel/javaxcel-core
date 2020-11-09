package com.github.javaxcel.in;

import com.github.javaxcel.converter.in.BasicReadingConverter;
import com.github.javaxcel.util.ExcelUtils;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract excel reader
 */
public abstract class AbstractExcelReader<W extends Workbook, T> implements ExcelReader<W, T> {

    /**
     * Formatter that stringifies the value in a cell with {@link FormulaEvaluator}.
     *
     * @see #readRow(Row)
     */
    protected static final DataFormatter dataFormatter = new DataFormatter();

    /**
     * Apache POI workbook.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
     */
    protected final W workbook;

    /**
     * Evaluator that evaluates the formula in a cell.
     *
     * @see Workbook#getCreationHelper()
     * @see CreationHelper#createFormulaEvaluator()
     * @see #readRow(Row)
     */
    protected final FormulaEvaluator formulaEvaluator;

    /**
     * Limitation of reading rows.
     *
     * @see #limit(int)
     */
    protected int limit = -1;

    /**
     * Total number of rows read by {@link AbstractExcelReader}.
     *
     * @see #limit(int)
     */
    protected int numOfRowsRead;

    protected AbstractExcelReader(W workbook) {
        this.workbook = workbook;
        this.formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link AbstractExcelReader}
     */
    @Override
    public AbstractExcelReader<W, T> limit(int limit) {
        if (limit < 0) throw new IllegalArgumentException("Limit cannot be negative");

        this.limit = limit;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<T> read() {
        List<T> list = new ArrayList<>();

        beforeRead(list);

        List<Sheet> sheets = ExcelUtils.getSheets(this.workbook);
        for (Sheet sheet : sheets) {
            if (this.numOfRowsRead == this.limit) break;
            list.addAll(readSheet(sheet));
        }

        afterRead(list);

        return list;
    }

    /**
     * Converts a row to a simulated model.
     *
     * <p> Reads rows to get data. this creates {@link Map} as a simulated model
     * and puts the key({@link Field#getName()}) and the value
     * ({@link DataFormatter#formatCellValue(Cell, FormulaEvaluator)})
     * to the model. The result is the same as the following code.
     *
     * <pre>{@code
     *     +------+--------+--------+----------+
     *     | name | height | weight | eyesight |
     *     +------+--------+--------+----------+
     *     | John | 180.5  | 79.2   |          |
     *     +------+--------+--------+----------+
     *
     *     This row will be converted to
     *
     *     { "name": "John", "height": "180.5", "weight": "79.2", "eyesight": null }
     * }</pre>
     *
     * @param row row in sheet
     * @return simulated model
     */
    protected Map<String, Object> readRow(Row row) {
        Map<String, Object> map = new HashMap<>();

        int numOfColumns = getNumOfColumns(row);
        for (int i = 0; i < numOfColumns; i++) {
            // If the cell is null, creates an empty cell.
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            // Evaluates the formula and returns a stringifed value.
            String cellValue = dataFormatter.formatCellValue(cell, this.formulaEvaluator);

            // Converts empty string to null because when CellType is BLANK, DataFormatter returns empty string.
            map.put(getColumnName(cell, i), cellValue.equals("") ? null : cellValue);
        }

        // Increase count of reading row.
        numOfRowsRead++;

        return map;
    }

    protected void beforeRead(List<T> list) {
    }

    protected void afterRead(List<T> list) {
    }

    protected abstract List<T> readSheet(Sheet sheet);

    protected abstract int getNumOfColumns(Row row);

    protected abstract String getColumnName(Cell cell, int columnIndex);

}
