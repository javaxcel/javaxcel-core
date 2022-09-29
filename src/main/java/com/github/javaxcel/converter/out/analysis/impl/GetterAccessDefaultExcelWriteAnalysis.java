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

package com.github.javaxcel.converter.out.analysis.impl;

import com.github.javaxcel.converter.out.analysis.AbstractExcelWriteAnalysis;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GetterAccessDefaultExcelWriteAnalysis extends AbstractExcelWriteAnalysis {

    private final Method getter;

    public GetterAccessDefaultExcelWriteAnalysis(Field field, String defaultValue) {
        super(field, defaultValue);

        Method getter;
        try {
            getter = FieldUtils.resolveGetter(field);
        } catch (RuntimeException ignored) {
            getter = null;
        }

        this.getter = getter;
    }

    @Override
    public Object getValue(Object model) {
        if (this.getter == null) {
            return ReflectionUtils.getFieldValue(model, getField());
        } else {
            return ReflectionUtils.invoke(this.getter, model);
        }
    }

}
