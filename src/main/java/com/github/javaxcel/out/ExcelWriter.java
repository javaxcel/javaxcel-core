package com.github.javaxcel.out;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.util.List;

public interface ExcelWriter<W extends Workbook, T> {

    /**
     * Sets default value when value to be written is null or empty.
     *
     * @param defaultValue replacement of the value when it is null or empty string.
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> defaultValue(String defaultValue);

    /**
     * Sets sheet name.
     *
     * @param sheetName sheet name
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> sheetName(String sheetName);

    /**
     * Sets header names.
     *
     * @param headerNames header name
     * @return {@link ExcelWriter}
     */
    ExcelWriter<W, T> headerNames(List<String> headerNames);

    /**
     * Writes the data in the excel file.
     *
     * @param out  output stream for writing excel file
     * @param list list of models
     */
    void write(OutputStream out, List<T> list);

}
