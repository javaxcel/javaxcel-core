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

package com.github.javaxcel.annotation;

import com.github.javaxcel.in.processor.ExcelModelCreationProcessor;
import com.github.javaxcel.in.resolver.AbstractExcelModelExecutableResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Executable;

/**
 * Indicates that a constructor or method is used to instantiate their declaring class.
 * {@link AbstractExcelModelExecutableResolver} finds {@link Executable} as a creator of the model.
 * The creator is invoked by {@link ExcelModelCreationProcessor}.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelModelCreator {

    /**
     * Indicates that name of parameter on creator of the model is mapped by
     * one of the fields in the model, when field name of the model doesn't match
     * the name of parameter.
     *
     * <pre>{@code
     *     class MyModel {
     *         private String name;
     *         private String title;
     *
     *         MyModel(@FieldName("name") String s1, @FieldName("title") String s2) {
     *             this.name = s1;
     *             this.title = s2;
     *         }
     *     }
     * }</pre>
     *
     * <p> If this value doesn't match with any field name, you will meet exception.
     * If the value is the same as one of other values on creator of the model,
     * you will also meet exception.
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FieldName {
        /**
         * Replacement of parameter name with field name.
         *
         * @return field name
         */
        String value();
    }

}
