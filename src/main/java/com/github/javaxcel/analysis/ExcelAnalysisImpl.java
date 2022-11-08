package com.github.javaxcel.analysis;

import com.github.javaxcel.converter.handler.ExcelTypeHandler;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.util.Objects;

public final class ExcelAnalysisImpl implements ExcelAnalysis {

    private final Field field;

    private int flags;

    private DefaultMeta defaultMeta;

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

    @Override
    public DefaultMeta getDefaultMeta() {
        return this.defaultMeta;
    }

    public void setDefaultMeta(DefaultMeta defaultMeta) {
        this.defaultMeta = Objects.requireNonNull(defaultMeta,
                () -> getClass().getSimpleName() + ".defaultMeta cannot be null");
    }

    @Null
    @Override
    public ExcelTypeHandler<?> getHandler() {
        return this.handler;
    }

    public void setHandler(ExcelTypeHandler<?> handler) {
        this.handler = Objects.requireNonNull(handler,
                () -> getClass().getSimpleName() + ".handler cannot be null");
    }

    // -------------------------------------------------------------------------------------------------

    public static final class DefaultMetaImpl implements DefaultMeta {
        public static final DefaultMetaImpl EMPTY = new DefaultMetaImpl(null, Source.NONE);

        private final String value;

        private final Source source;

        public DefaultMetaImpl(@Null String value, Source source) {
            this.value = value;
            this.source = source;
        }

        @Null
        @Override
        public String getValue() {
            return this.value;
        }


        @Override
        public Source getSource() {
            return this.source;
        }
    }

}
