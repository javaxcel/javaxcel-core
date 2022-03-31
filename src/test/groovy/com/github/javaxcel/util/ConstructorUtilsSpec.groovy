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

package com.github.javaxcel.util

import com.github.javaxcel.exception.NoTargetedConstructorException
import com.github.javaxcel.internal.model.ConstructorModel.PackagePrivateConstructor
import com.github.javaxcel.internal.model.ConstructorModel.PrivateConstructor
import com.github.javaxcel.internal.model.ConstructorModel.ProtectedConstructor
import com.github.javaxcel.internal.model.ConstructorModel.PublicConstructor
import com.github.javaxcel.internal.model.ExcelModelConstructorTester.AnnotatedConstructors
import com.github.javaxcel.internal.model.ExcelModelConstructorTester.InvalidFieldName
import com.github.javaxcel.internal.model.ExcelModelConstructorTester.NotAnnotatedAllConstructors
import com.github.javaxcel.internal.model.ExcelModelConstructorTester.PublicNoArgs
import spock.lang.Specification

class ConstructorUtilsSpec extends Specification {

    def "Gets the targeted constructor of type"() {
        when:
        def constructor = ConstructorUtils.getTargetedConstructor type

        then:
        noExceptionThrown()
        constructor != null

        where:
        type << [PublicNoArgs]
    }

    def "Failed to get the targeted constructor of type"() {
        when:
        ConstructorUtils.getTargetedConstructor type

        then:
        def e = thrown excecptionType
        e.message.matches message

        where:
        type                        || excecptionType                 | message
        NotAnnotatedAllConstructors || NoTargetedConstructorException | "Ambiguous constructors\\[.+] to resolve; Annotate constructor you want with @ExcelModelConstructor"
        AnnotatedConstructors       || NoTargetedConstructorException | "Ambiguous constructors\\[.+] to resolve; Remove @ExcelModelConstructor from other constructors except the one"
        InvalidFieldName            || IllegalArgumentException       | "@FieldName.value must have text, but it isn't: '.*'"
    }

    def "Stringifies constructor simply"() {
        given:
        def paramTypes = FieldUtils.getTargetedFields(type).collect({ it.type }) as Class<?>[]
        def constructor = type.getDeclaredConstructor paramTypes

        when:
        def actual = ConstructorUtils.toSimpleString constructor

        then:
        def paramTypeNames = paramTypes.collect({ it.simpleName }).toString().replaceAll('^\\[(.*)]$', '$1')
        actual == "$expected($paramTypeNames)"

        where:
        type                      | expected
        PublicConstructor         | "public $type.simpleName"
        ProtectedConstructor      | "protected $type.simpleName"
        PackagePrivateConstructor | "$type.simpleName"
        PrivateConstructor        | "private $type.simpleName"
    }

}
