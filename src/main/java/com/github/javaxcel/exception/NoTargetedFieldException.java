package com.github.javaxcel.exception;

public class NoTargetedFieldException extends RuntimeException {

    private final Class<?> type;

    public NoTargetedFieldException(Class<?> type) {
        super("Cannot find the targeted fields in the class(" + type.getName() + ")");
        this.type = type;
    }

    public NoTargetedFieldException(String message, Class<?> type) {
        super(message);
        this.type = type;
    }

    public NoTargetedFieldException(String message, Throwable cause, Class<?> type) {
        super(message, cause);
        this.type = type;
    }

    public NoTargetedFieldException(Throwable cause, Class<?> type) {
        super("Cannot find the targeted fields in the class(" + type.getName() + ")", cause);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

}
