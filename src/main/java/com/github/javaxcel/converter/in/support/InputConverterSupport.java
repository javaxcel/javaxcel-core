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

package com.github.javaxcel.converter.in.support;

import com.github.javaxcel.constant.ConversionType;
import com.github.javaxcel.constant.ConverterType;
import com.github.javaxcel.converter.in.DefaultInputConverter;
import com.github.javaxcel.converter.in.ExpressionInputConverter;
import com.github.javaxcel.converter.in.InputConverter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class InputConverterSupport implements InputConverter {

    private final Map<Field, Column> columnMap;

    private final InputConverter defaultConverter;

    private final InputConverter expressionConverter;

    public InputConverterSupport(List<Field> fields) {
        this.columnMap = fields.stream().collect(collectingAndThen(toMap(it -> it, Column::new),
                Collections::unmodifiableMap));

        this.defaultConverter = new DefaultInputConverter();
        // Caches expressions for each field to improve performance.
        this.expressionConverter = new ExpressionInputConverter(fields);
    }

    @Override
    public Object convert(Map<String, Object> variables, Field field) {
        Column column = this.columnMap.get(field);

        Object fieldValue;
        if (column.conversionType == ConversionType.DEFAULT) {
            fieldValue = this.defaultConverter.convert(variables, field);
        } else {
            fieldValue = this.expressionConverter.convert(variables, field);
        }

        return fieldValue;
    }

    private static class Column {
        private final ConversionType conversionType;

        private Column(Field field) {
            // Checks which conversion type of a field value,
            // when cell value is read and set it to the field.
            this.conversionType = ConversionType.of(field, ConverterType.IN);
        }
    }

}
