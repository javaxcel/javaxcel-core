package com.github.javaxcel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * Column name.
     *
     * @return column name
     */
    String value() default "";

    /**
     * Replacement of the value when the value is null or empty string.
     *
     * @return replacement of the value when the value is null or empty string
     */
    String defaultValue() default "";

}
