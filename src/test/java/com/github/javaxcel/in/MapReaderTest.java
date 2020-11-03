package com.github.javaxcel.in;

import com.github.javaxcel.factory.ExcelReaderFactory;
import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.Mockables;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class MapReaderTest {

    private Stopwatch stopWatch;

    @BeforeEach
    public void beforeEach() {
        this.stopWatch = new Stopwatch(TimeUnit.SECONDS);
    }

    @AfterEach
    public void afterEach() {
        System.out.println(this.stopWatch.getStatistics());
    }

    private static Map<String, Object> getRandomMap(@Nonnull List<String> keys) {
        return keys.stream().collect(toMap(it -> it, it -> Mockables.generateRandomText(it.length())));
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void read() {
        String filename = "maps.xlsx";
        List<String> keys = Arrays.asList("race", "name", "height", "weight", "eyesight", "favoriteFood");

        // given
        stopWatch.start(String.format("create '%s' file", filename));
        File file = new File("/data", filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        @Cleanup Workbook workbook = new XSSFWorkbook();
        stopWatch.stop();

        int numOfMocks = 10_000;
        stopWatch.start(String.format("create %,d mocks", numOfMocks));
        List<Map<String, Object>> maps = IntStream.range(0, numOfMocks)
                .mapToObj(i -> getRandomMap(keys)).collect(toList());
        stopWatch.stop();

        stopWatch.start(String.format("write %,d maps", numOfMocks));
        ExcelWriterFactory.create(workbook).sheetName("Maps").write(out, maps);
        stopWatch.stop();

        // when
        stopWatch.start(String.format("read %,d maps", numOfMocks));
        List<Map<String, Object>> actual = ExcelReaderFactory.create(workbook).read();
        stopWatch.stop();

        // then
        assertThat(actual.size())
                .as("#1 The number of loaded maps is %,s", maps.size())
                .isEqualTo(maps.size());
        assertThat(actual)
                .as("#2 Each loaded map is equal to each mock")
                .containsExactly(maps.toArray(new Map[0]));
    }

}