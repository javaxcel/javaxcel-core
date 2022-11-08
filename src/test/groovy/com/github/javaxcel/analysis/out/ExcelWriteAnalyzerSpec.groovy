package com.github.javaxcel.analysis.out

import com.github.javaxcel.converter.handler.impl.lang.DoubleTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.IntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.LongTypeHandler
import com.github.javaxcel.converter.handler.impl.lang.StringTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigDecimalTypeHandler
import com.github.javaxcel.converter.handler.impl.math.BigIntegerTypeHandler
import com.github.javaxcel.converter.handler.impl.util.UUIDTypeHandler
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.model.sample.ComplexSample
import com.github.javaxcel.model.sample.GenericSample
import com.github.javaxcel.model.sample.ModelSample
import com.github.javaxcel.model.sample.PlainSample
import com.github.javaxcel.out.strategy.impl.DefaultValue
import com.github.javaxcel.out.strategy.impl.UseGetters
import com.github.javaxcel.util.FieldUtils
import io.github.imsejin.common.util.NumberUtils
import spock.lang.Specification

import static com.github.javaxcel.analysis.out.ExcelWriteAnalyzer.EXPRESSION
import static com.github.javaxcel.analysis.out.ExcelWriteAnalyzer.FIELD_ACCESS
import static com.github.javaxcel.analysis.out.ExcelWriteAnalyzer.GETTER
import static com.github.javaxcel.analysis.out.ExcelWriteAnalyzer.HANDLER

class ExcelWriteAnalyzerSpec extends Specification {

    def "Constraints for flag"() {
        given:
        def flags = [HANDLER, EXPRESSION, FIELD_ACCESS, GETTER].sort()

        expect: """
            1. All the flags are type of integer, not decimal.
            2. Each flag must be unique.
            3. 
        """
        flags.grep(Integer).size() == flags.size()
        flags.unique() == flags
        flags.size().each {
            println it
        }

//        flags.sort().inject(flags.first(), { acc, cur ->
//            assert
//            cur * 2
//        })

//        flags.findAll { it > 2 }
//                .collect { Math.sqrt(it) }
//                .each { assert !NumberUtils.hasDecimalPart(it) }
//                .each { assert !NumberUtils.hasDecimalPart(Math.sqrt(it)) }
    }

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
        analyses*.defaultMeta*.value == defaultValues
        analyses.size().times {
            def analysis = analyses[it]
            def field = fields[it]

            assert analysis.field == field
        }

        where:
        type        | arguments               || defaultValues
        PlainSample | [new UseGetters()]      || [null, "0.00", null]
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
        ComplexSample | [LongTypeHandler, null, null, null, null, null, null, UUIDTypeHandler, UUIDTypeHandler,
                         UUIDTypeHandler, DoubleTypeHandler, DoubleTypeHandler, DoubleTypeHandler, null, null,
                         LongTypeHandler, LongTypeHandler, null, null, LongTypeHandler, LongTypeHandler, null, null,
                         null, null, null, null, null, null, UUIDTypeHandler, UUIDTypeHandler, UUIDTypeHandler,
                         UUIDTypeHandler, UUIDTypeHandler, UUIDTypeHandler, DoubleTypeHandler, null]
    }

}
