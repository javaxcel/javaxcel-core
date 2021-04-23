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
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.github.javaxcel.TestUtils.*;

/**
 * @see ExcelIgnore
 * @see ExcelModel#explicit()
 */
@StopwatchProvider
class IgnoreTest extends ExcelWriterTester {

    @ParameterizedTest
    @ValueSource(classes = {IgnoredModel.class, ExplicitModel.class})
    @DisplayName("@ExcelIgnore")
    void test(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        WhenModel whenModel = super.given(givenModel);
        final int numOfMocks = ExcelUtils.getMaxRows(whenModel.getWorkbook()) / 50;
        whenModel.setNumOfMocks(numOfMocks);

        return whenModel;
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        Class<?> type = givenModel.getType();
        List<?> models = thenModel.getModels();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");

        @Cleanup Workbook workbook = WorkbookFactory.create(file);
        assertEqualsNumOfModels(workbook, models, "#2 The number of actually written rows is %,d", models.size());
        assertEqualsHeaderSize(workbook, type, "#3 Header size is equal to the number of targeted fields in '%s'", type.getSimpleName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class IgnoredModel {
        private long id;

        @ExcelIgnore
        private Double width;

        @ExcelIgnore
        private double height;

        private Double weight;
    }

    @ExcelModel(explicit = true)
    private static class ExplicitModel {
        @ExcelColumn
        private Long id;

        @ExcelColumn
        private String name;

        private int count;
    }

}
