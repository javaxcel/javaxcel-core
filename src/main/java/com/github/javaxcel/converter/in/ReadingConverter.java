package com.github.javaxcel.converter.in;

import java.lang.reflect.Field;
import java.util.Map;

public interface ReadingConverter {

    /**
     * Converts a string in cell to the type of field.
     *
     * @param variables {@link Map} in which key is the model's field name and
     *                  value is the model's field value
     * @param field     targeted field of model
     * @return value converted to the type of field
     */
    Object convert(Map<String, Object> variables, Field field);

}
