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

package com.github.javaxcel.util.resolver.impl;

import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.exception.AmbiguousExcelModelCreatorException;
import com.github.javaxcel.util.resolver.AbstractExcelModelExecutableResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class ExcelModelConstructorResolver<T> extends AbstractExcelModelExecutableResolver<T, Constructor<T>> {

    private static final Class<?> EXECUTABLE_TYPE = Constructor.class;

    @SuppressWarnings("unchecked")
    public ExcelModelConstructorResolver(Class<T> modelType) {
        super(modelType, (Class<Constructor<T>>) EXECUTABLE_TYPE);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Constructor<T>> getCandidates() {
        List<Constructor<T>> candidates = new ArrayList<>();
        for (Constructor<?> candidate : modelType.getDeclaredConstructors()) {
            candidates.add((Constructor<T>) candidate);
        }

        candidates.sort(comparing(Executable::getModifiers, new ModifierComparator()));
        return Collections.unmodifiableList(candidates);
    }

    protected Constructor<T> elect(List<Constructor<T>> candidates) {
        // Sole candidate.
        if (candidates.size() == 1) return candidates.get(0);

        List<Constructor<T>> elected = new ArrayList<>();
        for (Constructor<T> candidate : candidates) {
            ExcelModelCreator annotation = candidate.getAnnotation(ExcelModelCreator.class);
            if (annotation == null) continue;

            elected.add(candidate);
        }

        if (elected.isEmpty()) {
            // There are two or more candidates that aren't annotated with that.
            throw new AmbiguousExcelModelCreatorException("Ambiguous constructors%s to resolve; " +
                    "Annotate constructor you want with @ExcelModelCreator", candidates);

        } else if (elected.size() > 1) {
            // There are two or more candidates that are annotated with that.
            throw new AmbiguousExcelModelCreatorException("Ambiguous constructors%s to resolve; " +
                    "Remove @ExcelModelCreator from other constructors except the one", elected);
        }

        // Elected candidate.
        return elected.get(0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    public static class ModifierComparator implements Comparator<Integer> {
        private static int convert(Integer modifier) {
            if (Modifier.isPublic(modifier)) return 0;
            if (Modifier.isProtected(modifier)) return 1;
            if (Modifier.isPrivate(modifier)) return 3;
            return 2; // package-private
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(convert(o1), convert(o2));
        }
    }

}
