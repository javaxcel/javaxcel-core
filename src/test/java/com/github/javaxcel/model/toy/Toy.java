package com.github.javaxcel.model.toy;

import com.github.javaxcel.TestUtils.ExcludeOnPercentage;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

    @ExcludeOnPercentage(0.25)
    private String name;

    @ExcludeOnPercentage(0.1)
    private ToyType toyType;

    @ExcludeOnPercentage(0.5)
    private Double weight;

}
