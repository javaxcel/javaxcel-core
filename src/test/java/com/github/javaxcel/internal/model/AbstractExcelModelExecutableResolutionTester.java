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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class AbstractExcelModelExecutableResolutionTester {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    public static class AnnotatedConstructorAndMethod {
        private final BigInteger bigInteger;
        private final BigDecimal bigDecimal;

        @ExcelModelCreator
        public static AnnotatedConstructorAndMethod of(BigInteger bigInt, BigDecimal decimal) {
            return new AnnotatedConstructorAndMethod(bigInt, decimal);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConstructorsAndAnnotatedMethod {
        private BigInteger bigInteger;
        private BigDecimal bigDecimal;

        @ExcelModelCreator
        public static ConstructorsAndAnnotatedMethod newInstance() {
            Random random = new Random();
            BigInteger bigInteger = BigInteger.valueOf(random.nextLong());
            BigDecimal bigDecimal = BigDecimal.valueOf(random.nextDouble());

            return new ConstructorsAndAnnotatedMethod(bigInteger, bigDecimal);
        }
    }

}
