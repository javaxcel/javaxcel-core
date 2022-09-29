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

package com.github.javaxcel.out.core;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.FilenameUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public abstract class ModelWriterTester {

    // Template method.
    protected final void run(File file, Class<?> type, Stopwatch stopwatch) throws Exception {
        GivenModel givenModel = new GivenModel(file, type);

        // given
        stopwatch.start("create '%s' file", givenModel.file.getName());
        WhenModel whenModel = given(givenModel);
        stopwatch.stop();

        try {
            // when: 1
            stopwatch.start("create %,d mocks", whenModel.numOfMocks);
            ThenModel thenModel = whenCreateModels(givenModel, whenModel);
            stopwatch.stop();

            // when: 2
            stopwatch.start("write %,d models", whenModel.numOfMocks);
            whenWriteWorkbook(givenModel, whenModel, thenModel);
            stopwatch.stop();

            // then
            then(givenModel, whenModel, thenModel);
        } finally {
            whenModel.outputStream.close();
        }
    }

    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.file);

        String extension = FilenameUtils.getExtension(givenModel.file.getName());
        Workbook workbook;
        switch (extension) {
            case ExcelUtils.EXCEL_97_EXTENSION:
                workbook = new HSSFWorkbook();
                break;
            case ExcelUtils.EXCEL_2007_EXTENSION:
                workbook = new SXSSFWorkbook();
                break;
            default:
                throw new IllegalArgumentException("Invalid extension of Excel file: " + extension);
        }

        return new WhenModel(out, workbook, 8192);
    }

    protected ThenModel whenCreateModels(GivenModel givenModel, WhenModel whenModel) {
        List<?> models = TestUtils.getMocks(givenModel.type, whenModel.numOfMocks);
        return new ThenModel(models);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        TestUtils.JAVAXCEL.writer(whenModel.workbook, givenModel.type)
                .write(whenModel.outputStream, (List) thenModel.models);
    }

    protected abstract void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception;

    // -------------------------------------------------------------------------------------------------

    @Getter
    @RequiredArgsConstructor
    protected static class GivenModel {
        @NotNull
        private final File file;
        @Null
        private final Class<?> type;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    protected static class WhenModel {
        @NotNull
        private final OutputStream outputStream;
        @NotNull
        private final Workbook workbook;
        private int numOfMocks;
    }

    @Getter
    @RequiredArgsConstructor
    protected static class ThenModel {
        @NotNull
        private final List<?> models;
    }

}
