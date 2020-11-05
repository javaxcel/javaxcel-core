package com.github.javaxcel.out;

import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.model.Mockables;
import com.github.javaxcel.util.ExcelUtils;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;

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
import static org.assertj.core.api.Assertions.*;

public class MapWriterTest {

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

    @Test
    @SneakyThrows
    public void write() {
        Stopwatch stopWatch = new Stopwatch(TimeUnit.SECONDS);
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
        System.out.println(stopWatch.getStatistics());
    }

}
