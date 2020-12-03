package com.github.javaxcel.in;

import java.util.List;

public interface ExcelReader<T> {

    /**
     * Returns a list after this reads the excel file.
     *
     * @return list
     */
    List<T> read();

}
