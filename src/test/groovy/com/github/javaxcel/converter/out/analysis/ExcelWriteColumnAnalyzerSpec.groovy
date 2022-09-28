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

package com.github.javaxcel.converter.out.analysis

import com.github.javaxcel.TestUtils
import com.github.javaxcel.annotation.ExcelColumn
import com.github.javaxcel.annotation.ExcelModel
import com.github.javaxcel.out.strategy.impl.DefaultValue
import com.github.javaxcel.out.strategy.impl.UseGetters
import com.github.javaxcel.util.FieldUtils
import spock.lang.Specification

class ExcelWriteColumnAnalyzerSpec extends Specification {

    def "Analyzes with access option"() {
        given:
        def model = new Sample(values: ["sample", "item"])
        def analyzer = new ExcelWriteColumnAnalyzer(model.class)
        def fields = FieldUtils.getTargetedFields(model.class)

        when: "Accesses value through field"
        def analyses = analyzer.analyze(fields)

        then:
        analyses.size() == fields.size()
        analyses[0].getValue(model) == model.@values
        analyses[0].getValue(model) != model.values

        when: "Accesses value through getter"
        analyses = analyzer.analyze(fields, new UseGetters())

        then:
        analyses.size() == fields.size()
        analyses[0].getValue(model) != model.@values
        analyses[0].getValue(model) == model.values
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def "Analyzes"() {
        given:
        def analyzer = new ExcelWriteColumnAnalyzer(type)
        def fields = FieldUtils.getTargetedFields(type)
        def model = TestUtils.randomize(type)

        when:
        def analyses = analyzer.analyze(fields, arguments as Object[])

        then:
        analyses.size() == fields.size()
        analyses*.defaultValue == defaultValues
        (0..<analyses.size()).each {
            def analysis = analyses[it]
            def fieldName = fields[it].name
            assert analysis.getValue(model) == model[fieldName]
        }

        where:
        type  | arguments               || defaultValues
        Plain | []                      || [null, null, "0.00", null]
        Plain | [new DefaultValue("-")] || ["-", "-", "-", "-"]
        Model | []                      || ["(empty)", "none", "(empty)", "[]"]
        Model | [new DefaultValue("-")] || ["-", "-", "-", "-"]
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample {
        Set<String> values

        Set<String> getValues() {
            values.collect { "#" + it }
        }
    }

    private static class Plain {
        Long id
        String name
        @ExcelColumn(defaultValue = "0.00")
        BigDecimal price
        Set<String> tags
    }

    @ExcelModel(defaultValue = "(empty)")
    private static class Model {
        Long id
        @ExcelColumn(defaultValue = "none")
        String name
        BigDecimal price
        @ExcelColumn(defaultValue = "[]")
        Set<String> tags
    }

}
