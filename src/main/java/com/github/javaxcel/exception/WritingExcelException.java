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

package com.github.javaxcel.exception;

public class WritingExcelException extends JavaxcelException {

    public WritingExcelException() {
        super("Failed to write data to the excel sheet");
    }

    public WritingExcelException(String format, Object... args) {
        super(format, args);
    }

    public WritingExcelException(Throwable cause) {
        super(cause, "Failed to write data to the excel sheet");
    }

    public WritingExcelException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

}
