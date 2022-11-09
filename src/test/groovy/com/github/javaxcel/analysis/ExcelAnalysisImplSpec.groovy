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

package com.github.javaxcel.analysis

import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl
import com.github.javaxcel.internal.ObjectTypeHandler
import com.github.javaxcel.internal.TimeUnitTypeHandler
import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

class ExcelAnalysisImplSpec extends Specification {

    def "Gets the field"() {
        given:
        def field = Sample.getDeclaredField("id")

        when:
        def analysis = new ExcelAnalysisImpl(field)

        then:
        analysis.field == field
    }

    def "Adds the flags and then gets them"() {
        given:
        def field = Sample.getDeclaredField("name")
        def numbers = [0x01, 0x04, 0x08, 0x20, 0x40]

        when:
        def analysis = new ExcelAnalysisImpl(field)
        def flags = numbers.inject(0) { acc, cur -> acc | cur }
        analysis.addFlags(flags)

        then: """
            1. ExcelAnalysis.flags is the same as the added flags.
            2. ExcelAnalysis has each flag and combination of the flags.
            3. ExcelAnalysis doesn't have a flag which is not added.
        """
        analysis.flags == flags
        numbers.inject(numbers.first()) { acc, cur ->
            assert analysis.hasFlag(cur) // flag
            assert analysis.hasFlag(acc) // combination of the flags
            acc | cur
        }
        !analysis.hasFlag(0x02)
        !analysis.hasFlag(0x10)

        when: "Adds flags including missed flags"
        (numbers.first()..numbers.last()).each { analysis.addFlags(it) }

        then: """
            1. ExcelAnalysis has the missed flags.
            2. ExcelAnalysis has all the flags from the least flag to the greatest flag.
            3. ExcelAnalysis.flags is the same as the next bit minus 1.
        """
        analysis.hasFlag(0x02 | 0x10)
        (numbers.first()..numbers.last()).each { assert analysis.hasFlag(it) }
        analysis.flags != flags
        analysis.flags == numbers.last() * 2 - 1
    }

    def "Sets a default meta information"() {
        given:
        def field = Sample.getDeclaredField("id")

        when:
        def analysis = new ExcelAnalysisImpl(field)

        then:
        analysis.defaultMeta == null

        when:
        analysis.defaultMeta = new DefaultMetaImpl("<null>", Source.COLUMN)

        then:
        analysis.defaultMeta != null
    }

    def "Sets a handler"() {
        given:
        def field = Sample.getDeclaredField("name")

        when:
        def analysis = new ExcelAnalysisImpl(field)

        then:
        analysis.handler == null

        when:
        analysis.handler = null

        then:
        def e = thrown(NullPointerException)
        e.message == "${analysis.class.simpleName}.handler cannot be null"
    }

    def "Does the handler resolved?"() {
        given:
        def field = Sample.getDeclaredField("id")

        when:
        def analysis = new ExcelAnalysisImpl(field)

        then:
        !analysis.doesHandlerResolved()

        when:
        analysis.handler = new ObjectTypeHandler()

        then:
        analysis.handler.type == Object
        !analysis.doesHandlerResolved()

        when:
        analysis.handler = new TimeUnitTypeHandler()

        then:
        analysis.handler.type != Object
        analysis.doesHandlerResolved()
    }

    // -------------------------------------------------------------------------------------------------

    @EqualsAndHashCode
    private static class Sample {
        Long id
        String name
    }

}
