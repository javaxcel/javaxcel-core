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
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class HeaderNamesTest extends ExcelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<Computer> type = Computer.class;
        String filename = type.getSimpleName().toLowerCase() + ".xlsx";
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);

        assertNotEmptyFile(file);

        List<String> headerNames = FieldUtils.getTargetedFields(givenModel.getType())
                .stream().map(f -> camelToSnake(f.getName()).toUpperCase()).collect(toList());
        Row header = workbook.getSheetAt(0).getRow(0);

        List<String> list = new ArrayList<>();
        for (Cell cell : header) {
            list.add(cell.getStringCellValue());
        }

        assertThat(list).containsExactlyElementsOf(headerNames);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Getter
    @Setter
    @ToString
    static class Computer {
        @ExcelColumn(name = "CPU")
        private BigInteger cpu;
        @ExcelColumn(name = "RAM")
        private Double ram;
        @ExcelColumn(name = "DISK")
        private Long disk;
        @ExcelColumn(name = "INPUT_DEVICE")
        private String inputDevice;
        @ExcelColumn(name = "OUTPUT_DEVICE")
        private String outputDevice;
        @ExcelColumn(name = "MANUFACTURER")
        private String manufacturer;
        @ExcelColumn(name = "PRICE")
        private int price;
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
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                // If the character is lower case then
                // add such character into result string
                result.append(ch);
            }
        }

        return result.toString();
    }

}
