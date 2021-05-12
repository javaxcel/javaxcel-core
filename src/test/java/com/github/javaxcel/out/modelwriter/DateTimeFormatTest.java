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

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.ModelWriterTester;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelDateTimeFormat#pattern()
 */
@StopwatchProvider
class DateTimeFormatTest extends ModelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<ChronoModel> type = ChronoModel.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertDateTimeFormat(file);
    }

    private static void assertDateTimeFormat(File file) throws IOException {
        @Cleanup Workbook workbook = WorkbookFactory.create(file);

        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put(ChronoModel.DATE_PATTERN,
                Pattern.compile("[1-9]\\d{3}" + // "yyyy"
                        "(0[1-9]|1[0-2])" + // "MM"
                        "(0[1-9]|1\\d|2\\d|3[0-1])")); // "dd"
        patternMap.put(ChronoModel.TIME_PATTERN,
                Pattern.compile("(0\\d|1\\d|2[0-3])/" + // "HH/"
                        "[0-5]\\d/" + // "mm/"
                        "[0-5]\\d/" + // "ss/"
                        "\\d{3}")); // "SSS"
        patternMap.put(ChronoModel.DATE_TIME_PATTERN,
                Pattern.compile("[1-9]\\d{3}/" + // "yyyy/"
                        "(0[1-9]|1[0-2])/" + // "MM/"
                        "(0[1-9]|1\\d|2\\d|3[0-1]) \\| " + // "dd | "
                        "(0\\d|1\\d|2[0-3]):" + // "HH:"
                        "[0-5]\\d:" + // "mm:"
                        "[0-5]\\d\\." + // "ss."
                        "\\d{3}")); // "SSS"

        List<Map<String, Object>> models = ExcelReaderFactory.create(workbook).read();
        for (Map<String, Object> model : models) {
            String date = (String) model.get("date");
            String time = (String) model.get("time");
            String dateTime = (String) model.get("dateTime");

            assertThat(date)
                    .as("#2 Pattern of LocalDate field is equal to '%s'", ChronoModel.DATE_PATTERN)
                    .isNotBlank()
                    .hasSameSizeAs(ChronoModel.DATE_PATTERN)
                    .matches(patternMap.get(ChronoModel.DATE_PATTERN));
            assertThat(time)
                    .as("#3 Pattern of LocalTime field is equal to '%s'", ChronoModel.TIME_PATTERN)
                    .isNotBlank()
                    .hasSameSizeAs(ChronoModel.TIME_PATTERN)
                    .matches(patternMap.get(ChronoModel.TIME_PATTERN));
            assertThat(dateTime)
                    .as("#4 Pattern of LocalDateTime field is equal to '%s'", ChronoModel.DATE_TIME_PATTERN)
                    .isNotBlank()
                    .hasSize(ChronoModel.DATE_TIME_PATTERN.length())
                    .matches(patternMap.get(ChronoModel.DATE_TIME_PATTERN));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class ChronoModel {
        static final String DATE_PATTERN = "yyyyMMdd";
        static final String TIME_PATTERN = "HH/mm/ss/SSS";
        static final String DATE_TIME_PATTERN = "yyyy/MM/dd | HH:mm:ss.SSS";

        private long id;
        @ExcelDateTimeFormat(pattern = DATE_PATTERN)
        private LocalDate date;
        @ExcelDateTimeFormat(pattern = TIME_PATTERN)
        private LocalTime time;
        @ExcelDateTimeFormat(pattern = DATE_TIME_PATTERN)
        private LocalDateTime dateTime;
    }

}
