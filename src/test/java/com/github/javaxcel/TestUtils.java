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

package com.github.javaxcel;

import com.github.javaxcel.annotation.ExcelIgnore;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    private static final EasyRandom generator;

    static {
        EasyRandomParameters parameters =
                new EasyRandomParameters()
                        .charset(StandardCharsets.UTF_8)
                        .dateRange(LocalDate.of(1000, Month.JANUARY, 1), LocalDate.now())
                        .timeRange(LocalTime.MIN, LocalTime.MAX)
                        .stringLengthRange(0, 15)
                        .collectionSizeRange(0, 10)
                        .excludeField(field -> field.getAnnotation(Unrandomized.class) != null || field.getAnnotation(ExcelIgnore.class) != null)
                        .overrideDefaultInitialization(false)
                        .scanClasspathForConcreteTypes(true);
        generator = new EasyRandom(parameters);
    }

    public static <T> T randomize(Class<? extends T> type) {
        return generator.nextObject(type);
    }

    public static <T> List<T> getMocks(Class<? extends T> type, int size) {
        if (size < 0) throw new IllegalArgumentException("Size cannot be negative");

        return IntStream.range(0, size).parallel()
                       .mapToObj(i -> randomize(type)).collect(toList());
    }

    public static void assertNotEmptyFile(File file) {
        assertNotEmptyFile(file, "File must be created and have content");
    }

    public static void assertNotEmptyFile(File file, String description) {
        assertNotEmptyFile(file, description, (Object) null);
    }

    public static void assertNotEmptyFile(File file, String description, Object... args) {
        assertThat(file)
                .as(description, args)
                .isNotNull().exists().canRead().isNotEmpty();
    }

    @Target(FIELD)
    @Retention(RUNTIME)
    public @interface Unrandomized {
    }

}
