package com.github.javaxcel.model.toy;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "toyType")
@NoArgsConstructor
@AllArgsConstructor
public class Toy {

    private String name;

    private ToyType toyType;

    private Double weight;

}
