package com.github.javaxcel.out;

import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.Mockables;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MapWriterTest {

    private Stopwatch stopWatch;

    @SneakyThrows
    private static long getNumOfWrittenModels(Class<? extends Workbook> type, File file) {
        @Cleanup
        Workbook workbook = type == HSSFWorkbook.class
                ? new HSSFWorkbook(new FileInputStream(file))
                : new XSSFWorkbook(file);
        return ExcelUtils.getNumOfModels(workbook);
    }

    private static Map<String, Object> getRandomMap(@Nonnull List<String> keys) {
        return keys.stream().collect(toMap(it -> it, it -> Mockables.generateRandomText(it.length())));
    }

    @BeforeEach
    public void beforeEach() {
        this.stopWatch = new Stopwatch(TimeUnit.SECONDS);
    }

    @AfterEach
    public void afterEach() {
        System.out.println(this.stopWatch.getStatistics());
    }

    @Test
    @SneakyThrows
    public void write() {
        String filename = "maps.xlsx";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup SXSSFWorkbook workbook = new SXSSFWorkbook();
        stopWatch.stop();

        final int numOfMocks = ExcelUtils.getMaxRows(workbook) / 10;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook)
                .sheetName("Maps")
                .write(out, maps);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(XSSFWorkbook.class, file))
                .as("#2 The number of actually written maps is %,d", maps.size())
                .isEqualTo(maps.size());
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
        String filename = "maps-styled.xls";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "strength", "eyesight", "favoriteFood");

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        stopWatch.stop();

        final int numOfMocks = 1000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopWatch.stop();

        // when
        stopWatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook)
                .sheetName("Maps")
                .headerNames(keys.stream().map(String::toUpperCase).collect(toList()))
                .autoResizeCols().hideExtraCols()
                .headerStyles(getRainbowHeader())
                .bodyStyles(new DefaultBodyStyleConfig())
                .write(out, maps);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(getNumOfWrittenModels(HSSFWorkbook.class, file))
                .as("#2 The number of actually written maps is %,d", maps.size())
                .isEqualTo(maps.size());
    }

    private static ExcelStyleConfig[] getRainbowHeader() {
        ExcelStyleConfig r = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.RED)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig a = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.ORANGE)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig i = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GOLD)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold();
        ExcelStyleConfig n = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREEN)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig b = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.BLUE)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig o = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.INDIGO)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);
        ExcelStyleConfig w = it -> it.alignment()
                .horizontal(HorizontalAlignment.CENTER).vertical(VerticalAlignment.CENTER)
                .and()
                .background(FillPatternType.SOLID_FOREGROUND, IndexedColors.VIOLET)
                .border().all(BorderStyle.THIN, IndexedColors.GREY_25_PERCENT)
                .and()
                .font().name("Arial").size(12).bold().color(IndexedColors.WHITE);

        return new ExcelStyleConfig[]{r, a, i, n, b, o, w};
    }

}
