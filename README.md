<p align="center" width="40%">
	<img  src="./src/main/resources/main-image.png" alt="Javaxcel Core">
</p>



<div style="display: flex; justify-content: center;">
    <p align="center" style="width: 50%">
        <img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/m/javaxcel/javaxcel-core">
        <img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/javaxcel/javaxcel-core?label=github">
        <img alt="Bintray" src="https://img.shields.io/bintray/v/imsejin/Javaxcel/javaxcel-core">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-core">
        <img alt="GitHub All Releases" src="https://img.shields.io/github/downloads/javaxcel/javaxcel-core/total?label=downloads%20at%20github">
        <img alt="Bintray" src="https://img.shields.io/bintray/dt/imsejin/Javaxcel/javaxcel-core?label=downloads%20at%20bintray">
        <img alt="GitHub" src="https://img.shields.io/github/license/javaxcel/javaxcel-core">
        <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange">
    </p>
</div>



Javaxcel is utilities for writing and reading excel file with simple usage based annotations.

<br><br>

# Getting started

```xml
<!-- Maven -->
<dependency>
  <groupId>com.github.javaxcel</groupId>
  <artifactId>javaxcel-core</artifactId>
  <version>${javaxcel.version}</version>
</dependency>
```

```groovy
// Gradle
implementation 'com.github.javaxcel:javaxcel-core:$javaxcel_version'
```

<br>

```java
File src = new File("/data", "old-products.xls");
File dest = new File("/data", "new-products.xlsx");

try (FileOutputStream out = new FileOutputStream(dest);
        HSSFWorkbook oldWorkbook = new HSSFWorkbook(src);
        XSSFWorkbook newWorkbook = new XSSFWorkbook()) {
    // Reads the first excel sheet and returns data as a list.
    List<Product> products = ExcelReader.init(oldWorkbook, Product.class).read();
    
    // Creates a excel file and writes data to the first excel sheet.
    ExcelWriter.init(newWorkbook, Product.class).write(out, products);
} catch (IOException e) {
    e.printStackTrace();
}
```

Code with simple usage.

<br><br>

# Examples

```java
class Product {
    private long serialNumber;
    private String name;
    private String accessId;
    private Double width;
    private double depth;
    private double height;
    private Double weight;
}

/* ... */

Product product = Product.builder()
    .serialNumber(10000)
    .name("Choco cereal")
    .accessId("2a60-4973-aec0-685e")
    .height(20.5)
    .weight(580.5)
    .build();
List<Product> products = Collections.singletonList(product);
```

There is a list that contains a `Product`.

<br><br>

## No option

### write:

```java
File dest = new File("/data", "products.xlsx")
FileOutputStream out = new FileOutputStream(dest);
XSSFWorkbook workbook = new XSSFWorkbook();

ExcelWriter.init(workbook, Product.class).write(out, products);
```
The result is

| serialNumber | name         | accessId            | width | depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

The column order is the same as the declared field order.

If nothing is specified for the column, header name is the field name.

<br>

### read:

```java
List<Product> products = ExcelReader.init(workbook, Product.class).read();
```

The result is

```json
[
    {
        "serialNumber": 10000,
        "name": "Choco cereal",
        "apiId": "2a60-4973-aec0-685e",
        "width": null,
        "depth": 0.0,
        "height": 20.5,
        "weight": 580.5
	}
]
```

The column order, also when read, is the same as the declared field order.

Model must has a constructor without parameters, so that [ExcelReader](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/in/ExcelReader.java) can instantiate.

<br><br>

## Exclude field

```java
@ExcelIgnore
private String accessId;
```

### write:

| serialNumber | name         | width | depth | height | weight |
| ------------ | ------------ | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal |       | 0.0   | 20.5   | 580.5  |

If you want to exclude several fields, annotate [ExcelIgnore](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/annotation/ExcelIgnore.java) to them.

<br>

### read:

```json
[
    {
        "serialNumber": 10000,
        "name": "Choco cereal",
        "apiId": null,
        "width": null,
        "depth": 0.0,
        "height": 20.5,
        "weight": 580.5
    }
]
```

[ExcelReader](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/in/ExcelReader.java) will pass the fields that annotated [ExcelIgnore](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/annotation/ExcelIgnore.java) by.

If column `apiId` exists and `Product#apiId` is still annotated [ExcelIgnore](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/annotation/ExcelIgnore.java),

the exception will occur becauseof setting `apiId` to `width` (NumberFormatException).

<br><br>

## Name the header

```java
@ExcelColumn(name = "PRODUCT_NO")
private long serialNumber;
@ExcelColumn // Ineffective
private String name;
```

### write:

| PRODUCT_NO | name         | accessId            | width | depth | height | weight |
| ---------- | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000      | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

