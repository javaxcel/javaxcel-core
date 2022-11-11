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

import com.github.javaxcel.converter.handler.ExcelTypeHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Indicates the formatting pattern that applies to field value, when writing and reading.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDateTimeFormat {

    /**
     * Formatting pattern for datetime object.
     *
     * <p> If you want to format other object which is not supported by default,
     * you should implement {@link ExcelTypeHandler} for that object and handle
     * this annotation on the implementation.
     *
     * <p> Supported types are by default:
     * <ul>
     *     <li>{@link Date}</li>
     *     <li>{@link Instant}</li>
     *     <li>{@link LocalDateTime}</li>
     *     <li>{@link LocalDate}</li>
     *     <li>{@link LocalTime}</li>
     *     <li>{@link MonthDay}</li>
     *     <li>{@link Month}</li>
     *     <li>{@link OffsetDateTime}</li>
     *     <li>{@link OffsetTime}</li>
     *     <li>{@link YearMonth}</li>
     *     <li>{@link Year}</li>
     *     <li>{@link ZonedDateTime}</li>
     * </ul>
     *
     * @return formatting pattern
     * @see java.text.SimpleDateFormat
     * @see java.time.format.DateTimeFormatter
     */
    String pattern();

    /**
     * Timezone for datetime object.
     *
     * @return timezone
     * @deprecated not supported yet
     */
    @Deprecated
    String timezone() default "";

}
