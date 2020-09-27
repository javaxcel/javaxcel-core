package com.github.javaxcel.model;

import java.util.Random;

public interface Mockable<E> {

    Random RANDOM = new Random();

    static String generateRandomText(int len) {
        final int leftLimit = 97; // letter 'a'
        final int rightLimit = 122; // letter 'z'

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    E createDesignee();

    E createRandom();

}
