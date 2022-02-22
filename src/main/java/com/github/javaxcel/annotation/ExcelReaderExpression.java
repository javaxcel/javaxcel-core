/*
 * Copyright 2020 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.annotation;

import com.github.javaxcel.converter.in.ExpressionExcelReadConverter;

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
     * @see ExpressionExcelReadConverter
     * @see io.github.imsejin.expression.Expression
     */
    String value();

}
