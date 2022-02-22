package com.github.javaxcel.model.computer;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"cpu", "disk", "manufacturer", "price"})
@ExcelModel(explicit = true, headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
public class Computer {

    @ExcelColumn(name = "CPU_CLOCK")
    private BigInteger cpu;

    private Double ram;

    @ExcelColumn(name = "DISK_SIZE")
    private Long disk;

    private String inputDevice;

    private String outputDevice;

    @ExcelColumn
    private String manufacturer;

    @ExcelColumn
    private int price;

    public static List<Computer> newRandomList(int size) {
        List<Computer> computers = new ArrayList<>();
        Random random = TestUtils.getRandom();

        for (int i = 0; i < size; i++) {
            BigInteger cpu = random.nextDouble() <= 0.9
                    ? BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(random.nextLong())) : null;
            Double ram = random.nextDouble() <= 0.85
                    ? random.nextDouble() * 1000 : null;
            Long disk = random.nextDouble() <= 0.8
                    ? Math.abs(random.nextLong()) + 1000 : null;
            String inputDevice = random.nextDouble() <= 0.3
                    ? TestUtils.generateRandomText(random.nextInt(10) + 1) : null;
            String outputDevice = random.nextDouble() <= 0.33
                    ? TestUtils.generateRandomText(random.nextInt(10) + 1) : null;
            String manufacturer = random.nextDouble() <= 0.75
                    ? TestUtils.generateRandomText(random.nextInt(16) + 1) : null;
            int price = random.nextDouble() <= 0.5
                    ? random.nextInt(10_000_000) + 200_000 : 0;

            Computer computer = new Computer(cpu, ram, disk, inputDevice, outputDevice, manufacturer, price);
            computers.add(computer);
        }

        return computers;
    }

}
