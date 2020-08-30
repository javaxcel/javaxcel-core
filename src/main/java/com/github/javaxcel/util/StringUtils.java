package com.github.javaxcel.util;

import java.util.function.Supplier;

public final class StringUtils {

    private StringUtils() {}

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String ifNullOrEmpty(String str, String defaultValue) {
        return isNullOrEmpty(str) ? defaultValue : str;
    }

    public static String ifNullOrEmpty(String str, Supplier<String> defaultValueSupplier) {
        return isNullOrEmpty(str) ? defaultValueSupplier.get() : str;
    }

    public static boolean anyMatches(String criterion, CharSequence... strs) {
        if (criterion == null) return false;

        for (CharSequence str : strs) {
            if (criterion.contentEquals(str)) return true;
        }

        return false;
    }

}
