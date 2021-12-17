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

package com.github.javaxcel.out.core.mapwriter;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.core.MapWriterTester;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.KeyNames;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.StreamUtils;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.javaxcel.TestUtils.MAP_KEY_PREFIX;
import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @see KeyNames
 */
@StopwatchProvider
class HeaderNamesTest extends MapWriterTester {

    private static final int NUM_OF_COLUMNS = 10;

    private static final List<String> orderedKeys = IntStream.range(0, NUM_OF_COLUMNS)
            .mapToObj(n -> MAP_KEY_PREFIX + (n + 1)).collect(toList());

    private static final List<String> headerNames = orderedKeys.stream()
            .map(name -> name.replace(MAP_KEY_PREFIX, "column-")).collect(toList());

    @Test
    @StopwatchProvider(TimeUnit.MILLISECONDS)
    void fail(Stopwatch stopwatch) {
        // expect
        stopwatch.start("sort with empty list");
        assertThatThrownBy(() -> ExcelWriterFactory.create(new XSSFWorkbook())
                .options(new KeyNames(Collections.emptyList())))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("keyOrders is not allowed to be null or empty");
        stopwatch.stop();

        stopwatch.start("#1 sort with unmatched list");
        assertThatThrownBy(() -> ExcelWriterFactory.create(new XSSFWorkbook())
                .options(new KeyNames(Arrays.asList("column_1", "column_2", "column_3")))
                .write(null, TestUtils.getRandomMaps(10, NUM_OF_COLUMNS)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("MapWriter.keys is not equal to keyMap.orders.size");
        stopwatch.stop();

        stopwatch.start("#2 sort with unmatched list");
        assertThatThrownBy(() -> ExcelWriterFactory.create(new XSSFWorkbook())
                .options(new KeyNames(headerNames))
                .write(null, TestUtils.getRandomMaps(10, NUM_OF_COLUMNS)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("MapWriter.keys is at variance with keyMap.orders.keySet");
        stopwatch.stop();

        stopwatch.start("convert header names with unmatched list");
        assertThatThrownBy(() -> ExcelWriterFactory.create(new XSSFWorkbook())
                .options(new KeyNames(Arrays.asList("column_1", "column_2", "column_3"), Arrays.asList("column_1", "column_2"))))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("newKeyNames.size is not equal to keyOrders.size");
    }

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
        ExcelWriter<Map<String, Object>> writer = ExcelWriterFactory.create(whenModel.getWorkbook());

        TestCase testCase = (TestCase) Objects.requireNonNull(givenModel.getArgs())[0];
        if (testCase == TestCase.JUST_ORDERED) {
            writer.options(new KeyNames(orderedKeys));
        } else if (testCase == TestCase.ORDER_AND_CONVERT) {
            writer.options(new KeyNames(orderedKeys, headerNames));
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
