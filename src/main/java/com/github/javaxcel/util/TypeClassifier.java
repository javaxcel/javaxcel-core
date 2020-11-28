package com.github.javaxcel.util;

import java.time.*;

public final class TypeClassifier {

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
        return contains(type, Types.DATETIME);
    }

    /**
     * Checks if type is numeric and primitive.
     *
     * @param type class
     * @return whether type is {@code byte}, {@code short}, {@code int}, {@code long}, {@code float} or {@code double}
     */
    public static boolean isNumericPrimitive(Class<?> type) {
        return contains(type, Types.PRIMITIVE_NUMBER);
    }

    /**
     * Checks if type is numeric and wrapper.
     *
     * @param type class
     * @return whether type is {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Float} or {@link Double}
     */
    public static boolean isNumericWrapper(Class<?> type) {
        return contains(type, Types.WRAPPER_NUMBER);
    }

    /**
     * Checks if type is numeric.
     *
     * @param type class
     * @return whether type is {@code byte}, {@code short}, {@code int}, {@code long}, {@code float}, {@code double},
     * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Float} or {@link Double}
     */
    public static boolean isNumeric(Class<?> type) {
        return contains(type, Types.NUMBER);
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
        return contains(type, Types.PRIMITIVE);
    }

    /**
     * Checks if type is wrapper class.
     *
     * @param type class
     * @return whether type is {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float}, {@link Double}, {@link Character} or {@link Boolean}
     */
    public static boolean isWrapper(Class<?> type) {
        return contains(type, Types.WRAPPER);
    }

    private static boolean contains(Class<?> type, Types types) {
        if (type == null) return false;

        for (Class<?> clazz : types.classes) {
            if (type == clazz) return true;
        }

        return false;
    }

    public enum Types {
        /**
         * Primitive types.
         */
        PRIMITIVE(new Class[]{
                byte.class, short.class, int.class, long.class,
                float.class, double.class, char.class, boolean.class
        }),

        /**
         * Wrapper types.
         */
        WRAPPER(new Class[]{
                Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class, Character.class, Boolean.class
        }),

        /**
         * Numeric types.
         */
        NUMBER(new Class[]{
                byte.class, short.class, int.class, long.class, float.class, double.class,
                Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
        }),

        /**
         * Numeric primitive types.
         */
        PRIMITIVE_NUMBER(new Class[]{
                byte.class, short.class, int.class, long.class, float.class, double.class
        }),

        /**
         * Numeric wrapper types.
         */
        WRAPPER_NUMBER(new Class[]{
                Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
        }),

        /**
         * Datetime types.
         */
        DATETIME(new Class[]{
                LocalTime.class, LocalDate.class, LocalDateTime.class,
                ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class
        });

        private final Class<?>[] classes;

        Types(Class<?>[] classes) {
            this.classes = classes;
        }

        public Class<?>[] getClasses() {
            return this.classes;
        }
    }

}
