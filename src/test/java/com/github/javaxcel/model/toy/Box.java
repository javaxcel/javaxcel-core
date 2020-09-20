package com.github.javaxcel.model.toy;

import java.util.List;

public interface Box<T> {

    T get(int i);

    List<T> getAll();

    Box<T> put(T... t);

    Box<T> put(int i, T... t);

    Box<T> putAll(List<T> t);

    Box<T> putAll(int i, List<T> t);

    int size();

}
