package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.FinalFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.out.ExcelWriter;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelReaderTest {

    @Test
    @SneakyThrows
    public void getDeclaredConstructorWithMinimumParameters() {
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
                .orElseThrow(() -> new NoTargetedConstructorException(clazz));
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
    @SneakyThrows
    public void readWithNotInheritedTypeAndExcelIgnore() {
        // given
        List<Product> mocks = new Product().createDesignees();
        File file = new File("/data", "products.xls");
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
    @SneakyThrows
    public void readWithTargetedFieldPolicyAndDateTimePattern() {
        // given
        List<EducationToy> mocks = new EducationToy().createDesignees();
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
    @SneakyThrows
    public void readWithFinalFields() {
        // given
        File file = new File("/data", "final-fields.xls");
        @Cleanup
        Workbook workbook = HSSFWorkbookFactory.create(file);

        // when
        List<FinalFieldModel> list = ExcelReader.init(workbook, FinalFieldModel.class).read();

        // then
        list.forEach(System.out::println);
    }

    @Test
    @SneakyThrows
    public void readMultipleSheets() {
        // given
        List<Product> products = new Product().createDesignees();
        List<EducationToy> educationToys = new EducationToy().createDesignees();
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

    @Test
    @SneakyThrows
    public void readPeople() {
        // given
        File file = new File("/data", "people.xlsx");
        @Cleanup
        Workbook workbook = WorkbookFactory.create(file);

        // when
        List<Human> people = ExcelReader.init(workbook, Human.class).read();

        // then
        people.forEach(System.out::println);
    }

}
