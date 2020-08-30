package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.*;
import com.github.javaxcel.util.ExcelUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelWriterTest {

    /**
     * Products for test.
     */
    private final List<Product> products = Arrays.asList(
            new Product(100000, "알티지 클린 Omega 3", "9b9e7d29-2a60-4973-aec0-685e672eb07a", 3.0, 3.765, 20.5, 580.5),
            new Product(100001, "레이델 면역쾌청", "a7f3be7b-b235-45b8-9fc5-28f2578ee8e0", 14.0, 140, 15, 570.50),
            new Product(100002, "그린스토어 우먼케어 건강한 질엔", "d3a6b7c4-c328-470b-b2c9-5e1b937acd0a", 10.75, 14.1, 15, 170.55),
            new Product(100003, "Bubbleless Vitamin-C", "8a2d7b5d-1a57-4055-a75b-98e495e58a4e", 18.0, 6, 20, 340.07)
    );

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
    @ValueSource(classes = {Product.class, ToyBox.class, Toy.class, EducationToy.class})
    public void getFields(Class<?> type) throws IllegalAccessException {
        // given
        ExcelModel annotation = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(type.getDeclaredFields())
                : ExcelUtils.getInheritedFields(type).stream();

        // when
        List<Field> fields = stream
                .filter(field -> field.getAnnotation(ExcelIgnore.class) == null) // Excludes the fields annotated @ExcelIgnore.
//                .filter(field -> TypeClassifier.isWritableClass(field.getType())) // Excludes the fields that are un-writable of excel.
                .collect(Collectors.toList());

        // then
        for (Field field : fields) {
            field.setAccessible(true);

            if (Toy.class.equals(type) || EducationToy.class.equals(type)) {
                System.out.println(field.getName() + ":\t" + field.get(this.box.get(0)));
            } else if (Product.class.equals(type)) {
                System.out.println(field.getName() + ":\t" + field.get(this.products.get(0)));
            } else {
                System.out.println(field.getName() + ":\t" + field);
            }
        }
    }

    /**
     * 1. {@link ExcelIgnore}
     * <br>
     * 2. {@link ExcelColumn#value()}
     * <br>
     * 3. {@link ExcelColumn#defaultValue()}
     */
    @Test
    public void writeWithIgnoreAndDefaultValue() throws IOException, IllegalAccessException {
        // when
        File file = new File("/data", "products.xlsx");
        ExcelWriter.init(Product.class, this.products)
                .sheetName("")
                .write(file);

        // then
        assertTrue(file.exists());
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    public void writeWithTargetedFieldPolicyAndDateTimePattern() throws IOException, IllegalAccessException {
        // when
        File file = new File("/data", "toys.xlsx");
        ExcelWriter.init(EducationToy.class, this.box.getAll())
                .write(file);

        // then
        assertTrue(file.exists());
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
        String strBigInt = "123456789123456789123456789123456789";
        String strBigDec = "123456789123456789123456789123456789123456.07890";

        // when
        String bigInteger = String.valueOf(new BigInteger(strBigInt));
        String bigDecimal = String.valueOf(new BigDecimal(strBigDec));

        // then
        assertEquals(bigInteger, strBigInt);
        assertEquals(bigDecimal, strBigDec);
    }

}

