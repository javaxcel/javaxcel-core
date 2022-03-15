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

import com.github.javaxcel.converter.handler.registry.impl.ExcelTypeHandlerRegistryImpl
import com.github.javaxcel.internal.Array1D
import com.github.javaxcel.internal.Array2D
import spock.lang.Specification

class DefaultExcelWriteConverterSpec extends Specification {

    def static final EMPTY = new Object[0]

    def "Converts 1D array"() {
        given:
        def converter = new DefaultExcelWriteConverter<>(new ExcelTypeHandlerRegistryImpl())
        def array1D = new Array1D(array)
        def fieldName = array == null ? "objectArray" : array1D.properties.keySet().stream()
                .filter({ it.startsWith(array.class.componentType.simpleName.toLowerCase()) }).find() as String
        def field = Array1D.getDeclaredField(fieldName)

        when:
        def actual = converter.convert(array1D, field)

        then:
        actual == expected

        where:
        array                                  || expected
        null                                   || null
        [false, true] as boolean[]             || "[false, true]"
        [-128, 0, 127] as byte[]               || "[-128, 0, 127]"
        [-32768, 0, 32767] as short[]          || "[-32768, 0, 32767]"
        ['a', 'B', '0', '/'] as char[]         || "[a, B, 0, /]"
        [74, 0, -12] as int[]                  || "[74, 0, -12]"
        [0, 9720, -8715] as long[]             || "[0, 9720, -8715]"
        [0.0, 9.745, -1.14157] as float[]      || "[0.0, 9.745, -1.14157]"
        [3.141592, -0.0879, 0.0] as double[]   || "[3.141592, -0.0879, 0.0]"
        EMPTY                                  || "[]"
        [null] as Object[]                     || "[]"
        [null, null, null] as Object[]         || "[, , ]"
        ["alpha", "beta", "gamma"] as Object[] || "[alpha, beta, gamma]"
    }

    def "Converts 2D array"() {
        given:
        def converter = new DefaultExcelWriteConverter<>(new ExcelTypeHandlerRegistryImpl())
        def array2D = new Array2D(array)
        def fieldName = array == null ? "objectArray" : array2D.properties.keySet().stream()
                .filter({ it.startsWith(array.class.componentType.componentType.simpleName.toLowerCase()) }).find() as String
        def field = Array2D.getDeclaredField(fieldName)

        when:
        def actual = converter.convert(array2D, field)

        then:
        actual == expected

        where:
        array                                                                     || expected
        null                                                                      || null
        [[false], [true], [false, true]] as boolean[][]                           || "[[false], [true], [false, true]]"
        [[-128], [0], [127]] as byte[][]                                          || "[[-128], [0], [127]]"
        [[-32768, 0, 32767]] as short[][]                                         || "[[-32768, 0, 32767]]"
        [['a', 'B'], [], ['0', '/']] as char[][]                                  || "[[a, B], [], [0, /]]"
        [[], [74, 0, -12]] as int[][]                                             || "[[], [74, 0, -12]]"
        [[0], [], [9720, -8715]] as long[][]                                      || "[[0], [], [9720, -8715]]"
        [[0.0], [9.745, -1.14157]] as float[][]                                   || "[[0.0], [9.745, -1.14157]]"
        [[3.141592, -0.0879, 0.0], []] as double[][]                              || "[[3.141592, -0.0879, 0.0], []]"
        [null, EMPTY, null, new Object[]{null, null}] as Object[][]               || "[, [], , [, ]]"
        [EMPTY] as Object[][]                                                     || "[[]]"
        [EMPTY, EMPTY] as Object[][]                                              || "[[], []]"
        [EMPTY, null, EMPTY] as Object[][]                                        || "[[], , []]"
        [EMPTY, null, EMPTY, null] as Object[][]                                  || "[[], , [], ]"
        [null, ["alpha", "beta"] as String[], EMPTY, null] as Object[][]          || "[, [alpha, beta], [], ]"
        [["alpha"] as String[], EMPTY, ["beta"] as String[], EMPTY] as Object[][] || "[[alpha], [], [beta], []]"
    }

}
