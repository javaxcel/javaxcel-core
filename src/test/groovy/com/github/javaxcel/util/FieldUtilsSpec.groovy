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

import java.lang.reflect.Field
import java.util.function.Function

import static java.util.stream.Collectors.toList
import static java.util.stream.Collectors.toMap

class FieldUtilsSpec extends Specification {

    def "Gets the targeted fields from the class"() {
        given:
        def toNames = { List<Field> fields -> fields.stream().map({ it.name }).collect toList() }

        when:
        def fields = FieldUtils.getTargetedFields type

        then:
        fields.size() == expected.size()
        toNames(fields) == expected

        where:
        type                       | expected
        NoFieldSample              | []
        PlainSample                | ["f0", "f1"]
        ExcelColumnSample          | ["f0", "f1"]
        ExplicitSample             | ["f0"]
        ExcludeSuperSample         | ["f1"]
        IncludeSuperSample         | ["f0", "f1"]
        ExplicitIncludeSuperSample | ["f0"]
    }

    def "Converts the fields to their names"() {
        given:
        def fields = FieldUtils.getTargetedFields type

        when:
        def headerNames = FieldUtils.toHeaderNames(fields, false)

        then:
        headerNames == expected

        where:
        type              | expected
        NoFieldSample     | []
        PlainSample       | ["f0", "f1"]
        ExcelColumnSample | ["FIELD_0", "f1"]
        ExplicitSample    | ["f0"]
    }

    def "Converts java object to map"() {
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
        type                       | f0     | f1        || expected
        NoFieldSample              | 0.27   | "none"    || [:]
        PlainSample                | 5.6    | "alpha"   || [f0: f0, f1: f1]
        ExcelColumnSample          | 3.14   | "beta"    || [f0: f0, f1: f1]
        ExplicitSample             | -1.141 | "gamma"   || [f0: f0]
        ExcludeSuperSample         | 15.942 | "delta"   || [f1: f1]
        IncludeSuperSample         | -0.1   | "epsilon" || [f0: f0, f1: f1]
        ExplicitIncludeSuperSample | 0      | "zeta"    || [f0: f0]
    }

    // -------------------------------------------------------------------------------------------------

    private static class NoFieldSample {
    }

    private static class PlainSample {
        Double f0
        String f1
    }

    private static class ExcelColumnSample {
        @ExcelColumn(name = "FIELD_0")
        Double f0
        String f1
    }

    @ExcelModel(explicit = true)
    private static class ExplicitSample {
        @ExcelColumn
        Double f0
        String f1
    }

    @ExcelModel(explicit = true)
    private static class Parent {
        @ExcelColumn
        Double f0
    }

    private static class ExcludeSuperSample extends Parent {
        String f1
    }

    @ExcelModel(includeSuper = true)
    private static class IncludeSuperSample extends Parent {
        String f1
    }

    @ExcelModel(explicit = true, includeSuper = true)
    private static class ExplicitIncludeSuperSample extends Parent {
        String f1
    }

}
