package com.github.javaxcel.model.factory;

import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public final class MockFactory {

    private static final Random RANDOM = new Random();

    /**
     * Products for test.
     */
    private static final List<Product> products = Arrays.asList(
            new Product(100000, "알티지 클린 Omega 3", "9b9e7d29-2a60-4973-aec0-685e672eb07a", 3.0, 3.765, 20.5, 580.5),
            new Product(100001, "레이델 면역쾌청", "a7f3be7b-b235-45b8-9fc5-28f2578ee8e0", 14.0, 140, 15, 570.50),
            new Product(100002, "그린스토어 우먼케어 건강한 질엔", "d3a6b7c4-c328-470b-b2c9-5e1b937acd0a", 10.75, 14.1, 15, 170.55),
            new Product(100003, "Bubbleless Vitamin-C", "8a2d7b5d-1a57-4055-a75b-98e495e58a4e", 18.0, 6, 20, 340.07)
    );

    /**
     * Box for test.
     */
    private static final Box<EducationToy> box = new ToyBox<>(Arrays.asList(
            new EducationToy("", ToyType.CHILD, 1800.0, null, "goals"),
            new EducationToy("레이델 면역쾌청", ToyType.ADULT, 585.54, new int[]{4, 5, 6, 7, 8, 9}, "Goals"),
            new EducationToy("Braun Series 7", ToyType.ADULT, 270.00, null, null),
            new EducationToy("베이비버스 가방퍼즐 키키·묘묘와 친구들", ToyType.CHILD, 2450.50, new int[]{9, 10, 11, 12, 13}, "education for children"),
            new EducationToy("마누스 기획 성인장갑 남", ToyType.ADULT, 126.6, null, "education for adult")
    ));

    private MockFactory() {}

    public static List<Product> generateStaticProducts() {
        return products;
    }

    public static Box<EducationToy> generateStaticBox() {
        return box;
    }

    public static List<Product> generateRandomProducts(long size) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            long serialNumber = RANDOM.nextInt(1000000) + 1000000;
            String name = RANDOM.nextDouble() <= 0.75 ? generateRandomText(RANDOM.nextInt(16)) : null;
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

    public static Box<EducationToy> generateRandomBox(long size) {
        Box<EducationToy> box = new ToyBox<>(new ArrayList<>());

        for (int i = 0; i < size; i++) {
            String name = RANDOM.nextDouble() <= 0.75 ? generateRandomText(RANDOM.nextInt(16)) : null;
            ToyType toyType = RANDOM.nextDouble() >= 0.666 ? ToyType.CHILD : RANDOM.nextDouble() >= 0.333 ? ToyType.ADULT : null;
            Double weight = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 1000 : null;
            int[] targetAges = RANDOM.nextDouble() >= 0.5 ? RANDOM.ints(5).toArray() : null;
            String goals = RANDOM.nextDouble() <= 0.75 ? generateRandomText(RANDOM.nextInt(8)) : null;

            EducationToy educationToy = new EducationToy(name, toyType, weight, targetAges, goals);
            box.getAll().add(educationToy);
        }

        return box;
    }

    private static String generateRandomText(int len) {
        final int leftLimit = 97; // letter 'a'
        final int rightLimit = 122; // letter 'z'

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
