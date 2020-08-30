package com.github.javaxcel.annotation;

import com.github.javaxcel.constant.TargetedFieldPolicy;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelModel {

    /**
     * Policy that determines the range of fields to use as columns of excel sheet.
     *
     * @return policy that determines the range of fields to use as columns of excel sheet
     */
    TargetedFieldPolicy policy() default TargetedFieldPolicy.OWN_FIELDS;

}
