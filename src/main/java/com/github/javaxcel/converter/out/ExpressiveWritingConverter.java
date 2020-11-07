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

public class ExpressiveWritingConverter<T> extends AbstractWritingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private final Map<String, Expression> caches;

    /**
     * Replacement for field value when it is null or empty.
     */
    private String defaultValue;

    private Map<String, Object> variables;

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
        Map<String, Expression> caches = new HashMap<>();

        for (Field field : fields) {
            ExcelWriterExpression writerExpression = field.getAnnotation(ExcelWriterExpression.class);
            if (writerExpression == null) continue;

            Expression expression = parser.parseExpression(writerExpression.value());
            caches.put(field.getName(), expression);
        }

        this.caches = caches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets up the variables.
     *
     * @param variables {@link Map} in which key is the model's field name and value is the model's field value
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
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
        context.setRootObject(model);
        context.setVariables(this.variables);

        String result = this.caches.get(field.getName()).getValue(context, String.class);

        return FieldUtils.convertIfFaulty(result, this.defaultValue, field);
    }

}
