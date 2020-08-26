package com.github.javaxcel.out;

import com.github.javaxcel.model.Product;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ExcelWriterTest {

    @Test
    void write() throws IOException, IllegalAccessException {
        // given
        List<Product> products = Arrays.asList(
                Product.builder().serialNumber(100000).name("알티지 클린 Omega 3").apiId("9b9e7d29-2a60-4973-aec0-685e672eb07a").width(3.0).depth(3.765).height(20.5).build(),
                Product.builder().serialNumber(100001).name("레이델 면역쾌청").apiId("a7f3be7b-b235-45b8-9fc5-28f2578ee8e0").width(14.0).depth(140).height(15).weight(570.50).build(),
                Product.builder().serialNumber(100002).name("그린스토어 우먼케어 건강한 질엔").apiId("d3a6b7c4-c328-470b-b2c9-5e1b937acd0a").depth(10.75).height(14.2).weight(170.55).build(),
                Product.builder().serialNumber(100003).name("Bubbleless Vitamin-C").apiId("8a2d7b5d-1a57-4055-a75b-98e495e58a4e").width(18.0).height(6).weight(340.07).build()
        );

        // when
        File file = new File("/data", "products.xlsx");
        ExcelWriter.init(Product.class, products)
                .sheetName("")
                .write(file);

        // then
        assertTrue(file.exists());
    }

}

