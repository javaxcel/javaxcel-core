package com.github.javaxcel.out;

import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.AllIgnoredModel;
import com.github.javaxcel.model.etc.NoFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.styler.ExcelStyler;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class ExcelWriterTest {

    @Test
    @DisplayName("Default value + @ExcelIgnore")
    @SneakyThrows
    public void writeWithIgnoreAndDefaultValue() {
        // given
        File file = new File("/data", "products.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // when
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start("create product mocks");

        List<Product> products = new Product().createRandoms(ExcelStyler.HSSF_MAX_ROWS - 1);

        stopWatch.stop();
        stopWatch.start("write products");

        ExcelWriter.init(workbook, Product.class)
                .sheetName("Products")
                .write(out, products);

        stopWatch.stop();
        System.out.println(stopWatch.getStatistics());

        // then
        assertThat(file.exists()).isTrue();
    }

    @Test
    @DisplayName("Including inherited fields + @ExcelDateTimeFormat")
    @SneakyThrows
    public void writeWithTargetedFieldPolicyAndDateTimePattern() {
        // given
        File file = new File("/data", "toys.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start("create toy mocks");

        List<EducationToy> toys = new EducationToy().createRandoms(10_000);

        stopWatch.stop();
        stopWatch.start("write with toys");

        ExcelWriter.init(workbook, EducationToy.class).write(out, toys);

        stopWatch.stop();
        System.out.println(stopWatch.getStatistics());

        // then
        assertThat(file.exists()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class})
    @DisplayName("Model without targeted fields")
    @SneakyThrows
    public void writeWithClassThatHasNoTargetFields(Class<?> type) {
        // given
        File file = new File("/data", "no-field-model.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start("initialize");

        // then
        assertThrows(NoTargetedFieldException.class,
                () -> ExcelWriter.init(workbook, type).write(out, new ArrayList<>()));

        stopWatch.stop();
        System.out.println(stopWatch.getStatistics());
    }

    @Test
    @DisplayName("Adjust sheet + styling")
    @SneakyThrows
    public void writeAndDecorate() {
        // given
        File file = new File("/data", "people-styled.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
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

        // when
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start("create product mocks");

        List<Human> people = new Human().createRandoms(1000);

        stopWatch.stop();
        stopWatch.start("write people and decorate");

        ExcelWriter.init(workbook, Human.class)
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
        System.out.println(stopWatch.getStatistics());

        // then
        assertThat(file.exists()).isTrue();
    }

    @Test
    @DisplayName("Including inherited fields + @ExcelWriterConversion")
    @SneakyThrows
    public void writePeople() {
        // given
        File file = new File("/data", "people.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start("create human mocks");

        List<Human> people = new Human().createRandoms(10_000);

        stopWatch.stop();
        stopWatch.start("write people");

        // when
        ExcelWriter.init(workbook, Human.class).write(out, people);

        stopWatch.stop();
        System.out.println(stopWatch.getStatistics());

        // then
        assertThat(file.exists()).isTrue();
    }

}
