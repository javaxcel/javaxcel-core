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

package com.github.javaxcel.converter.out;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class DefaultExcelWriteConverter<T> implements ExcelWriteConverter<T> {

    private final ExcelTypeHandlerRegistry registry;

    public DefaultExcelWriteConverter(ExcelTypeHandlerRegistry registry) {
        Asserts.that(registry)
                .as("DefaultExcelWriteConverter.registry is not allowed to be null")
                .isNotNull()
                .as("DefaultExcelWriteConverter.registry.allTypes is not allowed to be null")
                .predicate(it -> it.getAllTypes() != null);

        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String convert(T model, Field field) {
        Object value = ReflectionUtils.getFieldValue(model, field);
        if (value == null) return null;

        Class<?> type = field.getType();

        // Supports one-dimensional array type.
        if (type.isArray()) return handleArray(field, type, value);

        return handleNonArray(field, type, value);
    }

    private String handleArray(Field field, Class<?> type, Object value) {
        // Not support multi-dimensional array type.
        Class<?> componentType = type.getComponentType();
        if (componentType.isArray()) return null;

        StringBuilder sb = new StringBuilder();

        // To solve that ClassCastException(primitive array doesn't be assignable to Object array),
        // we use java.lang.reflect.Array API instead of casting primitive array to Object array.
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(value, i);
            sb.append(handleNonArray(field, componentType, element));
            if (i < length - 1) sb.append(", ");
        }

        return sb.toString();
    }

    private String handleNonArray(Field field, Class<?> type, Object value) {
        ExcelTypeHandler<?> handler = this.registry.getHandler(type);

        // When type is not registered in registry, just stringifies value.
        if (handler == null) return value.toString();

        try {
            // Converts value to string with the handler.
            return handler.write(value, field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
