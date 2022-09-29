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

package com.github.javaxcel.converter.handler.registry.impl;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import io.github.imsejin.common.assertion.Asserts;
import jakarta.validation.constraints.Null;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExcelTypeHandlerRegistryImpl implements ExcelTypeHandlerRegistry {

    private final Map<Class<?>, ExcelTypeHandler<?>> handlerMap = new HashMap<>();

    @Null
    @Override
    public ExcelTypeHandler<?> getHandler(Class<?> type) {
        return this.handlerMap.get(type);
    }

    @Override
    public Set<Class<?>> getAllTypes() {
        return this.handlerMap.keySet();
    }

    @Override
    public <T> boolean add(ExcelTypeHandler<T> handler) {
        Asserts.that(handler)
                .describedAs("ExcelTypeHandlerRegistry doesn't allow the addition of null as a handler.")
                .isNotNull();

        return add(handler.getType(), handler);
    }

    @Override
    public <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
        Asserts.that(type)
                .describedAs("ExcelTypeHandlerRegistry doesn't allow the addition of null as a type. (type: '{0}', handler: '{1}')", type, handler)
                .isNotNull();
        Asserts.that(handler)
                .describedAs("ExcelTypeHandlerRegistry doesn't allow the addition of null as a handler. (type: '{0}', handler: '{1}')", type, handler)
                .isNotNull()
                .describedAs("ExcelTypeHandlerRegistry doesn't allow the addition of handler with unmatched type as a pair. (type: '{0}', handler: '{1}')", type, handler)
                .thrownBy(IllegalStateException::new)
                .predicate(it -> it.getType() == type);

        boolean added = !this.handlerMap.containsKey(type);
        this.handlerMap.put(type, handler);

        return added;
    }

    /**
     * {@inheritDoc}
     *
     * <p> Type checking is guaranteed by {@link #add(Class, ExcelTypeHandler)},
     * so this is safe without type checking. To solve the below problem,
     * we use raw type.
     *
     * <pre>
     * Incompatible equality constraint: capture of ? and capture of ?
     * </pre>
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean addAll(ExcelTypeHandlerRegistry registry) {
        Set<Class<?>> allTypes = registry.getAllTypes();
        boolean added = !allTypes.isEmpty();

        for (Class<?> type : allTypes) {
            ExcelTypeHandler handler = registry.getHandler(type);
            added &= add(type, handler);
        }

        return added;
    }

}
