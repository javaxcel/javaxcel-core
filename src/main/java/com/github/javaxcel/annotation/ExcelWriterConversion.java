package com.github.javaxcel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelWriterConversion {

    /**
     * Expression to be written as cell value.
     *
     * @return expression to be parsed
     * @see org.springframework.expression.spel.standard.SpelExpressionParser
     * @see org.springframework.expression.spel.support.StandardEvaluationContext
     * @see org.springframework.expression.Expression
     */
    String value();

}
