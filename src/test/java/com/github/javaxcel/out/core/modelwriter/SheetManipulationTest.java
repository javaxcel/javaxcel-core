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

package com.github.javaxcel.out.core.modelwriter;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.internal.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.ModelWriterTester;
import com.github.javaxcel.out.strategy.impl.AutoResizedColumns;
import com.github.javaxcel.out.strategy.impl.HiddenExtraColumns;
import com.github.javaxcel.out.strategy.impl.HiddenExtraRows;
import com.github.javaxcel.out.strategy.impl.SheetName;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.ToString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see AutoResizedColumns
 * @see HiddenExtraRows
 * @see HiddenExtraColumns
 */
@StopwatchProvider
class SheetManipulationTest extends ModelWriterTester {

    @Test
    void succeed(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = getClass().getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_97_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, Book.class, stopwatch);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.getFile());
        Workbook workbook = new HSSFWorkbook();

        return new WhenModel(out, workbook, 1024);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        TestUtils.JAVAXCEL.writer(whenModel.getWorkbook(), givenModel.getType())
                .options(new SheetName("Rainbow"), new AutoResizedColumns(), new HiddenExtraRows(), new HiddenExtraColumns())
                .write(whenModel.getOutputStream(), (List) thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        int maxModels = ExcelUtils.getMaxRows(workbook) - 1;
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written maps is %,d", maxModels)
                .isEqualTo(maxModels);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @ToString
    @ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
    private static class Book {
        private Long id;
        private String title;
        private List<String> authors;
        private List<String> publishers;
        private LocalDateTime createdAt;
        private LocalDateTime publishedAt;
        private short price;
    }

}
