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

package com.github.javaxcel.analysis.in;

import com.github.javaxcel.analysis.AbstractExcelAnalyzer;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.in.ExcelReadExpressionConverter;
import com.github.javaxcel.converter.in.ExcelReadHandlerConverter;
import com.github.javaxcel.in.strategy.impl.UseSetters;
import com.github.javaxcel.util.FieldUtils;

import java.lang.reflect.Field;

/**
 * Analyzer for reading Excel
 */
public class ExcelReadAnalyzer extends AbstractExcelAnalyzer {

    /**
     * Flag which indicates that the field should be handled by {@link ExcelReadHandlerConverter}.
     */
    public static final int HANDLER = 0x01;

    /**
     * Flag which indicates that the field should be handled by {@link ExcelReadExpressionConverter}.
     */
    public static final int EXPRESSION = 0x02;

    /**
     * Flag which indicates that value of the field should be set through access to field.
     */
    public static final int FIELD_ACCESS = 0x04;

    /**
     * Flag which indicates that value of the field should be set through setter for the field.
     */
    public static final int SETTER = 0x08;

    /**
     * Instantiates a new analyzer for reading Excel.
     *
     * @param registry registry of handlers
     */
    public ExcelReadAnalyzer(ExcelTypeHandlerRegistry registry) {
        super(registry);
    }

    @Override
    protected DefaultMeta analyzeDefaultMeta(Field field, Object[] arguments) {
        // ExcelReader supports only @ExcelColumn.defaultValue.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().isEmpty()) {
            String value = columnAnnotation.defaultValue();
            return new DefaultMetaImpl(value, Source.COLUMN);
        }

        return DefaultMetaImpl.EMPTY;
    }

    @Override
    protected int analyzeFlags(Field field, Object[] arguments) {
        UseSetters us = FieldUtils.resolveFirst(UseSetters.class, arguments);

        int flags = 0x00;
        flags |= field.isAnnotationPresent(ExcelReadExpression.class) ? EXPRESSION : HANDLER;
        flags |= us == null ? FIELD_ACCESS : SETTER;

        // Checks if getter of the field exists.
        if ((flags & SETTER) == SETTER) {
            try {
                FieldUtils.resolveSetter(field);
            } catch (RuntimeException ignored) {
                // When it doesn't exist, removes flag of SETTER from the flags.
                flags = flags ^ SETTER;
                flags |= FIELD_ACCESS;
            }
        }

        return flags;
    }

}
