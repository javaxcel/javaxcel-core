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

import io.github.imsejin.common.tool.RandomString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static com.github.javaxcel.util.ExcelUtils.EXCEL_2007_EXTENSION
import static com.github.javaxcel.util.ExcelUtils.EXCEL_97_EXTENSION

class ExcelUtilsSpec extends Specification {

    @TempDir
    private Path tempPath

    def "Gets instance of workbook"() {
        given:
        def createWorkbookPath = { Workbook workbook ->
            def extension = workbook instanceof HSSFWorkbook ? EXCEL_97_EXTENSION : EXCEL_2007_EXTENSION
            def fileName = "${new RandomString().nextString(8)}.$extension"
            def tempFile = tempPath.resolve(fileName)
            workbook.write(new FileOutputStream(tempFile.toFile()))
            tempFile
        }

        when: "Writes Excel 97 file"
        def workbookPath = createWorkbookPath(new HSSFWorkbook())
        def workbook = ExcelUtils.getWorkbook(workbookPath.toFile())

        then:
        workbook != null
        workbook instanceof HSSFWorkbook

        when: "Writes Excel 2007 file"
        workbookPath = createWorkbookPath(new XSSFWorkbook())
        workbook = ExcelUtils.getWorkbook(workbookPath.toFile())

        then:
        workbook != null
        workbook instanceof XSSFWorkbook

        when: "Writes empty file"
        workbookPath = tempPath.resolve("${new RandomString().nextString(8)}.xlsx")
        Files.createFile(workbookPath)
        ExcelUtils.getWorkbook(workbookPath.toFile())

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "The supplied file was empty (zero bytes long)"
    }

}
