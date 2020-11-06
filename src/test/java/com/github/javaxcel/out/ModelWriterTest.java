package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.computer.Computer;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.model.etc.AllIgnoredModel;
import com.github.javaxcel.model.etc.NoFieldModel;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModelWriterTest {

    private Stopwatch stopWatch;

    @SneakyThrows
    private static long getNumOfWrittenModels(Class<? extends Workbook> type, File file) {
        @Cleanup
        Workbook workbook = type == HSSFWorkbook.class
                ? new HSSFWorkbook(new FileInputStream(file))
                : new XSSFWorkbook(file);
        return ExcelUtils.getNumOfModels(workbook);
    }

    @BeforeEach
    public void beforeEach() {
        this.stopWatch = new Stopwatch(TimeUnit.SECONDS);
    }

    @AfterEach
    public void afterEach() {
        System.out.println(this.stopWatch.getStatistics());
    }

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
    public void writeWithProducts() {
        String filename = "products.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup SXSSFWorkbook workbook = new SXSSFWorkbook();
        stopWatch.stop();

        final int numOfMocks = ExcelUtils.getMaxRows(workbook) / 10;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Product> products = new Product().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Product.class)
                .sheetName("Products")
                .write(out, products);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", products.size())
                .isEqualTo(products.size());
    }

    /**
     * @see ExcelModel#explicit()
     */
    @Test
    @DisplayName("@ExcelModel(explicit = true)")
    @SneakyThrows
    public void writeWithComputers() {
        String filename = "computers.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new SXSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Computer> computers = Computer.createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Computer.class).write(out, computers);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", computers.size())
                .isEqualTo(computers.size());
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelDateTimeFormat#pattern()
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelDateTimeFormat")
    @SneakyThrows
    public void writeWithEducationToys() {
        String filename = "toys.xlsx";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup XSSFWorkbook workbook = new XSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<EducationToy> toys = new EducationToy().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, EducationToy.class).write(out, toys);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", toys.size())
                .isEqualTo(toys.size());
    }

    @ParameterizedTest
    @ValueSource(classes = {NoFieldModel.class, AllIgnoredModel.class})
    @DisplayName("Model without targeted fields")
    @SneakyThrows
    public void writeWithModelThatHasNoTargetFields(Class<?> type) {
        String filename = type.getSimpleName().toLowerCase() + ".xls";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        stopWatch.stop();

        // when & then
        stopWatch.start(String.format("write '%s' file", filename));
        assertThatThrownBy(() -> ExcelWriterFactory.create(workbook, type).write(out, new ArrayList<>()))
                .as("When write with a model that has targeted fields")
                .isInstanceOf(NoTargetedFieldException.class);
        stopWatch.stop();
    }

    /**
     * @see ExcelModel#includeSuper()
     * @see ExcelModel#headerStyle()
     * @see ExcelModel#bodyStyle()
     * @see ExcelColumn#headerStyle()
     * @see ExcelColumn#bodyStyle()
     * @see com.github.javaxcel.annotation.ExcelWriterExpression
     */
    @Test
    @DisplayName("@ExcelModel(includeSuper = true) + @ExcelWriterExpression")
    @SneakyThrows
    public void writePeople() {
        String filename = "people.xls";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new HSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = SpreadsheetVersion.EXCEL97.getMaxRows() - 1;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class).write(out, people);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(HSSFWorkbook.class, file))
                .as("#2 The number of actually written model is %,d", people.size())
                .isEqualTo(people.size());
    }

    /**
     * @see AbstractExcelWriter#autoResizeCols()
     * @see AbstractExcelWriter#hideExtraRows()
     * @see AbstractExcelWriter#hideExtraCols()
     * @see AbstractExcelWriter#headerStyles(ExcelStyleConfig...)
     * @see AbstractExcelWriter#bodyStyles(ExcelStyleConfig...)
     */
    @Test
    @DisplayName("Adjust sheet + header/body style")
    @SneakyThrows
    public void writeAndDecorate() {
        String filename = "people-styled.xls";

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        stopWatch.stop();

        // when
        int numOfMocks = 1000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Human> people = new Human().createRandoms(numOfMocks);
        stopWatch.stop();

        stopWatch.start(String.format("write and decorate %,d models", numOfMocks));
        ExcelWriterFactory.create(workbook, Human.class).sheetName("People")
                .autoResizeCols().hideExtraRows().hideExtraCols()
                .headerStyles(new DefaultHeaderStyleConfig())
                .bodyStyles(new DefaultBodyStyleConfig())
                .write(out, people);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
    }

}
