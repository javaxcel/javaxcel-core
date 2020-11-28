package com.github.javaxcel.converter.in;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.util.TypeClassifier;
import io.github.imsejin.common.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class BasicReadingConverter implements ReadingConverter {

    /**
     * Gets initial value of the type.
     *
     * @param type type of the object
     * @return initial value of the type
     * @see TypeClassifier#isNumericPrimitive(Class)
     */
    @Nullable
    private static Object initialValueOf(Class<?> type) {
        // Value of primitive type cannot be null.
        if (TypeClassifier.isNumericPrimitive(type)) return 0;
        else if (type == char.class) return '\u0000';
        else if (type == boolean.class) return false;

        // The others can be null.
        return null;
    }

    @Nullable
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
            ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
            if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
                // When pattern is undefined or implicitly defined.
                if (type == LocalTime.class) return LocalTime.parse(value);
                else if (type == LocalDate.class) return LocalDate.parse(value);
                else if (type == LocalDateTime.class) return LocalDateTime.parse(value);
                else if (type == ZonedDateTime.class) return ZonedDateTime.parse(value);
                else if (type == OffsetDateTime.class) return OffsetDateTime.parse(value);
                else if (type == OffsetTime.class) return OffsetTime.parse(value);

            } else {
                // When pattern is explicitly defined.
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.pattern());
                if (type == LocalTime.class) return LocalTime.parse(value, formatter);
                else if (type == LocalDate.class) return LocalDate.parse(value, formatter);
                else if (type == LocalDateTime.class) return LocalDateTime.parse(value, formatter);
                else if (type == ZonedDateTime.class) return ZonedDateTime.parse(value, formatter);
                else if (type == OffsetDateTime.class) return OffsetDateTime.parse(value, formatter);
                else if (type == OffsetTime.class) return OffsetTime.parse(value, formatter);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Object convert(Map<String, Object> variables, Field field) {
        String value = (String) variables.get(field.getName());

        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
        Class<?> type = field.getType();

        // When you don't explicitly define default value and the cell value is null or empty.
        if ((excelColumn == null || excelColumn.defaultValue().equals("")) && StringUtils.isNullOrEmpty(value)) {
            return initialValueOf(type);
        }

        // Converts string to the type of field.
        return parse(value, field);
    }

}
