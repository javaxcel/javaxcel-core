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

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.converter.in.BasicReadingConverter;
import com.github.javaxcel.converter.in.ExpressiveReadingConverter;
import com.github.javaxcel.converter.in.ReadingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
public final class ModelReader<W extends Workbook, T> extends AbstractExcelReader<W, T> {

    private final ReadingConverter basicConverter = new BasicReadingConverter();

    private final ReadingConverter expConverter;

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    private boolean parallel;

    private ModelReader(W workbook, Class<T> type) {
        super(workbook);

        this.type = type;
        this.fields = FieldUtils.getTargetedFields(type);

        if (this.fields.isEmpty()) throw new NoTargetedFieldException(this.type);

        this.expConverter = new ExpressiveReadingConverter(this.fields);
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
     * Makes the conversion from simulated model into real model parallel.
     *
     * <p> We recommend processing in parallel only when
     * dealing with large data. The following table is a benchmark.
     *
     * <pre>{@code
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
     * }</pre>
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
        List<Map<String, Object>> simulatedModels = getSimulatedModels(sheet);
        Stream<Map<String, Object>> stream = this.parallel
                ? simulatedModels.parallelStream() : simulatedModels.stream();

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
     * Gets simulated models from a sheet.
     *
     * @param sheet excel sheet
     * @return simulated models
     */
    private List<Map<String, Object>> getSimulatedModels(Sheet sheet) {
        int numOfRows = ExcelUtils.getNumOfModels(sheet);
        if (this.limit >= 0) numOfRows = Math.min(this.limit, numOfRows);

        // Reads rows.
        List<Map<String, Object>> simulatedModels = new ArrayList<>();
        for (int i = 0; i < numOfRows; i++) {
            if (this.numOfRowsRead == this.limit) break;

            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            // Adds a row data of the sheet.
            simulatedModels.add(readRow(row));
        }

        return simulatedModels;
    }

    /**
     * Converts a simulated model to the real model.
     *
     * @param sModel simulated model
     * @return real model
     */
    private T toRealModel(Map<String, Object> sModel) {
        T model = FieldUtils.instantiate(this.type);

        for (Field field : this.fields) {
            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            Object fieldValue;

            if (annotation == null) {
                // When the field is not annotated with @ExcelReaderExpression.
                fieldValue = this.basicConverter.convert(sModel, field);
            } else {
                // When the field is annotated with @ExcelReaderExpression.
                fieldValue = this.expConverter.convert(sModel, field);
            }

            FieldUtils.setFieldValue(model, field, fieldValue);
        }

        return model;
    }

}
