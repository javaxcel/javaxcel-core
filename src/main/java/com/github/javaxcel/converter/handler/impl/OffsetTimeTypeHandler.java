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

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeTypeHandler extends AbstractExcelTypeHandler<OffsetTime> {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss Z");

    public OffsetTimeTypeHandler() {
        super(OffsetTime.class);
    }

    @Override
    protected String writeInternal(OffsetTime value, Object... args) {
        // Resolve field from arguments.
        Field field = FieldUtils.resolveFirst(Field.class, args);
        if (field == null) return DEFAULT_FORMATTER.format(value);

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return DEFAULT_FORMATTER.format(value);
        } else {
            return DateTimeFormatter.ofPattern(annotation.pattern()).format(value);
        }
    }

    @Override
    public OffsetTime read(String value, Object... args) {
        // Resolve field from arguments.
        Field field = FieldUtils.resolveFirst(Field.class, args);
        if (field == null) return OffsetTime.parse(value, DEFAULT_FORMATTER);

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return OffsetTime.parse(value, DEFAULT_FORMATTER);
        } else {
            return OffsetTime.parse(value, DateTimeFormatter.ofPattern(annotation.pattern()));
        }
    }

}
