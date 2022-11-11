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

package com.github.javaxcel.converter.handler.impl.time.temporal;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.util.StringUtils;

import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

public abstract class AbstractTemporalAccessorTypeHandler<T extends TemporalAccessor> extends AbstractExcelTypeHandler<T> {

    private final DateTimeFormatter defaultFormatter;

    protected AbstractTemporalAccessorTypeHandler(Class<T> type, DateTimeFormatter defaultFormatter) {
        super(type);
        this.defaultFormatter = defaultFormatter;
    }

    protected abstract TemporalQuery<T> getTemporalQuery();

    // -------------------------------------------------------------------------------------------------

    @Override
    protected String writeInternal(T value, Object... arguments) {
        // Resolve field from arguments.
        Field field = FieldUtils.resolveFirst(Field.class, arguments);
        if (field == null) {
            return stringify(value, this.defaultFormatter);
        }

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return stringify(value, this.defaultFormatter);
        } else {
            return stringify(value, DateTimeFormatter.ofPattern(annotation.pattern()));
        }
    }

    @Override
    public T read(String value, Object... arguments) {
        // Resolve field from arguments.
        Field field = FieldUtils.resolveFirst(Field.class, arguments);
        if (field == null) {
            return parse(value, this.defaultFormatter);
        }

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return parse(value, this.defaultFormatter);
        } else {
            return parse(value, DateTimeFormatter.ofPattern(annotation.pattern()));
        }
    }

    // -------------------------------------------------------------------------------------------------

    private String stringify(T value, DateTimeFormatter formatter) {
        return formatter.format(value);
    }

    private T parse(String value, DateTimeFormatter formatter) {
        TemporalQuery<T> temporalQuery = getTemporalQuery();
        return formatter.parse(value, temporalQuery);
    }

}
