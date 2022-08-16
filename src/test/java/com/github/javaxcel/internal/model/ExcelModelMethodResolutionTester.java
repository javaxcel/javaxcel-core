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
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ExcelModelMethodResolutionTester {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PublicNoArgsMethod {
        private static final PublicNoArgsMethod INSTANCE = new PublicNoArgsMethod();

        @ExcelModelCreator
        public static PublicNoArgsMethod getInstance() {
            return INSTANCE;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class PublicMethod {
        private final Integer id;
        private final String title;
        private final URL url;

        @ExcelModelCreator
        public static PublicMethod of(Integer id, String title, URL url) {
            return new PublicMethod(id, title, url);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MethodArgsWithoutOrder {
        private final String numeric;
        private final String name;
        private final String path;

        @ExcelModelCreator
        public static MethodArgsWithoutOrder of(String name, String path, String numeric) {
            return new MethodArgsWithoutOrder(numeric, name, path);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LackOfMethodArgsWithoutOrder {
        private final long number;
        private final String name;
        private final Path path;

        @ExcelModelCreator
        public static LackOfMethodArgsWithoutOrder from(String name) throws IOException {
            return new LackOfMethodArgsWithoutOrder(new Random().nextLong(), name, Paths.get(".").toRealPath());
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParamNameDoesNotMatchFieldNameButBothTypeIsUnique {
        private final BigInteger bigInteger;
        private final BigDecimal bigDecimal;

        @ExcelModelCreator
        public static ParamNameDoesNotMatchFieldNameButBothTypeIsUnique of(BigInteger bigInt, BigDecimal decimal) {
            return new ParamNameDoesNotMatchFieldNameButBothTypeIsUnique(bigInt, decimal);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldNameButOtherFieldIsIgnored {
        private String[] strings;
        @ExcelIgnore
        private String[] dummies;

        @ExcelModelCreator
        public static NoMatchFieldNameButOtherFieldIsIgnored from(@FieldName("dummy") String[] strings) {
            return new NoMatchFieldNameButOtherFieldIsIgnored(strings, new String[0]);
        }
    }

    @ExcelModel(explicit = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldNameButFieldIsExplicit {
        @ExcelColumn
        private String[] strings;
        private String[] dummies;

        @ExcelModelCreator
        public static NoMatchFieldNameButFieldIsExplicit from(@FieldName("dummy") String[] strings) {
            return new NoMatchFieldNameButFieldIsExplicit(strings, new String[0]);
        }
    }

    // -------------------------------------------------------------------------------------------------

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public static class ProtectedMethod {
        private final Integer id;
        private final String title;
        private final URL url;

        @ExcelModelCreator
        protected static ProtectedMethod of(Integer id, String title, URL url) {
            return new ProtectedMethod(id, title, url);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PackagePrivateMethod {
        private final Integer id;
        private final String title;
        private final URL url;

        @ExcelModelCreator
        static PackagePrivateMethod of(Integer id, String title, URL url) {
            return new PackagePrivateMethod(id, title, url);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PrivateMethod {
        private final Integer id;
        private final String title;
        private final URL url;

        @ExcelModelCreator
        private static PrivateMethod of(Integer id, String title, URL url) {
            return new PrivateMethod(id, title, url);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AllMethodsAreNotAnnotated {
        private final AccessMode accessMode;

        public static AllMethodsAreNotAnnotated withRead() {
            return new AllMethodsAreNotAnnotated(AccessMode.READ);
        }

        public static AllMethodsAreNotAnnotated with(AccessMode accessMode) {
            return new AllMethodsAreNotAnnotated(accessMode);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MethodsAreAnnotated {
        private final AccessMode accessMode;

        public static MethodsAreAnnotated withRead() {
            return new MethodsAreAnnotated(AccessMode.READ);
        }

        @ExcelModelCreator
        public static MethodsAreAnnotated withWrite() {
            return new MethodsAreAnnotated(AccessMode.WRITE);
        }

        public static MethodsAreAnnotated withExecute() {
            return new MethodsAreAnnotated(AccessMode.EXECUTE);
        }

        @ExcelModelCreator
        public static MethodsAreAnnotated with(AccessMode accessMode) {
            return new MethodsAreAnnotated(accessMode);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InstanceMethod {
        private final AccessMode accessMode;

        @ExcelModelCreator
        public InstanceMethod readMode() {
            return new InstanceMethod(AccessMode.READ);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InvalidReturnType {
        private final AccessMode accessMode;

        @ExcelModelCreator
        public static Object writeMode() {
            return new InvalidReturnType(AccessMode.WRITE);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldType {
        private final AccessMode accessMode;

        @ExcelModelCreator
        public static NoMatchFieldType from(TimeUnit accessMode) {
            return new NoMatchFieldType(AccessMode.READ);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EmptyFieldName {
        private final byte[] bytes;
        private byte[] dummies; // If this field doesn't exist, will be passes on test.

        @ExcelModelCreator
        public static EmptyFieldName from(@FieldName("") byte[] bytes) {
            return new EmptyFieldName(bytes);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldName {
        private final String[] strings;
        private String[] dummies; // If this field doesn't exist, will be passes on test.

        @ExcelModelCreator
        public static NoMatchFieldName from(@FieldName("dummy") String[] strings) {
            return new NoMatchFieldName(strings);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DuplicatedFieldName {
        private final char[] chars;
        private final char[] characters;

        @ExcelModelCreator
        public static DuplicatedFieldName of(@FieldName("chars") char[] chars, @FieldName("chars") char[] characters) {
            return new DuplicatedFieldName(chars, characters);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldTypeAndName {
        private final AccessMode accessMode;
        private TimeUnit minute;
        private TimeUnit second;

        @ExcelModelCreator
        public static NoMatchFieldTypeAndName of(TimeUnit accessMode, AccessMode minute) {
            return new NoMatchFieldTypeAndName(AccessMode.EXECUTE);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoMatchFieldTypeAndNameWithAnnotation {
        private final AccessMode read;
        private final AccessMode write;
        private final TimeUnit timeUnit;

        @ExcelModelCreator
        public static NoMatchFieldTypeAndNameWithAnnotation of(AccessMode read, @FieldName("timeUnit") AccessMode write) {
            return new NoMatchFieldTypeAndNameWithAnnotation(read, write, null);
        }
    }

}
