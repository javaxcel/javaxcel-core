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
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.MapWriterTester;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy.DefaultValue;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.StringUtils;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see DefaultValue
 */
@StopwatchProvider
class DefaultValueTest extends MapWriterTester {

    private static final String DEFAULT_VALUE = "(default)";

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        File file = new File(path.toFile(), "sample.xlsx");

        run(file, stopwatch);
    }

    @Override
    protected ThenModel whenCreateModels(GivenModel givenModel, WhenModel whenModel) {
        List<Map<String, Object>> models = TestUtils.getRandomMaps(whenModel.getNumOfMocks(), 10);
        return new ThenModel(models);
    }

    @Override
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        TestUtils.JAVAXCEL.writer(whenModel.getWorkbook())
                .options(new DefaultValue(DEFAULT_VALUE))
                .write(whenModel.getOutputStream(), thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertDefaultValue(file, thenModel.getModels());
    }

    private static void assertDefaultValue(File file, List<Map<String, Object>> models) throws IOException {
        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        List<Map<String, Object>> written = TestUtils.JAVAXCEL.reader(workbook).read();

        assertThat(written.stream().map(Map::values).flatMap(Collection::stream)
                .map(Object::toString).noneMatch(StringUtils::isNullOrEmpty))
                .as("#2 There is no empty value")
                .isTrue();

        assertThat(models.stream().map(Map::values).flatMap(Collection::stream)
                .filter(s -> s == null || (s instanceof String && ((String) s).isEmpty())).count())
                .as("#3 Empty value must be converted '%s' as default value", DEFAULT_VALUE)
                .isEqualTo(written.stream().map(Map::values).flatMap(Collection::stream)
                        .filter(DEFAULT_VALUE::equals).count());
    }

}
