package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.converter.WritingConverter;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.util.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BasicWritingConverter<T> implements WritingConverter<T> {

    private String defaultValue;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see FieldUtils#getFieldValue(Object, Field)
     * @see ExcelColumn#defaultValue()
     */
    @Override
    public String convert(T model, Field field) {
        String value = stringify(model, field);
        return WritingConverter.convertIfDefault(value, this.defaultValue, field);
    }

    /**
     * Stringifies a value of the field.
     *
     * @param model object in list
     * @param field field of object
     * @return value of the field in value object
     * @see ExcelDateTimeFormat#pattern()
     */
    private String stringify(T model, Field field) {
        Object value = FieldUtils.getFieldValue(model, field);

        if (value == null) return null;

        // Formats datetime when the value of type is datetime.
        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation != null && !StringUtils.isNullOrEmpty(annotation.pattern())) {
            Class<?> type = field.getType();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.pattern());

            if (type == LocalTime.class) value = ((LocalTime) value).format(formatter);
            else if (type == LocalDate.class) value = ((LocalDate) value).format(formatter);
            else if (type == LocalDateTime.class) value = ((LocalDateTime) value).format(formatter);
        }

        // Converts value to string.
        return String.valueOf(value);
    }

}
