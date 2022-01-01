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
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utilities for reflection.
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

        // Excludes internal groovy fields.
        Predicate<Field> filter = field -> !Modifier.isTransient(field.getModifiers()) ||
                !field.getType().getName().equals("groovy.lang.MetaClass");
        // Excludes the fields to be ignored.
        filter = filter.and(field -> field.getAnnotation(ExcelIgnore.class) == null);
        // Excludes the static fields.
        filter = filter.and(field -> !Modifier.isStatic(field.getModifiers()));
        // Excludes the implicit fields.
        if (excelModel != null && excelModel.explicit()) {
            filter = filter.and(field -> field.getAnnotation(ExcelColumn.class) != null);
        }

        return stream.filter(filter).collect(toList());
    }

    /**
     * Converts fields to header names.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not empty,
     * this returns a header name defined in the field, otherwise returns name of the field.
     *
     * @param fields targeted fields
     * @return list of {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static List<String> toHeaderNames(List<Field> fields) {
        List<String> headerNames = new ArrayList<>();
        for (Field field : fields) {
            headerNames.add(toHeaderName(field));
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
     * @param field targeted field
     * @return {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static String toHeaderName(Field field) {
        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

        return annotation == null || StringUtils.isNullOrEmpty(annotation.name())
                ? field.getName() : annotation.name();
    }

    /**
     * Converts fields to a map.
     *
     * @param model model in list
     * @param <T>   type of the object
     * @return {@link Map} in which key is the model's field name and value is the model's field value
     * @see Field#getName()
     * @see ReflectionUtils#getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toMap(T model) {
        return toMap(model, getTargetedFields(model.getClass()));
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

}
