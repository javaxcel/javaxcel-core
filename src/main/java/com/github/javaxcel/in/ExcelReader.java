package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.util.ExcelUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private final W workbook;

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    /**
     * Sheet's indexes that {@code ExcelReader} will read.
     * <br>
     * Default value is {@code {0}} (it mean index of the first sheet).
     */
    private int[] sheetIndexes = {0};

    /**
     * Row's index that {@code ExcelReader} will start to read.
     */
    private int startIndex;

    /**
     * Row's index that {@code ExcelReader} will end to read.
     * <br>
     * Default value is {@code -1} (it mean index of the last row).
     */
    private int endIndex = -1;

    public static <W extends Workbook, E> ExcelReader<W, E> init(W workbook, Class<E> type) {
        return new ExcelReader<>(workbook, type);
    }

    private ExcelReader(W workbook, Class<T> type) {
        this.workbook = workbook;
        this.type = type;

        // @ExcelModel의 타깃 필드 정책에 따라 가져오는 필드가 다르다
        ExcelModel annotation = this.type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(this.type.getDeclaredFields())
                : ExcelUtils.getInheritedFields(this.type).stream();

        // Excludes the fields annotated @ExcelIgnore.
        this.fields = stream.filter(field -> field.getAnnotation(ExcelIgnore.class) == null)
                .collect(Collectors.toList());
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
     * 헤더는 제외되며, 지정된 로우부터 또 달리 지정된 로우까지 읽어 VO를 반환한다.
     */
    public List<T> read() throws ReflectiveOperationException {
        if (this.sheetIndexes.length > 1) throw new IllegalArgumentException("Must input only one sheet index.");

        List<T> list = new ArrayList<>();
        Sheet sheet = this.workbook.getSheetAt(this.sheetIndexes[0]);
        setSheetDataIntoList(sheet, list);

        return list;
    }

    public Map<String, List<T>> readAllSheets() throws ReflectiveOperationException {
        Map<String, List<T>> lists = new HashMap<>();
        setSheetList(ExcelUtils.getSheetRange(this.workbook), lists);

        return lists;
    }

    public Map<String, List<T>> readSelectedSheets() throws ReflectiveOperationException {
        Map<String, List<T>> lists = new HashMap<>();
        setSheetList(this.sheetIndexes, lists);

        return lists;
    }

    private void setSheetList(int[] sheetIndexes, Map<String, List<T>> lists) throws ReflectiveOperationException {
        for (int sheetIndex : sheetIndexes) {
            List<T> list = new ArrayList<>();
            Sheet sheet = this.workbook.getSheetAt(sheetIndex);

            setSheetDataIntoList(sheet, list);

            // Add a sheet data of the workbook.
            lists.put(sheet.getSheetName(), list);
        }
    }

    /**
     * Reads a sheet and Adds rows of the data into list.
     *
     * @param sheet sheet to read
     * @param list list to be added
     * @throws ReflectiveOperationException
     */
    private void setSheetDataIntoList(Sheet sheet, List<T> list) throws ReflectiveOperationException {
        // 헤더를 제외한 전체 로우 개수
        final int numOfRows = sheet.getPhysicalNumberOfRows() - 1;

        // 인덱스 유효성을 체크한다
        if (this.endIndex == -1 || this.endIndex > numOfRows) this.endIndex = numOfRows;

        // 엑셀 파일을 읽는다
        for (int i = this.startIndex; i < this.endIndex; i++) {
            // Skips the first row that is header.
            Row row = sheet.getRow(i + 1);

            Constructor<T> constructor = this.type.getDeclaredConstructor();
            constructor.setAccessible(true);

            T element = constructor.newInstance();
            setValueIntoField(element, row);

            // Adds a row data of the sheet.
            list.add(element);
        }
    }

    private void setValueIntoField(T element, Row row) throws ReflectiveOperationException {
        int fieldsSize = this.fields.size();
        for (int i = 0; i < fieldsSize; i++) {
            Field field = this.fields.get(i);

            // NPE를 방지하고 모든 데이터를 문자열로 취급한다
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//            cell.setCellType(CellType.STRING); // Deprecated for removal since v5.0
            String cellValue = cell.getStringCellValue();

            // private 접근자라도 접근하게 한다
            field.setAccessible(true);

            // Sets value into the field.
            field.set(element, ExcelUtils.convertValue(cellValue, field));
        }
    }

}
