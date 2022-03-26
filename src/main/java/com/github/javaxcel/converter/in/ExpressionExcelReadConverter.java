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

import com.github.javaxcel.annotation.ExcelReadExpression;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.expression.Expression;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionExcelReadConverter implements ExcelReadConverter {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final Map<Field, Expression> cache;

    public ExpressionExcelReadConverter() {
        this.cache = Collections.emptyMap();
    }

    public ExpressionExcelReadConverter(@Nonnull List<Field> fields) {
        this.cache = createCache(fields);
    }

    /**
     * Creates unmodifiable cache of expressions.
     *
     * @param fields fields of model
     * @return unmodifiable cache of expressions
     */
    private static Map<Field, Expression> createCache(List<Field> fields) {
        Map<Field, Expression> cache = new HashMap<>();

        for (Field field : fields) {
            ExcelReadExpression annotation = field.getAnnotation(ExcelReadExpression.class);
            if (annotation == null) continue;

            Expression expression = parser.parseExpression(annotation.value());
            cache.put(field, expression);
        }

        return Collections.unmodifiableMap(cache);
    }

    /**
     * {@inheritDoc}
     *
     * <p> If expressions are already parsed, uses it or parses an expression at that time.
     * This assigns the parsed value to field.
     *
     * @see ExcelReadExpression#value()
     */
    @Override
    @Nullable
    public Object convert(Map<String, Object> variables, Field field) {
        Expression expression;
        if (CollectionUtils.isNullOrEmpty(this.cache) || !this.cache.containsKey(field)) {
            // When this instantiated without cache.
            ExcelReadExpression annotation = field.getAnnotation(ExcelReadExpression.class);
            expression = parser.parseExpression(annotation.value());

        } else {
            // When this instantiated with cache.
            expression = this.cache.get(field);
        }

        // To read in parallel, instantiates on each call.
        // Enables to use value of the field as "#FIELD_NAME" in 'ExcelReadExpression'.
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(variables);

        return expression.getValue(context, field.getType());
    }

}
