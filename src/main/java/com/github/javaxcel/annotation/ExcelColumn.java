/*
 * Copyright 2020 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.annotation;

import com.github.javaxcel.out.strategy.impl.DefaultValue;
import com.github.javaxcel.out.strategy.impl.EnumDropdown;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel column.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * Header name on Excel file.
     *
     * @return header name
     */
    String name() default "";

    /**
     * Policy that determines whether this will set constraint to {@link Enum} field or not.
     *
     * <p> If this is {@code true}, this will make the column of {@link Enum} field dropdown.
     *
     * @return whether this column will be dropdown or not.
     * @see EnumDropdown
     * @see ExcelModel#enumDropdown() ExcelModel.enumDropdown
     * @see #dropdownItems() ExcelColumn.dropdownItems
     */
    boolean enumDropdown() default false;

    /**
     * Dropdown items for the constraint.
     *
     * <p> If this is empty, dropdown items are {@link Enum#name() Enum.name}.
     *
     * @return dropdown items
     * @see EnumDropdown
     * @see ExcelModel#enumDropdown() ExcelModel.enumDropdown
     * @see #enumDropdown() ExcelColumn.enumDropdown
     */
    String[] dropdownItems() default {};

    /**
     * Replacement when the value that is null or empty string.
     *
     * <p> <b>When writing</b> a value of the field which is null or empty string, converter replaces it
     * with this default value. <b>When reading</b> a cell value from Excel file which is null or empty string,
     * converter replaces it with this default value.
     *
     * <p> <u>If the field is also annotated with {@link ExcelWriteExpression} or {@link ExcelReadExpression},
     * this value is considered as a expression(SpEL).</u>
     *
     * <pre>{@code
     *      // Regarded as a string.
     *      @ExcelColumn(defaultValue = "[alpha, beta]")
     *      private List<String> strings;
     *
     *      // Regarded as a expression.
     *      @ExcelColumn(defaultValue = "T(java.util.Arrays).asList('alpha', 'beta')")
     *      @ExcelWriteExpression("#strings?.subList(1)")
     *      private List<String> strings;
     * }</pre>
     *
     * <p> This is ineffective to a field whose type is primitive, <b>when writing</b>.
     * Because primitive type doesn't allow {@code null}.
     *
     * @return replacement of the value when the value is null or empty string
     * @see DefaultValue
     * @see ExcelModel#defaultValue() ExcelModel.defaultValue
     */
    String defaultValue() default "";

    /**
     * Configuration of header style.
     *
     * <p> This takes precedence over {@link ExcelModel#headerStyle() ExcelModel.headerStyle}.
     *
     * @return configuration of header style
     * @see ExcelModel#headerStyle() ExcelModel.headerStyle
     */
    Class<? extends ExcelStyleConfig> headerStyle() default NoStyleConfig.class;

    /**
     * Configuration of body style.
     *
     * <p> This takes precedence over {@link ExcelModel#bodyStyle() ExcelModel.bodyStyle}.
     *
     * @return configuration of body style
     * @see ExcelModel#bodyStyle() ExcelModel.bodyStyle
     */
    Class<? extends ExcelStyleConfig> bodyStyle() default NoStyleConfig.class;

}
