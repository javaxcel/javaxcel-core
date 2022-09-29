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

package com.github.javaxcel.util.resolver;

import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.CollectionUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ExcelModelExecutableParameterNameResolver {

    private final Executable executable;

    private final List<Parameter> parameters;

    public ExcelModelExecutableParameterNameResolver(Executable executable) {
        Asserts.that(executable).isNotNull();

        this.executable = executable;
        this.parameters = Collections.unmodifiableList(Arrays.asList(executable.getParameters()));
    }

    public List<ResolvedParameter> resolve() {
        if (CollectionUtils.isNullOrEmpty(this.parameters)) return Collections.emptyList();

        List<ResolvedParameter> resolvedParameters = this.parameters.stream()
                .map(MethodParameter::forParameter).map(ResolvedParameter::new).collect(toList());

        Asserts.that(resolvedParameters)
                .describedAs("Failed to discover parameter names of {1}[{2}]",
                        this.executable.getClass().getSimpleName().toLowerCase(), this.executable)
                .isNotNull()
                .isNotEmpty()
                .doesNotContainNull()
                .hasSameSizeAs(this.parameters);

        return Collections.unmodifiableList(resolvedParameters);
    }

    // -------------------------------------------------------------------------------------------------

    public static class ResolvedParameter {
        private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

        private final String name;
        private final boolean annotated;
        private final MethodParameter methodParameter;

        private ResolvedParameter(MethodParameter methodParameter) {
            FieldName annotation = methodParameter.getParameterAnnotation(FieldName.class);
            boolean annotated = annotation != null;

            String name;
            if (annotated) {
                name = annotation.value();
            } else {
                methodParameter.initParameterNameDiscovery(DISCOVERER);
                name = methodParameter.getParameterName();
            }

            this.name = name;
            this.annotated = annotated;
            this.methodParameter = methodParameter;
        }

        public String getName() {
            return this.name;
        }

        public Class<?> getType() {
            return this.methodParameter.getParameterType();
        }

        public int getIndex() {
            return this.methodParameter.getParameterIndex();
        }

        public Executable getDeclaringExecutable() {
            return this.methodParameter.getExecutable();
        }

        public boolean isAnnotated() {
            return this.annotated;
        }

        @Override
        public String toString() {
            if (isAnnotated()) {
                this.methodParameter.initParameterNameDiscovery(DISCOVERER);
                String originalName = this.methodParameter.getParameterName();
                return "@FieldName('" + this.name + "') " + getType().getName() + ' ' + originalName;
            } else {
                return getType().getName() + ' ' + this.name;
            }
        }
    }

}
