package com.github.javaxcel.out.strategy;

import java.util.Objects;

public abstract class AbstractExcelWriteStrategy implements ExcelWriteStrategy {

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AbstractExcelWriteStrategy)) return false;

        AbstractExcelWriteStrategy that = (AbstractExcelWriteStrategy) obj;
        return Objects.equals(this.getClass(), that.getClass());
    }

}
