package com.github.javaxcel.constant;

public enum TargetedFieldPolicy {

    /**
     * Selects only own declared fields except the inherited.
     */
    OWN_FIELDS,

    /**
     * Selects declared fields including the inherited.
     */
    INCLUDES_INHERITED,

}
