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

package com.github.javaxcel.util.resolver

import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException
import com.github.javaxcel.exception.InvalidExcelModelCreatorException
import com.github.javaxcel.internal.model.AbstractExcelModelExecutableResolutionTester.AnnotatedConstructorAndMethod
import com.github.javaxcel.internal.model.AbstractExcelModelExecutableResolutionTester.ConstructorsAndAnnotatedMethod
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.ConstructorsAreAnnotated
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.EmptyFieldName
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.PrivateConstructor
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.LackOfMethodArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.MethodArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.MethodsAreAnnotated
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.ProtectedMethod
import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.Method

class AbstractExcelModelExecutableResolverSpec extends Specification {

    def "succeed"() {
        when:
        def executable = AbstractExcelModelExecutableResolver.resolve modelType

        then:
        executable != null
        expected.isInstance executable

        where:
        modelType                      | expected
        MethodArgsWithoutOrder         | Method
        LackOfMethodArgsWithoutOrder   | Method
        ConstructorsAndAnnotatedMethod | Method
        ProtectedMethod                | Constructor
        PrivateConstructor             | Constructor
    }

    def "fail"() {
        when:
        AbstractExcelModelExecutableResolver.resolve modelType

        then:
        def e = thrown exceptionType
        e.message.matches message
        where:
        modelType                     || exceptionType                       | message
        EmptyFieldName                || InvalidExcelModelCreatorException   | "ResolvedParameter.name must have text, but it isn't: '.*'"
        MethodsAreAnnotated           || AmbiguousExcelModelCreatorException | "Ambiguous methods\\[.+] to resolve; Remove @ExcelModelCreator from other methods except the one"
        ConstructorsAreAnnotated      || AmbiguousExcelModelCreatorException | "Ambiguous constructors\\[.+] to resolve; Remove @ExcelModelCreator from other constructors except the one"
        AnnotatedConstructorAndMethod || AmbiguousExcelModelCreatorException | "Ambiguous method\\[.+] and constructor\\[.+] to resolve; Remove one of the annotations\\[@ExcelModelCreator] from the method and constructor"
    }

}
