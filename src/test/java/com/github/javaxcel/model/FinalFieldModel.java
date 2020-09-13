package com.github.javaxcel.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FinalFieldModel {

    private final int number = 100;

    private final String text = "TEXT";

}
