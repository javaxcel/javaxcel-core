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
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.util.WorkbookUtil;

public class SheetName extends AbstractExcelWriteStrategy {

    private final String value;

    /**
     * Strategy for sheet name.
     *
     * @param value sheet name
     */
    public SheetName(String value) {
        Asserts.that(value)
                .describedAs("ExcelWriteStrategy.SheetName.value is not allowed to be null or blank, but it is: {0}", value)
                .isNotNull().hasText()
                .describedAs("ExcelWriteStrategy.SheetName.value is not allowed to contain invalid character: {0}", value)
                .is(it -> WorkbookUtil.createSafeSheetName(it).equals(it));

        this.value = value;
    }

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return ModelWriter.class.isAssignableFrom(writerType) || MapWriter.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelWriteContext<?> context) {
        return this.value;
    }

}
