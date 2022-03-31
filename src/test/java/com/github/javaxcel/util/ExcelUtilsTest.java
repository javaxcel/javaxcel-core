package com.github.javaxcel.util;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.handler.registry.impl.DefaultExcelTypeHandlerRegistry;
import com.github.javaxcel.converter.out.DefaultExcelWriteConverter;
import com.github.javaxcel.converter.out.ExcelWriteConverter;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.out.core.ExcelWriter;
import com.github.javaxcel.out.core.impl.ModelWriter;
import io.github.imsejin.common.tool.Stopwatch;
import io.github.imsejin.common.util.ReflectionUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

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
                .orElseThrow(() -> new NoTargetedConstructorException("Failed to find the targeted constructor"));
        if (!constructor.isAccessible()) constructor.setAccessible(true);
        stopwatch.stop();

        assertThat(constructor.newInstance())
                .as("Instantiates class without params")
                .isInstanceOf(clazz);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        System.out.printf("Constructor with minimum parameters: %s%n", constructor);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "date", "localTime", "localDate", "localDateTime",
            "zonedDateTime", "offsetTime", "offsetDateTime",
    })
    @SneakyThrows
    void stringifyValue(String fieldName) {
        // given
        ExcelTypeHandlerRegistry registry = new DefaultExcelTypeHandlerRegistry();
        ExcelWriteConverter<EducationToy> converter = new DefaultExcelWriteConverter<>(registry);
        Field field = EducationToy.class.getDeclaredField(fieldName);

        for (EducationToy toy : TestUtils.getMocks(EducationToy.class, 10)) {
            // when
            String stringified = converter.convert(toy, field);

            // then
            assertThat(stringified).isNotNull().isNotBlank();
        }
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void instantiateByClassName() {
        Stopwatch stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);

        // given
        String className = ModelWriter.class.getName();

        // when
        stopwatch.start("load class");
        Class<?> clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        stopwatch.stop();

        stopwatch.start("instantiate");
        Class<?>[] paramTypes = {Workbook.class, Class.class, ExcelTypeHandlerRegistry.class};
        Object[] initArgs = {new XSSFWorkbook(), Product.class, new DefaultExcelTypeHandlerRegistry()};
        ExcelWriter<Product> instance = (ExcelWriter<Product>) ReflectionUtils.instantiate(clazz, paramTypes, initArgs);
        stopwatch.stop();

        // then
        assertThat(instance)
                .isNotNull()
                .isInstanceOf(ModelWriter.class);
        System.out.println(stopwatch.getStatistics());
    }

    @Test
    @Disabled
    @SneakyThrows
    void test() {
        // given
        File file = new File("/data/hssf-rgb.xls");
        HSSFWorkbook workbook = HSSFWorkbookFactory.createWorkbook();
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
