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

package com.github.javaxcel.converter.out.analysis.impl;

import com.github.javaxcel.converter.out.analysis.ExcelWriteColumnAnalysis;
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

public class GetterAccessExpressionExcelWriteColumnAnalysis implements ExcelWriteColumnAnalysis {

    private final Expression expression;

    private final String defaultValue;

    private final Map<String, Method> getterMap;

    public GetterAccessExpressionExcelWriteColumnAnalysis(Expression expression, List<Field> fields, String defaultValue) {
        this.expression = expression;
        this.defaultValue = defaultValue;

        Map<String, Method> getterMap = new HashMap<>();
        for (Field field : fields) {
            Method getter = FieldUtils.resolveGetter(field);
            getterMap.put(field.getName(), getter);
        }

        this.getterMap = Collections.unmodifiableMap(getterMap);
    }

    @Override
    public Object getValue(Object model) {
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in 'ExcelWriteExpression'.
        Map<String, Object> variables = convertAsMap(model);
        variables.forEach(context::setVariable);

        return this.expression.getValue(context);
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    // -------------------------------------------------------------------------------------------------

    private Map<String, Object> convertAsMap(Object model) {
        Map<String, Object> variables = new HashMap<>();

        for (Entry<String, Method> entry : this.getterMap.entrySet()) {
            String fieldName = entry.getKey();
            Method getter = entry.getValue();

            Object value = ReflectionUtils.invoke(getter, model);

            variables.put(fieldName, value);
        }

        return variables;
    }

}
