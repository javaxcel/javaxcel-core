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

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ClassUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelWriteHandlerConverter implements ExcelWriteConverter {

    private final ExcelTypeHandlerRegistry registry;

    private final Map<Field, Method> getterMap;

    private final Map<Field, ExcelAnalysis> analysisMap;

    public ExcelWriteHandlerConverter(Iterable<ExcelAnalysis> analyses, ExcelTypeHandlerRegistry registry) {
        Asserts.that(analyses)
                .describedAs("ExcelWriteHandlerConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteHandlerConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());
        Asserts.that(registry)
                .describedAs("ExcelWriteHandlerConverter.registry is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteHandlerConverter.registry.allTypes is not allowed to be null")
                .isNot(it -> it.getAllTypes() == null);

        this.registry = registry;

        Map<Field, Method> getterMap = new HashMap<>();
        Map<Field, ExcelAnalysis> analysisMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            // Makes getter a cache.
            if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
                Method getter = FieldUtils.resolveGetter(field);
                getterMap.put(field, getter);
            }

            analysisMap.put(field, analysis);
        }

        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field);
        return analysis.hasFlag(ExcelWriteAnalyzer.HANDLER);
    }

    /**
     * {@inheritDoc}
     */
    @Null
    @Override
    public String convert(Object model, Field field) {
        // Gets property value of model.
        Object value = getValueOf(model, field);

        // Returns default value if the value is null or empty string.
        if (isNullOrEmpty(value)) {
            ExcelAnalysis analysis = this.analysisMap.get(field);
            String defaultValue = analysis.getDefaultMeta().getValue();

            // Returns null if the default value is also null or empty string.
            if (StringUtils.isNullOrEmpty(defaultValue)) {
                return null;
            }

            return defaultValue;
        }

        Class<?> type = field.getType();
        return handleInternal(field, type, value);
    }

    // -------------------------------------------------------------------------------------------------

    @Null
    private Object getValueOf(Object model, Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field);

        if (analysis.hasFlag(ExcelWriteAnalyzer.FIELD_ACCESS)) {
            return ReflectionUtils.getFieldValue(model, field);

        } else if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
            Method getter = this.getterMap.get(field);
            return ReflectionUtils.invoke(getter, model);

        } else {
            throw new RuntimeException("Never throw; ExcelWriteAnalyzer adds the flags into each analysis");
        }
    }

    private static boolean isNullOrEmpty(@Null Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
        }

        return false;
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
        ExcelAnalysis analysis = this.analysisMap.get(field);

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
