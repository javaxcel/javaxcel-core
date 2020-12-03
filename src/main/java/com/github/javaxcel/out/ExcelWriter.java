package com.github.javaxcel.out;

import java.io.OutputStream;
import java.util.List;

public interface ExcelWriter<T> {

    /**
     * Writes the data in the excel file.
     *
     * @param out  output stream for writing excel file
     * @param list list of models
     */
    void write(OutputStream out, List<T> list);

}
