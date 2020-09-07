package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.model.EducationToy;
import com.github.javaxcel.model.Product;
import com.github.javaxcel.model.factory.MockFactory;
import com.github.javaxcel.out.ExcelWriter;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelReaderTest {

    @Test
    public void getDeclaredConstructorWithMinimumParameters() throws ReflectiveOperationException {
        // given
        Class<Product> clazz = Product.class;

        // when
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

        // then
        for (Constructor<?> constructor : declaredConstructors) {
            System.out.println(constructor);
        }
        Constructor<?> constructor = Arrays.stream(declaredConstructors)
                .min((a, b) -> Math.min(a.getParameterCount(), b.getParameterCount()))
                .orElseThrow(NoSuchElementException::new);
        System.out.println("constructor with minimum parameters: " + constructor);
        constructor.setAccessible(true);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        Object o = constructor.newInstance();
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link com.github.javaxcel.constant.TargetedFieldPolicy#OWN_FIELDS}
     * <br>
     * 2. {@link com.github.javaxcel.annotation.ExcelIgnore}
     */
    @Test
    public void readWithNotInheritedTypeAndExcelIgnore() throws ReflectiveOperationException, IOException {
        // given
        List<Product> mocks = MockFactory.generateStaticProducts();
        File file = new File("/data", "products.xlsx");
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();
        @Cleanup
        OutputStream out = new FileOutputStream(file);

        // when
        ExcelWriter.init(workbook, Product.class).write(out, mocks);
        List<Product> products = ExcelReader.init(workbook, Product.class).read();

        // then
        assertTrue(mocks.stream()
                .peek(System.out::println)
                .allMatch(product -> Collections.frequency(products, product) > 0));
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link com.github.javaxcel.constant.TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    public void readWithTargetedFieldPolicyAndDateTimePattern() throws ReflectiveOperationException, IOException {
        // given
        List<EducationToy> mocks = MockFactory.generateStaticBox().getAll();
        File file = new File("/data", "toys.xlsx");
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
        @Cleanup
        OutputStream out = new FileOutputStream(file);

        // when
        ExcelWriter.init(workbook, EducationToy.class).write(out, mocks);
        List<EducationToy> educationToys = ExcelReader.init(workbook, EducationToy.class).startIndex(1).read();

        // then
        assertTrue(educationToys.stream()
                .peek(System.out::println)
                .allMatch(educationToy -> Collections.frequency(mocks, educationToy) > 0));
    }

    @Test
    public void readMultipleSheets() throws ReflectiveOperationException, IOException, InvalidFormatException {
        // given
        List<Product> products = MockFactory.generateStaticProducts();
        List<EducationToy> educationToys = MockFactory.generateStaticBox().getAll();
        File file = new File("/data", "merged.xlsx");
        @Cleanup
        Workbook workbook = WorkbookFactory.create(file);

        // when
        List<Product> sheet1 = ExcelReader.init(workbook, Product.class).sheetIndexes(0).read();
        List<EducationToy> sheet2 = ExcelReader.init(workbook, EducationToy.class).sheetIndexes(1).read();

        // then
        assertTrue(products.stream()
                .peek(System.out::println)
                .allMatch(product -> Collections.frequency(sheet1, product) > 0));
        assertTrue(educationToys.stream()
                .peek(System.out::println)
                .allMatch(educationToy -> Collections.frequency(sheet2, educationToy) > 0));
    }

}
