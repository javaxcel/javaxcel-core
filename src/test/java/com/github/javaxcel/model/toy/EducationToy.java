package com.github.javaxcel.model.toy;

import com.github.javaxcel.TestUtils.ExcludeOnPercentage;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import lombok.*;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "targetAges") // Because of default value.
@ExcelModel(includeSuper = true, enumDropdown = true)
public class EducationToy extends Toy {

    @ExcludeOnPercentage(0.5)
    @ExcelColumn(defaultValue = "[]")
    private int[][] targetAges;

    @ExcludeOnPercentage(0.25)
    private String goals;

    @ExcelDateTimeFormat(pattern = "yyyy_MM_dd/HH_mm_ss")
    private Date formattedDate = Date.from(Instant.now().with(ChronoField.MILLI_OF_SECOND, 0));
    private Date date = Date.from(Instant.now().with(ChronoField.MILLI_OF_SECOND, 0));

    @ExcelDateTimeFormat(pattern = "HH/mm/ss/SSS")
    private LocalTime formattedLocalTime = LocalTime.now().withNano(123_000_000); // with 123 ms
    private LocalTime localTime = LocalTime.now().withNano(0);

    @ExcelDateTimeFormat(pattern = "MM. dd. yyyy.")
    private LocalDate formattedLocalDate = LocalDate.now();
    private LocalDate localDate = LocalDate.now();

    @ExcelDateTimeFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime formattedLocalDateTime = LocalDateTime.now().withNano(0);
    private LocalDateTime localDateTime = LocalDateTime.now().withNano(0);

    @ExcelDateTimeFormat(pattern = "yyyyMMddHHmmssz")
    private ZonedDateTime formattedZonedDateTime = ZonedDateTime.now().withNano(0);
    private ZonedDateTime zonedDateTime = ZonedDateTime.now().withNano(0);

    @ExcelDateTimeFormat(pattern = "HH-mm-ss-SSS-Z")
    private OffsetTime formattedOffsetTime = OffsetTime.now().withNano(456_000_000); // with 456 ms
    private OffsetTime offsetTime = OffsetTime.now().withNano(0);

    @ExcelDateTimeFormat(pattern = "yyyyMMddHHmmssZ")
    private OffsetDateTime formattedOffsetDateTime = OffsetDateTime.now().withNano(0);
    private OffsetDateTime offsetDateTime = OffsetDateTime.now().withNano(0);

}
