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
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;

public interface ExcelAnalysis {

    Field getField();

    int getFlags();

    DefaultMeta getDefaultMeta();

    @Null
    ExcelTypeHandler<?> getHandler();

    default boolean hasFlag(int flag) {
        int flags = getFlags();
        return (flags & flag) == flag;
    }

    default boolean doesHandlerResolved() {
        ExcelTypeHandler<?> handler = getHandler();
        return handler != null && handler.getType() != Object.class;
    }

    // -------------------------------------------------------------------------------------------------

    interface DefaultMeta {
        @Null
        String getValue();

        Source getSource();

        enum Source {
            NONE, MODEL, COLUMN, OPTION
        }
    }

}
