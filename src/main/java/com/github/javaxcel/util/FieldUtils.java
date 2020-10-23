package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.GettingFieldValueException;
import com.github.javaxcel.exception.SettingFieldValueException;
import io.github.imsejin.common.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class FieldUtils {

    private FieldUtils() {
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
     * @param type type of the object
     * @return targeted fields
     * @see ExcelModel#includeSuper()
     * @see ExcelModel#explicit()
     * @see ExcelIgnore
     */
    public static List<Field> getTargetedFields(Class<?> type) {
        // Gets fields depending on the policies.
        ExcelModel excelModel = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = excelModel == null || !excelModel.includeSuper()
                ? Arrays.stream(type.getDeclaredFields())
                : getInheritedFields(type).stream();

        // Excludes the fields to be ignored.
        final Predicate<Field> predicate = excelModel == null || !excelModel.explicit()
                ? field -> field.getAnnotation(ExcelIgnore.class) == null
                : field -> field.getAnnotation(ExcelIgnore.class) == null && field.getAnnotation(ExcelColumn.class) != null;
        return stream.filter(predicate).collect(toList());
    }

    /**
     * Gets fields of the type including its inherited fields.
     *
     * @param type type of the object
     * @return inherited and own fields
     * @see Class#getDeclaredFields()
     * @see Class#getSuperclass()
     */
    public static List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            fields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
        }

        return fields;
    }

    /**
     * Converts fields to header names.
     *
     * <p> This checks whether the field is annotated with {@link ExcelColumn} or not.
     * If {@link ExcelColumn#name()} is not null and not empty,
     * this returns a header name defined in the field.
     * Otherwise returns name of the field.
     *
     * @param fields targeted fields
     * @return list of {@link ExcelColumn#name()} or {@link Field#getName()}
     */
    public static List<String> toHeaderNames(List<Field> fields) {
        return fields.stream().map(field -> {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            return annotation == null || StringUtils.isNullOrEmpty(annotation.name()) ? field.getName() : annotation.name();
        }).collect(toList());
    }

    /**
     * Converts fields to a map.
     *
     * @param model  model in list
     * @param fields targeted fields
     * @param <T>    type of the object
     * @return {@link Map} in which key is the model's field name and value is the model's field value
     * @see Field#getName()
     * @see #getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toMap(T model, List<Field> fields) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), getFieldValue(model, field));
        }

        return map;
    }

    /**
     * Returns value of the field.
     *
     * @param model model in list
     * @param field targeted field
     * @param <T>   type of the object
     * @return field value
     */
    public static <T> Object getFieldValue(T model, Field field) {
        // Enables to have access to the field even private field.
        field.setAccessible(true);

        try {
            // Returns value in the field.
            return field.get(model);
        } catch (IllegalAccessException e) {
            throw new GettingFieldValueException(e, model.getClass(), field);
        }
    }

    /**
     * Sets up value into the field.
     *
     * @param model model in list
     * @param field targeted field
     * @param value value to be set into field
     * @param <T>   type of the object
     */
    public static <T> void setFieldValue(T model, Field field, Object value) {
        // Enables to have access to the field even private field.
        field.setAccessible(true);

        // Sets value into the field.
        try {
            field.set(model, value);
        } catch (IllegalAccessException e) {
            throw new SettingFieldValueException(e, model.getClass(), field);
        }
    }

    /**
     * If the value is null or empty string, converts a value to default value.
     *
     * @param maybeFaulty  value that may be null or empty
     * @param defaultValue 1. {@link com.github.javaxcel.out.ExcelWriter#defaultValue(String)}
     *                     2. {@link ExcelColumn#defaultValue()}
     *                     3. {@link ExcelModel#defaultValue()}
     * @param field        field of model
     * @return origin value or default value
     */
    @Nullable
    public static String convertIfFaulty(String maybeFaulty, String defaultValue, Field field) {
        if (!StringUtils.isNullOrEmpty(maybeFaulty)) return maybeFaulty;

        // Default value assigned by ExcelWriter takes precedence over ExcelColumn's default value.
        if (!StringUtils.isNullOrEmpty(defaultValue)) return defaultValue;

        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
        if (excelColumn != null && !excelColumn.defaultValue().equals("")) {
            return excelColumn.defaultValue();
        }

        return null;
    }

}
