package com.github.javaxcel.util;

import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.model.toy.Toy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldUtilsTest {

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
        Arrays.stream(headerNames).forEach(System.out::println);
    }

}
