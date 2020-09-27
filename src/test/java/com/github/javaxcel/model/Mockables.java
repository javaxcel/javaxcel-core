package com.github.javaxcel.model;

import java.util.List;
import java.util.Random;

public interface Mockables<E> {

    Random RANDOM = new Random();

    static String generateRandomText(int len) {
        final int leftLimit = 97; // letter 'a'
        final int rightLimit = 122; // letter 'z'

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    List<E> createDesignees();

    List<E> createRandoms(int size);

}
