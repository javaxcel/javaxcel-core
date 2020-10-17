package com.github.javaxcel.out;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.util.List;

public interface ExcelWriter<W extends Workbook, T> {

    ExcelWriter<W, T> defaultValue(String defaultValue);

    ExcelWriter<W, T> sheetName(String sheetName);

    ExcelWriter<W, T> headerNames(List<String> headerNames);

    /**
     * Writes the data in the excel file.
     *
     * @param out  output stream for writing excel file
     * @param list list of models
     */
    void write(OutputStream out, List<T> list);

}
