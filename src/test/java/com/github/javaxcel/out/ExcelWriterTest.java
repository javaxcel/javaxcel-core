package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.model.AllIgnoredModel;
import com.github.javaxcel.model.EducationToy;
import com.github.javaxcel.model.NoFieldModel;
import com.github.javaxcel.model.Product;
import com.github.javaxcel.model.factory.MockFactory;
import lombok.Cleanup;
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

    /**
     * 1. {@link ExcelIgnore}
     * <br>
     * 2. {@link ExcelColumn#value()}
     * <br>
     * 3. {@link ExcelColumn#defaultValue()}
     */
    @Test
    public void writeWithIgnoreAndDefaultValue() throws IOException {
        // given
        File file = new File("/data", "products.xls");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        ExcelWriter.init(workbook, Product.class)
                .sheetName("Products")
                .write(out, products);

        // then
        assertTrue(file.exists());
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    public void writeWithTargetedFieldPolicyAndDateTimePattern() throws IOException {
        // given
        File file = new File("/data", "toys.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<EducationToy> toys = MockFactory.generateRandomBox(1000).getAll();
        ExcelWriter.init(workbook, EducationToy.class).write(out, toys);

        // then
        assertTrue(file.exists());
    }

    @DisplayName("ExcelWriter writes with the model that has no targeted fields. ==> occurs NoSuchFieldException")
    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class})
    public void writeWithClassThatHasNoTargetFields(Class<?> type) throws IOException {
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
    public void writeAndDecorate() throws IOException {
        // given
        File file = new File("/data", "products-styled.xlsx");
        @Cleanup
        FileOutputStream out = new FileOutputStream(file);
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
        BiFunction<CellStyle, Font, CellStyle> blueColumn = (style, font) -> {
            font.setColor(IndexedColors.WHITE.getIndex());
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
        List<Product> products = MockFactory.generateRandomProducts(1000);
        ExcelWriter.init(workbook, Product.class)
                .sheetName("PROD")
                .adjustSheet((sheet, numOfRows, numOfColumns) -> {
                    // Makes the columns fit content.
                    for (int i = 0; i < numOfColumns; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    // Hides extra rows.
                    int maxRows = sheet instanceof HSSFSheet ? 65_536 : 1_048_576;
                    for (int i = numOfRows - 1; i < maxRows; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) row = sheet.createRow(i);
                        row.setZeroHeight(true);
                    }

                    // Hides extra columns.
                    int maxColumns = sheet instanceof HSSFSheet ? 256 : 16_384;
                    for (int i = numOfColumns; i < maxColumns; i++) {
                        sheet.setColumnHidden(i, true);
                    }
                })
                .headerStyle((style, font) -> {
                    font.setItalic(true);
                    font.setFontHeightInPoints((short) 14);
                    font.setColor((short) 8);
                    font.setBold(true);
                    style.setFont(font);

                    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    return style;
                })
                .columnStyles(blueColumn, greenColumn, blueColumn, greenColumn, blueColumn, greenColumn)
                .write(out, products);

        // then
        assertTrue(file.exists());
    }

}
