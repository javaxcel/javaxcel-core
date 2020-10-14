package com.github.javaxcel.exception;

public class UnsupportedWorkbookException extends RuntimeException {

    public UnsupportedWorkbookException() {
        super("SXSSFWorkbook is not supported workbook");
    }

    public UnsupportedWorkbookException(String message) {
        super(message);
    }

    public UnsupportedWorkbookException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedWorkbookException(Throwable cause) {
        super("SXSSFWorkbook is not supported workbook", cause);
    }

}
