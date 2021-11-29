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

package com.github.javaxcel.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WritingExcelExceptionTest {

    @Test
    @DisplayName("WritingExcelException()")
    void test0() {
        // when
        WritingExcelException exception = new WritingExcelException();

        // then
        assertThat(exception)
                .isExactlyInstanceOf(WritingExcelException.class)
                .hasMessage("Failed to write data to the Excel sheet");
    }

    @Test
    @DisplayName("WritingExcelException(String, Object...)")
    void test1() {
        // given
        String message = "Exception of failure of writing Excel file";

        // when
        WritingExcelException exception = new WritingExcelException(message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(WritingExcelException.class)
                .hasMessage(message);
    }

    @Test
    @DisplayName("WritingExcelException(Throwable)")
    void test2() {
        // given
        Throwable cause = new RuntimeException("Exception of failure of writing Excel file");

        // when
        WritingExcelException exception = new WritingExcelException(cause);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(WritingExcelException.class)
                .hasCause(cause)
                .hasMessage("Failed to write data to the Excel sheet");
    }

    @Test
    @DisplayName("WritingExcelException(Throwable, String, Object...)")
    void test3() {
        // given
        String message = "Exception of failure of writing Excel file";
        Throwable cause = new RuntimeException(message);

        // when
        WritingExcelException exception = new WritingExcelException(cause, message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(WritingExcelException.class)
                .hasCause(cause)
                .hasMessage(message);
    }

}
