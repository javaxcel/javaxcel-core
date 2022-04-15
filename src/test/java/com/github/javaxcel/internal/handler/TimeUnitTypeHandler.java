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

package com.github.javaxcel.internal.handler;

import com.github.javaxcel.converter.handler.AbstractExcelTypeHandler;

import java.util.concurrent.TimeUnit;

public class TimeUnitTypeHandler extends AbstractExcelTypeHandler<TimeUnit> {

    public TimeUnitTypeHandler() {
        super(TimeUnit.class);
    }

    public static boolean validate(String value) {
        switch (value) {
            case "days":
            case "hrs":
            case "min":
            case "sec":
            case "s":
            case "μs":
            case "ns":
                return true;
            default:
                return false;
        }
    }

    @Override
    protected String writeInternal(TimeUnit value, Object... args) {
        switch (value) {
            case DAYS:
                return "days";
            case HOURS:
                return "hrs";
            case MINUTES:
                return "min";
            case SECONDS:
                return "sec";
            case MILLISECONDS:
                return "s";
            case MICROSECONDS:
                return "μs";
            case NANOSECONDS:
                return "ns";
            default:
                throw new IllegalStateException("Invalid TimeUnit constant: " + value);
        }
    }

    @Override
    public TimeUnit read(String value, Object... args) throws Exception {
        switch (value) {
            case "days":
                return TimeUnit.DAYS;
            case "hrs":
                return TimeUnit.HOURS;
            case "min":
                return TimeUnit.MINUTES;
            case "sec":
                return TimeUnit.SECONDS;
            case "s":
                return TimeUnit.MILLISECONDS;
            case "μs":
                return TimeUnit.MICROSECONDS;
            case "ns":
                return TimeUnit.NANOSECONDS;
            default:
                throw new IllegalStateException("Invalid TimeUnit value: " + value);
        }
    }

}
