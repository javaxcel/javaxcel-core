package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.FinalFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.out.ExcelWriter;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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
        constructor.setAccessible(true);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);

        assertThat(constructor.newInstance())
                .as("Instantiates class without params")
                .isInstanceOf(clazz);
        System.out.println("Constructor with minimum parameters: " + constructor);
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
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        stopWatch.start("load 'products.xls' file");

        // given
        File file = new File("/data", "products.xls");
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);

        stopWatch.stop();
        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));

        List<Product> mocks = new Product().createRandoms(numOfMocks);

        stopWatch.stop();
        stopWatch.start("read products");

        // when
        ExcelWriter.init(workbook, Product.class).write(out, mocks);
        List<Product> products = ExcelReader.init(workbook, Product.class).read();

        stopWatch.stop();

        // then
        assertThat(products.size()).isEqualTo(mocks.size());
        assertThat(products).containsAll(mocks);
        System.out.println(stopWatch.getStatistics());
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
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        stopWatch.start("load 'toys.xlsx' file");

        // given
        File file = new File("/data", "toys.xlsx");
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);

        stopWatch.stop();
        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));

        List<EducationToy> mocks = new EducationToy().createRandoms(numOfMocks);

        stopWatch.stop();
        stopWatch.start("read toys");

        // when
        ExcelWriter.init(workbook, EducationToy.class).write(out, mocks);
        List<EducationToy> educationToys = ExcelReader.init(workbook, EducationToy.class).read();

        stopWatch.stop();

        // then
        assertThat(educationToys.size()).isEqualTo(mocks.size());
        assertThat(educationToys).containsAll(mocks);
        System.out.println(stopWatch.getStatistics());
    }

    @Test
    @DisplayName("Model with final fields")
    @SneakyThrows
    public void readWithFinalFields() {
        // given
        File file = new File("/data", "final-fields.xls");
        @Cleanup Workbook workbook = HSSFWorkbookFactory.create(file);

        // when
        List<FinalFieldModel> list = ExcelReader.init(workbook, FinalFieldModel.class).read();

        // then
        list.forEach(it -> {
            // FinalFieldModel(number=100, text=TEXT)
            assertThat(it.getNumber())
                    .as("Value of final field is never changed")
                    .isEqualTo(100);
            assertThat(it.getText())
                    .as("Value of final field is never changed")
                    .isEqualTo("TEXT");
            System.out.println(it);
        });
    }

    @Test
    @SneakyThrows
    public void readMultipleSheets() {
        // given
        List<Product> products = new Product().createDesignees();
        List<EducationToy> educationToys = new EducationToy().createDesignees();
        File file = new File("/data", "merged.xlsx");
        @Cleanup Workbook workbook = WorkbookFactory.create(file);

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
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
        stopWatch.start("load 'people.xlsx' file");

        // given
        File file = new File("/data", "people.xlsx");
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);

        stopWatch.stop();
        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));

        List<Human> mocks = new Human().createRandoms(numOfMocks);

        stopWatch.stop();
        stopWatch.start("read people");

        // when
        ExcelWriter.init(workbook, Human.class).write(out, mocks);
        List<Human> people = ExcelReader.init(workbook, Human.class).parallel().read();
        people.forEach(System.out::println);

        stopWatch.stop();

        // then
        assertThat(people.size())
                .as("#1 The number of loaded model is %,s", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(people)
                .as("#2 Each loaded model is equal to each mock")
                .containsAll(mocks);
        System.out.println(stopWatch.getStatistics());
    }

}
