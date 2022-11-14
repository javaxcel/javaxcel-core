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

package com.github.javaxcel.in.resolver.impl.method.failure

import com.github.javaxcel.annotation.ExcelModelCreator
import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException
import com.github.javaxcel.in.resolver.impl.ExcelModelMethodResolver
import spock.lang.Specification

import java.nio.file.AccessMode

class TooManyAnnotatedMethodsSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(AmbiguousExcelModelCreatorException)
        e.message ==~ /^Ambiguous methods\[.+] to resolve; Remove @ExcelModelCreator from other methods except the one$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final AccessMode accessMode

        private TestModel(AccessMode accessMode) {
            this.accessMode = accessMode
        }

        @ExcelModelCreator
        static TestModel withRead() {
            with(AccessMode.READ)
        }

        @ExcelModelCreator
        static TestModel withWrite() {
            with(AccessMode.WRITE)
        }

        @ExcelModelCreator
        static TestModel withExecute() {
            with(AccessMode.EXECUTE)
        }

        static TestModel with(AccessMode accessMode) {
            new TestModel(accessMode)
        }
    }

}
