package com.github.javaxcel.model.sample;

import com.github.javaxcel.annotation.ExcelColumn;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

@ToString
@EqualsAndHashCode
public class PlainSample {

    private Long id;

    private String name;

    @ExcelColumn(defaultValue = "0.00")
    private BigDecimal price;

    private Set<String> tags;

}
