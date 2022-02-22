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

package com.github.javaxcel.converter.in;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ClassUtils;
import io.github.imsejin.common.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;

public class DefaultExcelReadConverter implements ExcelReadConverter {

    private final ExcelTypeHandlerRegistry registry;

    public DefaultExcelReadConverter(ExcelTypeHandlerRegistry registry) {
        Asserts.that(registry)
                .as("DefaultExcelReadConverter.registry is not allowed to be null")
                .isNotNull()
                .as("DefaultExcelReadConverter.registry.allTypes is not allowed to be null")
                .predicate(it -> it.getAllTypes() != null);

        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Object convert(Map<String, Object> variables, Field field) {
        Class<?> type = field.getType();
        String value = (String) variables.get(field.getName());

        return convertInternal(field, type, value);
    }

    private Object convertInternal(Field field, Class<?> type, String value) {
        // When cell value is null or empty.
        if (StringUtils.isNullOrEmpty(value)) {
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if (excelColumn != null && !excelColumn.defaultValue().equals("")) {
                // Converts again with the default value.
                return convertInternal(field, type, excelColumn.defaultValue());
            } else {
                // When you don't explicitly define default value.
                return ClassUtils.initialValueOf(type);
            }
        }

        // Supports one-dimensional array type.
        if (type.isArray()) return handleArray(field, type, value);

        return handleNonArray(field, type, value);
    }

    private Object handleArray(Field field, Class<?> type, String value) {
        // Not support multi-dimensional array type.
        Class<?> componentType = type.getComponentType();
        if (componentType.isArray()) return null;

        String[] splitValue = value.split(", ");

        // To solve that ClassCastException(primitive array doesn't be assignable to Object array),
        // we use java.lang.reflect.Array API instead of casting primitive array to Object array.
        Object array = Array.newInstance(componentType, splitValue.length);
        for (int i = 0; i < splitValue.length; i++) {
            String element = splitValue[i];
            Object converted = handleNonArray(field, componentType, element);
            Array.set(array, i, converted);
        }

        return array;
    }

    private Object handleNonArray(Field field, Class<?> type, String value) {
        // Resolves a handler of the type.
        ExcelTypeHandler<?> handler = this.registry.getHandler(type);

        // When there is no handler matched with the type.
        if (handler == null) return ClassUtils.initialValueOf(type);

        try {
            // Converts string to the type of field.
            return handler.read(value, field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
