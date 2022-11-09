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
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    private final Map<Field, Cache> analysisMap;

    public ExcelWriteExpressionConverter(Iterable<ExcelAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExcelWriteExpressionConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteExpressionConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());

        List<Field> fields = new ArrayList<>();
        Map<Field, Method> getterMap = new HashMap<>();
        Map<Field, Cache> analysisMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            // Makes instance of expression a cache.
            Cache cache = new Cache(analysis);
            if (analysis.hasFlag(ExcelWriteAnalyzer.EXPRESSION)) {
                // DO NOT CHECK IF @ExcelWriteExpression.value IS EMPTY STRING.
                // BECAUSE THE ANNOTATION HAS ONLY ONE MANDATORY ATTRIBUTE.
                // THIS CLASS IS RESPONSIBLE FOR INFORMING USER OF FAILURE OF PARSING EXPRESSION.
                ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);
                cache.expression = EXPRESSION_PARSER.parseExpression(annotation.value());

                // HOWEVER, @ExcelColumn.defaultValue IS NOT MANDATORY ATTRIBUTE
                // AND ALSO THE ANNOTATION CAN BE USED FOR OTHER PURPOSES.
                // SO YOU SHOULD MAKE SURE THAT THE VALUE IS SPECIFIED EXPLICITLY.
                DefaultMeta defaultMeta = analysis.getDefaultMeta();
                String defaultValue = defaultMeta.getValue();
                if (defaultMeta.getSource() == Source.COLUMN && !StringUtils.isNullOrEmpty(defaultValue)) {
                    cache.expressionForDefault = EXPRESSION_PARSER.parseExpression(defaultValue);
                }
            }

            // Makes getter a cache.
            if (analysis.hasFlag(ExcelWriteAnalyzer.GETTER)) {
                Method getter = FieldUtils.resolveGetter(field);
                getterMap.put(field, getter);
            }

            fields.add(field);
            analysisMap.put(field, cache);
        }

        this.fields = Collections.unmodifiableList(fields);
        this.getterMap = Collections.unmodifiableMap(getterMap);
        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field).analysis;
        return analysis.hasFlag(ExcelWriteAnalyzer.EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    @Null
    @Override
    public String convert(Object model, Field field) {
        // Don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in @ExcelWriteExpression.
        Map<String, Object> variables = getVariablesOf(model, field);
        variables.forEach(context::setVariable);

        Cache cache = this.analysisMap.get(field);
        Object value = cache.expression.getValue(context);
        value = isNullOrEmpty(value) ? null : value;

        // Checks if the value is null or empty string.
        if (value == null) {
            // Returns null if default expression is not defined.
            if (cache.expressionForDefault == null) {
                return null;
            }

            // There is no access to fields(variables) on default expression.
            Object defaultValue = cache.expressionForDefault.getValue();

            // Returns null if the default value is also null or empty string.
            if (isNullOrEmpty(defaultValue)) {
                return null;
            }

            // Forces the evaluated value to be a string
            // even if desired return type of expression is not String.
            return defaultValue.toString();
        }

        // Forces the evaluated value to be a string
        // even if desired return type of expression is not String.
        return value.toString();
    }

    // -------------------------------------------------------------------------------------------------

    private Map<String, Object> getVariablesOf(Object model, Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field).analysis;

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

    private static boolean isNullOrEmpty(@Null Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
        }

        return false;
    }

    private static class Cache {
        private final ExcelAnalysis analysis;
        @Null
        private Expression expression;
        @Null
        private Expression expressionForDefault;

        private Cache(ExcelAnalysis analysis) {
            this.analysis = analysis;
        }
    }

}
