package com.github.javaxcel.model.creature;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReaderConversion;
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
    @ExcelWriterConversion("#kingdom.toString().toLowerCase()")
    @ExcelReaderConversion("T(com.github.javaxcel.model.creature.Kingdom).valueOf(#kingdom.toUpperCase())")
    private Kingdom kingdom;

    @ExcelColumn("Sex")
    @ExcelWriterConversion("#kingdom.toString() + sex.toString().replaceAll('(.+)', '/$1/')")
    @ExcelReaderConversion("T(com.github.javaxcel.model.creature.Sex).valueOf(#sex.replaceAll('ANIMALIA|/', ''))")
    private Sex sex;

    @ExcelColumn("Lifespan")
    @ExcelWriterConversion("#lifespan + (#lifespan > 1 ? ' years' : ' year')")
    @ExcelReaderConversion("#lifespan.replaceAll('(\\d+).+', '$1')")
    private int lifespan;

}
