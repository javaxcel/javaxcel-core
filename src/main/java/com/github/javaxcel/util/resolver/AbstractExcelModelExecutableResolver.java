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

package com.github.javaxcel.util.resolver;

import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import com.github.javaxcel.exception.JavaxcelException;
import com.github.javaxcel.exception.NoResolvedMethodException;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.resolver.impl.ExcelModelConstructorResolver;
import com.github.javaxcel.util.resolver.impl.ExcelModelMethodResolver;
import io.github.imsejin.common.assertion.Asserts;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public abstract class AbstractExcelModelExecutableResolver<T, E extends Executable> {

    protected final Class<T> modelType;

    private final String executableName;

    private final List<Field> fields;

    protected AbstractExcelModelExecutableResolver(Class<T> modelType, Class<E> executableType) {
        Asserts.that(modelType)
                .as("AbstractExecutableResolver.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(executableType)
                .as("AbstractExecutableResolver.executableType is not allowed to be null")
                .isNotNull();

        this.modelType = modelType;
        this.executableName = executableType.getSimpleName().toLowerCase();
        this.fields = FieldUtils.getTargetedFields(this.modelType);
    }

    public static Executable resolve(Class<?> type) {
        // Resolution of method takes precedence over constructor.
        Method method = null;
        try {
            method = new ExcelModelMethodResolver<>(type).resolve();
        } catch (JavaxcelException e) {
            // If method to be resolved doesn't exist, tries to resolve constructor.
            if (!(e instanceof NoResolvedMethodException)) throw e;
        }

        // Resolves constructor to prevent @ExcelModelCreator from being annotating
        // to two executables, even though there is a resolved method.
        Constructor<?> constructor = null;
        try {
            constructor = new ExcelModelConstructorResolver<>(type).resolve();
        } catch (JavaxcelException e) {
            // There is no executable to be resolved.
            if (method == null) throw e;
        }

        Executable executable;
        if (method == null) {
            executable = Objects.requireNonNull(constructor, "NEVER HAPPENED");
        } else {
            if (constructor != null && constructor.isAnnotationPresent(ExcelModelCreator.class)) {
                throw new RuntimeException();
            }

            executable = method;
        }

        Asserts.that(executable)
                .isNotNull()
                .returns(type, Executable::getDeclaringClass);

        return executable;
    }

    public final E resolve() {
        List<E> candidates = getCandidates();
        E candidate = elect(candidates);
        verify(candidate);

        return candidate;
    }

    /**
     * Returns the executables as candidates.
     *
     * @return candidates
     */
    protected abstract List<E> getCandidates();

    /**
     * Selects the executable as a candidate.
     *
     * @param candidates the executables
     * @return candidate
     */
    protected abstract E elect(List<E> candidates);

    protected void verify(E candidate) {
        // Does candidate have parameter?
        if (candidate.getParameterCount() == 0) return;

        List<Parameter> params = Arrays.asList(candidate.getParameters());
        List<String> mappedFieldNames = params.stream()
                .map(it -> it.getAnnotation(FieldName.class))
                .filter(Objects::nonNull).map(FieldName::value)
                .collect(toList());

        if (!mappedFieldNames.isEmpty()) {
            Map<String, Field> fieldNameMap = this.fields.stream().collect(toMap(Field::getName, Function.identity()));

            for (String fieldName : mappedFieldNames) {
                Asserts.that(fieldName)
                        .as("@{0}.value must have text, but it isn't: '{1}'", FieldName.class.getSimpleName(), fieldName)
                        .isNotNull().hasText()
                        .as("@{0}.value must match name of the targeted fields, but it isn't: (actual: '{1}', allowed: {2})",
                                FieldName.class.getSimpleName(), fieldName, fieldNameMap.keySet())
                        .predicate(fieldNameMap::containsKey)
                        .as("@{0}.value on each parameter must be unique, but it isn't: '{1}'",
                                FieldName.class.getSimpleName(), fieldName)
                        .predicate(it -> Collections.frequency(mappedFieldNames, it) == 1);
            }
        }

        Map<Class<?>, Long> fieldTypeCountMap = this.fields.stream().collect(groupingBy(Field::getType, counting()));
        Map<Class<?>, Long> paramTypeCountMap = params.stream().collect(groupingBy(Parameter::getType, counting()));

        for (Parameter param : params) {
            Class<?> paramType = param.getType();
            Long fieldTypeCount = fieldTypeCountMap.get(paramType);

            // Do types of the targeted fields contain all parameter types of the candidate?
            if (fieldTypeCount == null) {
                List<String> fieldTypeNames = fieldTypeCountMap.keySet().stream().map(Class::getName).collect(toList());
                throw new NoTargetedConstructorException("Unable to resolve parameter type[%s] of the %s[%s]; " +
                        "%s has parameter type that is not contained in types of the targeted fields%s",
                        paramType.getName(), this.executableName, candidate, this.executableName, fieldTypeNames);
            }

            Long paramTypeCount = paramTypeCountMap.get(paramType);

            // Does candidate have the known parameter types, but more than type of the targeted fields?
            if (fieldTypeCount > paramTypeCount) {
                List<String> fieldTypeNames = fieldTypeCountMap.keySet().stream().map(Class::getName).collect(toList());
                throw new NoTargetedConstructorException("Unable to resolve parameter type[%s] of the %s[%s]; " +
                        "%s has that type more than type of the targeted fields%s",
                        paramType.getName(), this.executableName, candidate, this.executableName, fieldTypeNames);
            }

            if (fieldTypeCount == 1 && paramTypeCount == 1) continue;

            FieldName annotation = param.getAnnotation(FieldName.class);
            if (annotation == null) {
                throw new NoTargetedConstructorException("Ambiguous parameter[%s] of the %s[%s] to resolve the field; " +
                        "Annotate the parameter with @%s",
                        param, this.executableName, candidate, FieldName.class.getSimpleName());
            }

            String fieldName = annotation.value();

            if (this.fields.stream().filter(it -> it.getType() == paramType && it.getName().equals(fieldName)).count() == 1) {
                continue;
            }

            throw new NoTargetedConstructorException("Not found field[%s %s] to map parameter[%s] with; " +
                    "Check if the parameter of the %s[%s] matches its type and name with that fields",
                    paramType, fieldName, param, this.executableName, candidate);
        }
    }

}
