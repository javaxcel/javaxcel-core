package com.github.javaxcel.in;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelReader<W extends Workbook, T> {

    /**
     * Limits the number of models.
     *
     * @param limit limit for the number of models
     * @return {@link ExcelReader}
     */
    ExcelReader<W, T> limit(int limit);

    /**
     * Returns a list after this reads the excel file.
     *
     * @return list
     */
    List<T> read();

}
