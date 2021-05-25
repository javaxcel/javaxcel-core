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

package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.converter.out.support.OutputConverterSupport;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.usermodel.*;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Excel writer
 *
 * @param <W> excel workbook
 * @param <T> type of model
 */
public class ModelWriter<W extends Workbook, T> extends AbstractExcelWriter<W, T> {

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    private final OutputConverterSupport<T> converter;

    /**
     * @see #enumDropdown()
     */
    private boolean enableDropdown;

    /**
     * @see #beforeWrite(OutputStream, List)
     */
    private Map<Integer, String[]> enumDropdownItemsMap;

    /**
     * @see com.github.javaxcel.factory.ExcelWriterFactory#create(Workbook, Class)
     */
    public ModelWriter(W workbook, Class<T> type) {
        super(workbook);

        Asserts.that(type)
                .as("Type is not allowed to be null")
                .isNotNull();
        this.type = type;

        // Finds targeted fields.
        this.fields = FieldUtils.getTargetedFields(this.type);
        Asserts.that(this.fields)
                .as("Cannot find the targeted fields in the class({0})", this.type.getName())
                .exception(desc -> new NoTargetedFieldException(this.type, desc))
                .hasElement();

        this.converter = new OutputConverterSupport<>(fields);

        ExcelModel excelModel = type.getAnnotation(ExcelModel.class);
        if (excelModel != null) {
            // Sets configurations for header and body style by 'ExcelModel'.
            setStylesByModel(excelModel);
        }

        // Sets configurations for header and body style by 'ExcelColumn'.
        setStylesByColumns();
    }

    private void setStylesByModel(ExcelModel excelModel) {
        // Sets configurations for header style.
        ExcelStyleConfig headerConfig = FieldUtils.instantiate(excelModel.headerStyle());
        if (!(headerConfig instanceof NoStyleConfig)) {
            CellStyle headerStyle = ExcelUtils.toCellStyle(this.workbook, headerConfig);
            this.headerStyles = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> headerStyle).toArray(CellStyle[]::new);
        }

