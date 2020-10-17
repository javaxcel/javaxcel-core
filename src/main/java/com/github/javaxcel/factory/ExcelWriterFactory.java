package com.github.javaxcel.factory;

import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.out.ExcelWriter;
import com.github.javaxcel.out.ModelWriter;
import org.apache.poi.ss.usermodel.Workbook;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Factory for creating the appropriate type of {@link ExcelWriter}.
 * This will create.
 */
public abstract class ExcelWriterFactory {

    public static <W extends Workbook, K, V> ExcelWriter<W, Map<K, V>> create(W workbook) {
        return null;
    }

    public static <W extends Workbook, T> ModelWriter<W, T> create(W workbook, Class<T> type) {
        return instantiate(workbook, type);
    }

    private static <W extends Workbook, K, V> ExcelWriter<W, Map<K, V>> instantiate(W workbook) {
        return null; // TODO: create MapWriter
    }

    /**
     * Instantiates {@link ModelWriter}.
     *
     * @param workbook workbook
     * @param type     type of model
     * @return {@link ModelWriter}
     */
    @SuppressWarnings("unchecked")
    private static <W extends Workbook, T> ModelWriter<W, T> instantiate(W workbook, Class<T> type) {
        Constructor<?> constructor;
        try {
            Class<?> clazz = Class.forName("com.github.javaxcel.out.ModelWriter", true, ExcelWriterFactory.class.getClassLoader());
            constructor = clazz.getDeclaredConstructor(Workbook.class, Class.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, type);
        }
        constructor.setAccessible(true);

        ModelWriter<W, T> writer;
        try {
            writer = (ModelWriter<W, T>) constructor.newInstance(workbook, type);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", type.getName()));
        }

        return writer;
    }

}
