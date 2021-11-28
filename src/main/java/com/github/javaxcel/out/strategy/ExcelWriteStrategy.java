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

package com.github.javaxcel.out.strategy;

import com.github.javaxcel.out.$MapWriter;
import com.github.javaxcel.out.$ModelWriter;
import com.github.javaxcel.out.ExcelWriter;
import com.github.javaxcel.out.context.ExcelWriteContext;
import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.assertion.Asserts;
import org.apache.poi.ss.util.WorkbookUtil;

import java.util.*;

public interface ExcelWriteStrategy {

    boolean isSupported(ExcelWriteContext<?> context);

    Object execute(ExcelWriteContext<?> context);

    ///////////////////////////////////////////////////////////////////////////////////////

    class DefaultValue extends AbstractExcelWriteStrategy {
        private final String defaultValue;

        /**
         * Strategy for default value when value to be written is null or empty.
         *
         * @param defaultValue replacement of the value when it is null or empty string
         */
        public DefaultValue(String defaultValue) {
            Asserts.that(defaultValue)
                    .as("defaultValue is not allowed to be null or blank")
                    .isNotNull().hasText();

            this.defaultValue = defaultValue;
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.defaultValue;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class SheetName extends AbstractExcelWriteStrategy {
        private final String sheetName;

        /**
         * Strategy for sheet name.
         *
         * @param sheetName sheet name
         */
        public SheetName(String sheetName) {
            Asserts.that(sheetName)
                    .as("sheetName is not allowed to be null or blank, but it is: {0}", sheetName)
                    .isNotNull().hasText()
                    .as("sheetName is not allowed to contain invalid character: {0}", sheetName)
                    .predicate(it -> WorkbookUtil.createSafeSheetName(sheetName).equals(sheetName));

            this.sheetName = sheetName;
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.sheetName;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class HeaderNames extends AbstractExcelWriteStrategy {
        private final List<String> headerNames;

        public HeaderNames(List<String> headerNames) {
            Asserts.that(headerNames)
                    .as("headerNames is not allowed to be null or empty: {0}", headerNames)
                    .isNotNull().hasElement()
                    .as("headerNames cannot have null element: {0}", headerNames)
                    .doesNotContainNull()
                    .as("headerNames must be a implementation of java.util.List: {0}", headerNames)
                    .isInstanceOf(List.class)
                    .as("headerNames cannot have duplicated elements: {0}", headerNames)
                    .predicate(them -> them.stream().noneMatch(it -> Collections.frequency(them, it) > 1));

            this.headerNames = Collections.unmodifiableList(headerNames);
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.headerNames;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class KeyNames extends AbstractExcelWriteStrategy {
        private final Map<String, ?> keyMap;

        public KeyNames(List<String> keyOrders) {
            Asserts.that(keyOrders)
                    .as("keyOrders is not allowed to be null or empty: {0}", keyOrders)
                    .isNotNull().hasElement()
                    .as("keyOrders cannot have null element: {0}", keyOrders)
                    .doesNotContainNull()
                    .as("keyOrders must be a implementation of java.util.List: {0}", keyOrders)
                    .isInstanceOf(List.class)
                    .as("keyOrders cannot have duplicated elements: {0}", keyOrders)
                    .predicate(them -> them.stream().noneMatch(it -> Collections.frequency(them, it) > 1));

            // Creates a map of which key is element in keys and value is column index.
            Map<String, Integer> orders = new HashMap<>();
            for (int i = 0; i < keyOrders.size(); i++) {
                orders.put(keyOrders.get(i), i);
            }

            this.keyMap = Collections.singletonMap("orders", orders);
        }

        public KeyNames(List<String> keyOrders, List<String> newKeyNames) {
            Asserts.that(keyOrders)
                    .as("keyOrders is not allowed to be null or empty: {0}", keyOrders)
                    .isNotNull().hasElement()
                    .as("keyOrders cannot have null element: {0}", keyOrders)
                    .doesNotContainNull()
                    .as("keyOrders must be a implementation of java.util.List: {0}", keyOrders)
                    .isInstanceOf(List.class)
                    .as("keyOrders cannot have duplicated elements: {0}", keyOrders)
                    .predicate(them -> them.stream().noneMatch(it -> Collections.frequency(them, it) > 1));

            Asserts.that(newKeyNames)
                    .as("newKeyNames is not allowed to be null or empty: {0}", newKeyNames)
                    .isNotNull().hasElement()
                    .as("newKeyNames.size is not equal to keyOrders.size (newKeyNames.size: {0}, keyOrders.size: {1})",
                            newKeyNames.size(), keyOrders.size())
                    .isSameSize(keyOrders)
                    .as("newKeyNames cannot have null element: {0}", newKeyNames)
                    .doesNotContainNull()
                    .as("newKeyNames must be a implementation of java.util.List: {0}", newKeyNames)
                    .isInstanceOf(List.class)
                    .as("newKeyNames cannot have duplicated elements: {0}", newKeyNames)
                    .predicate(them -> them.stream().noneMatch(it -> Collections.frequency(them, it) > 1));

            // Creates a map of which key is element in keys and value is column index.
            Map<String, Object> keyMap = new HashMap<>();
            Map<String, Integer> orders = new HashMap<>();
            for (int i = 0; i < keyOrders.size(); i++) {
                orders.put(keyOrders.get(i), i);
            }

            keyMap.put("orders", orders);
            keyMap.put("names", newKeyNames);

            this.keyMap = Collections.unmodifiableMap(keyMap);
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.keyMap;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class Filter extends AbstractExcelWriteStrategy {
        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return null; // Not used.
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class EnumDropdown extends AbstractExcelWriteStrategy {
        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return null; // Not used.
        }
    }

    ///////////////////////////////////// Decoration //////////////////////////////////////

    class AutoResizedColumns extends AbstractExcelWriteStrategy {
        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return null; // Not used.
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class HiddenExtraRows extends AbstractExcelWriteStrategy {
        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return null; // Not used.
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class HiddenExtraColumns extends AbstractExcelWriteStrategy {
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return null; // Not used.
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class HeaderStyles extends AbstractExcelWriteStrategy {
        private final List<ExcelStyleConfig> styleConfigs;

        public HeaderStyles(List<ExcelStyleConfig> styleConfigs) {
            Asserts.that(styleConfigs)
                    .as("styleConfigs is not allowed to be null or empty: {0}", styleConfigs)
                    .isNotNull().hasElement()
                    .as("styleConfigs cannot have null element: {0}", styleConfigs)
                    .doesNotContainNull()
                    .as("styleConfigs must be a implementation of java.util.List: {0}", styleConfigs)
                    .isInstanceOf(List.class);

            this.styleConfigs = Collections.unmodifiableList(styleConfigs);
        }

        public HeaderStyles(ExcelStyleConfig styleConfig) {
            Asserts.that(styleConfig)
                    .as("styleConfig is not allowed to be null")
                    .isNotNull();

            this.styleConfigs = Collections.singletonList(styleConfig);
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.styleConfigs;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    class BodyStyles extends AbstractExcelWriteStrategy {
        private final List<ExcelStyleConfig> styleConfigs;

        public BodyStyles(List<ExcelStyleConfig> styleConfigs) {
            Asserts.that(styleConfigs)
                    .as("styleConfigs is not allowed to be null or empty: {0}", styleConfigs)
                    .isNotNull().hasElement()
                    .as("styleConfigs cannot have null element: {0}", styleConfigs)
                    .doesNotContainNull()
                    .as("styleConfigs must be a implementation of java.util.List: {0}", styleConfigs)
                    .isInstanceOf(List.class);

            this.styleConfigs = Collections.unmodifiableList(styleConfigs);
        }

        public BodyStyles(ExcelStyleConfig styleConfig) {
            Asserts.that(styleConfig)
                    .as("styleConfig is not allowed to be null")
                    .isNotNull();

            this.styleConfigs = Collections.singletonList(styleConfig);
        }

        @Override
        public boolean isSupported(ExcelWriteContext<?> context) {
            Class<? extends ExcelWriter<?>> writerType = context.getWriterType();
            return $ModelWriter.class.isAssignableFrom(writerType) || $MapWriter.class.isAssignableFrom(writerType);
        }

        @Override
        public Object execute(ExcelWriteContext<?> context) {
            return this.styleConfigs;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

}

abstract class AbstractExcelWriteStrategy implements ExcelWriteStrategy {

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractExcelWriteStrategy)) return false;

        AbstractExcelWriteStrategy that = (AbstractExcelWriteStrategy) obj;
        return Objects.equals(this.getClass(), that.getClass());
    }

}
