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

import com.github.javaxcel.analysis.AbstractExcelWriteAnalyzer;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.in.strategy.impl.UseSetters;
import com.github.javaxcel.util.FieldUtils;

import java.lang.reflect.Field;

/**
 * The type Excel read analyzer.
 */
public class ExcelReadAnalyzer extends AbstractExcelWriteAnalyzer {

    /**
     * The constant HANDLER.
     */
    public static final int HANDLER = 0x01;

    /**
     * The constant EXPRESSION.
     */
    public static final int EXPRESSION = 0x02;

    /**
     * The constant FIELD_ACCESS.
     */
    public static final int FIELD_ACCESS = 0x04;

    /**
     * The constant SETTER.
     */
    public static final int SETTER = 0x08;

    /**
     * Instantiates a new Excel read analyzer.
     *
     * @param registry the registry
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
