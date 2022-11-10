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

package com.github.javaxcel.in.processor

import com.github.javaxcel.analysis.in.ExcelReadAnalyzer
import com.github.javaxcel.annotation.ExcelModelCreator
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry
import com.github.javaxcel.in.strategy.impl.UseSetters
import com.github.javaxcel.util.FieldUtils
import com.github.javaxcel.in.resolver.AbstractExcelModelExecutableResolver
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ExcelModelCreationProcessorSpec extends Specification {

    def "Creates a excel model"() {
        given:
        def fields = FieldUtils.getTargetedFields(modelType)
        def executable = AbstractExcelModelExecutableResolver.resolve(modelType)
        def processor = new ExcelModelCreationProcessor<>(modelType as Class, fields, executable)

        when:
        def actual = processor.createModel(mock)

        then:
        modelType.isInstance(actual)
        actual == expected

        where:
        modelType | mock                                                   || expected
        Alpha     | [number: 256, name: "alpha", decimal: 2.173]           || new Alpha(256, "alpha", 2.173)
        Beta      | [id: 1024L, timeUnit: TimeUnit.DAYS, tags: ["A", "B"]] || new Beta(1024L, TimeUnit.DAYS, ["A", "B"])
        Gamma     | [uuid: new UUID(0x512, 64), locale: Locale.US]         || new Gamma(new UUID(0x512, 64), Locale.US)
        Delta     | [name: "delta", title: "DELTA", point: 3.14D]          || new Delta("delta", "DELTA", 3.14D)
    }

    def "Creates a excel model with setter"() {
        given:
        def fields = FieldUtils.getTargetedFields(modelType)
        def executable = AbstractExcelModelExecutableResolver.resolve(modelType)
        def analyses = new ExcelReadAnalyzer(new DefaultExcelTypeHandlerRegistry()).analyze(fields, new UseSetters())

        when:
        def processor = new ExcelModelCreationProcessor<>(modelType as Class, fields, executable)
        processor.analyses = analyses
        def actual = processor.createModel(mock)

        then:
        modelType.isInstance(actual)
        actual == expected

        where:
        modelType | mock                                                   || expected
        Alpha     | [number: 256, name: "alpha", decimal: 2.173]           || new Alpha(256, "alpha", 2.173)
        Beta      | [id: 1024L, timeUnit: TimeUnit.DAYS, tags: ["A", "B"]] || new Beta(-1024L, TimeUnit.DAYS, ["0", "1"])
        Gamma     | [uuid: new UUID(0x512, 64), locale: Locale.US]         || new Gamma(new UUID(0, 0), Locale.ROOT)
        Delta     | [name: "delta", title: "DELTA", point: 3.14D]          || new Delta("delta", "DELTA", 3.14D)
    }

    // -------------------------------------------------------------------------------------------------

    @EqualsAndHashCode
    private static class Alpha {
        Integer number
        String name
        BigDecimal decimal

        Alpha(Integer number, String name, BigDecimal decimal) {
            this.number = number
            this.name = name
            this.decimal = decimal
        }

        void setNumber(Integer number) {
            this.number = number * 2
        }

        void setName(String name) {
            this.name = name.toUpperCase(Locale.US)
        }

        void setDecimal(BigDecimal decimal) {
            this.decimal = decimal.toBigInteger().toBigDecimal()
        }
    }

    @EqualsAndHashCode
    private static class Beta {
        Long id
        TimeUnit timeUnit
        List<String> tags

        Beta(@FieldName("l") Long id, TimeUnit timeUnit, @FieldName("t") List<String> tags) {
            this.id = id
            this.timeUnit = timeUnit
            this.tags = tags
        }

        void setId(Long id) {
            this.id = -id
        }

        void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = TimeUnit.HOURS
        }

        void setTags(List<String> tags) {
            def numbers = (0..<tags.size()).collect { "$it" }
            this.tags = numbers
        }
    }

    @EqualsAndHashCode
    private static class Gamma {
        UUID uuid
        Locale locale

        Gamma(@FieldName("u") UUID uuid, @FieldName("l") Locale locale) {
            this.uuid = uuid
            this.locale = locale
        }

        void setUuid(UUID uuid) {
            this.uuid = new UUID(0, 0)
        }

        void setLocale(Locale locale) {
            this.locale = Locale.ROOT
        }
    }

    @EqualsAndHashCode
    private static class Delta {
        String name
        String title
        Double point

        @ExcelModelCreator
        Delta(Double point, String name) {
            this.point = point
            this.name = name
        }

        Delta(String name, String title, Double point) {
            this.name = name
            this.title = title
            this.point = point
        }
    }

}
