package com.github.javaxcel.annotation;

import java.lang.annotation.*;

/**
 * Expression for conversion of cell value to field value.
 *
 * @see io.github.imsejin.expression.spel.standard.SpelExpressionParser
 * @see io.github.imsejin.expression.spel.support.StandardEvaluationContext
 * @see io.github.imsejin.expression.Expression
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelReaderExpression {

    /**
     * Expression to be assigned as field value.
     *
     * @return expression to be parsed
     * @see com.github.javaxcel.converter.impl.ExpressiveReadingConverter
     * @see io.github.imsejin.expression.Expression
     */
    String value();

}
