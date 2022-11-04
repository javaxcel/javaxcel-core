package com.github.javaxcel.analysis;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.Objects;

public class ExcelAnalysisImpl implements ExcelAnalysis {

    private final Field field;

    private int flags;

    private String defaultValue;

    private ExcelTypeHandler<?> handler;

    public ExcelAnalysisImpl(Field field) {
        this.field = field;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    @Null
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, () -> getClass().getSimpleName() + ".defaultValue cannot be null");
    }

    @Null
    @Override
    public ExcelTypeHandler<?> getHandler() {
        return this.handler;
    }

    public void setHandler(ExcelTypeHandler<?> handler) {
        this.handler = Objects.requireNonNull(handler, () -> getClass().getSimpleName() + ".handler cannot be null");
    }

}
