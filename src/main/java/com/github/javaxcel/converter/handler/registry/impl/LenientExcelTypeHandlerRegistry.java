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
import com.github.javaxcel.converter.handler.impl.*;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.model.graph.DirectedGraph;
import io.github.imsejin.common.model.graph.Graph;
import io.github.imsejin.common.model.graph.traverse.BreadthFirstIterator;
import io.github.imsejin.common.util.ClassUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.*;
import java.util.*;

public class LenientExcelTypeHandlerRegistry implements ExcelTypeHandlerRegistry {

//    private final Graph<Class<?>> classGraph = new DirectedGraph<>();

    private final Map<Class<?>, ExcelTypeHandler<?>> handlerMap = new HashMap<>();

    public LenientExcelTypeHandlerRegistry() {
        // java.lang
        add(Boolean.class, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Short.class, new ShortTypeHandler());
        add(Character.class, new CharacterTypeHandler());
        add(Integer.class, new IntegerTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(String.class, new StringTypeHandler());
        // java.math
        add(BigInteger.class, new BigIntegerTypeHandler());
        add(BigDecimal.class, new BigDecimalTypeHandler());
        // java.util
        add(Date.class, new DateTypeHandler());
        add(UUID.class, new UUIDTypeHandler());
        add(Locale.class, new LocaleTypeHandler());
        // java.time
        add(LocalTime.class, new LocalTimeTypeHandler());
        add(LocalDate.class, new LocalDateTypeHandler());
        add(LocalDateTime.class, new LocalDateTimeTypeHandler());
        add(ZonedDateTime.class, new ZonedDateTimeTypeHandler());
        add(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
        add(OffsetTime.class, new OffsetTimeTypeHandler());
        // java.net
        add(URI.class, new URITypeHandler());
        add(URL.class, new URLTypeHandler());
        // java.io
        add(File.class, new FileTypeHandler());
        // java.nio.file
        add(Path.class, new PathTypeHandler());
    }

    /**
     * {@inheritDoc}
     *
     * <p> Type checking is guaranteed by {@link #add(Class, ExcelTypeHandler)},
     * so this is safe without type checking.
     */
    @Nullable
    @Override
    public ExcelTypeHandler<?> getHandler(Class<?> type) {
        // Finds a handler matched with the type leniently.
        // To handle primitive value by wrapper type handler,
        // Makes the type boxed.
        if (type.isPrimitive()) type = ClassUtils.wrap(type);

        ExcelTypeHandler<?> handler = this.handlerMap.get(type);
        if (handler != null) return handler;

        Graph<Class<?>> graph = ClassUtils.getAllExtendedOrImplementedTypesAsGraph(type);
        Iterator<Class<?>> iterator = new BreadthFirstIterator<>(graph, type);
        while (iterator.hasNext()) {
            Class<?> clazz = iterator.next();
            ExcelTypeHandler<?> delegate = this.handlerMap.get(clazz);

            if (delegate != null) return delegate;
        }

        return null;
    }

    @Override
    public Set<Class<?>> getAllTypes() {
        return this.handlerMap.keySet();
    }

    @Override
    public <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
        Asserts.that(handler)
                .isNotNull()
                .as("ExcelTypeHandlerRegistry doesn't allow the addition of unmatched type and handler as a pair. (type: '{0}', handler: '{1}')", type, handler)
                .predicate(it -> it.getType() == type);

//        Graph<Class<?>> graph = ClassUtils.getAllExtendedOrImplementedTypesAsGraph(type);
//        this.classGraph.addVertex(type);
//        this.classGraph.addAll(graph);

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
