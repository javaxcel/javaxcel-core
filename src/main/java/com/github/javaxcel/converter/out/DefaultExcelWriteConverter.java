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

import com.github.javaxcel.analysis.out.ExcelWriteAnalysis;
import com.github.javaxcel.analysis.out.impl.FieldAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.analysis.out.impl.GetterAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ClassUtils;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class DefaultExcelWriteConverter implements ExcelWriteConverter {

    private final ExcelTypeHandlerRegistry registry;

    private final Map<Field, ExcelWriteAnalysis> analysisMap;

    public DefaultExcelWriteConverter(ExcelTypeHandlerRegistry registry, List<ExcelWriteAnalysis> analyses) {
        Asserts.that(registry)
                .describedAs("DefaultExcelWriteConverter.registry is not allowed to be null")
                .isNotNull()
                .describedAs("DefaultExcelWriteConverter.registry.allTypes is not allowed to be null")
                .isNot(it -> it.getAllTypes() == null);
        Asserts.that(analyses)
                .describedAs("DefaultExcelWriteConverter.analyses is not allowed to be null")
                .isNotNull();

        this.registry = registry;
        this.analysisMap = analyses.stream().collect(collectingAndThen(
                toMap(ExcelWriteAnalysis::getField, Function.identity()), Collections::unmodifiableMap));
    }

    @Override
    public boolean supports(Field field) {
        ExcelWriteAnalysis analysis = this.analysisMap.get(field);

        return analysis instanceof FieldAccessDefaultExcelWriteAnalysis
                || analysis instanceof GetterAccessDefaultExcelWriteAnalysis;
    }

    /**
     * {@inheritDoc}
     */
    @Null
    @Override
    public String convert(Object model, Field field) {
        ExcelWriteAnalysis analysis = this.analysisMap.get(field);
        Object value = analysis.getValue(model);

        // Returns default value if the value is null.
        if (value == null) {
            return analysis.getDefaultValue();
        }

        Class<?> type = field.getType();
        return handleInternal(field, type, value);
    }

    private String handleInternal(Field field, Class<?> type, Object value) {
        if (type.isArray()) {
            // Supports multi-dimensional array type.
            return handleArray(field, value);
        } else if (value instanceof Iterable) {
            // Supports nested iterable type.
            return handleIterable(field, (Iterable<?>) value);
        } else {
            return handleConcrete(field, type, value);
        }
    }

    private String handleArray(Field field, Object value) {
        // Fast return.
        int length = Array.getLength(value);
        if (length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");

        // To solve that ClassCastException(primitive array doesn't be assignable to Object array),
        // we use java.lang.reflect.Array API instead of casting primitive array to Object array.
        for (int i = 0; i < length; i++) {
            Object element = Array.get(value, i);

            if (element != null) {
                // Resolves type from each element, not from component type.
                // Because one dimensional Object array can have array instance as an element.
                Class<?> elementType = element.getClass();

                String string = handleInternal(field, elementType, element);

                // Considers null as empty string.
                if (string != null) {
                    sb.append(string);
                }
            }

            // Keeps element separator when element is null.
            if (i < length - 1) {
                sb.append(", ");
            }
        }

        return sb.append(']').toString();
    }

    private String handleIterable(Field field, Iterable<?> value) {
        // Fast return.
        Iterator<?> iterator = value.iterator();
        if (!iterator.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");

        while (iterator.hasNext()) {
            Object element = iterator.next();

            if (element != null) {
                // Resolves type from each element, not from component type.
                // Because one dimensional Object array can have array instance as an element.
                Class<?> elementType = element.getClass();

                String string = handleInternal(field, elementType, element);

                // Considers null as empty string.
                if (string != null) {
                    sb.append(string);
                }
            }

            // Keeps element separator when element is null.
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.append(']').toString();
    }

    private String handleConcrete(Field field, Class<?> type, Object value) {
        ExcelWriteAnalysis analysis = this.analysisMap.get(field);

        ExcelTypeHandler<?> handler;
        if (analysis != null && analysis.doesHandlerResolved()) {
            // When ExcelWriteAnalyzer has analysis for the field and its handler is resolved.
            handler = analysis.getHandler();
        } else {
            // When ExcelWriteAnalyzer can't resolve handler for the field or this converter has no analysis.
            handler = this.registry.getHandler(type);
        }

        if (handler == null) {
            // When there is no handler for the type, just stringifies value.
            if (!ClassUtils.isEnumOrEnumConstant(type)) {
                return value.toString();
            }

            // When there is no handler for the specific enum type,
            // use EnumTypeHandler as default.
            handler = this.registry.getHandler(Enum.class);
        }

        try {
            // Converts value to string with the handler.
            return handler.write(value, field);
        } catch (Exception e) {
            String message = String.format("Failed to convert %s(%s) to string", value, type.getSimpleName());
            throw new RuntimeException(message, e);
        }
    }

}
