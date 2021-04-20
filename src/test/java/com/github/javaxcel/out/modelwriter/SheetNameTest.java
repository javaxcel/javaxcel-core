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

import com.github.javaxcel.ExcelWriterTester;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.ModelWriter;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @see com.github.javaxcel.out.AbstractExcelWriter#sheetName(String)
 */
@StopwatchProvider
class SheetNameTest extends ExcelWriterTester {

    // Maximum sheet name length is 31.
    private static final String SHEET_NAME = UUID.randomUUID().toString().substring(0, 31);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "sheet:name", "sheet\\name", "sheet*name", "sheet?name",
            "sheet/name", "sheet[name", "sheet]name", "sheet[name]",
            "c12e89a4-846b-4c74-b60e-b9c78b577577",
    })
    @DisplayName("When sets invalid sheet name")
    void fail(String sheetName, Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", XSSFWorkbook.class.getSimpleName());
        Workbook workbook = new XSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("create '%s' instance with invalid sheet name(%s)",
                ModelWriter.class.getSimpleName(), sheetName);
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, FailureModel.class)
                .sheetName(sheetName))
                .as("Throws IllegalArgumentException")
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("When sets valid sheet name")
    void succeed(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<SuccessModel> type = SuccessModel.class;
        String filename = type.getSimpleName().toLowerCase() + ".xlsx";
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        ExcelWriterFactory.create(whenModel.getWorkbook(), givenModel.getType())
                .sheetName(WorkbookUtil.createSafeSheetName(SHEET_NAME))
                .write(whenModel.getOutputStream(), (List) thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file);

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        assertThat(workbook.getSheetAt(0).getSheetName())
                .isEqualTo(SHEET_NAME);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    static class SuccessModel {
        private int integer;
        private String string;
    }

    static class FailureModel {
        private int integer;
        private String string;
    }

}
