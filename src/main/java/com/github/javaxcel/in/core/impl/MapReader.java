/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.in.core.impl;

import com.github.javaxcel.in.context.ExcelReadContext;
import com.github.javaxcel.in.core.AbstractExcelReader;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel reader for {@link Map}.
 */
@SuppressWarnings("unchecked")
public class MapReader extends AbstractExcelReader<Map<String, Object>> {

    private static final Class<Map<String, Object>> MAP_TYPE;

    private final List<String> headerNames = new ArrayList<>();

    static {
        try {
            // Compiler doesn't allow instance of the class java.util.Map to generic variable
            // defined by class. To solve the problem, we use the method Class.forName(String).
            // This is the compile error message.
            // incompatible types: java.lang.Class<java.util.Map> cannot be converted to java.lang.Class<T>
            MAP_TYPE = (Class<Map<String, Object>>) Class.forName(Map.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param workbook Excel workbook
     */
    public MapReader(Workbook workbook) {
        super(workbook, MAP_TYPE);
    }

    @Override
    protected List<String> readHeader(ExcelReadContext<Map<String, Object>> context) {
        // If header names is empty, sets first row's values to it.
        List<String> headerNames = new ArrayList<>();
        for (Row header : context.getSheet()) {
            int numOfColumns = header.getLastCellNum();

            for (int i = 0; i < numOfColumns; i++) {
                Cell cell = header.getCell(i);
                String cellValue = cell == null ? null : cell.getStringCellValue();

                // If cell value in first row is empty, sets stringified column number.
                String headerName = StringUtils.ifNullOrEmpty(cellValue, String.valueOf(i));
                headerNames.add(headerName);
            }

            // Reads only the first row.
            break;
        }

        return headerNames;
    }

    @Override
    protected List<Map<String, Object>> readBody(ExcelReadContext<Map<String, Object>> context) {
        return super.readBodyAsMaps(context.getSheet());
    }

}
