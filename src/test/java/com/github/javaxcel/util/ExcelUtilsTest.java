package com.github.javaxcel.util;

import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.product.Product;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelUtilsTest {

    @Test
    @SneakyThrows
    @StopwatchProvider
    @DisplayName("Find constructor with min params")
    void getDeclaredConstructorWithMinimumParameters(Stopwatch stopwatch) {
        // given
        stopwatch.start();
        Class<Product> clazz = Product.class;

        // when
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

        // then
        for (Constructor<?> constructor : declaredConstructors) {
            System.out.println(constructor);
        }
        Constructor<?> constructor = Arrays.stream(declaredConstructors)
                .min(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new AmbiguousExcelModelCreatorException("Failed to find the targeted constructor"));
        if (!constructor.isAccessible()) constructor.setAccessible(true);
        stopwatch.stop();

        assertThat(constructor.newInstance())
                .as("Instantiates class without params")
                .isInstanceOf(clazz);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        System.out.printf("Constructor with minimum parameters: %s%n", constructor);
    }

    @Test
    @Disabled
    @SneakyThrows
    void test() {
        // given
        File file = new File("/data/hssf-rgb.xls");
        HSSFWorkbook workbook = (HSSFWorkbook) WorkbookFactory.create(false);
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        // when
        HSSFPalette palette = workbook.getCustomPalette();
        // `HSSFColorPredefined.WHITE`의 RGB를 사용자지정 RGB로 대체한다.
        palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.WHITE.getIndex(),
                (byte) 192, (byte) 168, (byte) 7);
//        palette.addColor((byte) 192, (byte) 168, (byte) 7); // RuntimeException: Could not find free color index

        // 해당 RGB를 갖는 `HSSFColorPredefined`를 찾는다.
        HSSFColor hssfColor = palette.findColor((byte) 192, (byte) 168, (byte) 7);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(hssfColor.getIndex()); // Hexadecimal
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(cellStyle);

        cell.setCellValue("RGB");

        workbook.write(file);

        // then
        assertThat(file).exists();
    }

    @Test
    @Disabled
    @SneakyThrows
    void func() {
        // given
        File file = new File("/data/hssf-rgb.xls");
        @Cleanup Workbook workbook = new HSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        // when
        CellStyle cellStyle = cell.getCellStyle();
        System.out.println(cellStyle.getFillForegroundColor()); // decimal
        System.out.println(cellStyle.getFillForegroundColorColor());
    }

}
