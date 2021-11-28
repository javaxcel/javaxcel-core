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
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.ModelWriter;
import com.github.javaxcel.out.core.ModelWriterTester;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.DefaultValue;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelModel#defaultValue()
 * @see ExcelColumn#defaultValue()
 * @see ModelWriter#defaultValue(String)
 */
@StopwatchProvider
class DefaultValueTest extends ModelWriterTester {

    private static final String MODEL_DEFAULT_VALUE = "(empty)";
    private static final String COLUMN_DEFAULT_VALUE = "<null>";
    private static final String DIRECT_DEFAULT_VALUE = "[none]";

    @ParameterizedTest
    @ValueSource(classes = {
            WithModel.class, WithColumn.class, WithModelAndColumn.class,
            WithModelAndDirect.class, WithColumnAndDirect.class, WithModelAndColumnAndDirect.class,
    })
    @DisplayName("When sets default value")
    void test(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        Class<?> type = givenModel.getType();

        ExcelWriter<?> writer = ExcelWriterFactory.init().create(whenModel.getWorkbook(), type);
        if (getDefaultValueFromType(type).equals(DIRECT_DEFAULT_VALUE)) {
            writer.options(new DefaultValue(DIRECT_DEFAULT_VALUE));
        }

        writer.write(whenModel.getOutputStream(), (List) thenModel.getModels());
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

            String defaultValue = getDefaultValueFromType(type);
            assertThat(title)
                    .as("#2 Empty value must be converted '%s' as default value", defaultValue)
                    .isNotNull().isEqualTo(defaultValue);
        }
    }

    private static String getDefaultValueFromType(Class<?> type) {
        if (type == WithModel.class) return MODEL_DEFAULT_VALUE;
        else if (type == WithColumn.class || type == WithModelAndColumn.class) return COLUMN_DEFAULT_VALUE;
        else return DIRECT_DEFAULT_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @ToString
    @ExcelModel(defaultValue = MODEL_DEFAULT_VALUE)
    private static class WithModel {
        private Long id;
        @TestUtils.Unrandomized
        private String title;
        private LocalDateTime createdAt;
    }

    @ToString
    private static class WithColumn {
        private Long id;
        @TestUtils.Unrandomized
        @ExcelColumn(defaultValue = COLUMN_DEFAULT_VALUE)
        private String title;
        private LocalDateTime createdAt;
    }

    @ToString
    @ExcelModel(defaultValue = MODEL_DEFAULT_VALUE)
    private static class WithModelAndColumn {
        private Long id;
        @TestUtils.Unrandomized
        @ExcelColumn(defaultValue = COLUMN_DEFAULT_VALUE)
        private String title;
        private LocalDateTime createdAt;
    }

    @ToString
    @ExcelModel(defaultValue = MODEL_DEFAULT_VALUE)
    private static class WithModelAndDirect {
        private Long id;
        @TestUtils.Unrandomized
        private String title;
        private LocalDateTime createdAt;
    }

    @ToString
    private static class WithColumnAndDirect {
        private Long id;
        @TestUtils.Unrandomized
        @ExcelColumn(defaultValue = COLUMN_DEFAULT_VALUE)
        private String title;
        private LocalDateTime createdAt;
    }

    @ToString
    @ExcelModel(defaultValue = MODEL_DEFAULT_VALUE)
    private static class WithModelAndColumnAndDirect {
        private Long id;
        @TestUtils.Unrandomized
        @ExcelColumn(defaultValue = COLUMN_DEFAULT_VALUE)
        private String title;
        private LocalDateTime createdAt;
    }

}
