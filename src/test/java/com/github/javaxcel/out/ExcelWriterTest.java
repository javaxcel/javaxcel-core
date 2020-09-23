package com.github.javaxcel.out;

import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.AllIgnoredModel;
import com.github.javaxcel.model.etc.NoFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.styler.ExcelStyler;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelWriterTest {

    @Test
    @DisplayName("필드 제외/기본값")
    @SneakyThrows(IOException.class)
    public void writeWithIgnoreAndDefaultValue() {
        // given
        File file = new File("/data", "products.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // when
        List<Product> products = new Product().createRandoms(1000);
        ExcelWriter.init(workbook, Product.class)
                .sheetName("Products")
                .write(out, products);

        // then
        assertTrue(file.exists());
    }

    @Test
    @DisplayName("자식 객체/날짜타입")
    @SneakyThrows(IOException.class)
    public void writeWithTargetedFieldPolicyAndDateTimePattern() {
        // given
        File file = new File("/data", "toys.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<EducationToy> toys = new EducationToy().createRandoms(1000);
        ExcelWriter.init(workbook, EducationToy.class).write(out, toys);

        // then
        assertTrue(file.exists());
    }

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class})
    @DisplayName("Targeted field가 없는 경우")
    @SneakyThrows(IOException.class)
    public void writeWithClassThatHasNoTargetFields(Class<?> type) {
        // given
        File file = new File("/data", "no-field-model.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // then
        assertThrows(NoTargetedFieldException.class,
                () -> ExcelWriter.init(workbook, type).write(out, new ArrayList<>()));
    }

    @Test
    @DisplayName("스타일/데코레이션")
    @SneakyThrows(IOException.class)
    public void writeAndDecorate() {
        // given
        File file = new File("/data", "products-styled.xlsx");
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
        List<Product> products = new Product().createRandoms(1000);
        ExcelWriter.init(workbook, Product.class)
                .sheetName("PROD")
                .adjustSheet((sheet, numOfRows, numOfColumns) -> {
                    ExcelStyler.autoResizeColumns(sheet, numOfColumns);
                    ExcelStyler.hideExtraRows(sheet, numOfRows);
                    ExcelStyler.hideExtraColumns(sheet, numOfColumns);
                })
                .headerStyle(ExcelStyler::applyBasicHeaderStyle)
                .columnStyles(blueColumn, greenColumn, blueColumn, greenColumn, blueColumn, greenColumn)
                .write(out, products);

        // then
        assertTrue(file.exists());
    }

    @Test
    @SneakyThrows
    public void writePeople() {
        // given
        File file = new File("/data", "people.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
        List<Human> people = new Human().createRandoms(1000);

        // when
        ExcelWriter.init(workbook, Human.class)
                .adjustSheet((sheet, numOfRows, numOfColumns) -> ExcelStyler.autoResizeColumns(sheet, numOfColumns))
                .headerStyle((style, font) -> {
                    font.setFontHeightInPoints((short) 10);
                    font.setColor(IndexedColors.BLACK.getIndex());
                    font.setBold(true);
                    font.setFontName("Malgun Gothic");
                    style.setFont(font);

                    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    return style;
                })
                .columnStyles((style, font) -> {
                    font.setFontHeightInPoints((short) 10);
                    font.setColor(IndexedColors.BLACK.getIndex());
                    font.setFontName("Malgun Gothic");
                    style.setFont(font);
                    return style;
                })
                .write(out, people);

        // then
        assertTrue(file.exists());
    }

}
