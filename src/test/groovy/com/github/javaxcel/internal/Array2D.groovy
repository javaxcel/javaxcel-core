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

package com.github.javaxcel.internal

import io.github.imsejin.common.assertion.Asserts
import io.github.imsejin.common.util.ArrayUtils

class Array2D {

    Array2D(Object array) {
        if (array == null) return
        Asserts.that(array.class).isArray()

        def componentType = ArrayUtils.resolveActualComponentType(array.class)
        Asserts.that(componentType)
                .isNotNull().predicate({ !it.isArray() })

        if (componentType == boolean) this.booleans = array as boolean[][]
        if (componentType == byte) this.bytes = array as byte[][]
        if (componentType == short) this.shorts = array as short[][]
        if (componentType == char) this.chars = array as char[][]
        if (componentType == int) this.ints = array as int[][]
        if (componentType == long) this.longs = array as long[][]
        if (componentType == float) this.floats = array as float[][]
        if (componentType == double) this.doubles = array as double[][]
        if (componentType == Object) this.objects = array as Object[][]
        if (componentType == Locale) this.locales = array as Locale[][]
    }

    boolean[][] booleans
    byte[][] bytes
    short[][] shorts
    char[][] chars
    int[][] ints
    long[][] longs
    float[][] floats
    double[][] doubles
    Object[][] objects
    Locale[][] locales

}
