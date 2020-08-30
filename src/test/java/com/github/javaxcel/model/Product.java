package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelIgnore;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "apiId")
public class Product {

    @ExcelColumn("상품번호")
    private long serialNumber;

    private String name;

    @ExcelIgnore
    @ExcelColumn("API_ID")
    private String apiId;

    @ExcelColumn(value = "가로")
    private Double width;

    private double depth;

    private double height;

    @ExcelColumn(value = "WEIGHT", defaultValue = "0")
    private Double weight;

}
