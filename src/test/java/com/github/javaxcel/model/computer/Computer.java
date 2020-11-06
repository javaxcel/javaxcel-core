package com.github.javaxcel.model.computer;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.model.Mockable;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Computer> createRandoms(int size) {
        List<Computer> computers = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            BigInteger cpu = Mockable.RANDOM.nextDouble() <= 0.9
                    ? BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Mockable.RANDOM.nextLong())) : null;
            Double ram = Mockable.RANDOM.nextDouble() <= 0.85
                    ? Mockable.RANDOM.nextDouble() * 1000 : null;
            Long disk = Mockable.RANDOM.nextDouble() <= 0.8
                    ? Math.abs(Mockable.RANDOM.nextLong()) + 1000 : null;
            String inputDevice = Mockable.RANDOM.nextDouble() <= 0.3
                    ? Mockable.generateRandomText(Mockable.RANDOM.nextInt(10) + 1) : null;
            String outputDevice = Mockable.RANDOM.nextDouble() <= 0.33
                    ? Mockable.generateRandomText(Mockable.RANDOM.nextInt(10) + 1) : null;
            String manufacturer = Mockable.RANDOM.nextDouble() <= 0.75
                    ? Mockable.generateRandomText(Mockable.RANDOM.nextInt(16) + 1) : null;
            int price = Mockable.RANDOM.nextDouble() <= 0.5
                    ? Mockable.RANDOM.nextInt(10_000_000) + 200_000 : 0;

            Computer computer = new Computer(cpu, ram, disk, inputDevice, outputDevice, manufacturer, price);
            computers.add(computer);
        }

        return computers;
    }

}
