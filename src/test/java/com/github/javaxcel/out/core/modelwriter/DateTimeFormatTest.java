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
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.ModelWriterTester;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.constant.DateType;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelDateTimeFormat#pattern()
 * @see com.github.javaxcel.converter.handler.impl.DateTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.LocalDateTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.LocalTimeTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.LocalDateTimeTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.ZonedDateTimeTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.OffsetTimeTypeHandler
 * @see com.github.javaxcel.converter.handler.impl.OffsetDateTimeTypeHandler
 */
@StopwatchProvider
class DateTimeFormatTest extends ModelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<ChronoModel> type = ChronoModel.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = path.resolve(filename).toFile();

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
        patternMap.put(DateType.F_DATE_TIME.getPattern(),
                Pattern.compile("[1-9]\\d{3}-" + // "yyyy-"
                        "(0[1-9]|1[0-2])-" + // "MM-"
                        "(0[1-9]|1\\d|2\\d|3[0-1]) " + // "dd "
                        "(0\\d|1\\d|2[0-3]):" + // "HH:"
                        "[0-5]\\d:" + // "mm:"
                        "[0-5]\\d")); // "ss"

        List<Map<String, Object>> models = TestUtils.JAVAXCEL.reader(workbook).read();
        for (Map<String, Object> model : models) {
            String date = (String) model.get("date");
            String localDate = (String) model.get("localDate");
            String localTime = (String) model.get("localTime");
            String localDateTime = (String) model.get("localDateTime");
            String zonedDateTime = (String) model.get("zonedDateTime");
            String offsetTime = (String) model.get("offsetTime");
            String offsetDateTime = (String) model.get("offsetDateTime");

            assertThat(date)
                    .as("#2 Pattern of Date field is equal to '%s'", DateType.F_DATE_TIME.getPattern())
                    .isNotBlank()
                    .hasSameSizeAs(DateType.F_DATE_TIME.getPattern())
                    .matches(patternMap.get(DateType.F_DATE_TIME.getPattern()));
            assertThat(localDate)
                    .as("#3 Pattern of LocalDate field is equal to '%s'", ChronoModel.DATE_PATTERN)
                    .isNotBlank()
                    .hasSameSizeAs(ChronoModel.DATE_PATTERN)
                    .matches(patternMap.get(ChronoModel.DATE_PATTERN));
            assertThat(localTime)
                    .as("#4 Pattern of LocalTime field is equal to '%s'", ChronoModel.TIME_PATTERN)
                    .isNotBlank()
                    .hasSameSizeAs(ChronoModel.TIME_PATTERN)
                    .matches(patternMap.get(ChronoModel.TIME_PATTERN));
            assertThat(localDateTime)
                    .as("#5 Pattern of LocalDateTime field is equal to '%s'", ChronoModel.DATE_TIME_PATTERN)
                    .isNotBlank()
                    .hasSize(ChronoModel.DATE_TIME_PATTERN.length())
                    .matches(patternMap.get(ChronoModel.DATE_TIME_PATTERN));
            assertThat(zonedDateTime)
                    .as("#6 Pattern of ZonedDateTime field is equal to '%s'", ChronoModel.DATE_TIME_PATTERN)
                    .isNotBlank()
                    .hasSize(ChronoModel.DATE_TIME_PATTERN.length())
                    .matches(patternMap.get(ChronoModel.DATE_TIME_PATTERN));
            assertThat(offsetTime)
                    .as("#7 Pattern of OffsetTime field is equal to '%s'", ChronoModel.TIME_PATTERN)
                    .isNotBlank()
                    .hasSameSizeAs(ChronoModel.TIME_PATTERN)
                    .matches(patternMap.get(ChronoModel.TIME_PATTERN));
            assertThat(offsetDateTime)
                    .as("#8 Pattern of OffsetDateTime field is equal to '%s'", ChronoModel.DATE_TIME_PATTERN)
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

        // Default pattern is "yyyy-MM-dd HH:mm:ss"
        private Date date;
        @ExcelDateTimeFormat(pattern = DATE_PATTERN)
        private LocalDate localDate;
        @ExcelDateTimeFormat(pattern = TIME_PATTERN)
        private LocalTime localTime;
        @ExcelDateTimeFormat(pattern = DATE_TIME_PATTERN)
        private LocalDateTime localDateTime;
        @ExcelDateTimeFormat(pattern = DATE_TIME_PATTERN)
        private ZonedDateTime zonedDateTime;
        @ExcelDateTimeFormat(pattern = TIME_PATTERN)
        private OffsetTime offsetTime;
        @ExcelDateTimeFormat(pattern = DATE_TIME_PATTERN)
        private OffsetDateTime offsetDateTime;
    }

}
