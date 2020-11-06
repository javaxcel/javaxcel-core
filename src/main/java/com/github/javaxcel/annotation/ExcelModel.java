package com.github.javaxcel.annotation;

import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelModel {

    /**
     * Policy that determines whether this will select super classes' fields or own fields.
     *
     * <p> If this is {@code true}, this will select declared own fields including the inherited.
     * Otherwise, this will select only declared own fields except the inherited.
     *
     * @return policy that determines whether this will select super classes' fields or not
     */
    boolean includeSuper() default false;

    /**
     * Policy that determines whether this will select explicitly designated fields or not.
     *
     * <p> If this is {@code true}, this will select the fields that annotated with {@link ExcelColumn}.
     * Otherwise, this will select all the fields whether it is annotated with {@link ExcelColumn} or not.
     *
     * @return policy that determines whether this will select explicitly designated fields or not.
     */
    boolean explicit() default false;

    /**
     * Replacement of the value when the value is null or empty string.
     *
     * @return replacement of the value when the value is null or empty string
     */
    String defaultValue() default "";

    /**
     * Configuration of common header style.
     *
     * <p> this configuration is applied to all header cells.
     *
     * @return configuration of common header style
     * @see ExcelColumn#headerStyle()
     */
    Class<? extends ExcelStyleConfig> headerStyle() default NoStyleConfig.class;

    /**
     * Configuration of common body style.
     *
     * <p> this configuration is applied to all body cells.
     *
     * @return configuration of common body style
     * @see ExcelColumn#bodyStyle()
     */
    Class<? extends ExcelStyleConfig> bodyStyle() default NoStyleConfig.class;

}
