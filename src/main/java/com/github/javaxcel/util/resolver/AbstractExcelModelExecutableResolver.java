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
import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException;
import com.github.javaxcel.exception.InvalidExcelModelCreatorException;
import com.github.javaxcel.exception.JavaxcelException;
import com.github.javaxcel.exception.NoResolvedExcelModelCreatorException;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.resolver.ExcelModelExecutableParameterNameResolver.ResolvedParameter;
import com.github.javaxcel.util.resolver.impl.ExcelModelConstructorResolver;
import com.github.javaxcel.util.resolver.impl.ExcelModelMethodResolver;
import io.github.imsejin.common.assertion.Asserts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
        this.fields = Collections.unmodifiableList(FieldUtils.getTargetedFields(this.modelType));
    }

    public static Executable resolve(Class<?> type) {
        // Resolution of method takes precedence over constructor.
        Method method = null;
        try {
            method = new ExcelModelMethodResolver<>(type).resolve();
        } catch (JavaxcelException e) {
            // If method to be resolved doesn't exist, tries to resolve constructor.
            if (!(e instanceof NoResolvedExcelModelCreatorException)) throw e;
        }

        // Resolves constructor to prevent @ExcelModelCreator from being annotated
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
            // Method and constructor are annotated with @ExcelModelCreator.
            if (constructor != null && constructor.isAnnotationPresent(ExcelModelCreator.class)) {
                throw new AmbiguousExcelModelCreatorException("Ambiguous method[%s] and constructor[%s] to resolve; " +
                        "Remove one of the annotations[@ExcelModelCreator] from the method and constructor",
                        method, constructor);
            }

            // Resolution of method takes precedence over constructor.
            executable = method;
        }

        Asserts.that(executable)
                .isNotNull()
                .returns(type, Executable::getDeclaringClass);

        return executable;
    }

    // -------------------------------------------------------------------------------------------------

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
        // Does candidate have no parameter?
        if (candidate.getParameterCount() == 0) return;

        List<ResolvedParameter> resolvedParams = new ExcelModelExecutableParameterNameResolver(candidate).resolve();
        List<String> paramNames = resolvedParams.stream().map(ResolvedParameter::getName).collect(toList());

        Map<String, Field> fieldNameMap = this.fields.stream().collect(toMap(Field::getName, Function.identity()));
        Map<Class<?>, Long> fieldTypeCountMap = this.fields.stream().collect(groupingBy(Field::getType, counting()));
        Map<Class<?>, Long> paramTypeCountMap = resolvedParams.stream().collect(groupingBy(ResolvedParameter::getType, counting()));

        for (ResolvedParameter resolvedParam : resolvedParams) {
            String paramName = resolvedParam.getName();
            Class<?> paramType = resolvedParam.getType();
            Long fieldTypeCount = fieldTypeCountMap.get(paramType);

            // Do types of the targeted fields contain all parameter types of the candidate?
            if (fieldTypeCount == null) {
                List<String> fieldTypeNames = fieldTypeCountMap.keySet().stream().map(Class::getName).collect(toList());
                throw new InvalidExcelModelCreatorException("Unable to resolve parameter type[%s] of the %s[%s]; " +
                        "%s has parameter type that is not contained in types of the targeted fields%s",
                        paramType.getName(), this.executableName, candidate, this.executableName, fieldTypeNames);
            }

            // Both names of parameter and field are different,
            // but their type is unique, so the parameter can be resolved.
            Long paramTypeCount = paramTypeCountMap.get(paramType);
            if (fieldTypeCount == 1 && paramTypeCount == 1) continue;

            Asserts.that(paramName)
                    .exception(InvalidExcelModelCreatorException::new)
                    .as("ResolvedParameter.name must have text, but it isn't: '{0}'", paramName)
                    .isNotNull().hasText()
                    .as("ResolvedParameter.name must match name of the targeted fields, but it isn't: (actual: '{0}', allowed: {1})",
                            paramName, fieldNameMap.keySet())
                    .predicate(fieldNameMap::containsKey)
                    .as("Each ResolvedParameter.name must be unique, but it isn't: (duplicated: '{0}', names: {1})", paramName, paramNames)
                    .predicate(it -> Collections.frequency(paramNames, it) == 1);

            if (this.fields.stream().filter(it -> it.getType() == paramType && it.getName().equals(paramName)).count() != 1) {
                throw new InvalidExcelModelCreatorException("Not found field[%s %s] to map parameter[%s] with; " +
                        "Check if the parameter of the %s[%s] matches its type and name with that fields",
                        paramType, paramName, resolvedParam, this.executableName, candidate);
            }
        }
    }

}
