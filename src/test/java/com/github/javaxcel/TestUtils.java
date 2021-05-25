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
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import com.github.javaxcel.util.TypeClassifier;
import io.github.imsejin.common.util.MathUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    public static final String MAP_KEY_PREFIX = "FIELD_";

    private static final EasyRandom generator;

    private static final Class<?>[] classes = Stream.of(
            TypeClassifier.Types.PRIMITIVE.getClasses(),
            TypeClassifier.Types.WRAPPER.getClasses(),
            TypeClassifier.Types.DATETIME.getClasses(),
            new Class[]{String.class})
            .flatMap(Arrays::stream).toArray(Class[]::new);

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

    public static Map<String, Object> randomMap(int numOfEntries) {
        if (numOfEntries < 0) throw new IllegalArgumentException("Number of entries cannot be negative");

        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < numOfEntries; i++) {
            int index = generator.nextInt(classes.length);
            Object randomized = randomize(classes[index]);

            // For abundant test cases, some empty strings will be converted to null.
            if (MathUtils.isOdd(i) && "".equals(randomized)) {
                randomized = null;
            }

            String key = MAP_KEY_PREFIX + (i + 1);
            map.put(key, randomized);
        }

        return map;
    }

    public static List<Map<String, Object>> getRandomMaps(int size, int numOfEntries) {
        if (size < 0) throw new IllegalArgumentException("Size cannot be negative");
        if (numOfEntries < 0) throw new IllegalArgumentException("Number of entries cannot be negative");

        return IntStream.range(0, size).parallel()
                .mapToObj(i -> randomMap(numOfEntries)).collect(toList());
    }

    @Target(FIELD)
    @Retention(RUNTIME)
    public @interface Unrandomized {
    }

    ///////////////////////////////////////////////////////////////////////////////////////

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

    public static void assertEqualsNumOfModels(Workbook workbook, List<?> models) {
        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as("The number of actually written rows is equal to the number of models")
                .isEqualTo(models.size());
    }

    public static void assertEqualsNumOfModels(Workbook workbook, List<?> models, String description) {
        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as(description)
                .isEqualTo(models.size());
    }

    public static void assertEqualsNumOfModels(Workbook workbook, List<?> models, String description, Object... args) {
        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as(description, args)
                .isEqualTo(models.size());
    }

    public static void assertEqualsHeaderSize(Workbook workbook, Class<?> type) {
        assertThat((double) FieldUtils.getTargetedFields(type).size())
                .as("Header size is equal to the number of targeted fields in '%s'", type.getSimpleName())
                .isEqualTo(ExcelUtils.getSheets(workbook).stream()
                        .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                        .average().orElse(-1));
    }

    public static void assertEqualsHeaderSize(Workbook workbook, Class<?> type, String description) {
        assertThat((double) FieldUtils.getTargetedFields(type).size())
                .as(description)
                .isEqualTo(ExcelUtils.getSheets(workbook).stream()
                        .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                        .average().orElse(-1));
    }

    public static void assertEqualsHeaderSize(Workbook workbook, Class<?> type, String description, Object... args) {
        assertThat((double) FieldUtils.getTargetedFields(type).size())
                .as(description, args)
                .isEqualTo(ExcelUtils.getSheets(workbook).stream()
                        .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                        .average().orElse(-1));
    }

}
