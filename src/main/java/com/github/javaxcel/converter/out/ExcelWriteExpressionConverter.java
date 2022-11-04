/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.converter.out;

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;
import jakarta.validation.constraints.Null;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExcelWriteExpressionConverter implements ExcelWriteConverter {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final List<Field> fields;

    private final Map<Field, Method> getterMap;

    private final Map<Field, Entry<ExcelAnalysis, Expression>> analysisMap;

    public ExcelWriteExpressionConverter(List<ExcelAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExcelWriteExpressionConverter.analyses is not allowed to be null")
                .isNotNull();

        List<Field> fields = new ArrayList<>();
        Map<Field, Method> getterMap = new HashMap<>();
        Map<Field, Entry<ExcelAnalysis, Expression>> analysisMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
                Method getter = FieldUtils.resolveGetter(field);
                getterMap.put(field, getter);
            }

            ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);
            Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());

            fields.add(field);
            analysisMap.put(field, new SimpleEntry<>(analysis, expression));
        }

        this.fields = Collections.unmodifiableList(fields);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field).getKey();
        return analysis.hasFlag(ExcelWriteAnalyzer.EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    @Null
    @Override
    public String convert(Object model, Field field) {
        // We don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in 'ExcelWriteExpression'.
        Map<String, Object> variables = getVariablesOf(model, field);
        variables.forEach(context::setVariable);

        Expression expression = this.analysisMap.get(field).getValue();
        Object value = expression.getValue(context);

        // Returns default value if the value is null.
        if (value == null) {
            ExcelAnalysis analysis = this.analysisMap.get(field).getKey();
            return analysis.getDefaultValue();
        }

        return value.toString();
    }

    // -------------------------------------------------------------------------------------------------

    private Map<String, Object> getVariablesOf(Object model, Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field).getKey();

        if (analysis.hasFlag(ExcelWriteAnalyzer.FIELD_ACCESS)) {
            return FieldUtils.toMap(model, this.fields);

        } else if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
            Map<String, Object> variables = new HashMap<>();
            for (Entry<Field, Method> entry : this.getterMap.entrySet()) {
                Method getter = entry.getValue();
                Object value = ReflectionUtils.invoke(getter, model);

                variables.put(entry.getKey().getName(), value);
            }

            return variables;

        } else {
            throw new RuntimeException("Never throw; ExcelWriteAnalyzer adds the flags into each analysis");
        }
    }

}
