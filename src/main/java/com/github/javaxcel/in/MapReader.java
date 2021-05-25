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

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel reader for {@link Map}.
 *
 * @param <W> excel workbook
 * @param <T> {@link Map}
 */
public class MapReader<W extends Workbook, T extends Map<String, ?>> extends AbstractExcelReader<W, T> {

    private int numOfColumns;

    private final List<String> headerNames = new ArrayList<>();

    /**
     * @param workbook excel workbook
     * @see com.github.javaxcel.factory.ExcelReaderFactory#create(Workbook)
     */
    public MapReader(W workbook) {
        super(workbook);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapReader}
     */
    @Override
    public MapReader<W, T> limit(int limit) {
        super.limit(limit);
        return this;
    }

    public MapReader<W, T> headerNames(List<String> headerNames) {
        Asserts.that(headerNames)
                .as("Header names cannot be null or empty")
                .isNotNull().hasElement();

        // Replaces current header names with the new things.
        this.headerNames.clear();
        this.headerNames.addAll(headerNames);

        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    @Override
    @SuppressWarnings("unchecked")
    protected List<T> readSheet(Sheet sheet) {
        return (List<T>) readSheetAsMaps(sheet);
    }

    @Override
    protected void beforeReadModels(Sheet sheet) {
        Row header = sheet.getRow(0);
        this.numOfColumns = header.getPhysicalNumberOfCells();

        if (!this.headerNames.isEmpty()) return;

        // If header names is empty, sets first row's values to it.
        for (int i = 0; i < this.numOfColumns; i++) {
            Cell cell = header.getCell(i);

            // If cell value in first row is empty, sets stringified column number.
            String headerName = StringUtils.ifNullOrEmpty(cell.getStringCellValue(), String.valueOf(i));
            this.headerNames.add(headerName);
        }
    }

    @Override
    protected int getNumOfColumns(Row row) {
        return this.numOfColumns;
    }

    @Override
    protected String getColumnName(Cell cell, int columnIndex) {
        return this.headerNames.get(columnIndex);
    }

}
