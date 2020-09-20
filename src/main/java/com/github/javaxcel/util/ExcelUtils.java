package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.exception.GettingFieldValueException;
import io.github.imsejin.util.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ExcelUtils {

    private ExcelUtils() {}

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
     * @return fields of the type including its inherited fields
     */
    public static List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            fields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
        }

        return fields;
    }

    public static String[] toHeaderNames(List<Field> fields) {
        return fields.stream().map(field -> {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            return annotation == null || StringUtils.isNullOrEmpty(annotation.value()) ? field.getName() : annotation.value();
        }).toArray(String[]::new);
    }

    /**
     * Gets range of the sheets.
     *
     * @param workbook excel workbook
     * @return range that from 0 to (the number of sheets - 1)
     */
    public static int[] getSheetRange(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets()).toArray();
    }

    /**
     * Stringify value of the field.
     *
     * @param field field in object
     * @param vo    object in list
     * @param <T>   type of the object
     * @return value of the field in value object
     */
    public static <T> String stringifyValue(T vo, Field field) {
        Object value = getFieldValue(vo, field);

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

    private static <T> Object getFieldValue(T vo, Field field) {
        // Enables to have access to the field even private field.
        field.setAccessible(true);

        try {
            // Returns value in the field.
            return field.get(vo);
        } catch (IllegalAccessException e) {
            throw new GettingFieldValueException(e, vo.getClass(), field);
        }
    }

    /**
     * Converts a string in cell to the type of field.
     *
     * @param cellValue string in cell of excel sheet
     * @param field     field in object
     * @return value converted to the type of field
     */
    public static Object convertValue(String cellValue, Field field) {
        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
        Class<?> type = field.getType();

        if (excelColumn == null) {
            /*
            Field without @ExcelColumn.
             */

            // Sets up default value to the field.
            if (StringUtils.isNullOrEmpty(cellValue)) return initialValueOf(type);

        } else {
            /*
            Field with @ExcelColumn.
             */

            // Sets up default value to the field.
            if (StringUtils.isNullOrEmpty(cellValue)) {
                String defaultValue = excelColumn.defaultValue();

                // When not explicitly define default value.
                if (defaultValue.equals("")) return initialValueOf(type);
            }
        }

        // Converts string to the type of field.
        return convert(cellValue, field);
    }

    /**
     * Gets initial value of the type.
     *
     * @param type type of the object
     * @return initial value of the type
     */
    private static Object initialValueOf(Class<?> type) {
        // Value of primitive type cannot be null.
        if (TypeClassifier.isPrimitiveAndNumeric(type)) return 0;
        else if (type == char.class) return '\u0000';
        else if (type == boolean.class) return false;

        // The others can be null.
        return null;
    }

    private static Object convert(String value, Field field) {
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
