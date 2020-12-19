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

import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.util.FieldUtils;
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

public class ExpressiveWritingConverter<T> extends DefaultValueStore implements WritingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    @Nullable
    private final List<Field> fields;

    @Nullable
    private final Map<String, Expression> cache;

    public ExpressiveWritingConverter() {
        this.fields = null;
        this.cache = null;
    }

    /**
     * Brings default values for each field and caches them and
     * parses expressions for each field and caches them, too.
     *
     * <p> The following table is a benchmark.
     *
     * <pre>{@code
     *     +--------------+--------+--------+
     *     | row \ cached | true   | false  |
     *     +--------------+--------+--------+
     *     | 10,000       | 2s     | 5s     |
     *     +--------------+--------+--------+
     *     | 65,535       | 7s     | 25s    |
     *     +--------------+--------+--------+
     *     | 100,000      | 10s    | 40s    |
     *     +--------------+--------+--------+
     *     | 300,000      | 27s    | 1m 58s |
     *     +--------------+--------+--------+
     *     | 500,000      | 43s    | 3m 18s |
     *     +--------------+--------+--------+
     *     | 1,048,574    | 1m 28s | 6m 35s |
     *     +--------------+--------+--------+
     * }</pre>
     *
     * @param fields fields of model
     */
    public ExpressiveWritingConverter(@Nonnull List<Field> fields) {
        this.fields = fields;
        this.cache = createCache(fields);
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
            ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
            if (annotation == null) continue;

            Expression expression = parser.parseExpression(annotation.value());
            cache.put(field.getName(), expression);
        }

        return cache;
    }

    /**
     * {@inheritDoc}
     *
     * <p> Parses a expression to be written as cell value.
     *
     * @param model element in list
     * @param field field of model
     * @return computed string
     * @see ExcelWriterExpression#value()
     */
    @Override
    public String convert(T model, Field field) {
        Map<String, Object> variables;
        Expression expression;

        if (this.fields == null || this.cache == null) {
            // When this instantiated by constructor without argument.
            List<Field> fields = FieldUtils.getTargetedFields(model.getClass());
            ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
            expression = parser.parseExpression(annotation.value());
            variables = FieldUtils.toMap(model, fields);

        } else {
            // When this instantiated by constructor with fields.
            expression = this.cache.get(field.getName());
            variables = FieldUtils.toMap(model, this.fields);
        }

        this.context.setVariables(variables);

        String result = expression.getValue(this.context, String.class);

        return FieldUtils.convertIfFaulty(result, this.defaultValue, field);
    }

}
