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

package com.github.javaxcel.out.modelwriter;

import com.github.javaxcel.out.ModelWriterTester;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.ModelWriter;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ModelWriter#unrotate()
 */
@StopwatchProvider
class SheetUnrotationTest extends ModelWriterTester {

    private static final String SHEET_NAME = SheetUnrotationTest.class.getSimpleName() + "Sheet";

    @Test
    @DisplayName("When writes models unrotating sheet")
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<SimpleModel> type = SimpleModel.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_97_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.getFile());
        Workbook workbook = new HSSFWorkbook();

        /*
         To create multiple sheets, generates models as many
         as the amount exceeds the maximum number of rows per sheet.
         */
        final int numOfMocks = (int) (ExcelUtils.getMaxRows(workbook) * 1.1);

        return new WhenModel(out, workbook, numOfMocks);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        ExcelWriterFactory.create(whenModel.getWorkbook(), givenModel.getType())
                .sheetName(SHEET_NAME).unrotate()
                .write(whenModel.getOutputStream(), (List) thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        assertNotEmptyFile(givenModel.getFile(), "#1 Excel file must be created and have content");

        @Cleanup Workbook workbook = WorkbookFactory.create(givenModel.getFile());
        List<Sheet> sheets = ExcelUtils.getSheets(workbook);

        assertThat(sheets)
                .isNotNull().hasSize(1);
        assertThat(sheets.stream()
                .map(Sheet::getSheetName).collect(toList()))
                .as("#2 Sheet name is equal to '%s'", SHEET_NAME)
                .allMatch(SHEET_NAME::equals);

        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as("#3 The number of actually written all models is equal to the number of models in first sheet")
                .isEqualTo(ExcelUtils.getNumOfModels(sheets.get(0)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class SimpleModel {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
    }

}
