package com.github.javaxcel.exception;

import java.lang.reflect.Field;

public class GettingFieldValueException extends RuntimeException {

    private final Class<?> type;

    private final Field field;

    public GettingFieldValueException(Class<?> type, Field field) {
        super(String.format("Failed to get value in the field(%s) of the class(%s)", field.getName(), type.getName()));
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(String message, Class<?> type, Field field) {
        super(message);
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(String message, Throwable cause, Class<?> type, Field field) {
        super(message, cause);
        this.type = type;
        this.field = field;
    }

    public GettingFieldValueException(Throwable cause, Class<?> type, Field field) {
        super(String.format("Failed to get value in the field(%s) of the class(%s)", field.getName(), type.getName()), cause);
        this.type = type;
        this.field = field;
    }

    public Class<?> getType() {
        return type;
    }

    public Field getField() {
        return field;
    }

}
