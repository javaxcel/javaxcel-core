package com.github.javaxcel.util;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.model.toy.Toy;
import io.github.imsejin.util.StringUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
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

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    public void getTargetedFields(Class<?> type) {
        // when
        List<Field> targetedFields = FieldUtils.getTargetedFields(type);

        // then
        targetedFields.forEach(System.out::println);
    }

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    public void getInheritedFields(Class<?> type) {
        // when
        List<Field> inheritedFields = FieldUtils.getInheritedFields(type);

        // then
        inheritedFields.forEach(System.out::println);
    }

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    public void toHeaderNames(Class<?> type) {
        // given
        List<Field> targetedFields = FieldUtils.getTargetedFields(type);

        // when
        String[] headerNames = FieldUtils.toHeaderNames(targetedFields);

        // then
        Arrays.asList(headerNames).forEach(System.out::println);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "E:\\works\\대한상공회의소 - 유통상품지식뱅크 서비스포털\\2020\\06\\20200618_미기재 설명 등록\\상품분류별 부가속성\\200519_분류별_부가속성예시.xlsx",
            "E:\\works\\대한상공회의소 - 유통상품지식뱅크 서비스포털\\2020\\06\\20200618_미기재 설명 등록\\상품분류별 부가속성\\[가공] 상품분류별_부가속성_설명.xlsx",
    })
    @SneakyThrows({IOException.class, InvalidFormatException.class})
    public void getSheetRange(String pathname) {
        // given
        File file = new File(pathname);
        @Cleanup
        Workbook workbook = new XSSFWorkbook(file);

        // when
        int[] range = ExcelUtils.getSheetRange(workbook);

        // then
        IntStream.of(range).forEach(i -> System.out.println("[" + i + "] " + workbook.getSheetAt(i).getSheetName()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"targetAges", "goals", "date", "time", "dateTime"})
    @SneakyThrows(NoSuchFieldException.class)
    public void stringifyValue(String fieldName) {
        for (EducationToy toy : new EducationToy().createRandoms(1000)) {
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
        List<Field> targetedFields = FieldUtils.getTargetedFields(EducationToy.class);

        assertEquals(values.size(), targetedFields.size());

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Field field = targetedFields.get(i);

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

    @Test
    public void formatDateTime() {
        // given
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        LocalDateTime dateTime = LocalDateTime.now();

        // when
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yy/M/dd"));
        String formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // then
        System.out.println(formattedDate);
        System.out.println(formattedTime);
        System.out.println(formattedDateTime);
    }

    @Test
    public void stringifyBigNumbers() {
        // given
        String strBigInt = Long.valueOf(Long.MAX_VALUE).toString();
        String strBigDec = "123456789123456789123456789123456789123456.07890";

        // when
        String bigInteger = String.valueOf(BigInteger.valueOf(Long.parseLong(strBigInt)));
        String bigDecimal = String.valueOf(new BigDecimal(strBigDec));

        // then
        assertEquals(bigInteger, strBigInt);
        assertEquals(bigDecimal, strBigDec);
    }

}
