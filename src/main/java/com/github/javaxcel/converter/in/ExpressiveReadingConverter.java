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

public class ExpressiveReadingConverter implements ReadingConverter {

    private static final ExpressionParser parser = new SpelExpressionParser();

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
     * {@inheritDoc}
     *
     * <p> Parses a expression to be assigned as field value.
     */
    @Override
    public Object convert(Map<String, Object> variables, Field field) {
        ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
        Expression expression = parser.parseExpression(annotation.value());

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(variables);

        return expression.getValue(context, field.getType());
    }

    /**
     * Converts cached expression into field value.
     *
     * @param variables  {@link Map} in which key is the model's field name and value is the model's field value
     * @param field      targeted field of model
     * @param expression expression
     * @return value converted to the type of field
     */
    public Object convert(Map<String, Object> variables, Field field, Expression expression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(variables);

        return expression.getValue(context, field.getType());
    }

}
