/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.in;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelReader<W extends Workbook, T> {

    /**
     * Limits the number of models.
     *
     * @param limit limit for the number of models
     * @return {@link ExcelReader}
     */
    ExcelReader<W, T> limit(int limit);

    /**
     * Returns a list after this reads the excel file.
     *
     * @return list
     */
    List<T> read();

}
