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

import com.github.javaxcel.out.ModelWriterTester;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.AbstractExcelWriter;
import com.github.javaxcel.out.ModelWriter;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.StreamUtils;
import lombok.Cleanup;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @see ModelWriter#headerNames(List)
 */
@StopwatchProvider
class HeaderNamesTest extends ModelWriterTester {

    /**
     * @see #invalidHeaderNames()
     */
    @ParameterizedTest
    @NullSource
    @MethodSource("invalidHeaderNames")
    @DisplayName("When sets invalid header names")
    void fail(List<String> headerNames, Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", XSSFWorkbook.class.getSimpleName());
        Workbook workbook = new XSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("create '%s' instance with invalid header names(%s)",
                ModelWriter.class.getSimpleName(), headerNames);
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, KebabCaseComputer.class)
                .headerNames(headerNames))
                .as("Throws IllegalArgumentException")
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(classes = {UpperSnakeCaseComputer.class, KebabCaseComputer.class})
    @DisplayName("When sets valid header names")
    void succeed(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        Class<?> type = givenModel.getType();
        OutputStream outputStream = whenModel.getOutputStream();
        Workbook workbook = whenModel.getWorkbook();
        List<?> models = thenModel.getModels();

        AbstractExcelWriter<Workbook, ?> writer = ExcelWriterFactory.create(workbook, type);
        if (type == KebabCaseComputer.class) {
            List<String> headerNames = FieldUtils.getTargetedFields(type)
                    .stream().map(Field::getName).map(getFunctionByType(type)).collect(toList());
            writer.headerNames(headerNames);
        }
        writer.write(outputStream, (List) models);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        Class<?> type = givenModel.getType();

        assertNotEmptyFile(file);

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        assertEqualsHeaderNames(workbook, type);
    }

    private static void assertEqualsHeaderNames(Workbook workbook, Class<?> type) {
        List<String> headerNames = FieldUtils.getTargetedFields(type)
                .stream().map(Field::getName).map(getFunctionByType(type)).collect(toList());

        Row header = workbook.getSheetAt(0).getRow(0);
        assertThat(StreamUtils.toStream(header.cellIterator()).map(Cell::getStringCellValue).collect(toList()))
                .containsExactlyElementsOf(headerNames);
    }

    private static Function<String, String> getFunctionByType(Class<?> type) {
        if (type == UpperSnakeCaseComputer.class) return HeaderNamesTest::camelToUpperSnake;
        else return HeaderNamesTest::camelToKebab;
    }

    private static Stream<Arguments> invalidHeaderNames() {
        int numOfFields = FieldUtils.getTargetedFields(KebabCaseComputer.class).size();

        return Stream.of(
                Arguments.of(Collections.emptyList()),
                Arguments.of(IntStream.range(0, numOfFields - 1).mapToObj(n -> "Field_" + n).collect(toList())),
                Arguments.of(IntStream.range(0, numOfFields + 1).mapToObj(n -> "Field_" + n).collect(toList()))
        );
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @ToString
    private static class UpperSnakeCaseComputer {
        @ExcelColumn(name = "CPU_CLOCK_RATE")
        private BigInteger cpuClockRate;
        @ExcelColumn(name = "RANDOM_ACCESS_MEMORY")
        private Double randomAccessMemory;
        @ExcelColumn(name = "DISK")
        private Long disk;
        @ExcelColumn(name = "IO_DEVICE")
        private String ioDevice;
    }

    @ToString
    private static class KebabCaseComputer {
        private BigInteger cpuClockRate;
        private Double randomAccessMemory;
        private Long disk;
        private String ioDevice;
    }

    private static String camelToUpperSnake(String str) {
        return camelToSnake(str).toUpperCase();
    }

    private static String camelToKebab(String str) {
        return camelToSnake(str).replace('_', '-');
    }

    private static String camelToSnake(String str) {
        // Empty String
        StringBuilder result = new StringBuilder();

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        // Tarverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (Character.isUpperCase(ch)) {
                // Check if the character is upper case
                // then append '_' and such character
                // (in lower case) to result string
                result.append('_').append(Character.toLowerCase(ch));
            } else {
                // If the character is lower case then
                // add such character into result string
                result.append(ch);
            }
        }

        return result.toString();
    }

}
