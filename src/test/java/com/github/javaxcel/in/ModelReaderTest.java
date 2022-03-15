package com.github.javaxcel.in;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.Limit;
import com.github.javaxcel.in.strategy.ExcelReadStrategy.Parallel;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.computer.Computer;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.FinalFieldModel;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.monitorjbl.xlsx.StreamingReader;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class ModelReaderTest {

    /**
     * @see com.github.javaxcel.annotation.ExcelIgnore
     */
    @Test
    @DisplayName("@ExcelIgnore + @ExcelModel(includeSuper = false)")
    @SneakyThrows
    void readProducts(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "products.xls";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new HSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<Product> mocks = TestUtils.getMocks(Product.class, numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, Product.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = new HSSFWorkbook(new FileInputStream(file));
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", numOfMocks);
        List<Product> products = TestUtils.JAVAXCEL.reader(wb, Product.class).read();
        stopwatch.stop();

        // then
        assertThat(products)
                .as("#1 The number of loaded models is %,d", mocks.size())
                .hasSameSizeAs(mocks)
                .as("#2 Each loaded model is equal to each mock")
                .containsExactly(mocks.toArray(new Product[0]))
                .as("#3 Each loaded model has default value")
                .flatMap(it -> Arrays.asList(it.getDates()))
                .isNotNull()
                .doesNotContainNull()
                .containsAll(Arrays.asList(
                        LocalDate.of(1999, 1, 31),
                        LocalDate.of(2009, 7, 31),
                        LocalDate.of(2019, 12, 31)));
    }

    @Test
    @DisplayName("ExcelReader#limit(int)")
    @SneakyThrows
    void readComputers(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "computers.xlsx";

        // given
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int limit = 10;
        int numOfMocks = 1000;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<Computer> mocks = TestUtils.getMocks(Computer.class, numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, Computer.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = StreamingReader.builder().open(file);
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", Math.min(limit, numOfMocks));
        List<Computer> computers = TestUtils.JAVAXCEL.reader(wb, Computer.class)
                .options(new Limit(limit)).read();
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
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<EducationToy> mocks = TestUtils.getMocks(EducationToy.class, numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, EducationToy.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = StreamingReader.builder().open(file);
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", numOfMocks);
        List<EducationToy> educationToys = TestUtils.JAVAXCEL.reader(wb, EducationToy.class).read();
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
        List<FinalFieldModel> list = TestUtils.JAVAXCEL.reader(workbook, FinalFieldModel.class).read();

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
        stopwatch.start("create '%s' file", filename);
        File file = new File(path.toFile(), filename);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        @Cleanup OutputStream out = new FileOutputStream(file);
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start("create %,d mocks", numOfMocks);
        List<Human> mocks = Human.newRandomList(numOfMocks);
        stopwatch.stop();

        stopwatch.start("write %,d models", numOfMocks);
        TestUtils.JAVAXCEL.writer(workbook, Human.class).write(out, mocks);
        stopwatch.stop();

        stopwatch.start("load '%s' file", filename);
        @Cleanup Workbook wb = StreamingReader.builder().open(file);
        stopwatch.stop();

        // when
        stopwatch.start("read %,d models", numOfMocks);
        List<Human> people = TestUtils.JAVAXCEL.reader(wb, Human.class).options(new Parallel()).read();
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
