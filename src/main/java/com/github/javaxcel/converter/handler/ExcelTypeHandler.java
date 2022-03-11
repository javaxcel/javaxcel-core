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

package com.github.javaxcel.converter.handler;

/**
 * Handler for type to convert to string and from string.
 *
 * @param <T> handled type
 */
public interface ExcelTypeHandler<T> {

    /**
     * Returns type handled by this handler.
     *
     * @return handled type
     */
    Class<T> getType();

    /**
     * Stringifies the value with arguments to write in Excel file.
     *
     * @param value object value
     * @param args  optional arguments
     * @return stringified value
     * @throws Exception if failed to handle the value
     */
    String write(Object value, Object... args) throws Exception;

    /**
     * Instantiates the handled type with string value read from Excel file
     * and arguments.
     *
     * @param value string value
     * @param args  optional arguments
     * @return stringified value
     * @throws Exception if failed to handle the value
     */
    T read(String value, Object... args) throws Exception;

}
