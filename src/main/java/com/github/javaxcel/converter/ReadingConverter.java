package com.github.javaxcel.converter;

import java.lang.reflect.Field;

public interface ReadingConverter<T> {

    Object convert(String value, Field field);

}
