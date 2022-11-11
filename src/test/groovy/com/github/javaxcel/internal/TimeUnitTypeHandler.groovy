/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.internal

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler

import java.util.concurrent.TimeUnit

import static java.util.concurrent.TimeUnit.DAYS
import static java.util.concurrent.TimeUnit.HOURS
import static java.util.concurrent.TimeUnit.MICROSECONDS
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.MINUTES
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS;

class TimeUnitTypeHandler extends AbstractExcelTypeHandler<TimeUnit> {

    TimeUnitTypeHandler() {
        super(TimeUnit.class)
    }

    @Override
    protected String writeInternal(TimeUnit value, Object... arguments) {
        switch (value) {
            case DAYS:
                return "days"
            case HOURS:
                return "hrs"
            case MINUTES:
                return "min"
            case SECONDS:
                return "sec"
            case MILLISECONDS:
                return "ms"
            case MICROSECONDS:
                return "μs"
            case NANOSECONDS:
                return "ns"
            default:
                throw new IllegalStateException("Invalid TimeUnit constant: " + value)
        }
    }

    @Override
    TimeUnit read(String value, Object... arguments) throws Exception {
        switch (value) {
            case "days":
                return DAYS
            case "hrs":
                return HOURS
            case "min":
                return MINUTES
            case "sec":
                return SECONDS
            case "ms":
                return MILLISECONDS
            case "μs":
                return MICROSECONDS
            case "ns":
                return NANOSECONDS
            default:
                throw new IllegalStateException("Invalid TimeUnit value: " + value)
        }
    }

    // -------------------------------------------------------------------------------------------------

    static boolean validate(String value) {
        switch (value) {
            case "days":
            case "hrs":
            case "min":
            case "sec":
            case "ms":
            case "μs":
            case "ns":
                return true
            default:
                return false
        }
    }

}
