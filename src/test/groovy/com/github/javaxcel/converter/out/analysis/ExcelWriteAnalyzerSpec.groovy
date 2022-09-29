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
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.model.sample.ModelSample
import com.github.javaxcel.model.sample.PlainSample
import com.github.javaxcel.out.strategy.impl.DefaultValue
import com.github.javaxcel.out.strategy.impl.UseGetters
import com.github.javaxcel.util.FieldUtils
import spock.lang.Specification

class ExcelWriteAnalyzerSpec extends Specification {

    def "Analyzes with access option"() {
        given:
        def model = new Sample(values: ["sample", "item"])
        def analyzer = new ExcelWriteAnalyzer(model.class)
        def fields = FieldUtils.getTargetedFields(model.class)
        def arguments = [new DefaultExcelTypeHandlerRegistry()] as Object[]

        when: "Accesses value through field"
        def analyses = analyzer.analyze(fields, arguments)

        then:
        analyses.size() == fields.size()
        analyses[0].getValue(model) == model.@values
        analyses[0].getValue(model) != model.values

        when: "Accesses value through getter"
        arguments += new UseGetters()
        analyses = analyzer.analyze(fields, arguments)

        then:
        analyses.size() == fields.size()
        analyses[0].getValue(model) != model.@values
        analyses[0].getValue(model) == model.values
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def "Analyzes"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)
        def model = TestUtils.randomize(type)
        arguments += new DefaultExcelTypeHandlerRegistry()

        when:
        def analyzer = new ExcelWriteAnalyzer(type)
        def analyses = analyzer.analyze(fields, arguments as Object[])

        then:
        analyses.size() == fields.size()
        analyses*.defaultValue == defaultValues
        (0..<analyses.size()).each {
            def analysis = analyses[it]
            def field = fields[it]

            assert analysis.field == field
            assert analysis.getValue(model) == model[field.name]
        }

        where:
        type        | arguments               || defaultValues
        PlainSample | []                      || [null, null, "0.00", null]
        PlainSample | [new DefaultValue("-")] || ["-", "-", "-", "-"]
        ModelSample | []                      || ["(empty)", "none", "(empty)", "[]"]
        ModelSample | [new DefaultValue("-")] || ["-", "-", "-", "-"]
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample {
        Set<String> values

        Set<String> getValues() {
            values.collect { "#" + it }
        }
    }

}
