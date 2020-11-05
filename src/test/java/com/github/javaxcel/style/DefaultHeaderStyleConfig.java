package com.github.javaxcel.style;

import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.styler.config.ExcelStyleConfig;
import org.apache.poi.ss.usermodel.*;

public class DefaultHeaderStyleConfig implements ExcelStyleConfig {

    @Override
    public void configure(Configurer configurer) {
        configurer.alignment()
                .horizontal(HorizontalAlignment.CENTER)
                .vertical(VerticalAlignment.CENTER);
        configurer.background(FillPatternType.SOLID_FOREGROUND, IndexedColors.GREY_25_PERCENT);
        configurer.border()
                .leftAndRight(BorderStyle.THIN, IndexedColors.BLACK)
                .bottom(BorderStyle.MEDIUM, IndexedColors.BLACK);
        configurer.font()
                .name("Arial")
                .size(12)
                .bold();
    }

}
