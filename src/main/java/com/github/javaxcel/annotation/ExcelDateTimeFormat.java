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
     * @see com.github.javaxcel.util.TypeClassifier#isTemporal(Class)
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
