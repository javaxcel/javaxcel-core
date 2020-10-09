package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.converter.WritingConverter;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.Map;

public class ExpressiveWritingConverter<T> implements WritingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private final StandardEvaluationContext context = new StandardEvaluationContext();

    private String defaultValue;

    private Map<String, Object> variables;

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
     * @param model object in list
     * @param field field of object
     * @return computed string
     * @see ExcelWriterExpression#value()
     */
    @Override
    public String convert(T model, Field field) {
        ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
        context.setRootObject(model);
        context.setVariables(this.variables);

        String result = parser.parseExpression(annotation.value())
                .getValue(context, String.class);

        return WritingConverter.convertIfDefault(result, this.defaultValue, field);
    }

}
