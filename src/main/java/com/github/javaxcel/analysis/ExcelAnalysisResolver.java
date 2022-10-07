package com.github.javaxcel.analysis;

import com.github.javaxcel.analysis.out.ExcelWriteAnalysis;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.out.strategy.impl.DefaultValue;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;

public interface ExcelAnalysisResolver {

    int getBit();

    ExcelWriteAnalysis resolve(Object... args);

    static String resolveDefaultValue(Field field, @Null DefaultValue strategy) {
        if (strategy != null) {
            return (String) strategy.execute(null);
        }

        String defaultValue = null;

        // Decides the proper default value for a field value.
        // @ExcelColumn's default value takes precedence over ExcelModel's default value.
        ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
        if (columnAnnotation != null && !columnAnnotation.defaultValue().equals("")) {
            // Default value on @ExcelColumn
            defaultValue = columnAnnotation.defaultValue();
        } else {
            ExcelModel modelAnnotation = field.getDeclaringClass().getAnnotation(ExcelModel.class);
            if (modelAnnotation != null && !modelAnnotation.defaultValue().equals("")) {
                // Default value on @ExcelModel
                defaultValue = modelAnnotation.defaultValue();
            }
        }

        return defaultValue;
    }

}
