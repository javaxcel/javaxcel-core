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

import static sun.util.locale.LanguageTag.*;

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
                if (isLanguage(language)) {
                    return Locale.forLanguageTag(language);
                }

                // Creates a locale instance with brand-new language.
                return new Locale(language);

            case 2:
                language = segments[0];
                country = segments[1];

                // Uses the cached locale instance.
                if (isLanguage(language) && isRegion(country)) {
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
                if (isLanguage(language) && isRegion(country) && isVariant(variant)) {
                    return new Locale.Builder().setLanguage(language).setRegion(country).setVariant(variant).build();
                }

                // Creates a locale instance with brand-new language, country and variant.
                return new Locale(language, country, variant);
        }
    }

}
