package com.github.javaxcel.model.computer;

import com.github.javaxcel.TestUtils.ExcludeOnPercentage;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"cpu", "disk", "manufacturer", "price"})
@ExcelModel(explicit = true, headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
public class Computer {

    @ExcludeOnPercentage(0.1)
    @ExcelColumn(name = "CPU_CLOCK")
    private BigInteger cpu;

    @ExcludeOnPercentage(0.15)
    private Double ram;

    @ExcludeOnPercentage(0.2)
    @ExcelColumn(name = "DISK_SIZE")
    private Long disk;

    @ExcludeOnPercentage(0.7)
    private String inputDevice;

    @ExcludeOnPercentage(0.67)
    private String outputDevice;

    @ExcelColumn
    @ExcludeOnPercentage(0.25)
    private String manufacturer;

    @ExcelColumn
    @ExcludeOnPercentage(0.5)
    private int price;

}
