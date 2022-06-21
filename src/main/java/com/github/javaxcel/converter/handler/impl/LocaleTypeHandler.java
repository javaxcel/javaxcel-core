/*
 * Copyright 2022 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.converter.handler.impl;

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;

import java.util.Locale;

public class LocaleTypeHandler extends AbstractExcelTypeHandler<Locale> {

    private static final String DELIMITER = "_";

    public LocaleTypeHandler() {
        super(Locale.class);
    }

    @Override
    protected String writeInternal(Locale value, Object... args) {
        boolean l = (value.getLanguage().length() != 0);
        boolean c = (value.getCountry().length() != 0);
        boolean v = (value.getVariant().length() != 0);

        if (!l && !c && !v) return "";

        StringBuilder result = new StringBuilder(value.getLanguage());
        if (!l || c || v) result.append(DELIMITER).append(value.getCountry());
        if (v) result.append(DELIMITER).append(value.getVariant());

        return result.toString();
    }

    @Override
    public Locale read(String value, Object... args) {
        // Uses the cached locale instance with empty language, country and variant.
        if (value.isEmpty()) return Locale.ROOT;

        String language;
        String country;
        String variant;

        String[] segments = value.split(DELIMITER, 3);

        switch (segments.length) {
            case 0:
                // Uses the cached locale instance with empty language, country and variant.
                return Locale.ROOT;

            case 1:
                language = segments[0];

                // Uses the cached locale instance.
                if (LanguageTag.isLanguage(language)) {
                    return Locale.forLanguageTag(language);
                }

                // Creates a locale instance with brand-new language.
                return new Locale(language);

            case 2:
                language = segments[0];
                country = segments[1];

                // Uses the cached locale instance.
                if (LanguageTag.isLanguage(language) && LanguageTag.isRegion(country)) {
                    return new Locale.Builder().setLanguage(language).setRegion(country).build();
                }

                // Creates a locale instance with brand-new language and country.
                return new Locale(language, country);

            case 3:
            default:
                language = segments[0];
                country = segments[1];
                variant = segments[2];

                // Uses the cached locale instance.
                if (LanguageTag.isLanguage(language) && LanguageTag.isRegion(country) && LanguageTag.isVariant(variant)) {
                    return new Locale.Builder().setLanguage(language).setRegion(country).setVariant(variant).build();
                }

                // Creates a locale instance with brand-new language, country and variant.
                return new Locale(language, country, variant);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is a clone of {@link sun.util.locale.LanguageTag}.
     *
     * <p> Since the module system has been introduced in Java 9,
     * internal packages(e.g. 'com.sun.*', 'sun.*', 'jdk.*', ...)
     * should not be used by third-party applications.
     *
     * @see sun.util.locale.LanguageTag
     */
    private static final class LanguageTag {
        public static boolean isLanguage(String s) {
            // language      = 2*3ALPHA            ; shortest ISO 639 code
            //                 ["-" extlang]       ; sometimes followed by
            //                                     ;   extended language subtags
            //               / 4ALPHA              ; or reserved for future use
            //               / 5*8ALPHA            ; or registered language subtag
            int len = s.length();
            return (len >= 2) && (len <= 8) && LocaleUtils.isAlphaString(s);
        }

        public static boolean isRegion(String s) {
            // region        = 2ALPHA              ; ISO 3166-1 code
            //               / 3DIGIT              ; UN M.49 code
            return ((s.length() == 2) && LocaleUtils.isAlphaString(s))
                    || ((s.length() == 3) && LocaleUtils.isNumericString(s));
        }

        public static boolean isVariant(String s) {
            // variant       = 5*8alphanum         ; registered variants
            //               / (DIGIT 3alphanum)
            int len = s.length();
            if (len >= 5 && len <= 8) return LocaleUtils.isAlphaNumericString(s);
            if (len == 4) {
                return LocaleUtils.isNumeric(s.charAt(0))
                        && LocaleUtils.isAlphaNumeric(s.charAt(1))
                        && LocaleUtils.isAlphaNumeric(s.charAt(2))
                        && LocaleUtils.isAlphaNumeric(s.charAt(3));
            }

            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is a clone of {@link sun.util.locale.LocaleUtils}.
     *
     * <p> Since the module system has been introduced in Java 9,
     * internal packages(e.g. 'com.sun.*', 'sun.*', 'jdk.*', ...)
     * should not be used by third-party applications.
     *
     * @see sun.util.locale.LocaleUtils
     */
    private static final class LocaleUtils {
        public static boolean isAlpha(char c) {
            return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
        }

        public static boolean isAlphaString(String s) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                if (!isAlpha(s.charAt(i))) return false;
            }

            return true;
        }

        public static boolean isNumeric(char c) {
            return (c >= '0' && c <= '9');
        }

        public static boolean isNumericString(String s) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                if (!isNumeric(s.charAt(i))) return false;
            }

            return true;
        }

        public static boolean isAlphaNumeric(char c) {
            return isAlpha(c) || isNumeric(c);
        }

        public static boolean isAlphaNumericString(String s) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                if (!isAlphaNumeric(s.charAt(i))) return false;
            }

            return true;
        }
    }

}
