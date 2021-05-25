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

package com.github.javaxcel.converter.out.support;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.ConversionType;
import com.github.javaxcel.constant.ConverterType;
import com.github.javaxcel.converter.out.DefaultOutputConverter;
import com.github.javaxcel.converter.out.ExpressionOutputConverter;
import com.github.javaxcel.converter.out.OutputConverter;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class OutputConverterSupport<T> implements OutputConverter<T> {

    private final Map<Field, Column> columnMap;

    private final OutputConverter<T> defaultConverter;

    private final OutputConverter<T> expressionConverter;

    public OutputConverterSupport(List<Field> fields) {
        this.columnMap = fields.stream().collect(collectingAndThen(toMap(it -> it, Column::new),
                Collections::unmodifiableMap));

        this.defaultConverter = new DefaultOutputConverter<>();
        // Caches expressions for each field to improve performance.
        this.expressionConverter = new ExpressionOutputConverter<>(fields);
    }

    public void setDefaultValue(String defaultValue) {
        this.columnMap.values().forEach(column -> column.defaultValue = defaultValue);
    }

    /**
     * Computes a field value.
     * if the value is null or empty string, converts it to default value.
     *
     * <ol>
     *     <li>{@link com.github.javaxcel.out.AbstractExcelWriter#defaultValue(String)}</li>
     *     <li>{@link ExcelColumn#defaultValue()}</li>
     *     <li>{@link ExcelModel#defaultValue()}</li>
     * </ol>
     *
     * @param model model in list
     * @param field field of model
     * @return origin value or default value
     */
    public String convert(T model, Field field) {
        Column column = this.columnMap.get(field);

        String cellValue;
        if (column.conversionType == ConversionType.DEFAULT) {
            cellValue = this.defaultConverter.convert(model, field);
        } else {
            cellValue = this.expressionConverter.convert(model, field);
        }

        return StringUtils.ifNullOrEmpty(cellValue, column.defaultValue);
    }

    private static class Column {
        private final ConversionType conversionType;
        private String defaultValue;

        private Column(Field field) {
            // Checks which conversion type of a field value, when it is written.
            this.conversionType = ConversionType.of(field, ConverterType.OUT);

            // Decides the proper default value for a field value.
            // @ExcelColumn's default value takes precedence over @ExcelModel's default value.
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
            if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
                // Default value on @ExcelColumn
                this.defaultValue = columnAnnotation.defaultValue();
            } else {
                ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
                if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
                    // Default value on @ExcelModel
                    this.defaultValue = modelAnnotation.defaultValue();
                }
            }
        }
    }

}