If you want to name the header, annotate [ExcelColumn](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelColumn.java#L8) and assign [#name()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelColumn.java#L15) you want.

If you don't assign, it's the same as not annotating it.

<br>

If you want to use header names only once or override [ExcelColumn#name()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelColumn.java#L15), invoke [ExcelWriter#headerNames(String...)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L99).

```java
ExcelWriter.init(workbook, Product.class)
    .headerNames("PRD_NO","NM","ACS_ID","WID","DEP","HEI","WEI") // 7
//    .headerNames("PRD_NO","NM","ACS_ID","WID","DEP","HEI") // 6 => Occurs exception.
    .write(out, products);
```

The result is

| PRD_NO | NM           | ASC_ID              | WID  | DEP  | HEI  | WEI   |
| ------ | ------------ | ------------------- | ---- | ---- | ---- | ----- |
| 10000  | Choco cereal | 2a60-4973-aec0-685e |      | 0.0  | 20.5 | 580.5 |

If the number of arguments is not equal to the number of targeted fields, [ExcelWriter](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L33) throws exception.

<br>

### read:

Not affected.

<br><br>

## Set the default value

```java
@ExcelColumn(name = "WIDTH", defaultValue = "0.0mm") // Default value is effective except primitive type.
private Double width;
@ExcelColumn(name = "Depth", defaultValue = "(empty)") // Default value is ineffective to primitive type.
private double depth;
@ExcelColumn(defaultValue = "0")
private double height;
```

### write:

| serialNumber | name         | accessId            | WIDTH | Depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e | 0.0mm | 0.0   | 20.5   | 580.5  |

It's ineffective to assign default value to primitive type, because the field of primitive type is always initialized.

<br>

If you want to use default value only once or override [ExcelColumn#defaultValue()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelColumn.java#L22), invoke [ExcelWriter#defaultValue(String)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L110).

```java
Product product = Product.builder().build(); // Not assigns to all fields.
List<Product> products = Collections.singletonList(product);

ExcelWriter.init(workbook, Product.class)
	.defaultValue("(empty)")
	.write(out, products);
```

The result is

| serialNumber | name    | accessId | WIDTH   | Depth | height | weight  |
| ------------ | ------- | -------- | ------- | ----- | ------ | ------- |
| 0            | (empty) | (empty)  | (empty) | 0.0   | 0.0    | (empty) |

[ExcelWriter#defaultValue(String)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L110) will be applied to all fields.

<br>

### read:

Not affected,

but if you set [ExcelColumn#defaultValue()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelColumn.java#L22) that doesn't match type of its field, the exception for type casting may occurred.

<br><br>

## Model without the targeted fields

```java
class NoFieldModel {}
class AllIgnoredModel {
    @ExcelIgnore
    private int number;
    @ExcelIgnore
    private Character character;
}
```

### write:

```java
ExcelWriter.init(workbook, NoFieldModel.class).write(out, list); // Occurs exception.
ExcelWriter.init(workbook, AllIgnoredModel.class).write(out, list); // Occurs exception.
```

If you try to write with the class that has no targeted fields, [ExcelWriter](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L33) will throw exception.

<br>

### read:

```java
List<NoFieldModel> noFieldModels = ExcelReader.init(workbook, NoFieldModel.class).read(); // Occurs exception.
List<AllIgnoredModel> allIgnoredModels = ExcelReader.init(workbook, AllIgnoredModel.class).read(); // Occurs exception.
```

If you try to write with the class that has no targeted fields, [ExcelReader](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/in/ExcelReader.java) will throw exception.

<br><br>

## Model that extends class

```java
class EducationalProduct extends Product {
    private int[] targetAges;
    private String goals;
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime dateTime;
}

/* ... */

EducationalProduct eduProduct = EducationalProduct.builder()
    .serialNumber(10001)
    .name("Mathematics puzzle toys for kids")
    .accessId("1a57-4055-a75b-98e4")
    .width(18.0)
    .depth(6)
    .height(20)
    .weight(340.07)
    .targetAges(4,5,6,7,8,9)
    .goals("Develop intelligence")
    .date(LocalDate.now())
    .time(LocalTime.now())
    .dateTime(LocalDateTime.now())
    .build();
List<EducationalProduct> eduProducts = Collections.singletonList(eduProduct);
```

There is a list that contains a `EducationalProduct`.

<br>

### write:

```java
ExcelWriter.init(workbook, EducationalProduct.class).write(out, list);
```

| targetAges  | goals                | date       | time         | dateTime                |
| ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

It writes the declared own fields, not including the inherited fields.

It's default.

<br>

```java
@ExcelModel(policy = TargetedFieldPolicy.INCLUDES_INHERITED)
class EducationalProduct extends Product { /* ... */ }
```

But if you annotate [ExcelModel](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelModel.java#L10) and assign [TargetedFieldPolicy#INCLUDES_INHERITED](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/constant/TargetedFieldPolicy.java#L13) into [#policy()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelModel.java#L17), it writes including the inherited fields.

The result is

| serialNumber | name                             | accessId            | width | depth | height | weight | targetAges  | goals                | date       | time         | dateTime                |
| ------------ | -------------------------------- | ------------------- | ----- | ----- | ------ | ------ | ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| 10001        | Mathematics puzzle toys for kids | 1a57-4055-a75b-98e4 | 18.0  | 6.0   | 20.0   | 340.07 | [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

<br>

### read:

```java
List<EducationalProduct> eduProducts = ExcelReader.init(workbook, EducationalProduct.class).read();
```

```json
[
    {
        "serialNumber": 10001,
        "name": "Mathematics puzzle toys for kids",
        "accessId": "1a57-4055-a75b-98e4",
        "width": 18.0,
        "depth": 6.0,
        "height": 20.0,
        "weight": 340.07,
        "targetAges": null, // Not supported type
        "goals": "Develop intelligence",
        "date": "2020-09-13",
        "time": "11:54:26.176",
        "dateTime": "2020-09-13T11:54:26.176"
    }	
]
```

Basically supported type

- primitive type, Wrapper(primitive) type
- `String`
- `BigInteger`, `BigDecimal`
- `LocalDate`, `LocalTime`, `LocalDateTime`

<br>

Others are not supported, so that the field value will be null.

<br><br>

## Format date/time

```java
@ExcelDateTimeFormat(pattern = "yyyyMMdd")
private LocalDate date = LocalDate.now();
@ExcelDateTimeFormat(pattern = "HH/mm/ss")
private LocalTime time = LocalTime.now();
@ExcelDateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
private LocalDateTime dateTime = LocalDateTime.now();
```

### write:

| date     | time     | dateTime                |
| -------- | -------- | ----------------------- |
| 20200913 | 11/54/26 | 2020-09-13 11:54:26.176 |

If you want to write formatted `LocalDate`, `LocalTime` or `LocalDateTime`, annotate [ExcelDateTimeFormat](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelDateTimeFormat.java#L8) and assign [#pattern()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelDateTimeFormat.java#L19) you want.

<br>

### read:

```json
{
    "date": "2020-09-13",
    "time": "11:54:26.0",
    "dateTime": "2020-09-13T11:54:26.176"
}
```

[ExcelReader](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/in/ExcelReader.java) parses `LocalDate`, `LocalTime` and `LocalDateTime` with [ExcelDateTimeFormat#pattern()](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/annotation/ExcelDateTimeFormat.java#L19).

<br><br>

## Name a Sheet

### write:

```java
ExcelWriter.init(workbook, Product.class)
	.sheetName("Products")
	.write(out, products);
```

If you want to name a sheet, invoke [ExcelWriter#sheetName(String)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L119).

If you don't, the name is `Sheet`.

<br>

### read:

Not affected.

<br><br>

## Decorate sheet/row/cell

### write:

```java
// Function that colors columns into blue.
BiFunction<CellStyle, Font, CellStyle> blueColumn = (style, font) -> {
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
};
// Function that colors columns into green.
BiFunction<CellStyle, Font, CellStyle> greenColumn = (style, font) -> {
    style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
};

ExcelWriter.init(workbook, Product.class)
	.adjustSheet((sheet, numOfRows, numOfColumns) -> { // TriConsumer
        // Makes the columns fit content.
        for (int i = 0; i < numOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        // Hides extra rows.
        int maxRows = 1_048_576;
        for (int i = numOfRows - 1; i < maxRows; i++) {
            Row row = sheet.createRow(i);
            row.setZeroHeight(true);
        }

        // Hides extra columns.
        int maxColumns = 16_384;
        for (int i = numOfColumns; i < maxColumns; i++) {
            sheet.setColumnHidden(i, true);
        }
    })
    .headerStyle((style, font) -> {
        font.setItalic(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor((short) 8);
        font.setBold(true);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    })
    .columnStyles(blueColumn, greenColumn, blueColumn, greenColumn, blueColumn, greenColumn, blueColumn) // 7 => Each columns
//    .columnStyles(blueColumn) // 1 => All columns
    .write(out, products);
```

You can adjust a sheet with [ExcelWriter#adjustSheet(TriConsumer)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L132) that provides three parameters that are sheet, the number of rows and the number of columns.

<br>

You can decorate the first row with [ExcelWriter#headerStyle(BiFunction)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L139).

<br>

You can decorate the columns except the first row with [ExcelWriter#columnStyles(BiFunction...)](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L150).

If the number of arguments is not equal to the number of targeted fields, [ExcelWriter](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L33) throws exception.

When you input single argument, [ExcelWriter](https://github.com/javaxcel/javaxcel-core/blob/a0de67fb87ddb8254c25b7d738228819fe332065/src/main/java/com/github/javaxcel/out/ExcelWriter.java#L33) applies it to all columns.

<br>

### read:

Not affected.

<br><br>

## Expression

### write:

```java
// Product
@ExcelWriterExpression("T(io.github.imsejin.common.util.StringUtils).formatComma(#serialNumber)")
private long serialNumber;
@ExcelWriterExpression("#name.toUpperCase()")
private String name;
@ExcelWriterExpression("#accessId.replaceAll('\\d+', '')")
private String accessId;
@ExcelWriterExpression("T(String).valueOf(#width).replaceAll('\\.0+$', '')")
private Double width;
@ExcelWriterExpression("#depth + 'cm'")
private double depth;
private double height;
@ExcelWriterExpression("T(Math).ceil(#weight)")
private Double weight;

// EducationalProduct
@ExcelWriterExpression("T(java.util.Arrays).stream(#targetAges)" +
        ".boxed()" +
        ".collect(T(java.util.stream.Collectors).toList())" +
        ".toString()" +
        ".replaceAll('[\\[\\]]', '')")
private int[] targetAges;
@ExcelWriterExpression("'none'") // Static value
private String goals;
@ExcelWriterExpression("T(java.time.LocalDateTime).of(#date, #time)") // Refers other field
private LocalDate date;
private LocalTime time;
private LocalDateTime dateTime;
```

| serialNumber | name                             | accessId | width | depth | height | weight | targetAges       | goals | date                    | time         | dateTime                |
| ------------ | -------------------------------- | -------- | ----- | ----- | ------ | ------ | ---------------- | ----- | ----------------------- | ------------ | ----------------------- |
| 10,001       | MATHEMATICS PUZZLE TOYS FOR KIDS | a--ab-e  | 18    | 6.0cm | 20.0   | 341.0  | 4, 5, 6, 7, 8, 9 | none  | 2020-09-13T11:54:26.176 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

You can pre-process field value with [ExcelWriterExpression](https://github.com/javaxcel/javaxcel-core/blob/435b9208b10a06cb2056b9690bd04f6dd416e573/src/main/java/com/github/javaxcel/annotation/ExcelWriterExpression.java#L15) before set value into a cell.

The expression language is `SpEL(Spring Expression Language)`.

Look [here](https://github.com/spring-projects/spring-framework/tree/master/spring-expression) for more details about this.

<br>

If you want to refer field in a expression, write `#` and the `field name`. (e.g. #targetAges)

Also you can refer other field. We call this as `variable`.

<br>

Field you can refer is only targeted field.

It means you cannot refer the field that is annotated with [ExcelIgnore](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/annotation/ExcelIgnore.java).

If type of expression result is not `String`, the converter will invoke `Object#toString()`.

<br>

### read:

```java
// Product
@ExcelReaderExpression("T(Long).parseLong(#serialNumber.replace(',', ''))")
private long serialNumber;
@ExcelReaderExpression("#name.charAt(0) + #name.substring(1).toLowerCase()")
private String name;
@ExcelReaderExpression("#accessId.replaceAll('-', '0')")
private String accessId;
private Double width;
@ExcelReaderExpression("#depth.replace('cm', '')") // This string will be parsed as double.
private double depth;
private double height;
@ExcelReaderExpression("T(Double).parseDouble(#weight) - 0.93")
private Double weight;

// EducationalProduct
@ExcelReaderExpression("T(com.github.javaxcel.Converter).toIntArray(#targetAges.split(', ')") // Custom converter method
private int[] targetAges;
@ExcelReaderExpression("'Develop intelligence'") // Static value
private String goals;
@ExcelReaderExpression("T(java.time.LocalDate).parse(#date)")
private LocalDate date;
private LocalTime time;
private LocalDateTime dateTime;

// com.github.javaxcel.Converter
public class Converter {
    public static int[] toIntArray(String[] strs) {
        return Arrays.stream(strs).mapToInt(Integer::parseInt).toArray();
    }    
}
```

```json
[
    {
        "serialNumber": 10001,
        "name": "Mathematics puzzle toys for kids",
        "accessId": "a00ab0e",
        "width": 18.0,
        "depth": 6.0,
        "height": 20.0,
        "weight": 340.07,
        "targetAges": [
            4, 5, 6, 7, 8, 9
        ],
        "goals": "Develop intelligence",
        "date": "2020-09-13",
        "time": "11:54:26.176",
        "dateTime": "2020-09-13T11:54:26.176"
    }	
]
```

You can support not basic supported type with [ExcelReaderExpression](https://github.com/javaxcel/javaxcel-core/blob/dev/src/main/java/com/github/javaxcel/annotation/ExcelReaderExpression.java).

The type of `variable` is `String`. It is value in cell.

