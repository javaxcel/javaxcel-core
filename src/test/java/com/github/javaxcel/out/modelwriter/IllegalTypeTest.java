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

import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.ModelWriter;
import io.github.imsejin.common.tool.Stopwatch;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@StopwatchProvider(TimeUnit.MILLISECONDS)
class IllegalTypeTest {

    @Test
    @DisplayName("When creates ModelWriter inserting null into type argument")
    void test(Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", XSSFWorkbook.class.getSimpleName());
        Workbook workbook = new XSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("create '%s' instance without type", ModelWriter.class.getSimpleName());
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, null))
                .as("Throws IllegalArgumentException")
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
