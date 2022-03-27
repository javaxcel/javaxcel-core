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

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.in.core.ModelReaderTester;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelColumn#defaultValue()
 */
@StopwatchProvider
class DefaultValueTest extends ModelReaderTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<Product> type = Product.class;
        String filename = type.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = path.resolve(filename).toFile();

        run(file, type, stopwatch);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        List<Product> products = (List<Product>) thenModel.getModels();
        List<Product> mocks = whenModel.getMocks();

        assertThat(products)
                .as("#1 The number of loaded models is %,d", mocks.size())
                .hasSameSizeAs(mocks)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactlyElementsOf(mocks)
                .as("#3 Each loaded model has default value")
                .flatMap(it -> Arrays.asList(it.getDates()))
                .isNotNull()
                .doesNotContainNull()
                .containsOnly(
                        LocalDate.of(1999, 1, 31),
                        LocalDate.of(2009, 7, 31),
                        LocalDate.of(2019, 12, 31));
    }

}
