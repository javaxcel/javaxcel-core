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

import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.in.support.ExcelReadConverterSupport;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.in.context.ExcelReadContext;
import com.github.javaxcel.in.core.AbstractExcelReader;
import com.github.javaxcel.in.strategy.impl.Parallel;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.resolver.AbstractExcelModelExecutableResolver;
import com.github.javaxcel.util.resolver.ExcelModelExecutableParameterNameResolver;
import com.github.javaxcel.util.resolver.ExcelModelExecutableParameterNameResolver.ResolvedParameter;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader for model.
 *
 * @param <T> model type
 */
public class ModelReader<T> extends AbstractExcelReader<T> {

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

    private final ExcelModelExecutableParameterNameResolver paramNameResolver;

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

        Executable executable = AbstractExcelModelExecutableResolver.resolve(type);

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the executable that is not accessible. (ExcelReadStrategy.Parallel)
        if (!executable.isAccessible()) {
            executable.setAccessible(true);
        }

        this.executable = executable;
        this.paramNameResolver = new ExcelModelExecutableParameterNameResolver(executable);

        // Finds the targeted fields.
        List<Field> fields = FieldUtils.getTargetedFields(type);
        Asserts.that(fields)
                .describedAs("ModelReader.fields cannot find the targeted fields in the class: {0}", type.getName())
                .thrownBy(desc -> new NoTargetedFieldException(type, desc))
                .isNotEmpty()
                .describedAs("ModelReader.fields cannot have null element: {0}", fields)
                .doesNotContainNull();

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the fields that are not accessible. (ExcelReadStrategy.Parallel)
        fields.stream().filter(it -> !it.isAccessible()).forEach(it -> it.setAccessible(true));
        this.fields = Collections.unmodifiableList(fields);

        this.converter = new ExcelReadConverterSupport(this.fields, registry);
    }

    @Override
    protected List<String> readHeader(ExcelReadContext<T> context) {
        // To convert Map to the actual model, ignores @ExcelColumn.name().
        return FieldUtils.toHeaderNames(this.fields, true);
    }

    @Override
    protected List<T> readBody(ExcelReadContext<T> context) {
        List<Map<String, String>> maps = super.readBodyAsMaps(context.getSheet());

        if (context.getStrategyMap().containsKey(Parallel.class)) {
            return maps.parallelStream().map(this::toActualModel).collect(toList());
        } else {
            // Makes sure not to grow length of internal array.
            List<T> models = new ArrayList<>(maps.size());

            for (Map<String, String> map : maps) {
                T model = toActualModel(map);
                models.add(model);
            }

            return models;
        }
    }

    /**
     * Converts an imitated model to the real model.
     *
     * @param variables variables
     * @return real model
     */
    @SuppressWarnings("unchecked")
    private T toActualModel(Map<String, String> variables) {
        // Creates a mock model for actual model.
        Map<String, Object> mock = new HashMap<>();
        for (Field field : this.fields) {
            String key = field.getName();
            Object value = this.converter.convert(variables, field);

            mock.put(key, value);
        }

        List<ResolvedParameter> resolvedParams = this.paramNameResolver.resolve();
        List<String> paramNames = resolvedParams.stream().map(ResolvedParameter::getName).collect(toList());

        // Maps a mock model to initial arguments for @ExcelModelCreator.
        Object[] arguments = new Object[resolvedParams.size()];
        for (int i = 0; i < resolvedParams.size(); i++) {
            ResolvedParameter resolvedParam = resolvedParams.get(i);
            String paramName = resolvedParam.getName();

            if (mock.containsKey(paramName)) {
                arguments[i] = mock.get(paramName);
                continue;
            }

            // Both names of parameter and field are different,
            // but their type is unique, so the parameter can be resolved.
            Field field = this.fields.stream().filter(it -> it.getType() == resolvedParam.getType()).findFirst().get();
            arguments[i] = mock.get(field.getName());
        }

        // Instantiates the actual model through the cached ExcelModelCreator.
        T model = (T) ReflectionUtils.execute(this.executable, null, arguments);

        for (Field field : this.fields) {
            // To prevent ModelReader from changing value of final field by reflection API.
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            // Skips over conversion of field assigned by parameters of the executable.
            if (paramNames.contains(field.getName())) {
                continue;
            }

            Object value = mock.get(field.getName());
            ReflectionUtils.setFieldValue(model, field, value);
        }

        return model;
    }

}
