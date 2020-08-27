package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.constant.ToyType;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

    private String name;

    private ToyType toyType;

    private Double weight;

}
