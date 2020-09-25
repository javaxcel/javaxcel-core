package com.github.javaxcel.model.toy;

import com.github.javaxcel.annotation.ExcelColumn;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "toyType")
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

//    @ExcelColumn(defaultValue = "(empty)")
    private String name;

    private ToyType toyType;

    private Double weight;

}
