package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelUtilsTest {

    /**
     * Box for test.
     */
    private final Box<EducationToy> box = new ToyBox<>(Arrays.asList(
            new EducationToy("", ToyType.CHILD, 1800.0, null, "goals"),
            new EducationToy("레이델 면역쾌청", ToyType.ADULT, 585.54, new int[]{4, 5, 6, 7, 8, 9}, "Goals"),
            new EducationToy("Braun Series 7", ToyType.ADULT, 270.00, null, null),
            new EducationToy("베이비버스 가방퍼즐 키키·묘묘와 친구들", ToyType.CHILD, 2450.50, new int[]{9, 10, 11, 12, 13}, "education for children"),
            new EducationToy("마누스 기획 성인장갑 남", ToyType.ADULT, 126.6, null, "education for adult")
    ));

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    public void getInheritedFields(Class<?> type) {
        // when
        List<Field> inheritedFields = ExcelUtils.getInheritedFields(type);

        // then
        inheritedFields.forEach(System.out::println);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "E:\\works\\대한상공회의소 - 유통상품지식뱅크 서비스포털\\2020\\06\\20200618_미기재 설명 등록\\상품분류별 부가속성\\200519_분류별_부가속성예시.xlsx",
            "E:\\works\\대한상공회의소 - 유통상품지식뱅크 서비스포털\\2020\\06\\20200618_미기재 설명 등록\\상품분류별 부가속성\\[가공] 상품분류별_부가속성_설명.xlsx",
    })
    public void getSheetRange(String pathname) throws IOException, InvalidFormatException {
        // given
        File file = new File(pathname);
        Workbook workbook = new XSSFWorkbook(file);

        // when
        int[] range = IntStream.range(0, workbook.getNumberOfSheets()).toArray();
        workbook.close();

        // then
        IntStream.of(range).forEach(i -> System.out.println("[" + i + "] " + workbook.getSheetAt(i).getSheetName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"targetAges", "goals", "date", "time", "dateTime"})
    public void stringifyValue(String fieldName) throws IllegalAccessException, NoSuchFieldException {
        for (EducationToy toy : this.box.getAll()) {
            // when
            String stringifyValue = ExcelUtils.stringifyValue(toy, toy.getClass().getDeclaredField(fieldName));

            // then
            System.out.println(stringifyValue);
        }
    }

    @Test
    public void convert() {
        // given
        List<String> values = Arrays.asList("Toy.name", "ADULT", "645.70", "[1,2,3,4]", "educationToys.goals", "2020-08-31", "01/23/45/678", "2020-08-31T01:23:45");
        List<Field> inheritedFields = ExcelUtils.getInheritedFields(EducationToy.class);

        assertEquals(values.size(), inheritedFields.size());
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Field field = inheritedFields.get(i);

            // when
            Object converted = convert(value, field);

            // then
            System.out.println(converted);
        }
    }

    private static Object convert(String value, Field field) {
        Class<?> type = field.getType();

        if (String.class.equals(type)) return value;
        else if (byte.class.equals(type) || Byte.class.equals(type)) return Byte.parseByte(value);
        else if (short.class.equals(type) || Short.class.equals(type)) return Short.parseShort(value);
        else if (int.class.equals(type) || Integer.class.equals(type)) return Integer.parseInt(value);
        else if (long.class.equals(type) || Long.class.equals(type)) return Long.parseLong(value);
        else if (float.class.equals(type) || Float.class.equals(type)) return Float.parseFloat(value);
        else if (double.class.equals(type) || Double.class.equals(type)) return Double.parseDouble(value);
        else if (char.class.equals(type) || Character.class.equals(type)) return value.charAt(0);
        else if (boolean.class.equals(type) || Boolean.class.equals(type)) return Boolean.parseBoolean(value);
        else if (BigInteger.class.equals(type)) return new BigInteger(value);
        else if (BigDecimal.class.equals(type)) return new BigDecimal(value);
        else if (TypeClassifier.isTemporal(type)) {
            ExcelDateTimeFormat excelDateTimeFormat = field.getAnnotation(ExcelDateTimeFormat.class);
            String pattern = excelDateTimeFormat == null ? null : excelDateTimeFormat.pattern();

            if (StringUtils.isNullOrEmpty(pattern)) {
                // When pattern is undefined or implicitly defined.
                if (LocalTime.class.equals(type)) return LocalTime.parse(value);
                else if (LocalDate.class.equals(type)) return LocalDate.parse(value);
                else if (LocalDateTime.class.equals(type)) return LocalDateTime.parse(value);

            } else {
                // When pattern is explicitly defined.
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (LocalTime.class.equals(type)) return LocalTime.parse(value, formatter);
                else if (LocalDate.class.equals(type)) return LocalDate.parse(value, formatter);
                else if (LocalDateTime.class.equals(type)) return LocalDateTime.parse(value, formatter);
            }
        }

        return null;
    }

}