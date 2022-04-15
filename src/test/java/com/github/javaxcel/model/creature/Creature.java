package com.github.javaxcel.model.creature;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.internal.style.DefaultHeaderStyleConfig;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class Creature {

    @ExcelColumn(name = "Kingdom", enumDropdown = true, dropdownItems = {"archaea", "bacteria", "protista", "animalia", "fungi", "plantae"})
    @ExcelWriteExpression("#kingdom.toString().toLowerCase()")
    @ExcelReadExpression("T(com.github.javaxcel.model.creature.Kingdom).valueOf(#kingdom.toUpperCase())")
    private Kingdom kingdom;

    @ExcelColumn(name = "Sex", headerStyle = DefaultBodyStyleConfig.class, bodyStyle = DefaultHeaderStyleConfig.class)
    @ExcelWriteExpression("#kingdom.toString() + #sex.toString().replaceAll('(.+)', '/$1/')")
    @ExcelReadExpression("T(com.github.javaxcel.model.creature.Sex).valueOf(#sex.replaceAll(#kingdom.toUpperCase() + '|/', ''))")
    private Sex sex;

    @ExcelColumn(name = "Lifespan")
    @ExcelWriteExpression("#lifespan + (#lifespan > 1 ? ' years' : ' year')")
    @ExcelReadExpression("#lifespan.replaceAll('(\\d+).+', '$1')")
    private int lifespan;

}
