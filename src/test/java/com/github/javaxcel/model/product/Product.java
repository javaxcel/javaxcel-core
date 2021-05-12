package com.github.javaxcel.model.product;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.model.Mockables;
import lombok.*;

import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"width", "weight"})
public class Product {

    @ExcelColumn(name = "상품번호")
    private long serialNumber;

    private String name;

    @ExcelColumn(name = "API_ID")
    private String apiId;

    @ExcelIgnore
    @ExcelColumn(name = "가로")
    private Double width;

    @ExcelColumn(defaultValue = "(empty)") // Default value is ineffective to primitive type.
    private double depth;

    private double height;

    @ExcelColumn(name = "WEIGHT", defaultValue = "-1") // Default value is effective except primitive type.
    private Double weight;

    public static List<Product> createDesignees() {
        return Arrays.asList(
                new Product(100000, "알티지 클린 Omega 3", "9b9e7d29-2a60-4973-aec0-685e672eb07a", 3.0, 3.765, 20.5, 580.5),
                new Product(100001, "레이델 면역쾌청", "a7f3be7b-b235-45b8-9fc5-28f2578ee8e0", 14.0, 140, 15, 570.50),
                new Product(100002, "그린스토어 우먼케어 건강한 질엔", "d3a6b7c4-c328-470b-b2c9-5e1b937acd0a", 10.75, 14.1, 15, 170.55),
                new Product(100003, "Bubbleless Vitamin-C", "8a2d7b5d-1a57-4055-a75b-98e495e58a4e", 18.0, 6, 20, 340.07)
        );
    }

    public static Product createRandom() {
        long serialNumber = random.nextInt(1000000) + 1000000;
        String name = random.nextDouble() <= 0.75 ? Mockables.generateRandomText(random.nextInt(16) + 1) : null;
        String apiId = UUID.randomUUID().toString();
        Double width = random.nextDouble() >= 0.5 ? random.nextDouble() * 100 : null;
        double depth = random.nextDouble() * 100;
        double height = random.nextDouble() * 100;
        Double weight = random.nextDouble() >= 0.5 ? random.nextDouble() * 1000 : null;

        return new Product(serialNumber, name, apiId, width, depth, height, weight);
    }

    public static List<Product> createRandoms(int size) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Product product = createRandom();
            products.add(product);
        }

        return products;
    }

    private static Random random = new Random();

}
