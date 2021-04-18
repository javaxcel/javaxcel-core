/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.converter.out;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.TypeClassifier;
import io.github.imsejin.common.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DefaultOutputConverter<T> implements OutputConverter<T> {

    /**
     * {@inheritDoc}
     *
     * @see FieldUtils#getFieldValue(Object, Field)
     * @see ExcelDateTimeFormat#pattern()
     */
    @Nullable
    @Override
    public String convert(T model, Field field) {
        Object value = FieldUtils.getFieldValue(model, field);
        if (value == null) return null;

        // Stringifies datetime with pattern if the value of type is datetime.
        Class<?> type = field.getType();
        if (TypeClassifier.isTemporal(type)) {
            ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
            if (annotation != null && !StringUtils.isNullOrEmpty(annotation.pattern())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.pattern());

                if (type == LocalTime.class) value = ((LocalTime) value).format(formatter);
                else if (type == LocalDate.class) value = ((LocalDate) value).format(formatter);
                else if (type == LocalDateTime.class) value = ((LocalDateTime) value).format(formatter);
                else if (type == ZonedDateTime.class) value = ((ZonedDateTime) value).format(formatter);
                else if (type == OffsetDateTime.class) value = ((OffsetDateTime) value).format(formatter);
                else if (type == OffsetTime.class) value = ((OffsetTime) value).format(formatter);
            }
        }

        // Converts value to string.
        return String.valueOf(value);
    }

}
