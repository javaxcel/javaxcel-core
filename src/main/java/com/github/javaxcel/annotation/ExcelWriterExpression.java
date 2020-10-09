package com.github.javaxcel.annotation;

import java.lang.annotation.*;

/**
 * Expression for converting field value to cell value.
 *
 * @see io.github.imsejin.expression.spel.standard.SpelExpressionParser
 * @see io.github.imsejin.expression.spel.support.StandardEvaluationContext
 * @see io.github.imsejin.expression.Expression
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
