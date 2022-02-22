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

package com.github.javaxcel.out.core;

import com.github.javaxcel.out.strategy.ExcelWriteStrategy;

import java.io.OutputStream;
import java.util.List;

public interface ExcelWriter<T> {

    /**
     * Sets strategies for writing Excel file.
     *
     * @param strategies Excel strategies
     * @return Excel writer
     * @throws IllegalArgumentException if strategies is null or contain null
     */
    ExcelWriter<T> options(ExcelWriteStrategy... strategies);

    /**
     * Writes models in the Excel file.
     *
     * @param out  output stream for writing Excel file
     * @param list models
     */
    void write(OutputStream out, List<T> list);

}
