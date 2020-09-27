package com.github.javaxcel.model.creature;

import com.github.javaxcel.model.Mockable;

public enum Sex implements Mockable<Sex> {

    MALE, INTERSEX, FEMALE;

    @Override
    public Sex createDesignee() {
        return MALE;
    }

    @Override
    public Sex createRandom() {
        int randomInt = RANDOM.nextInt(100);
        return randomInt < 50 ? FEMALE // 50%
                : randomInt > 50 ? MALE // 49%
                : INTERSEX; // 1%
    }

}
