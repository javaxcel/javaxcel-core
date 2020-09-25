package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelReaderConversion;
import com.github.javaxcel.converter.ReadingConverter;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.Map;

public class ExpressiveReadingConverter<T> implements ReadingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private static final StandardEvaluationContext context = new StandardEvaluationContext();

    private final Class<T> type;

    private Map<String, Object> variables;

    public ExpressiveReadingConverter(Class<T> type) {
        this.type = type;
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
     * Parses a expression to be assigned as field value.
     *
     * @param value value
     * @param field targeted field
     * @return converted model
     */
    @Override
    public Object convert(String value, Field field) {
        ExcelReaderConversion annotation = field.getAnnotation(ExcelReaderConversion.class);
        context.setVariables(this.variables);

        return parser.parseExpression(annotation.value()).getValue(context, field.getType());
    }

}
