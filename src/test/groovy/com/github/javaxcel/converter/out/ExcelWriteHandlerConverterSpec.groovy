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

import com.github.javaxcel.analysis.ExcelAnalysis
import com.github.javaxcel.analysis.ExcelAnalysisImpl
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.internal.Array1D
import com.github.javaxcel.internal.Array2D
import com.github.javaxcel.internal.Array3D
import com.github.javaxcel.internal.TimeUnitTypeHandler
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Field
import java.nio.file.AccessMode
import java.util.concurrent.TimeUnit

class ExcelWriteHandlerConverterSpec extends Specification {

    def "Converts 1D array"() {
        given:
        def model = new Array1D(array)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
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
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
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
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
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

    def "Converts enum by custom handler"() {
        given:
        def registry = new DefaultExcelTypeHandlerRegistry()
        registry.add(new TimeUnitTypeHandler())
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, registry)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName    | model                                          || expected
        "accessMode" | new EnumModel(accessMode: null)                || null
        "accessMode" | new EnumModel(accessMode: AccessMode.READ)     || "READ"
        "accessMode" | new EnumModel(accessMode: AccessMode.WRITE)    || "WRITE"
        "accessMode" | new EnumModel(accessMode: AccessMode.EXECUTE)  || "EXECUTE"
        "timeUnit"   | new EnumModel(timeUnit: null)                  || null
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.DAYS)         || "days"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.HOURS)        || "hrs"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.MINUTES)      || "min"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.SECONDS)      || "sec"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.MILLISECONDS) || "ms"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.MICROSECONDS) || "μs"
        "timeUnit"   | new EnumModel(timeUnit: TimeUnit.NANOSECONDS)  || "ns"
    }

    def "Converts iterable and array"() {
        given:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteHandlerConverter(analyses, new DefaultExcelTypeHandlerRegistry())
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName           | model                                                                       || expected
        "collection_array"  | new IterableArray(collection_array: [])                                     || "[]"
        "collection_array"  | new IterableArray(collection_array: [[], [1, 2, 3], [4], null, [5, 6]])     || "[[], [1, 2, 3], [4], , [5, 6]]"
        "list_2d_array"     | new IterableArray(list_2d_array: [[["a"], ["b"]], [["c", "d"]], [["e"]]])   || "[[[a], [b]], [[c, d]], [[e]]]"
        "iterable_iterable" | new IterableArray(iterable_iterable: [[2.5, 3.2], null, [-0.14, null], []]) || "[[2.5, 3.2], , [-0.14, ], []]"
    }

    // -------------------------------------------------------------------------------------------------

    private static Iterable<ExcelAnalysis> analyze(Field[] fields, int flags) {
        fields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)
            analysis.addFlags(ExcelWriteAnalyzer.HANDLER | flags)
            analysis
        }
    }

    @EqualsAndHashCode
    private static class EnumModel {
        AccessMode accessMode
        TimeUnit timeUnit
    }

    @EqualsAndHashCode
    private static class IterableArray {
        Collection<int[]> collection_array
        List<String>[][] list_2d_array
        Iterable<Iterable<BigDecimal>> iterable_iterable
    }

}
