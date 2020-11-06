package com.github.javaxcel.converter;

import java.lang.reflect.Field;

public interface ReadingConverter<T> {

    /**
     * Converts a string in cell to the type of field.
     *
     * @param value string that is cell value
     * @param field targeted field of model
     * @return value converted to the type of field
     */
    Object convert(String value, Field field);

}
