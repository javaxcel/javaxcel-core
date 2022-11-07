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

package com.github.javaxcel.converter.handler.impl

import com.github.javaxcel.converter.handler.ExcelTypeHandler
import com.github.javaxcel.converter.handler.impl.io.FileTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.BooleanTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.ByteTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.CharacterTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.DoubleTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.FloatTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.IntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.LongTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.ShortTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.StringTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigDecimalTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigIntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.net.URITypeHandler
import com.github.javaxcel.converter.handler.impl.net.URLTypeHandler
import com.github.javaxcel.converter.handler.impl.nio.file.PathTypeHandler
import com.github.javaxcel.converter.handler.impl.time.InstantTypeHandler
import com.github.javaxcel.converter.handler.impl.time.LocalDateTimeTypeHandler
import com.github.javaxcel.converter.handler.impl.time.LocalDateTypeHandler
import com.github.javaxcel.converter.handler.impl.time.LocalTimeTypeHandler
import com.github.javaxcel.converter.handler.impl.time.MonthDayTypeHandler
import com.github.javaxcel.converter.handler.impl.time.MonthTypeHandler
import com.github.javaxcel.converter.handler.impl.time.OffsetDateTimeTypeHandler
import com.github.javaxcel.converter.handler.impl.time.OffsetTimeTypeHandler
import com.github.javaxcel.converter.handler.impl.time.YearMonthTypeHandler
import com.github.javaxcel.converter.handler.impl.time.YearTypeHandler
import com.github.javaxcel.converter.handler.impl.time.ZonedDateTimeTypeHandler
import com.github.javaxcel.converter.handler.impl.util.DateTypeHandler
import com.github.javaxcel.converter.handler.impl.util.LocaleTypeHandler
import com.github.javaxcel.converter.handler.impl.util.UUIDTypeHandler
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ExcelTypeHandlerSpec extends Specification {

    @Unroll("#impl.class.simpleName: origin(#origin) <=> written(#written)")
    def "test"() {
        given:
        def handler = impl as ExcelTypeHandler

        when:
        def writtenValue = handler.write(origin)
        def readValue = handler.read(written)

        then:
        written == writtenValue
        origin == readValue

        where:
        impl                            || origin                                                                       | written
        // primitive
        new BooleanTypeHandler(true)    || false as boolean                                                             | "false"
        new ByteTypeHandler(true)       || 2 as byte                                                                    | "2"
        new ShortTypeHandler(true)      || -56 as short                                                                 | "-56"
        new CharacterTypeHandler(true)  || 'A' as char                                                                  | "A"
        new IntegerTypeHandler(true)    || 1024 as int                                                                  | "1024"
        new LongTypeHandler(true)       || -789456L as long                                                             | "-789456"
        new FloatTypeHandler(true)      || 3.14F as float                                                               | "3.14"
        new DoubleTypeHandler(true)     || -1.141414D as double                                                         | "-1.141414"
        // java.lang
        new BooleanTypeHandler()        || Boolean.TRUE                                                                 | "true"
        new ByteTypeHandler()           || new Byte("-2")                                                               | "-2"
        new ShortTypeHandler()          || new Short("56")                                                              | "56"
        new CharacterTypeHandler()      || new Character('Z' as char)                                                   | "Z"
        new IntegerTypeHandler()        || new Integer("-1024")                                                         | "-1024"
        new LongTypeHandler()           || new Long("789456")                                                           | "789456"
        new FloatTypeHandler()          || new Float("-3.14")                                                           | "-3.14"
        new DoubleTypeHandler()         || new Double("1.141414")                                                       | "1.141414"
        new StringTypeHandler()         || "alpha-beta"                                                                 | "alpha-beta"
        // java.math
        new BigIntegerTypeHandler()     || new BigInteger("82487158456540")                                             | "82487158456540"
        new BigDecimalTypeHandler()     || new BigDecimal("3.141592653580")                                             | "3.14159265358"
        // java.util
        new DateTypeHandler()           || new Date(2022 - 1900, 12 - 1, 31, 23, 59, 59)                                | "2022-12-31 23:59:59"
        new UUIDTypeHandler()           || UUID.fromString("d7930b58-f7b0-43c0-af15-08c0f99e33df")                      | "d7930b58-f7b0-43c0-af15-08c0f99e33df"
        new LocaleTypeHandler()         || Locale.ROOT                                                                  | ""
        new LocaleTypeHandler()         || Locale.ENGLISH                                                               | "en"
        new LocaleTypeHandler()         || Locale.US                                                                    | "en_US"
        new LocaleTypeHandler()         || new Locale("lang4", "cntry", "Var")                                          | "lang4_CNTRY_Var"
        new LocaleTypeHandler()         || new Locale("LANG4", "", "Var")                                               | "lang4__Var"
        new LocaleTypeHandler()         || new Locale("", "cntry", "Var")                                               | "_CNTRY_Var"
        new LocaleTypeHandler()         || new Locale("", "", "Var")                                                    | "__Var"
        new LocaleTypeHandler()         || new Locale("", "cntry", "")                                                  | "_CNTRY"
        // java.time
        new YearTypeHandler()           || Year.of(2020)                                                                | "2020"
        new YearMonthTypeHandler()      || YearMonth.of(2014, 12)                                                       | "2014-12"
        new MonthTypeHandler()          || Month.of(6)                                                                  | "06"
        new MonthDayTypeHandler()       || MonthDay.of(2, 29)                                                           | "02-29"
        new LocalTimeTypeHandler()      || LocalTime.of(12, 34, 56)                                                     | "12:34:56"
        new LocalDateTypeHandler()      || LocalDate.of(2002, 5, 31)                                                    | "2002-05-31"
        new LocalDateTimeTypeHandler()  || LocalDateTime.of(1999, 12, 31, 5, 9, 59)                                     | "1999-12-31 05:09:59"
        new ZonedDateTimeTypeHandler()  || ZonedDateTime.of(2022, 9, 14, 9, 30, 7, 0, ZoneId.of("America/Los_Angeles")) | "2022-09-14 09:30:07 -0700/PDT"
        new OffsetTimeTypeHandler()     || OffsetTime.of(1, 55, 9, 0, ZoneOffset.ofHoursMinutes(-4, -30))               | "01:55:09 -0430"
        new OffsetDateTimeTypeHandler() || OffsetDateTime.of(1592, 5, 23, 18, 47, 3, 0, ZoneOffset.ofHours(9))          | "1592-05-23 18:47:03 +0900"
        new InstantTypeHandler()        || Instant.ofEpochSecond(1663134385)                                            | "2022-09-14 05:46:25"
        // java.net
        new URITypeHandler()            || URI.create("alpha/beta/gamma")                                               | "alpha/beta/gamma"
        new URLTypeHandler()            || new URL("https://github.com")                                                | "https://github.com"
        // java.io
        new FileTypeHandler()           || new File("/usr", "local")                                                    | "${File.separator}usr${File.separator}local"
        // java.nio.file
        new PathTypeHandler()           || Paths.get("/usr", "local")                                                   | "${File.separator}usr${File.separator}local"
    }

}
