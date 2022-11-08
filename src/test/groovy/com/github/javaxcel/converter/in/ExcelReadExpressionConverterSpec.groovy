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

package com.github.javaxcel.converter.in

import com.github.javaxcel.analysis.ExcelAnalysis
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.analysis.ExcelAnalysisImpl
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.analysis.in.ExcelReadAnalyzer
import com.github.javaxcel.annotation.ExcelColumn
import com.github.javaxcel.annotation.ExcelReadExpression
import spock.lang.Specification

import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ExcelReadExpressionConverterSpec extends Specification {

    def "Converts field value through expression"() {
        given:
        def analyses = analyze(Sample.declaredFields, ExcelReadAnalyzer.FIELD_ACCESS)
        def field = Sample.getDeclaredField(fieldName)

        when:
        def converter = new ExcelReadExpressionConverter(analyses)
        def actual = converter.convert(variables, field)

        then:
        actual == expected

        where:
        variables                           | fieldName || expected
        [i: "-51", l: "128"]                | "i"       || -6528
        [:]                                 | "l"       || Long.MIN_VALUE
        [d: "PI is approximately 3.141592"] | "d"       || 3.141592
        [o: null]                           | "o"       || null
        [s: ""]                             | "s"       || "nullable"
        [bi: "795168742300441387849318"]    | "bi"      || -795168742300441387849318
        [bi: "27182818284590"]              | "bd"      || 2.718281828459
        [tu: "nanoseconds"]                 | "tu"      || TimeUnit.NANOSECONDS
        [ss: "ALPHA,BETA,GAMMA"]            | "ss"      || ['ALPHA', 'BETA', 'GAMMA']
        [loc: "ko/KR"]                      | "loc"     || Locale.KOREA
        [i: "4", date: "2022-01-05"]        | "date"    || LocalDate.of(2022, 1, 1)
        [time: "12:34:56"]                  | "time"    || LocalTime.of(12, 34, 56)
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample {
        @ExcelReadExpression("T(Integer).parseInt(#i) * T(Long).parseLong(#l)")
        int i
        @ExcelReadExpression("T(Long).MIN_VALUE")
        long l
        @ExcelReadExpression("T(Double).parseDouble(#d.replaceAll('[^\\d.]', ''))")
        double d
        @ExcelReadExpression("#o ?: ''")
        Object o
        @ExcelColumn(defaultValue = "'nullable'")
        @ExcelReadExpression("#s")
        String s
        @ExcelReadExpression("new java.math.BigInteger('-' + #bi)")
        BigInteger bi
        @ExcelReadExpression("new java.math.BigDecimal(#bi).divide(T(java.math.BigDecimal).valueOf(T(Math).pow(10, 13)))")
        BigDecimal bd
        @ExcelReadExpression("T(java.util.concurrent.TimeUnit).valueOf(#tu.toUpperCase())")
        TimeUnit tu
        @ExcelReadExpression("#ss.split(',')")
        List<String> ss
        @ExcelReadExpression("new java.util.Locale(#loc.split('/')[0], #loc.split('/')[1])")
        Locale loc
        @ExcelReadExpression("T(java.time.LocalDate).parse(#date).minusDays(T(Integer).parseInt(#i))")
        LocalDate date
        @ExcelReadExpression("T(java.time.LocalTime).parse(#time, T(java.time.format.DateTimeFormatter).ofPattern('HH:mm:ss'))")
        LocalTime time
    }

    // -------------------------------------------------------------------------------------------------

    private static Iterable<ExcelAnalysis> analyze(Field[] fields, int flags) {
        fields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)

            def defaultMeta = new DefaultMetaImpl(null, Source.NONE)
            if (it.isAnnotationPresent(ExcelColumn)) {
                defaultMeta = new DefaultMetaImpl(it.getAnnotation(ExcelColumn).defaultValue(), Source.COLUMN)
            }
            analysis.defaultMeta = defaultMeta
            analysis.addFlags(ExcelReadAnalyzer.EXPRESSION | flags)

            analysis
        }
    }

}
