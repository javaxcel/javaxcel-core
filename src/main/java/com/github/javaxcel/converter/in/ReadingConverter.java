package com.github.javaxcel.converter.in;

import java.lang.reflect.Field;

public interface ReadingConverter {

    /**
     * Converts a string in cell to the type of field.
     *
     * @param value string that is cell value
     * @param field targeted field of model
     * @return value converted to the type of field
     */
    Object convert(String value, Field field);

}
