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

import java.lang.reflect.Field;

public class GettingFieldValueException extends JavaxcelException {

    private final Class<?> type;

    private final transient Field field;

    public GettingFieldValueException(Class<?> type, Field field) {
        super("Failed to get value in the field(%s) of the class(%s)", field.getName(), type.getName());
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(Class<?> type, Field field, String format, Object... args) {
        super(format, args);
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(Class<?> type, Field field, Throwable cause) {
        super(cause, "Failed to get value in the field(%s) of the class(%s)", field.getName(), type.getName());
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(Class<?> type, Field field, Throwable cause, String format, Object... args) {
        super(cause, format, args);
        this.type = type;
        this.field = field;
    }

    public Class<?> getType() {
        return type;
    }

    public Field getField() {
        return field;
    }

}
