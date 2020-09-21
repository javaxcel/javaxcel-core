

<p align="center">
	<img width="35%" src="./src/main/resources/main-image.png" alt="Javaxcel Core">
</p>


<p align="center">
    <img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/m/javaxcel/javaxcel-core">
    <img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/javaxcel/javaxcel-core?label=github">
    <img alt="Bintray" src="https://img.shields.io/bintray/v/imsejin/Javaxcel/javaxcel-core">
    <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-core">
    <img alt="GitHub All Releases" src="https://img.shields.io/github/downloads/javaxcel/javaxcel-core/total?label=downloads%20at%20github">
    <img alt="Bintray" src="https://img.shields.io/bintray/dt/imsejin/Javaxcel/javaxcel-core?label=downloads%20at%20bintray">
    <img alt="GitHub" src="https://img.shields.io/github/license/javaxcel/javaxcel-core">
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange">
</p>
Javaxcel is utilities for writing and reading excel file with simple usage based annotations.



### Pre-requirement

- Add **apache-poi** in your dependencies (this depends on **apache-poi**).



### Getting started

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
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
```

Code with simple usage.



### Examples

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

This is `Product` class for examples.



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

This is `EducationalProduct` class for examples.



#### ExcelWriter

##### No option

```java
public void save(List<Product> products, File dest) throw IOException {
    FileOutputStream out = new FileOutputStream(dest);
    XSSFWorkbook workbook = new XSSFWorkbook();
    
    ExcelWriter.init(workbook, Product.class).write(out, products);
}

/* ... */

save(products, new File("/data", "products.xlsx"));
```
The result is

| serialNumber | name         | accessId            | width | depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

The header order is the same as the declared field order.

If nothing is specified for the column, the field name is header name.



##### Exclude field

```java
@ExcelIgnore
private String accessId;
```

The result is

| serialNumber | name         | width | depth | height | weight |
| ------------ | ------------ | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal |       | 0.0   | 20.5   | 580.5  |

If you want to exclude several fields, annotate `ExcelIgnore`.



##### Name the header

```java
@ExcelColumn("PRODUCT_NO")
private long serialNumber;
@ExcelColumn // Ineffective
private String name;
```

The result is

| PRODUCT_NO | name         | accessId            | width | depth | height | weight |
| ---------- | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000      | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

If you want to name the header, annotate `ExcelColumn` and assign `#value` you want.

If you don't assign the value, it's the same as not annotating it.



If you want to use header names only once or override `ExcelColumn#value`, invoke `ExcelWriter#headerNames`.

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

If the number of arguments is not equal to the number of targeted fields, `ExcelWriter` throws exception.



##### Set the default value

```java
@ExcelColumn(value = "WIDTH", defaultValue = "0.0mm") // Default value is effective except primitive type.
private Double width;
@ExcelColumn(value = "Depth", defaultValue = "(empty)") // Default value is ineffective to primitive type.
private double depth;
@ExcelColumn(defaultValue = "0")
private double height;
```

The result is

| serialNumber | name         | accessId            | WIDTH | Depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e | 0.0mm | 0.0   | 20.5   | 580.5  |

It's ineffective to assign default value to primitive type, because the field of primitive type is always initialized.



If you want to use default value only once or override `ExcelColumn#defaultValue`, invoke `ExcelWriter#defaultValue`.

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

`ExcelWriter#defaultValue` applies to all fields.



##### Write the class without the targeted fields

```java
class NoFieldModel {}
class AllIgnoredModel {
    @ExcelIgnore
    private int number;
    @ExcelIgnore
    private Character character;
}

/* ... */

ExcelWriter.init(workbook, NoFieldModel.class).write(out, list); // Occurs exception.
ExcelWriter.init(workbook, AllIgnoredModel.class).write(out, list); // Occurs exception.
```

If you try to write with the class that has no targeted fields, `ExcelWriter` throws exception.



##### Model that extends class

```java
ExcelWriter.init(workbook, EducationalProduct.class).write(out, list);
```

The result is

| targetAges  | goals                | date       | time         | dateTime                |
| ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |

It writes the declared own fields, not including the inherited fields.



```java
@ExcelModel(policy = TargetedFieldPolicy.INCLUDES_INHERITED)
class EducationalProduct extends Product {
    /* ... */
}
```

But if you annotate `ExcelModel` and assign `TargetedFieldPolicy.INCLUDES_INHERITED` into `#policy`, it writes including the inherited fields.

The result is

| serialNumber | name                             | accessId            | width | depth | height | weight | targetAges  | goals                | date       | time         | dateTime                |
| ------------ | -------------------------------- | ------------------- | ----- | ----- | ------ | ------ | ----------- | -------------------- | ---------- | ------------ | ----------------------- |
| 10001        | Mathematics puzzle toys for kids | 1a57-4055-a75b-98e4 | 18.0  | 6.0   | 20.0   | 340.07 | [I@6a84a97d | Develop intelligence | 2020-09-13 | 11:54:26.176 | 2020-09-13T11:54:26.176 |



##### Format date/time

```java
@ExcelDateTimeFormat(pattern = "yyyyMMdd")
private LocalDate date = LocalDate.now();
@ExcelDateTimeFormat(pattern = "HH/mm/ss")
private LocalTime time = LocalTime.now();
@ExcelDateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
private LocalDateTime dateTime = LocalDateTime.now();
```

The result is

| date     | time     | dateTime                |
| -------- | -------- | ----------------------- |
| 20200913 | 11/54/26 | 2020-09-13 11:54:26.176 |

If you want to write formatted `LocalDate`, `LocalTime` or `LocalDateTime`, annotate `ExcelDateTimeFormat` and assign `#pattern` you want.



##### Name a Sheet

```java
ExcelWriter.init(workbook, Product.class)
	.sheetName("Products")
	.write(out, products);
```

If you want to name a sheet, invoke `ExcelWriter#sheetName`.

If you don't, the name of sheet is `Sheet`.



##### Decorate sheet/row/cell

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
	.adjustSheet((sheet, numOfRows, numOfColumns) -> {
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

You can adjust a sheet with `ExcelWriter#adjustSheet` that provides three parameters that are sheet, the number of rows and the number of columns.



You can decorate the first row with `ExcelWriter#headerStyle`.



You can decorate the columns except the first row with `ExcelWriter#columnStyles`.

If the number of arguments is not equal to the number of targeted fields, `ExcelWriter` throws exception.

When you input single argument, `ExcelWriter` applies it to all columns.



#### ExcelReader

Model must has a constructor without parameters, so that `ExcelReader` can instantiate.

