package com.github.javaxcel.model.product;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelIgnore;
import lombok.*;
import org.jeasy.random.annotation.Exclude;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"width", "weight", "dates"})
public class Product {

    @ExcelColumn(name = "상품번호")
    private long serialNumber;

    private String name;

    @ExcelColumn(name = "API_ID")
    private UUID apiId;

    @ExcelIgnore
    @ExcelColumn(name = "가로")
    private Double width;

    @ExcelColumn(defaultValue = "(empty)") // Default value is ineffective to primitive type.
    private double depth;

    private double height;

    @ExcelColumn(name = "WEIGHT", defaultValue = "-1") // Default value is effective to reference type.
    private Double weight;

    @Exclude
    @ExcelColumn(defaultValue = "[1999.01.31., 2009.07.31., 2019.12.31.]")
    @ExcelDateTimeFormat(pattern = "yyyy.MM.dd.")
    private LocalDate[] dates;

}
