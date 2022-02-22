package com.github.javaxcel.model.creature;

import com.github.javaxcel.TestUtils;
import com.github.javaxcel.annotation.*;
import com.github.javaxcel.style.DefaultBodyStyleConfig;
import com.github.javaxcel.style.DefaultHeaderStyleConfig;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "agesFromTwilightToDeath")
@NoArgsConstructor
@ExcelModel(includeSuper = true, headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
public class Human extends Creature {

    @ExcelColumn(name = "Name", bodyStyle = DefaultHeaderStyleConfig.class)
    private String name;

    @ExcelColumn(name = "Birthday")
    @ExcelDateTimeFormat(pattern = "yyyy/MM/dd")
    @ExcelReaderExpression("T(java.time.LocalDate).parse(#birthday, T(java.time.format.DateTimeFormatter).ofPattern('yyyy/MM/dd'))")
    private LocalDate birthday;

    @ExcelColumn(name = "Birth Time")
    @ExcelDateTimeFormat(pattern = "HH/mm/ss.SSS")
    @ExcelReaderExpression("T(java.time.LocalTime).parse(#birthTime, T(java.time.format.DateTimeFormatter).ofPattern('HH/mm/ss.SSS'))")
    private LocalTime birthTime;

    @ExcelColumn(name = "Place of Birth")
    @ExcelWriterExpression("T(com.github.javaxcel.Converter).capitalize(#placeOfBirth, '-')")
    @ExcelReaderExpression("#placeOfBirth?.toLowerCase()") // null-safe operator '?.'
    private UUID placeOfBirth;

    @ExcelColumn(name = "Rest Seconds of Life")
    @ExcelWriterExpression("#restSecondsOfLife + ' sec'")
    @ExcelReaderExpression("new java.math.BigDecimal(#restSecondsOfLife.replace(' sec', ''))") // constructor
    private BigDecimal restSecondsOfLife;

    @ExcelColumn(name = "Number of Cells")
    @ExcelWriterExpression("#numOfCells + ' cells/kg'")
    @ExcelReaderExpression("new java.math.BigInteger(#numOfCells.replace(' cells/kg', ''))") // constructor
    private BigInteger numOfCells;

    @ExcelColumn(name = "Height")
    @ExcelWriterExpression("#height + ' cm'")
    @ExcelReaderExpression("T(Float).parseFloat(#height.replace(' cm', ''))")
    private float height;

    @ExcelColumn(name = "Weight")
    @ExcelWriterExpression("#weight + ' kg'")
    @ExcelReaderExpression("#weight.replace(' kg', '')") // This string will be parsed as float.
    private float weight;

    @ExcelColumn(name = "Ages from Birth to Puberty")
    @ExcelWriterExpression("T(java.util.Arrays).stream(#agesFromBirthToPuberty).boxed()" +
            ".collect(T(java.util.stream.Collectors).toList()).toString().replaceAll('[\\[\\]]', '')")
    @ExcelReaderExpression("#agesFromBirthToPuberty == null || #agesFromBirthToPuberty.equals('') ? null" +
            ": T(com.github.javaxcel.Converter).toIntArray(#agesFromBirthToPuberty.split(', '))")
    private int[] agesFromBirthToPuberty;

    @ExcelColumn(name = "Ages from Twilight to Death")
    @ExcelReaderExpression("new int[] {}")
    private int[] agesFromTwilightToDeath;

    @ExcelColumn(name = "Whether Disabled or Not")
    @ExcelWriterExpression("#disabled ? 'yes' : 'no'")
    @ExcelReaderExpression("#disabled eq 'yes' ? true : false")
    private boolean disabled;

    public Human(Kingdom kingdom, Sex sex, int lifespan, String name, LocalDate birthday, LocalTime birthTime,
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

    public static List<Human> createDesignees() {
        return Arrays.asList(
                new Human(Kingdom.ANIMALIA, Sex.MALE, 30, "Jeremy", LocalDate.now(), LocalTime.now(),
                        UUID.randomUUID(), new BigDecimal("34857058102347105675.583417804735034534756348701"),
                        new BigInteger("3024563964348504345428940615799280516897078902523043244"), 180, 70,
                        new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, new int[]{30}, true),
                new Human(Kingdom.ARCHAEA, Sex.INTERSEX, 100, "germ", LocalDate.now(), LocalTime.now(),
                        UUID.randomUUID(), new BigDecimal("95230947174310317483424235.32758234"),
                        new BigInteger("232549312541241462344"), 1, 1,
                        new int[]{0, 1}, new int[]{99, 100}, false),
                new Human(Kingdom.BACTERIA, Sex.INTERSEX, 47_000, "virus", LocalDate.now(), LocalTime.now(),
                        UUID.randomUUID(), new BigDecimal("1679811295023592390682741892423423905802.69345023"),
                        new BigInteger("672586170780398545235540893463155661323"), 1, 1,
                        new int[]{0}, new int[]{}, false),
                new Human(Kingdom.PLANTAE, Sex.FEMALE, 2000, "tree of life", LocalDate.now(), LocalTime.now(),
                        UUID.randomUUID(), new BigDecimal("728349210342742.2346791209564390683103567314567813420124892047128537183"),
                        new BigInteger("13489570439503567143859483067247856304724853452034"), 1000, 10_000,
                        new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{1996, 1997, 1998, 1999, 2000}, false)
        );
    }

    public static List<Human> newRandomList(int size) {
        List<Human> people = new ArrayList<>();
        Random random = TestUtils.getRandom();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            Kingdom kingdom = Kingdom.ANIMALIA;
            Sex sex = Sex.newRandom();
            int lifespan = random.nextInt(140) + 1;
            String name = random.nextDouble() <= 0.85 ? TestUtils.generateRandomText(random.nextInt(24) + 1) : null;
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

            Human human = new Human(kingdom, sex, lifespan, name, birthday, birthTime,
                    placeOfBirth, restSecondsOfLife, numOfCells, height, weight,
                    agesFromBirthToPuberty, agesFromTwilightToDeath, disabled);
            people.add(human);
        }

        return people;
    }

}
