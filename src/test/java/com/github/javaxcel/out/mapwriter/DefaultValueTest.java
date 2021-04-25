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

package com.github.javaxcel.out.mapwriter;

import com.github.javaxcel.ExcelWriterTester;
import com.github.javaxcel.TestUtils;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
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
import java.util.*;
import java.util.stream.IntStream;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see com.github.javaxcel.out.MapWriter#defaultValue(String)
 */
@StopwatchProvider
class DefaultValueTest extends ExcelWriterTester {

    private static final String DEFAULT_VALUE = "(default)";

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        File file = new File(path.toFile(), "sample.xlsx");

        run(file, null, stopwatch);
    }

    @Override
    protected ThenModel whenCreateModels(GivenModel givenModel, WhenModel whenModel) {
        List<Map<String, Object>> models = new ArrayList<>();
        for (int i = 0; i < whenModel.getNumOfMocks(); i++) {
            Map<String, Object> model = IntStream.range(0, 10).collect(HashMap::new, (map, n) -> {
                        String key = "FIELD_" + (n + 1);
                        String randomText = TestUtils.randomize(String.class);
                        // If n is odd, empty string or else null.
                        String defaultValue = (n & 1) == 1 ? "" : null;
                        map.put(key, StringUtils.ifNullOrEmpty(randomText, defaultValue));
                    },
                    HashMap::putAll);
            models.add(model);
        }

        return new ThenModel(models);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        ExcelWriterFactory.create(whenModel.getWorkbook())
                .defaultValue(DEFAULT_VALUE)
                .write(whenModel.getOutputStream(), (List) thenModel.getModels());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();

        assertNotEmptyFile(file, "#1 Excel file must be created and have content");
        assertDefaultValue(file, (List<Map<String, Object>>) thenModel.getModels());
    }

    private void assertDefaultValue(File file, List<Map<String, Object>> models) throws IOException {
        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        List<Map<String, Object>> list = ExcelReaderFactory.create(workbook).read();

        assertThat(list.stream().map(Map::values).flatMap(Collection::stream)
                .map(Object::toString).noneMatch(StringUtils::isNullOrEmpty))
                .as("#2 There is no empty value")
                .isTrue();

        assertThat(models.stream().map(Map::values).flatMap(Collection::stream)
                .filter(s -> s == null || ((String) s).isEmpty()).count())
                .as("#3 Empty value must be converted '%s' as default value", DEFAULT_VALUE)
                .isEqualTo(list.stream().map(Map::values).flatMap(Collection::stream)
                        .filter(DEFAULT_VALUE::equals).count());
    }

}
