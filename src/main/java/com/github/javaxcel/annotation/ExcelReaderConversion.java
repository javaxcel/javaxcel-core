package com.github.javaxcel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelReaderConversion {

    /**
     * Expression to be assigned as field value.
     *
     * @return expression parser will parse
     * @see org.springframework.expression.spel.standard.SpelExpressionParser
     * @see org.springframework.expression.spel.support.StandardEvaluationContext
     * @see org.springframework.expression.Expression
     */
    String value();

}
