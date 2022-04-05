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

package com.github.javaxcel.in.core.impl;

import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.in.support.ExcelReadConverterSupport;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.in.context.ExcelReadContext;
import com.github.javaxcel.in.core.AbstractExcelReader;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.Parallel;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.resolver.AbstractExcelModelExecutableResolver;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader for model.
 *
 * @param <T> model type
 */
public class ModelReader<T> extends AbstractExcelReader<T> {

    private final Class<T> type;

    /**
     * For cache
     *
     * <p> The following table is a benchmark with 100,000 rows.
     *
     * <pre>
     *     +-----+-----------+-----------+
     *     | try | cached    | no cache  |
     *     +-----+-----------+-----------+
     *     | 1   | 3.809887s | 3.978322s |
     *     +-----+-----------+-----------+
     *     | 2   | 2.732647s | 2.751642s |
     *     +-----+-----------+-----------+
     *     | 3   | 2.661144s | 2.760901s |
     *     +-----+-----------+-----------+
     *     | 4   | 2.745241s | 2.801942s |
     *     +-----+-----------+-----------+
     *     | 5   | 2.526665s | 2.596132s |
     *     +-----+-----------+-----------+
     *     | 6   | 2.713802s | 2.622913s |
     *     +-----+-----------+-----------+
     *     | 7   | 2.505731s | 2.594136s |
     *     +-----+-----------+-----------+
     *     | 8   | 2.51782s  | 2.634646s |
     *     +-----+-----------+-----------+
     *     | 9   | 2.506474s | 2.703751s |
     *     +-----+-----------+-----------+
     *     | 10  | 2.532037s | 2.604461s |
     *     +-----+-----------+-----------+
     * </pre>
     *
     * <p> Each average is 2.725144s and 2.804884s. More efficient about 2.8%.
     */
    private final Executable executable;

    /**
     * The fields of the type that will is actually read from Excel file.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    private final ExcelReadConverterSupport converter;

    /**
     * Creates a reader for model.
     *
     * @param workbook Excel workbook
     * @param type     type of model
     */
    public ModelReader(Workbook workbook, Class<T> type, ExcelTypeHandlerRegistry registry) {
        super(workbook, type);
        this.type = type;

        Executable executable = AbstractExcelModelExecutableResolver.resolve(type);

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the executable that is not accessible. (ExcelReadStrategy.Parallel)
        if (!executable.isAccessible()) executable.setAccessible(true);
        this.executable = executable;

        // Finds the targeted fields.
        List<Field> fields = FieldUtils.getTargetedFields(type);
        Asserts.that(fields)
                .as("ModelReader.fields cannot find the targeted fields in the class: {0}", type.getName())
                .exception(desc -> new NoTargetedFieldException(type, desc))
                .hasElement()
                .as("ModelReader.fields cannot have null element: {0}", fields)
                .doesNotContainNull();

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the fields that are not accessible. (ExcelReadStrategy.Parallel)
        fields.stream().filter(it -> !it.isAccessible()).forEach(it -> it.setAccessible(true));
        this.fields = fields;

        this.converter = new ExcelReadConverterSupport(this.fields, registry);
    }

    @Override
    protected List<String> readHeader(ExcelReadContext<T> context) {
        return FieldUtils.toHeaderNames(this.fields, true);
    }

    @Override
    protected List<T> readBody(ExcelReadContext<T> context) {
        List<Map<String, Object>> maps = super.readBodyAsMaps(context.getSheet());

        Stream<Map<String, Object>> stream = context.getStrategyMap().containsKey(Parallel.class)
                ? maps.parallelStream() : maps.stream();

        return stream.map(this::toActualModel).collect(toList());
    }

    /**
     * Converts an imitated model to the real model.
     *
     * @param variables variables
     * @return real model
     */
    @SuppressWarnings("unchecked")
    private T toActualModel(Map<String, Object> variables) {
        // Creates a mock model for actual model.
        Map<String, Object> mock = this.fields.stream().collect(HashMap::new,
                (map, it) -> map.put(it.getName(), this.converter.convert(variables, it)), Map::putAll);

        List<String> paramNames = Arrays.stream(this.executable.getParameters())
                .map(it -> it.getAnnotation(FieldName.class))
                .filter(Objects::nonNull).map(FieldName::value)
                .collect(toList());

        // Instantiates the actual model.
        Object[] arguments = paramNames.stream().map(mock::get).toArray();
        T model = (T) ReflectionUtils.execute(this.executable, null, arguments);

        for (Field field : this.fields) {
            // To prevent ModelReader from changing value of final field by reflection API.
            if (Modifier.isFinal(field.getModifiers())) continue;
            // Skips over conversion of field assigned by parameters of the executable.
            if (paramNames.contains(field.getName())) continue;

            Object fieldValue = this.converter.convert(variables, field);
            ReflectionUtils.setFieldValue(model, field, fieldValue);
        }

        return model;
    }

}
