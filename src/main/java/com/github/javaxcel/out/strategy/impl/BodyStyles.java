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

package com.github.javaxcel.out.strategy.impl;

import com.github.javaxcel.out.context.ExcelWriteContext;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.core.impl.MapWriter;
import com.github.javaxcel.out.core.impl.ModelWriter;
import com.github.javaxcel.out.strategy.AbstractExcelWriteStrategy;
import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.assertion.Asserts;

import java.util.Collections;
import java.util.List;

public class BodyStyles extends AbstractExcelWriteStrategy {

    private final List<ExcelStyleConfig> styleConfigs;

    public BodyStyles(List<ExcelStyleConfig> styleConfigs) {
        Asserts.that(styleConfigs)
                .describedAs("ExcelWriteStrategy.BodyStyle.styleConfigs is not allowed to be null or empty: {0}", styleConfigs)
                .isNotNull().isNotEmpty()
                .describedAs("ExcelWriteStrategy.BodyStyle.styleConfigs cannot have null element: {0}", styleConfigs)
                .doesNotContainNull()
                .describedAs("ExcelWriteStrategy.BodyStyle.styleConfigs must be an implementation of java.util.List: {0}", styleConfigs)
                .isInstanceOf(List.class);

        this.styleConfigs = Collections.unmodifiableList(styleConfigs);
    }

    public BodyStyles(ExcelStyleConfig styleConfig) {
        Asserts.that(styleConfig)
                .describedAs("ExcelWriteStrategy.BodyStyle.styleConfigs cannot have null element")
                .isNotNull();

        this.styleConfigs = Collections.singletonList(styleConfig);
    }

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return ModelWriter.class.isAssignableFrom(writerType) || MapWriter.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelWriteContext<?> context) {
        return this.styleConfigs;
    }

}