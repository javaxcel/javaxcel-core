package com.github.javaxcel.out;

import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.AllIgnoredModel;
import com.github.javaxcel.model.etc.NoFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.styler.ExcelStyler;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.*;

public class ModelWriterTest {

    @SneakyThrows
    private static long getNumOfWrittenModels(Class<? extends Workbook> type, File file) {
        @Cleanup
        Workbook workbook = type == HSSFWorkbook.class
                ? new HSSFWorkbook(new FileInputStream(file))
                : new XSSFWorkbook(file);
        return ExcelUtils.getNumOfModels(workbook);
    }

    /**
     * When write 349,525 mocks,
     * <p> 1. XSSFWorkbook: 45 sec
     * <p> 2. SXSSFWorkbook: 6 sec
     */
    @Test
    @DisplayName("Default value + @ExcelIgnore")
    @SneakyThrows
    public void writeWithIgnoreAndDefaultValue() {
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        String filename = "products.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup SXSSFWorkbook workbook = new SXSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = ExcelStyler.XSSF_MAX_ROWS / 10;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Product> products = new Product().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Product.class)
                .sheetName("Products")
                .write(out, products);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", products.size())
                .isEqualTo(products.size());
        System.out.println(stopWatch.getStatistics());
    }

    @Test
    @DisplayName("Including inherited fields + @ExcelDateTimeFormat")
    @SneakyThrows
    public void writeWithTargetedFieldPolicyAndDateTimePattern() {
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        String filename = "toys.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> toys = new EducationToy().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, toys);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", toys.size())
                .isEqualTo(toys.size());
        System.out.println(stopWatch.getStatistics());
    }

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class})
    @DisplayName("Model without targeted fields")
    @SneakyThrows
    public void writeWithClassThatHasNoTargetFields(Class<?> type) {
        // given
        File file = new File("/data", "no-field-model.xls");
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();

        // when & then
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, type).write(out, new ArrayList<>()))
                .as("When write with a model that has targeted fields")
                .isInstanceOf(NoTargetedFieldException.class);
    }

    @Test
    @DisplayName("Adjust sheet + styling")
    @SneakyThrows
    public void writeAndDecorate() {
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        String filename = "people-styled.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
        BiFunction<CellStyle, Font, CellStyle> blueColumn = (style, font) -> {
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setFontName("Malgun Gothic");
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        };
        BiFunction<CellStyle, Font, CellStyle> greenColumn = (style, font) -> {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        };
        stopWatch.stop();

        // when
        int numOfMocks = 1000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopWatch.stop();

        stopWatch.start(String.format("write and decorate %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class)
                .sheetName("People")
                .adjustSheet((sheet, numOfRows, numOfColumns) -> {
                    ExcelStyler.autoResizeColumns(sheet, numOfColumns);
                    ExcelStyler.hideExtraRows(sheet, numOfRows);
                    ExcelStyler.hideExtraColumns(sheet, numOfColumns);
                })
                .headerStyle(ExcelStyler::applyBasicHeaderStyle)
                .columnStyles(blueColumn, greenColumn, blueColumn, greenColumn, blueColumn, greenColumn, blueColumn,
                        greenColumn, blueColumn, greenColumn, blueColumn, greenColumn, blueColumn, greenColumn)
                .write(out, people);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        System.out.println(stopWatch.getStatistics());
    }

    @Test
    @DisplayName("Including inherited fields + @ExcelWriterConversion")
    @SneakyThrows
    public void writePeople() {
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        String filename = "people.xls";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new HSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = SpreadsheetVersion.EXCEL97.getMaxRows() + 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class).write(out, people);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(HSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", people.size())
                .isEqualTo(people.size());
        System.out.println(stopWatch.getStatistics());
    }

}
