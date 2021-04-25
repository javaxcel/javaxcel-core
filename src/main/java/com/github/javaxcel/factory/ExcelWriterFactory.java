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

import com.github.javaxcel.out.ExcelWriter;
import com.github.javaxcel.out.MapWriter;
import com.github.javaxcel.out.ModelWriter;
import org.apache.poi.ss.usermodel.Workbook;

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
     * @param <W>      implementation of {@link Workbook}
     * @param <V>      {@link Map}'s value
     * @return {@link MapWriter}
     */
    public static <W extends Workbook, V> MapWriter<W, Map<String, V>> create(W workbook) {
        return new MapWriter<>(workbook);
    }

    /**
     * Returns instance of {@link ModelWriter}.
     *
     * @param workbook excel workbook
     * @param type     type of model
     * @param <W>      implementation of {@link Workbook}
     * @param <T>      type of the element
     * @return {@link ModelWriter}
     */
    public static <W extends Workbook, T> ModelWriter<W, T> create(W workbook, Class<T> type) {
        return new ModelWriter<>(workbook, type);
    }

}
