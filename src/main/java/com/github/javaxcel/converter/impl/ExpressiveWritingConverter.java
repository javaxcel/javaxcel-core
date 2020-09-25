package com.github.javaxcel.converter.impl;

import com.github.javaxcel.annotation.ExcelWriterConversion;
import com.github.javaxcel.converter.WritingConverter;
import io.github.imsejin.util.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.Map;

public class ExpressiveWritingConverter<T> implements WritingConverter<T> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private static final StandardEvaluationContext context = new StandardEvaluationContext();

    private String defaultValue;

    private Map<String, Object> variables;

    /**
     * Sets up the default value.
     *
     * @param defaultValue default value
     */
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
     * @param model        object in list
     * @param field        field of object
     * @return computed string
     * @see ExcelWriterConversion#value()
     */
    @Override
    public String convert(T model, Field field) {
        ExcelWriterConversion annotation = field.getAnnotation(ExcelWriterConversion.class);
        context.setRootObject(model);
        context.setVariables(this.variables);

        String result = parser.parseExpression(annotation.value())
                .getValue(context, String.class);

        return StringUtils.ifNullOrEmpty(result, (String) null);
    }

}
