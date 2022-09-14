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

package com.github.javaxcel.converter.handler.registry.impl;

import com.github.javaxcel.converter.handler.impl.BigDecimalTypeHandler;
import com.github.javaxcel.converter.handler.impl.BigIntegerTypeHandler;
import com.github.javaxcel.converter.handler.impl.BooleanTypeHandler;
import com.github.javaxcel.converter.handler.impl.ByteTypeHandler;
import com.github.javaxcel.converter.handler.impl.CharacterTypeHandler;
import com.github.javaxcel.converter.handler.impl.DateTypeHandler;
import com.github.javaxcel.converter.handler.impl.DoubleTypeHandler;
import com.github.javaxcel.converter.handler.impl.EnumTypeHandler;
import com.github.javaxcel.converter.handler.impl.FileTypeHandler;
import com.github.javaxcel.converter.handler.impl.FloatTypeHandler;
import com.github.javaxcel.converter.handler.impl.InstantTypeHandler;
import com.github.javaxcel.converter.handler.impl.IntegerTypeHandler;
import com.github.javaxcel.converter.handler.impl.LocalDateTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.LocalDateTypeHandler;
import com.github.javaxcel.converter.handler.impl.LocalTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.LocaleTypeHandler;
import com.github.javaxcel.converter.handler.impl.LongTypeHandler;
import com.github.javaxcel.converter.handler.impl.MonthDayTypeHandler;
import com.github.javaxcel.converter.handler.impl.MonthTypeHandler;
import com.github.javaxcel.converter.handler.impl.OffsetDateTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.OffsetTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.PathTypeHandler;
import com.github.javaxcel.converter.handler.impl.ShortTypeHandler;
import com.github.javaxcel.converter.handler.impl.StringTypeHandler;
import com.github.javaxcel.converter.handler.impl.URITypeHandler;
import com.github.javaxcel.converter.handler.impl.URLTypeHandler;
import com.github.javaxcel.converter.handler.impl.UUIDTypeHandler;
import com.github.javaxcel.converter.handler.impl.YearMonthTypeHandler;
import com.github.javaxcel.converter.handler.impl.YearTypeHandler;
import com.github.javaxcel.converter.handler.impl.ZonedDateTimeTypeHandler;

public class DefaultExcelTypeHandlerRegistry extends ExcelTypeHandlerRegistryImpl {

    public DefaultExcelTypeHandlerRegistry() {
        // primitive
        add(new BooleanTypeHandler(true));
        add(new ByteTypeHandler(true));
        add(new ShortTypeHandler(true));
        add(new CharacterTypeHandler(true));
        add(new IntegerTypeHandler(true));
        add(new LongTypeHandler(true));
        add(new FloatTypeHandler(true));
        add(new DoubleTypeHandler(true));
        // java.lang
        add(new BooleanTypeHandler());
        add(new ByteTypeHandler());
        add(new ShortTypeHandler());
        add(new CharacterTypeHandler());
        add(new IntegerTypeHandler());
        add(new LongTypeHandler());
        add(new FloatTypeHandler());
        add(new DoubleTypeHandler());
        add(new StringTypeHandler());
        add(new EnumTypeHandler());
        // java.math
        add(new BigIntegerTypeHandler());
        add(new BigDecimalTypeHandler());
        // java.util
        add(new DateTypeHandler());
        add(new UUIDTypeHandler());
        add(new LocaleTypeHandler());
        // java.time
        add(new YearTypeHandler());
        add(new YearMonthTypeHandler());
        add(new MonthTypeHandler());
        add(new MonthDayTypeHandler());
        add(new LocalTimeTypeHandler());
        add(new LocalDateTypeHandler());
        add(new LocalDateTimeTypeHandler());
        add(new ZonedDateTimeTypeHandler());
        add(new OffsetTimeTypeHandler());
        add(new OffsetDateTimeTypeHandler());
        add(new InstantTypeHandler());
        // java.net
        add(new URITypeHandler());
        add(new URLTypeHandler());
        // java.io
        add(new FileTypeHandler());
        // java.nio.file
        add(new PathTypeHandler());
    }

}
