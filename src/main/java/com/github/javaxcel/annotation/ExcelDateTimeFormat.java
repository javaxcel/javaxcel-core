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
     */
    String pattern();

    /**
     * Timezone for formatting datetime.
     *
     * @return timezone for formatting datetime
     */
    String timezone() default "";

}
