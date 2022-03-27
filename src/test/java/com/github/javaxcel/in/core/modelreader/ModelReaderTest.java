/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.in.core.modelreader;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.Parallel;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.toy.EducationToy;
import com.monitorjbl.xlsx.StreamingReader;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class ModelReaderTest {

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelDateTimeFormat#pattern()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelDateTimeFormat")
    @SneakyThrows
    void readEducationToys(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "toys.xlsx";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<EducationToy> mocks = TestUtils.getMocks(EducationToy.class, numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, EducationToy.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = StreamingReader.builder().open(file);
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", numOfMocks);
        List<EducationToy> educationToys = TestUtils.JAVAXCEL.reader(wb, EducationToy.class).read();
        stopwatch.stop();

        // then
        assertThat(educationToys.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(educationToys)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new EducationToy[0]));
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelReadExpression
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelReadExpression")
    @SneakyThrows
    void readPeople(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "people.xlsx";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<Human> mocks = Human.newRandomList(numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, Human.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = StreamingReader.builder().open(file);
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", numOfMocks);
        List<Human> people = TestUtils.JAVAXCEL.reader(wb, Human.class).options(new Parallel()).read();
        stopwatch.stop();

        // then
        assertThat(people.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(people)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Human[0]));

    }

}
