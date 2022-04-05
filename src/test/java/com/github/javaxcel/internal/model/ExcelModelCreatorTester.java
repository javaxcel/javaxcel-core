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

package com.github.javaxcel.internal.model;

import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.file.AccessMode;
import java.util.List;

public class ExcelModelCreatorTester {

    public static class PublicNoArgs {
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class NotAnnotatedAllConstructors {
        private AccessMode accessMode;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    @AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    public static class AnnotatedConstructors {
        private Object object;
        private List<String> strings;
    }

    public static class InvalidFieldName {
        public InvalidFieldName(@FieldName("") byte[] bytes, char[] characters) {
            this.bytes = bytes;
            this.characters = characters;
        }

        private byte[] bytes;
        private char[] characters;
    }

}
