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

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class SheetRollingTest {

    @Test
    @DisplayName("SheetRolling")
    void writeModelsWithIgnoredFields(@TempDir Path path, Stopwatch stopwatch) throws IOException {
        String filename = SimpleModel.class.getSimpleName().toLowerCase() + ".xls";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup OutputStream out = new FileOutputStream(file);
        Workbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        /*
        To create multiple sheets, generates models as many
        as the amount exceeds the maximum number of rows per sheet.
        */
        final int numOfMocks = (int) (ExcelUtils.getMaxRows(workbook) * 1.1);
        stopwatch.start("create %,d mocks", numOfMocks);
        List<SimpleModel> models = TestUtils.getMocks(SimpleModel.class, numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start("write %,d models", numOfMocks);
        ExcelWriterFactory.create(workbook, SimpleModel.class).write(out, models);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file must be created and have content")
                .isNotNull().exists().canRead().isNotEmpty();

        @Cleanup Workbook wb = WorkbookFactory.create(file);
        assertThat(ExcelUtils.getNumOfModels(wb))
                .as("#2 The number of actually written model is %,d", models.size())
                .isEqualTo(models.size());
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Getter
    @Setter
    static class SimpleModel {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
    }

}
