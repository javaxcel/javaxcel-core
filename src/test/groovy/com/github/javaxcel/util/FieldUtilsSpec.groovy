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

class FieldUtilsSpec extends Specification {

    def "Gets the targeted fields from the class"() {
        expect:
        FieldUtils.getTargetedFields(type).size() == expected

        where:
        type                       | expected
        NoFieldSample              | 0
        PlainSample                | 2
        ExcelColumnSample          | 2
        ExplicitSample             | 1
        ExcludeSuperSample         | 1
        IncludeSuperSample         | 2
        ExplicitIncludeSuperSample | 1
    }

    def "Converts the fields to their names"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)

        expect:
        FieldUtils.toHeaderNames(fields) == expected

        where:
        type              | expected
        NoFieldSample     | []
        PlainSample       | ["field0", "field1"]
        ExcelColumnSample | ["FIELD_0", "field1"]
        ExplicitSample    | ["field0"]
    }

    def "Converts java object to map"() {
        given:
        def model = ReflectionUtils.instantiate type
        model.field0 = field0
        model.field1 = field1

        expect:
        FieldUtils.toMap(new NoFieldSample()) == [:]
        FieldUtils.toMap(model) == expected

        where:
        type                       | field0 | field1    | expected
        PlainSample                | 5.6    | "alpha"   | [field0: field0, field1: field1]
        ExcelColumnSample          | 3.14   | "beta"    | [field0: field0, field1: field1]
        ExplicitSample             | -1.141 | "gamma"   | [field0: field0]
        ExcludeSuperSample         | 15.942 | "delta"   | [field1: field1]
        IncludeSuperSample         | -0.1   | "epsilon" | [field0: field0, field1: field1]
        ExplicitIncludeSuperSample | 0      | "zeta"    | [field0: field0]
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    static class NoFieldSample {
    }

    static class PlainSample {
        Double field0
        String field1
    }

    static class ExcelColumnSample {
        @ExcelColumn(name = "FIELD_0")
        Double field0
        String field1
    }

    @ExcelModel(explicit = true)
    static class ExplicitSample {
        @ExcelColumn
        Double field0
        String field1
    }

    @ExcelModel(explicit = true)
    static class Parent {
        @ExcelColumn
        Double field0
    }

    static class ExcludeSuperSample extends Parent {
        String field1
    }

    @ExcelModel(includeSuper = true)
    static class IncludeSuperSample extends Parent {
        String field1
    }

    @ExcelModel(explicit = true, includeSuper = true)
    static class ExplicitIncludeSuperSample extends Parent {
        String field1
    }

}
