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

import com.github.javaxcel.util.ConstructorUtilsSpec;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.file.AccessMode;
import java.util.List;

/**
 * @see ConstructorUtilsSpec
 */
public class ConstructorModel {

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    public static class PublicConstructor {
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProtectedConstructor {
        private AccessMode accessMode;
    }

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class PackagePrivateConstructor {
        private Object object;
        private List<String> strings;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PrivateConstructor {
        private byte[] bytes;
        private char[] characters;
        private String string;
    }

}
