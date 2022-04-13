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

import com.github.javaxcel.exception.NoTargetedConstructorException
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.ConstructorsAreAnnotated
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.InvalidFieldName
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.AllConstructorsAreNotAnnotated
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.PackagePrivateConstructor
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.ParamNameDoesNotMatchFieldNameButBothTypeIsUnique
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.PrivateConstructor
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.ProtectedConstructor
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.ConstructorArgsWithoutOrder
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.PublicConstructor
import com.github.javaxcel.internal.model.ExcelModelCreatorTester.PublicNoArgsConstructor
import spock.lang.Specification

import java.lang.reflect.Constructor

class ExcelModelConstructorResolverSpec extends Specification {

    def "Gets the resolved constructor of type"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(type)

        when:
        def constructor = resolver.resolve()

        then:
        noExceptionThrown()
        constructor instanceof Constructor

        where:
        type << [
                PublicNoArgsConstructor, PublicConstructor, ProtectedConstructor, PackagePrivateConstructor,
                PrivateConstructor, ConstructorArgsWithoutOrder, ParamNameDoesNotMatchFieldNameButBothTypeIsUnique,
        ]
    }

    def "Failed to resolve the constructor of type"() {
        given:
        def resolver = new ExcelModelConstructorResolver<>(type)

        when:
        resolver.resolve()

        then:
        def e = thrown excecptionType
        e.message.matches message

        where:
        type                           || excecptionType                 | message
        AllConstructorsAreNotAnnotated || NoTargetedConstructorException | "Ambiguous constructors\\[.+] to resolve; Annotate constructor you want with @ExcelModelCreator"
        ConstructorsAreAnnotated       || NoTargetedConstructorException | "Ambiguous constructors\\[.+] to resolve; Remove @ExcelModelCreator from other constructors except the one"
        InvalidFieldName               || IllegalArgumentException       | "ResolvedParameter.name must have text, but it isn't: '.*'"
    }

}
