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

package com.github.javaxcel.in.core;

import com.github.javaxcel.in.strategy.ExcelReadStrategy;

import java.util.List;

public interface ExcelReader<T> {

    /**
     * Sets strategies for reading Excel file.
     *
     * @param strategies Excel strategies
     * @return Excel reader
     * @throws IllegalArgumentException if strategies is null or contain null
     */
    ExcelReader<T> options(ExcelReadStrategy... strategies);

    /**
     * Returns a list after this reads the Excel file.
     *
     * @return list
     */
    List<T> read();

}
