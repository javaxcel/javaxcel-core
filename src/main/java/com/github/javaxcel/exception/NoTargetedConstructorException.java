package com.github.javaxcel.exception;

public class NoTargetedConstructorException extends RuntimeException {

    private final Class<?> type;

    public NoTargetedConstructorException(Class<?> type) {
        super(String.format("Cannot find the constructor without parameter in the class(%s)", type.getName()));
        this.type = type;
    }

    public NoTargetedConstructorException(String message, Class<?> type) {
        super(message);
        this.type = type;
    }

    public NoTargetedConstructorException(String message, Throwable cause, Class<?> type) {
        super(message, cause);
        this.type = type;
    }

    public NoTargetedConstructorException(Throwable cause, Class<?> type) {
        super(String.format("Cannot find the constructor without parameter in the class(%s)", type.getName()), cause);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

}
