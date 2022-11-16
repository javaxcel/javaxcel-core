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

package com.github.javaxcel.converter.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Converter for writing Excel
 */
public interface ExcelWriteConverter {

    /**
     * Returns whether the field is supported by this converter.
     *
     * @param field field of model
     * @return whether the field is supported
     */
    boolean supports(Field field);

    /**
     * Converts a value of the field into a string.
     *
     * <p> If the value is null, it is converted into <b>default value</b>.
     * The priority is determined by the below policy orders.
     *
     * <ol>
     *     <li>{@link DefaultValue}</li>
     *     <li>{@link ExcelColumn#defaultValue()}</li>
     *     <li>{@link ExcelModel#defaultValue()}</li>
     * </ol>
     *
     * <p> To write a value to cell, the converter makes it turn into a string.
     * The converted string will be written to cell by {@link ExcelWriter}.
     *
     * @param model element in list
     * @param field field of model
     * @return stringified value of field or default value
     */
    @Nullable
    String convert(Object model, Field field);

}
