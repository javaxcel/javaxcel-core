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

package com.github.javaxcel.junit.extension;

import com.github.javaxcel.junit.annotation.StopwatchProvider;
import io.github.imsejin.common.tool.Stopwatch;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * @see StopwatchProvider
 */
public class StopwatchExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    private Stopwatch stopwatch = new Stopwatch();

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        StopwatchProvider annotation = extensionContext.getRequiredTestMethod().getAnnotation(StopwatchProvider.class);
        this.stopwatch.setTimeUnit(annotation.value());
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (this.stopwatch.isRunning()) this.stopwatch.stop();
        System.out.println(this.stopwatch.getStatistics());

        /*
        If test method is annotated with @ParameterizedTest or @RepeatedTest,
        this extension is reused. It seems that the test cases are in a group.
        */
        extensionContext.getTestMethod().ifPresent(method -> {
            ParameterizedTest parameterizedTest = method.getAnnotation(ParameterizedTest.class);
            RepeatedTest repeatedTest = method.getAnnotation(RepeatedTest.class);
            if (parameterizedTest != null || repeatedTest != null) this.stopwatch = new Stopwatch();
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(Stopwatch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return this.stopwatch;
    }

}
