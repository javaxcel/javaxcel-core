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

package com.github.javaxcel.analysis.out.impl;

import com.github.javaxcel.analysis.AbstractExcelAnalysis;
import com.github.javaxcel.analysis.out.ExcelWriteAnalysis;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.util.ReflectionUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public final class GetterAccessExpressionExcelWriteAnalysis extends AbstractExcelAnalysis implements ExcelWriteAnalysis {

    private Expression expression;

    private Map<Field, Method> getterMap;

    public GetterAccessExpressionExcelWriteAnalysis(Field field, String defaultValue) {
        super(field, defaultValue);
    }

    @Override
    public Object getValue(Object model) {
        // We don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in 'ExcelWriteExpression'.
        Map<String, Object> variables = convertAsMap(model);
        variables.forEach(context::setVariable);

        return this.expression.getValue(context);
    }

    public void setExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression, () -> getClass().getSimpleName() + ".expression cannot be null");
    }

    public void setGetters(List<Field> fields) {
        Objects.requireNonNull(fields, () -> getClass().getSimpleName() + ".fields cannot be null");

        Map<Field, Method> getterMap = new HashMap<>();
        for (Field field : fields) {
            Method getter;
            try {
                getter = FieldUtils.resolveGetter(field);
            } catch (RuntimeException ignored) {
                getter = null;
            }

            getterMap.put(field, getter);
        }

        this.getterMap = Collections.unmodifiableMap(getterMap);
    }

    // -------------------------------------------------------------------------------------------------

    private Map<String, Object> convertAsMap(Object model) {
        Map<String, Object> variables = new HashMap<>();

        for (Entry<Field, Method> entry : this.getterMap.entrySet()) {
            Field field = entry.getKey();
            Method getter = entry.getValue();

            Object value;
            if (getter == null) {
                value = ReflectionUtils.getFieldValue(model, field);
            } else {
                value = ReflectionUtils.invoke(getter, model);
            }

            variables.put(field.getName(), value);
        }

        return variables;
    }

}
