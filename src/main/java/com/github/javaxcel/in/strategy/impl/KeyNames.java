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
import com.github.javaxcel.in.core.impl.MapReader;
import com.github.javaxcel.in.strategy.AbstractExcelReadStrategy;
import io.github.imsejin.common.assertion.Asserts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyNames extends AbstractExcelReadStrategy {

    private final List<String> headerNames;

    /**
     * Changes the keys of {@link Map} with custom names.
     *
     * <p> If you import an Excel file as list of {@link Map},
     * column order is not guaranteed, unless the type of its instance is
     * {@link LinkedHashMap}. For example, the following list will be exported.
     *
     * <pre><code>
     * +-------+-------+--------+--------+--------------+----------------+---------------------+
     * | width | depth | height | weight | serialNumber | name           | apiId               |
     * +-------+-------+--------+--------+--------------+----------------+---------------------+
     * |       | 0.0   | 20.5   | 580.5  | 10000        | Choco cereal   | 2a60-4973-aec0-685e |
     * +-------+-------+--------+--------+--------------+----------------+---------------------+
     * | 10.2  | 4.0   | 6.0    | 575.0  | 10001        | Oatmeal cereal | f15d-384d-0a4b-97ec |
     * +-------+-------+--------+--------+--------------+----------------+---------------------+
     * </code></pre>
     *
     * <p> To change the column names, place the keys in the order you want like this.
     *
     * <pre>{@code
     *     List<String> newKeyNames = Arrays.asList(
     *             "WIDTH" "DEPTH", "HEIGHT", "WEIGHT", "SERIAL_NUMBER", "NAME", "API_ID");
     *
     *     Javaxcel.newInstance()
     *             .reader(new SXSSFWorkbook())
     *             .options(new KeyNames(newKeyNames))
     *             .read();
     * }</pre>
     *
     * <p> Then the column names will be changed to the order you want.
     *
     * <pre><code>
     * [
     *   {
     *     "WIDTH": null,
     *     "DEPTH": 0.0,
     *     "HEIGHT": 20.5,
     *     "WEIGHT": 580.5,
     *     "SERIAL_NUMBER": 10000,
     *     "NAME": "Choco cereal",
     *     "API_ID": "2a60-4973-aec0-685e"
     *   },
     *   {
     *     "WIDTH": 10.2,
     *     "DEPTH": 4.0,
     *     "HEIGHT": 6.0,
     *     "WEIGHT": 575.0,
     *     "SERIAL_NUMBER": 10001,
     *     "NAME": "Oatmeal cereal",
     *     "API_ID": "f15d-384d-0a4b-97ec"
     *   }
     * ]
     * </code></pre>
     *
     * @param newKeyNames header names in key order to be changed
     * @throws IllegalArgumentException if newKeyNames is invalid
     */
    public KeyNames(List<String> newKeyNames) {
        Asserts.that(newKeyNames)
                .as("ExcelReadStrategy.KeyNames.headerNames is not allowed to be null or empty: {0}", newKeyNames)
                .isNotNull().hasElement()
                .as("ExcelReadStrategy.KeyNames.headerNames cannot have null element: {0}", newKeyNames)
                .doesNotContainNull()
                .as("ExcelReadStrategy.KeyNames.headerNames must be an implementation of java.util.List: {0}", newKeyNames)
                .isInstanceOf(List.class)
                .as("ExcelReadStrategy.KeyNames.headerNames cannot have duplicated elements: {0}", newKeyNames)
                .doesNotHaveDuplicates();

        this.headerNames = Collections.unmodifiableList(newKeyNames);
    }

    @Override
    public boolean isSupported(ExcelReadContext<?> context) {
        Class<? extends ExcelReader<?>> writerType = context.getReaderType();
        return MapReader.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelReadContext<?> context) {
        return this.headerNames;
    }

}
