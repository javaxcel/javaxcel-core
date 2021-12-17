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

package com.github.javaxcel.out.core.impl;

import com.github.javaxcel.out.context.ExcelWriteContext;
import com.github.javaxcel.out.core.AbstractExcelWriter;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.*;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unchecked")
public class MapWriter extends AbstractExcelWriter<Map<String, Object>> {

    private static final Class<Map<String, Object>> MAP_TYPE;

    private List<String> keys;

    private List<String> headerNames;

    /**
     * Default column value when the value is null or empty.
     */
    private String defaultValue;

    static {
        try {
            MAP_TYPE = (Class<Map<String, Object>>) Class.forName(Map.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MapWriter(Workbook workbook) throws ClassNotFoundException {
        // incompatible types: java.lang.Class<java.util.Map> cannot be converted to java.lang.Class<T>
        super(workbook, MAP_TYPE);
    }

    @Override
    public void prepare(ExcelWriteContext<Map<String, Object>> context) {
        setKeys(context);
        changeKeys(context);
        setDefaultValue(context);
        setHeaderStyles(context);
        setBodyStyles(context);
    }

    private void setKeys(ExcelWriteContext<Map<String, Object>> context) {
        List<Map<String, Object>> list = context.getList();

        // Gets the keys of all maps.
        this.keys = list.stream().flatMap(it -> it.keySet().stream()).distinct().collect(toList());

        // To write a header, this doesn't allow accepting invalid keys.
        Asserts.that(this.keys)
                .as("MapWriter.keys is not allowed to be empty")
                .hasElement()
                .as("MapWriter.keys cannot have null or blank element: {0}", this.keys)
                .predicate(them -> them.stream().noneMatch(StringUtils::isNullOrBlank))
                .as("MapWriter.keys cannot have duplicated elements: {0}", this.keys)
                .predicate(them -> them.stream().noneMatch(it -> Collections.frequency(them, it) > 1));
    }

    private void changeKeys(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(KeyNames.class);
        if (strategy == null) return;

        Map<String, Object> keyMap = (Map<String, Object>) strategy.execute(context);
        Map<String, Integer> orders = (Map<String, Integer>) keyMap.get("orders");

        // Validates the number of ordered keys and their each element.
        Asserts.that(this.keys)
                .as("MapWriter.keys is not equal to keyMap.orders.size (keys.size: {0}, keyMap.orders.size: {1})",
                        this.keys.size(), orders.size())
                .hasSizeOf(orders.size())
                .as("MapWriter.keys is at variance with keyMap.orders.keySet (keys: {0}, keyMap.orders.keySet: {1})",
                        this.keys, orders.keySet())
                .containsAll(orders.keySet());

        if (keyMap.containsKey("names")) this.headerNames = (List<String>) keyMap.get("names");

        // Rearranges the keys as you want: it changes order of columns.
        this.keys.sort(comparing(orders::get));
    }

    private void setDefaultValue(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(DefaultValue.class);
        if (strategy == null) return;

        this.defaultValue = (String) strategy.execute(context);
    }

    private void setHeaderStyles(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderStyles.class);
        if (strategy == null) return;

        List<ExcelStyleConfig> headerStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

        // Validates header styles.
        Asserts.that(headerStyleConfigs)
                .as("headerStyles.size must be 1 or equal to keys.size (headerStyles.size: {0}, keys.size: {1})",
                        headerStyleConfigs.size(), this.keys.size())
                .predicate(them -> them.size() == 1 || them.size() == this.keys.size());

        ExcelStyleConfig[] headerConfigs = headerStyleConfigs.toArray(new ExcelStyleConfig[0]);
        CellStyle[] headerStyles = ExcelUtils.toCellStyles(context.getWorkbook(), headerConfigs);
        context.setHeaderStyles(Arrays.asList(headerStyles));
    }

    private void setBodyStyles(ExcelWriteContext<Map<String, Object>> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(BodyStyles.class);
        if (strategy == null) return;

        List<ExcelStyleConfig> bodyStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

        // Validates body styles.
        Asserts.that(bodyStyleConfigs)
                .as("bodyStyles.size must be 1 or equal to keys.size (bodyStyles.size: {0}, keys.size: {1})",
                        bodyStyleConfigs.size(), this.keys.size())
                .predicate(them -> them.size() == 1 || them.size() == this.keys.size());

        ExcelStyleConfig[] bodyConfigs = bodyStyleConfigs.toArray(new ExcelStyleConfig[0]);
        CellStyle[] bodyStyles = ExcelUtils.toCellStyles(context.getWorkbook(), bodyConfigs);
        context.setBodyStyles(Arrays.asList(bodyStyles));
    }

    @Override
    public void preWriteSheet(ExcelWriteContext<Map<String, Object>> context) {
        if (context.getStrategyMap().containsKey(Filter.class)) {
            Sheet sheet = context.getSheet();
            String ref = ExcelUtils.toRangeReference(sheet, 0, 0, this.keys.size() - 1, context.getChunk().size() - 1);
            sheet.setAutoFilter(CellRangeAddress.valueOf(ref));
        }
    }

    @Override
    protected void createHeader(ExcelWriteContext<Map<String, Object>> context) {
        // Creates the first row that is header.
        Row row = context.getSheet().createRow(0);

        List<String> headerNames = this.headerNames == null ? this.keys : this.headerNames;
        List<CellStyle> headerStyles = context.getHeaderStyles();

        // Names the header given values.
        final int numOfHeaders = headerNames.size();
        for (int i = 0; i < numOfHeaders; i++) {
            String headerName = headerNames.get(i);

            Cell cell = row.createCell(i);
            cell.setCellValue(headerName);

            if (CollectionUtils.isNullOrEmpty(headerStyles)) continue;

            // Sets common style to all header cells or each style to each header cell.
            CellStyle headerStyle = headerStyles.size() == 1
                    ? headerStyles.get(0) : headerStyles.get(i);

            // There is possibility that headerStyles has null elements, if you set NoStyleConfig.
            if (headerStyle != null) cell.setCellStyle(headerStyle);
        }
    }

    @Override
    protected void createBody(ExcelWriteContext<Map<String, Object>> context) {
        Sheet sheet = context.getSheet();
        List<Map<String, Object>> list = context.getChunk();
        List<CellStyle> bodyStyles = context.getBodyStyles();

        final int numOfMaps = list.size();
        final int numOfKeys = this.keys.size();

        for (int i = 0; i < numOfMaps; i++) {
            Map<String, Object> map = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfKeys; j++) {
                Object value = map.get(this.keys.get(j));
                Cell cell = row.createCell(j);

                // Not allows empty string to be written.
                if (value != null && !"".equals(value)) {
                    cell.setCellValue(value.toString());
                } else if (this.defaultValue != null) {
                    cell.setCellValue(this.defaultValue);
                }

                if (CollectionUtils.isNullOrEmpty(bodyStyles)) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = bodyStyles.size() == 1
                        ? bodyStyles.get(0) : bodyStyles.get(j);

                // There is possibility that bodyStyles has null elements, if you set NoStyleConfig.
                if (bodyStyle != null) cell.setCellStyle(bodyStyle);
            }
        }
    }

    @Override
    public void postWriteSheet(ExcelWriteContext<Map<String, Object>> context) {
        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap = context.getStrategyMap();
        Sheet sheet = context.getSheet();

        // Adjusts rows and columns.
        if (strategyMap.containsKey(AutoResizedColumns.class)) {
            ExcelUtils.autoResizeColumns(sheet, this.keys.size());
        }
        if (strategyMap.containsKey(HiddenExtraRows.class)) {
            ExcelUtils.hideExtraRows(sheet, context.getChunk().size() + 1);
        }
        if (strategyMap.containsKey(HiddenExtraColumns.class)) {
            ExcelUtils.hideExtraColumns(sheet, this.keys.size());
        }
    }

}
