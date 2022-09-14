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

package com.github.javaxcel.converter.handler.impl;

import io.github.imsejin.common.constant.DateType;

import java.time.LocalDate;
import java.time.temporal.TemporalQuery;

public class LocalDateTypeHandler extends AbstractTemporalAccessorTypeHandler<LocalDate> {

    public LocalDateTypeHandler() {
        super(LocalDate.class, DateType.F_DATE.getFormatter());
    }

    @Override
    protected TemporalQuery<LocalDate> getTemporalQuery() {
        return LocalDate::from;
    }

}
