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

package com.github.javaxcel.in.resolver.impl.constructor.failure

import com.github.javaxcel.annotation.ExcelModelCreator.FieldName
import com.github.javaxcel.exception.InvalidExcelModelCreatorException
import com.github.javaxcel.in.resolver.impl.ExcelModelConstructorResolver
import spock.lang.Specification

class DuplicatedFieldNameSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(InvalidExcelModelCreatorException)
        e.message ==~ /^Each ResolvedParameter\.name must be unique, but it isn't: \(duplicated: '.+', names: \[.+]\)[\s\S]*$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final char[] chars
        final char[] characters

        private TestModel(@FieldName("chars") char[] c1, @FieldName("chars") char[] c2) {
            this.chars = c1
            this.characters = c2
        }
    }

}
