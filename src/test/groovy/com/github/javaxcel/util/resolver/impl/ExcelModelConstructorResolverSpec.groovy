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
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.AllConstructorsAreNotAnnotated
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.ConstructorArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.ConstructorsAreAnnotated
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.DuplicatedFieldName
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.EmptyFieldName
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.LackOfConstructorArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldName
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldNameButFieldIsExplicit
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldNameButOtherFieldIsIgnored
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldType
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldTypeAndName
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.NoMatchFieldTypeAndNameWithAnnotation
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.PackagePrivateConstructor
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.ParamNameDoesNotMatchFieldNameButBothTypeIsUnique
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.PrivateConstructor
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.ProtectedConstructor
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.PublicConstructor
import com.github.javaxcel.internal.model.ExcelModelConstructorResolutionTester.PublicNoArgsConstructor
import spock.lang.Specification

import java.lang.reflect.Constructor

class ExcelModelConstructorResolverSpec extends Specification {

    def "Gets the resolved constructor of type"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(modelType)

        when:
        def constructor = resolver.resolve()

        then:
        noExceptionThrown()
        constructor instanceof Constructor

        where:
        modelType << [
                PublicNoArgsConstructor, PublicConstructor, ProtectedConstructor, PackagePrivateConstructor,
                PrivateConstructor, ConstructorArgsWithoutOrder, LackOfConstructorArgsWithoutOrder,
                ParamNameDoesNotMatchFieldNameButBothTypeIsUnique, NoMatchFieldNameButOtherFieldIsIgnored,
                NoMatchFieldNameButFieldIsExplicit,
        ]
    }

    def "Failed to resolve the constructor of type"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(modelType)

        when:
        resolver.resolve()

        then:
        def e = thrown excecptionType
        e.message.split("\n")[0].matches message

        where:
        modelType                             || excecptionType                      | message
        AllConstructorsAreNotAnnotated        || AmbiguousExcelModelCreatorException | "Ambiguous constructors\\[.+] to resolve; Annotate constructor you want with @ExcelModelCreator"
        ConstructorsAreAnnotated              || AmbiguousExcelModelCreatorException | "Ambiguous constructors\\[.+] to resolve; Remove @ExcelModelCreator from other constructors except the one"
        NoMatchFieldType                      || InvalidExcelModelCreatorException   | "Unable to resolve parameter type\\[.+] of the .+\\[.+]; .+ has parameter type that is not contained in types of the targeted fields\\[.+]"
        EmptyFieldName                        || InvalidExcelModelCreatorException   | "ResolvedParameter.name must have text, but it isn't: '.*'"
        NoMatchFieldName                      || InvalidExcelModelCreatorException   | "ResolvedParameter.name must match name of the targeted fields, but it isn't: \\(actual: '.+', allowed: \\[.+]\\)"
        DuplicatedFieldName                   || InvalidExcelModelCreatorException   | "Each ResolvedParameter.name must be unique, but it isn't: \\(duplicated: '.+', names: \\[.+]\\)"
        NoMatchFieldTypeAndName               || InvalidExcelModelCreatorException   | "Not found field\\[.+ .+] to map parameter\\[.+ .+] with; Check if the parameter of the .+\\[.+] matches its type and name with that fields"
        NoMatchFieldTypeAndNameWithAnnotation || InvalidExcelModelCreatorException   | "Not found field\\[.+ .+] to map parameter\\[@FieldName\\('.+'\\) .+ .+] with; Check if the parameter of the .+\\[.+] matches its type and name with that fields"
    }

}
