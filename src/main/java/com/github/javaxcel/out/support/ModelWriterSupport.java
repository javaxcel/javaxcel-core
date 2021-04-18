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

package com.github.javaxcel.out.support;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.ConvertType;
import com.github.javaxcel.converter.out.BasicWritingConverter;
import com.github.javaxcel.converter.out.ExpressiveWritingConverter;
import com.github.javaxcel.converter.out.WritingConverter;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelWriterSupport<T> {

    private final Map<Field, Column> columnMap;

    private final WritingConverter<T> basicConverter;

    private final WritingConverter<T> expressiveConverter;

    public ModelWriterSupport(List<Field> fields) {
        Map<Field, Column> map = new HashMap<>();

        for (Field field : fields) {
            map.put(field, Column.from(field));
        }

        this.columnMap = map;
        this.basicConverter = new BasicWritingConverter<>();
        // Caches expressions for each field to improve performance.
        this.expressiveConverter = new ExpressiveWritingConverter<>(fields);
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
    public String compute(T model, Field field) {
        Column column = this.columnMap.get(field);

        String cellValue;
        if (column.convertType == ConvertType.BASIC) {
            cellValue = this.basicConverter.convert(model, field);
        } else {
            cellValue = this.expressiveConverter.convert(model, field);
        }

        return StringUtils.ifNullOrEmpty(cellValue, column.defaultValue);
    }

    private static class Column {
        private ConvertType convertType;
        private String defaultValue;

        private Column() {
        }

        private static Column from(Field field) {
            Column column = new Column();

            // Checks if a field value requires ExpressiveWritingConverter when it is written.
            column.convertType = ConvertType.of(field);

            // Decides the proper default value for a field value.
            // @ExcelColumn's default value takes precedence over @ExcelModel's default value.
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
            if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
                // Default value on @ExcelColumn
                column.defaultValue = columnAnnotation.defaultValue();
            } else {
                ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
                if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
                    // Default value on @ExcelModel
                    column.defaultValue = modelAnnotation.defaultValue();
                }
            }

            return column;
        }
    }

}
