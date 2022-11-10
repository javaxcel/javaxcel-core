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

package com.github.javaxcel.in.processor;

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.converter.in.ExcelReadConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.resolver.ExcelModelExecutableParameterNameResolver;
import com.github.javaxcel.util.resolver.ExcelModelExecutableParameterNameResolver.ResolvedParameter;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.ReflectionUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Processor for creation of Excel model
 *
 * @param <T> type of model
 */
public class ExcelModelCreationProcessor<T> {

    private final List<Field> fields;

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

    private final List<ResolvedParameter> resolvedParameters;

    private List<ExcelAnalysis> analyses;

    private Map<Field, Method> setterMap;

    /**
     * Instantiates a new processor.
     *
     * @param modelType  type of model
     * @param fields     targeted fields of model
     * @param executable creator of model
     */
    public ExcelModelCreationProcessor(Class<T> modelType, List<Field> fields, Executable executable) {
        Asserts.that(modelType)
                .describedAs("ExcelModelCreationProcessor.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(fields)
                .describedAs("ExcelModelCreationProcessor.fields cannot find the targeted fields in the class: {0}", modelType.getName())
                .thrownBy(desc -> new NoTargetedFieldException(modelType, desc))
                .isNotEmpty()
                .describedAs("ExcelModelCreationProcessor.fields cannot have null element: {0}", fields)
                .doesNotContainNull()
                .describedAs("ExcelModelCreationProcessor.fields are declared on model class, but it doesn't: (modelType: {0}, fields: {1})", modelType.getName(), fields)
                .allMatch(field -> field.getDeclaringClass().isAssignableFrom(modelType));

        this.fields = fields;

        Asserts.that(executable)
                .isNotNull()
                .returns(modelType, Executable::getDeclaringClass);

        // To prevent exception from occurring on multi-threaded environment,
        // Permits access to the executable that is not accessible. (ExcelReadStrategy.Parallel)
        if (!executable.isAccessible()) {
            executable.setAccessible(true);
        }

        this.executable = executable;

        // Parameters are already validated on AbstractExcelModelExecutableResolver.
        this.resolvedParameters = new ExcelModelExecutableParameterNameResolver(executable).resolve();
    }

    public void setAnalyses(List<ExcelAnalysis> analyses) {
        Map<Field, Method> setterMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            if (analysis.hasFlag(ExcelReadAnalyzer.SETTER)) {
                Field field = analysis.getField();
                Method setter = FieldUtils.resolveSetter(field);

                setterMap.put(field, setter);
            }
        }

        this.analyses = analyses;
        this.setterMap = Collections.unmodifiableMap(setterMap);
    }

    /**
     * Creates a Excel model.
     *
     * <p> The parameter has a entry which has the key as {@link Field#getName()}
     * and the value from {@link ExcelReadConverter}.
     *
     * @param mock mock of the model
     * @return model
     */
    @SuppressWarnings("unchecked")
    public T createModel(Map<String, Object> mock) {
        Object[] arguments = resolveInitialArguments(mock);

        // Instantiates the actual model through the cached ExcelModelCreator.
        T model = (T) ReflectionUtils.execute(this.executable, null, arguments);

        List<String> paramNames = this.resolvedParameters.stream().map(ResolvedParameter::getName).collect(toList());

        for (int i = 0; i < this.fields.size(); i++) {
            Field field = this.fields.get(i);

            // To prevent ModelReader from changing value of final field by reflection API.
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            // Skips over conversion of field already injected as parameter of the executable.
            if (paramNames.contains(field.getName())) {
                continue;
            }

            Object value = mock.get(field.getName());

            if (!CollectionUtils.isNullOrEmpty(this.analyses)) {
                ExcelAnalysis analysis = this.analyses.get(i);

                // Binds the argument through setter for the field.
                if (analysis.hasFlag(ExcelReadAnalyzer.SETTER)) {
                    Method setter = this.setterMap.get(field);
                    ReflectionUtils.invoke(setter, model, value);
                    continue;
                }
            }

            // Without setter, binds the argument to the field directly.
            ReflectionUtils.setFieldValue(model, field, value);
        }

        return model;
    }

    /**
     * Maps a mock model to initial arguments for {@link ExcelModelCreator @ExcelModelCreator}.
     *
     * @param mock mock model
     */
    private Object[] resolveInitialArguments(Map<String, Object> mock) {
        Object[] arguments = new Object[this.resolvedParameters.size()];

        for (int i = 0; i < arguments.length; i++) {
            ResolvedParameter resolvedParam = this.resolvedParameters.get(i);
            String paramName = resolvedParam.getName();

            if (mock.containsKey(paramName)) {
                arguments[i] = mock.get(paramName);
                continue;
            }

            // It is able to map argument to this parameter
            // when its type is unique on the fields that model class has
            // even though both parameter name and field type is not matched.
            Field field = this.fields.stream().filter(it -> it.getType() == resolvedParam.getType()).findFirst().get();
            arguments[i] = mock.get(field.getName());
        }

        return arguments;
    }

}
