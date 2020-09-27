package com.github.javaxcel.exception;

import java.lang.reflect.Field;

public class SettingFieldValueException extends RuntimeException {

    private final Class<?> type;

    private final Field field;

    public SettingFieldValueException(Class<?> type, Field field) {
        super(String.format("Failed to set value into the field(%s) of the class(%s)", field.getName(), type.getName()));
        this.type = type;
        this.field = field;
    }

    public SettingFieldValueException(String message, Class<?> type, Field field) {
        super(message);
        this.type = type;
        this.field = field;
    }

    public SettingFieldValueException(String message, Throwable cause, Class<?> type, Field field) {
        super(message, cause);
        this.type = type;
        this.field = field;
    }

    public SettingFieldValueException(Throwable cause, Class<?> type, Field field) {
        super(String.format("Failed to set value into the field(%s) of the class(%s)", field.getName(), type.getName()), cause);
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
