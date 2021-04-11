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
