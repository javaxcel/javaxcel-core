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

package com.github.javaxcel.converter.out.support;

import com.github.javaxcel.analysis.out.ExcelWriteAnalysis;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.DefaultExcelWriteConverter;
import com.github.javaxcel.converter.out.ExcelWriteConverter;
import com.github.javaxcel.converter.out.ExpressionExcelWriteConverter;
import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ExcelWriteConverterSupport implements ExcelWriteConverter {

    private final List<ExcelWriteConverter> converters;

    public ExcelWriteConverterSupport(ExcelTypeHandlerRegistry registry, List<ExcelWriteAnalysis> analyses) {
        List<ExcelWriteConverter> converters = new ArrayList<>();

        converters.add(new DefaultExcelWriteConverter(registry, analyses));
        converters.add(new ExpressionExcelWriteConverter(analyses));

        this.converters = Collections.unmodifiableList(converters);
    }

    @Override
    @ExcludeFromGeneratedJacocoReport
    public boolean supports(Field field) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String convert(Object model, Field field) {
        for (ExcelWriteConverter converter : this.converters) {
            if (converter.supports(field)) {
                return converter.convert(model, field);
            }
        }

        return null;
    }

}
