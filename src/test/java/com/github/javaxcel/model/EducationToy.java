package com.github.javaxcel.model;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.constant.ToyType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = {"targetAges", "date", "time", "dateTime"})
@NoArgsConstructor
@ExcelModel(policy = TargetedFieldPolicy.INCLUDES_INHERITED)
public class EducationToy extends Toy {

    @ExcelColumn(defaultValue = "<null>")
    private int[] targetAges;

    private String goals;

    @ExcelColumn
    private LocalDate date = LocalDate.now();

//    @ExcelColumn
    @ExcelDateTimeFormat(pattern = "HH/mm/ss/SSS")
    private LocalTime time = LocalTime.now();

    private LocalDateTime dateTime = LocalDateTime.now();

    public EducationToy(String name, ToyType toyType, Double weight, int[] targetAges, String goals) {
        super(name, toyType, weight);
        this.targetAges = targetAges;
        this.goals = goals;
    }

}
