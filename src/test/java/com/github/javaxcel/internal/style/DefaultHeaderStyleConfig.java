package com.github.javaxcel.internal.style;

import com.github.javaxcel.styler.config.Configurer;
import com.github.javaxcel.styler.ExcelStyleConfig;
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

    public static ExcelStyleConfig[] getRainbowHeader() {
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
