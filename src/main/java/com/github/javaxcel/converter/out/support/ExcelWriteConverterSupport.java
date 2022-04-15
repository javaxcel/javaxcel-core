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
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.DefaultExcelWriteConverter;
import com.github.javaxcel.converter.out.ExcelWriteConverter;
import com.github.javaxcel.converter.out.ExpressionExcelWriteConverter;
import com.github.javaxcel.model.Column;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.DefaultValue;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class ExcelWriteConverterSupport<T> implements ExcelWriteConverter<T> {

    private final Map<Field, Column> columnMap;

    private final ExcelWriteConverter<T> defaultConverter;

    private final ExcelWriteConverter<T> expressionConverter;

    public ExcelWriteConverterSupport(List<Field> fields, ExcelTypeHandlerRegistry registry) {
        this.columnMap = fields.stream().collect(collectingAndThen(
                toMap(Function.identity(), it -> new Column(it, ConverterType.OUT)),
                Collections::unmodifiableMap));

        this.defaultConverter = new DefaultExcelWriteConverter<>(registry);
        // Caches expressions for each field to improve performance.
        this.expressionConverter = new ExpressionExcelWriteConverter<>(fields);
    }

    public void setAllDefaultValues(String defaultValue) {
        this.columnMap.values().forEach(it -> it.setDefaultValue(defaultValue));
    }

    /**
     * Computes a field value.
     * if the value is null or empty string, converts it to default value.
     *
     * <ol>
     *     <li>{@link DefaultValue}</li>
     *     <li>{@link ExcelColumn#defaultValue()}</li>
     *     <li>{@link ExcelModel#defaultValue()}</li>
     * </ol>
     *
     * @param model model in list
     * @param field field of model
     * @return origin value or default value
     */
    @Override
    public String convert(T model, Field field) {
        Column column = this.columnMap.get(field);

        String cellValue;
        if (column.getConversionType() == ConversionType.DEFAULT) {
            cellValue = this.defaultConverter.convert(model, field);
        } else {
            cellValue = this.expressionConverter.convert(model, field);
        }

        return StringUtils.ifNullOrEmpty(cellValue, column.getDefaultValue());
    }

}
