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

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.in.ExcelReadConverter;
import com.github.javaxcel.converter.in.support.ExcelReadConverters;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.in.context.ExcelReadContext;
import com.github.javaxcel.in.core.AbstractExcelReader;
import com.github.javaxcel.in.strategy.ExcelReadStrategy;
import com.github.javaxcel.in.strategy.impl.Parallel;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.in.processor.ExcelModelCreationProcessor;
import com.github.javaxcel.in.resolver.AbstractExcelModelExecutableResolver;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Excel reader for model.
 *
 * @param <T> type of model
 */
public class ModelReader<T> extends AbstractExcelReader<T> {

    /**
     * The fields of the type that will is actually read from Excel file.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    private final ExcelTypeHandlerRegistry registry;

    private final ExcelModelCreationProcessor<T> modelProcessor;

    private ExcelReadConverter converter;

    /**
     * Creates a reader for model.
     *
     * @param workbook  Excel workbook
     * @param modelType type of Excel model
     */
    public ModelReader(Workbook workbook, Class<T> modelType, ExcelTypeHandlerRegistry registry) {
        super(workbook, modelType);

        // Finds the targeted fields.
        List<Field> fields = FieldUtils.getTargetedFields(modelType);
        Asserts.that(fields)
                .describedAs("ModelReader.fields cannot find the targeted fields in the class: {0}", modelType.getName())
                .thrownBy(desc -> new NoTargetedFieldException(modelType, desc))
                .isNotEmpty()
                .describedAs("ModelReader.fields cannot have null element: {0}", fields)
                .doesNotContainNull();

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the fields that are not accessible. (ExcelReadStrategy.Parallel)
        fields.stream().filter(it -> !it.isAccessible()).forEach(it -> it.setAccessible(true));
        this.fields = Collections.unmodifiableList(fields);

        Asserts.that(registry)
                .describedAs("ModelReader.registry is not allowed to be null")
                .isNotNull();
        this.registry = registry;

        Executable executable = AbstractExcelModelExecutableResolver.resolve(modelType);
        this.modelProcessor = new ExcelModelCreationProcessor<>(modelType, this.fields, executable);
    }

    @Override
    public void prepare(ExcelReadContext<T> context) {
        // Analyzes the fields with arguments.
        ExcelReadAnalyzer analyzer = new ExcelReadAnalyzer(this.registry);
        Collection<ExcelReadStrategy> strategies = context.getStrategyMap().values();
        List<ExcelAnalysis> analyses = analyzer.analyze(this.fields, strategies.toArray());

        // Creates a converter.
        this.converter = new ExcelReadConverters(analyses, registry);

        // ExcelModelCreationProcessor needs the analyses.
        this.modelProcessor.setAnalyses(analyses);
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
    private T toActualModel(Map<String, String> variables) {
        // Creates a mock model for actual model.
        Map<String, Object> mock = new HashMap<>();
        for (Field field : this.fields) {
            String key = field.getName();
            Object value = this.converter.convert(variables, field);

            mock.put(key, value);
        }

        return this.modelProcessor.createModel(mock);
    }

}
