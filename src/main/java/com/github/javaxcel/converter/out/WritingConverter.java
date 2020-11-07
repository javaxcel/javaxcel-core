package com.github.javaxcel.converter.out;

import java.lang.reflect.Field;

public interface WritingConverter<T> {

    /**
     * Returns the default value.
     *
     * @return default value
     */
    String getDefaultValue();

    /**
     * Sets up the default value.
     *
     * @param defaultValue default value
     */
    void setDefaultValue(String defaultValue);

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
    String convert(T model, Field field);

}
