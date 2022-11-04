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

import com.github.javaxcel.converter.handler.impl.io.FileTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.BooleanTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.ByteTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.CharacterTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.DoubleTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.EnumTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.FloatTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.IntegerTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.LongTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.ShortTypeHandler;
import com.github.javaxcel.converter.handler.impl.lang.StringTypeHandler;
import com.github.javaxcel.converter.handler.impl.math.BigDecimalTypeHandler;
import com.github.javaxcel.converter.handler.impl.math.BigIntegerTypeHandler;
import com.github.javaxcel.converter.handler.impl.net.URITypeHandler;
import com.github.javaxcel.converter.handler.impl.net.URLTypeHandler;
import com.github.javaxcel.converter.handler.impl.nio.file.PathTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.InstantTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.LocalDateTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.LocalDateTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.LocalTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.MonthDayTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.MonthTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.OffsetDateTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.OffsetTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.YearMonthTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.YearTypeHandler;
import com.github.javaxcel.converter.handler.impl.time.ZonedDateTimeTypeHandler;
import com.github.javaxcel.converter.handler.impl.util.DateTypeHandler;
import com.github.javaxcel.converter.handler.impl.util.LocaleTypeHandler;
import com.github.javaxcel.converter.handler.impl.util.UUIDTypeHandler;

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
