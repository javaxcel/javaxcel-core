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
import org.junit.jupiter.api.extension.*;

/**
 * @see StopwatchProvider
 */
public class StopwatchProviderExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    protected Stopwatch stopwatch = new Stopwatch();

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        extensionContext.getTestClass().ifPresent(clazz -> {
            StopwatchProvider annotation = clazz.getAnnotation(StopwatchProvider.class);
            if (annotation != null) this.stopwatch.setTimeUnit(annotation.value());
        });

        // Time unit on method takes precedence over on class.
        extensionContext.getTestMethod().ifPresent(method -> {
            StopwatchProvider annotation = method.getAnnotation(StopwatchProvider.class);
            if (annotation != null) this.stopwatch.setTimeUnit(annotation.value());
        });
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (this.stopwatch.isRunning()) this.stopwatch.stop();
        System.out.println(this.stopwatch.getStatistics());

        /*
        If test method is annotated with @ParameterizedTest or @RepeatedTest
        or test class is annotated with @StopwatchProvider,
        a instance of this extension is reused.
        It seems that the test cases are in a group.
        */
        if (!this.stopwatch.hasNeverBeenStopped()) this.stopwatch = new Stopwatch();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(Stopwatch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        // Parameter[] params = parameterContext.getDeclaringExecutable().getParameters();
        // System.out.println(params[0].getType());
        // System.out.println(params[0].getParameterizedType().getTypeName());
        // System.out.println(params[0].getName());
        // System.out.println(params[0].getClass());
        // System.out.println(params[0].getDeclaringExecutable().getDeclaringClass());
        // System.out.println(params[0].getDeclaringExecutable().getParameterCount());
        // System.out.println(Arrays.toString(params[0].getDeclaringExecutable().getParameterTypes()));

        return this.stopwatch;
    }

}
