package com.github.javaxcel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDateTimeFormat {

    /**
     * Pattern for formatting datetime.
     *
     * @return pattern for formatting datetime
     * @see java.time.LocalDate
     * @see java.time.LocalTime
     * @see java.time.LocalDateTime
     * @see java.time.format.DateTimeFormatter
     */
    String pattern();

    /**
     * Timezone for formatting datetime.
     *
     * @return timezone for formatting datetime
     */
    @Deprecated
    String timezone() default "";

}
