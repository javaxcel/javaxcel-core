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

import com.github.javaxcel.annotation.ExcelReaderExpression;
import io.github.imsejin.expression.Expression;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressiveReadingConverter implements ReadingConverter {

    private static final ExpressionParser parser = new SpelExpressionParser();

    @Nullable
    private final Map<String, Expression> cache;

    public ExpressiveReadingConverter() {
        this.cache = null;
    }

    public ExpressiveReadingConverter(@Nonnull List<Field> fields) {
        this(fields, true);
    }

    public ExpressiveReadingConverter(@Nonnull List<Field> fields, boolean enableCache) {
        this.cache = enableCache ? createCache(fields) : null;
    }

    /**
     * Creates cache of expression.
     *
     * @param fields fields of model
     * @return cache of expression
     */
    private static Map<String, Expression> createCache(List<Field> fields) {
        Map<String, Expression> cache = new HashMap<>();

        for (Field field : fields) {
            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            if (annotation == null) continue;

            Expression expression = parser.parseExpression(annotation.value());
            cache.put(field.getName(), expression);
        }

        return cache;
    }

    /**
     * {@inheritDoc}
     *
     * <p> If expressions are already parsed, uses it or parses a expression at that time.
     * This assigns the parsed value to field.
     *
     * @see ExcelReaderExpression#value()
     */
    @Override
    @Nullable
    public Object convert(Map<String, Object> variables, Field field) {
        Expression expression;
        if (this.cache == null) {
            // When this instantiated without cache.
            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            expression = parser.parseExpression(annotation.value());

        } else {
            // When this instantiated with cache.
            expression = this.cache.get(field.getName());
        }

        // To read in parallel, instantiates on each call.
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(variables);

        return expression.getValue(context, field.getType());
    }

}
