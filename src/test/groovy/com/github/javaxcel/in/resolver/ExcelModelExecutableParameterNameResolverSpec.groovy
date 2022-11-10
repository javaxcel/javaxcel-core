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

package com.github.javaxcel.in.resolver

import com.github.javaxcel.annotation.ExcelModelCreator.FieldName
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExcelModelExecutableParameterNameResolverSpec extends Specification {

    def "Resolves parameters of the executable"() {
        given:
        def resolver = new ExcelModelExecutableParameterNameResolver(executable)

        when:
        def parameters = resolver.resolve()

        then:
        parameters != null
        parameters*.name == names
        parameters*.type == types
        parameters*.index == indexes
        parameters*.annotated == annotateds
        parameters*.declaringExecutable == [executable] * parameters.size()

        if (!parameters.isEmpty()) {
            (0..<parameters.size()).each {
                def paramName = names[it]
                def paramTypeName = types[it].name
                def pattern = annotateds[it]
                        ? ~/^@FieldName\('$paramName'\) $paramTypeName [A-Za-z]+$/
                        : ~/^$paramTypeName $paramName$/
                assert parameters[it].toString() ==~ pattern
            }
        }

        where:
        executable                                               || names              | types                 | indexes | annotateds
        TestModel.getDeclaredConstructor(Long)                   || ["id"]             | [Long]                | [0]     | [false]
        TestModel.getDeclaredConstructor(Long, LocalDateTime)    || ["ID", "dateTime"] | [Long, LocalDateTime] | [0, 1]  | [true, false]
        TestModel.getDeclaredMethod("of", Instant)               || ["instant"]        | [Instant]             | [0]     | [false]
        TestModel.getDeclaredMethod("getName")                   || []                 | []                    | []      | []
        TestModel.getDeclaredMethod("format", DateTimeFormatter) || ["format"]         | [DateTimeFormatter]   | [0]     | [true]
    }

    // -------------------------------------------------------------------------------------------------

    private static class TestModel {
        final Long id
        final LocalDateTime dateTime

        TestModel(Long id) {
            this(id, LocalDateTime.now())
        }

        TestModel(@FieldName("ID") Long id, LocalDateTime dateTime) {
            this.id = id
            this.dateTime = dateTime
        }

        static TestModel of(Instant instant) {
            def id = instant.toEpochMilli()
            def dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            new TestModel(id, dateTime)
        }

        String getName() {
            "${this.class.name}($id)"
        }

        String format(@FieldName("format") DateTimeFormatter formatter) {
            this.dateTime.format(formatter)
        }
    }

}
