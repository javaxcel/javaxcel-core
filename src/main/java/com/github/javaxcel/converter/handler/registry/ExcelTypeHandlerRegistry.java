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

package com.github.javaxcel.converter.handler.registry;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;

import java.util.Set;

/**
 * Registry of type handlers
 *
 * <p> This only allows the registration of type handler by
 * {@link #add(Class, ExcelTypeHandler)} and {@link #addAll(ExcelTypeHandlerRegistry)}.
 */
public interface ExcelTypeHandlerRegistry {

    /**
     * Returns a handler of the type.
     *
     * @param type handled type
     * @return type handler
     */
    ExcelTypeHandler<?> getHandler(Class<?> type);

    /**
     * Returns all registered types.
     *
     * <p> This shouldn't return null.
     *
     * @return all registered types
     */
    Set<Class<?>> getAllTypes();

    /**
     * Adds the matched type and handler as a pair.
     *
     * <p> If the type is already added, its handler will be overridden as new handler.
     *
     * @param type    handled type
     * @param handler type handler
     * @param <T>     type
     * @return whether type has never been added this registry
     * @throws IllegalArgumentException if a pair of type and handler is unmatched
     */
    <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler);

    /**
     * Adds all the matched types and handlers.
     *
     * <p> If the type is already added, its handler will be overridden as new handler.
     *
     * @param registry registry of type handler
     * @return whether all types have never been added this registry
     * @throws IllegalArgumentException if any pair of type and handler is unmatched
     */
    boolean addAll(ExcelTypeHandlerRegistry registry);

}
