package com.github.javaxcel.model.creature;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReaderExpression;
import com.github.javaxcel.annotation.ExcelWriterExpression;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class Creature {

    @ExcelColumn("Kingdom")
    @ExcelWriterExpression("#kingdom.toString().toLowerCase()")
    @ExcelReaderExpression("T(com.github.javaxcel.model.creature.Kingdom).valueOf(#kingdom.toUpperCase())")
    private Kingdom kingdom;

    @ExcelColumn("Sex")
    @ExcelWriterExpression("#kingdom.toString() + sex.toString().replaceAll('(.+)', '/$1/')")
    @ExcelReaderExpression("T(com.github.javaxcel.model.creature.Sex).valueOf(#sex.replaceAll(#kingdom.toUpperCase() + '|/', ''))")
    private Sex sex;

    @ExcelColumn("Lifespan")
    @ExcelWriterExpression("#lifespan + (#lifespan > 1 ? ' years' : ' year')")
    @ExcelReaderExpression("#lifespan.replaceAll('(\\d+).+', '$1')")
    private int lifespan;

}
