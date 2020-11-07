package com.github.javaxcel.converter.out;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface WritingConverter<T> {

    /**
     * Converts a field's value to the string.
     *
     * <p> To write a value in the cell, this converts a field's value
     * to the string. The converted string will written in cell.
     *
     * @param model element in list
     * @param field field of model
     * @return stringified field's value
     */
    @Nullable
    String convert(T model, Field field);

}
