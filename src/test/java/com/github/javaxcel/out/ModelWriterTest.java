package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.computer.Computer;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class ModelWriterTest {

    /**
     * When write 349,525 mocks,
     * <p> 1. XSSFWorkbook: 45 sec
     * <p> 2. SXSSFWorkbook: 6 sec
     *
     * @see com.github.javaxcel.annotation.ExcelIgnore
     * @see ExcelColumn#defaultValue()
     */
    @Test
    @DisplayName("@ExcelIgnore + @ExcelColumn(defaultValue = \"-1\")")
    @SneakyThrows
    void writeWithProducts(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "products.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup SXSSFWorkbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = ExcelUtils.getMaxRows(workbook) / 10;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Product> products = Product.createRandoms(numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Product.class)
                .sheetName("Products").write(out, products);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written model is %,d", products.size())
                .isEqualTo(products.size());
    }

    /**
     * @see ExcelModel#explicit()
     * @see AbstractExcelWriter#autoResizeCols()
     */
    @Test
    @DisplayName("@ExcelModel(explicit = true) + autoResizeCols()")
    @SneakyThrows
    void writeWithComputers(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "computers.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Computer> computers = Computer.createRandoms(numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Computer.class)
                .autoResizeCols().write(out, computers);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written model is %,d", computers.size())
                .isEqualTo(computers.size());
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelDateTimeFormat#pattern()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true, enumDropdown = true) + @ExcelDateTimeFormat")
    @SneakyThrows
    void writeWithEducationToys(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "toys.xlsx";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
        stopwatch.stop();

        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> toys = new EducationToy().createRandoms(numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, toys);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written model is %,d", toys.size())
                .isEqualTo(toys.size());
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelModel#headerStyle()
     * @see ExcelModel#bodyStyle()
     * @see ExcelColumn#headerStyle()
     * @see ExcelColumn#bodyStyle()
     * @see com.github.javaxcel.annotation.ExcelWriterExpression
     * @see AbstractExcelWriter#unrotate()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelWriterExpression + unrotate() + enumDropdown()")
    @SneakyThrows
    void writePeople(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "people.xls";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        int numOfMocks = SpreadsheetVersion.EXCEL97.getMaxRows() - 1;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class)
                .enumDropdown().unrotate().write(out, people);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written model is %,d", people.size())
                .isEqualTo(people.size());
    }

    /**
     * @see AbstractExcelWriter#autoResizeCols()
     * @see AbstractExcelWriter#hideExtraRows()
     * @see AbstractExcelWriter#hideExtraCols()
     * @see AbstractExcelWriter#headerStyles(ExcelStyleConfig...)
     * @see AbstractExcelWriter#bodyStyles(ExcelStyleConfig...)
     * @see AbstractExcelWriter#unrotate()
     */
    @Test
    @DisplayName("Decorate")
    @SneakyThrows
    void writeAndDecorate(Stopwatch stopwatch) {
        String filename = "people-styled.xls";

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        // when
        int numOfMocks = 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopwatch.stop();

        stopwatch.start(String.format("write and decorate %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class)
                .sheetName("People")
                .autoResizeCols().hideExtraRows().hideExtraCols()
                .headerStyles(new DefaultHeaderStyleConfig())
                .bodyStyles(new DefaultBodyStyleConfig())
                .unrotate().write(out, people);
        stopwatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
    }

}
