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
import com.github.javaxcel.out.strategy.AbstractExcelWriteStrategy;
import io.github.imsejin.common.assertion.Asserts;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyNames extends AbstractExcelWriteStrategy {

    private final Map<String, Object> keyMap;

    /**
     * Rearranges the keys of {@link Map} with custom order.
     *
     * <p> If you export list of {@link Map} as an Excel file,
     * column order is not guaranteed, unless the type of its instance is
     * {@link LinkedHashMap}. For example, the following list will be exported.
     *
     * <pre><code>
     * [
     *   {
     *     "serialNumber": 10000,
     *     "name": "Choco cereal",
     *     "apiId": "2a60-4973-aec0-685e",
     *     "width": null,
     *     "depth": 0.0,
     *     "height": 20.5,
     *     "weight": 580.5
     *   },
     *   {
     *     "serialNumber": 10001,
     *     "name": "Oatmeal cereal",
     *     "apiId": "f15d-384d-0a4b-97ec",
     *     "width": 10.2,
     *     "depth": 4.0,
     *     "height": 6.0,
     *     "weight": 575.0
     *   }
     * ]
     * </code></pre>
     *
     * <p> To rearrange the column order, place the keys in the order you want like this.
     *
     * <pre>{@code
     *     List<String> keyOrders = Arrays.asList(
     *             "width" "depth", "height", "weight", "serialNumber", "name", "apiId");
     *
     *     Javaxcel.newInstance()
     *             .writer(new SXSSFWorkbook())
     *             .options(new KeyNames(keyOrders))
     *             .write(new FileOutputStream(file), list);
     * }</pre>
     *
     * <p> Then the columns will be arranged in the order you want.
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
     * @param keyOrders keys ordered as you want
     * @throws IllegalArgumentException if keyOrders is invalid
     */
    public KeyNames(List<String> keyOrders) {
        Asserts.that(keyOrders)
                .as("keyOrders is not allowed to be null or empty: {0}", keyOrders)
                .isNotNull().hasElement()
                .as("keyOrders cannot have null element: {0}", keyOrders)
                .doesNotContainNull()
                .as("keyOrders must be an implementation of java.util.List: {0}", keyOrders)
                .isInstanceOf(List.class)
                .as("keyOrders cannot have duplicated elements: {0}", keyOrders)
                .doesNotHaveDuplicates();

        // Creates a map of which key is element in keys and value is column index.
        Map<String, Integer> orders = new HashMap<>();
        for (int i = 0; i < keyOrders.size(); i++) {
            orders.put(keyOrders.get(i), i);
        }

        this.keyMap = Collections.singletonMap("orders", Collections.unmodifiableMap(orders));
    }

    /**
     * Rearranges the keys of {@link Map} with custom order and sets header names.
     *
     * <p> If you export list of {@link Map} as an Excel file,
     * column order is not guaranteed, unless the type of its instance is
     * {@link LinkedHashMap}. For example, the following list will be exported.
     *
     * <pre><code>
     * [
     *   {
     *     "serialNumber": 10000,
     *     "name": "Choco cereal",
     *     "apiId": "2a60-4973-aec0-685e",
     *     "width": null,
     *     "depth": 0.0,
     *     "height": 20.5,
     *     "weight": 580.5
     *   },
     *   {
     *     "serialNumber": 10001,
     *     "name": "Oatmeal cereal",
     *     "apiId": "f15d-384d-0a4b-97ec",
     *     "width": 10.2,
     *     "depth": 4.0,
     *     "height": 6.0,
     *     "weight": 575.0
     *   }
     * ]
     * </code></pre>
     *
     * <p> To rearrange the column order, place the keys in the order you want
     * to the first argument. To change the header names, place the names
     * you want them changed to in the custom order to the second argument like this.
     *
     * <pre>{@code
     *     List<String> keyOrders = Arrays.asList(
     *             "width" "depth", "height", "weight", "serialNumber", "name", "apiId");
     *     List<String> newKeyNames = Arrays.asList(
     *             "WIDTH" "DEPTH", "HEIGHT", "WEIGHT", "SERIAL_NUMBER", "NAME", "API_ID");
     *
     *     Javaxcel.newInstance()
     *             .writer(new SXSSFWorkbook())
     *             .options(new KeyNames(keyOrders, newKeyNames))
     *             .write(new FileOutputStream(file), list);
     * }</pre>
     *
     * <p> Then the column order and the names will be changed you want.
     *
     * <pre><code>
     * +-------+-------+--------+--------+---------------+----------------+---------------------+
     * | WIDTH | DEPTH | HEIGHT | WEIGHT | SERIAL_NUMBER | NAME           | API_ID              |
     * +-------+-------+--------+--------+---------------+----------------+---------------------+
     * |       | 0.0   | 20.5   | 580.5  | 10000         | Choco cereal   | 2a60-4973-aec0-685e |
     * +-------+-------+--------+--------+---------------+----------------+---------------------+
     * | 10.2  | 4.0   | 6.0    | 575.0  | 10001         | Oatmeal cereal | f15d-384d-0a4b-97ec |
     * +-------+-------+--------+--------+---------------+----------------+---------------------+
     * </code></pre>
     *
     * @param keyOrders   keys ordered as you want
     * @param newKeyNames header names in key order to be changed
     * @throws IllegalArgumentException if keyOrders is invalid
     * @throws IllegalArgumentException if newKeyNames is invalid
     */
    public KeyNames(List<String> keyOrders, List<String> newKeyNames) {
        Asserts.that(keyOrders)
                .as("keyOrders is not allowed to be null or empty: {0}", keyOrders)
                .isNotNull().hasElement()
                .as("keyOrders cannot have null element: {0}", keyOrders)
                .doesNotContainNull()
                .as("keyOrders must be an implementation of java.util.List: {0}", keyOrders)
                .isInstanceOf(List.class)
                .as("keyOrders cannot have duplicated elements: {0}", keyOrders)
                .doesNotHaveDuplicates();

        Asserts.that(newKeyNames)
                .as("newKeyNames is not allowed to be null or empty: {0}", newKeyNames)
                .isNotNull().hasElement()
                .as("newKeyNames.size is not equal to keyOrders.size (newKeyNames.size: {0}, keyOrders.size: {1})",
                        newKeyNames.size(), keyOrders.size())
                .isSameSize(keyOrders)
                .as("newKeyNames cannot have null element: {0}", newKeyNames)
                .doesNotContainNull()
                .as("newKeyNames must be an implementation of java.util.List: {0}", newKeyNames)
                .isInstanceOf(List.class)
                .as("newKeyNames cannot have duplicated elements: {0}", newKeyNames)
                .doesNotHaveDuplicates();

        // Creates a map of which key is element in keys and value is column index.
        Map<String, Object> keyMap = new HashMap<>();
        Map<String, Integer> orders = new HashMap<>();
        for (int i = 0; i < keyOrders.size(); i++) {
            orders.put(keyOrders.get(i), i);
        }

        keyMap.put("orders", Collections.unmodifiableMap(orders));
        keyMap.put("names", Collections.unmodifiableList(newKeyNames));

        this.keyMap = Collections.unmodifiableMap(keyMap);
    }

    @Override
    public boolean isSupported(ExcelWriteContext<?> context) {
        Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
        return MapWriter.class.isAssignableFrom(writerType);
    }

    @Override
    public Object execute(ExcelWriteContext<?> context) {
        return this.keyMap;
    }

}
