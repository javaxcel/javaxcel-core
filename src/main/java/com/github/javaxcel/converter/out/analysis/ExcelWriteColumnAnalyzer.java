/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.converter.out.analysis;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.analysis.impl.FieldAccessDefaultExcelWriteColumnAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.FieldAccessExpressionExcelWriteColumnAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.GetterAccessDefaultExcelWriteColumnAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.GetterAccessExpressionExcelWriteColumnAnalysis;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import com.github.javaxcel.out.strategy.impl.UseGetters;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcelWriteColumnAnalyzer {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final Class<?> type;

    public ExcelWriteColumnAnalyzer(Class<?> type) {
        this.type = type;
    }

    public List<ExcelWriteColumnAnalysis> analyze(List<Field> fields, Object... args) {
        Asserts.that(fields)
                .isNotNull()
                .isNotEmpty()
                .allMatch(field -> field.getDeclaringClass() == this.type);

        DefaultValue defaultValueStrategy = FieldUtils.resolveFirst(DefaultValue.class, args);
        UseGetters useGettersStrategy = FieldUtils.resolveFirst(UseGetters.class, args);
        ExcelTypeHandlerRegistry registry = FieldUtils.resolveFirst(ExcelTypeHandlerRegistry.class, args);

        List<ExcelWriteColumnAnalysis> analyses = new ArrayList<>();

        for (Field field : fields) {
            // Resolves default value for the field.
            String defaultValue;
            if (defaultValueStrategy == null) {
                defaultValue = resolveDefaultValue(field);
            } else {
                defaultValue = (String) defaultValueStrategy.execute(null);
            }

            ExcelWriteColumnAnalysis analysis;
            ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);

            if (annotation == null) {
                // DefaultExcelWriteConverter
                if (useGettersStrategy == null) {
                    analysis = new FieldAccessDefaultExcelWriteColumnAnalysis(field, defaultValue);
                } else {
                    analysis = new GetterAccessDefaultExcelWriteColumnAnalysis(field, defaultValue);
                }

            } else {
                // ExpressionExcelWriteConverter
                Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());

                if (useGettersStrategy == null) {
                    analysis = new FieldAccessExpressionExcelWriteColumnAnalysis(expression, fields, defaultValue);
                } else {
                    analysis = new GetterAccessExpressionExcelWriteColumnAnalysis(expression, fields, defaultValue);
                }
            }

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    private static String resolveDefaultValue(Field field) {
        String defaultValue = null;

        // Decides the proper default value for a field value.
        // @ExcelColumn's default value takes precedence over ExcelModel's default value.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
            // Default value on @ExcelColumn
            defaultValue = columnAnnotation.defaultValue();
        } else {
            ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
            if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
                // Default value on @ExcelModel
                defaultValue = modelAnnotation.defaultValue();
            }
        }

        return defaultValue;
    }

}
