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

package com.github.javaxcel.converter.out

import com.github.javaxcel.analysis.ExcelAnalysisImpl
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.internal.Array1D
import com.github.javaxcel.internal.Array2D
import com.github.javaxcel.internal.Array3D
import spock.lang.Specification

class ExcelWriteHandlerConverterSpec extends Specification {

    def "Converts 1D array"() {
        given:
        def model = new Array1D(array)
        def field = model.class.getDeclaredField(fieldName)
        def analyses = model.class.declaredFields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)
            analysis.addFlags(ExcelWriteAnalyzer.HANDLER | ExcelWriteAnalyzer.GETTER)
            analysis
        }

        when:
        def converter = new ExcelWriteHandlerConverter(new DefaultExcelTypeHandlerRegistry(), analyses)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                                || expected
        "objects"  | null                                                 || null
        "booleans" | [false, true] as boolean[]                           || "[false, true]"
        "bytes"    | [-128, 0, 127] as byte[]                             || "[-128, 0, 127]"
        "shorts"   | [-32768, 0, 32767] as short[]                        || "[-32768, 0, 32767]"
        "chars"    | ['a', 'B', '0', '/'] as char[]                       || "[a, B, 0, /]"
        "ints"     | [74, 0, -12] as int[]                                || "[74, 0, -12]"
        "longs"    | [0, 9720, -8715] as long[]                           || "[0, 9720, -8715]"
        "floats"   | [0.0, 9.745, -1.14157] as float[]                    || "[0.0, 9.745, -1.14157]"
        "doubles"  | [3.141592, -0.0879, 0.0] as double[]                 || "[3.141592, -0.0879, 0.0]"
        "objects"  | [] as Object[]                                       || "[]"
        "objects"  | [null] as Object[]                                   || "[]"
        "objects"  | [new Object() {
            String toString() { "java.lang.Object@x" }
        }] as Object[]                                                    || "[java.lang.Object@x]"
        "locales"  | [] as Locale[]                                       || "[]"
        "locales"  | [null] as Locale[]                                   || "[]"
        "locales"  | [null, Locale.ROOT, null] as Locale[]                || "[, , ]"
        "locales"  | [Locale.US, Locale.KOREA, Locale.FRANCE] as Locale[] || "[en_US, ko_KR, fr_FR]"
    }

    def "Converts 2D array"() {
        given:
        def model = new Array2D(array)
        def field = model.class.getDeclaredField(fieldName)
        def analyses = model.class.declaredFields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)
            analysis.addFlags(ExcelWriteAnalyzer.HANDLER | ExcelWriteAnalyzer.FIELD_ACCESS)
            analysis
        }

        when:
        def converter = new ExcelWriteHandlerConverter(new DefaultExcelTypeHandlerRegistry(), analyses)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                                          || expected
        "objects"  | null                                                           || null
        "booleans" | [[false], [true], [false, true]] as boolean[][]                || "[[false], [true], [false, true]]"
        "bytes"    | [[-128], null, [127]] as byte[][]                              || "[[-128], , [127]]"
        "shorts"   | [[-32768, 0, 32767]] as short[][]                              || "[[-32768, 0, 32767]]"
        "chars"    | [['a', 'B'], [], ['0', '/']] as char[][]                       || "[[a, B], [], [0, /]]"
        "ints"     | [null, [74, 0, -12]] as int[][]                                || "[, [74, 0, -12]]"
        "longs"    | [[0], [], [9720, -8715]] as long[][]                           || "[[0], [], [9720, -8715]]"
        "floats"   | [[0.0], [9.745, -1.14157]] as float[][]                        || "[[0.0], [9.745, -1.14157]]"
        "doubles"  | [[3.141592, -0.0879, 0.0], null] as double[][]                 || "[[3.141592, -0.0879, 0.0], ]"
        "objects"  | [] as Object[][]                                               || "[]"
        "objects"  | [[null]] as Object[][]                                         || "[[]]"
        "objects"  | [[null, new Object() {
            String toString() { "java.lang.Object@x" }
        }]] as Object[][]                                                           || "[[, java.lang.Object@x]]"
        "locales"  | [] as Locale[][]                                               || "[]"
        "locales"  | [[]] as Locale[][]                                             || "[[]]"
        "locales"  | [[], []] as Locale[][]                                         || "[[], []]"
        "locales"  | [[], null, []] as Locale[][]                                   || "[[], , []]"
        "locales"  | [null, [], [], null] as Locale[][]                             || "[, [], [], ]"
        "locales"  | [null, [], null, [null, null]] as Locale[][]                   || "[, [], , [, ]]"
        "locales"  | [null, [Locale.GERMANY, Locale.CHINA], [], null] as Locale[][] || "[, [de_DE, zh_CN], [], ]"
        "locales"  | [[Locale.UK], [], [Locale.ITALY], []] as Locale[][]            || "[[en_GB], [], [it_IT], []]"
    }

    def "Converts 3D array"() {
        given:
        def model = new Array3D(array)
        def field = model.class.getDeclaredField(fieldName)
        def analyses = model.class.declaredFields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)
            analysis.addFlags(ExcelWriteAnalyzer.HANDLER | ExcelWriteAnalyzer.FIELD_ACCESS)
            analysis
        }

        when:
        def converter = new ExcelWriteHandlerConverter(new DefaultExcelTypeHandlerRegistry(), analyses)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName  | array                                                                            || expected
        "objects"  | null                                                                             || null
        "booleans" | [[], [[false], [true]], null] as boolean[][][]                                   || "[[], [[false], [true]], ]"
        "bytes"    | [null, [[-128]], [[127]]] as byte[][][]                                          || "[, [[-128]], [[127]]]"
        "shorts"   | [[[-32768, 0, 32767]]] as short[][][]                                            || "[[[-32768, 0, 32767]]]"
        "chars"    | [[['a'], [], ['B']], [], [['0', '/']]] as char[][][]                             || "[[[a], [], [B]], [], [[0, /]]]"
        "ints"     | [null, [null, [74], [0, -12]]] as int[][][]                                      || "[, [, [74], [0, -12]]]"
        "longs"    | [[[0], null], [], [[9720, -8715], null]] as long[][][]                           || "[[[0], ], [], [[9720, -8715], ]]"
        "floats"   | [[[], [0.0]], [[9.745], [-1.14157]]] as float[][][]                              || "[[[], [0.0]], [[9.745], [-1.14157]]]"
        "doubles"  | [[[3.141592, -0.0879], [0.0]], null] as double[][][]                             || "[[[3.141592, -0.0879], [0.0]], ]"
        "objects"  | [] as Object[][][]                                                               || "[]"
        "objects"  | [null, [[]], []] as Object[][][]                                                 || "[, [[]], []]"
        "objects"  | [[], null, [[null, new Object() {
            String toString() { "java.lang.Object@x" }
        }, null]]] as Object[][][]                                                                    || "[[], , [[, java.lang.Object@x, ]]]"
        "locales"  | [] as Locale[][][]                                                               || "[]"
        "locales"  | [[]] as Locale[][][]                                                             || "[[]]"
        "locales"  | [[], []] as Locale[][][]                                                         || "[[], []]"
        "locales"  | [[], null, []] as Locale[][][]                                                   || "[[], , []]"
        "locales"  | [null, null, null, null] as Locale[][][]                                         || "[, , , ]"
        "locales"  | [null, [[], []], [], null] as Locale[][][]                                       || "[, [[], []], [], ]"
        "locales"  | [[[Locale.US, Locale.ENGLISH], [Locale.KOREA, Locale.KOREAN]]] as Locale[][][]   || "[[[en_US, en], [ko_KR, ko]]]"
        "locales"  | [[null, [null, Locale.ROOT], [Locale.JAPAN, Locale.TAIWAN]], []] as Locale[][][] || "[[, [, ], [ja_JP, zh_TW]], []]"
    }

}
