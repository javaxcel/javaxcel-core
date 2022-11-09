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

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ClassUtils;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReadHandlerConverter implements ExcelReadConverter {

    private final ExcelTypeHandlerRegistry registry;

    private final Map<Field, ExcelAnalysis> analysisMap;

    public ExcelReadHandlerConverter(Iterable<ExcelAnalysis> analyses, ExcelTypeHandlerRegistry registry) {
        Asserts.that(analyses)
                .describedAs("ExcelReadHandlerConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadHandlerConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());
        Asserts.that(registry)
                .describedAs("ExcelReadHandlerConverter.registry is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadHandlerConverter.registry.allTypes is not allowed to be null")
                .isNot(it -> it.getAllTypes() == null);

        this.registry = registry;

        Map<Field, ExcelAnalysis> analysisMap = new HashMap<>();
        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();
            analysisMap.put(field, analysis);
        }

        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field);
        return analysis.hasFlag(ExcelReadAnalyzer.HANDLER);
    }

    @Null
    @Override
    public Object convert(Map<String, String> variables, Field field) {
        Class<?> type = field.getType();
        String value = variables.get(field.getName());

        return handleInternal(field, type, value);
    }

    private Object handleInternal(Field field, Class<?> type, String value) {
        // When cell value is null or empty.
        if (StringUtils.isNullOrEmpty(value)) {
            ExcelAnalysis analysis = this.analysisMap.get(field);
            String defaultValue = analysis.getDefaultMeta().getValue();

            if (StringUtils.isNullOrEmpty(defaultValue)) {
                // When you don't explicitly define default value.
                return ClassUtils.initialValueOf(type);
            } else {
                // Converts again with the default value.
                return handleInternal(field, type, defaultValue);
            }
        }

        if (type.isArray()) {
            // Supports multi-dimensional array type.
            return handleArray(field, type, value);
        } else if (Iterable.class.isAssignableFrom(type)) {
            // Supports nested iterable type.
            Class<?> actualType = FieldUtils.resolveActualType(field);
            return handleIterable(field, actualType, value);
        } else {
            return handleConcrete(field, type, value);
        }
    }

    private Object handleArray(Field field, Class<?> type, String value) {
        Class<?> componentType = type.getComponentType();
        String[] strings = Utils.shallowSplit(value, ", ");

        // To solve that ClassCastException(primitive array doesn't be assignable to Object array),
        // we use java.lang.reflect.Array API instead of casting primitive array to Object array.
        Object array = Array.newInstance(componentType, strings.length);

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];

            Object element;
            if (componentType.isArray()) {
                element = string.isEmpty() ? null : handleArray(field, componentType, string);
            } else if (Iterable.class.isAssignableFrom(componentType)) {
                element = string.isEmpty() ? null : handleIterable(field, componentType, string);
            } else {
                // Allows empty string to handler for non-array type.
                element = handleConcrete(field, componentType, string);
            }

            Array.set(array, i, element);
        }

        return array;
    }

    // TODO: Fix this.
    private Iterable<?> handleIterable(Field field, Class<?> type, String value) {
        Class<?> componentType = type.getComponentType();
        String[] strings = Utils.shallowSplit(value, ", ");

        List<Object> list = new ArrayList<>(strings.length);

        for (String string : strings) {
            Object element;
            if (componentType.isArray()) {
                element = string.isEmpty() ? null : handleArray(field, componentType, string);
            } else if (Iterable.class.isAssignableFrom(componentType)) {
                element = string.isEmpty() ? null : handleIterable(field, componentType, string);
            } else {
                // Allows empty string to handler for non-array type.
                element = handleConcrete(field, componentType, string);
            }

            list.add(element);
        }

        return list;
    }

    private Object handleConcrete(Field field, Class<?> type, String value) {
        // Resolves a handler of the type.
        ExcelTypeHandler<?> handler = this.registry.getHandler(type);

        if (handler == null) {
            // When there is no handler for the type.
            if (!ClassUtils.isEnumOrEnumConstant(type)) {
                return ClassUtils.initialValueOf(type);
            }

            // When there is no handler for the specific enum type, use EnumTypeHandler as default.
            handler = this.registry.getHandler(Enum.class);
        }

        try {
            // Converts string to the type of field.
            return handler.read(value, field);
        } catch (Exception e) {
            String message = String.format("Failed to convert %s(String) to %s", value, type.getSimpleName());
            throw new RuntimeException(message, e);
        }
    }

    // -------------------------------------------------------------------------------------------------

    // To access at test source, modifier should be package-private.
    @VisibleForTesting
    static class Utils {

        private static final String[] EMPTY_STRING_ARRAY = new String[0];

        /**
         * Splits the string from only elements in one-dimensional array.
         *
         * @param src       array-like string
         * @param delimiter delimiter of array
         * @return separated strings
         */
        public static String[] shallowSplit(String src, String delimiter) {
            Asserts.that(src)
                    .describedAs("src must be array-like string, but it isn't: '{0}'", src)
                    .isNotNull().startsWith("[").endsWith("]");
            Asserts.that(delimiter)
                    .describedAs("delimiter is not allowed to be null or empty: '{0}'", delimiter)
                    .isNotNull().isNotEmpty();

            // Fast return.
            if (src.equals("[]")) return EMPTY_STRING_ARRAY;

            char opener = '[';
            char closer = ']';

            StringBuilder sb = new StringBuilder();
            List<String> list = new ArrayList<>();

            for (int i = 0, depth = 0; i < src.length(); i++) {
                char c = src.charAt(i);

                if (c == opener) {
                    int index = StringUtils.indexOfCurrentClosingBracket(src, i, opener, closer);
                    if (index == -1) throw new IllegalArgumentException("Unclosed bracket: index " + i + " of " + src);

                    depth++;

                    // Skips characters of nested array.
                    if (depth > 1) {
                        list.add(src.substring(i, index + 1));
                        i = index - 1;
                    }

                    continue;
                }

                if (c == closer) {
                    depth--;
                    continue;
                }

                if (depth == 1) {
                    if (isDelimiterByChar(src, i, delimiter)) {
                        // '], '
                        if (src.charAt(i - 1) != closer) {
                            list.add(sb.toString());
                            sb.setLength(0);
                        }

                        // Skips characters of delimiter.
                        i = i + delimiter.length() - 1;
                    } else {
                        sb.append(c);
                    }
                }
            }

            // Adds not flushed string as the last element.
            if (sb.length() > 0) list.add(sb.toString());

            // Adds empty string as the last element.
            if (src.endsWith(delimiter + closer)) list.add("");

            return list.toArray(new String[0]);
        }

        /**
         * Returns length of one-dimensional array.
         *
         * @param str array-like string
         * @return array length
         */
        public static int getShallowLength(String str) {
            int length = 0;
            boolean isEmpty = false;
            char opener = '[';
            char closer = ']';

            for (int i = 0, depth = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                if (c == opener) {
                    int index = StringUtils.indexOfCurrentClosingBracket(str, i, opener, closer);
                    if (index == -1) throw new IllegalArgumentException("Unclosed bracket: index " + i + " of " + str);

                    depth++;

                    // Checks if str is '[]'.
                    if (depth == 1 && str.charAt(i + 1) == closer) isEmpty = true;

                    // Skips characters until inner closer.
                    if (depth > 1) i = index - 1;
                }

                if (c == closer) depth--;
                if (depth == 1 && isDelimiterByChar(str, i, ", ")) length++;
            }

            if (!isEmpty) length++;

            return length;
        }

        private static boolean isDelimiterByChar(String src, int pos, String delimiter) {
            if (src == null || src.isEmpty() || delimiter == null || delimiter.isEmpty()) return false;
            if (src.length() < delimiter.length()) return false;

            for (int i = 0; i < delimiter.length(); i++) {
                char c0 = delimiter.charAt(i);
                char c1 = src.charAt(pos + i);

                if (c0 != c1) return false;
            }

            return true;
        }

    }

}
