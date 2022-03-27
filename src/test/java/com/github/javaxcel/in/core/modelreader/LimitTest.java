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
import com.github.javaxcel.in.core.ModelReaderTester;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.Limit;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.computer.Computer;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class LimitTest extends ModelReaderTester {

    private static final int FETCH_SIZE = 100;

    @Test
    void test0(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<Computer> type = Computer.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = path.resolve(filename).toFile();

        run(file, type, stopwatch);
    }

    @Test
    void test1(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<Computer> type = Computer.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = path.resolve(filename).toFile();

        run(file, type, stopwatch, FETCH_SIZE / 2);
    }

    @Override
    protected ThenModel whenReadModels(GivenModel givenModel, WhenModel whenModel) {
        List<?> models = TestUtils.JAVAXCEL.reader(whenModel.getWorkbook(), givenModel.getType())
                .options(new Limit(FETCH_SIZE)).read();
        return new ThenModel(models);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        List<Computer> computers = (List<Computer>) thenModel.getModels();
        List<Computer> mocks = whenModel.getMocks();
        int fetchSize = Math.min(FETCH_SIZE, computers.size());

        assertThat(computers.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(fetchSize);
        assertThat(computers)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.stream().limit(fetchSize).toArray(Computer[]::new));
    }

}
