/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.out.context;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExcelWriteContext<T> {

    private final Workbook workbook;
    private final Class<T> elementType;
    private final Class<? extends ExcelWriter<?>> writerType;

    /**
     * Strategies for writing excel file.
     * <p>
     * To prevent {@link NullPointerException} from being thrown,
     * initialize this field with empty map.
     */
    private Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap = Collections.emptyMap();
    private List<T> list;

    private Sheet sheet;
    private List<T> chunk;

    /**
     * @see ExcelColumn#headerStyle()
     * @see ExcelModel#headerStyle()
     */
    private List<CellStyle> headerStyles;

    /**
     * @see ExcelColumn#bodyStyle()
     * @see ExcelModel#bodyStyle()
     */
    private List<CellStyle> bodyStyles;

    public ExcelWriteContext(Workbook workbook, Class<T> elementType, Class<? extends ExcelWriter<?>> writerType) {
        Asserts.that(workbook)
                .as("ExcelWriteContext.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(elementType)
                .as("ExcelWriteContext.elementType is not allowed to be null")
                .isNotNull();
        Asserts.that(writerType)
                .as("ExcelWriteContext.writerType is not allowed to be null")
                .isNotNull();

        this.workbook = workbook;
        this.elementType = elementType;
        this.writerType = writerType;
    }

    @Nonnull
    public Workbook getWorkbook() {
        return this.workbook;
    }

    @Nonnull
    public Class<T> getElementType() {
        return this.elementType;
    }

    @Nonnull
    public Class<? extends ExcelWriter<?>> getWriterType() {
        return this.writerType;
    }

    @Nonnull
    public Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> getStrategyMap() {
        return this.strategyMap;
    }

    public void setStrategyMap(Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap) {
        Asserts.that(strategyMap)
                .as("ExcelWriteContext.strategyMap is not allowed to be null")
                .isNotNull()
                .as("ExcelWriteContext.strategyMap.values is not allowed to contain null: {0}", strategyMap)
                .asValues().doesNotContainNull();

        this.strategyMap = strategyMap;
    }

    @Nonnull
    public List<T> getList() {
        return this.list;
    }

    public void setList(List<T> list) {
        Asserts.that(list)
                .as("ExcelWriteContext.list is not allowed to be null")
                .isNotNull();

        this.list = list;
    }

    @Nonnull
    public List<T> getChunk() {
        return this.chunk;
    }

    public void setChunk(List<T> chunk) {
        Asserts.that(chunk)
                .as("ExcelWriteContext.chunk is not allowed to be null")
                .isNotNull();

        this.chunk = chunk;
    }

    @Nonnull
    public Sheet getSheet() {
        return this.sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public List<CellStyle> getHeaderStyles() {
        return this.headerStyles;
    }

    public void setHeaderStyles(List<CellStyle> headerStyles) {
        Asserts.that(headerStyles)
                .as("ExcelWriteContext.headerStyles is not allowed to be null or empty: {0}", headerStyles)
                .isNotNull().hasElement();

        this.headerStyles = headerStyles;
    }

    public List<CellStyle> getBodyStyles() {
        return this.bodyStyles;
    }

    public void setBodyStyles(List<CellStyle> bodyStyles) {
        Asserts.that(bodyStyles)
                .as("ExcelWriteContext.bodyStyles is not allowed to be null or empty: {0}", bodyStyles)
                .isNotNull().hasElement();

        this.bodyStyles = bodyStyles;
    }

}
