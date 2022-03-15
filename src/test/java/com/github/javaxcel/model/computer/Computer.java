package com.github.javaxcel.model.computer;

import com.github.javaxcel.TestUtils.ConditionalOnPercentage;
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

    @ConditionalOnPercentage(0.9)
    @ExcelColumn(name = "CPU_CLOCK")
    private BigInteger cpu;

    @ConditionalOnPercentage(0.85)
    private Double ram;

    @ConditionalOnPercentage(0.8)
    @ExcelColumn(name = "DISK_SIZE")
    private Long disk;

    @ConditionalOnPercentage(0.3)
    private String inputDevice;

    @ConditionalOnPercentage(0.33)
    private String outputDevice;

    @ExcelColumn
    @ConditionalOnPercentage(0.75)
    private String manufacturer;

    @ExcelColumn
    @ConditionalOnPercentage(0.5)
    private int price;

}
