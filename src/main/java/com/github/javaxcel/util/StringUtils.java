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

}
