package com.github.javaxcel.analysis.out

import com.github.javaxcel.TestUtils
import com.github.javaxcel.converter.handler.impl.BigDecimalTypeHandler
import com.github.javaxcel.converter.handler.impl.BigIntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.DoubleTypeHandler
import com.github.javaxcel.converter.handler.impl.IntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.LongTypeHandler
import com.github.javaxcel.converter.handler.impl.StringTypeHandler
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.model.sample.ComplexSample
import com.github.javaxcel.model.sample.GenericSample
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
        def analyzer = new ExcelWriteAnalyzer()
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
    def "Analyzes fields of randomized model"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)
        def model = TestUtils.randomize(type)
        arguments += new DefaultExcelTypeHandlerRegistry()

        when:
        def analyzer = new ExcelWriteAnalyzer()
        def analyses = analyzer.analyze(fields, arguments as Object[])

        then: """
            1. Result count of analyses is equal to count of the given fields.
            2. Default value of each analysis is equal to the expected.
        """
        analyses.size() == fields.size()
        analyses*.defaultValue == defaultValues
        analyses.size().times {
            def analysis = analyses[it]
            def field = fields[it]

            assert analysis.field == field
            assert analysis.getValue(model) == model[field.name]
        }

        where:
        type        | arguments               || defaultValues
        PlainSample | []                      || [null, "0.00", null]
        PlainSample | [new DefaultValue("-")] || ["-", "-", "-"]
        ModelSample | []                      || ["(empty)", "none", "(empty)", "[]"]
        ModelSample | [new DefaultValue("-")] || ["-", "-", "-", "-"]
    }

    def "Analyzes fields of non-randomized model"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)

        when:
        def analyzer = new ExcelWriteAnalyzer()
        def analyses = analyzer.analyze(fields, new DefaultExcelTypeHandlerRegistry())

        then: "Field, value and handler of analysis is equal to the expected"
        analyses.size() == fields.size()
        analyses.size().times {
            def analysis = analyses[it]
            def handleType = handlerTypes[it]

            assert analysis.handler?.class == handleType
        }

        where:
        type          | handlerTypes
        PlainSample   | [LongTypeHandler, BigDecimalTypeHandler, StringTypeHandler]
        ModelSample   | [IntegerTypeHandler, StringTypeHandler, BigIntegerTypeHandler, StringTypeHandler]
        GenericSample | [null, StringTypeHandler, BigDecimalTypeHandler, StringTypeHandler, StringTypeHandler, StringTypeHandler]
        ComplexSample | [LongTypeHandler, null, null, null, null, null, null, null, null, null, DoubleTypeHandler, DoubleTypeHandler,
                         DoubleTypeHandler, null, null, LongTypeHandler, LongTypeHandler, null, null, LongTypeHandler, LongTypeHandler,
                         null, null, null, null, null, null, null, null, null, null, null, null, null, null, DoubleTypeHandler, null]
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample {
        Set<String> values

        Set<String> getValues() {
            values.collect { "#" + it }
        }
    }

}
