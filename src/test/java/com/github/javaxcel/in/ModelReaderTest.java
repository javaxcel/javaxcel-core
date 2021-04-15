package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.computer.Computer;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class ModelReaderTest {

    @Test
    @DisplayName("Find constructor with min params")
    @SneakyThrows
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
                .orElseThrow(() -> new NoTargetedConstructorException(clazz));
        constructor.setAccessible(true);
        stopwatch.stop();

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
    void readProducts(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "products.xls";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Product> mocks = Product.createRandoms(numOfMocks);
        stopwatch.stop();

        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Product.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start(String.format("load '%s' file", filename));
        @Cleanup HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
        stopwatch.stop();

        // when
        stopwatch.start(String.format("read %,d models", numOfMocks));
        List<Product> products = ExcelReaderFactory.create(wb, Product.class).read();
        stopwatch.stop();

        // then
        assertThat(products.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(products)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Product[0]));
    }

    /**
     * @see AbstractExcelReader#limit(int)
     */
    @Test
    @DisplayName("ExcelReader#limit(int)")
    @SneakyThrows
    void readComputers(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "computers.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int limit = 10;
        int numOfMocks = 1000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Computer> mocks = Computer.createRandoms(numOfMocks);
        stopwatch.stop();

        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Computer.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start(String.format("load '%s' file", filename));
        @Cleanup Workbook wb = new XSSFWorkbook(file);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("read %,d models", Math.min(limit, numOfMocks)));
        List<Computer> computers = ExcelReaderFactory.create(wb, Computer.class)
                .limit(limit).read();
        stopwatch.stop();

        // then
        assertThat(computers.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(Math.min(limit, computers.size()));
        assertThat(computers)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.stream().limit(limit).toArray(Computer[]::new));
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelDateTimeFormat#pattern()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelDateTimeFormat")
    @SneakyThrows
    void readEducationToys(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "toys.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> mocks = new EducationToy().createRandoms(numOfMocks);
        stopwatch.stop();

        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start(String.format("load '%s' file", filename));
        @Cleanup Workbook wb = new XSSFWorkbook(file);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("read %,d models", numOfMocks));
        List<EducationToy> educationToys = ExcelReaderFactory.create(wb, EducationToy.class).read();
        stopwatch.stop();

        // then
        assertThat(educationToys.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(educationToys)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new EducationToy[0]));
    }

    @Test
    @Disabled
    @DisplayName("Model with final fields")
    @SneakyThrows
    void readFinalFields(@TempDir Path path) {
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
    void readMultipleSheets(@TempDir Path path) {
        // given
        List<Product> products = Product.createDesignees();
        List<EducationToy> educationToys = new EducationToy().createDesignees();
        File file = new File(path.toFile(), "merged.xlsx");
        @Cleanup Workbook workbook = WorkbookFactory.create(file);

        // when
        List<Product> sheet1 = ExcelReaderFactory.create(workbook, Product.class).read();
        List<EducationToy> sheet2 = ExcelReaderFactory.create(workbook, EducationToy.class).read();

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
    void readPeople(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "people.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> mocks = new Human().createRandoms(numOfMocks);
        stopwatch.stop();

        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start(String.format("load '%s' file", filename));
        @Cleanup Workbook wb = new XSSFWorkbook(file);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("read %,d models", numOfMocks));
        List<Human> people = ExcelReaderFactory.create(wb, Human.class).parallel().read();
        stopwatch.stop();

        // then
        assertThat(people.size())
                .as("#1 The number of loaded models is %,d", mocks.size())
                .isEqualTo(mocks.size());
        assertThat(people)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Human[0]));
    }

}
