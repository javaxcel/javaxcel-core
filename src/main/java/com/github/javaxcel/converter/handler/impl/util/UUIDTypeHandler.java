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

package com.github.javaxcel.converter.handler.impl.util;

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;

import java.util.UUID;

public class UUIDTypeHandler extends AbstractExcelTypeHandler<UUID> {

    public UUIDTypeHandler() {
        super(UUID.class);
    }

    @Override
    protected String writeInternal(UUID value, Object... args) {
        return value.toString();
    }

    @Override
    public UUID read(String value, Object... args) {
        return UUID.fromString(value);
    }

}
