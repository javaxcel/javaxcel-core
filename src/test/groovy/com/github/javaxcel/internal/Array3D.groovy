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

class Array3D {

    def static DIMENSION = 3

    Array3D(Object array) {
        if (array == null) return
        Asserts.that(array.class).isArray()

        def componentType = array.class
        for (i in 1..DIMENSION) {
            componentType = componentType.componentType
        }

        Asserts.that(componentType)
                .isNotNull().predicate({ !it.isArray() })

        if (!componentType.isPrimitive()) {
            this.localeArray = array as Locale[][][]
            return
        }

        if (componentType == boolean) this.booleanArray = array as boolean[][][]
        if (componentType == byte) this.byteArray = array as byte[][][]
        if (componentType == short) this.shortArray = array as short[][][]
        if (componentType == char) this.charArray = array as char[][][]
        if (componentType == int) this.intArray = array as int[][][]
        if (componentType == long) this.longArray = array as long[][][]
        if (componentType == float) this.floatArray = array as float[][][]
        if (componentType == double) this.doubleArray = array as double[][][]
    }

    boolean[][][] booleanArray
    byte[][][] byteArray
    short[][][] shortArray
    char[][][] charArray
    int[][][] intArray
    long[][][] longArray
    float[][][] floatArray
    double[][][] doubleArray
    Locale[][][] localeArray

}