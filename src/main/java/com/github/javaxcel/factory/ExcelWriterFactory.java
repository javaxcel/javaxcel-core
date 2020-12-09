/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.factory;

import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.out.ExcelWriter;
import com.github.javaxcel.out.MapWriter;
import com.github.javaxcel.out.ModelWriter;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Factory for creating the appropriate implementation of {@link ExcelWriter}.
 * This will create instance of {@link ModelWriter} or {@link MapWriter}.
 */
public abstract class ExcelWriterFactory {

    private ExcelWriterFactory() {
    }

    /**
     * Returns instance of {@link MapWriter}.
     *
     * @param workbook excel workbook
     * @return {@link MapWriter}
     */
    public static <W extends Workbook, V> MapWriter<W, Map<String, V>> create(W workbook) {
        return instantiate(workbook);
    }

    /**
     * Returns instance of {@link ModelWriter}.
     *
     * @param workbook excel workbook
     * @param type     type of model
     * @return {@link ModelWriter}
     */
    public static <W extends Workbook, T> ModelWriter<W, T> create(W workbook, Class<T> type) {
        return instantiate(workbook, type);
    }

    /**
     * Instantiates {@link MapWriter}.
     *
     * @param workbook excel workbook
     * @param <W>      implementation of {@link Workbook}
     * @param <V>      {@link Map}'s value
     * @return {@link MapWriter}
     */
    @SuppressWarnings("unchecked")
    private static <W extends Workbook, V> MapWriter<W, Map<String, V>> instantiate(W workbook) {
        Constructor<?> constructor;
        try {
            constructor = findConstructor("com.github.javaxcel.out.MapWriter",
                    Workbook.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, Map.class);
        }

        MapWriter<W, Map<String, V>> writer;
        try {
            writer = (MapWriter<W, Map<String, V>>) constructor.newInstance(workbook);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", Map.class.getName()), e);
        }

        return writer;
    }

    /**
     * Instantiates {@link ModelWriter}.
     *
     * @param workbook excel workbook
     * @param type     type of model
     * @param <W>      implementation of {@link Workbook}
     * @param <T>      type of the element
     * @return {@link ModelWriter}
     */
    @SuppressWarnings("unchecked")
    private static <W extends Workbook, T> ModelWriter<W, T> instantiate(W workbook, Class<T> type) {
        Constructor<?> constructor;
        try {
            constructor = findConstructor("com.github.javaxcel.out.ModelWriter",
                    Workbook.class, Class.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, type);
        }

        ModelWriter<W, T> writer;
        try {
            writer = (ModelWriter<W, T>) constructor.newInstance(workbook, type);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) throw new IllegalArgumentException(e);
            if (e.getCause() instanceof NoTargetedFieldException) throw new NoTargetedFieldException(e);
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", type.getName()), e);
        }

        return writer;
    }

    private static Constructor<?> findConstructor(String className, Class<?>... paramTypes)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName(className, true, ExcelWriterFactory.class.getClassLoader());
        Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);

        return constructor;
    }

}
