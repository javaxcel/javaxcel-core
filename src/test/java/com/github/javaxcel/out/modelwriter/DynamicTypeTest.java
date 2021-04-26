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

package com.github.javaxcel.out.modelwriter;

import com.github.javaxcel.out.ModelWriterTester;
import com.github.javaxcel.TestUtils;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.ToStringMethod;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.github.javaxcel.TestUtils.*;

@StopwatchProvider
class DynamicTypeTest extends ModelWriterTester {

    @Test
    void test(@TempDir Path path, Stopwatch stopwatch) throws Exception {
        Class<?> dynamicType = createDynamicType();
        String filename = dynamicType.getSimpleName().toLowerCase() + '.' + ExcelUtils.EXCEL_2007_EXTENSION;
        File file = new File(path.toFile(), filename);

        run(file, dynamicType, stopwatch);
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        Class<?> type = givenModel.getType();
        List<?> models = thenModel.getModels();

        assertNotEmptyFile(file);

        @Cleanup Workbook workbook = WorkbookFactory.create(file);
        assertEqualsNumOfModels(workbook, models, "The number of actually written rows is %,d", models.size());
        assertEqualsHeaderSize(workbook, type, "Header size is equal to the number of targeted fields in '%s'", type.getSimpleName());
    }

    private Class<?> createDynamicType() {
        AnnotationDescription unrandomized = AnnotationDescription.Builder
                .ofType(TestUtils.Unrandomized.class).build();

        return new ByteBuddy()
                .subclass(Object.class)
                .name("DynamicModel")
                .defineField("id", Long.class, Visibility.PRIVATE)
                .defineField("name", String.class, Visibility.PRIVATE)
                .defineField("uuid", UUID.class, Visibility.PRIVATE)
                .annotateField(unrandomized)
                .defineMethod("toString", String.class, Visibility.PUBLIC)
                .intercept(ToStringMethod.prefixedBySimpleClassName())
                .make().load(getClass().getClassLoader()).getLoaded();
    }

}
