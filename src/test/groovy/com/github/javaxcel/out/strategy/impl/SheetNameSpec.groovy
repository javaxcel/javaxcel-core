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

@SuppressWarnings("GroovyResultOfObjectAllocationIgnored")
class SheetNameSpec extends Specification {

    def "test"() {
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
        e.message.split("\n")[0] == "ExcelWriteStrategy.SheetName.value is not allowed to contain invalid character: $illegalSheetName"
    }

}
