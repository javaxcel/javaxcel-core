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

package com.github.javaxcel.out.mapwriter;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.MapWriter;
import com.github.javaxcel.out.MapWriterTester;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.StreamUtils;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see com.github.javaxcel.out.MapWriter#headerNames(List)
 * @see com.github.javaxcel.out.MapWriter#headerNames(List, List)
 */
@StopwatchProvider
class HeaderNamesTest extends MapWriterTester {

    private static final int NUM_OF_COLUMNS = 10;

    private static final List<String> orderedKeys = IntStream.range(0, NUM_OF_COLUMNS)
            .mapToObj(n -> "FIELD_" + (n + 1)).collect(toList());

    private static final List<String> headerNames = orderedKeys.stream()
            .map(name -> name.replace("FIELD_", "column-")).collect(toList());

    @ParameterizedTest
    @EnumSource(TestCase.class)
    void succeed(TestCase testCase, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = testCase.name().toLowerCase() + '.' + ExcelUtils.EXCEL_97_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, stopwatch, testCase);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.getFile());
        Workbook workbook = new HSSFWorkbook();

        /*
        To create multiple sheets, generates models as many
        as the amount exceeds the maximum number of rows per sheet.
         */
        return new WhenModel(out, workbook, (int) (ExcelUtils.getMaxRows(workbook) * 1.1));
    }

    @Override
    protected ThenModel whenCreateModels(GivenModel givenModel, WhenModel whenModel) {
        List<Map<String, Object>> models = TestUtils.getRandomMaps(whenModel.getNumOfMocks(), NUM_OF_COLUMNS);
        return new ThenModel(models);
    }

    @Override
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        MapWriter<Workbook, Map<String, Object>> writer = ExcelWriterFactory.create(whenModel.getWorkbook());

        TestCase testCase = (TestCase) Objects.requireNonNull(givenModel.getArgs())[0];
        if (testCase == TestCase.JUST_ORDERED) {
            writer.headerNames(orderedKeys);
        } else if (testCase == TestCase.ORDER_AND_CONVERT) {
            writer.headerNames(orderedKeys, headerNames);
        } else {
            throw new IllegalArgumentException("Unexpected enum constant: " + testCase);
        }

        writer.write(whenModel.getOutputStream(), thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        TestCase testCase = (TestCase) Objects.requireNonNull(givenModel.getArgs())[0];

        assertThat(ExcelUtils.getSheets(workbook).stream()
                .flatMap(it -> StreamUtils.toStream(it.getRow(0).iterator()))
                .map(Cell::getStringCellValue)
                .collect(toList()))
                .as("#2 All header names at each sheet must be sorted")
                .containsExactlyElementsOf(Collections.nCopies(workbook.getNumberOfSheets(),
                        testCase == TestCase.JUST_ORDERED ? orderedKeys : headerNames)
                        .stream().flatMap(List::stream).collect(toList()));
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private enum TestCase {
        JUST_ORDERED, ORDER_AND_CONVERT
    }

}
