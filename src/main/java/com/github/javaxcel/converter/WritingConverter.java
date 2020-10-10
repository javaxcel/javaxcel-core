package com.github.javaxcel.converter;

import com.github.javaxcel.annotation.ExcelColumn;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;

public interface WritingConverter<T> {

    /**
     * If the value is null or empty string, converts a value to default value.
     *
     * @param maybeDefault value that may be null or empty
     * @param defaultValue default value
     * @param field        field of model
     * @return origin value or {@link com.github.javaxcel.out.ExcelWriter#defaultValue(String)}
     */
    static String convertIfDefault(String maybeDefault, String defaultValue, Field field) {
        return StringUtils.ifNullOrEmpty(maybeDefault, () -> {
            // Default value assigned by ExcelWriter takes precedence over ExcelColumn's default value.
            if (!StringUtils.isNullOrEmpty(defaultValue)) return defaultValue;
            ExcelColumn column = field.getAnnotation(ExcelColumn.class);
            return column == null || column.defaultValue().equals("") ? null : column.defaultValue();
        });
    }

    /**
     * Sets up the default value.
     *
     * @param defaultValue default value
     */
    void setDefaultValue(String defaultValue);

    /**
     * Converts a field's value to the string.
     *
     * <p> To write a value in the cell, this converts a field's value
     * to the string. The converted string will written in cell.
     *
     * @param model object in list
     * @param field field of object
     * @return stringified field's value
     */
    String convert(T model, Field field);

}
