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
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.*;
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
import java.util.List;
import java.util.Map;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class IncludeSuperClassesTest extends ExcelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = "toys.xlsx";
        File file = new File(path.toFile(), filename);

        run(file, EducationToy.class, stopwatch);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertIncludeSuperClasses(file, givenModel.getType(), thenModel.getModels());
    }

    private void assertIncludeSuperClasses(File file, Class<?> type, List<?> list) throws IOException {
        @Cleanup Workbook workbook = WorkbookFactory.create(file);
        List<Map<String, Object>> models = ExcelReaderFactory.create(workbook).read();

        assertThat(models.stream().mapToInt(Map::size).average().orElse(-1))
                .as("#2 The header size is the number of targeted fields in '%s'", type.getSimpleName())
                .isEqualTo(FieldUtils.getTargetedFields(type).size());
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
