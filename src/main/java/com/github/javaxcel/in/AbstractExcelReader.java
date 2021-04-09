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

package com.github.javaxcel.in;

import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract excel reader
 */
public abstract class AbstractExcelReader<W extends Workbook, T> implements ExcelReader<T> {

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
     * Total number of models read by {@link AbstractExcelReader}.
     *
     * @see #limit(int)
     */
    protected int numOfModelsRead;

    protected AbstractExcelReader(W workbook) {
        this.workbook = workbook;
        this.formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
    }

    /**
     * Limits the number of models.
     *
     * @param limit limit for the number of models
     * @return {@link AbstractExcelReader}
     */
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

        // When this gets a cell and it is null, creates an empty cell.
        this.workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        List<Sheet> sheets = ExcelUtils.getSheets(this.workbook);
        for (Sheet sheet : sheets) {
            if (this.numOfModelsRead == this.limit) break;
            list.addAll(readSheet(sheet));
        }

        afterRead(list);

        return list;
    }

    protected final int getNumOfModels(Sheet sheet) {
        int numOfModels = ExcelUtils.getNumOfModels(sheet);
        if (this.limit >= 0) numOfModels = Math.min(this.limit, numOfModels);

        return numOfModels;
    }

    /**
     * Gets models read as map from a sheet.
     *
     * @param sheet excel sheet
     * @return models read as map
     */
    protected final List<Map<String, Object>> readSheetAsMaps(Sheet sheet) {
        beforeReadModels(sheet);

        final int numOfModels = getNumOfModels(sheet);

        // Reads rows.
        List<Map<String, Object>> maps = new ArrayList<>();
        for (int i = 0; i < numOfModels; i++) {
            if (this.numOfModelsRead == this.limit) break;

            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            // Adds a row data of the sheet.
            maps.add(readRow(row));
        }

        return maps;
    }

    /**
     * Converts a row to a imitated model.
     *
     * <p> Reads rows to get data. this creates {@link Map} as a imitated model
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
     * @return imitated model
     */
    protected final Map<String, Object> readRow(Row row) {
        Map<String, Object> map = new HashMap<>();

        int numOfColumns = getNumOfColumns(row);
        for (int i = 0; i < numOfColumns; i++) {
            Cell cell = row.getCell(i);

            // Evaluates the formula and returns a stringifed value.
            String cellValue = dataFormatter.formatCellValue(cell, this.formulaEvaluator);

            // Converts empty string to null because when CellType is BLANK, DataFormatter returns empty string.
            map.put(getColumnName(cell, i), StringUtils.ifNullOrEmpty(cellValue, (String) null));
        }

        // Increases count of model read.
        numOfModelsRead++;

        return map;
    }

    protected void beforeRead(List<T> list) {
    }

    protected void afterRead(List<T> list) {
    }

    protected abstract List<T> readSheet(Sheet sheet);

    protected void beforeReadModels(Sheet sheet) {
    }

    protected abstract int getNumOfColumns(Row row);

    protected abstract String getColumnName(Cell cell, int columnIndex);

}
