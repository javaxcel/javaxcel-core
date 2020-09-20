package com.github.javaxcel.model.creature;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelWriterConversion;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class Creature {

    @ExcelColumn("Kingdom")
    @ExcelWriterConversion(expression = "#kingdom.toString().toLowerCase()")
    private Kingdom kingdom;

    @ExcelColumn("Sex")
    @ExcelWriterConversion(expression = "#kingdom.toString() + #sex.toString().replaceAll('(.+)', '/$1/')")
    private Sex sex;

    @ExcelColumn("Lifespan")
    @ExcelWriterConversion(expression = "#lifespan + (#lifespan > 1 ? ' years' : ' year')")
    private int lifespan;

}
