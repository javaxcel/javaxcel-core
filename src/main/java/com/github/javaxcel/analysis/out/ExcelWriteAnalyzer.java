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

package com.github.javaxcel.analysis.out;

import com.github.javaxcel.analysis.ExcelAnalysisResolver;
import com.github.javaxcel.analysis.ExcelAnalyzer;
import com.github.javaxcel.analysis.out.impl.FieldAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.analysis.out.impl.FieldAccessExpressionExcelWriteAnalysis;
import com.github.javaxcel.analysis.out.impl.GetterAccessDefaultExcelWriteAnalysis;
import com.github.javaxcel.analysis.out.impl.GetterAccessExpressionExcelWriteAnalysis;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
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
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class ExcelWriteAnalyzer implements ExcelAnalyzer<ExcelWriteAnalysis> {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final int VOID_BIT = 0x00;
    private static final int EXPRESSION_BIT = 0x01;
    private static final int USE_GETTERS_BIT = 0x02;
    private static final List<ExcelAnalysisResolver> ANALYSIS_RESOLVERS = Stream.of(
            new FieldAccessDefaultExcelAnalysisResolver(), new FieldAccessExpressionExcelAnalysisResolver(),
            new GetterAccessDefaultExcelAnalysisResolver(), new GetterAccessExpressionExcelAnalysisResolver())
            .collect(collectingAndThen(toList(), Collections::unmodifiableList));

    @Override
    public List<ExcelWriteAnalysis> analyze(List<Field> fields, Object... arguments) {
        Asserts.that(fields)
                .describedAs("ExcelWriteAnalyzer cannot analyze null as fields")
                .isNotNull()
                .describedAs("ExcelWriteAnalyzer cannot analyze empty fields")
                .isNotEmpty();

        ExcelTypeHandlerRegistry registry = Objects.requireNonNull(
                FieldUtils.resolveFirst(ExcelTypeHandlerRegistry.class, arguments),
                "Never throw; ExcelTypeHandlerRegistry is injected into arguments by caller");

        List<ExcelWriteAnalysis> analyses = new ArrayList<>();
        for (Field field : fields) {
            Class<?> actualType = FieldUtils.resolveActualType(field);
            ExcelTypeHandler<?> handler = registry.getHandler(actualType);

            Object[] args = ArrayUtils.prepend(arguments, fields, field, handler);
            ExcelWriteAnalysis analysis = resolveAnalysis(field, args);

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    private static ExcelWriteAnalysis resolveAnalysis(Field field, Object... args) {
        UseGetters useGettersStrategy = FieldUtils.resolveFirst(UseGetters.class, args);
        ExcelWriteExpression annotation = field.getAnnotation(ExcelWriteExpression.class);

        int bit = VOID_BIT;
        bit |= annotation == null ? VOID_BIT : EXPRESSION_BIT;
        bit |= useGettersStrategy == null ? VOID_BIT : USE_GETTERS_BIT;

        for (ExcelAnalysisResolver resolver : ANALYSIS_RESOLVERS) {
            if (resolver.getBit() == bit) {
                return resolver.resolve(args);
            }
        }

        throw new RuntimeException("Never throw; ExcelWriteAnalyzer.ANALYSIS_RESOLVERS can deal with a bit in every case");
    }

    // -------------------------------------------------------------------------------------------------

    private static class FieldAccessDefaultExcelAnalysisResolver implements ExcelAnalysisResolver {
        @Override
        public int getBit() {
            return VOID_BIT;
        }

        @Override
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = ExcelAnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelTypeHandler<?> handler = FieldUtils.resolveFirst(ExcelTypeHandler.class, args);

            FieldAccessDefaultExcelWriteAnalysis fad = new FieldAccessDefaultExcelWriteAnalysis(field, defaultValue);
            if (handler != null) {
                fad.setHandler(handler);
            }

            return fad;
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class FieldAccessExpressionExcelAnalysisResolver implements ExcelAnalysisResolver {
        @Override
        public int getBit() {
            return EXPRESSION_BIT;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = Objects.requireNonNull(FieldUtils.resolveFirst(Field.class, args),
                    "Never throw; Field is injected into arguments by ExcelWriteAnalyzer");

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = ExcelAnalysisResolver.resolveDefaultValue(field, strategy);

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

    private static class GetterAccessDefaultExcelAnalysisResolver implements ExcelAnalysisResolver {
        @Override
        public int getBit() {
            return USE_GETTERS_BIT;
        }

        @Override
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = FieldUtils.resolveFirst(Field.class, args);

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = ExcelAnalysisResolver.resolveDefaultValue(field, strategy);

            ExcelTypeHandler<?> handler = FieldUtils.resolveFirst(ExcelTypeHandler.class, args);

            GetterAccessDefaultExcelWriteAnalysis gad = new GetterAccessDefaultExcelWriteAnalysis(field, defaultValue);
            if (handler != null) {
                gad.setHandler(handler);
            }

            return gad;
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static class GetterAccessExpressionExcelAnalysisResolver implements ExcelAnalysisResolver {
        @Override
        public int getBit() {
            return EXPRESSION_BIT | USE_GETTERS_BIT;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExcelWriteAnalysis resolve(Object... args) {
            Field field = Objects.requireNonNull(FieldUtils.resolveFirst(Field.class, args),
                    "Never throw; Field is injected into arguments by ExcelWriteAnalyzer");

            DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, args);
            String defaultValue = ExcelAnalysisResolver.resolveDefaultValue(field, strategy);

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
