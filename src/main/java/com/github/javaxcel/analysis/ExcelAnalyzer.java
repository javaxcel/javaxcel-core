package com.github.javaxcel.analysis;

import java.lang.reflect.Field;
import java.util.List;

public interface ExcelAnalyzer {

    List<ExcelAnalysis> analyze(List<Field> fields, Object... arguments);

}
