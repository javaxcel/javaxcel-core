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

package com.github.javaxcel;

import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.handler.registry.impl.StrictExcelTypeHandlerRegistry;
import com.github.javaxcel.in.core.ExcelReader;
import com.github.javaxcel.in.core.impl.MapReader;
import com.github.javaxcel.in.core.impl.ModelReader;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.core.impl.MapWriter;
import com.github.javaxcel.out.core.impl.ModelWriter;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Factory for creating the appropriate implementation of {@link ExcelWriter} and {@link ExcelReader}.
 *
 * @see ModelWriter
 * @see MapWriter
 * @see ModelReader
 * @see MapReader
 */
public class Javaxcel {

    private final ExcelTypeHandlerRegistry registry;

    private Javaxcel(ExcelTypeHandlerRegistry registry) {
        this.registry = registry;
    }

    public static Javaxcel newInstance() {
        return new Javaxcel(new StrictExcelTypeHandlerRegistry());
    }

    public static Javaxcel newInstance(ExcelTypeHandlerRegistry registry) {
        return new Javaxcel(registry);
    }

    /**
     * Returns a new instance of implementation of {@link ExcelWriter}.
     *
     * @param workbook Excel workbook
     * @param type     type of model
     * @param <T>      type
     * @return implementation that can handle the given type when you write
     */
    public <T> ExcelWriter<T> writer(Workbook workbook, Class<T> type) {
        return new ModelWriter<>(workbook, type, this.registry);
    }

    /**
     * Returns a new instance of implementation of {@link ExcelWriter}.
     *
     * @param workbook Excel workbook
     * @return implementation that can handle {@link Map} when you write
     */
    public ExcelWriter<Map<String, Object>> writer(Workbook workbook) {
        return new MapWriter(workbook);
    }

    /**
     * Returns a new instance of implementation of {@link ExcelReader}.
     *
     * @param workbook Excel workbook
     * @param type     type of model
     * @param <T>      type
     * @return implementation that can handle the given type when you read
     */
    public <T> ExcelReader<T> reader(Workbook workbook, Class<T> type) {
        return new ModelReader<>(workbook, type, this.registry);
    }

    /**
     * Returns a new instance of implementation of {@link ExcelReader}.
     *
     * @param workbook Excel workbook
     * @return implementation that can handle {@link Map} when you read
     */
    public ExcelReader<Map<String, Object>> reader(Workbook workbook) {
        return new MapReader(workbook);
    }

}
