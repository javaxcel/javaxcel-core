package com.github.javaxcel.out;

import com.github.javaxcel.factory.ExcelWriterFactory;
import com.github.javaxcel.junit.annotation.StopwatchProvider;
import com.github.javaxcel.model.creature.Human;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import com.github.javaxcel.styler.ExcelStyleConfig;
import io.github.imsejin.common.tool.Stopwatch;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@StopwatchProvider
class ModelWriterTest {

    /**
     * @see AbstractExcelWriter#autoResizeColumns()
     * @see AbstractExcelWriter#hideExtraRows()
     * @see AbstractExcelWriter#hideExtraColumns()
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
                .autoResizeColumns().hideExtraRows().hideExtraColumns()
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
