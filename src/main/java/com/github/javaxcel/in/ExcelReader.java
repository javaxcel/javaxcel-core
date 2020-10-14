package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.converter.impl.BasicReadingConverter;
import com.github.javaxcel.converter.impl.ExpressiveReadingConverter;
import com.github.javaxcel.exception.NoTargetedFieldException;
import com.github.javaxcel.util.ExcelUtils;
import com.github.javaxcel.util.FieldUtils;
import org.apache.poi.ss.usermodel.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * ExcelReader
 *
 * <pre>
 * 1. VO의 필드가 오직 `Wrapper Class` 또는 `String`이어야 하며, 기초형 필드가 있어서는 안된다.
 *    이외의 타입을 갖는 필드는 모두 null이 할당된다.
 *
 * 2. 상속받은 필드는 제외된다, 즉 해당 VO에서 정의된 필드만 계산한다.
 * </pre>
 */
public final class ExcelReader<W extends Workbook, T> {

    /**
     * Formatter that stringifies the value in a cell with {@link FormulaEvaluator}.
     */
    private static final DataFormatter dataFormatter = new DataFormatter();

    private final BasicReadingConverter<T> basicConverter = new BasicReadingConverter<>();

    /**
     * @see Workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook
     */
    private final W workbook;
    private final Class<T> type;
    /**
     * Evaluator that evaluates the formula in a cell.
     *
     * @see W
     */
    private final FormulaEvaluator formulaEvaluator;
    /**
     * The type's fields that will be actually written in excel.
     *
     * @see Class<T>
     */
    private final List<Field> fields;

    /**
     * Sheet's indexes that {@link ExcelReader} will read.
     * <br>
     * Default value is {@code {0}} (it means index of the first sheet).
     */
    private int[] sheetIndexes = {0};

    /**
     * Row's index that {@link ExcelReader} will start to read.
     */
    private int startIndex;

    /**
     * Row's index that {@link ExcelReader} will end to read.
     * <br>
     * Default value is {@code -1} (it means index of the last row).
     */
    private int endIndex = -1;

    private boolean parallel;

    private ExcelReader(W workbook, Class<T> type) {
        this.workbook = workbook;
        this.type = type;
        this.formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        this.fields = FieldUtils.getTargetedFields(type);

        if (this.fields.isEmpty()) throw new NoTargetedFieldException(this.type);
    }

    public static <W extends Workbook, E> ExcelReader<W, E> init(W workbook, Class<E> type) {
        return new ExcelReader<>(workbook, type);
    }

    public ExcelReader<W, T> sheetIndexes(int... sheetIndexes) {
        if (sheetIndexes == null || sheetIndexes.length == 0 || IntStream.of(sheetIndexes).anyMatch(i -> i < 0)) {
            throw new IllegalArgumentException("Sheet indexes cannot be null, empty or less than 0.");
        }

        this.sheetIndexes = sheetIndexes;
        return this;
    }

    public ExcelReader<W, T> startIndex(int startIndex) {
        if (startIndex < 0) throw new IllegalArgumentException("Start index cannot be less than 0.");

        this.startIndex = startIndex;
        return this;
    }

    public ExcelReader<W, T> endIndex(int endIndex) {
        if (endIndex < 0) throw new IllegalArgumentException("End index cannot be less than 0.");

        this.endIndex = endIndex;
        return this;
    }

    /**
     * Makes the conversion from simulated model into real model parallel.
     *
     * <p> We recommend processing in parallel only when
     * dealing with large data. The following table is a benchmark.
     *
     * <pre>{@code
     *     +------------+------------+----------+
     *     | row \ type | sequential | parallel |
     *     +------------+------------+----------+
     *     | 10,000     | 16s        | 13s      |
     *     +------------+------------+----------+
     *     | 25,000     | 31s        | 21s      |
     *     +------------+------------+----------+
     *     | 100,000    | 2m 7s      | 1m 31s   |
     *     +------------+------------+----------+
     *     | 150,000    | 3m 28s     | 2m 1s    |
     *     +------------+------------+----------+
     * }</pre>
     *
     * @return {@link ExcelReader}
     */
    public ExcelReader<W, T> parallel() {
        this.parallel = true;
        return this;
    }

    /**
     * Gets a list after this reads the excel file.
     *
     * @return list
     */
    public List<T> read() {
        if (this.sheetIndexes.length > 1) throw new IllegalArgumentException("Must input only one sheet index.");

        List<T> list = new ArrayList<>();
        Sheet sheet = this.workbook.getSheetAt(this.sheetIndexes[0]);
        sheetToList(sheet, list);

        return list;
    }

