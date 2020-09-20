package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.exception.GettingFieldValueException;
import io.github.imsejin.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FieldUtils {

    private FieldUtils() {
    }

    /**
     * Gets the targeted fields.
     *
     * @param type type of the object
     * @return targeted fields
     * @see ExcelModel#policy()
     * @see TargetedFieldPolicy
     * @see Class#getDeclaredFields()
     * @see FieldUtils#getInheritedFields(Class)
     */
    public static List<Field> getTargetedFields(Class<?> type) {
        // @ExcelModel의 타깃 필드 정책에 따라 가져오는 필드가 다르다
        ExcelModel annotation = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(type.getDeclaredFields())
                : getInheritedFields(type).stream();

        // Excludes the fields annotated @ExcelIgnore.
        return stream.filter(field -> field.getAnnotation(ExcelIgnore.class) == null)
                .collect(Collectors.toList());
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
     * If {@link ExcelColumn#value()} is not null and not empty,
     * this returns a header name defined in the field.
     * Otherwise returns name of the field.
     *
     * @param fields targeted fields
     * @return list of {@link ExcelColumn#value()} or {@link Field#getName()}
     */
    public static String[] toHeaderNames(List<Field> fields) {
        return fields.stream().map(field -> {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            return annotation == null || StringUtils.isNullOrEmpty(annotation.value()) ? field.getName() : annotation.value();
        }).toArray(String[]::new);
    }

    /**
     * Converts fields to entries.
     *
     * @param vo     object in list
     * @param fields targeted fields
     * @param <T>    type of the object
     * @return entries in which key is VO's field name and value is value of the field
     * @see Field#getName()
     * @see FieldUtils#getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toEntries(T vo, List<Field> fields) {
        Map<String, Object> entries = new HashMap<>();
        for (Field field : fields) {
            entries.put(field.getName(), getFieldValue(vo, field));
        }

        return entries;
    }

    static <T> Object getFieldValue(T vo, Field field) {
        // Enables to have access to the field even private field.
        field.setAccessible(true);

        try {
            // Returns value in the field.
            return field.get(vo);
        } catch (IllegalAccessException e) {
            throw new GettingFieldValueException(e, vo.getClass(), field);
        }
    }

}
