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

package com.github.javaxcel.out.strategy.impl

import com.github.javaxcel.out.context.ExcelWriteContext
import com.github.javaxcel.out.core.impl.MapWriter
import com.github.javaxcel.out.core.impl.ModelWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.util.function.Function

import static java.util.stream.Collectors.toMap

@SuppressWarnings("GroovyResultOfObjectAllocationIgnored")
class KeyNamesSpec extends Specification {

    def "test"() {
        given:
        def legalKeyNames = ["width", "depth", "height"]
        def illegalKeyNames = ["alpha", "beta", "alpha"]
        def contextMap = [
                (ModelWriter.class): new ExcelWriteContext<>(new XSSFWorkbook(), String, ModelWriter),
                (MapWriter.class)  : new ExcelWriteContext<>(new XSSFWorkbook(), String, MapWriter),
        ]

        when: "Create strategy with legal argument"
        def strategy = new KeyNames(legalKeyNames, legalKeyNames)

        then: "Succeed to create strategy"
        !strategy.isSupported(contextMap[ModelWriter])
        strategy.isSupported(contextMap[MapWriter])
        def orders = (0..legalKeyNames.size() - 1).stream().collect toMap({ legalKeyNames[it] }, Function.identity())
        strategy.execute(null) == [orders: orders, names: legalKeyNames]

        when: "Create strategy with illegal argument"
        new KeyNames(illegalKeyNames)

        then: "Failed to create strategy"
        def e0 = thrown IllegalArgumentException
        e0.message == "keyOrders cannot have duplicated elements: $illegalKeyNames"

        when: "Create strategy with illegal arguments"
        new KeyNames(legalKeyNames, illegalKeyNames)

        then: "Failed to create strategy"
        def e1 = thrown IllegalArgumentException
        e1.message == "newKeyNames cannot have duplicated elements: $illegalKeyNames"
    }

}
