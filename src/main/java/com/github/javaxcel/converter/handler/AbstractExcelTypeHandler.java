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

package com.github.javaxcel.converter.handler;

import io.github.imsejin.common.assertion.Asserts;

public abstract class AbstractExcelTypeHandler<T> implements ExcelTypeHandler<T> {

    private final Class<T> type;

    protected AbstractExcelTypeHandler(Class<T> type) {
        Asserts.that(type)
                .as("AbstractExcelTypeHandler.type is not allowed to be null")
                .isNotNull();

        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final String write(Object value, Object... args) throws Exception {
        return writeInternal((T) value, args);
    }

    protected abstract String writeInternal(T value, Object... args) throws Exception;

}
