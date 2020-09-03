package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.constant.ToyType;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "toyType")
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

    @ExcelColumn(defaultValue = "(empty)")
    private String name;

    private ToyType toyType;

    private Double weight;

}
