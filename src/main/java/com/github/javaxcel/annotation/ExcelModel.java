package com.github.javaxcel.annotation;

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

}
