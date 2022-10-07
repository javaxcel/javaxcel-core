package com.github.javaxcel.analysis;

import java.lang.reflect.Field;
import java.util.List;

public interface ExcelAnalyzer<T extends ExcelAnalysis> {

    List<T> analyze(List<Field> fields, Object... arguments);

}
