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

package com.github.javaxcel.in.strategy.impl;

import com.github.javaxcel.in.context.ExcelReadContext;
import com.github.javaxcel.in.core.ExcelReader;
import com.github.javaxcel.in.core.impl.ModelReader;
import com.github.javaxcel.in.strategy.AbstractExcelReadStrategy;

/**
 * Makes the conversion from imitated model into real model parallel.
 *
 * <p> We recommend processing in parallel only when
 * dealing with large data. The following table is a benchmark.
 *
 * <pre><code>
 *     +------------+------------+----------+
 *     | row \ type | sequential | parallel |
 *     +------------+------------+----------+
 *     | 10,000     | 16s        | 13s      |
 *     +------------+------------+----------+
 *     | 25,000     | 31s        | 21s      |
 *     +------------+------------+----------+
 *     | 100,000    | 2m 7s      | 1m 31s   |
 *     +------------+------------+----------+
 *     | 150,000    | 3m 28s     | 2m 1s    |
 *     +------------+------------+----------+
 * </code></pre>
 */
public class Parallel extends AbstractExcelReadStrategy {

    @Override
    public boolean isSupported(ExcelReadContext<?> context) {
        Class<? extends ExcelReader<?>> writerType = context.getReaderType();
        return ModelReader.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelReadContext<?> context) {
        throw new UnsupportedOperationException("ExcelReadStrategy." + getClass().getSimpleName() + " is not supported");
    }

}
