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
