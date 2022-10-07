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

package com.github.javaxcel.converter.in;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.converter.handler.impl.EnumTypeHandler;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry;
import com.github.javaxcel.internal.handler.TimeUnitTypeHandler;
import com.github.javaxcel.util.FieldUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.AccessMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see com.github.javaxcel.converter.handler.impl.EnumTypeHandler
 * @see DefaultExcelReadConverter
 */
class CustomEnumConversionTest {

    @Test
    void test() throws Exception {
        // given
        ExcelTypeHandlerRegistry registry = new DefaultExcelTypeHandlerRegistry();
        registry.add(TimeUnit.class, new TimeUnitTypeHandler());
        ExcelReadConverter converter = new DefaultExcelReadConverter(registry);

        Map<String, Field> fieldMap = FieldUtils.getTargetedFields(EnumModel.class)
                .stream().collect(toMap(Field::getName, Function.identity()));
        List<Map<String, String>> mocks = getMocks(10);

        // when
        List<EnumModel> models = new ArrayList<>();
        for (Map<String, String> mock : mocks) {
            AccessMode accessMode = (AccessMode) converter.convert(mock, fieldMap.get("accessMode"));
            TimeUnit timeUnit = (TimeUnit) converter.convert(mock, fieldMap.get("timeUnit"));

            models.add(new EnumModel(accessMode, timeUnit));
        }

        // then
        assertThat(mocks)
                .isNotNull().isNotEmpty()
                .as("If EnumModel.timeUnit is null, user-defined handler is not used.")
                .map(it -> it.get("timeUnit")).isNotEmpty().doesNotContainNull()
                .allMatch(TimeUnitTypeHandler::validate);
        assertThat(models)
                .isNotNull().isNotEmpty().hasSameSizeAs(mocks)
                .as("If EnumModel.accessMode is null, default enum handler is not used.")
                .map(it -> it.accessMode).isNotEmpty().doesNotContainNull();
        assertThat(models)
                .isNotNull().isNotEmpty().hasSameSizeAs(mocks)
                .as("If EnumModel.timeUnit is null, user-defined handler is not used.")
                .map(it -> it.timeUnit).isNotEmpty().doesNotContainNull();
    }

    private static List<Map<String, String>> getMocks(int size) throws Exception {
        List<EnumModel> models = TestUtils.getMocks(EnumModel.class, size);
        EnumTypeHandler enumHandler = new EnumTypeHandler();
        TimeUnitTypeHandler timeUnitHandler = new TimeUnitTypeHandler();

        List<Map<String, String>> mocks = new ArrayList<>();
        for (EnumModel model : models) {
            Map<String, String> mock = new HashMap<>();
            mock.put("accessMode", enumHandler.write(model.accessMode));
            mock.put("timeUnit", timeUnitHandler.write(model.timeUnit));

            mocks.add(mock);
        }

        return mocks;
    }

    // -------------------------------------------------------------------------------------------------

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class EnumModel {
        private AccessMode accessMode;
        private TimeUnit timeUnit;
    }

}
