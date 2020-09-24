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
     * Gets the targeted fields depending on the policy.
     *
     * <p> If the targeting policy is {@link TargetedFieldPolicy#OWN_FIELDS},
     * this returns its own declared fields.
     * Otherwise({@link TargetedFieldPolicy#INCLUDES_INHERITED}) this returns
     * its own declared fields including super classes's fields.
     *
     * <p> This doesn't return the fields annotated with {@link ExcelIgnore}.
     *
     * @param type type of the object
     * @return targeted fields
     * @see ExcelModel#policy()
     * @see TargetedFieldPolicy
     * @see ExcelIgnore
     */
    public static List<Field> getTargetedFields(Class<?> type) {
        // Gets fields depending on the policy.
        ExcelModel annotation = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(type.getDeclaredFields())
                : getInheritedFields(type).stream();

        // Excludes the fields to be ignored.
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
     * Converts fields to a map.
     *
     * @param vo     object in list
     * @param fields targeted fields
     * @param <T>    type of the object
     * @return {@link Map} in which key is the model's field name and value is the model's field value
     * @see Field#getName()
     * @see #getFieldValue(Object, Field)
     */
    public static <T> Map<String, Object> toMap(T vo, List<Field> fields) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), getFieldValue(vo, field));
        }

        return map;
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
