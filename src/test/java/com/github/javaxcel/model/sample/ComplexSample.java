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

package com.github.javaxcel.model.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public class ComplexSample<S extends UUID, T, C extends Iterable<Double>> {

    Long concrete;
    ComplexSample raw;
    ComplexSample<UUID, String, Set<Double>> generic;
    ComplexSample<UUID, String, Set<Double>>[] generic_array;
    T type_variable;
    T[] type_variable_array;
    T[][] type_variable_2d_array;
    S bounded_type_variable;
    S[] bounded_type_variable_array;
    S[][] bounded_type_variable_2d_array;
    C bounded_iterable_type_variable;
    C[] bounded_iterable_type_variable_array;
    C[][] bounded_iterable_type_variable_2d_array;
    ArrayList iterable;
    List<?> iterable_unknown;
    Collection<Long> iterable_concrete;
    Iterable<Long>[] iterable_concrete_array;
    ArrayList<ComplexSample> iterable_raw;
    List<ComplexSample<UUID, ?, Collection<Double>>> iterable_generic;
    Collection<? extends Long> iterable_upper_wildcard_concrete;
    Iterable<? super Long> iterable_lower_wildcard_concrete;
    ArrayList<? extends ComplexSample<UUID, ?, Queue<Double>>> iterable_upper_wildcard_generic;
    List<? super ComplexSample<UUID, ?, Deque<Double>>> iterable_lower_wildcard_generic;
    Collection<T> iterable_type_variable;
    Iterable<T[]> iterable_type_variable_array;
    ArrayList<? extends T> iterable_upper_wildcard_type_variable;
    List<? super T> iterable_lower_wildcard_type_variable;
    Collection<? extends T[]> iterable_upper_wildcard_type_variable_array;
    Iterable<? super T[]> iterable_lower_wildcard_type_variable_array;
    ArrayList<S> iterable_bounded_type_variable;
    List<S[]> iterable_bounded_type_variable_array;
    Collection<? extends S> iterable_upper_wildcard_bounded_type_variable;
    Iterable<? super S> iterable_lower_wildcard_bounded_type_variable;
    ArrayList<? extends S[]> iterable_upper_wildcard_bounded_type_variable_array;
    List<? super S[]> iterable_lower_wildcard_bounded_type_variable_array;
    Collection<C> iterable_bounded_iterable_type_variable;
    Iterable<Set<ComplexSample<UUID, String, Queue<Double>>>> iterable_iterable_generic;

}
