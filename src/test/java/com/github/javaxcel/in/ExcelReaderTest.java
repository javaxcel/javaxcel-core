package com.github.javaxcel.in;

import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.Box;
import com.github.javaxcel.model.EducationToy;
import com.github.javaxcel.model.Product;
import com.github.javaxcel.model.ToyBox;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelReaderTest {

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

    @Test
    public void getDeclaredConstructorWithMinimumParameters() throws ReflectiveOperationException {
        // given
        Class<Product> clazz = Product.class;

        // when
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

        // then
        for (Constructor<?> constructor : declaredConstructors) {
            System.out.println(constructor);
        }
        Constructor<?> constructor = Arrays.stream(declaredConstructors)
                .min((a, b) -> Math.min(a.getParameterCount(), b.getParameterCount()))
                .orElseThrow(NoSuchElementException::new);
        System.out.println("constructor with minimum parameters: " + constructor);
        constructor.setAccessible(true);
        Arrays.stream(constructor.getParameterTypes()).forEach(System.out::println);
        Object o = constructor.newInstance();
    }

    @Test
    public void read() throws InvalidFormatException, ReflectiveOperationException, IOException {
        // when
        File file = new File("/data", "products.xlsx");
        List<Product> products = ExcelReader.init(Product.class)
                .read(file);

        // then
        products.forEach(System.out::println);
        assertTrue(this.products.stream().allMatch(product -> Collections.frequency(products, product) > 0));
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link com.github.javaxcel.constant.TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    public void readWithTargetedFieldPolicyAndDateTimePattern() throws InvalidFormatException, ReflectiveOperationException, IOException {
        // when
        File file = new File("/data", "toys.xlsx");
        List<EducationToy> educationToys = ExcelReader.init(EducationToy.class)
                .startIndex(1)
                .endIndex(10)
                .read(file);

        // then
        educationToys.forEach(System.out::println);
        assertTrue(this.box.getAll().stream().allMatch(product -> Collections.frequency(this.box.getAll(), product) > 0));
    }

}
