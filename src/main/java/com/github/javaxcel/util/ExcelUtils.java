package com.github.javaxcel.util;

import com.github.javaxcel.exception.NoTargetedConstructorException;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;

public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * Gets range of the sheets.
     *
     * @param workbook excel workbook
     * @return range that from 0 to (the number of sheets - 1)
     * @see Workbook#getNumberOfSheets()
     */
    public static int[] getSheetRange(Workbook workbook) {
        return IntStream.range(0, workbook.getNumberOfSheets()).toArray();
    }

    public static <T> T instantiate(Class<T> type) {
        // Allows only constructor without parameter. TODO: write it in javadoc.
        Constructor<T> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, type);
        }
        constructor.setAccessible(true);

        // Instantiates new model and sets up data into the model's fields.
        T model;
        try {
            model = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", type.getName()));
        }

        return model;
    }

}
