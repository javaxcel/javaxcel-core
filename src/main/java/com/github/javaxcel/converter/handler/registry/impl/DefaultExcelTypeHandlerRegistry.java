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

import com.github.javaxcel.converter.handler.impl.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.*;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class DefaultExcelTypeHandlerRegistry extends ExcelTypeHandlerRegistryImpl {

    public DefaultExcelTypeHandlerRegistry() {
        // primitive
        add(boolean.class, new BooleanTypeHandler(true));
        add(byte.class, new ByteTypeHandler(true));
        add(short.class, new ShortTypeHandler(true));
        add(char.class, new CharacterTypeHandler(true));
        add(int.class, new IntegerTypeHandler(true));
        add(long.class, new LongTypeHandler(true));
        add(float.class, new FloatTypeHandler(true));
        add(double.class, new DoubleTypeHandler(true));
        // java.lang
        add(Boolean.class, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Short.class, new ShortTypeHandler());
        add(Character.class, new CharacterTypeHandler());
        add(Integer.class, new IntegerTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(String.class, new StringTypeHandler());
        add(Enum.class, new EnumTypeHandler());
        // java.math
        add(BigInteger.class, new BigIntegerTypeHandler());
        add(BigDecimal.class, new BigDecimalTypeHandler());
        // java.util
        add(Date.class, new DateTypeHandler());
        add(UUID.class, new UUIDTypeHandler());
        add(Locale.class, new LocaleTypeHandler());
        // java.time
        add(LocalTime.class, new LocalTimeTypeHandler());
        add(LocalDate.class, new LocalDateTypeHandler());
        add(LocalDateTime.class, new LocalDateTimeTypeHandler());
        add(ZonedDateTime.class, new ZonedDateTimeTypeHandler());
        add(OffsetTime.class, new OffsetTimeTypeHandler());
        add(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
        // java.net
        add(URI.class, new URITypeHandler());
        add(URL.class, new URLTypeHandler());
        // java.io
        add(File.class, new FileTypeHandler());
        // java.nio.file
        add(Path.class, new PathTypeHandler());
    }

}
