package com.github.javaxcel.model.product;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.model.Mockables;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"apiId", "depth", "weight"})
public class Product implements Mockables<Product> {

    @ExcelColumn(name = "상품번호")
    private long serialNumber;

    private String name;

    @ExcelIgnore
    @ExcelColumn(name = "API_ID")
    private String apiId;

    @ExcelColumn(name = "가로")
    private Double width;

    @ExcelColumn(defaultValue = "(empty)") // Default value is ineffective to primitive type.
    private double depth;

    private double height;

    @ExcelColumn(name = "WEIGHT", defaultValue = "-1") // Default value is effective except primitive type.
    private Double weight;

    @Override
    public List<Product> createDesignees() {
        return Arrays.asList(
                new Product(100000, "알티지 클린 Omega 3", "9b9e7d29-2a60-4973-aec0-685e672eb07a", 3.0, 3.765, 20.5, 580.5),
                new Product(100001, "레이델 면역쾌청", "a7f3be7b-b235-45b8-9fc5-28f2578ee8e0", 14.0, 140, 15, 570.50),
                new Product(100002, "그린스토어 우먼케어 건강한 질엔", "d3a6b7c4-c328-470b-b2c9-5e1b937acd0a", 10.75, 14.1, 15, 170.55),
                new Product(100003, "Bubbleless Vitamin-C", "8a2d7b5d-1a57-4055-a75b-98e495e58a4e", 18.0, 6, 20, 340.07)
        );
    }

    @Override
    public List<Product> createRandoms(int size) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            long serialNumber = RANDOM.nextInt(1000000) + 1000000;
            String name = RANDOM.nextDouble() <= 0.75 ? Mockables.generateRandomText(RANDOM.nextInt(16) + 1) : null;
            String apiId = UUID.randomUUID().toString();
            Double width = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 100 : null;
            double depth = RANDOM.nextDouble() * 100;
            double height = RANDOM.nextDouble() * 100;
            Double weight = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 1000 : null;

            Product product = new Product(serialNumber, name, apiId, width, depth, height, weight);
            products.add(product);
        }

        return products;
    }

}
