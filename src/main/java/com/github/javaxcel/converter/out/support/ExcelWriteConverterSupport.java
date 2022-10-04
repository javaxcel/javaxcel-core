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

import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.DefaultExcelWriteConverter;
import com.github.javaxcel.converter.out.ExcelWriteConverter;
import com.github.javaxcel.converter.out.ExpressionExcelWriteConverter;
import com.github.javaxcel.converter.out.analysis.ExcelWriteAnalysis;

import java.lang.reflect.Field;
import java.util.List;

public class ExcelWriteConverterSupport implements ExcelWriteConverter {

//    private final Map<Field, Column> columnMap;

    private final ExcelWriteConverter defaultConverter;

//    private final ExcelWriteConverter expressionConverter;

    public ExcelWriteConverterSupport(List<Field> fields, ExcelTypeHandlerRegistry registry, List<ExcelWriteAnalysis> analyses) {
//        this.columnMap = fields.stream().collect(collectingAndThen(
//                toMap(Function.identity(), it -> new Column(it, ConverterType.OUT)),
//                Collections::unmodifiableMap));

        this.defaultConverter = new DefaultExcelWriteConverter(registry, analyses);
        // Caches expressions for each field to improve performance.
//        this.expressionConverter = new ExpressionExcelWriteConverter(fields);
    }

    @Deprecated
    public void setAllDefaultValues(String defaultValue) {
//        this.columnMap.values().forEach(it -> it.setDefaultValue(defaultValue));
    }

    @Override
    public String convert(Object model, Field field) {
        return this.defaultConverter.convert(model, field);
//        Column column = this.columnMap.get(field);
//
//        String cellValue;
//        if (column.getConversionType() == ConversionType.DEFAULT) {
//            cellValue = this.defaultConverter.convert(model, field);
//        } else {
//            cellValue = this.expressionConverter.convert(model, field);
//        }
//
//        return StringUtils.ifNullOrEmpty(cellValue, column.getDefaultValue());
    }

}
