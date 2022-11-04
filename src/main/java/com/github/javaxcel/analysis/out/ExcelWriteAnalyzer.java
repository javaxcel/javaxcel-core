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

import com.github.javaxcel.analysis.ExcelAnalysis;
import com.github.javaxcel.analysis.ExcelAnalysisImpl;
import com.github.javaxcel.analysis.ExcelAnalyzer;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import com.github.javaxcel.out.strategy.impl.UseGetters;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcelWriteAnalyzer implements ExcelAnalyzer<ExcelAnalysis> {

    public static final int HANDLER = 0x01;

    public static final int EXPRESSION = 0x02;

    public static final int FIELD_ACCESS = 0x04;

    public static final int GETTER = 0x08;

    private final ExcelTypeHandlerRegistry registry;

    public ExcelWriteAnalyzer(ExcelTypeHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments) {
        Asserts.that(fields)
                .describedAs("ExcelWriteAnalyzer cannot analyze null as fields")
                .isNotNull()
                .describedAs("ExcelWriteAnalyzer cannot analyze empty fields")
                .isNotEmpty();

        List<ExcelAnalysis> analyses = new ArrayList<>();
        for (Field field : fields) {
            ExcelAnalysisImpl analysis = new ExcelAnalysisImpl(field);

            // Analyzes default value for the field.
            DefaultValue dva = FieldUtils.resolveFirst(DefaultValue.class, arguments);
            String defaultValue = analyzeDefaultValue(field, dva);
            if (!StringUtils.isNullOrEmpty(defaultValue)) {
                analysis.setDefaultValue(defaultValue);
            }

            // Analyzes handler for the field.
            Class<?> actualType = FieldUtils.resolveActualType(field);
            ExcelTypeHandler<?> handler = this.registry.getHandler(actualType);
            if (handler != null) {
                analysis.setHandler(handler);
            }

            // Analyzes flags for the field.
            UseGetters uga = FieldUtils.resolveFirst(UseGetters.class, arguments);
            int flags = analyzeFlags(field, uga);
            analysis.addFlags(flags);

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    @Null
    private static String analyzeDefaultValue(Field field, @Null DefaultValue strategy) {
        if (strategy != null) {
            return (String) strategy.execute(null);
        }

        // Decides the proper default value for a field value.
        // @ExcelColumn's default value takes precedence over ExcelModel's default value.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
            // Default value on @ExcelColumn
            return columnAnnotation.defaultValue();
        }

        ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
        if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
            // Default value on @ExcelModel
            return modelAnnotation.defaultValue();
        }

        return null;
    }

    private static int analyzeFlags(Field field, @Null UseGetters strategy) {
        int flags = 0x00;

        flags |= field.isAnnotationPresent(ExcelWriteExpression.class) ? EXPRESSION : HANDLER;
        flags |= strategy == null ? FIELD_ACCESS : GETTER;

        // Checks if getter of the field exists.
        if ((flags & GETTER) == GETTER) {
            try {
                FieldUtils.resolveGetter(field);
            } catch (RuntimeException ignored) {
                // When it doesn't exist, removes flag of GETTER from the flags.
                flags = flags ^ GETTER;
            }
        }

        return flags;
    }

}
