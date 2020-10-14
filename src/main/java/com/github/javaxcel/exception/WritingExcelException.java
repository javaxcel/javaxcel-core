package com.github.javaxcel.exception;

public class WritingExcelException extends RuntimeException {

    public WritingExcelException() {
        super("Failed to write data to the excel sheet");
    }

    public WritingExcelException(String message) {
        super(message);
    }

    public WritingExcelException(String message, Throwable cause) {
        super(message, cause);
    }

    public WritingExcelException(Throwable cause) {
        super("Failed to write data to the excel sheet", cause);
    }

}
