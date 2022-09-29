/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.constant;

import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.converter.in.DefaultExcelReadConverter;
import com.github.javaxcel.converter.in.ExpressionExcelReadConverter;
import com.github.javaxcel.converter.out.DefaultExcelWriteConverter;
import com.github.javaxcel.converter.out.ExpressionExcelWriteConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Type of conversion
 */
public enum ConversionType {

    /**
     * @see DefaultExcelWriteConverter
     * @see DefaultExcelReadConverter
     */
    DEFAULT,

    /**
     * @see ExpressionExcelWriteConverter
     * @see ExpressionExcelReadConverter
     */
    EXPRESSION;

    public static ConversionType of(Field field, ConverterType converterType) {
        Annotation annotation;
        switch (converterType) {
            case IN:
                annotation = field.getAnnotation(ExcelReadExpression.class);
                break;
            case OUT:
                annotation = field.getAnnotation(ExcelWriteExpression.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown ConverterType: " + converterType);
        }

        return annotation == null ? DEFAULT : EXPRESSION;
    }

}
