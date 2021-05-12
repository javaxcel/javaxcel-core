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

import com.github.javaxcel.in.MapReader;
import com.github.javaxcel.in.ModelReader;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Factory for creating the appropriate implementation of {@link com.github.javaxcel.in.ExcelReader}.
 * This will create instance of {@link ModelReader} or {@link MapReader}.
 */
public abstract class ExcelReaderFactory {

    private ExcelReaderFactory() {
    }

    /**
     * Returns instance of {@link MapReader}.
     *
     * @param workbook excel workbook
     * @param <W>      implementation of {@link Workbook}
     * @param <V>      {@link Map}'s value
     * @return {@link MapReader}
     */
    public static <W extends Workbook, V> MapReader<W, Map<String, V>> create(W workbook) {
        return new MapReader<>(workbook);
    }

    /**
     * Returns instance of {@link ModelReader}.
     *
     * @param workbook excel workbook
     * @param type     type of model
     * @param <W>      implementation of {@link Workbook}
     * @param <T>      type of the element
     * @return {@link ModelReader}
     */
    public static <W extends Workbook, T> ModelReader<W, T> create(W workbook, Class<T> type) {
        return new ModelReader<>(workbook, type);
    }

}
