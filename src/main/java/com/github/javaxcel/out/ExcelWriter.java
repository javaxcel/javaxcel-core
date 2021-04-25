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

package com.github.javaxcel.out;

import com.github.javaxcel.styler.ExcelStyleConfig;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.util.List;

public interface ExcelWriter<W extends Workbook, T> {

    /**
     * Sets default value when value to be written is null or empty.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> defaultValue(String defaultValue);

    /**
     * Sets sheet name.
     *
     * @param sheetName sheet name
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> sheetName(String sheetName);

    /**
     * Sets header names.
     *
     * @param headerNames header names
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> headerNames(List<String> headerNames);

    /**
     * Disables to rotate sheet.
     *
     * <p> If this is invoked, excel file has only one sheet.
     *
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> unrotate();

    ///////////////////////////////////// Decoration //////////////////////////////////////

    /**
     * Sets style to header.
     *
     * @param config style config
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> headerStyle(ExcelStyleConfig config);

    /**
     * Sets styles to header.
     *
     * @param configs style configs
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> headerStyles(ExcelStyleConfig... configs);

    /**
     * Sets style to body.
     *
     * @param config style config
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> bodyStyle(ExcelStyleConfig config);

    /**
     * Sets styles to body.
     *
     * @param configs style configs
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> bodyStyles(ExcelStyleConfig... configs);

    ExcelWriter<W, T> autoResizeColumns();

    ExcelWriter<W, T> hideExtraRows();

    ExcelWriter<W, T> hideExtraColumns();

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Writes the data in the excel file.
     *
     * @param out  output stream for writing excel file
     * @param list list of models
     */
    void write(OutputStream out, List<T> list);

}
