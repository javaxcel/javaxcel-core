/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.out.strategy

import com.github.javaxcel.out.context.ExcelWriteContext
import com.github.javaxcel.out.core.impl.MapWriter
import com.github.javaxcel.out.core.impl.ModelWriter
import com.github.javaxcel.out.strategy.impl.DefaultValue
import com.github.javaxcel.out.strategy.impl.HeaderNames
import com.github.javaxcel.out.strategy.impl.KeyNames
import com.github.javaxcel.out.strategy.impl.SheetName
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.util.function.Function

import static java.util.stream.Collectors.toMap

@SuppressWarnings("GroovyResultOfObjectAllocationIgnored")
class ExcelWriteStrategySpec extends Specification {

    def "defaultValue"() {
        given:
        def legalDefaultValue = "<null>"
        def illegalDefaultValue = " "
        def contextMap = [
                (ModelWriter): new ExcelWriteContext<>(new XSSFWorkbook(), String, ModelWriter),
                (MapWriter)  : new ExcelWriteContext<>(new XSSFWorkbook(), String, MapWriter),
        ]

        when: "Create strategy with legal argument"
        def strategy = new DefaultValue(legalDefaultValue)

        then: "Succeed to create strategy"
        strategy.isSupported(contextMap[ModelWriter])
        strategy.isSupported(contextMap[MapWriter])
        strategy.execute(null) == legalDefaultValue

        when: "Create strategy with illegal argument"
        new DefaultValue(illegalDefaultValue)

        then: "Failed to create strategy"
        def e = thrown IllegalArgumentException
        e.message == "defaultValue is not allowed to be null or blank"
    }

    def "sheetName"() {
        given:
        def legalSheetName = "my-sheet"
        def illegalSheetName = "[Sheet:1]"
        def contextMap = [
                (ModelWriter.class): new ExcelWriteContext<>(new XSSFWorkbook(), String, ModelWriter),
                (MapWriter.class)  : new ExcelWriteContext<>(new XSSFWorkbook(), String, MapWriter),
        ]

        when: "Create strategy with legal argument"
        def strategy = new SheetName(legalSheetName)

        then: "Succeed to create strategy"
        strategy.isSupported(contextMap[ModelWriter])
        strategy.isSupported(contextMap[MapWriter])
        strategy.execute(null) == legalSheetName

        when: "Create strategy with illegal argument"
        new SheetName(illegalSheetName)

        then: "Failed to create strategy"
        def e = thrown IllegalArgumentException
        e.message == "sheetName is not allowed to contain invalid character: $illegalSheetName"
    }

    def "headerNames"() {
        given:
        def legalHeaderNames = ["alpha", "beta", "gamma"]
        def illegalHeaderNames = ["alpha", "beta", "alpha"]
        def contextMap = [
                (ModelWriter.class): new ExcelWriteContext<>(new XSSFWorkbook(), String, ModelWriter),
                (MapWriter.class)  : new ExcelWriteContext<>(new XSSFWorkbook(), String, MapWriter),
        ]

        when: "Create strategy with legal argument"
        def strategy = new HeaderNames(legalHeaderNames)

        then: "Succeed to create strategy"
        strategy.isSupported(contextMap[ModelWriter])
        !strategy.isSupported(contextMap[MapWriter])
        strategy.execute(null) == legalHeaderNames

        when: "Create strategy with illegal argument"
        new HeaderNames(illegalHeaderNames)

        then: "Failed to create strategy"
        def e = thrown IllegalArgumentException
        e.message == "headerNames cannot have duplicated elements: $illegalHeaderNames"
    }

    def "keyNames"() {
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
