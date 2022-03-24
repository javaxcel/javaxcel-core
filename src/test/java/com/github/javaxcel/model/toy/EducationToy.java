package com.github.javaxcel.model.toy;

import com.github.javaxcel.TestUtils.ConditionalOnPercentage;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "targetAges")
@ExcelModel(includeSuper = true, enumDropdown = true)
public class EducationToy extends Toy {

    @ConditionalOnPercentage(0.5)
    @ExcelColumn(defaultValue = "[]")
    private int[][] targetAges;

    @ConditionalOnPercentage(0.75)
    private String goals;

    @ExcelColumn
    private LocalDate date = LocalDate.now();

    @ExcelDateTimeFormat(pattern = "HH/mm/ss/SSS")
    private LocalTime time = LocalTime.now().withNano(123_000_000); // with 123 ms

    private LocalDateTime dateTime = LocalDateTime.now().withNano(0);

    public EducationToy(String name, ToyType toyType, Double weight, int[] targetAges, String goals) {
        super(name, toyType, weight);
        this.targetAges = targetAges;
        this.goals = goals;
    }

}
