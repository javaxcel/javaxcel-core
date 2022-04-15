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

class AmbiguousExcelModelCreatorExceptionTest {

    @Test
    @DisplayName("NoTargetedConstructorException(String, Object...)")
    void test0() {
        // given
        String message = "Failed to find the targeted constructor";

        // when
        AmbiguousExcelModelCreatorException exception = new AmbiguousExcelModelCreatorException(message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(AmbiguousExcelModelCreatorException.class)
                .hasMessage(message);
    }

    @Test
    @DisplayName("NoTargetedConstructorException(Throwable, String, Object...)")
    void test1() {
        // given
        String message = "NoTargetedConstructorException: ";
        Throwable cause = new RuntimeException("Failed to find the targeted constructor");

        // when
        AmbiguousExcelModelCreatorException exception = new AmbiguousExcelModelCreatorException(cause, message, Object.class);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(AmbiguousExcelModelCreatorException.class)
                .hasCause(cause)
                .hasMessageStartingWith(message);
    }

}
