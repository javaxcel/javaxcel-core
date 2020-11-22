package com.github.javaxcel.util;

import java.time.*;
import java.util.Arrays;
import java.util.List;

public final class TypeClassifier {

    private static final List<Class<?>> PRIMITIVE_TYPES = Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class);

    private static final List<Class<?>> WRAPPER_TYPES = Arrays.asList(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class);

    private static final List<Class<?>> NUMBER_TYPES = Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);

    private static final List<Class<?>> PRIMITIVE_NUMBER_TYPES = Arrays.asList(
            byte.class, short.class, int.class, long.class, float.class, double.class);

    private static final List<Class<?>> DATETIME_TYPES = Arrays.asList(
            LocalTime.class, LocalDate.class, LocalDateTime.class, ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class);

    private TypeClassifier() {
    }

    /**
     * Checks if type is temporal.
     *
     * @param type class
     * @return whether type is {@link LocalDate}, {@link LocalTime}, {@link LocalDateTime}
     * {@link ZonedDateTime}, {@link OffsetDateTime} or {@link OffsetTime}
     */
    public static boolean isTemporal(Class<?> type) {
        return DATETIME_TYPES.contains(type);
    }

    /**
     * Checks if type is primitive and numeric.
     *
     * @param type class
     * @return whether type is {@code byte}, {@code short}, {@code int}, {@code long}, {@code float} or {@code double}
     */
    public static boolean isPrimitiveAndNumeric(Class<?> type) {
        return PRIMITIVE_NUMBER_TYPES.contains(type);
    }

    /**
     * Checks if type is numeric.
     *
     * @param type class
     * @return whether type is {@code byte}, {@code short}, {@code int}, {@code long}, {@code float}, {@code double},
     * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Float} or {@link Double}
     */
    public static boolean isNumeric(Class<?> type) {
        return NUMBER_TYPES.contains(type);
    }

    /**
     * Checks if type is appropriate to write value to cell.
     *
     * @param type class
     * @return if type is writable with excel
     */
    public static boolean isWritable(Class<?> type) {
        return isString(type) || isPrimitive(type) || isWrapper(type);
    }

    /**
     * Checks if type is string.
     *
     * @param type class
     * @return whether type is {@link String}
     */
    public static boolean isString(Class<?> type) {
        return type == String.class;
    }

    /**
     * Checks if type is primitive.
     *
     * @param type class
     * @return whether type is {@code byte}, {@code short}, {@code int}, {@code long},
     * {@code float}, {@code double}, {@code char} or {@code boolean}
     */
    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    /**
     * Checks if type is wrapper class.
     *
     * @param type class
     * @return whether type is {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float}, {@link Double}, {@link Character} or {@link Boolean}
     */
    public static boolean isWrapper(Class<?> type) {
        return WRAPPER_TYPES.contains(type);
    }

}
