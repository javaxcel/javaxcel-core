/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.ConversionType;
import com.github.javaxcel.constant.ConverterType;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class Column {

    private final ConversionType conversionType;

    private String defaultValue;

    public Column(Field field, ConverterType converterType) {
        // Checks which conversion type of field value.
        this.conversionType = ConversionType.of(field, converterType);
        if (converterType == ConverterType.OUT) this.defaultValue = resolveDefaultValue(field);
    }

    private static String resolveDefaultValue(Field field) {
        String defaultValue = null;

        // Decides the proper default value for a field value.
        // @ExcelColumn's default value takes precedence over ExcelModel's default value.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
            // Default value on @ExcelColumn
            defaultValue = columnAnnotation.defaultValue();
        } else {
            ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
            if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
                // Default value on @ExcelModel
                defaultValue = modelAnnotation.defaultValue();
            }
        }

        return defaultValue;
    }

    public ConversionType getConversionType() {
        return this.conversionType;
    }

    @Nullable
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
