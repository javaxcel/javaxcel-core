package com.github.javaxcel.model.toy;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.model.Mockables;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "targetAges")
@NoArgsConstructor
@ExcelModel(policy = TargetedFieldPolicy.INCLUDES_INHERITED)
public class EducationToy extends Toy implements Mockables<EducationToy> {

    @ExcelColumn(defaultValue = "<null>")
    private int[] targetAges;

    private String goals;

    @ExcelColumn
    private LocalDate date = LocalDate.now();

    @ExcelDateTimeFormat(pattern = "HH/mm/ss/SSS")
    private LocalTime time = LocalTime.now();

    private LocalDateTime dateTime = LocalDateTime.now();

    public EducationToy(String name, ToyType toyType, Double weight, int[] targetAges, String goals) {
        super(name, toyType, weight);
        this.targetAges = targetAges;
        this.goals = goals;
    }

    @Override
    public List<EducationToy> createDesignees() {
        return Arrays.asList(
                new EducationToy(null, ToyType.CHILD, 1800.0, null, "goals"),
                new EducationToy("레이델 면역쾌청", ToyType.ADULT, 585.54, new int[]{4, 5, 6, 7, 8, 9}, "Goals"),
                new EducationToy("Braun Series 7", ToyType.ADULT, 270.00, null, null),
                new EducationToy("베이비버스 가방퍼즐 키키·묘묘와 친구들", ToyType.CHILD, 2450.50, new int[]{9, 10, 11, 12, 13}, "education for children"),
                new EducationToy("마누스 기획 성인장갑 남", ToyType.ADULT, 126.6, null, "education for adult")
        );
    }

    @Override
    public List<EducationToy> createRandoms(int size) {
        List<EducationToy> toys = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String name = RANDOM.nextDouble() <= 0.75 ? Mockables.generateRandomText(RANDOM.nextInt(16) + 1) : null;
            ToyType toyType = RANDOM.nextDouble() >= 0.666 ? ToyType.CHILD : RANDOM.nextDouble() >= 0.333 ? ToyType.ADULT : null;
            Double weight = RANDOM.nextDouble() >= 0.5 ? RANDOM.nextDouble() * 1000 : null;
            int[] targetAges = RANDOM.nextDouble() >= 0.5 ? RANDOM.ints(5).toArray() : null;
            String goals = RANDOM.nextDouble() <= 0.75 ? Mockables.generateRandomText(RANDOM.nextInt(8) + 1) : null;

            EducationToy toy = new EducationToy(name, toyType, weight, targetAges, goals);
            toys.add(toy);
        }

        return toys;
    }

}
