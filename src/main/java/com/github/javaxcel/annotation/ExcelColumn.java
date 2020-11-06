package com.github.javaxcel.annotation;

import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * Header name.
     *
     * @return header name
     */
    String name() default "";

    /**
     * Replacement of the value when the value is null or empty string.
     *
     * @return replacement of the value when the value is null or empty string
     */
    String defaultValue() default "";

    /**
     * Configuration of header style.
     *
     * <p> this takes precedence over {@link ExcelModel#headerStyle()}.
     *
     * @return configuration of header style
     * @see ExcelModel#headerStyle()
     */
    Class<? extends ExcelStyleConfig> headerStyle() default NoStyleConfig.class;

    /**
     * Configuration of body style.
     *
     * <p> this takes precedence over {@link ExcelModel#bodyStyle()}.
     *
     * @return configuration of body style
     * @see ExcelModel#bodyStyle()
     */
    Class<? extends ExcelStyleConfig> bodyStyle() default NoStyleConfig.class;

}
