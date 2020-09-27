package com.github.javaxcel.exception;

public class NotExistConverterException extends RuntimeException {

    private final Class<?> type;

    public NotExistConverterException(Class<?> type) {
        super(String.format("Cannot find the converter method in the class(%s)", type.getName()));
        this.type = type;
    }

    public NotExistConverterException(String message, Class<?> type) {
        super(message);
        this.type = type;
    }

    public NotExistConverterException(String message, Throwable cause, Class<?> type) {
        super(message, cause);
        this.type = type;
    }

    public NotExistConverterException(Throwable cause, Class<?> type) {
        super(String.format("Cannot find the converter method in the class(%s)", type.getName()), cause);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

}
