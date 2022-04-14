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

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ExcelModelConstructorResolutionTester {

    public static class PublicNoArgsConstructor {
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public static class PublicConstructor {
        private final Integer id;
        private final String title;
        private final URL url;
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProtectedConstructor {
        private final String name;
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class PackagePrivateConstructor {
        private final String name;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PrivateConstructor {
        private final String name;
    }

    public static class ConstructorArgsWithoutOrder {
        private final String numeric;
        private final String name;
        private final String path;

        public ConstructorArgsWithoutOrder(String name, String path, String numeric) {
            this.numeric = numeric;
            this.name = name;
            this.path = path;
        }
    }

    public static class LackOfConstructorArgsWithoutOrder {
        private final long number;
        private final String name;
        private final Path path;

        public LackOfConstructorArgsWithoutOrder(String name) throws IOException {
            this.number = new Random().nextLong();
            this.name = name;
            this.path = Paths.get(".").toRealPath();
        }
    }

    public static class ParamNameDoesNotMatchFieldNameButBothTypeIsUnique {
        private final BigInteger bigInteger;
        private final BigDecimal bigDecimal;

        private ParamNameDoesNotMatchFieldNameButBothTypeIsUnique(BigInteger bigInt, BigDecimal decimal) {
            this.bigInteger = bigInt;
            this.bigDecimal = decimal;
        }
    }

    public static class NoMatchFieldNameButOtherFieldIsIgnored {
        private String[] strings;
        @ExcelIgnore
        private String[] dummies;

        public NoMatchFieldNameButOtherFieldIsIgnored(@FieldName("dummy") String[] strings) {
            this.strings = strings;
        }
    }

    @ExcelModel(explicit = true)
    public static class NoMatchFieldNameButFieldIsExplicit {
        @ExcelColumn
        private String[] strings;
        private String[] dummies;

        public NoMatchFieldNameButFieldIsExplicit(@FieldName("dummy") String[] strings) {
            this.strings = strings;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AllConstructorsAreNotAnnotated {
        private AccessMode accessMode;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    @AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    public static class ConstructorsAreAnnotated {
        private Object object;
        private List<String> strings;
    }

    public static class NoMatchFieldType {
        private final AccessMode accessMode;

        public NoMatchFieldType(TimeUnit accessMode) {
            this.accessMode = AccessMode.READ;
        }
    }

    public static class EmptyFieldName {
        private byte[] bytes;
        private byte[] dummies; // If this field doesn't exist, will be passes on test.

        public EmptyFieldName(@FieldName("") byte[] bytes) {
            this.bytes = bytes;
        }
    }

    public static class NoMatchFieldName {
        private String[] strings;
        private String[] dummies; // If this field doesn't exist, will be passes on test.

        public NoMatchFieldName(@FieldName("dummy") String[] strings) {
            this.strings = strings;
        }
    }

    public static class DuplicatedFieldName {
        private final char[] chars;
        private final char[] characters;

        public DuplicatedFieldName(@FieldName("chars") char[] chars, @FieldName("chars") char[] characters) {
            this.chars = chars;
            this.characters = characters;
        }
    }

    public static class NoMatchFieldTypeAndName {
        private final AccessMode accessMode;
        private TimeUnit minute;
        private TimeUnit second;

        public NoMatchFieldTypeAndName(TimeUnit accessMode, AccessMode minute) {
            this.accessMode = AccessMode.EXECUTE;
        }
    }

    public static class NoMatchFieldTypeAndNameWithAnnotation {
        private AccessMode read;
        private AccessMode write;
        private TimeUnit timeUnit;

        public NoMatchFieldTypeAndNameWithAnnotation(AccessMode read, @FieldName("timeUnit") AccessMode write) {
            this.read = read;
            this.write = write;
        }
    }

}
