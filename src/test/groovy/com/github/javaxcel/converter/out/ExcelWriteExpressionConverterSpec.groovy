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

package com.github.javaxcel.converter.out

import com.github.javaxcel.analysis.ExcelAnalysis
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.analysis.ExcelAnalysisImpl
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer
import com.github.javaxcel.annotation.ExcelColumn
import com.github.javaxcel.annotation.ExcelModel
import com.github.javaxcel.annotation.ExcelWriteExpression
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ExcelWriteExpressionConverterSpec extends Specification {

    def "Converts field value through expression"() {
        given:
        def model = new Sample(fieldName, value)
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.GETTER)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteExpressionConverter(analyses)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName    | value                      || expected
        "_int"       | -51                        || "-102"
        "_long"      | 128                        || "0"
        "_double"    | 3.141592                   || "3"
        "object"     | null                       || "object is null"
        "string"     | "able"                     || "nullable"
        "bigInt"     | 16                         || "18446744073709551616"
        "bigDecimal" | 2.7182818284590            || "12"
        "timeUnit"   | TimeUnit.HOURS             || "86400"
        "strings"    | ['alpha', 'BETA', 'gamma'] || "ALPHA-GAMMA"
        "locale"     | Locale.US                  || "en/US"
        "date"       | LocalDate.of(2022, 1, 5)   || "2022-01-05"
        "time"       | LocalTime.of(12, 34, 56)   || "12:35:01"
    }

    def "Returns default value"() {
        given:
        def analyses = analyze(model.class.declaredFields, ExcelWriteAnalyzer.FIELD_ACCESS)
        def field = model.class.getDeclaredField(fieldName)

        when:
        def converter = new ExcelWriteExpressionConverter(analyses)
        def actual = converter.convert(model, field)

        then:
        actual == expected

        where:
        fieldName | model                                || expected
        "id"      | new DefaultValueModel(id: 12)        || '$24'
        "id"      | new DefaultValueModel(id: null)      || "$Long.MIN_VALUE"
        "name"    | new DefaultValueModel(name: "alpha") || "{alpha}"
        "name"    | new DefaultValueModel(name: null)    || null // @ExcelModel.defaultValue is ignored on ExcelWriteExpressionConverter
    }

    // -------------------------------------------------------------------------------------------------

    private static Iterable<ExcelAnalysis> analyze(Field[] fields, int flags) {
        fields.findAll { !it.isSynthetic() }.collect {
            def analysis = new ExcelAnalysisImpl(it)

            def defaultMeta = new DefaultMetaImpl(null, Source.NONE)
            if (it.isAnnotationPresent(ExcelColumn)) {
                defaultMeta = new DefaultMetaImpl(it.getAnnotation(ExcelColumn).defaultValue(), Source.COLUMN)
            } else if (it.declaringClass.isAnnotationPresent(ExcelModel)) {
                defaultMeta = new DefaultMetaImpl(it.declaringClass.getAnnotation(ExcelModel).defaultValue(), Source.MODEL)
            }
            analysis.defaultMeta = defaultMeta
            analysis.addFlags(ExcelWriteAnalyzer.EXPRESSION | flags)

            analysis
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class Sample {
        @ExcelWriteExpression("#_int + #_int")
        int _int
        @ExcelWriteExpression("#_long * #_int")
        long _long
        @ExcelWriteExpression("T(Math).round(#_double)")
        double _double
        @ExcelWriteExpression("#object?.toString() ?: 'object is null'")
        Object object
        @ExcelWriteExpression("#bigInt + #string")
        String string
        @ExcelWriteExpression("#bigInt.pow(#bigInt.intValue())")
        BigInteger bigInt
        @ExcelWriteExpression("#bigDecimal.stripTrailingZeros().scale()")
        BigDecimal bigDecimal
        @ExcelWriteExpression("#timeUnit.toSeconds(24)")
        TimeUnit timeUnit
        @ExcelWriteExpression("""
            #strings.?[T(Character).isLowerCase(#this.charAt(0))]
                    .![#this.toUpperCase()]
                    .stream().collect(T(java.util.stream.Collectors).joining('-', '', ''))
        """)
        List<String> strings
        @ExcelWriteExpression("#locale.language + '/' + #locale.country")
        Locale locale
        @ExcelWriteExpression("#date")
        LocalDate date
        @ExcelWriteExpression("#time.plusSeconds(5)")
        LocalTime time

        Sample(String key, Object value) {
            this[key] = value
        }
    }

    @ExcelModel(defaultValue = "<null>")
    @EqualsAndHashCode
    private static class DefaultValueModel {
        @ExcelColumn(defaultValue = "T(Long).MIN_VALUE")
        @ExcelWriteExpression('#id == null ? null : "$" + (#id * 2)')
        Long id
        @ExcelWriteExpression("#name == null or #name.isEmpty() ? null : '{' + #name + '}'")
        String name
    }

}
