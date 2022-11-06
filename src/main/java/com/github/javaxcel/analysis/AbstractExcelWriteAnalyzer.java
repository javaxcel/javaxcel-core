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

package com.github.javaxcel.analysis;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractExcelWriteAnalyzer implements ExcelAnalyzer<ExcelAnalysis> {

    private final ExcelTypeHandlerRegistry registry;

    protected AbstractExcelWriteAnalyzer(ExcelTypeHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public final List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments) {
        Asserts.that(fields)
                .describedAs("ExcelAnalyzer cannot analyze null as fields")
                .isNotNull()
                .describedAs("ExcelAnalyzer cannot analyze empty fields")
                .isNotEmpty();

        List<ExcelAnalysis> analyses = new ArrayList<>();
        for (Field field : fields) {
            ExcelAnalysisImpl analysis = new ExcelAnalysisImpl(field);

            // Analyzes default value for the field.
            String defaultValue = analyzeDefaultValue(field, arguments);
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
            int flags = analyzeFlags(field, arguments);
            analysis.addFlags(flags);

            analyses.add(analysis);
        }

        return Collections.unmodifiableList(analyses);
    }

    // -------------------------------------------------------------------------------------------------

    @Null
    protected abstract String analyzeDefaultValue(Field field, Object[] arguments);

    protected abstract int analyzeFlags(Field field, Object[] arguments);

}
