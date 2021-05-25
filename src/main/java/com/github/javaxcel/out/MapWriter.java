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

package com.github.javaxcel.out;

import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.usermodel.*;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Excel writer for {@link Map}.
 *
 * @param <W> excel workbook
 * @param <T> {@link Map}
 */
public class MapWriter<W extends Workbook, T extends Map<String, ?>> extends AbstractExcelWriter<W, T> {

    /**
     * @see #beforeWrite(OutputStream, List)
     */
    private final List<String> keys = new ArrayList<>();

    /**
     * Map of which key is the key and value is index number.
     *
     * @see #headerNames(List, List)
     * @see #beforeWrite(OutputStream, List)
     */
    private Map<String, Integer> indexedMap;

    private String defaultValue;

    /**
     * @see com.github.javaxcel.factory.ExcelWriterFactory#create(Workbook)
     */
    public MapWriter(W workbook) {
        super(workbook);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> sheetName(String sheetName) {
        super.sheetName(sheetName);
        return this;
    }

    /**
     * Rearranges the keys of {@link Map} with custom order.
     *
     * <p> If you export list of {@link Map} as a excel file,
     * column order is not guaranteed, unless the type of its instance is {@link LinkedHashMap}.
     * For example, the following list will be exported.
     *
     * <pre><code>
     * [
     *     {
     *         "serialNumber": 10000,
     *         "name": "Choco cereal",
     *         "apiId": "2a60-4973-aec0-685e",
     *         "width": null,
     *         "depth": 0.0,
     *         "height": 20.5,
     *         "weight": 580.5
     *     },
     *     {
     *         "serialNumber": 10001,
     *         "name": "Oatmeal cereal",
     *         "apiId": "f15d-384d-0a4b-97ec",
     *         "width": 10.2,
     *         "depth": 4.0,
     *         "height": 6.0,
     *         "weight": 575.0
     *     }
     * ]
     * </code></pre>
     *
     * <p> To rearrange the column order, place the keys in the order you want like this.
     *
     * <pre><code>
     *     List<String> orderedKeys = Arrays.asList(
     *             "width" "depth", "height", "weight", "serialNumber", "name", "apiId");
     *
     *     ExcelWriterFactory.create(new SXSSFWorkbook())
     *             .headerNames(orderedKeys)
     *             .write(new FileOutputStream(file), list);
     * </code></pre>
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
     * @param orderedKeys keys ordered as you want
     * @return {@link MapWriter}
     * @throws IllegalArgumentException if ordered keys is null or empty
     */
    @Override
    public MapWriter<W, T> headerNames(List<String> orderedKeys) {
        return headerNames(orderedKeys, null);
    }

    /**
     * Rearranges the keys of {@link Map} with custom order and sets header names.
     *
     * <p> If you export list of {@link Map} as a excel file,
     * column order is not guaranteed, unless the type of its instance is {@link LinkedHashMap}.
     * For example, the following list will be exported.
     *
     * <pre><code>
     * [
     *     {
     *         "serialNumber": 10000,
     *         "name": "Choco cereal",
     *         "apiId": "2a60-4973-aec0-685e",
     *         "width": null,
     *         "depth": 0.0,
     *         "height": 20.5,
     *         "weight": 580.5
     *     },
     *     {
     *         "serialNumber": 10001,
     *         "name": "Oatmeal cereal",
     *         "apiId": "f15d-384d-0a4b-97ec",
     *         "width": 10.2,
     *         "depth": 4.0,
     *         "height": 6.0,
     *         "weight": 575.0
     *     }
     * ]
     * </code></pre>
     *
     * <p> To rearrange the column order, place the keys in the order you want
     * to the first argument. To change the header names, place the names
     * you want them changed to in the custom order to the second argument like this.
     *
     * <pre><code>
     *     List<String> orderedKeys = Arrays.asList(
     *             "width" "depth", "height", "weight", "serialNumber", "name", "apiId");
     *     List<String> name = Arrays.asList(
     *             "WIDTH" "DEPTH", "HEIGHT", "WEIGHT", "SERIAL_NUMBER", "NAME", "API_ID");
     *
     *     ExcelWriterFactory.create(new SXSSFWorkbook())
     *             .headerNames(orderedKeys, name)
     *             .write(new FileOutputStream(file), list);
     * </code></pre>
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
     * @param orderedKeys keys ordered as you want
     * @param headerNames header names in key order
     * @return {@link MapWriter}
     * @throws IllegalArgumentException if ordered keys is null or empty
     * @throws IllegalArgumentException if num of ordered keys is not equal to num of header names
     */
    public MapWriter<W, T> headerNames(List<String> orderedKeys, @Nullable List<String> headerNames) {
        Asserts.that(orderedKeys)
                .as("Ordered keys is now allowed to be null or empty")
                .hasElement();

        if (headerNames != null) {
            // Validates ordered keys and header names.
            Asserts.that(orderedKeys)
                    .as("The number of ordered keys is not equal to the number of header names")
                    .isSameSize(headerNames);

            super.headerNames(headerNames);
        }

        // Creates indexed map for rearrange.
        Map<String, Integer> indexedMap = new HashMap<>();
        for (int i = 0; i < orderedKeys.size(); i++) {
            indexedMap.put(orderedKeys.get(i), i);
        }

        this.indexedMap = indexedMap;

        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> unrotate() {
        super.unrotate();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> filter() {
        super.filter();
        return this;
    }

    ///////////////////////////////////// Decoration //////////////////////////////////////

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> headerStyle(ExcelStyleConfig config) {
        return headerStyles(config);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> headerStyles(ExcelStyleConfig... configs) {
        super.headerStyles(configs);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> bodyStyle(ExcelStyleConfig config) {
        return bodyStyles(config);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> bodyStyles(ExcelStyleConfig... configs) {
        super.bodyStyles(configs);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> autoResizeColumns() {
        super.autoResizeColumns();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> hideExtraRows() {
        super.hideExtraRows();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link MapWriter}
     */
    @Override
    public MapWriter<W, T> hideExtraColumns() {
        super.hideExtraColumns();
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * {@inheritDoc}
     *
     * <p> Unlike {@link ModelWriter}, {@link MapWriter} cannot validate header names,
     * header styles and body styles before {@link #write(OutputStream, List)} is invoked
     * because the only way to get {@link Map} exists in that method. So it does using this hook.
     */
    @Override
    protected void beforeWrite(OutputStream out, List<T> list) {
        // To write a header, this doesn't allow to accept empty list of maps.
        Asserts.that(list)
                .as("List of maps is not allowed to be empty")
                .hasElement();

        // Gets all the maps' keys.
        this.keys.addAll(list.stream().flatMap(it -> it.keySet().stream()).distinct().collect(toList()));

        if (this.indexedMap != null) {
            // Invalidates the number of ordered keys and their each element.
            Set<String> orderedKeys = this.indexedMap.keySet();

            Asserts.that(this.keys)
                    .as("Ordered keys are at variance with maps' keys\nmaps' keys: {0}\nordered keys: {1}",
                            this.keys, orderedKeys)
                    .isSameSize(orderedKeys)
                    .containsAll(orderedKeys);

            // Rearranges the keys as you want: it changes order of columns.
            this.keys.sort(comparing(this.indexedMap::get));
        }

        final int numOfKeys = this.keys.size();
        Predicate<CellStyle[]> validator = them -> them.length == 1 || them.length == numOfKeys;

        // Validates the number of header styles.
        if (this.headerStyles != null) {
            Asserts.that(validator.test(this.headerStyles))
                    .as("Number of header styles({0}) must be 1 or equal to number of maps' keys({1})",
                            this.headerStyles.length, numOfKeys)
                    .isTrue();
        }

        // Validates the number of body styles.
        if (this.bodyStyles != null) {
            Asserts.that(validator.test(this.bodyStyles))
                    .as("Number of body styles({0}) must be 1 or equal to number of maps' keys({1})",
                            this.bodyStyles.length, numOfKeys)
                    .isTrue();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p> If the header names are not set through {@link #headerNames(List, List)},
     * this method brings the values from {@link Map#keySet()}.
     *
     * @see Map#keySet()
     */
    @Override
    protected void ifHeaderNamesAreEmpty(List<String> headerNames) {
        headerNames.addAll(this.keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeToSheet(Sheet sheet, List<T> list) {
        final int numOfMaps = list.size();
        final int numOfKeys = this.keys.size();

        for (int i = 0; i < numOfMaps; i++) {
            T map = list.get(i);

            // Skips the first row that is header.
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < numOfKeys; j++) {
                Object value = map.get(this.keys.get(j));
                Cell cell = row.createCell(j);

                // Not allows empty string to be written.
                if (value != null && !"".equals(value)) {
                    cell.setCellValue(value.toString());
                } else if (this.defaultValue != null) {
                    cell.setCellValue(this.defaultValue);
                }

                if (this.bodyStyles == null) continue;

                // Sets styles to body's cell.
                CellStyle bodyStyle = this.bodyStyles.length == 1
                        ? this.bodyStyles[0] : this.bodyStyles[j];

                // There is possibility that 'bodyStyles' has null elements, if you set 'NoStyleConfig'.
                if (bodyStyle != null) cell.setCellStyle(bodyStyle);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumOfColumns() {
        return this.keys.size();
    }

}
