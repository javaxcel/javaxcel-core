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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;

public class ZonedDateTimeTypeHandler extends AbstractTemporalAccessorTypeHandler<ZonedDateTime> {

    public ZonedDateTimeTypeHandler() {
        super(ZonedDateTime.class, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z/z"));
    }

    @Override
    protected TemporalQuery<ZonedDateTime> getTemporalQuery() {
        return ZonedDateTime::from;
    }

}
