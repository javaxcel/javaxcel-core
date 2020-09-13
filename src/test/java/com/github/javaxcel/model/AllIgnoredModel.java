package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AllIgnoredModel {

    @ExcelIgnore
    private int number;

    @ExcelIgnore
    private Character character;

}
