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

package com.github.javaxcel.util.processor

import com.github.javaxcel.util.FieldUtils
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ExcelModelCreationProcessorSpec extends Specification {

    def "Creates a excel model"() {
        given:
        def fields = FieldUtils.getTargetedFields(modelType)
        def processor = new ExcelModelCreationProcessor<>(modelType as Class, fields)

        when:
        def actual = processor.createModel(mock)

        then:
        modelType.isInstance(actual)
        actual == expected

        where:
        modelType | mock                                                   || expected
        Alpha     | [number: 256, name: "alpha", decimal: 2.173]           || new Alpha(number: 256, name: "alpha", decimal: 2.173)
        Beta      | [id: 1024L, timeUnit: TimeUnit.DAYS, tags: ["A", "B"]] || new Beta(id: 1024L, timeUnit: TimeUnit.DAYS, tags: ["A", "B"])
        Gamma     | [uuid: new UUID(0x512, 64), locale: Locale.US]         || new Gamma([uuid: new UUID(0x512, 64), locale: Locale.US])
    }

    // -------------------------------------------------------------------------------------------------

    @EqualsAndHashCode
    static class Alpha {
        Integer number
        String name
        BigDecimal decimal
    }

    @EqualsAndHashCode
    static class Beta {
        Long id
        TimeUnit timeUnit
        List<String> tags
    }

    @EqualsAndHashCode
    static class Gamma {
        UUID uuid
        Locale locale
    }

}
