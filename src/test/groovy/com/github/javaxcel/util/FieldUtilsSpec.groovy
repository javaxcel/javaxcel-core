/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.util

import com.github.javaxcel.annotation.ExcelColumn
import com.github.javaxcel.annotation.ExcelModel
import io.github.imsejin.common.util.ReflectionUtils
import spock.lang.Specification

import javax.xml.bind.annotation.XmlAccessType
import java.nio.file.AccessMode
import java.util.concurrent.TimeUnit
import java.util.function.Function

import static java.util.stream.Collectors.toMap

class FieldUtilsSpec extends Specification {

    def "Gets the targeted fields from the class"() {
        when:
        def fields = FieldUtils.getTargetedFields type

        then:
        fields.size() == expected.size()
        fields*.name == expected

        where:
        type                     | expected
        NoField                  | []
        Plain                    | ["f0", "f1"]
        SpecifiedColumn          | ["f0", "f1"]
        Explicit                 | ["f0"]
        ExcludedSuper            | ["f1"]
        IncludedSuper            | ["f0", "f1"]
        ExplicitAndIncludedSuper | ["f0"]
    }

    def "Converts the fields into their names"() {
        given:
        def fields = FieldUtils.getTargetedFields type

        when:
        def headerNames = FieldUtils.toHeaderNames(fields, false)

        then:
        headerNames == expected

        where:
        type            | expected
        NoField         | []
        Plain           | ["f0", "f1"]
        SpecifiedColumn | ["FIELD_0", "f1"]
        Explicit        | ["f0"]
    }

    def "Converts java object into map"() {
        given:
        def model = ReflectionUtils.instantiate type
        def fields = FieldUtils.getTargetedFields type

        // Set value to the field dynamically.
        def keyMap = model.properties.keySet().stream().collect toMap(Function.identity(), Function.identity())
        Optional.ofNullable(keyMap["f0"]).ifPresent { model["f0"] = f0 }
        Optional.ofNullable(keyMap["f1"]).ifPresent { model["f1"] = f1 }

        when:
        def map = FieldUtils.toMap(model, fields)

        then:
        map == expected

        where:
        type                     | f0     | f1        || expected
        NoField                  | 0.27   | "none"    || [:]
        Plain                    | 5.6    | "alpha"   || [f0: f0, f1: f1]
        SpecifiedColumn          | 3.14   | "beta"    || [f0: f0, f1: f1]
        Explicit                 | -1.141 | "gamma"   || [f0: f0]
        ExcludedSuper            | 15.942 | "delta"   || [f1: f1]
        IncludedSuper            | -0.1   | "epsilon" || [f0: f0, f1: f1]
        ExplicitAndIncludedSuper | 0      | "zeta"    || [f0: f0]
    }

    def "Resolves the first matched object in arguments"() {
        when:
        def resolution = FieldUtils.resolveFirst(type, arguments as Object[])

        then:
        resolution == expected

        where:
        type       | arguments                                                  || expected
        Object     | []                                                         || null
        Class      | [0, 1, 2, 3]                                               || null
        Object     | [null, 1, 2, 3]                                            || 1
        Number     | [new Object(), "alpha", 0.15, 10]                          || 0.15
        String     | [2, null, "beta", String, "gamma"]                         || "beta"
        Enum       | [AccessMode.READ, TimeUnit.DAYS, XmlAccessType.FIELD]      || AccessMode.READ
        Comparable | [0, "delta", 128L, 3.14D, BigInteger.ZERO, BigDecimal.TEN] || 0
    }

    def "Resolves the last matched object in arguments"() {
        when:
        def resolution = FieldUtils.resolveLast(type, arguments as Object[])

        then:
        resolution == expected

        where:
        type       | arguments                                                  || expected
        Object     | []                                                         || null
        Class      | [0, 1, 2, 3]                                               || null
        Object     | [null, 1, 2, 3]                                            || 3
        Number     | [new Object(), "alpha", 0.15, 10]                          || 10
        String     | [2, null, "beta", String, "gamma"]                         || "gamma"
        Enum       | [AccessMode.READ, TimeUnit.DAYS, XmlAccessType.FIELD]      || XmlAccessType.FIELD
        Comparable | [0, "delta", 128L, 3.14D, BigInteger.ZERO, BigDecimal.TEN] || BigDecimal.TEN
    }

