package com.github.javaxcel.model.toy;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "targetAges")
@NoArgsConstructor
@ExcelModel(includeSuper = true, enumDropdown = true)
public class EducationToy extends Toy {

    @ExcelColumn(defaultValue = "14")
    private int[] targetAges;

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

    public static List<EducationToy> createDesignees() {
        return Arrays.asList(
                new EducationToy(null, ToyType.CHILD, 1800.0, null, "goals"),
                new EducationToy("레이델 면역쾌청", ToyType.ADULT, 585.54, new int[]{4, 5, 6, 7, 8, 9}, "Goals"),
                new EducationToy("Braun Series 7", ToyType.ADULT, 270.00, null, null),
                new EducationToy("베이비버스 가방퍼즐 키키·묘묘와 친구들", ToyType.CHILD, 2450.50, new int[]{9, 10, 11, 12, 13}, "education for children"),
                new EducationToy("마누스 기획 성인장갑 남", ToyType.ADULT, 126.6, null, "education for adult")
        );
    }

    public static List<EducationToy> newRandomList(int size) {
        List<EducationToy> toys = new ArrayList<>();
        Random random = TestUtils.getRandom();

        for (int i = 0; i < size; i++) {
            String name = random.nextDouble() <= 0.75 ? TestUtils.generateRandomText(random.nextInt(16) + 1) : null;
            ToyType toyType = random.nextDouble() >= 0.666 ? ToyType.CHILD : random.nextDouble() >= 0.333 ? ToyType.ADULT : null;
            Double weight = random.nextDouble() >= 0.5 ? random.nextDouble() * 1000 : null;
            int[] targetAges = random.nextDouble() >= 0.5 ? random.ints(5).toArray() : null;
            String goals = random.nextDouble() <= 0.75 ? TestUtils.generateRandomText(random.nextInt(8) + 1) : null;

            EducationToy toy = new EducationToy(name, toyType, weight, targetAges, goals);
            toys.add(toy);
        }

        return toys;
    }

}
