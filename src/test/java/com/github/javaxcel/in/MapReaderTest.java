package com.github.javaxcel.in;

import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.Mockables;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class MapReaderTest {

    private static Map<String, Object> getRandomMap(@Nonnull List<String> keys) {
        return keys.stream().collect(toMap(it -> it, it -> Mockables.generateRandomText(it.length())));
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void read(@TempDir Path path, Stopwatch stopwatch) {
        String filename = "maps.xlsx";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");

        // given
        stopwatch.start(String.format("create '%s' file", filename));
        File file = new File(path.toFile(), filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new HSSFWorkbook();
        stopwatch.stop();

        int numOfMocks = ExcelUtils.getMaxRows(workbook) + 10_000;
        stopwatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                                                 .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopwatch.stop();

        stopwatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook).sheetName("Maps")
                .headerNames(keys).write(out, maps);
        stopwatch.stop();

        // when
        stopwatch.start(String.format("read %,d maps", numOfMocks));
        @Cleanup Workbook wb = new HSSFWorkbook(new FileInputStream(file));
        List<Map<String, Object>> actual = ExcelReaderFactory.create(wb).read();
        stopwatch.stop();

        // then
        assertThat(actual.size())
                .as("#1 The number of loaded maps is %,d", maps.size())
                .isEqualTo(maps.size());
        assertThat(actual)
                .as("#2 Each loaded map is equal to each mock")
                .containsExactly(maps.toArray(new Map[0]));
    }

}