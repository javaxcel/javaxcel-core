package com.github.javaxcel.factory;

import com.github.javaxcel.exception.NoTargetedConstructorException;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.out.ExcelWriter;
import com.github.javaxcel.out.MapWriter;
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

    public static <W extends Workbook, V> ExcelWriter<W, Map<String, V>> create(W workbook) {
        return instantiate(workbook);
    }

    public static <W extends Workbook, T> ModelWriter<W, T> create(W workbook, Class<T> type) {
        return instantiate(workbook, type);
    }

    /**
     * Instantiates {@link MapWriter}.
     *
     * @param workbook excel workbook
     * @param <W>      instance that implements {@link Workbook}
     * @param <V>      {@link Map}'s value
     * @return {@link MapWriter}
     */
    @SuppressWarnings("unchecked")
    private static <W extends Workbook, V> MapWriter<W, Map<String, V>> instantiate(W workbook) {
        Constructor<?> constructor;
        try {
            Class<?> clazz = Class.forName("com.github.javaxcel.out.MapWriter", true, ExcelWriterFactory.class.getClassLoader());
            constructor = clazz.getDeclaredConstructor(Workbook.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new NoTargetedConstructorException(e, Map.class);
        }
        constructor.setAccessible(true);

        MapWriter<W, Map<String, V>> writer;
        try {
            writer = (MapWriter<W, Map<String, V>>) constructor.newInstance(workbook);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", Map.class.getName()));
        }

        return writer;
    }

    /**
     * Instantiates {@link ModelWriter}.
     *
     * @param workbook excel workbook
     * @param type     type of model
     * @param <W>      instance that implements {@link Workbook}
     * @param <T>      type of the element
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
            if (e.getCause() instanceof IllegalArgumentException) throw new IllegalArgumentException(e);
            if (e.getCause() instanceof NoTargetedFieldException) throw new NoTargetedFieldException(e);
            throw new RuntimeException(String.format("Failed to instantiate of the class(%s)", type.getName()));
        }

        return writer;
    }

}
