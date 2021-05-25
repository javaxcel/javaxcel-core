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

class NoTargetedConstructorExceptionTest {

    @Test
    @DisplayName("NoTargetedConstructorException(Class)")
    void test0() {
        // given
        Class<TestModel> type = TestModel.class;

        // when
        NoTargetedConstructorException exception = new NoTargetedConstructorException(type);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(NoTargetedConstructorException.class)
                .hasMessage("Cannot find a default constructor in the class(%s)", type.getName());
        assertThat(exception.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("NoTargetedConstructorException(Class, String, Object...)")
    void test1() {
        // given
        String message = "Exception of failure of finding constructor";
        Class<TestModel> type = TestModel.class;

        // when
        NoTargetedConstructorException exception = new NoTargetedConstructorException(type, message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(NoTargetedConstructorException.class)
                .hasMessage(message);
        assertThat(exception.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("NoTargetedConstructorException(Class, Throwable)")
    void test2() {
        // given
        Throwable cause = new RuntimeException("Exception of failure of finding constructor");
        Class<TestModel> type = TestModel.class;

        // when
        NoTargetedConstructorException exception = new NoTargetedConstructorException(type, cause);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(NoTargetedConstructorException.class)
                .hasCause(cause)
                .hasMessage("Cannot find a default constructor in the class(%s)", type.getName());
        assertThat(exception.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("NoTargetedConstructorException(Class, Throwable, String, Object...)")
    void test3() {
        // given
        String message = "Exception of failure of finding constructor";
        Throwable cause = new RuntimeException(message);
        Class<TestModel> type = TestModel.class;

        // when
        NoTargetedConstructorException exception = new NoTargetedConstructorException(type, cause, message);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(NoTargetedConstructorException.class)
                .hasCause(cause)
                .hasMessage(message);
        assertThat(exception.getType()).isEqualTo(type);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class TestModel {
        private Long id;
        private String name;
    }

}
