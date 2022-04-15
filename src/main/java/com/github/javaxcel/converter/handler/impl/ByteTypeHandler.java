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

package com.github.javaxcel.converter.handler.impl;

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;

public class ByteTypeHandler extends AbstractExcelTypeHandler<Byte> {

    public ByteTypeHandler() {
        this(false);
    }

    public ByteTypeHandler(boolean primitive) {
        super(primitive ? byte.class : Byte.class);
    }

    @Override
    protected String writeInternal(Byte value, Object... args) {
        return value.toString();
    }

    @Override
    public Byte read(String value, Object... args) {
        return Byte.parseByte(value);
    }

}
