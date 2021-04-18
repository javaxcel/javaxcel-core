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

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface WritingConverter<T> {

    /**
     * Converts a field's value to the string.
     *
     * <p> To write a value in the cell, this converts a field's value
     * to the string. The converted string will be written in cell.
     *
     * @param model element in list
     * @param field field of model
     * @return stringified field's value
     */
    @Nullable
    String convert(T model, Field field);

}
