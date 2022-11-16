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

package com.github.javaxcel.converter.in;

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReadExpression;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExcelReadExpressionConverter implements ExcelReadConverter {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final Map<Field, Cache> analysisMap;

    public ExcelReadExpressionConverter(Iterable<ExcelAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExcelReadExpressionConverter.analyses is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelReadExpressionConverter.analyses is not allowed to be empty")
                .is(them -> them.iterator().hasNext());

        Map<Field, Cache> analysisMap = new HashMap<>();

        for (ExcelAnalysis analysis : analyses) {
            Field field = analysis.getField();

            // Makes instance of expression a cache.
            Cache cache = new Cache(analysis);
            if (analysis.hasFlag(ExcelReadAnalyzer.EXPRESSION)) {
                // DO NOT CHECK IF @ExcelReadExpression.value IS EMPTY STRING.
                // BECAUSE THE ANNOTATION HAS ONLY ONE MANDATORY ATTRIBUTE.
                // THIS CLASS IS RESPONSIBLE FOR INFORMING USER OF FAILURE OF PARSING EXPRESSION.
                ExcelReadExpression annotation = field.getAnnotation(ExcelReadExpression.class);
                cache.expression = EXPRESSION_PARSER.parseExpression(annotation.value());

                // HOWEVER, @ExcelColumn.defaultValue IS NOT MANDATORY ATTRIBUTE
                // AND ALSO THE ANNOTATION CAN BE USED FOR OTHER PURPOSES.
                // SO YOU SHOULD MAKE SURE THAT THE VALUE IS SPECIFIED EXPLICITLY.
                DefaultMeta defaultMeta = analysis.getDefaultMeta();
                String defaultExpressionString = defaultMeta.getValue();
                if (defaultMeta.getSource() == Source.COLUMN && !StringUtils.isNullOrEmpty(defaultExpressionString)) {
                    cache.expressionForDefault = EXPRESSION_PARSER.parseExpression(defaultExpressionString);
                }
            }

            analysisMap.put(field, cache);
        }

        this.analysisMap = Collections.unmodifiableMap(analysisMap);
    }

    @Override
    public boolean supports(Field field) {
        ExcelAnalysis analysis = this.analysisMap.get(field).analysis;
        return analysis.hasFlag(ExcelReadAnalyzer.EXPRESSION);
    }

    /**
     * {@inheritDoc}
     *
     * @see ExcelReadExpression#value()
     * @see ExcelColumn#defaultValue()
     */
    @Nullable
    @Override
    public Object convert(Map<String, String> variables, Field field) {
        // To read in parallel, instantiates on each call of this method.
        // Don't set root object to prevent user from assigning value
        // to the field of model with the way we don't intend.
        EvaluationContext context = new StandardEvaluationContext();

        // Enables to use value of the field as "#FIELD_NAME" in @ExcelReadExpression.
        variables.forEach(context::setVariable);

        Cache cache = this.analysisMap.get(field);
        Object value = cache.expression.getValue(context, field.getType());
        value = isNullOrEmpty(value) ? null : value;

        // Returns default value if the value is null or empty string.
        if (value == null && cache.expressionForDefault != null) {
            // There is no access to fields(variables) on default expression.
            Object defaultValue = cache.expressionForDefault.getValue(field.getType());

            // Returns null if the default value is also null or empty string.
            if (isNullOrEmpty(defaultValue)) {
                return null;
            }

            return defaultValue;
        }

        return value;
    }

    // -------------------------------------------------------------------------------------------------

    private static boolean isNullOrEmpty(@Nullable Object object) {
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
        @Nullable
        private Expression expression;
        @Nullable
        private Expression expressionForDefault;

        private Cache(ExcelAnalysis analysis) {
            this.analysis = analysis;
        }
    }

}
