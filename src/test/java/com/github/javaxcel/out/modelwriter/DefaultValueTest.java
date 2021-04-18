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
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class DefaultValueTest extends ExcelWriterTester {

    private static final String MODEL_DEFAULT_VALUE = "(empty)";
    private static final String COLUMN_DEFAULT_VALUE = "<null>";

    @ParameterizedTest
    @ValueSource(classes = {WithModel.class, WithColumn.class})
    @DisplayName("When sets default value")
    void test(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = type.getSimpleName().toLowerCase() + ".xlsx";
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        Class<?> type = givenModel.getType();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertDefaultValue(type, file);
    }

    private void assertDefaultValue(Class<?> type, File file) throws IOException {
        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        List<Map<String, Object>> models = ExcelReaderFactory.create(workbook).read();

        for (Map<String, Object> model : models) {
            String title = (String) model.get("title");

            String defaultValue = type == WithModel.class
                                          ? MODEL_DEFAULT_VALUE
                                          : COLUMN_DEFAULT_VALUE;
            assertThat(title)
                    .as("#2 Empty value must be converted '%s' as default value", defaultValue)
                    .isNotNull().isEqualTo(defaultValue);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Getter
    @Setter
    @ToString
    @ExcelModel(defaultValue = MODEL_DEFAULT_VALUE)
    static class WithModel {
        private Long id;
        @TestUtils.Unrandomized
        private String title;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @ToString
    static class WithColumn {
        private Long id;
        @TestUtils.Unrandomized
        @ExcelColumn(defaultValue = COLUMN_DEFAULT_VALUE)
        private String title;
        private LocalDateTime createdAt;
    }

}
