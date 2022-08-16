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

package com.github.javaxcel.out.core;

import com.github.javaxcel.exception.WritingExcelException;
import com.github.javaxcel.out.context.ExcelWriteContext;
import com.github.javaxcel.out.lifecycle.ExcelWriteLifecycle;
import com.github.javaxcel.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.out.strategy.impl.SheetName;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ArrayUtils;
import io.github.imsejin.common.util.CollectionUtils;
import io.github.imsejin.common.util.NumberUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractExcelWriter<T> implements ExcelWriter<T>, ExcelWriteLifecycle<T> {

    /**
     * Default cell style configuration to save memory.
     */
    protected static final ExcelStyleConfig DEFAULT_STYLE_CONFIG = new NoStyleConfig();

    private final ExcelWriteContext<T> context;

    @SuppressWarnings("unchecked")
    protected AbstractExcelWriter(Workbook workbook, Class<T> modelType) {
        Class<? extends ExcelWriter<T>> writerType = (Class<? extends ExcelWriter<T>>) getClass();
        this.context = new ExcelWriteContext<>(workbook, modelType, writerType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ExcelWriter<T> options(ExcelWriteStrategy... strategies) {
        Asserts.that(strategies)
                .as("strategies is not allowed to be null")
                .isNotNull()
                .as("strategies cannot have null element: {0}", ArrayUtils.toString(strategies))
                .doesNotContainNull();
        if (strategies.length == 0) return this;

        // Makes each strategy be unique; removes duplication.
        Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategyMap = Arrays.stream(strategies)
                .distinct().filter(it -> it.isSupported(this.context))
                .collect(toMap(ExcelWriteStrategy::getClass, Function.identity()));
        this.context.setStrategyMap(Collections.unmodifiableMap(strategyMap));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void write(OutputStream out, List<T> list) {
        this.context.setList(list);

        // Lifecycle method.
        prepare(this.context);

        Workbook workbook = this.context.getWorkbook();
        final int maxRows = ExcelUtils.getMaxRows(workbook) - 1; // Subtracts 1 because of header row.
        List<List<T>> chunkedList = CollectionUtils.partitionBySize(list, maxRows);
        final int numOfSheets = NumberUtils.toPositive(chunkedList.size());

        // Creates sheet names by this or implementation.
        List<String> sheetNames = createSheetNames(this.context, numOfSheets);
        Asserts.that(sheetNames)
                .as("sheetNames is not allowed to be null or empty: {0}", sheetNames)
                .isNotNull().hasElement()
                .as("sheetNames is not allowed to contain null: {0}", sheetNames)
                .doesNotContainNull()
                .as("sheetNames cannot have duplicated elements: {0}", sheetNames)
                .doesNotHaveDuplicates()
                .asSize().as("sheetNames.size is not equal to numOfSheets: (sheetName.size: {0}, numOfSheets: {1})",
                sheetNames.size(), numOfSheets)
                .isEqualTo(numOfSheets);

        for (int i = 0; i < numOfSheets; i++) {
            String sheetName = sheetNames.get(i);
            Sheet sheet = workbook.createSheet(sheetName);

            // To write 1 sheet at least, even if the list is empty.
            List<T> chunk = chunkedList.isEmpty() ? Collections.emptyList() : chunkedList.get(i);
            this.context.setChunk(chunk);
            this.context.setSheet(sheet);

            // Lifecycle method.
            preWriteSheet(this.context);

            createHeader(this.context);
            createBody(this.context);

            // Lifecycle method.
            postWriteSheet(this.context);
        }

        save(out);

        // Lifecycle method.
        complete(this.context);
    }

    /**
     * Saves models into an Excel file.
     *
     * @param out output stream
     */
    private void save(OutputStream out) {
        try {
            this.context.getWorkbook().write(out);
        } catch (IOException e) {
            throw new WritingExcelException(e);
        }
    }

    // Overridable -------------------------------------------------------------------------------------

    /**
     * Creates sheet names following with the below instructions.
     *
     * <ul>
     *     <li>Sheet names don't be null or empty.</li>
     *     <li>Sheet names don't have null element.</li>
     *     <li>Sheet names don't have duplicated elements.</li>
     *     <li>The number of sheet names is equal to numOfSheets.</li>
     * </ul>
     *
     * @param context     context with current sheet and chunked models.
     * @param numOfSheets the number of sheets
     * @return sheet names
     * @throws IllegalArgumentException if sheet names is null or empty
     */
    protected List<String> createSheetNames(ExcelWriteContext<T> context, int numOfSheets) {
        ExcelWriteStrategy strategy = context.getStrategyMap().get(SheetName.class);
        String sheetName = strategy == null ? "Sheet" : (String) strategy.execute(context);

        if (numOfSheets < 2) return Collections.singletonList(sheetName);

        List<String> sheetNames = new ArrayList<>();
        for (int i = 1; i <= numOfSheets; i++) {
            sheetNames.add(sheetName + i);
        }

        return Collections.unmodifiableList(sheetNames);
    }

    /**
     * Creates the first row as header for each sheet.
     *
     * @param context context with current sheet and chunked models
     */
    protected abstract void createHeader(ExcelWriteContext<T> context);

    /**
     * Creates the second row and below as body for each sheet.
     *
     * @param context context with current sheet and chunked models
     */
    protected abstract void createBody(ExcelWriteContext<T> context);

}
