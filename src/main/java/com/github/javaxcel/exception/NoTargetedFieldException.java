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

public class NoTargetedFieldException extends RuntimeException {

    private final Class<?> type;

    public NoTargetedFieldException(Throwable cause) {
        super(cause);
        this.type = null;
    }

    public NoTargetedFieldException(Class<?> type) {
        super(String.format("Cannot find the targeted fields in the class(%s)", type.getName()));
        this.type = type;
    }

    public NoTargetedFieldException(String message, Class<?> type) {
        super(message);
        this.type = type;
    }

    public NoTargetedFieldException(String message, Throwable cause, Class<?> type) {
        super(message, cause);
        this.type = type;
    }

    public NoTargetedFieldException(Throwable cause, Class<?> type) {
        super(String.format("Cannot find the targeted fields in the class(%s)", type.getName()), cause);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

}
