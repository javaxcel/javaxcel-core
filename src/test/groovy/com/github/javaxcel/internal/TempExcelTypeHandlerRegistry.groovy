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

package com.github.javaxcel.internal

import com.github.javaxcel.converter.handler.ExcelTypeHandler
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry

class TempExcelTypeHandlerRegistry implements ExcelTypeHandlerRegistry {

    final Map<Class<?>, ExcelTypeHandler> handlerMap = new HashMap<>()

    @Override
    ExcelTypeHandler<?> getHandler(Class<?> type) {
        this.handlerMap.get(type)
    }

    @Override
    Set<Class<?>> getAllTypes() {
        this.handlerMap.keySet()
    }

    @Override
    <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
        def hasType = this.handlerMap.containsKey type
        if (!hasType) this.handlerMap.put(type, handler)

        hasType
    }

    @Override
    boolean addAll(ExcelTypeHandlerRegistry registry) {
        def types = registry.allTypes
        def hasType = !types.isEmpty()

        for (def type : types) {
            hasType &= add(type, registry.getHandler(type))
        }

        hasType
    }

}
