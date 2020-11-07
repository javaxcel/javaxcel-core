package com.github.javaxcel.converter.in;

import java.lang.reflect.Field;

public abstract class AbstractReadingConverter<T> {

    /**
     * Converts a string in cell to the type of field.
     *
     * @param value string that is cell value
     * @param field targeted field of model
     * @return value converted to the type of field
     */
    abstract Object convert(String value, Field field);

}
