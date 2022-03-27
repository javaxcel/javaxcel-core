package com.github.javaxcel.model.toy;

import com.github.javaxcel.TestUtils.ConditionalOnPercentage;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

    @ConditionalOnPercentage(0.75)
    private String name;

    @ConditionalOnPercentage(0.99)
    private ToyType toyType;

    @ConditionalOnPercentage(0.5)
    private Double weight;

}
