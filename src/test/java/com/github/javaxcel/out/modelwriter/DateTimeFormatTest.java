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
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

@StopwatchProvider
class DateTimeFormatTest {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws IOException {
        // given
        String filename = ChronoModel.class.getSimpleName().toLowerCase() + ".xlsx";
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup OutputStream out = new FileOutputStream(file);
        Workbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = 8192;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<ChronoModel> models = TestUtils.getMocks(ChronoModel.class, numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start("write %,d models", numOfMocks);
        ExcelWriterFactory.create(workbook, ChronoModel.class).write(out, models);
        stopwatch.stop();

        // then
        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertDateTimeFormat(file);
    }

    private void assertDateTimeFormat(File file) throws IOException {
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

    @Getter
    @Setter
    static class ChronoModel {
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