package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.util.ExcelUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
public final class ExcelReader<T> {

    private final Class<T> type;

    /**
     * The type's fields that will be actually written in excel.
     */
    private final List<Field> fields;

    private int startIndex;

    private int endIndex = -1;

    public static <E> ExcelReader<E> init(@NotNull Class<E> type) {
        return new ExcelReader<E>(type);
    }

    private ExcelReader(Class<T> type) {
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

    public ExcelReader<T> startIndex(int startIndex) {
        if (startIndex < 0) throw new IllegalArgumentException("Start index cannot be less than 0.");

        this.startIndex = startIndex;
        return this;
    }

    public ExcelReader<T> endIndex(int endIndex) {
        if (endIndex < 0) throw new IllegalArgumentException("End index cannot be less than 0.");

        this.endIndex = endIndex;
        return this;
    }

    /**
     * 헤더는 제외되며, 지정된 로우부터 또 달리 지정된 로우까지 읽어 VO를 반환한다.
     */
    public List<T> read(File file) throws ReflectiveOperationException, IOException, InvalidFormatException {
        List<T> result = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 헤더를 제외한 전체 로우 개수
            final int NUMBER_OF_ROWS = sheet.getPhysicalNumberOfRows() - 1;

            // 인덱스 유효성을 체크한다
            if (this.endIndex == -1 || this.endIndex > NUMBER_OF_ROWS) this.endIndex = NUMBER_OF_ROWS;

            // 엑셀 파일을 읽는다
            for (int i = this.startIndex; i < this.endIndex; i++) {
                // Skips the first row that is header.
                Row row = sheet.getRow(i + 1);

                Constructor<T> constructor = this.type.getDeclaredConstructor();
                constructor.setAccessible(true);

                T element = constructor.newInstance();
                setValueIntoField(element, row);

                result.add(element);
            }
        }

        return result;
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
