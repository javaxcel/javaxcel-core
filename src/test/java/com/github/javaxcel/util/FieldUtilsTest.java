package com.github.javaxcel.util;

import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import com.github.javaxcel.model.toy.Toy;
import io.github.imsejin.common.util.ReflectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.List;

class FieldUtilsTest {

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    void getTargetedFields(Class<?> type) {
        // when
        List<Field> targetedFields = FieldUtils.getTargetedFields(type);

        // then
        targetedFields.forEach(System.out::println);
    }

    @ParameterizedTest
    @ValueSource(classes = {Product.class, Toy.class, EducationToy.class})
    void toHeaderNames(Class<?> type) {
        // given
        List<Field> targetedFields = FieldUtils.getTargetedFields(type);

        // when
        List<String> headerNames = FieldUtils.toHeaderNames(targetedFields);

        // then
        headerNames.forEach(System.out::println);
    }

}
