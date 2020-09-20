package com.github.javaxcel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public final class TypeClassifier {

    private TypeClassifier() {}

    private static final List<Class<?>> PRIMITIVE_TYPES = Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class);
    private static final List<Class<?>> WRAPPER_TYPES = Arrays.asList(Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class);
    private static final List<Class<?>> NUMBER_TYPES = Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);
    private static final List<Class<?>> PRIMITIVE_NUMBER_TYPES = Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class);
    private static final List<Class<?>> DATETIME_TYPES = Arrays.asList(LocalTime.class, LocalDate.class, LocalDateTime.class);

    public static boolean isTemporal(Class<?> type) {
        return DATETIME_TYPES.contains(type);
    }

    public static boolean isPrimitiveAndNumeric(Class<?> type) {
        return PRIMITIVE_NUMBER_TYPES.contains(type);
    }

    public static boolean isNumeric(Class<?> type) {
        return NUMBER_TYPES.contains(type);
    }

    /**
     *
     * @param type class
     * @return if the class is writable with excel or not.
     */
    public static boolean isWritable(Class<?> type) {
        return isString(type) || isPrimitive(type) || isWrapper(type);
    }

    /**
     * 자료형이 문자열(java.lang.String)인지 확인한다.
     */
    public static boolean isString(Class<?> type) {
        return type == String.class;
    }

    /**
     * 자료형이 기초형인지 확인한다.
     */
    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    /**
     * 자료형이 래퍼클래스인지 확인한다.
     */
    public static boolean isWrapper(Class<?> type) {
        return WRAPPER_TYPES.contains(type);
    }

}
