package com.github.javaxcel.converter.out;

abstract class DefaultValueStore {

    /**
     * Replacement for field value when it is null or empty.
     */
    String defaultValue;

    /**
     * Returns the default value.
     *
     * @return default value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets up the default value.
     *
     * @param defaultValue default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
