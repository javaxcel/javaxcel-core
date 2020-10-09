package com.github.javaxcel.annotation;

import java.lang.annotation.*;

/**
 * Expression for converting field value to cell value.
 *
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.expression.spel.support.StandardEvaluationContext
 * @see org.springframework.expression.Expression
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelWriterExpression {

    /**
     * Expression to be written as cell value.
     *
     * @return expression to be parsed
     */
    String value();

}
