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

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.internal.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.core.ModelWriterTester;
import com.github.javaxcel.out.strategy.impl.BodyStyles;
import com.github.javaxcel.out.strategy.impl.HeaderStyles;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;
import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.ToString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.github.javaxcel.TestUtils.assertNotEmptyFile;
import static com.github.javaxcel.util.ExcelUtils.equalsCellStyleAndFont;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @see HeaderStyles
 * @see BodyStyles
 */
@StopwatchProvider
class DecorationTest extends ModelWriterTester {

    @Test
    @StopwatchProvider(TimeUnit.MILLISECONDS)
    void fail(Stopwatch stopwatch) {
        // given
        stopwatch.start("create '%s' instance", SXSSFWorkbook.class.getSimpleName());
        Workbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        // when & then
        stopwatch.start("set unmatched header style");
        assertThatThrownBy(() -> TestUtils.JAVAXCEL.writer(workbook, WithModel.class)
                .options(new HeaderStyles(Arrays.asList(DefaultHeaderStyleConfig.getRainbowHeader())))
                .write(null, TestUtils.getMocks(WithModel.class, 10)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("headerStyles.size must be 1 or equal to fields.size");
        stopwatch.stop();

        stopwatch.start("set unmatched body style");
        assertThatThrownBy(() -> TestUtils.JAVAXCEL.writer(workbook, WithColumn.class)
                .options(new BodyStyles(Arrays.asList(DefaultHeaderStyleConfig.getRainbowHeader())))
                .write(null, TestUtils.getMocks(WithColumn.class, 10)))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("bodyStyles.size must be 1 or equal to fields.size");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            WithModel.class, WithColumn.class, WithModelAndColumn.class,
            WithModelAndDirect.class, WithColumnAndDirect.class, WithModelAndColumnAndDirect.class,
    })
    void succeed(Class<?> type, @TempDir Path path, Stopwatch stopwatch) throws Exception {
        String filename = type.getSimpleName().toLowerCase() + '.' + getExtensionByType(type);
        File file = new File(path.toFile(), filename);

        run(file, type, stopwatch);
    }

    @Override
    protected WhenModel given(GivenModel givenModel) throws Exception {
        OutputStream out = new FileOutputStream(givenModel.getFile());
        Workbook workbook = getWorkbookByType(givenModel.getType());

        return new WhenModel(out, workbook, 1024);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void whenWriteWorkbook(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) {
        Workbook workbook = whenModel.getWorkbook();
        Class<?> type = givenModel.getType();

        ExcelWriter<?> writer = TestUtils.JAVAXCEL.writer(workbook, type);

        if (isTypeDefinedStyleDirectly(type)) {
            ExcelStyleConfig config = new NoStyleConfig();
            int numOfFields = FieldUtils.getTargetedFields(type).size();
            List<ExcelStyleConfig> configs = IntStream.range(0, numOfFields)
                    .mapToObj(i -> config).collect(toList());

            writer.options(new HeaderStyles(config), new HeaderStyles(configs),
                    new BodyStyles(config), new BodyStyles(configs));
        }

        writer.write(whenModel.getOutputStream(), (List) thenModel.getModels());
    }

    @Override
    protected void then(GivenModel givenModel, WhenModel whenModel, ThenModel thenModel) throws Exception {
        File file = givenModel.getFile();
        Class<?> type = givenModel.getType();

        assertNotEmptyFile(file);

        @Cleanup Workbook workbook = WorkbookFactory.create(file);
        assertEqualsCellStyles(workbook, type);
    }

    private static void assertEqualsCellStyles(Workbook workbook, Class<?> type) {
        List<CellStyle> cellStyles = ExcelUtils.getDeclaredCellStyles(workbook);

        CellStyle headerStyle = workbook.createCellStyle();
        new DefaultHeaderStyleConfig().configure(new Configurer(headerStyle, workbook.createFont()));
        CellStyle bodyStyle = workbook.createCellStyle();
        new DefaultBodyStyleConfig().configure(new Configurer(bodyStyle, workbook.createFont()));
        CellStyle noStyle = workbook.createCellStyle();

        if (type == WithModel.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(1);
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(1);

            for (Sheet sheet : workbook) {
                // Header.
                for (Cell cell : sheet.getRow(0)) {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, headerStyle)).isTrue();
                }

                // Body.
                forEachColumnBody(sheet, 0, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 1, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 2, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
            }

        } else if (type == WithColumn.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(3);
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(3);

            for (Sheet sheet : workbook) {
                // Header.
                for (Cell cell : sheet.getRow(0)) {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, headerStyle)).isTrue();
                }

                // Body.
                forEachColumnBody(sheet, 0, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 1, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 2, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
            }

        } else if (type == WithModelAndColumn.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(2);
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(2);

            for (Sheet sheet : workbook) {
                // Header.
                Row header = sheet.getRow(0);
                CellStyle style0 = header.getCell(0).getCellStyle();
                assertThat(equalsCellStyleAndFont(workbook, style0, workbook, headerStyle)).isTrue();
                CellStyle style1 = header.getCell(1).getCellStyle();
                assertThat(equalsCellStyleAndFont(workbook, style1, workbook, headerStyle)).isTrue();
                CellStyle style2 = header.getCell(2).getCellStyle();
                assertThat(equalsCellStyleAndFont(workbook, style2, workbook, bodyStyle)).isTrue();

                // Body.
                forEachColumnBody(sheet, 0, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 1, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, bodyStyle)).isTrue();
                });
                forEachColumnBody(sheet, 2, cell -> {
                    CellStyle cellStyle = cell.getCellStyle();
                    assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, headerStyle)).isTrue();
                });
            }

        } else if (type == WithModelAndDirect.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        CellStyle cellStyle = cell.getCellStyle();
                        assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, noStyle)).isTrue();
                    }
                }
            }

        } else if (type == WithColumnAndDirect.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        CellStyle cellStyle = cell.getCellStyle();
                        assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, noStyle)).isTrue();
                    }
                }
            }

        } else if (type == WithModelAndColumnAndDirect.class) {
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, headerStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.
            assertThat(cellStyles.stream()
                    .filter(it -> equalsCellStyleAndFont(workbook, it, workbook, bodyStyle)).count())
                    .isEqualTo(0); // Overrides NoStyleConfig that doesn't make CellStyle.

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        CellStyle cellStyle = cell.getCellStyle();
                        assertThat(equalsCellStyleAndFont(workbook, cellStyle, workbook, noStyle)).isTrue();
                    }
                }
            }
        }
    }

    private static boolean isTypeDefinedStyleDirectly(Class<?> type) {
        return type == WithModelAndDirect.class ||
                type == WithColumnAndDirect.class ||
                type == WithModelAndColumnAndDirect.class;
    }

    private static String getExtensionByType(Class<?> type) {
        if (type == WithModel.class || type == WithColumn.class) return ExcelUtils.EXCEL_97_EXTENSION;
        return ExcelUtils.EXCEL_2007_EXTENSION;
    }

    private static Workbook getWorkbookByType(Class<?> type) {
        if (type == WithModel.class || type == WithColumn.class) {
            return new HSSFWorkbook();
        } else {
            return new XSSFWorkbook();
        }
    }

    private static void forEachColumnBody(Sheet sheet, int columnIndex, Consumer<Cell> consumer) {
        int numOfModels = ExcelUtils.getNumOfModels(sheet);
        for (int i = 1; i <= numOfModels; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(columnIndex);
            consumer.accept(cell);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @ToString
    @ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
    private static class WithModel {
        private String name;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    @ToString
    private static class WithColumn {
        @ExcelColumn(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
        private String name;
        @ExcelColumn(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
        private LocalDateTime createdAt;
        @ExcelColumn(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
        private LocalDateTime modifiedAt;
    }

    @ToString
    @ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
    private static class WithModelAndColumn {
        private String name;
        @ExcelColumn
        private LocalDateTime createdAt;
        @ExcelColumn(headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
        private LocalDateTime modifiedAt;
    }

    @ToString
    @ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
    private static class WithModelAndDirect {
        private String name;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    @ToString
    private static class WithColumnAndDirect {
        @ExcelColumn(headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
        private String name;
        @ExcelColumn(headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
        private LocalDateTime createdAt;
        @ExcelColumn(headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
        private LocalDateTime modifiedAt;
    }

    @ToString
    @ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
    private static class WithModelAndColumnAndDirect {
        @ExcelColumn
        private String name;
        @ExcelColumn(bodyStyle = DefaultHeaderStyleConfig.class)
        private LocalDateTime createdAt;
        @ExcelColumn(headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
        private LocalDateTime modifiedAt;
    }

}
