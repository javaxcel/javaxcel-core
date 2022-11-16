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
import groovy.transform.PackageScope
import spock.lang.Specification

class NonPublicModifierSpec extends Specification {

    def "Resolves a method"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(type)

        when:
        resolver.resolve()

        then:
        def e = thrown(InvalidExcelModelCreatorException)
        e.message ==~ /^@ExcelModelCreator is not allowed to be annotated on non-public method; Remove the annotation from the method\[.+]$/

        where:
        type << [Protected, PackagePrivate, Private]
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static class Protected {
        @ExcelModelCreator
        protected static Protected newInstance() {
            new Protected()
        }
    }

    @SuppressWarnings("unused")
    private static class PackagePrivate {
        @PackageScope
        @ExcelModelCreator
        static PackagePrivate newInstance() {
            new PackagePrivate()
        }
    }

    @SuppressWarnings("unused")
    private static class Private {
        @ExcelModelCreator
        private static Private newInstance() {
            new Private()
        }
    }

}