    def "Resolves the actual type of field"() {
        given:
        def field = Sample.declaredFields.find({ it.name == fieldName })

        when:
        println fieldName
        println field.type
        println field.genericType
        println "--------------------------------"

        then:
        def actualType = FieldUtils.resolveActualType(field)
        actualType == expected

        where:
        fieldName                                             | expected
        "concrete"                                            | Long
        "raw"                                                 | Sample
        "generic"                                             | Sample
        "generic_array"                                       | Sample[]
        "type_variable"                                       | Object
        "type_variable_array"                                 | Object[]
        "type_variable_2d_array"                              | Object[][]
        "bounded_type_variable"                               | Number
        "bounded_type_variable_array"                         | Number[]
        "bounded_type_variable_2d_array"                      | Number[][]
        "iterable"                                            | Object
        "iterable_unknown"                                    | Object
        "iterable_concrete"                                   | Long
        "iterable_concrete_array"                             | Long[] //
        "iterable_raw"                                        | Sample
        "iterable_generic"                                    | Sample
        "iterable_upper_wildcard_concrete"                    | Long
        "iterable_lower_wildcard_concrete"                    | Long
        "iterable_upper_wildcard_generic"                     | Sample //
        "iterable_lower_wildcard_generic"                     | Sample //
        "iterable_type_variable"                              | Object
        "iterable_type_variable_array"                        | Object[] //
        "iterable_upper_wildcard_type_variable"               | Object
        "iterable_lower_wildcard_type_variable"               | Object
        "iterable_upper_wildcard_type_variable_array"         | Object[]
        "iterable_lower_wildcard_type_variable_array"         | Object[]
        "iterable_bounded_type_variable"                      | Number //
        "iterable_bounded_type_variable_array"                | Number[] //
        "iterable_upper_wildcard_bounded_type_variable"       | Number //
        "iterable_lower_wildcard_bounded_type_variable"       | Number //
        "iterable_upper_wildcard_bounded_type_variable_array" | Number[]
        "iterable_lower_wildcard_bounded_type_variable_array" | Number[]
        "iterable_iterable_generic"                           | Sample
    }

    // -------------------------------------------------------------------------------------------------

    private static class NoField {
    }

    private static class Plain {
        Double f0
        String f1
    }

    private static class SpecifiedColumn {
        @ExcelColumn(name = "FIELD_0")
        Double f0
        String f1
    }

    @ExcelModel(explicit = true)
    private static class Explicit {
        @ExcelColumn
        Double f0
        String f1
    }

    @ExcelModel(explicit = true)
    private static class Parent {
        @ExcelColumn
        Double f0
    }

    private static class ExcludedSuper extends Parent {
        String f1
    }

    @ExcelModel(includeSuper = true)
    private static class IncludedSuper extends Parent {
        String f1
    }

    @ExcelModel(explicit = true, includeSuper = true)
    private static class ExplicitAndIncludedSuper extends Parent {
        String f1
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample<S extends Number, T> {
        Long concrete
        Sample raw
        Sample<Long, String> generic
        Sample<Long, String>[] generic_array
        T type_variable
        T[] type_variable_array
        T[][] type_variable_2d_array
        S bounded_type_variable
        S[] bounded_type_variable_array
        S[][] bounded_type_variable_2d_array
        List iterable
        List<?> iterable_unknown
        List<Long> iterable_concrete
        List<Long>[] iterable_concrete_array
        List<Sample> iterable_raw
        List<Sample<Integer, ?>> iterable_generic
        List<? extends Long> iterable_upper_wildcard_concrete
        List<? super Long> iterable_lower_wildcard_concrete
        List<? extends Sample<Short, ?>> iterable_upper_wildcard_generic
        List<? super Sample<Byte, ?>> iterable_lower_wildcard_generic
        List<T> iterable_type_variable
        List<T[]> iterable_type_variable_array
        List<? extends T> iterable_upper_wildcard_type_variable
        List<? super T> iterable_lower_wildcard_type_variable
        List<? extends T[]> iterable_upper_wildcard_type_variable_array
        List<? super T[]> iterable_lower_wildcard_type_variable_array
        List<S> iterable_bounded_type_variable
        List<S[]> iterable_bounded_type_variable_array
        List<? extends S> iterable_upper_wildcard_bounded_type_variable
        List<? super S> iterable_lower_wildcard_bounded_type_variable
        List<? extends S[]> iterable_upper_wildcard_bounded_type_variable_array
        List<? super S[]> iterable_lower_wildcard_bounded_type_variable_array
        List<List<Sample<BigInteger, String>>> iterable_iterable_generic
    }

}