        // Sets configurations for body style.
        ExcelStyleConfig bodyConfig = FieldUtils.instantiate(excelModel.bodyStyle());
        if (!(bodyConfig instanceof NoStyleConfig)) {
            CellStyle bodyStyle = ExcelUtils.toCellStyle(this.workbook, bodyConfig);
            this.bodyStyles = IntStream.range(0, this.fields.size())
                    .mapToObj(i -> bodyStyle).toArray(CellStyle[]::new);
        }
    }

    private void setStylesByColumns() {
        // Unless configure header/body style with 'ExcelModel', creates empty arrays.
        if (this.headerStyles == null) this.headerStyles = new CellStyle[this.fields.size()];
        if (this.bodyStyles == null) this.bodyStyles = new CellStyle[this.fields.size()];

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);

            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if (excelColumn == null) continue;

            // Replaces header style of 'ExcelModel' with header style of 'ExcelColumn'.
            ExcelStyleConfig headerConfig = FieldUtils.instantiate(excelColumn.headerStyle());
            if (!(headerConfig instanceof NoStyleConfig)) {
                CellStyle headerStyle = ExcelUtils.toCellStyle(this.workbook, headerConfig);
                this.headerStyles[i] = headerStyle;
            }

            // Replaces body style of 'ExcelModel' with body style of 'ExcelColumn'.
            ExcelStyleConfig bodyConfig = FieldUtils.instantiate(excelColumn.bodyStyle());
            if (!(bodyConfig instanceof NoStyleConfig)) {
                CellStyle bodyStyle = ExcelUtils.toCellStyle(this.workbook, bodyConfig);
                this.bodyStyles[i] = bodyStyle;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.converter.setDefaultValue(defaultValue);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> sheetName(String sheetName) {
        super.sheetName(sheetName);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> headerNames(List<String> headerNames) {
        Asserts.that(headerNames)
                .as("Header names cannot be null or empty")
                .isNotNull().hasElement()
                .as("The number of header names is not equal to the number of targeted fields in the class '{0}'",
                        this.type.getName())
                .isSameSize(this.fields);

        super.headerNames(headerNames);

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> unrotate() {
        super.unrotate();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> filter() {
        super.filter();
        return this;
    }

    /**
     * Enables to create dropdowns for columns of enum.
     *
     * <p> If this is invoked, excel file has only one sheet.
     *
     * @return {@link ModelWriter}
     * @see ExcelModel#enumDropdown()
     * @see ExcelColumn#enumDropdown()
     */
    public ModelWriter<W, T> enumDropdown() {
        this.enableDropdown = true;
        return this;
    }

    ///////////////////////////////////// Decoration //////////////////////////////////////

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> headerStyle(ExcelStyleConfig config) {
        return headerStyles(config);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> headerStyles(ExcelStyleConfig... configs) {
        super.headerStyles(configs);

        if (this.headerStyles != null) {
            Asserts.that(this.headerStyles.length == 1 || this.headerStyles.length == this.fields.size())
                    .as("Number of header styles({0}) must be 1 or equal to number of targeted fields({1}) in the class '{2}'",
                            this.headerStyles.length, this.fields.size(), this.type.getName())
                    .isTrue();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> bodyStyle(ExcelStyleConfig config) {
        return bodyStyles(config);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> bodyStyles(ExcelStyleConfig... configs) {
        super.bodyStyles(configs);

        if (this.bodyStyles != null) {
            Asserts.that(this.bodyStyles.length == 1 || this.bodyStyles.length == this.fields.size())
                    .as("Number of body styles({0}) must be 1 or equal to number of targeted fields({1}) in the class '{2}'",
                            this.bodyStyles.length, this.fields.size(), this.type.getName())
                    .isTrue();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> autoResizeColumns() {
        super.autoResizeColumns();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> hideExtraRows() {
        super.hideExtraRows();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelWriter}
     */
    @Override
    public ModelWriter<W, T> hideExtraColumns() {
        super.hideExtraColumns();
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * @see #createDropdowns(Sheet)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void beforeWrite(OutputStream out, List<T> list) {
        Map<Integer, String[]> map = new HashMap<>();
        ExcelModel excelModel = this.type.getAnnotation(ExcelModel.class);
        boolean enableByModel = excelModel != null && excelModel.enumDropdown();

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);
            if (field.getType().getSuperclass() != Enum.class) continue;

            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            boolean enableByColumn = excelColumn != null && excelColumn.enumDropdown();

            if (this.enableDropdown || enableByModel || enableByColumn) {
                String[] dropdownItems;

                if (excelColumn != null && excelColumn.dropdownItems().length > 0) {
                    // Sets custom dropdown items for enum.
                    dropdownItems = excelColumn.dropdownItems();

                } else {
                    // Sets default dropdown items for enum.
                    Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
                    dropdownItems = (String[]) EnumSet.allOf(enumType).stream()
                            .map(e -> ((Enum) e).name()).toArray(String[]::new);
                }

                map.put(i, dropdownItems);
            }
        }

        // Assigns null if map is empty.
        if (!map.isEmpty()) this.enumDropdownItemsMap = map;
    }

    /**
     * {@inheritDoc}
     *
     * <p> If the header names are not set through {@link #headerNames(List)},
     * this method brings the values from {@link ExcelColumn#name()}.
     *
     * @see FieldUtils#toHeaderNames(List)
     */
    @Override
    protected void ifHeaderNamesAreEmpty(List<String> headerNames) {
        headerNames.addAll(FieldUtils.toHeaderNames(this.fields));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeToSheet(Sheet sheet, List<T> list) {
        // Creates constraint for columns of enum.
        if (this.enumDropdownItemsMap != null) createDropdowns(sheet);

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

                if (this.bodyStyles == null) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = this.bodyStyles.length == 1
                        ? this.bodyStyles[0] : this.bodyStyles[j];

                // There is possibility that 'bodyStyles' has null elements, if you set 'NoStyleConfig'.
                if (bodyStyle != null) cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumOfColumns() {
        return this.fields.size();
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates dropdowns for columns of {@link Enum}.
     *
     * @see #beforeWrite(OutputStream, List)
     */
    private void createDropdowns(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();

        this.enumDropdownItemsMap.forEach((i, items) -> {
            // Creates reference of the column range except first row.
            String ref = ExcelUtils.toColumnRangeReference(sheet, i);

            // Sets validation with the dropdown items at the reference.
            ExcelUtils.setValidation(sheet, helper, ref, items);
        });
    }

}
