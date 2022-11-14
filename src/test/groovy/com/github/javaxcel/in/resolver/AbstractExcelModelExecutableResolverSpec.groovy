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

import com.github.javaxcel.annotation.ExcelModelCreator
import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException
import io.github.imsejin.common.tool.RandomString
import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.Method

class AbstractExcelModelExecutableResolverSpec extends Specification {

    def "succeed"() {
        when:
        def executable = AbstractExcelModelExecutableResolver.resolve(modelType)

        then:
        executable != null
        expected.isInstance(executable)

        where:
        modelType                    | expected
        OneMethodAndOneConstructor   | Method
        OneMethodAndManyConstructors | Method
        NoAnnotatedMethod            | Constructor
    }

    def "fail"() {
        when:
        AbstractExcelModelExecutableResolver.resolve(modelType)

        then:
        def e = thrown exceptionType
        e.message ==~ message

        where:
        modelType                      || exceptionType                       | message
        TooManyAnnotatedMethods        || AmbiguousExcelModelCreatorException | ~/^Ambiguous methods\[.+] to resolve; Remove @ExcelModelCreator from other methods except the one[\s\S]*$/
        TooManyAnnotatedConstructors   || AmbiguousExcelModelCreatorException | ~/^Ambiguous constructors\[.+] to resolve; Remove @ExcelModelCreator from other constructors except the one[\s\S]*$/
        ConflictedMethodAndConstructor || AmbiguousExcelModelCreatorException | ~/^Ambiguous method\[.+] and constructor\[.+] to resolve; Remove one of the annotations\[@ExcelModelCreator] from the method and constructor[\s\S]*$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class OneMethodAndOneConstructor {
        String title

        @ExcelModelCreator
        static OneMethodAndOneConstructor from(String name) {
            new OneMethodAndOneConstructor(title: name)
        }
    }

    @SuppressWarnings("unused")
    private static class OneMethodAndManyConstructors {
        String title

        private OneMethodAndManyConstructors() {
            this.title = new RandomString().nextString(16)
        }

        private OneMethodAndManyConstructors(String title) {
            this.title = title
        }

        @ExcelModelCreator
        static OneMethodAndManyConstructors from(String name) {
            new OneMethodAndManyConstructors(title: name)
        }
    }

    @SuppressWarnings("unused")
    private static class NoAnnotatedMethod {
        Long id

        static NoAnnotatedMethod from(Long number) {
            new NoAnnotatedMethod(id: number)
        }
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TooManyAnnotatedMethods {
        Long id
        String title

        @ExcelModelCreator
        static TooManyAnnotatedMethods createRandomId(String name) {
            def id = new Random().nextLong()
            of(id, name)
        }

        @ExcelModelCreator
        static TooManyAnnotatedMethods of(Long id, String name) {
            new TooManyAnnotatedMethods(id: id, title: name)
        }
    }

    @SuppressWarnings("unused")
    private static class TooManyAnnotatedConstructors {
        Long id
        String title

        @ExcelModelCreator
        TooManyAnnotatedConstructors(String name) {
            this(new Random().nextLong(), name)
        }

        @ExcelModelCreator
        TooManyAnnotatedConstructors(Long id, String name) {
            this.id = id
            this.title = name
        }
    }

    @SuppressWarnings("unused")
    private static class ConflictedMethodAndConstructor {
        Long id
        String title

        @ExcelModelCreator
        ConflictedMethodAndConstructor(Long id, String name) {
            this.id = id
            this.title = name
        }

        @ExcelModelCreator
        static ConflictedMethodAndConstructor createRandomId(String name) {
            def id = new Random().nextLong()
            new ConflictedMethodAndConstructor(id, name)
        }
    }

}
