package com.github.javaxcel.annotation;

import java.lang.annotation.*;

/**
 * Expression for converting cell value to field value.
 *
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.expression.spel.support.StandardEvaluationContext
 * @see org.springframework.expression.Expression
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelReaderExpression {

    /**
     * Expression to be assigned as field value.
     *
     * @return expression parser will parse
     */
    String value();

}
