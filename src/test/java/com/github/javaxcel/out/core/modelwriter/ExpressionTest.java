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

package com.github.javaxcel.out.core.modelwriter;

import com.github.javaxcel.Converter;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriterExpression;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.KeyNames;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.out.core.ModelWriterTester;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see ExcelModel#includeSuper()
 * @see ExcelWriterExpression#value()
 */
@StopwatchProvider
class ExpressionTest extends ModelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        File file = new File(path.toFile(), "people.xlsx");

        run(file, Human.class, stopwatch);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.getFile());
        Workbook workbook = new SXSSFWorkbook();

        return new WhenModel(out, workbook, 4096);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        List<Human> models = (List<Human>) thenModel.getModels();

        assertNotEmptyFile(file);

        @Cleanup Workbook workbook = ExcelUtils.getWorkbook(file);
        assertCorrectParsed(workbook, models);
    }

    private static void assertCorrectParsed(Workbook workbook, List<Human> models) {
        int numOfFields = FieldUtils.getTargetedFields(Human.class).size();
        List<String> keyNames = IntStream.range(0, numOfFields)
                .mapToObj(Integer::toString).collect(toList());
        List<Map<String, Object>> list = ExcelReaderFactory.create(workbook)
                .options(new KeyNames(keyNames)).read();

        Map<String, List<Object>> columns = list.stream().flatMap(map -> map.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey,
                        collectingAndThen(toList(),
                                it -> it.stream().map(Map.Entry::getValue).collect(toList()))));

        // Kingdom
        assertThat(columns.get("0"))
                .as("Kingdom")
                .containsExactlyElementsOf(models.stream().map(Human::getKingdom)
                        .map(it -> it.toString().toLowerCase()).collect(toList()));
        // Sex
        assertThat(columns.get("1"))
                .as("Sex")
                .containsExactlyElementsOf(models.stream()
                        .map(it -> it.getKingdom().toString() +
                                it.getSex().toString()
                                        .replaceAll("(.+)", "/$1/"))
                        .collect(toList()));
        // Lifespan
        assertThat(columns.get("2"))
                .as("Lifespan")
                .containsExactlyElementsOf(models.stream().map(Human::getLifespan)
                        .map(it -> it > 1 ? it + " years" : it + " year").collect(toList()));
        // Name
        assertThat(columns.get("3"))
                .as("Name")
                .containsExactlyElementsOf(models.stream().map(Human::getName)
                        .map(it -> it.isEmpty() ? null : it).collect(toList()));
        // Birthday
        assertThat(columns.get("4"))
                .as("Birthday")
                .containsExactlyElementsOf(models.stream().map(Human::getBirthday)
                        .map(it -> it.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                        .collect(toList()));
        // Birth time
        assertThat(columns.get("5"))
                .as("Birth time")
                .containsExactlyElementsOf(models.stream().map(Human::getBirthTime)
                        .map(it -> it.format(DateTimeFormatter.ofPattern("HH/mm/ss.SSS")))
                        .collect(toList()));
        // Place of birth
        assertThat(columns.get("6"))
                .as("Place of birth")
                .containsExactlyElementsOf(models.stream().map(Human::getPlaceOfBirth)
                        .map(it -> Converter.capitalize(it.toString(), "-"))
                        .collect(toList()));
        // Rest seconds of life
        assertThat(columns.get("7"))
                .as("Rest seconds of life")
                .containsExactlyElementsOf(models.stream().map(Human::getRestSecondsOfLife)
                        .map(it -> it + " sec").collect(toList()));
        // Num of cells
        assertThat(columns.get("8"))
                .as("Num of cells")
                .containsExactlyElementsOf(models.stream().map(Human::getNumOfCells)
                        .map(it -> it + " cells/kg").collect(toList()));
        // Height
        assertThat(columns.get("9"))
                .as("Height")
                .containsExactlyElementsOf(models.stream().map(Human::getHeight)
                        .map(it -> it + " cm").collect(toList()));
        // Weight
        assertThat(columns.get("10"))
                .as("Weight")
                .containsExactlyElementsOf(models.stream().map(Human::getWeight)
                        .map(it -> it + " kg").collect(toList()));
        // Ages from birth to puberty
        assertThat(columns.get("11"))
                .as("Ages from birth to puberty")
                .containsExactlyElementsOf(models.stream().map(Human::getAgesFromBirthToPuberty)
                        .map(them -> Arrays.stream(them).boxed().collect(toList())
                                .toString().replaceAll("[\\[\\]]", ""))
                        .map(it -> it.isEmpty() ? null : it).collect(toList()));
        // Ages from twilight to death
        assertThat(columns.get("12"))
                .as("Ages from twilight to death")
                .containsExactlyElementsOf(models.stream().map(Human::getAgesFromTwilightToDeath)
                        .map(it -> Arrays.toString(it).replaceAll("\\[|]", "")).collect(toList()));
        // Disabled
        assertThat(columns.get("13"))
                .as("Disabled")
                .containsExactlyElementsOf(models.stream().map(Human::isDisabled)
                        .map(it -> it ? "yes" : "no").collect(toList()));
    }

}
