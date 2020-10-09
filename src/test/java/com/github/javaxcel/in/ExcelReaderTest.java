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
import org.junit.jupiter.api.*;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ExcelReaderTest {

    @Test
    @DisplayName("Find constructor with min params")
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
                .min(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new NoTargetedConstructorException(clazz));
        System.out.println("constructor with minimum parameters: " + constructor);
        constructor.setAccessible(true);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        constructor.newInstance();
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link com.github.javaxcel.constant.TargetedFieldPolicy#OWN_FIELDS}
     * <br>
     * 2. {@link com.github.javaxcel.annotation.ExcelIgnore}
     */
    @Test
    @DisplayName("Own fields + @ExcelIgnore")
    @SneakyThrows
    public void readWithNotInheritedTypeAndExcelIgnore() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("load 'products.xls' file");

        // given
        File file = new File("/data", "products.xls");
        @Cleanup
        HSSFWorkbook workbook = new HSSFWorkbook();
        @Cleanup
        OutputStream out = new FileOutputStream(file);

        stopWatch.stop();

        List<Product> mocks = new Product().createRandoms(10_000);

        stopWatch.start("read products");

        // when
        ExcelWriter.init(workbook, Product.class).write(out, mocks);
        List<Product> products = ExcelReader.init(workbook, Product.class).read();

        stopWatch.stop();

        // then
        assertThat(mocks.containsAll(products)).isTrue();
        System.out.println(stopWatch.prettyPrint());
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link com.github.javaxcel.constant.TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    @DisplayName("Including inherited fields + @ExcelDateTimeFormat")
    @SneakyThrows
    public void readWithTargetedFieldPolicyAndDateTimePattern() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("load 'toys.xlsx' file");

        // given
        File file = new File("/data", "toys.xlsx");
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
        @Cleanup
        OutputStream out = new FileOutputStream(file);

        stopWatch.stop();

        List<EducationToy> mocks = new EducationToy().createRandoms(10_000);

        stopWatch.start("read toys");

        // when
        ExcelWriter.init(workbook, EducationToy.class).write(out, mocks);
        List<EducationToy> educationToys = ExcelReader.init(workbook, EducationToy.class).read();

        stopWatch.stop();

        // then
        assertThat(mocks.containsAll(educationToys)).isTrue();
        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    @DisplayName("Model with final fields")
    @SneakyThrows
    public void readWithFinalFields() {
        // given
        File file = new File("/data", "final-fields.xls");
        @Cleanup
        Workbook workbook = HSSFWorkbookFactory.create(file);

        // when
        List<FinalFieldModel> list = ExcelReader.init(workbook, FinalFieldModel.class).read();

        // then
        list.forEach(System.out::println); // FinalFieldModel(number=100, text=TEXT)
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
        assertThat(products.stream()
                .peek(System.out::println)
                .allMatch(it -> Collections.frequency(sheet1, it) > 0))
                .isTrue();
        assertThat(educationToys.stream()
                .peek(System.out::println)
                .allMatch(it -> Collections.frequency(sheet2, it) > 0))
                .isTrue();
    }

    @Test
    @DisplayName("Including inherited fields + @ExcelReaderConversion")
    @SneakyThrows
    public void readPeople() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("load 'people.xlsx' file");

        // given
        File file = new File("/data", "people.xlsx");
        @Cleanup
        XSSFWorkbook workbook = new XSSFWorkbook();
        @Cleanup
        OutputStream out = new FileOutputStream(file);

        stopWatch.stop();

        List<Human> mocks = new Human().createRandoms(10_000);

        stopWatch.start("read people");

        // when
        ExcelWriter.init(workbook, Human.class).write(out, mocks);
        List<Human> people = ExcelReader.init(workbook, Human.class).read();

        stopWatch.stop();

        // then
        System.out.println(stopWatch.prettyPrint());
        assertThat(mocks.containsAll(people)).isTrue();
    }

}
