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

import java.nio.file.AccessMode
import java.util.concurrent.TimeUnit

class UnmatchedParamWithFieldSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(type)

        when:
        resolver.resolve()

        then:
        def e = thrown(InvalidExcelModelCreatorException)
        e.message ==~ /^Not found field\[.+ .+] to map parameter\[.+ .+] with; Check if the parameter of the .+\[.+] matches its type and name with that fields$/

        where:
        type << [InvalidParamName, InvalidFieldName]
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class InvalidParamName {
        final AccessMode accessMode
        TimeUnit minute
        TimeUnit second

        private InvalidParamName(TimeUnit accessMode) {
            this.accessMode = AccessMode.WRITE
        }
    }

    @SuppressWarnings("unused")
    private static class InvalidFieldName {
        final AccessMode read
        final AccessMode write
        final TimeUnit timeUnit

        private InvalidFieldName(AccessMode read, @FieldName("timeUnit") AccessMode write) {
            this.read = read
            this.write = write
            this.timeUnit = TimeUnit.HOURS
        }
    }

}
