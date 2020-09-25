package com.github.javaxcel.converter;

import java.lang.reflect.Field;

public interface WritingConverter<T> {

    /**
     * Converts a field's value to the string.
     *
     * <p> To write a value in the cell, this converts a field's value
     * to the string. The converted string will written in cell.
     *
     * @param model object in list
     * @param field field of object
     * @return stringified field's value
     */
    String convert(T model, Field field);

}
