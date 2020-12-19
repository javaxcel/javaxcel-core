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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MapWriterTest {

    private Stopwatch stopWatch;

    private static Map<String, Object> getRandomMap(@Nonnull List<String> keys) {
        return keys.stream().collect(toMap(it -> it, it -> Mockables.generateRandomText(it.length())));
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

    private static <E> Map<E, Integer> toIndexedMap(Collection<E> collection) {
        return collection.stream().collect(HashMap<E, Integer>::new,
                (map, val) -> map.put(val, map.size()),
                (map, map2) -> {
                });
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
    @DisplayName("Rearrange keys")
    public void rearrangeKeys() {
        // given
        stopWatch.start();
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");
        System.out.printf("original keys: %s\n", keys);

        // when
        List<String> rearrangedKeys = keys.stream().sorted().collect(toList());
        System.out.printf("rearranged keys: %s\n", rearrangedKeys);
        Map<String, Integer> indexedMap = toIndexedMap(rearrangedKeys);
        keys.sort(comparing(indexedMap::get));
        stopWatch.stop();

        // then
        assertThat(rearrangedKeys)
                .as("Original keys must be equal to rearranged keys")
                .containsExactlyElementsOf(keys);
        System.out.printf("original keys: %s\n", keys);
    }

    /**
     * @see MapWriter#headerNames(List)
     */
    @Test
    @DisplayName("headerNames(List)")
    @SneakyThrows
    public void write(@TempDir Path path) {
        String filename = "maps.xlsx";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
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
                .headerNames(keys)
                .write(out, maps);
        stopWatch.stop();

        // then
        assertThat(file)
                .as("#1 Excel file will be created")
                .isNotNull()
                .exists();
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written maps is %,d", maps.size())
                .isEqualTo(maps.size());
    }

    /**
     * @see MapWriter#headerNames(List, List)
     * @see AbstractExcelWriter#autoResizeCols()
     * @see AbstractExcelWriter#hideExtraRows()
     * @see AbstractExcelWriter#hideExtraCols()
     * @see AbstractExcelWriter#headerStyles(ExcelStyleConfig...)
     * @see AbstractExcelWriter#bodyStyles(ExcelStyleConfig...)
     */
    @Test
    @DisplayName("Decorate + headerNames(List, List)")
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
                .headerNames(keys, Arrays.asList("RACE", "NAME", "HEIGHT", "WEIGHT", "STRENGTH", "EYE_SIGHT", "FAVORITE_FOOD"))
                .disableRolling()
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
        assertThat(ExcelUtils.getNumOfModels(file))
                .as("#2 The number of actually written maps is %,d", maps.size())
                .isEqualTo(maps.size());
    }

}
