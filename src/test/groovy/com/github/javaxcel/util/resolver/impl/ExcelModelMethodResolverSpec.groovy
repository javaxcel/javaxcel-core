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

package com.github.javaxcel.util.resolver.impl

import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException
import com.github.javaxcel.exception.InvalidExcelModelCreatorException
import com.github.javaxcel.exception.NoResolvedExcelModelCreatorException
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.AllMethodsAreNotAnnotated
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.DuplicatedFieldName
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.EmptyFieldName
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.InstanceMethod
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.InvalidReturnType
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.LackOfMethodArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.MethodArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.MethodsAreAnnotated
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldName
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldNameButFieldIsExplicit
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldNameButOtherFieldIsIgnored
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldType
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldTypeAndName
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.NoMatchFieldTypeAndNameWithAnnotation
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.PackagePrivateMethod
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.ParamNameDoesNotMatchFieldNameButBothTypeIsUnique
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.PrivateMethod
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.ProtectedMethod
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.PublicMethod
import com.github.javaxcel.internal.model.ExcelModelMethodResolutionTester.PublicNoArgsMethod
import spock.lang.Specification

import java.lang.reflect.Method

class ExcelModelMethodResolverSpec extends Specification {

    def "Gets the resolved method of type"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(modelType)

        when:
        def method = resolver.resolve()

        then:
        noExceptionThrown()
        method instanceof Method

        where:
        modelType << [
                PublicNoArgsMethod, PublicMethod, MethodArgsWithoutOrder, LackOfMethodArgsWithoutOrder,
                ParamNameDoesNotMatchFieldNameButBothTypeIsUnique, NoMatchFieldNameButOtherFieldIsIgnored,
                NoMatchFieldNameButFieldIsExplicit,
        ]
    }

    def "Failed to resolve the method of type"() {
        given:
        def resolver = new ExcelModelMethodResolver<>(modelType)

        when:
        resolver.resolve()

        then:
        def e = thrown excecptionType
        e.message.split("\n")[0].matches message

        where:
        modelType                             || excecptionType                       | message
        ProtectedMethod                       || NoResolvedExcelModelCreatorException | "Not found method of type\\[.+] to resolve; Annotate static method you want with @ExcelModelCreator"
        PackagePrivateMethod                  || NoResolvedExcelModelCreatorException | "Not found method of type\\[.+] to resolve; Annotate static method you want with @ExcelModelCreator"
        PrivateMethod                         || NoResolvedExcelModelCreatorException | "Not found method of type\\[.+] to resolve; Annotate static method you want with @ExcelModelCreator"
        AllMethodsAreNotAnnotated             || NoResolvedExcelModelCreatorException | "Not found method of type\\[.+] to resolve; Annotate static method you want with @ExcelModelCreator"
        MethodsAreAnnotated                   || AmbiguousExcelModelCreatorException  | "Ambiguous methods\\[.+] to resolve; Remove @ExcelModelCreator from other methods except the one"
        InstanceMethod                        || InvalidExcelModelCreatorException    | "@ExcelModelCreator is not allowed to be annotated on instance method; Remove the annotation from the method\\[.+]"
        InvalidReturnType                     || InvalidExcelModelCreatorException    | "@ExcelModelCreator is not allowed to be annotated on method whose return type is assignable to model type\\[.+]; Remove the annotation from the method\\[.+]"
        NoMatchFieldType                      || InvalidExcelModelCreatorException    | "Unable to resolve parameter type\\[.+] of the .+\\[.+]; .+ has parameter type that is not contained in types of the targeted fields\\[.+]"
        EmptyFieldName                        || InvalidExcelModelCreatorException    | "ResolvedParameter.name must have text, but it isn't: '.*'"
        NoMatchFieldName                      || InvalidExcelModelCreatorException    | "ResolvedParameter.name must match name of the targeted fields, but it isn't: \\(actual: '.+', allowed: \\[.+]\\)"
        DuplicatedFieldName                   || InvalidExcelModelCreatorException    | "Each ResolvedParameter.name must be unique, but it isn't: \\(duplicated: '.+', names: \\[.+]\\)"
        NoMatchFieldTypeAndName               || InvalidExcelModelCreatorException    | "Not found field\\[.+ .+] to map parameter\\[.+ .+] with; Check if the parameter of the .+\\[.+] matches its type and name with that fields"
        NoMatchFieldTypeAndNameWithAnnotation || InvalidExcelModelCreatorException    | "Not found field\\[.+ .+] to map parameter\\[@FieldName\\('.+'\\) .+ .+] with; Check if the parameter of the .+\\[.+] matches its type and name with that fields"
    }

}
