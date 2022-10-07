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
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.in.DefaultExcelReadConverter;
import com.github.javaxcel.converter.in.ExcelReadConverter;
import com.github.javaxcel.converter.in.ExpressionExcelReadConverter;
import com.github.javaxcel.model.Column;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public final class ExcelReadConverterSupport implements ExcelReadConverter {

    private final Map<Field, Column> columnMap;

    private final ExcelReadConverter defaultConverter;

    private final ExcelReadConverter expressionConverter;

    public ExcelReadConverterSupport(List<Field> fields, ExcelTypeHandlerRegistry registry) {
        this.columnMap = fields.stream().collect(collectingAndThen(
                toMap(Function.identity(), it -> new Column(it, ConverterType.IN)),
                Collections::unmodifiableMap));

        this.defaultConverter = new DefaultExcelReadConverter(registry);
        // Caches expressions for each field to improve performance.
        this.expressionConverter = new ExpressionExcelReadConverter(fields);
    }

    @Override
    public Object convert(Map<String, String> variables, Field field) {
        Column column = this.columnMap.get(field);

        Object fieldValue;
        if (column.getConversionType() == ConversionType.DEFAULT) {
            fieldValue = this.defaultConverter.convert(variables, field);
        } else {
            fieldValue = this.expressionConverter.convert(variables, field);
        }

        return fieldValue;
    }

}
