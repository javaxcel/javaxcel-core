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

import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.in.core.ModelReaderTester;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelModel#includeSuper()
 * @see ExcelReadExpression
 */
@StopwatchProvider
class ExpressionTest extends ModelReaderTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<Human> type = Human.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = path.resolve(filename).toFile();

        run(file, type, stopwatch, 4096);
    }

    @Override
    protected WhenModel givenCreateMocks(GivenModel givenModel) {
        List<Human> mocks = Human.newRandomList(givenModel.getNumOfMocks());
        return new WhenModel(mocks);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        // given
        List<Human> toys = (List<Human>) thenModel.getModels();
        List<Human> mocks = whenModel.getMocks();
        UUID defaultPlaceOfBirth = new UUID(0, 0);

        // expect
        assertThat(toys)
                .as("#1 The number of loaded models is %,d", mocks.size())
                .hasSameSizeAs(mocks)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactlyElementsOf(mocks);
        assertThat(mocks)
                .filteredOn(mock -> mock.getPlaceOfBirth() == null)
                .hasSizeLessThanOrEqualTo((int) toys.stream().filter(toy -> toy.getPlaceOfBirth().equals(defaultPlaceOfBirth)).count());
    }

}
