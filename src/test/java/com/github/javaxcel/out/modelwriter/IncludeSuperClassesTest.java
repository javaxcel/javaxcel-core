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
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class IncludeSuperClassesTest {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws IOException {
        // given
        String filename = "toys.xlsx";
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup OutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new XSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = 8192;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> toys = TestUtils.getMocks(EducationToy.class, numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, toys);
        stopwatch.stop();

        // then
        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertIncludeSuperClasses(file, toys);
    }

    private void assertIncludeSuperClasses(File file, List<EducationToy> list) throws IOException {
        @Cleanup Workbook workbook = WorkbookFactory.create(file);
        List<Map<String, Object>> models = ExcelReaderFactory.create(workbook).read();

        assertThat(models.stream().mapToInt(Map::size).average().orElse(-1))
                .as("#2 The header size is the number of targeted fields in '%s'", EducationToy.class.getSimpleName())
                .isEqualTo(FieldUtils.getTargetedFields(EducationToy.class).size());
        assertThat(models.size())
                .as("#3 The number of rows is the number of list")
                .isEqualTo(list.size());
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    static class Toy {
        enum ToyType {
            CHILD, ADULT
        }

        private String name;
        private ToyType toyType;
        private Double weight;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @ExcelModel(includeSuper = true)
    static class EducationToy extends Toy {
        private int[] targetAges;
        private String goals;
        private LocalDate date;
        private LocalTime time;
        private LocalDateTime dateTime;

        EducationToy(String name, ToyType toyType, Double weight,
                     int[] targetAges, String goals, LocalDate date, LocalTime time, LocalDateTime dateTime) {
            super(name, toyType, weight);
            this.targetAges = targetAges;
            this.goals = goals;
            this.date = date;
            this.time = time;
            this.dateTime = dateTime;
        }
    }

}
