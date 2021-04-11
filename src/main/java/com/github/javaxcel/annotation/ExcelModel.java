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

import com.github.javaxcel.out.ModelWriter;
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
     * Policy that determines whether this will set constraint to {@link Enum} field or not.
     *
     * <p> If this is {@code true}, this will make the column of {@link Enum} field dropdown.
     *
     * @return whether this column will be dropdown or not.
     * @see ModelWriter#enumDropdown()
     * @see ExcelColumn#enumDropdown()
     * @see ExcelColumn#dropdownItems()
     */
    boolean enumDropdown() default false;

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
