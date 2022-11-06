package com.github.javaxcel.model.creature;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.annotation.ExcelReadExpression;
import com.github.javaxcel.annotation.ExcelWriteExpression;
import com.github.javaxcel.internal.style.DefaultBodyStyleConfig;
import com.github.javaxcel.internal.style.DefaultHeaderStyleConfig;
import io.github.imsejin.common.tool.RandomString;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = {"placeOfBirth", "agesFromTwilightToDeath"})
@NoArgsConstructor(onConstructor_ = @ExcelModelCreator)
@ExcelModel(includeSuper = true, headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
public class Human extends Creature {

    @ExcelColumn(name = "Name", bodyStyle = DefaultHeaderStyleConfig.class)
    private String name;

    @ExcelColumn(name = "Birthday")
    @ExcelDateTimeFormat(pattern = "yyyy/MM/dd")
    @ExcelReadExpression("T(java.time.LocalDate).parse(#birthday, T(java.time.format.DateTimeFormatter).ofPattern('yyyy/MM/dd'))")
    private LocalDate birthday;

    @ExcelColumn(name = "Birth Time")
    @ExcelDateTimeFormat(pattern = "HH/mm/ss.SSS")
    @ExcelReadExpression("T(java.time.LocalTime).parse(#birthTime, T(java.time.format.DateTimeFormatter).ofPattern('HH/mm/ss.SSS'))")
    private LocalTime birthTime;

    @ExcelColumn(name = "Place of Birth", defaultValue = "new java.util.UUID(0, 0)")
    @ExcelWriteExpression("T(com.github.javaxcel.Converter).capitalize(#placeOfBirth, '-')")
    @ExcelReadExpression("#placeOfBirth?.toLowerCase()") // null-safe operator '?.'
    private UUID placeOfBirth;

    @ExcelColumn(name = "Rest Seconds of Life")
    @ExcelWriteExpression("#restSecondsOfLife + ' sec'")
    @ExcelReadExpression("new java.math.BigDecimal(#restSecondsOfLife.replace(' sec', ''))") // constructor
    private BigDecimal restSecondsOfLife;

    @ExcelColumn(name = "Number of Cells")
    @ExcelWriteExpression("#numOfCells + ' cells/kg'")
    @ExcelReadExpression("new java.math.BigInteger(#numOfCells.replace(' cells/kg', ''))") // constructor
    private BigInteger numOfCells;

    @ExcelColumn(name = "Height")
    @ExcelWriteExpression("#height + ' cm'")
    @ExcelReadExpression("T(Float).parseFloat(#height.replace(' cm', ''))")
    private float height;

    @ExcelColumn(name = "Weight")
    @ExcelWriteExpression("#weight + ' kg'")
    @ExcelReadExpression("#weight.replace(' kg', '')") // This string will be parsed as float.
    private float weight;

    @ExcelColumn(name = "Ages from Birth to Puberty")
    @ExcelWriteExpression("T(java.util.Arrays).stream(#agesFromBirthToPuberty).boxed()" +
            ".collect(T(java.util.stream.Collectors).toList()).toString().replaceAll('[\\[\\]]', '')")
    @ExcelReadExpression("#agesFromBirthToPuberty == null || #agesFromBirthToPuberty.equals('') ? null" +
            ": T(com.github.javaxcel.Converter).toIntArray(#agesFromBirthToPuberty.split(', '))")
    private int[] agesFromBirthToPuberty;

    @ExcelColumn(name = "Ages from Twilight to Death")
    @ExcelReadExpression("new int[] {}")
    private int[] agesFromTwilightToDeath;

    @ExcelColumn(name = "Whether Disabled or Not")
    @ExcelWriteExpression("#disabled ? 'yes' : 'no'")
    @ExcelReadExpression("#disabled eq 'yes' ? true : false")
    private boolean disabled;

    @Builder
    private Human(Kingdom kingdom, Sex sex, int lifespan, String name, LocalDate birthday, LocalTime birthTime,
                  UUID placeOfBirth, BigDecimal restSecondsOfLife, BigInteger numOfCells, float height, float weight,
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

    public static List<Human> newRandomList(int size) {
        List<Human> people = new ArrayList<>();
        Random random = TestUtils.getRandom();
        RandomString randomString = new RandomString(random);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            Kingdom kingdom = Kingdom.ANIMALIA;
            Sex sex = Sex.newRandom();
            int lifespan = random.nextInt(140) + 1;
            String name = random.nextDouble() <= 0.85 ? randomString.nextString(24) : null;
            LocalDate birthday = LocalDate.now();
            LocalTime birthTime = LocalTime.now().withNano(123_000_000); // with 123 ms
            UUID placeOfBirth = name == null ? null : UUID.randomUUID();

            sb.setLength(0);
            sb.append(random.nextInt(Integer.MAX_VALUE - 10_000_000) + 10_000_000);
            sb.append(random.nextInt(Integer.MAX_VALUE));
            BigInteger numOfCells = new BigInteger(sb.toString());

            BigDecimal restSecondsOfLife = BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.valueOf(random.nextInt())).add(BigDecimal.valueOf(random.nextDouble()));
            int[] agesFromBirthToPuberty = random.nextDouble() >= 0.2
                    ? random.ints(10, 0, 18).toArray()
                    : random.ints(5, 0, 12).toArray();
            int[] agesFromTwilightToDeath = agesFromBirthToPuberty == null
                    ? null
                    : random.ints(18, lifespan - 1, lifespan).toArray();

            float height = random.nextFloat() * 160F + 30F; // 30.0 ~ 250.0
            float weight = random.nextFloat() * 330F + 2.5F; // 2.5 ~ 330.0
            boolean disabled = random.nextInt(100) <= 1; // 2%

            Human human = Human.builder().kingdom(kingdom)
                    .sex(sex)
                    .lifespan(lifespan)
                    .name(name)
                    .birthday(birthday)
                    .birthTime(birthTime)
                    .placeOfBirth(placeOfBirth)
                    .restSecondsOfLife(restSecondsOfLife)
                    .numOfCells(numOfCells)
                    .height(height)
                    .weight(weight)
                    .agesFromBirthToPuberty(agesFromBirthToPuberty)
                    .agesFromTwilightToDeath(agesFromTwilightToDeath)
                    .disabled(disabled)
                    .build();
            people.add(human);
        }

        return people;
    }

}
