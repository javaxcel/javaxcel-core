/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.constant;

import com.github.javaxcel.annotation.ExcelWriterExpression;

import java.lang.reflect.Field;

public enum ConvertType {

    /**
     * @see com.github.javaxcel.converter.out.BasicWritingConverter
     * @see com.github.javaxcel.converter.in.BasicReadingConverter
     */
    BASIC,

    /**
     * @see com.github.javaxcel.converter.out.ExpressiveWritingConverter
     * @see com.github.javaxcel.converter.in.ExpressiveReadingConverter
     */
    EXPRESSIVE;

    public static ConvertType of(Field field) {
        ExcelWriterExpression annotation = field.getAnnotation(ExcelWriterExpression.class);
        return annotation == null ? BASIC : EXPRESSIVE;
    }

}
