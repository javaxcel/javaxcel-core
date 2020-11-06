package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.FinalFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelReaderTest {

    private Stopwatch stopWatch;

    @BeforeEach
    public void beforeEach() {
        this.stopWatch = new Stopwatch(TimeUnit.SECONDS);
    }

    @AfterEach
    public void afterEach() {
        System.out.println(this.stopWatch.getStatistics());
    }

    @Test
    @DisplayName("Find constructor with min params")
    @SneakyThrows
    public void getDeclaredConstructorWithMinimumParameters() {
        // given
        stopWatch.start();
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
        stopWatch.stop();

        assertThat(constructor.newInstance())
                .as("Instantiates class without params")
                .isInstanceOf(clazz);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        System.out.printf("Constructor with minimum parameters: %s\n", constructor);
    }

    /**
     * @see com.github.javaxcel.annotation.ExcelIgnore
     */
    @Test
    @DisplayName("@ExcelIgnore + @ExcelModel(includeSuper = false)")
    @SneakyThrows
    public void readProducts(@TempDir Path path) {
        String filename = "products.xls";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Product> mocks = new Product().createRandoms(numOfMocks);
        stopWatch.stop();

        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Product.class).write(out, mocks);
        stopWatch.stop();

        stopWatch.start(String.format("load '%s' file", filename));
        @Cleanup HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
        stopWatch.stop();

        // when
        stopWatch.start(String.format("read %,d models", numOfMocks));
        List<Product> products = ExcelReaderFactory.create(wb, Product.class).read();
        stopWatch.stop();

        // then
        assertThat(products.size())
                .as("#1 The number of loaded models is %,s", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(products)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Product[0]));
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelDateTimeFormat#pattern()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelDateTimeFormat")
    @SneakyThrows
    public void readEducationToys(@TempDir Path path) {
        String filename = "toys.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> mocks = new EducationToy().createRandoms(numOfMocks);
        stopWatch.stop();

        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, mocks);
        stopWatch.stop();

        stopWatch.start(String.format("load '%s' file", filename));
        @Cleanup Workbook wb = new XSSFWorkbook(file);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("read %,d models", numOfMocks));
        List<EducationToy> educationToys = ExcelReaderFactory.create(wb, EducationToy.class).read();
        stopWatch.stop();

        // then
        assertThat(educationToys.size())
                .as("#1 The number of loaded models is %,s", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(educationToys)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new EducationToy[0]));
    }

    @Test
    @Disabled
    @DisplayName("Model with final fields")
    @SneakyThrows
    public void readFinalFields(@TempDir Path path) {
        // given
        File file = new File(path.toFile(), "final-fields.xls");
        @Cleanup Workbook workbook = HSSFWorkbookFactory.create(file);

        // when
        List<FinalFieldModel> list = ExcelReaderFactory.create(workbook, FinalFieldModel.class).read();

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
    @Disabled
    @SneakyThrows
    public void readMultipleSheets(@TempDir Path path) {
        // given
        List<Product> products = new Product().createDesignees();
        List<EducationToy> educationToys = new EducationToy().createDesignees();
        File file = new File(path.toFile(), "merged.xlsx");
        @Cleanup Workbook workbook = WorkbookFactory.create(file);

        // when
        List<Product> sheet1 = ExcelReaderFactory.create(workbook, Product.class).sheetIndexes(0).read();
        List<EducationToy> sheet2 = ExcelReaderFactory.create(workbook, EducationToy.class).sheetIndexes(1).read();

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

    /**
     * @see ExcelModel#includeSuper()
     * @see com.github.javaxcel.annotation.ExcelReaderExpression
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelReaderExpression")
    @SneakyThrows
    public void readPeople(@TempDir Path path) {
        String filename = "people.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), "people.xlsx");
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> mocks = new Human().createRandoms(numOfMocks);
        stopWatch.stop();

        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class).write(out, mocks);
        stopWatch.stop();

        stopWatch.start(String.format("load '%s' file", filename));
        @Cleanup Workbook wb = new XSSFWorkbook(file);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("read %,d models", numOfMocks));
        List<Human> people = ExcelReaderFactory.create(wb, Human.class).parallel().read();
        stopWatch.stop();

        // then
        assertThat(people.size())
                .as("#1 The number of loaded models is %,s", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(people)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Human[0]));
    }

}
