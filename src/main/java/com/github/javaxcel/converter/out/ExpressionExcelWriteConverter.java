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

package com.github.javaxcel.converter.out;

import com.github.javaxcel.converter.out.analysis.ExcelWriteAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.FieldAccessExpressionExcelWriteAnalysis;
import com.github.javaxcel.converter.out.analysis.impl.GetterAccessExpressionExcelWriteAnalysis;
import io.github.imsejin.common.assertion.Asserts;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class ExpressionExcelWriteConverter implements ExcelWriteConverter {

    private final Map<Field, ExcelWriteAnalysis> analysisMap;

    public ExpressionExcelWriteConverter(List<ExcelWriteAnalysis> analyses) {
        Asserts.that(analyses)
                .describedAs("ExpressionExcelWriteConverter.analyses is not allowed to be null")
                .isNotNull();

        this.analysisMap = analyses.stream().collect(collectingAndThen(
                toMap(ExcelWriteAnalysis::getField, Function.identity()), Collections::unmodifiableMap));
    }

    @Override
    public boolean supports(Field field) {
        ExcelWriteAnalysis analysis = this.analysisMap.get(field);

        return analysis instanceof FieldAccessExpressionExcelWriteAnalysis
                || analysis instanceof GetterAccessExpressionExcelWriteAnalysis;
    }

    /**
     * {@inheritDoc}
     */
    @Null
    @Override
    public String convert(Object model, Field field) {
        ExcelWriteAnalysis analysis = this.analysisMap.get(field);
        Object value = analysis.getValue(model);

        // Returns default value if the value is null.
        if (value == null) {
            return analysis.getDefaultValue();
        }

        return value.toString();
    }

}
