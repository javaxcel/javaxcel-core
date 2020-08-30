package com.github.javaxcel.model;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class ToyBox<T> implements Box<T> {

    private final List<T> list = new ArrayList<>();

    public ToyBox(List<T> toys) {
        this.list.addAll(toys);
    }

    @Override
    public T get(int i) {
        return this.list.get(i);
    }

    @Override
    public List<T> getAll() {
        return this.list;
    }

    @SafeVarargs
    @Override
    public final ToyBox<T> put(T... t) {
        this.list.addAll(Arrays.asList(t));
        return this;
    }

    @SafeVarargs
    @Override
    public final ToyBox<T> put(int i, T... t) {
        this.list.addAll(i, Arrays.asList(t));
        return this;
    }

    @Override
    public ToyBox<T> putAll(List<T> t) {
        this.list.addAll(t);
        return this;
    }

    @Override
    public ToyBox<T> putAll(int i, List<T> t) {
        this.list.addAll(t);
        return this;
    }

    @Override
    public int size() {
        return this.list.size();
    }

}
