package com.github.javaxcel.analysis.out

import com.github.javaxcel.converter.handler.impl.lang.DoubleTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.IntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.LongTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.StringTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigDecimalTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigIntegerTypeHandler
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.model.sample.ComplexSample
import com.github.javaxcel.model.sample.GenericSample
import com.github.javaxcel.model.sample.ModelSample
import com.github.javaxcel.model.sample.PlainSample
import com.github.javaxcel.out.strategy.impl.DefaultValue
import com.github.javaxcel.util.FieldUtils
import spock.lang.Specification

class ExcelWriteAnalyzerSpec extends Specification {

    @SuppressWarnings("GroovyAssignabilityCheck")
    def "Analyzes fields of randomized model"() {
        given:
        def fields = FieldUtils.getTargetedFields(type)

        when:
        def analyzer = new ExcelWriteAnalyzer(new DefaultExcelTypeHandlerRegistry())
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
        def analyzer = new ExcelWriteAnalyzer(new DefaultExcelTypeHandlerRegistry())
        def analyses = analyzer.analyze(fields)

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

}
