package com.github.javaxcel.converter.in;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import io.github.imsejin.expression.Expression;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressiveReadingConverter extends AbstractReadingConverter {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private Map<String, Object> variables;

    /**
     * Creates cache of expression.
     *
     * @param fields fields of model
     * @return cache of expression
     */
    public static Map<String, Expression> createCache(List<Field> fields) {
        Map<String, Expression> cache = new HashMap<>();

        for (Field field : fields) {
            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            if (annotation == null) continue;

            String fieldName = field.getName();
            Expression expression = parser.parseExpression(annotation.value());
            cache.put(fieldName, expression);
        }

        return cache;
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
     * <p> Parses a expression to be assigned as field value.
     */
    @Override
    public Object convert(String value, Field field) {
        ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
        context.setVariables(this.variables);

        return parser.parseExpression(annotation.value()).getValue(context, field.getType());
    }

    /**
     * Converts cached expression into field value.
     *
     * @param value      string that is cell value
     * @param field      targeted field of model
     * @param expression expression
     * @return value converted to the type of field
     */
    public Object convert(String value, Field field, Expression expression) {
        context.setVariables(this.variables);

        return expression.getValue(context, field.getType());
    }

}
