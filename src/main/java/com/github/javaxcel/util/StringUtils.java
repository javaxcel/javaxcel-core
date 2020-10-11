package com.github.javaxcel.util;

import java.util.function.Supplier;

/**
 * String utilities
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Checks whether the string is null or empty.
     *
     * <pre>{@code
     *     isNullOrEmpty(null);     // true
     *     isNullOrEmpty("");       // true
     *     isNullOrEmpty("abc");    // false
     * }</pre>
     *
     * @param str string
     * @return whether the string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * If the string is null or empty, this returns default string.
     * If not, this returns original string.
     *
     * <pre>{@code
     *     ifNullOrEmpty(null, "(empty)");      // (empty)
     *     ifNullOrEmpty("", "(empty)");        // (empty)
     *     ifNullOrEmpty(" ", "(empty)");       // \u0020
     * }</pre>
     *
     * @param str          original string
     * @param defaultValue default string
     * @return original string or default string
     */
    public static String ifNullOrEmpty(String str, String defaultValue) {
        return isNullOrEmpty(str) ? defaultValue : str;
    }

    /**
     * If the string is null or empty, this returns default string.
     * If not, this returns original string.
     *
     * <pre>{@code
     *     ifNullOrEmpty(null, () -> "(empty)");    // (empty)
     *     ifNullOrEmpty("", () -> "(empty)");      // (empty)
     *     ifNullOrEmpty(" ", () -> "(empty)");     // \u0020
     * }</pre>
     *
     * @param str      original string
     * @param supplier supplier that returns default string
     * @return original string or default string
     */
    public static String ifNullOrEmpty(String str, Supplier<String> supplier) {
        return isNullOrEmpty(str) ? supplier.get() : str;
    }

}
