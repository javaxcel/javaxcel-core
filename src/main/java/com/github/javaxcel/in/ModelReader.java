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

import com.github.javaxcel.converter.in.support.InputConverterSupport;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader for model.
 *
 * @param <W> excel workbook
 * @param <T> type of model
 */
public class ModelReader<W extends Workbook, T> extends AbstractExcelReader<W, T> {

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    private final InputConverterSupport converter;

    private boolean parallel;

    /**
     * @see com.github.javaxcel.factory.ExcelReaderFactory#create(Workbook, Class)
     */
    public ModelReader(W workbook, Class<T> type) {
        super(workbook);

        Asserts.that(type)
                .as("Type is not allowed to be null")
                .isNotNull();
        this.type = type;

        // Finds targeted fields.
        this.fields = FieldUtils.getTargetedFields(this.type);
        Asserts.that(this.fields)
                .as("Cannot find the targeted fields in the class({0})", this.type.getName())
                .exception(desc -> new NoTargetedFieldException(desc, this.type))
                .hasElement();

        this.converter = new InputConverterSupport(this.fields);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ModelReader}
     */
    @Override
    public ModelReader<W, T> limit(int limit) {
        super.limit(limit);
        return this;
    }

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
     *
     * @return {@link ModelReader}
     */
    public ModelReader<W, T> parallel() {
        if (this.parallel) return this;

        this.parallel = true;
        return this;
    }

    //////////////////////////////////////// Hooks ////////////////////////////////////////

    /**
     * Reads a sheet and Adds rows of the data into list.
     *
     * @param sheet sheet to be read
     */
    @Override
    protected List<T> readSheet(Sheet sheet) {
        List<Map<String, Object>> imitations = readSheetAsMaps(sheet);
        Stream<Map<String, Object>> stream = this.parallel
                ? imitations.parallelStream() : imitations.stream();

        return stream.map(this::toRealModel).collect(toList());
    }

    @Override
    protected int getNumOfColumns(Row row) {
        return this.fields.size();
    }

    @Override
    protected String getColumnName(Cell cell, int columnIndex) {
        return this.fields.get(columnIndex).getName();
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Converts a imitated model to the real model.
     *
     * @param imitation imitated model
     * @return real model
     */
    private T toRealModel(Map<String, Object> imitation) {
        T model = FieldUtils.instantiate(this.type);

        for (Field field : this.fields) {
            Object fieldValue = this.converter.convert(imitation, field);
            FieldUtils.setFieldValue(model, field, fieldValue);
        }

        return model;
    }

}
