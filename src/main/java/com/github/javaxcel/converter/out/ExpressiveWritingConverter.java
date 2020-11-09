package com.github.javaxcel.converter.out;

import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.expression.Expression;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressiveWritingConverter<T> extends DefaultValueStore implements WritingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private final List<Field> fields;

    private final Map<String, Expression> cache;

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
    public ExpressiveWritingConverter(List<Field> fields) {
        Map<String, Expression> cache = new HashMap<>();

        for (Field field : fields) {
            ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
            if (annotation == null) continue;

            Expression expression = parser.parseExpression(annotation.value());
            cache.put(field.getName(), expression);
        }

        this.fields = fields;
        this.cache = cache;
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
        Map<String, Object> variables = FieldUtils.toMap(model, this.fields);

        this.context.setRootObject(model);
        this.context.setVariables(variables);

        Expression expression = this.cache.get(field.getName());
        String result = expression.getValue(this.context, String.class);

        return FieldUtils.convertIfFaulty(result, this.defaultValue, field);
    }

}
