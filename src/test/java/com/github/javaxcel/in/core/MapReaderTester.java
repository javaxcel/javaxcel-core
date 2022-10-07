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

package com.github.javaxcel.in.core;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.util.ExcelUtils;
import com.github.pjfanning.xlsx.StreamingReader;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.FilenameUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public abstract class MapReaderTester {

    // Template method.
    protected final void run(File file, Stopwatch stopwatch) throws Exception {
        run(file, stopwatch, 8192);
    }

    // Template method.
    protected final void run(File file, Stopwatch stopwatch, int numOfMocks) throws Exception {
        GivenModel givenModel = new GivenModel(file);
        givenModel.numOfMocks = numOfMocks;

        WhenModel whenModel;
        try {
            // given: 1
            stopwatch.start("create '%s' file", givenModel.file.getName());
            givenCreateFile(givenModel);
            stopwatch.stop();
            // given: 2
            stopwatch.start("create %,d mocks", givenModel.numOfMocks);
            whenModel = givenCreateMocks(givenModel);
            stopwatch.stop();
            // given: 3
            stopwatch.start("write %,d maps", givenModel.numOfMocks);
            givenWriteMaps(givenModel, whenModel);
            stopwatch.stop();
        } finally {
            givenModel.outputStream.close();
            givenModel.workbook.close();
        }

        try {
            // when: 1
            stopwatch.start("load '%s' file", givenModel.file.getName());
            whenGetWorkbook(givenModel, whenModel);
            stopwatch.stop();
            // when: 2
            stopwatch.start("read %,d maps", givenModel.numOfMocks);
            ThenModel thenModel = whenReadMaps(givenModel, whenModel);
            stopwatch.stop();

            // then
            then(givenModel, whenModel, thenModel);
        } finally {
            whenModel.workbook.close();
        }
    }

    protected void givenCreateFile(GivenModel givenModel) throws Exception {
        givenModel.outputStream = new FileOutputStream(givenModel.file);

        String extension = FilenameUtils.getExtension(givenModel.file.getName());
        givenModel.workbook = extension.equals(ExcelUtils.EXCEL_97_EXTENSION)
                ? new HSSFWorkbook() : new SXSSFWorkbook();
    }

    protected WhenModel givenCreateMocks(GivenModel givenModel) {
        List<Map<String, Object>> mocks = TestUtils.getRandomMaps(givenModel.numOfMocks, 5);
        return new WhenModel(mocks);
    }

    protected void givenWriteMaps(GivenModel givenModel, WhenModel whenModel) {
        TestUtils.JAVAXCEL.writer(givenModel.workbook)
                .write(givenModel.outputStream, whenModel.mocks);
    }

    protected void whenGetWorkbook(GivenModel givenModel, WhenModel whenModel) throws Exception {
        String extension = FilenameUtils.getExtension(givenModel.file.getName());

        Workbook workbook;
        switch (extension) {
            case ExcelUtils.EXCEL_97_EXTENSION:
                workbook = new HSSFWorkbook(new FileInputStream(givenModel.file));
                break;
            case ExcelUtils.EXCEL_2007_EXTENSION:
                workbook = StreamingReader.builder().open(givenModel.file);
                break;
            default:
                throw new IllegalArgumentException("Invalid extension of Excel file: " + extension);
        }

        whenModel.workbook = workbook;
    }

    protected ThenModel whenReadMaps(GivenModel givenModel, WhenModel whenModel) {
        List<Map<String, String>> maps = TestUtils.JAVAXCEL.reader(whenModel.workbook).read();
        return new ThenModel(maps);
    }

    protected abstract void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception;

    // -------------------------------------------------------------------------------------------------

    @Getter
    @RequiredArgsConstructor
    protected static class GivenModel {
        @NotNull
        private final File file;
        private OutputStream outputStream;
        private Workbook workbook;
        private int numOfMocks;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    protected static class WhenModel {
        private final List<Map<String, Object>> mocks;
        private Workbook workbook;
    }

    @Getter
    @RequiredArgsConstructor
    protected static class ThenModel {
        @NotNull
        private final List<Map<String, String>> maps;
    }

}
