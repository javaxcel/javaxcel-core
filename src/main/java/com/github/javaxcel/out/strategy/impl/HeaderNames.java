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
import com.github.javaxcel.out.core.impl.ModelWriter;
import com.github.javaxcel.out.strategy.AbstractExcelWriteStrategy;
import io.github.imsejin.common.assertion.Asserts;

import java.util.Collections;
import java.util.List;

public class HeaderNames extends AbstractExcelWriteStrategy {

    private final List<String> values;

    public HeaderNames(List<String> values) {
        Asserts.that(values)
                .describedAs("ExcelWriteStrategy.HeaderNames.values is not allowed to be null or empty: {0}", values)
                .isNotNull().isNotEmpty()
                .describedAs("ExcelWriteStrategy.HeaderNames.values cannot have null element: {0}", values)
                .doesNotContainNull()
                .describedAs("ExcelWriteStrategy.HeaderNames.values must be an implementation of java.util.List: {0}", values)
                .isInstanceOf(List.class)
                .describedAs("ExcelWriteStrategy.HeaderNames.values cannot have duplicated elements: {0}", values)
                .doesNotHaveDuplicates();

        this.values = Collections.unmodifiableList(values);
    }

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return ModelWriter.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelWriteContext<?> context) {
        return this.values;
    }

}
