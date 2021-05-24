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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class SettingFieldValueExceptionTest {

    @Test
    void test0() throws NoSuchFieldException {
        // given
        Class<TestModel> type = TestModel.class;
        Field field = type.getDeclaredField("id");

        // when
        SettingFieldValueException exception = new SettingFieldValueException(type, field);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(SettingFieldValueException.class)
                .hasMessage("Failed to set value into the field(%s) of the class(%s)", field.getName(), type.getName());
        assertThat(exception.getType()).isEqualTo(type);
        assertThat(exception.getField()).isEqualTo(field);
    }

    @Test
    void test1() throws NoSuchFieldException {
        // given
        String message = "Exception of setting field value";
        Class<TestModel> type = TestModel.class;
        Field field = type.getDeclaredField("id");

        // when
        SettingFieldValueException exception = new SettingFieldValueException(message, type, field);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(SettingFieldValueException.class)
                .hasMessage(message);
        assertThat(exception.getType()).isEqualTo(type);
        assertThat(exception.getField()).isEqualTo(field);
    }

    @Test
    void test2() throws NoSuchFieldException {
        // given
        String message = "Exception of setting field value";
        Throwable cause = new RuntimeException(message);
        Class<TestModel> type = TestModel.class;
        Field field = type.getDeclaredField("id");

        // when
        SettingFieldValueException exception = new SettingFieldValueException(message, cause, type, field);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(SettingFieldValueException.class)
                .hasCause(cause)
                .hasMessage(cause.getMessage());
        assertThat(exception.getType()).isEqualTo(type);
        assertThat(exception.getField()).isEqualTo(field);
    }

    @Test
    void test3() throws NoSuchFieldException {
        // given
        Throwable cause = new RuntimeException("Exception of setting field value");
        Class<TestModel> type = TestModel.class;
        Field field = type.getDeclaredField("id");

        // when
        SettingFieldValueException exception = new SettingFieldValueException(cause, type, field);

        // then
        assertThat(exception)
                .isExactlyInstanceOf(SettingFieldValueException.class)
                .hasCause(cause)
                .hasMessage("Failed to set value into the field(%s) of the class(%s)", field.getName(), type.getName());
        assertThat(exception.getType()).isEqualTo(type);
        assertThat(exception.getField()).isEqualTo(field);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static class TestModel {
        private Long id;
        private String name;
    }

}
