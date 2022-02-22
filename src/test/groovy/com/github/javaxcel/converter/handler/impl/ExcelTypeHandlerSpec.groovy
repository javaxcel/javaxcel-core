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
import spock.lang.Specification
import spock.lang.Unroll

class ExcelTypeHandlerSpec extends Specification {

    @Unroll("#impl.class.simpleName: origin(#origin) <=> written(#written)")
    def "test"() {
        given:
        def handler = impl as ExcelTypeHandler

        expect:
        written == handler.write(origin)
        origin == handler.read(written)

        where:
        impl                           || origin                                                  | written
        new BooleanTypeHandler(true)   || false                                                   | "false"
        new ByteTypeHandler(true)      || 2 as byte                                               | "2"
        new ShortTypeHandler(true)     || -56 as short                                            | "-56"
        new CharacterTypeHandler(true) || 'A' as char                                             | "A"
        new IntegerTypeHandler(true)   || 1024                                                    | "1024"
        new LongTypeHandler(true)      || -789456L                                                | "-789456"
        new FloatTypeHandler(true)     || 3.14F                                                   | "3.14"
        new DoubleTypeHandler(true)    || -1.141414D                                              | "-1.141414"
        new BooleanTypeHandler()       || Boolean.TRUE                                            | "true"
        new ByteTypeHandler()          || new Byte("-2")                                          | "-2"
        new ShortTypeHandler()         || new Short("56")                                         | "56"
        new CharacterTypeHandler()     || new Character('Z' as char)                              | "Z"
        new IntegerTypeHandler()       || new Integer("-1024")                                    | "-1024"
        new LongTypeHandler()          || new Long("789456")                                      | "789456"
        new FloatTypeHandler()         || new Float("-3.14")                                      | "-3.14"
        new DoubleTypeHandler()        || new Double("1.141414")                                  | "1.141414"
        new BigIntegerTypeHandler()    || new BigInteger("82487158456540")                        | "82487158456540"
        new BigDecimalTypeHandler()    || new BigDecimal("3.141592653580")                        | "3.14159265358"
        new DateTypeHandler()          || new Date(2022 - 1900, 12 - 1, 31, 23, 59, 59)           | "2022-12-31 23:59:59"
        new UUIDTypeHandler()          || UUID.fromString("d7930b58-f7b0-43c0-af15-08c0f99e33df") | "d7930b58-f7b0-43c0-af15-08c0f99e33df"
        new LocaleTypeHandler()        || Locale.ROOT                                             | ""
        new LocaleTypeHandler()        || Locale.ENGLISH                                          | "en"
        new LocaleTypeHandler()        || Locale.US                                               | "en_US"
        new LocaleTypeHandler()        || new Locale("lang4", "cntry", "Var")                     | "lang4_CNTRY_Var"
        new LocaleTypeHandler()        || new Locale("lang4", "", "Var")                          | "lang4__Var"
        new LocaleTypeHandler()        || new Locale("", "cntry", "Var")                          | "_CNTRY_Var"
        new LocaleTypeHandler()        || new Locale("", "", "Var")                               | "__Var"
        new LocaleTypeHandler()        || new Locale("", "cntry", "")                             | "_CNTRY"
    }

}
