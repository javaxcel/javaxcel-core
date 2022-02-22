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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

public class ExcelTypeHandlerRegistryImpl implements ExcelTypeHandlerRegistry {

    private final Map<Class<?>, ExcelTypeHandler<?>> handlerMap = new HashMap<>();

    public ExcelTypeHandlerRegistryImpl() {
        // primitive
        add(boolean.class, new BooleanTypeHandler(true));
        add(byte.class, new ByteTypeHandler(true));
        add(short.class, new ShortTypeHandler(true));
        add(char.class, new CharacterTypeHandler(true));
        add(int.class, new IntegerTypeHandler(true));
        add(long.class, new LongTypeHandler(true));
        add(float.class, new FloatTypeHandler(true));
        add(double.class, new DoubleTypeHandler(true));
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
    }

    /**
     * {@inheritDoc}
     *
     * <p> Type checking is guaranteed by {@link #add(Class, ExcelTypeHandler)},
     * so this is safe without type checking.
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> ExcelTypeHandler<T> getHandler(Class<T> type) {
        return (ExcelTypeHandler<T>) this.handlerMap.get(type);
    }

    @Nonnull
    @Override
    public Set<Class<?>> getAllTypes() {
        return this.handlerMap.keySet();
    }

    @Override
    public <T> boolean add(Class<T> type, ExcelTypeHandler<T> handler) {
        Asserts.that(type)
                .isNotNull()
                .as("ExcelTypeHandlerRegistry doesn't allow the addition of unmatched type and handler as a pair. (type: '{0}', handler: '{1}')", type, handler)
                .predicate(handler::matches);

        boolean overridden = this.handlerMap.containsKey(type);
        this.handlerMap.put(type, handler);

        return overridden;
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
        boolean merged = false;

        for (Class<?> type : registry.getAllTypes()) {
            ExcelTypeHandler handler = registry.getHandler(type);
            merged |= add(type, handler);
        }

        return merged;
    }

}
