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

import com.github.javaxcel.converter.out.analysis.AbstractExcelWriteAnalysis;
import com.github.javaxcel.util.FieldUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FieldAccessExpressionExcelWriteAnalysis extends AbstractExcelWriteAnalysis {

    private Expression expression;

    private List<Field> fields;

    public FieldAccessExpressionExcelWriteAnalysis(Field field, String defaultValue) {
        super(field, defaultValue);
    }

    @Override
    public Object getValue(Object model) {
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in 'ExcelWriteExpression'.
        Map<String, Object> variables = FieldUtils.toMap(model, this.fields);
        variables.forEach(context::setVariable);

        return this.expression.getValue(context);
    }

    public void setExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression, () -> getClass().getSimpleName() + ".expression cannot be null");
    }

    public void setFields(List<Field> fields) {
        this.fields = Objects.requireNonNull(fields, () -> getClass().getSimpleName() + ".fields cannot be null");
    }

}
