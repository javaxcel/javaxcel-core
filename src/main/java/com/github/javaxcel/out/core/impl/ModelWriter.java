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

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.converter.out.factory.OutputConverterFactory;
import com.github.javaxcel.converter.out.support.OutputConverterSupport;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.out.context.ExcelWriteContext;
import com.github.javaxcel.out.core.AbstractExcelWriter;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.*;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Filter;
import java.util.stream.IntStream;

/**
 * Excel writer with model
 *
 * @param <T> model type
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ModelWriter<T> extends AbstractExcelWriter<T> {

    /**
     * The type's fields that will be actually written in Excel file.
     */
    private final List<Field> fields;

    private final OutputConverterSupport<T> converter;

    private Map<Integer, String[]> enumDropdownMap;

    public ModelWriter(Workbook workbook, Class<T> type, OutputConverterFactory factory) {
        super(workbook, type);

        // Finds targeted fields.
        this.fields = FieldUtils.getTargetedFields(type);
        Asserts.that(this.fields)
                .as("ModelWriter.fields cannot find the targeted fields in the class: {0}", type.getName())
                .exception(desc -> new NoTargetedFieldException(type, desc))
                .hasElement()
                .as("ModelWriter.fields cannot have null element: {0}", this.fields)
                .doesNotContainNull();

        this.converter = new OutputConverterSupport<>(fields, factory);
    }

    @Override
    public void prepare(ExcelWriteContext<T> context) {
        setEnumDropdowns(context);
        setDefaultValue(context);
        setHeaderStyles(context);
        setBodyStyles(context);
    }

    private void setEnumDropdowns(ExcelWriteContext<T> context) {
        Map<Integer, String[]> enumDropdownMap = new HashMap<>();

        boolean enabled = context.getStrategyMap().containsKey(EnumDropdown.class);
        if (!enabled) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);
            enabled = excelModel != null && excelModel.enumDropdown();
        }

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            if (!field.getType().isEnum()) continue;

            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            enabled |= excelColumn != null && excelColumn.enumDropdown();
            if (!enabled) continue;

            String[] dropdowns;
            if (excelColumn != null && excelColumn.dropdownItems().length > 0) {
                // Sets custom dropdown items for enum.
                dropdowns = excelColumn.dropdownItems();

            } else {
                // Sets default dropdown items for enum.
                Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
                dropdowns = (String[]) EnumSet.allOf(enumType).stream()
                        .map(e -> ((Enum) e).name()).toArray(String[]::new);
            }

            enumDropdownMap.put(i, dropdowns);
        }

        // Assigns null if map is empty.
        if (!enumDropdownMap.isEmpty()) this.enumDropdownMap = enumDropdownMap;
    }

    private void setDefaultValue(ExcelWriteContext<T> context) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(DefaultValue.class);
        if (strategy == null) return;

        this.converter.setDefaultValue((String) strategy.execute(context));
    }

    private void setHeaderStyles(ExcelWriteContext<T> context) {
        Workbook workbook = context.getWorkbook();
        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderStyles.class);

        // Sets configurations for header styles by ExcelStyleConfig.
        if (strategy != null) {
            List<ExcelStyleConfig> headerStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

            // Validates header styles.
            Asserts.that(headerStyleConfigs)
                    .as("headerStyles.size must be 1 or equal to fields.size (headerStyles.size: {0}, fields.size: {1})",
                            headerStyleConfigs.size(), this.fields.size())
                    .predicate(them -> them.size() == 1 || them.size() == this.fields.size());

            ExcelStyleConfig[] headerConfigs = headerStyleConfigs.toArray(new ExcelStyleConfig[0]);
            CellStyle[] headerStyles = ExcelUtils.toCellStyles(workbook, headerConfigs);
            context.setHeaderStyles(Arrays.asList(headerStyles));
            return;
        }

        // Sets configurations for header styles by ExcelModel.
        if (context.getModelType().isAnnotationPresent(ExcelModel.class)) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);

            // Sets cell style for header.
            ExcelStyleConfig headerConfig = excelModel.headerStyle() == NoStyleConfig.class
                    ? DEFAULT_STYLE_CONFIG : ReflectionUtils.instantiate(excelModel.headerStyle());
            ExcelStyleConfig[] headerConfigs = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> headerConfig).toArray(ExcelStyleConfig[]::new);

            CellStyle[] headerStyles = ExcelUtils.toCellStyles(workbook, headerConfigs);
            context.setHeaderStyles(Arrays.asList(headerStyles));
        }

        // Unless configure header styles with ExcelModel, creates empty arrays.
        List<CellStyle> headerStyles = CollectionUtils.ifNullOrEmpty(context.getHeaderStyles(), new ArrayList<>(this.fields.size()));

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if (excelColumn == null) continue; // Null means that not overrides style on the column.

            // Replaces header style of ExcelModel with header style of ExcelColumn.
            Class<? extends ExcelStyleConfig> headerConfigType = excelColumn.headerStyle();
            if (headerConfigType == NoStyleConfig.class) continue;

            ExcelStyleConfig headerConfig = ReflectionUtils.instantiate(headerConfigType);
            if (headerStyles.size() - 1 >= i) {
                headerStyles.set(i, ExcelUtils.toCellStyle(workbook, headerConfig));
            } else {
                headerStyles.add(i, ExcelUtils.toCellStyle(workbook, headerConfig));
            }
        }

        if (!headerStyles.isEmpty()) context.setHeaderStyles(headerStyles);
    }

    private void setBodyStyles(ExcelWriteContext<T> context) {
        Workbook workbook = context.getWorkbook();
        ExcelWriteStrategy strategy = context.getStrategyMap().get(BodyStyles.class);

        // Sets configurations for body styles by ExcelStyleConfig.
        if (strategy != null) {
            List<ExcelStyleConfig> bodyStyleConfigs = (List<ExcelStyleConfig>) strategy.execute(context);

            // Validates body styles.
            Asserts.that(bodyStyleConfigs)
                    .as("bodyStyles.size must be 1 or equal to fields.size (bodyStyles.size: {0}, fields.size: {1})",
                            bodyStyleConfigs.size(), this.fields.size())
                    .predicate(them -> them.size() == 1 || them.size() == this.fields.size());

            ExcelStyleConfig[] bodyConfigs = bodyStyleConfigs.toArray(new ExcelStyleConfig[0]);
            CellStyle[] bodyStyles = ExcelUtils.toCellStyles(workbook, bodyConfigs);
            context.setBodyStyles(Arrays.asList(bodyStyles));
            return;
        }

        // Sets configurations for body styles by ExcelModel.
        if (context.getModelType().isAnnotationPresent(ExcelModel.class)) {
            ExcelModel excelModel = context.getModelType().getAnnotation(ExcelModel.class);

            // Sets cell style for body.
            ExcelStyleConfig bodyConfig = excelModel.bodyStyle() == NoStyleConfig.class
                    ? DEFAULT_STYLE_CONFIG : ReflectionUtils.instantiate(excelModel.bodyStyle());
            ExcelStyleConfig[] bodyConfigs = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> bodyConfig).toArray(ExcelStyleConfig[]::new);

            CellStyle[] bodyStyles = ExcelUtils.toCellStyles(workbook, bodyConfigs);
            context.setBodyStyles(Arrays.asList(bodyStyles));
        }

        // Unless configure body styles with ExcelModel, creates empty arrays.
        List<CellStyle> bodyStyles = CollectionUtils.ifNullOrEmpty(context.getBodyStyles(), new ArrayList<>(this.fields.size()));

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if (excelColumn == null) continue; // Null means that not overrides style on the column.

            // Replaces body style of ExcelModel with body style of ExcelColumn.
            Class<? extends ExcelStyleConfig> bodyConfigType = excelColumn.bodyStyle();
            if (bodyConfigType == NoStyleConfig.class) continue;

            ExcelStyleConfig bodyConfig = ReflectionUtils.instantiate(bodyConfigType);
            if (bodyStyles.size() - 1 >= i) {
                bodyStyles.set(i, ExcelUtils.toCellStyle(workbook, bodyConfig));
            } else {
                bodyStyles.add(i, ExcelUtils.toCellStyle(workbook, bodyConfig));
            }
        }

        if (!bodyStyles.isEmpty()) context.setBodyStyles(bodyStyles);
    }

    @Override
    public void preWriteSheet(ExcelWriteContext<T> context) {
        if (!context.getStrategyMap().containsKey(Filter.class)) return;

        Sheet sheet = context.getSheet();
        String ref = ExcelUtils.toRangeReference(sheet, 0, 0, this.fields.size() - 1, context.getChunk().size() - 1);
        sheet.setAutoFilter(CellRangeAddress.valueOf(ref));
    }

    @Override
    protected void createHeader(ExcelWriteContext<T> context) {
        // Creates the first row that is header.
        Row row = context.getSheet().createRow(0);

        ExcelWriteStrategy strategy = context.getStrategyMap().get(HeaderNames.class);
        List<String> headerNames = strategy == null ? FieldUtils.toHeaderNames(this.fields)
                : (List<String>) strategy.execute(context);

        Asserts.that(headerNames)
                .as("headerNames is not allowed to be null or empty: {0}", headerNames)
                .isNotNull().hasElement()
                .as("headerNames.size is not equal to the number of targeted fields in the class: {0}",
                        context.getModelType().getName())
                .isSameSize(this.fields);

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
    protected void createBody(ExcelWriteContext<T> context) {
        Sheet sheet = context.getSheet();

        // Creates constraint for columns of enum.
        if (this.enumDropdownMap != null) createDropdowns(sheet);

        List<T> list = context.getChunk();
        List<CellStyle> bodyStyles = context.getBodyStyles();
        final int numOfModels = list.size();
        final int numOfFields = this.fields.size();

        for (int i = 0; i < numOfModels; i++) {
            T model = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfFields; j++) {
                Field field = this.fields.get(j);
                Cell cell = row.createCell(j);

                // Converts field value to the string.
                String value = this.converter.convert(model, field);
                if (value != null) cell.setCellValue(value);

                if (CollectionUtils.isNullOrEmpty(bodyStyles)) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = bodyStyles.size() == 1
                        ? bodyStyles.get(0) : bodyStyles.get(j);

                // There is possibility that bodyStyles has null elements, if you set NoStyleConfig.
                if (bodyStyle != null) cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * Creates dropdowns for columns of {@link Enum}.
     */
    private void createDropdowns(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();

        this.enumDropdownMap.forEach((i, them) -> {
            // Creates reference of the column range except first row.
            String ref = ExcelUtils.toColumnRangeReference(sheet, i);

            // Sets validation with the dropdown items at the reference.
            ExcelUtils.setValidation(sheet, helper, ref, them);
        });
    }

    @Override
    public void postWriteSheet(ExcelWriteContext<T> context) {
        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap = context.getStrategyMap();
        Sheet sheet = context.getSheet();

        // Adjusts rows and columns.
        if (strategyMap.containsKey(AutoResizedColumns.class)) {
            ExcelUtils.autoResizeColumns(sheet, this.fields.size());
        }
        if (strategyMap.containsKey(HiddenExtraRows.class)) {
            ExcelUtils.hideExtraRows(sheet, context.getChunk().size() + 1);
        }
        if (strategyMap.containsKey(HiddenExtraColumns.class)) {
            ExcelUtils.hideExtraColumns(sheet, this.fields.size());
        }
    }

}
