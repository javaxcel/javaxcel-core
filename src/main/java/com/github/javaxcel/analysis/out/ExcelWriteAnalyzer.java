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

import com.github.javaxcel.analysis.AbstractExcelWriteAnalyzer;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta;
import com.github.javaxcel.analysis.ExcelAnalysis.DefaultMeta.Source;
import com.github.javaxcel.analysis.ExcelAnalysisImpl.DefaultMetaImpl;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import com.github.javaxcel.out.strategy.impl.UseGetters;
import com.github.javaxcel.util.FieldUtils;

import java.lang.reflect.Field;

/**
 * The type Excel write analyzer.
 */
public class ExcelWriteAnalyzer extends AbstractExcelWriteAnalyzer {

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
     * The constant GETTER.
     */
    public static final int GETTER = 0x08;

    /**
     * Instantiates a new Excel write analyzer.
     *
     * @param registry the registry
     */
    public ExcelWriteAnalyzer(ExcelTypeHandlerRegistry registry) {
        super(registry);
    }

    @Override
    protected DefaultMeta analyzeDefaultMeta(Field field, Object[] arguments) {
        DefaultValue strategy = FieldUtils.resolveFirst(DefaultValue.class, arguments);

        // Decides the proper default value for the field value.
        if (strategy != null) {
            String value = (String) strategy.execute(null);
            return new DefaultMetaImpl(value, Source.OPTION);
        }

        // @ExcelColumn.defaultValue takes precedence over @ExcelModel.defaultValue.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().isEmpty()) {
            // Default value on @ExcelColumn
            String value = columnAnnotation.defaultValue();
            return new DefaultMetaImpl(value, Source.COLUMN);
        }

        ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
        if (modelAnnotation != null && !modelAnnotation.defaultValue().isEmpty()) {
            // Default value on @ExcelModel
            String value = modelAnnotation.defaultValue();
            return new DefaultMetaImpl(value, Source.MODEL);
        }

        return DefaultMetaImpl.EMPTY;
    }

    @Override
    protected int analyzeFlags(Field field, Object[] arguments) {
        UseGetters ug = FieldUtils.resolveFirst(UseGetters.class, arguments);

        int flags = 0x00;
        flags |= field.isAnnotationPresent(ExcelWriteExpression.class) ? EXPRESSION : HANDLER;
        flags |= ug == null ? FIELD_ACCESS : GETTER;

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
