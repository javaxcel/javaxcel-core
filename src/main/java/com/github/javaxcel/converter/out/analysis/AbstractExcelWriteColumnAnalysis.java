package com.github.javaxcel.converter.out.analysis;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.Objects;

public abstract class AbstractExcelWriteColumnAnalysis implements ExcelWriteColumnAnalysis {

    private final Field field;

    private final String defaultValue;

    private ExcelTypeHandler<?> handler;

    protected AbstractExcelWriteColumnAnalysis(Field field, @Null String defaultValue) {
        this.field = field;
        this.defaultValue = defaultValue;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Null
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
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
