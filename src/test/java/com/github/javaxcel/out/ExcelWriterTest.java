package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.*;
import com.github.javaxcel.util.TypeClassifier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelWriterTest {

    {
        List<EducationToy> toys = Arrays.asList(
                EducationToy.builder().toyType(ToyType.CHILD).weight(1800.0).build(),
                EducationToy.builder().targetAges(new int[]{4, 5, 6, 7, 8, 9}).goals("Goals").build(),
                EducationToy.builder().name("Braun Series 7").toyType(ToyType.ADULT).weight(270.00).build(),
                EducationToy.builder().name("베이비버스 가방퍼즐 키키·묘묘와 친구들").toyType(ToyType.CHILD).weight(2450.50).targetAges(new int[]{9, 10, 11, 12, 13}).goals("education for children").build(),
                EducationToy.builder().name("마누스 기획 성인장갑 남").toyType(ToyType.ADULT).weight(126.6).goals("education for adult").build()
        );

        this.box = new ToyBox<>(toys);
    }

    private final Box<EducationToy> box;

    /**
     * Products for test.
     */
    private final List<Product> products = Arrays.asList(
            Product.builder().serialNumber(100000).name("알티지 클린 Omega 3").apiId("9b9e7d29-2a60-4973-aec0-685e672eb07a").width(3.0).depth(3.765).height(20.5).build(),
            Product.builder().serialNumber(100001).name("레이델 면역쾌청").apiId("a7f3be7b-b235-45b8-9fc5-28f2578ee8e0").width(14.0).depth(140).height(15).weight(570.50).build(),
            Product.builder().serialNumber(100002).name("그린스토어 우먼케어 건강한 질엔").apiId("d3a6b7c4-c328-470b-b2c9-5e1b937acd0a").depth(10.75).height(14.2).weight(170.55).build(),
            Product.builder().serialNumber(100003).name("Bubbleless Vitamin-C").apiId("8a2d7b5d-1a57-4055-a75b-98e495e58a4e").width(18.0).height(6).weight(340.07).build()
    );

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Box.class, ToyBox.class, Toy.class, EducationToy.class})
    public void getFields(Class<?> type) throws IllegalAccessException {
        // given
        ExcelModel annotation = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(type.getDeclaredFields())
                : getInheritedFields(type).stream();

        // when
        List<Field> fields = stream
                .filter(field -> field.getAnnotation(ExcelIgnore.class) == null) // Excludes the fields annotated @ExcelIgnore.
//                .filter(field -> TypeClassifier.isWritableClass(field.getType())) // Excludes the fields that are un-writable of excel.
                .collect(Collectors.toList());

        // then
        for (Field field : fields) {
            field.setAccessible(true);

            if (type.equals(Toy.class) || type.equals(EducationToy.class)) {
                System.out.println(field.getName() + ":\t" + field.get(this.box.get(0)));
            } else {
                System.out.println(field.getName() + ":\t" + field);
            }
        }
    }

    private List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            fields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
        }

        return fields;
    }

    @Test
    public void write() throws IOException, IllegalAccessException {
        // when
        File file = new File("/data", "products.xlsx");
        ExcelWriter.init(Product.class, this.products)
                .sheetName("")
                .write(file);

        // then
        assertTrue(file.exists());
    }

    @Test
    public void writeWithExcelModel() throws IOException, IllegalAccessException {
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

}

