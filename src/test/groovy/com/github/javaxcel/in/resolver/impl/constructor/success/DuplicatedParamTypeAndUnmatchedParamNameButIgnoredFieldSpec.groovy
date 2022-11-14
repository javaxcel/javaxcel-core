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

package com.github.javaxcel.in.resolver.impl.constructor.success

import com.github.javaxcel.annotation.ExcelColumn
import com.github.javaxcel.annotation.ExcelIgnore
import com.github.javaxcel.annotation.ExcelModel
import com.github.javaxcel.in.resolver.impl.ExcelModelConstructorResolver
import spock.lang.Specification

import java.lang.reflect.Constructor

class DuplicatedParamTypeAndUnmatchedParamNameButIgnoredFieldSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(type)

        when:
        def constructor = resolver.resolve()

        then:
        noExceptionThrown()
        constructor != null
        constructor instanceof Constructor

        where:
        type << [IgnoredField, ExplicitField]
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class IgnoredField {
        final List<Integer> integers
        @ExcelIgnore
        final List<String> strings

        IgnoredField(List<Integer> numbers) {
            this.integers = numbers
            this.strings = Collections.emptyList()
        }
    }

    @SuppressWarnings("unused")
    @ExcelModel(explicit = true)
    private static class ExplicitField {
        @ExcelColumn
        final List<Integer> integers
        final List<String> strings

        ExplicitField(List<Integer> numbers) {
            this.integers = numbers
            this.strings = Collections.emptyList()
        }
    }

}
