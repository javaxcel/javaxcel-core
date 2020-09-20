package com.github.javaxcel.model.creature;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelWriterConversion;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.converter.Converter;
import com.github.javaxcel.model.Mockables;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = {"birthday", "birthTime"})
@NoArgsConstructor
@ExcelModel(policy = TargetedFieldPolicy.INCLUDES_INHERITED)
public class Human extends Creature implements Mockables<Human> {

    @ExcelColumn("Name")
    private String name;

    @ExcelColumn("Birthday")
    @ExcelDateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate birthday;

    @ExcelColumn("Birth Time")
    @ExcelDateTimeFormat(pattern = "HH/mm/ss.SSS")
    private LocalTime birthTime;

    @ExcelColumn("Place of Birth")
    @ExcelWriterConversion(expression = "#capitalize(#placeOfBirth)", clazz = Converter.class, methodName = "capitalize", paramTypes = String.class)
    private String placeOfBirth;

    @ExcelColumn("Rest Seconds of Life")
    @ExcelWriterConversion(expression = "#restSecondsOfLife + ' sec'")
    private BigDecimal restSecondsOfLife;

    @ExcelColumn("Number of Cells")
    @ExcelWriterConversion(expression = "#numOfCells + ' cells/kg'")
    private BigInteger numOfCells;

    @ExcelColumn("Height")
    @ExcelWriterConversion(expression = "#height + ' cm'")
    private float height;

    @ExcelColumn("Weight")
    @ExcelWriterConversion(expression = "#weight + ' kg'")
    private float weight;

    @ExcelColumn("Ages from Birth to Puberty")
    @ExcelWriterConversion(expression = "T(java.util.Arrays).stream(#agesFromBirthToPuberty).boxed()" +
            ".collect(T(java.util.stream.Collectors).toList()).toString().replaceAll('[\\[\\]]', '')")
    private int[] agesFromBirthToPuberty;

    @ExcelColumn("Ages from Twilight to Death")
    private int[] agesFromTwilightToDeath;

    @ExcelColumn("Whether Disabled or Not")
    @ExcelWriterConversion(expression = "#disabled ? 'yes' : 'no'")
    private boolean disabled;

    public Human(Kingdom kingdom, Sex sex, int lifespan, String name, LocalDate birthday, LocalTime birthTime,
                 String placeOfBirth, BigDecimal restSecondsOfLife, BigInteger numOfCells, float height, float weight,
                 int[] agesFromBirthToPuberty, int[] agesFromTwilightToDeath, boolean disabled) {
        super(kingdom, sex, lifespan);
        this.name = name;
        this.birthday = birthday;
        this.birthTime = birthTime;
        this.placeOfBirth = placeOfBirth;
        this.restSecondsOfLife = restSecondsOfLife;
        this.numOfCells = numOfCells;
        this.height = height;
        this.weight = weight;
        this.agesFromBirthToPuberty = agesFromBirthToPuberty;
        this.agesFromTwilightToDeath = agesFromTwilightToDeath;
        this.disabled = disabled;
    }

    @Override
    public List<Human> createDesignees() {
        return Arrays.asList(new Human(Kingdom.ANIMALIA, Sex.MALE, 30, "name", LocalDate.now(), LocalTime.now(),
                "Seoul, Republic of Korea", new BigDecimal("34857058102347105675.583417804735034534756348701"),
                new BigInteger("3024563964348504345428940615799280516897078902523043244"), 180, 70,
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, new int[]{}, false));
    }

    @Override
    public List<Human> createRandoms(int size) {
        List<Human> people = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            Kingdom kingdom = Kingdom.ANIMALIA;
            Sex sex = Sex.MALE.createRandom();
            int lifespan = RANDOM.nextInt(140) + 1;
            String name = RANDOM.nextDouble() <= 0.85 ? Mockables.generateRandomText(RANDOM.nextInt(24)) : null;
            LocalDate birthday = LocalDate.now();
            LocalTime birthTime = LocalTime.now();
            String placeOfBirth = name == null ? null : UUID.randomUUID().toString();

            sb.setLength(0);
            sb.append(RANDOM.nextInt(Integer.MAX_VALUE - 10_000_000) + 10_000_000);
            sb.append(RANDOM.nextInt(Integer.MAX_VALUE));
            BigInteger numOfCells = new BigInteger(sb.toString());

            BigDecimal restSecondsOfLife = BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.valueOf(RANDOM.nextInt())).add(BigDecimal.valueOf(RANDOM.nextDouble()));
            int[] agesFromBirthToPuberty = RANDOM.nextDouble() >= 0.2
                    ? RANDOM.ints(10, 0, 18).toArray()
                    : RANDOM.ints(5, 0, 12).toArray();
            int[] agesFromTwilightToDeath = agesFromBirthToPuberty == null
                    ? null
                    : RANDOM.ints(18, lifespan - 1, lifespan).toArray();

            float height = RANDOM.nextFloat() * 160F + 30F; // 30.0 ~ 250.0
            float weight = RANDOM.nextFloat() * 330F + 2.5F; // 2.5 ~ 330.0
            boolean disabled = RANDOM.nextInt(100) <= 1; // 2%

            Human human = new Human(kingdom, sex, lifespan, name, birthday, birthTime,
                    placeOfBirth, restSecondsOfLife, numOfCells, height, weight,
                    agesFromBirthToPuberty, agesFromTwilightToDeath, disabled);
            people.add(human);
        }

        return people;
    }

}
