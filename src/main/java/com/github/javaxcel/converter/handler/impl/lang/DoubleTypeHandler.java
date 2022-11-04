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

package com.github.javaxcel.converter.handler.impl.lang;

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;

public class DoubleTypeHandler extends AbstractExcelTypeHandler<Double> {

    public DoubleTypeHandler() {
        this(false);
    }

    public DoubleTypeHandler(boolean primitive) {
        super(primitive ? double.class : Double.class);
    }

    @Override
    protected String writeInternal(Double value, Object... args) {
        return value.toString();
    }

    @Override
    public Double read(String value, Object... args) {
        return Double.parseDouble(value);
    }

}
