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

package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelModelConstructor;
import com.github.javaxcel.annotation.ExcelModelConstructor.FieldName;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;
import io.github.imsejin.common.assertion.Asserts;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

/**
 * Utilities for reflection on {@link Constructor}.
 */
public final class ConstructorUtils {

    @ExcludeFromGeneratedJacocoReport
    private ConstructorUtils() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    public static <T> Constructor<T> getTargetedConstructor(Class<T> type) {
        Constructor<T> constructor = resolveConstructor(type);

        // Validates parameters of the constructor.
        ensure(constructor);

        return constructor;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> resolveConstructor(Class<T> type) {
        List<Constructor<?>> constructors = Arrays.stream(type.getDeclaredConstructors())
                .sorted(comparing(Constructor::getModifiers, new ModifierComparator())).collect(toList());

        if (constructors.size() > 1) {
            List<Constructor<T>> resolved = new ArrayList<>();
            for (Constructor<?> c : constructors) {
                ExcelModelConstructor annotation = c.getAnnotation(ExcelModelConstructor.class);
                if (annotation == null) continue;

                resolved.add((Constructor<T>) c);
            }

            if (resolved.isEmpty()) {
                // There are two or more constructors that aren't annotated with that.
                throw new NoTargetedConstructorException("Ambiguous constructors%s to resolve; " +
                        "Annotate constructor you want with @%s",
                        constructors.stream().map(ConstructorUtils::toSimpleString).collect(toList()),
                        ExcelModelConstructor.class.getSimpleName());

            } else if (resolved.size() > 1) {
                // There are two or more constructors that are annotated with that.
                throw new NoTargetedConstructorException("Ambiguous constructors%s to resolve; " +
                        "Remove @%s from other constructors except the one",
                        resolved.stream().map(ConstructorUtils::toSimpleString).collect(toList()),
                        ExcelModelConstructor.class.getSimpleName());
            }
        }

        return (Constructor<T>) constructors.get(0);
    }

    private static void ensure(Constructor<?> constructor) {
        // Does constructor have parameter?
        if (constructor.getParameterCount() == 0) return;

        List<Field> fields = FieldUtils.getTargetedFields(constructor.getDeclaringClass());
        List<Parameter> params = Arrays.asList(constructor.getParameters());

        List<String> resolvedFieldNames = params.stream().map(it -> it.getAnnotation(FieldName.class))
                .filter(Objects::nonNull).map(FieldName::value).collect(toList());
        if (!resolvedFieldNames.isEmpty()) {
            Map<String, Field> fieldNameMap = fields.stream().collect(toMap(Field::getName, Function.identity()));

            for (String fieldName : resolvedFieldNames) {
                Asserts.that(fieldName)
                        .as("@{0}.value must have text, but it isn't: '{1}'", FieldName.class.getSimpleName(), fieldName)
                        .isNotNull().hasText()
                        .as("@{0}.value must match name of the targeted fields, but it isn't: (actual: '{1}', allowed: {2})",
                                FieldName.class.getSimpleName(), fieldName, fieldNameMap.keySet())
                        .predicate(fieldNameMap::containsKey);
            }
        }

        Map<Class<?>, Long> fieldTypeCountMap = fields.stream().collect(groupingBy(Field::getType, counting()));
        Map<Class<?>, Long> paramTypeCountMap = params.stream().collect(groupingBy(Parameter::getType, counting()));

        for (Parameter param : params) {
            Class<?> paramType = param.getType();
            Long fieldTypeCount = fieldTypeCountMap.get(paramType);

            // Do types of the targeted fields contain all parameter types of the constructor?
            if (fieldTypeCount == null) {
                List<String> fieldTypeNames = fieldTypeCountMap.keySet().stream().map(Class::getSimpleName).collect(toList());
                throw new NoTargetedConstructorException("Unable to resolve parameter type[%s] of the constructor[%s]; " +
                        "constructor has parameter type that is not contained in types of the targeted fields%s",
                        paramType.getSimpleName(), toSimpleString(constructor), fieldTypeNames);
            }

            Long paramTypeCount = paramTypeCountMap.get(paramType);

            // Does constructor have the known parameter types, but more than type of the targeted fields?
            if (fieldTypeCount < paramTypeCount) {
                List<String> fieldTypeNames = fieldTypeCountMap.keySet().stream().map(Class::getSimpleName).collect(toList());
                throw new NoTargetedConstructorException("Unable to resolve parameter type[%s] of the constructor[%s]; " +
                        "constructor has that type more than type of the targeted fields%s",
                        paramType.getSimpleName(), toSimpleString(constructor), fieldTypeNames);
            }

            if (fieldTypeCount > paramTypeCount) {
                FieldName annotation = param.getAnnotation(FieldName.class);

                //
                if (annotation == null) {
                    throw new NoTargetedConstructorException("Ambiguous parameter type[%s] of the constructor[%s] to resolve; " +
                            "Annotate the parameter with @%s",
                            paramType.getSimpleName(), toSimpleString(constructor), FieldName.class.getSimpleName());
                }
            }
        }
    }

    public static String toSimpleString(@Nullable Constructor<?> constructor) {
        if (constructor == null) return "null";

        String modifier = "";
        switch (constructor.getModifiers()) {
            case Modifier.PUBLIC:
                modifier = "public ";
                break;
            case Modifier.PROTECTED:
                modifier = "protected ";
                break;
            case Modifier.PRIVATE:
                modifier = "private ";
                break;
        }

        String typeName = constructor.getDeclaringClass().getSimpleName();
        String paramTypes = Arrays.stream(constructor.getParameterTypes())
                .map(Class::getSimpleName).collect(joining(", "));

        return modifier + typeName + '(' + paramTypes + ')';
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    public static class ModifierComparator implements Comparator<Integer> {
        private static int convert(Integer modifier) {
            switch (modifier) {
                case Modifier.PUBLIC:
                    return 0;
                case Modifier.PROTECTED:
                    return 1;
                case 0:
                    return 2;
                case Modifier.PRIVATE:
                    return 3;
                default:
                    throw new IllegalArgumentException("Illegal modifier: " + modifier);
            }
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(convert(o1), convert(o2));
        }
    }

}
