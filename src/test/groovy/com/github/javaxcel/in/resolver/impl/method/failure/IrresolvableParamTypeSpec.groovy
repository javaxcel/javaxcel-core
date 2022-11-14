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
import com.github.javaxcel.exception.InvalidExcelModelCreatorException
import com.github.javaxcel.in.resolver.impl.ExcelModelMethodResolver
import spock.lang.Specification

class IrresolvableParamTypeSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(TestModel)

        when:
        resolver.resolve()

        then:
        def e = thrown(InvalidExcelModelCreatorException)
        e.message ==~ /^Unable to resolve parameter type\[.+] of the method\[.+]; method has parameter type that is not contained in types of the targeted fields\[.+]$/
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class TestModel {
        final String name

        TestModel(String name) {
            this.name = name
        }

        @ExcelModelCreator
        static TestModel from(CharSequence name) {
            new TestModel(name.toString())
        }
    }

}
