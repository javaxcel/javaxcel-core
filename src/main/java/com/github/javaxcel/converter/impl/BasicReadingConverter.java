package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.converter.ReadingConverter;
import com.github.javaxcel.util.TypeClassifier;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BasicReadingConverter<T> implements ReadingConverter<T> {

    /**
     * {@inheritDoc}
     *
     * <p> Converts a string in cell to the type of field.
     *
     * @param value string that is cell value
     * @param field field of model
     * @return value converted to the type of field
     */
    @Override
    public Object convert(String value, Field field) {
        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
        Class<?> type = field.getType();

        // When you don't explicitly define default value and the cell value is null or empty.
        if ((excelColumn == null || excelColumn.defaultValue().equals("")) && StringUtils.isNullOrEmpty(value)) {
            return initialValueOf(type);
        }

        // Converts string to the type of field.
        return parse(value, field);
    }

    /**
     * Gets initial value of the type.
     *
     * @param type type of the object
     * @return initial value of the type
     * @see TypeClassifier#isPrimitiveAndNumeric(Class)
     */
    private static Object initialValueOf(Class<?> type) {
        // Value of primitive type cannot be null.
        if (TypeClassifier.isPrimitiveAndNumeric(type)) return 0;
        else if (type == char.class) return '\u0000';
        else if (type == boolean.class) return false;

        // The others can be null.
        return null;
    }

    private static Object parse(String value, Field field) {
        Class<?> type = field.getType();

        if (type == String.class) return value;
        else if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
        else if (type == short.class || type == Short.class) return Short.parseShort(value);
        else if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        else if (type == long.class || type == Long.class) return Long.parseLong(value);
        else if (type == float.class || type == Float.class) return Float.parseFloat(value);
        else if (type == double.class || type == Double.class) return Double.parseDouble(value);
        else if (type == char.class || type == Character.class) return value.charAt(0);
        else if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        else if (type == BigInteger.class) return new BigInteger(value);
        else if (type == BigDecimal.class) return new BigDecimal(value);
        else if (TypeClassifier.isTemporal(type)) {
            ExcelDateTimeFormat excelDateTimeFormat = field.getAnnotation(ExcelDateTimeFormat.class);
            String pattern = excelDateTimeFormat == null ? null : excelDateTimeFormat.pattern();

            if (StringUtils.isNullOrEmpty(pattern)) {
                // When pattern is undefined or implicitly defined.
                if (type == LocalTime.class) return LocalTime.parse(value);
                else if (type == LocalDate.class) return LocalDate.parse(value);
                else if (type == LocalDateTime.class) return LocalDateTime.parse(value);

            } else {
                // When pattern is explicitly defined.
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (type == LocalTime.class) return LocalTime.parse(value, formatter);
                else if (type == LocalDate.class) return LocalDate.parse(value, formatter);
                else if (type == LocalDateTime.class) return LocalDateTime.parse(value, formatter);
            }
        }

        return null;
    }

}
