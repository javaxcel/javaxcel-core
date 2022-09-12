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

/**
 * Context that has objects used on writing Excel file.
 *
 * @param <T> model type
 */
public class ExcelWriteContext<T> {

    private final Workbook workbook;
    private final Class<T> modelType;
    private final Class<? extends ExcelWriter<T>> writerType;

    /**
     * Strategies for writing Excel file.
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

    public ExcelWriteContext(Workbook workbook, Class<T> modelType, Class<? extends ExcelWriter<T>> writerType) {
        Asserts.that(workbook)
                .describedAs("ExcelWriteContext.workbook is not allowed to be null")
                .isNotNull();
        Asserts.that(modelType)
                .describedAs("ExcelWriteContext.modelType is not allowed to be null")
                .isNotNull();
        Asserts.that(writerType)
                .describedAs("ExcelWriteContext.writerType is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteContext.writerType is type of implementation of ExcelWriter, but it isn't : '{0}'", writerType.getName())
                .predicate(ExcelWriter.class::isAssignableFrom);

        this.workbook = workbook;
        this.modelType = modelType;
        this.writerType = writerType;
    }

    @Nonnull
    public Workbook getWorkbook() {
        return this.workbook;
    }

    @Nonnull
    public Class<T> getModelType() {
        return this.modelType;
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
                .describedAs("ExcelWriteContext.strategyMap is not allowed to be null")
                .isNotNull()
                .describedAs("ExcelWriteContext.strategyMap.values is not allowed to contain null: {0}", strategyMap)
                .asValues().doesNotContainNull();

        this.strategyMap = strategyMap;
    }

    @Nonnull
    public List<T> getList() {
        return this.list;
    }

    public void setList(List<T> list) {
        Asserts.that(list)
                .describedAs("ExcelWriteContext.list is not allowed to be null")
                .isNotNull();

        this.list = list;
    }

    @Nonnull
    public List<T> getChunk() {
        return this.chunk;
    }

    public void setChunk(List<T> chunk) {
        Asserts.that(chunk)
                .describedAs("ExcelWriteContext.chunk is not allowed to be null")
                .isNotNull();

        this.chunk = chunk;
    }

    @Nonnull
    public Sheet getSheet() {
        return this.sheet;
    }

    public void setSheet(Sheet sheet) {
        Asserts.that(chunk)
                .describedAs("ExcelWriteContext.sheet is not allowed to be null")
                .isNotNull();

        this.sheet = sheet;
    }

    public List<CellStyle> getHeaderStyles() {
        return this.headerStyles;
    }

    public void setHeaderStyles(List<CellStyle> headerStyles) {
        Asserts.that(headerStyles)
                .describedAs("ExcelWriteContext.headerStyles is not allowed to be null or empty: {0}", headerStyles)
                .isNotNull().isNotEmpty();

        this.headerStyles = headerStyles;
    }

    public List<CellStyle> getBodyStyles() {
        return this.bodyStyles;
    }

    public void setBodyStyles(List<CellStyle> bodyStyles) {
        Asserts.that(bodyStyles)
                .describedAs("ExcelWriteContext.bodyStyles is not allowed to be null or empty: {0}", bodyStyles)
                .isNotNull().isNotEmpty();

        this.bodyStyles = bodyStyles;
    }

}
