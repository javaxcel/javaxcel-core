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

class UnsupportedWorkbookExceptionTest {

    @Test
    @DisplayName("UnsupportedWorkbookException(String, Object...)")
    void test0() {
        // given
        String message = "SXSSFWorkbook is not supported workbook";

        // when
        UnsupportedWorkbookException exception = new UnsupportedWorkbookException(message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(UnsupportedWorkbookException.class)
                .hasMessage(message);
    }

    @Test
    @DisplayName("UnsupportedWorkbookException(Throwable, String, Object...)")
    void test1() {
        // given
        String message = "SXSSFWorkbook is not supported workbook";
        Throwable cause = new RuntimeException(message);

        // when
        UnsupportedWorkbookException exception = new UnsupportedWorkbookException(cause, message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(UnsupportedWorkbookException.class)
                .hasCause(cause)
                .hasMessage(message);
    }

}
