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
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.analysis.impl.FieldAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.FieldAccessExpressionExcelWriteAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.GetterAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.GetterAccessExpressionExcelWriteAnalysis;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import com.github.javaxcel.out.strategy.impl.UseGetters;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class ExcelWriteAnalyzer {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final int EXPRESSION_BIT = 0x01;
    private static final int USE_GETTERS_BIT = 0x02;
    private static final List<AnalysisResolver> ANALYSIS_RESOLVERS = Stream.of(
            new FieldAccessDefaultAnalysisResolver(), new FieldAccessExpressionAnalysisResolver(),
            new GetterAccessDefaultAnalysisResolver(), new GetterAccessExpressionAnalysisResolver())
            .collect(collectingAndThen(toList(), Collections::unmodifiableList));

    private final Class<?> type;

    public ExcelWriteAnalyzer(Class<?> type) {
        this.type = type;
    }

    public List<ExcelWriteAnalysis> analyze(List<Field> fields, Object... arguments) {
        Asserts.that(fields)
                .describedAs("ExcelWriteAnalyzer cannot analyze null as fields")
                .isNotNull()
                .describedAs("ExcelWriteAnalyzer cannot analyze empty fields")
                .isNotEmpty()
                .describedAs("One of fields ExcelWriteAnalyzer analyze are declared on different type from the given type. (declared: '{0}', given: '{1}')",
                        fields.stream().filter(field -> field.getDeclaringClass() != this.type)
                                .findFirst().map(Field::getDeclaringClass).orElse(null), this.type)
                .allMatch(field -> field.getDeclaringClass() == this.type);

        ExcelTypeHandlerRegistry registry = FieldUtils.resolveFirst(ExcelTypeHandlerRegistry.class, arguments);

        List<ExcelWriteAnalysis> analyses = new ArrayList<>();
        for (Field field : fields) {
            ExcelTypeHandler<?> handler = registry.getHandler(field.getType());

            Object[] args = ArrayUtils.prepend(arguments, field, handler);
            ExcelWriteAnalysis analysis = resolveAnalysis(field, args);

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    private static ExcelWriteAnalysis resolveAnalysis(Field field, Object... args) {
        UseGetters useGettersStrategy = FieldUtils.resolveFirst(UseGetters.class, args);
        ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);

        int bit = 0;
        bit |= annotation == null ? 0 : EXPRESSION_BIT;
        bit |= useGettersStrategy == null ? 0 : USE_GETTERS_BIT;

        for (AnalysisResolver resolver : ANALYSIS_RESOLVERS) {
            if (resolver.getBit() == bit) {
                return resolver.resolve(args);
            }
        }

        throw new RuntimeException("NEVER THROW");
    }

    // -------------------------------------------------------------------------------------------------

    private interface AnalysisResolver {
        int getBit();

        ExcelWriteAnalysis resolve(Object... args);

        static String resolveDefaultValue(Field field, DefaultValue strategy) {
            if (strategy != null) {
                return (String) strategy.execute(null);
            }

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

    // -------------------------------------------------------------------------------------------------

    private static class FieldAccessDefaultAnalysisResolver implements AnalysisResolver {
        @Override
        public int getBit() {
            return 0x00;
        }

        @Override
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = AnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelTypeHandler<?> handler = FieldUtils.resolveFirst(ExcelTypeHandler.class, args);

            FieldAccessDefaultExcelWriteAnalysis fad = new FieldAccessDefaultExcelWriteAnalysis(field, defaultValue);
            if (handler != null) {
                fad.setHandler(handler);
            }

            return fad;
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class FieldAccessExpressionAnalysisResolver implements AnalysisResolver {
        @Override
        public int getBit() {
            return EXPRESSION_BIT;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = AnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);
            Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());
            List<Field> fields = (List<Field>) FieldUtils.resolveFirst(List.class, args);

            // Return type of the expression is determined on runtime, so analyzer can't resolve handler for this column.
            FieldAccessExpressionExcelWriteAnalysis fae = new FieldAccessExpressionExcelWriteAnalysis(field, defaultValue);
            fae.setExpression(expression);
            fae.setFields(fields);

            return fae;
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class GetterAccessDefaultAnalysisResolver implements AnalysisResolver {
        @Override
        public int getBit() {
            return USE_GETTERS_BIT;
        }

        @Override
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = AnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelTypeHandler<?> handler = FieldUtils.resolveFirst(ExcelTypeHandler.class, args);

            GetterAccessDefaultExcelWriteAnalysis gad = new GetterAccessDefaultExcelWriteAnalysis(field, defaultValue);
            if (handler != null) {
                gad.setHandler(handler);
            }

            return gad;
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class GetterAccessExpressionAnalysisResolver implements AnalysisResolver {
        @Override
        public int getBit() {
            return EXPRESSION_BIT | USE_GETTERS_BIT;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = AnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);
            Expression expression = EXPRESSION_PARSER.parseExpression(annotation.value());
            List<Field> fields = (List<Field>) FieldUtils.resolveFirst(List.class, args);

            // Return type of the expression is determined on runtime, so analyzer can't resolve handler for this column.
            GetterAccessExpressionExcelWriteAnalysis gae = new GetterAccessExpressionExcelWriteAnalysis(field, defaultValue);
            gae.setExpression(expression);
            gae.setGetters(fields);

            return gae;
        }
    }


}
