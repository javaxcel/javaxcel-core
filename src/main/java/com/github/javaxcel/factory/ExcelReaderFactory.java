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

import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.handler.registry.impl.ExcelTypeHandlerRegistryImpl;
import com.github.javaxcel.in.core.ExcelReader;
import com.github.javaxcel.in.core.impl.MapReader;
import com.github.javaxcel.in.core.impl.ModelReader;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Factory for creating the appropriate implementation of {@link ExcelReader}.
 * This will create instance of {@link ModelReader} or {@link MapReader}.
 */
public abstract class ExcelReaderFactory {

    private final ExcelTypeHandlerRegistry registry = new ExcelTypeHandlerRegistryImpl();

    private ExcelReaderFactory() {
    }

    public static ExcelReaderFactory init() {
        return new ExcelReaderFactoryImpl();
    }

    public ExcelReaderFactory registry(ExcelTypeHandlerRegistry registry) {
        this.registry.addAll(registry);
        return this;
    }

    /**
     * Returns instance of {@link ModelReader}.
     *
     * @param workbook Excel workbook
     * @param type     type of model
     * @param <T>      type of the element
     * @return {@link ModelReader}
     */
    public <T> ExcelReader<T> create(Workbook workbook, Class<T> type) {
        return new ModelReader<>(workbook, type, this.registry);
    }

    /**
     * Returns instance of {@link MapReader}.
     *
     * @param workbook Excel workbook
     * @return {@link MapReader}
     */
    public static ExcelReader<Map<String, Object>> create(Workbook workbook) {
        return new MapReader(workbook);
    }

    private static class ExcelReaderFactoryImpl extends ExcelReaderFactory {
    }

}
