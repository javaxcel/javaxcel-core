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

package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utilities for reflection on {@link Field}.
 */
public final class FieldUtils {

    @ExcludeFromGeneratedJacocoReport
    private FieldUtils() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    /**
     * Gets the targeted fields depending on the policy.
     *
     * <p> If the targeting policy depended on {@link ExcelModel#includeSuper()} is
     * {@code true}, this returns its own declared fields.
     * Otherwise, this returns its own declared fields including super classes' fields.
     *
     * <p> This doesn't return the fields annotated with {@link ExcelIgnore}.
     *
     * <p> If the model class' {@link ExcelModel#explicit()} is {@code true},
     * this excludes the fields not annotated with {@link ExcelColumn}.
     *
     * @param type type of the object
     * @return targeted fields
     * @see ExcelModel#includeSuper()
     * @see ExcelModel#explicit()
     * @see ExcelIgnore
     */
    public static List<Field> getTargetedFields(Class<?> type) {
        // Gets the fields depending on the policies.
        ExcelModel excelModel = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = excelModel == null || !excelModel.includeSuper()
                ? Arrays.stream(type.getDeclaredFields())
                : ReflectionUtils.getInheritedFields(type).stream();

        // Excludes the synthetic fields
        Predicate<Field> filter = it -> !it.isSynthetic();
        // Excludes the static fields.
        filter = filter.and(it -> !Modifier.isStatic(it.getModifiers()));
        // Excludes the fields to be ignored.
        filter = filter.and(it -> !it.isAnnotationPresent(ExcelIgnore.class));
        // Excludes the implicit fields.
        if (excelModel != null && excelModel.explicit()) {
            filter = filter.and(it -> it.isAnnotationPresent(ExcelColumn.class));
        }

        return stream.filter(filter).collect(toList());
    }

    public static Method resolveGetter(Field field) {
        String fieldName = field.getName();

        String getterName = "get";
        if (fieldName.length() > 1) {
            getterName += Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        } else {
            getterName += fieldName.toUpperCase();
        }

        Method getter = ReflectionUtils.getDeclaredMethod(field.getDeclaringClass(), getterName);
        if (!Modifier.isPublic(getter.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("Getter for field[%s] is not public: %s", field, getter));
        }

        return getter;
    }

    public static Method resolveSetter(Field field) {
        String fieldName = field.getName();

        String setterName = "set";
        if (fieldName.length() > 1) {
            setterName += Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        } else {
            setterName += fieldName.toUpperCase();
        }

        Method setter = ReflectionUtils.getDeclaredMethod(field.getDeclaringClass(), setterName, field.getType());
        if (!Modifier.isPublic(setter.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("Setter for field[%s] is not public: %s", field, setter));
        }

        return setter;
    }

    /**
     * Converts fields to header names.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not empty,
     * this returns a header name defined in the field, otherwise returns name of the field.
     *
     * @param fields           targeted fields
     * @param ignoreAnnotation whether {@link ExcelColumn#name()} is ignored
     * @return list of {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static List<String> toHeaderNames(List<Field> fields, boolean ignoreAnnotation) {
        List<String> headerNames = new ArrayList<>();
        for (Field field : fields) {
            headerNames.add(toHeaderName(field, ignoreAnnotation));
        }

        return headerNames;
    }

    /**
     * Converts field to header name.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not empty,
     * this returns a header name defined in the field, otherwise returns name of the field.
     *
     * @param field            targeted field
     * @param ignoreAnnotation whether {@link ExcelColumn#name()} is ignored
     * @return {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static String toHeaderName(Field field, boolean ignoreAnnotation) {
        if (ignoreAnnotation) return field.getName();

        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
        return annotation == null || StringUtils.isNullOrEmpty(annotation.name())
                ? field.getName() : annotation.name();
    }

    /**
     * Converts fields to a map.
     *
     * @param model  model in list
     * @param fields targeted fields
     * @param <T>    type of the object
     * @return {@link Map} in which key is the model's field name and value is the model's field value
     * @see Field#getName()
     * @see ReflectionUtils#getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toMap(T model, List<Field> fields) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), ReflectionUtils.getFieldValue(model, field));
        }

        return map;
    }

    @Null
    @SuppressWarnings("unchecked")
    public static <T> T resolveFirst(Class<T> type, Object... arguments) {
        for (Object argument : arguments) {
            if (type.isInstance(argument)) {
                return (T) argument;
            }
        }

        return null;
    }

    @Null
    @SuppressWarnings("unchecked")
    public static <T> T resolveLast(Class<T> type, Object... arguments) {
        for (int i = arguments.length - 1; i >= 0; i--) {
            Object argument = arguments[i];

            if (type.isInstance(argument)) {
                return (T) argument;
            }
        }

        return null;
    }

    public static Class<?> resolveActualType(Field field) {
        Class<?> fieldType = field.getType();
        Type genericType = field.getGenericType();

        outer:
        while (true) {
            // When type is concrete type.
            if (genericType instanceof Class) {
                Class<?> c = (Class<?>) genericType;
                if (Iterable.class.isAssignableFrom(c)) {
                    return Object.class;
                }

                return c;
            }

            // When type is wildcard type:
            // java.util.List<? super java.lang.String>
            // java.util.List<? extends java.lang.String>
            if (genericType instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) genericType;
                Type[] lowerBounds = wildcardType.getLowerBounds();
                Type t = ArrayUtils.exists(lowerBounds) ? lowerBounds[0] : wildcardType.getUpperBounds()[0];

                if (t instanceof Class) {
                    genericType = t;
                    continue;
                }

                // GenericArrayType: T[]
                if (t instanceof GenericArrayType) {
                    genericType = fieldType;
                    continue;
                }

                // TypeVariable: T
                genericType = Object.class;
                continue;
            }

            if (genericType instanceof TypeVariable) {
                genericType = fieldType;
                continue;
            }

            if (!(genericType instanceof ParameterizedType)) {
                genericType = Object.class;
                continue;
            }

            ParameterizedType paramType = (ParameterizedType) genericType;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            if (Iterable.class.isAssignableFrom(rawType)) {
                for (Type t : paramType.getActualTypeArguments()) {
                    genericType = t;
                    continue outer;
                }
            } else {
                genericType = rawType;
            }
        }
    }

    public static String getDeclaration(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            // types with parameters
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            String declaration = parameterizedType.getRawType().getTypeName();
            declaration += "<";

            Type[] typeArgs = parameterizedType.getActualTypeArguments();

            for (int i = 0; i < typeArgs.length; i++) {
                Type typeArg = typeArgs[i];

                if (i > 0) {
                    declaration += ", ";
                }

                // note: recursive call
                declaration += getDeclaration(typeArg);
            }

            declaration += ">";
            declaration = declaration.replace('$', '.');
            return declaration;
        } else if (genericType instanceof Class<?>) {
            Class<?> clazz = (Class<?>) genericType;

            if (clazz.isArray()) {
                // arrays
                return clazz.getComponentType().getCanonicalName() + "[]";
            } else {
                // primitive and types without parameters (normal/standard types)
                return clazz.getCanonicalName();
            }
        } else {
            // e.g. WildcardTypeImpl (Class<? extends Integer>)
            return genericType.getTypeName();
        }
    }

}