    public Map<String, List<T>> readAllSheets() {
        Map<String, List<T>> lists = new HashMap<>();
        sheetsToLists(ExcelUtils.getSheetRange(this.workbook), lists);

        return lists;
    }

    public Map<String, List<T>> readSelectedSheets() {
        Map<String, List<T>> lists = new HashMap<>();
        sheetsToLists(this.sheetIndexes, lists);

        return lists;
    }

    private void sheetsToLists(int[] sheetIndexes, Map<String, List<T>> lists) {
        for (int sheetIndex : sheetIndexes) {
            List<T> list = new ArrayList<>();
            Sheet sheet = this.workbook.getSheetAt(sheetIndex);

            sheetToList(sheet, list);

            // Add a sheet data of the workbook.
            lists.put(sheet.getSheetName(), list);
        }
    }

    /**
     * Reads a sheet and Adds rows of the data into list.
     *
     * @param sheet sheet to read
     * @param list  list to be added
     */
    private void sheetToList(Sheet sheet, List<T> list) {
        List<Map<String, Object>> sModels = getSimulatedModels(sheet);

        Stream<Map<String, Object>> stream = this.parallel ? sModels.parallelStream() : sModels.stream();
        List<T> realModels = stream.map(this::toRealModel).collect(toList());

        list.addAll(realModels);
    }

    /**
     * Converts a simulated model to the real model.
     *
     * @param sModel simulated model
     * @return real model
     */
    private T toRealModel(Map<String, Object> sModel) {
        T model = ExcelUtils.instantiate(this.type);

        for (Field field : this.fields) {
            String cellValue = (String) sModel.get(field.getName());

            ExcelReaderExpression annotation = field.getAnnotation(ExcelReaderExpression.class);
            Object fieldValue;
            if (annotation == null) {
                // When the field is not annotated with @ExcelReaderConversion.
                fieldValue = basicConverter.convert(cellValue, field);
            } else {
                // When the field is annotated with @ExcelReaderConversion.
                ExpressiveReadingConverter<T> expConverter = new ExpressiveReadingConverter<>(this.type);
                expConverter.setVariables(sModel);
                fieldValue = expConverter.convert(cellValue, field);
            }

            FieldUtils.setFieldValue(model, field, fieldValue);
        }

        return model;
    }

    /**
     * Gets simulated models from a sheet.
     *
     * <p>
     *
     * @param sheet excel sheet
     * @return simulated models
     */
    private List<Map<String, Object>> getSimulatedModels(Sheet sheet) {
        final int numOfModels = ExcelUtils.getNumOfModels(sheet);

        // 인덱스 유효성을 체크한다
        if (this.endIndex == -1 || this.endIndex > numOfModels) this.endIndex = numOfModels;

        // Reads rows.
        List<Map<String, Object>> simulatedModels = new ArrayList<>();
        for (int i = this.startIndex; i < this.endIndex; i++) {
            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            // Adds a row data of the sheet.
            simulatedModels.add(rowToSimulatedModel(row));
        }

        return simulatedModels;
    }

    /**
     * Converts a row to a simulated model.
     *
     * <p> Reads rows to get data. this creates {@link Map} as a simulated model
     * and puts the key({@link Field#getName()}) and the value
     * ({@link DataFormatter#formatCellValue(Cell, FormulaEvaluator)})
     * to the model. The result is the same as the following code.
     *
     * <pre>{@code
     *     +------+--------+--------+----------+
     *     | name | height | weight | eyesight |
     *     +------+--------+--------+----------+
     *     | John | 180.5  | 79.2   |          |
     *     +------+--------+--------+----------+
     *
     *     This row will be converted to
     *
     *     { "name": "John", "height": "180.5", "weight": "79.2", "eyesight": null }
     * }</pre>
     *
     * @param row row in sheet
     * @return simulated model
     */
    private Map<String, Object> rowToSimulatedModel(Row row) {
        Map<String, Object> simulatedModel = new HashMap<>();

        int fieldsSize = this.fields.size();
        for (int i = 0; i < fieldsSize; i++) {
            Field field = this.fields.get(i);

            // If the cell is null, creates an empty cell.
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            // Evaluates the formula and returns a stringifed value.
            String cellValue = dataFormatter.formatCellValue(cell, this.formulaEvaluator);

            // Converts empty string to null because when CellType is BLANK, DataFormatter returns empty string.
            simulatedModel.put(field.getName(), cellValue.equals("") ? null : cellValue);
        }

        return simulatedModel;
    }

}
