package com.github.javaxcel.out;

import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.Mockables;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class MapWriterTest {

    private static Map<String, Object> getRandomMap(@Nonnull List<String> keys) {
        return keys.stream().collect(toMap(it -> it, it -> Mockables.generateRandomText(it.length())));
    }

    private static <E> Map<E, Integer> toIndexedMap(Collection<E> collection) {
        return collection.stream().collect(HashMap<E, Integer>::new,
                (map, val) -> map.put(val, map.size()),
                (map, map2) -> {
                });
    }

    @Test
    @DisplayName("Rearrange keys")
    void rearrangeKeys(Stopwatch stopwatch) {
        // given
        stopwatch.start();
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");
        System.out.printf("original keys: %s\n", keys);

        // when
        List<String> rearrangedKeys = keys.stream().sorted().collect(toList());
        System.out.printf("rearranged keys: %s\n", rearrangedKeys);
        Map<String, Integer> indexedMap = toIndexedMap(rearrangedKeys);
        keys.sort(comparing(indexedMap::get));
        stopwatch.stop();

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
    void write(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "maps.xlsx";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup SXSSFWorkbook workbook = new SXSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = ExcelUtils.getMaxRows(workbook) / 10;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook)
                .sheetName("Maps")
                .headerNames(keys)
                .write(out, maps);
        stopwatch.stop();

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
    void writeAndDecorate(Stopwatch stopwatch) {
        String filename = "maps-styled.xls";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "strength", "eyesight", "favoriteFood");

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup HSSFWorkbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        final int numOfMocks = 1000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopwatch.stop();

        // when
        stopwatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook)
                .sheetName("Maps")
                .headerNames(keys, Arrays.asList("RACE", "NAME", "HEIGHT", "WEIGHT", "STRENGTH", "EYE_SIGHT", "FAVORITE_FOOD"))
                .unrotate()
                .autoResizeCols().hideExtraCols()
                .headerStyles(DefaultHeaderStyleConfig.getRainbowHeader())
                .bodyStyles(new DefaultBodyStyleConfig())
                .write(out, maps);
        stopwatch.stop();

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
