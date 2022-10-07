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

package com.github.javaxcel.analysis.out.impl;

import com.github.javaxcel.analysis.AbstractExcelAnalysis;
import com.github.javaxcel.analysis.out.ExcelWriteAnalysis;
import io.github.imsejin.common.util.ReflectionUtils;

import java.lang.reflect.Field;

public final class FieldAccessDefaultExcelWriteAnalysis extends AbstractExcelAnalysis implements ExcelWriteAnalysis {

    public FieldAccessDefaultExcelWriteAnalysis(Field field, String defaultValue) {
        super(field, defaultValue);
    }

    @Override
    public Object getValue(Object model) {
        return ReflectionUtils.getFieldValue(model, getField());
    }

}
