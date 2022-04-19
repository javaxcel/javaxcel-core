<p align="center">
    <img src="./src/main/resources/javaxcel-core-logo.png" alt="Javaxcel Core" width="20%">
</p>

<h1 align="center">Javaxcel Core</h1>

<p align="center">Supporter for export and import of excel file</p>

<p align="center">
    <!--
    <a href="https://travis-ci.com/github/javaxcel/javaxcel-core">
        <img alt="Travis CI" src="https://img.shields.io/travis/com/javaxcel/javaxcel-core/release?style=flat-square"/>
    </a>
    -->
    <a href="https://github.com/javaxcel/javaxcel-core/actions/workflows/maven-build.yml">
        <img alt="Github Workflow" src="https://img.shields.io/github/workflow/status/javaxcel/javaxcel-core/Java%20CI%20with%20Maven/release?style=flat-square">
    </a>
    <a href="https://codecov.io/gh/javaxcel/javaxcel-core">
        <img alt="Codecov branch" src="https://img.shields.io/codecov/c/github/javaxcel/javaxcel-core/release?label=code%20coverage&style=flat-square&token=X7ZO535W9K"/>
    </a>
    <a href="https://search.maven.org/artifact/com.github.javaxcel/javaxcel-core">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.github.javaxcel/javaxcel-core?style=flat-square">
    </a>
    <br/>
    <a href="https://lgtm.com/projects/g/javaxcel/javaxcel-core/context:java">
        <img alt="Lgtm grade" src="https://img.shields.io/lgtm/grade/java/github/javaxcel/javaxcel-core.svg?logo=&logoWidth=&label=lgtm%3A%20code%20quality&&style=flat-square"/>
    </a>
    <a href="https://www.codacy.com/gh/javaxcel/javaxcel-core/dashboard">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/a430e1580c5f44d2b2f1b2c11195cc49?label=codacy%3A%20code%20quality&style=flat-square">
    </a>
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange?style=flat-square">
</p>

## Table of Contents

- [What is Javaxcel?](#what-is-javaxcel)
- [Getting started](#getting-started)
- [Examples](#examples)
  1. [No option](#no-option)
  2. [Exclude field](#exclude-field)
  3. [Name the header](#name-the-header)
  4. [Set the default value](#set-the-default-value)
  5. [Model without the targeted fields](#model-without-the-targeted-fields)
  6. [Model that extends class](#model-that-extends-class)
  7. [Format date/time](#format-datetime)
  8. [Name a Sheet](#name-a-sheet)
  9. [Decoration](#decoration)
  10. [Expression](#expression)
  11. [Enum value constraint](#enum-value-constraint)
  12. [Limitation of reading rows](#limitation-of-reading-rows)
  13. [Parallel reading](#parallel-reading)
  13. [Resolution of constructor and method](#resolution-of-constructor-and-method)
  13. [Add handler for custom type](#add-handler-for-custom-type)
  14. [Support java.util.Map](#support-javautilmap)

<br><br>

# What is Javaxcel?

Javaxcel is a supporter for exporting `java.util.List` to spreadsheets and importing `java.util.List` from spreadsheets using [Apache POI](https://github.com/apache/poi).

<br><br>

# Getting started

### Maven

```xml
<dependency>
  <groupId>com.github.javaxcel</groupId>
  <artifactId>javaxcel-core</artifactId>
  <version>${javaxcel-core.version}</version>
</dependency>

<!-- Required dependency -->
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>${apache-poi.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "com.github.javaxcel", name: "javaxcel-core", version: "$javaxcelCoreVersion"

// Required dependency
implementation group: "org.apache.poi", name: "poi-ooxml", version: "$apachePoiVersion"
```

<br>

```java
// Creates an instance of Javaxcel.
Javaxcel javaxcel = Javaxcel.newInstance();

File src = new File("/data", "old-products.xls");
File dest = new File("/data", "new-products.xlsx");

try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        Workbook oldWorkbook = new HSSFWorkbook(in);
        Workbook newWorkbook = new SXSSFWorkbook()) {
    // Reads all the sheets and returns data as a list.
    List<Product> products = javaxcel.reader(oldWorkbook, Product.class).read();
    
    // Creates an Excel file and writes data to it.
    javaxcel.writer(newWorkbook, Product.class).write(out, products);
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

### writer:

```java
File dest = new File("/data", "products.xlsx")
OutputStream out = new FileOutputStream(dest);
Workbook workbook = new SXSSFWorkbook();

Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .write(out, products);
```
The result is

| serialNumber | name         | accessId            | width | depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

The column order is the same as the declared field order.

If nothing is specified for the column, header name is the field name.

<br>

### reader:

```java
File src = new File("/data", "products.xlsx");
Workbook workbook = new XSSFWorkbook(src);

List<Product> products = Javaxcel.newInstance()
        .reader(workbook, Product.class)
        .read();
```

The result is

```json
[
  {
    "serialNumber": 10000,
    "name": "Choco cereal",
    "accessId": "2a60-4973-aec0-685e",
    "width": null,
    "depth": 0.0,
    "height": 20.5,
    "weight": 580.5
  }
]
```

The column order, also when read, is the same as the declared field order.

Model must has a constructor without parameters, so that `ExcelReader` can instantiate.

<br><br>

## Exclude field

```java
@ExcelIgnore
private String accessId;
```

### writer:

| serialNumber | name         | width | depth | height | weight |
| ------------ | ------------ | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal |       | 0.0   | 20.5   | 580.5  |

If you want to exclude several fields, annotate `@ExcelIgnore` to them.

<br>

### reader:

```json
[
  {
    "serialNumber": 10000,
    "name": "Choco cereal",
    "accessId": null,
    "width": null,
    "depth": 0.0,
    "height": 20.5,
    "weight": 580.5
  }
]
```

`ExcelReader` will pass the fields that annotated `@ExcelIgnore` by.

If column `accessId` exists in Excel sheet and `Product#accessId` is still annotated `@ExcelIgnore`,

the exception will occur because of setting `accessId` to `width` (NumberFormatException).

<br><br>

## Name the header

```java
@ExcelColumn(name = "PRODUCT_NO")
private long serialNumber;

@ExcelColumn
private String name;
```

### writer:

| PRODUCT_NO | name         | accessId            | width | depth | height | weight |
| ---------- | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000      | Choco cereal | 2a60-4973-aec0-685e |       | 0.0   | 20.5   | 580.5  |

If you want to name the header, annotate `@ExcelColumn` and assign `name()` you want.

<br>

If you want to override `ExcelColumn#name()` (use header names on this moment),
invoke `ExcelWriter#options(ExcelWriteStrategy...)` with `ExcelWriteStrategy.HeaderNames`.

```java
List<String> headerNames = Arrays.asList("PRD_NO","NM","ACS_ID","WID","DEP","HEI","WEI"); // 7
// List<String> headerNames = Arrays.asList("PRD_NO","NM","ACS_ID","WID","DEP","HEI"); // 6: Occurs exception.

Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .options(new HeaderNames(headerNames))
        .write(out, products);
```

The result is

| PRD_NO | NM           | ASC_ID              | WID  | DEP  | HEI  | WEI   |
| ------ | ------------ | ------------------- | ---- | ---- | ---- | ----- |
| 10000  | Choco cereal | 2a60-4973-aec0-685e |      | 0.0  | 20.5 | 580.5 |

If the number of arguments is not equal to the number of targeted fields, `ExcelWriter` throws exception.

<br><br>

## Set the default value

```java
// Default value is effective except primitive type.
@ExcelColumn(name = "WIDTH", defaultValue = "0.0mm")
private Double width;

// Default value is ineffective to primitive type.
@ExcelColumn(name = "Depth", defaultValue = "(empty)")
private double depth;

@ExcelColumn(defaultValue = "0")
private double height;
```

### writer:

| serialNumber | name         | accessId            | WIDTH | Depth | height | weight |
| ------------ | ------------ | ------------------- | ----- | ----- | ------ | ------ |
| 10000        | Choco cereal | 2a60-4973-aec0-685e | 0.0mm | 0.0   | 20.5   | 580.5  |

It's ineffective to assign default value to primitive type, because the field of primitive type is always initialized.

<br>

If you want to override `ExcelColumn#defaultValue()` (use another default value on this moment),
invoke `ExcelWriter#options(ExcelWriteStrategy...)` with option `ExcelWriteStrategy.DefaultValue`.

```java
Product product = new Product(); // Empty product.
List<Product> products = Collections.singletonList(product);

Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .options(new DefaultValue("(empty)"))
        .write(out, products);
```

The result is

| serialNumber | name    | accessId | WIDTH   | Depth | height | weight  |
| ------------ | ------- | -------- | ------- | ----- | ------ | ------- |
| 0            | (empty) | (empty)  | (empty) | 0.0   | 0.0    | (empty) |

`ExcelWriteStrategy.DefaultValue(String)` will be applied to all fields.

<br>

### reader:

If cell value is null or empty string, `DefaultExcelReadConverter` parse `ExcelColumn#defaultValue()` and set it to field.

Specify `ExcelColumn#defaultValue()` with care.
It might not occur exception, when you write, though the default value doesn't match type of that field.
It must occur exception, when you read, if the default value doesn't.

If the field that is annotated by `@ExcelReadExpression`, its default value doesn't work.

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

### writer:

```java
Javaxcel.newInstance()
        .writer(workbook, NoFieldModel.class); // Occurs exception.
Javaxcel.newInstance()
        .writer(workbook, AllIgnoredModel.class); // Occurs exception.
```

If you try to write with the class that has no targeted fields, `ExcelWriter` will throw exception.

<br>

### reader:

```java
List<NoFieldModel> noFieldModels = Javaxcel.newInstance()
        .reader(workbook, NoFieldModel.class); // Occurs exception.
List<AllIgnoredModel> allIgnoredModels = Javaxcel.newInstance()
        .reader(workbook, AllIgnoredModel.class); // Occurs exception.
```

If you try to write with the class that has no targeted fields, `ExcelReader` will throw exception.

<br><br>

## Model that extends class

```java
class EducationalProduct extends Product {
    private int[] targetAges;
    private String goals;
    private Product related; // Unknown type.
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
        .related(new Product())
        .date(LocalDate.now())
        .time(LocalTime.now())
        .dateTime(LocalDateTime.now())
        .build();
List<EducationalProduct> eduProducts = Collections.singletonList(eduProduct);
```

There is a list that contains a `EducationalProduct`.

<br>

### writer:

```java
Javaxcel.newInstance()
        .writer(workbook, EducationalProduct.class)
        .write(out, list);
```

| targetAges         | goals                | related                              | date       | time     | dateTime            |
| ------------------ | -------------------- | ------------------------------------ | ---------- | -------- | ------------------- |
| [4, 5, 6, 7, 8, 9] | Develop intelligence | com.github.javaxcel.Product@4bf558aa | 2020-09-13 | 11:54:26 | 2020-09-13 11:54:26 |

It writes the declared own fields, not including the inherited fields.

It's default.

<br>

```java
@ExcelModel(includeSuper = true)
class EducationalProduct extends Product { /* ... */ }
```

But if you annotate `@ExcelModel` and assign true into `includeSuper()`, it writes including the inherited fields.

The result is

| serialNumber | name                             | accessId            | width | depth | height | weight | targetAges         | goals                | related                             | date       | time     | dateTime            |
| ------------ | -------------------------------- | ------------------- | ----- | ----- | ------ | ------ | ------------------ | -------------------- | ----------------------------------- | ---------- | -------- | ------------------- |
| 10001        | Mathematics puzzle toys for kids | 1a57-4055-a75b-98e4 | 18.0  | 6.0   | 20.0   | 340.07 | [4, 5, 6, 7, 8, 9] | Develop intelligence | com.github.javaxcel.Product@b1a58a3 | 2020-09-13 | 11:54:26 | 2020-09-13 11:54:26 |

<br>

### reader:

```java
List<EducationalProduct> eduProducts = Javaxcel.newInstance()
        .reader(workbook, EducationalProduct.class)
        .read();
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
    "targetAges": [4, 5, 6, 7, 8, 9],
    "goals": "Develop intelligence",
    "related": null, // No handler for this type.
    "date": "2020-09-13",
    "time": "11:54:26",
    "dateTime": "2020-09-13 11:54:26"
  }
]
```

To know what types are basically supported, see `DefaultExcelTypeHandlerRegistry`.

<br>

Others are not supported by default, so that the field value will be null (if primitive, will be initial value of that type).
You can register handler for unknown types.
1. Create an instance of `ExcelTypeHandlerRegistryImpl`.
2. Make implementations of `AbstractExcelTypeHandler`.
3. Add handlers to registry.
4. Invoke `Javaxcel.newInstance(ExcelTypeHandlerRegistry)` with your registry.

<br><br>

## Format date/time

```java
@ExcelDateTimeFormat(pattern = "yyyyMMdd")
private LocalDate date = LocalDate.now();

@ExcelDateTimeFormat(pattern = "HH/mm/ss")
private LocalTime time = LocalTime.now();

// Default pattern: "yyyy-MM-dd HH:mm:ss"
private LocalDateTime dateTime = LocalDateTime.now();
```

### writer:

| date     | time     | dateTime            |
| -------- | -------- | ------------------- |
| 20200913 | 11/54/26 | 2020-09-13 11:54:26 |

If you want to write formatted value, annotate `@ExcelDateTimeFormat` and assign `pattern()` you want.
These are supported types by default.
* `java.util.Date`
* `java.time.LocalTime`
* `java.time.LocalDate`
* `java.time.LocalDateTime`
* `java.time.ZonedDateTime`
* `java.time.OffsetTime`
* `java.time.OffsetDateTime`

<br>

### reader:

```json
{
  "date": "2020-09-13",
  "time": "11:54:26.0",
  "dateTime": "2020-09-13T11:54:26.0"
}
```

`ExcelReader` parses them with option `ExcelDateTimeFormat#pattern()`.

<br><br>

## Name a Sheet

### writer:

```java
Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .options(new SheetName("Products"))
        .write(out, products);
```

If you want to name a sheet, add option `ExcelWriteStrategy.SheetName`.

If you don't, the default name is `Sheet`.

<br><br>

## Decoration

### writer:

```java
Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .options(
            new AutoResizedColumns(),
            new HiddenExtraRows(),
            new HiddenExtraColumns(),
            new HeaderStyles(new DefaultHeaderStyleConfig()),
            new BodyStyles(new DefaultBodyStyleConfig())
        )
        .write(out, products);
```

You can adjust all sheets with option `ExcelWriteStrategy.AutoResizedColumns`, `ExcelWriteStrategy.HiddenExtraRows` and `ExcelWriteStrategyHiddenExtraColumns`.

<br>

You can decorate the header with option `ExcelWriteStrategy.HeaderStyles`

and also decorate the body with option `ExcelWriteStrategy.BodyStyles`.

<br>

If the number of arguments is not equal to 1 or the number of targeted fields, `ExcelWriter` throws exception.

When you input single argument, `ExcelWriter` applies it to all columns.

<br>

```java
@ExcelModel(headerStyle = DefaultHeaderStyleConfig.class, bodyStyle = DefaultBodyStyleConfig.class)
class Product {

    @ExcelColumn(headerStyle = RedColumnStyleConfig.class)
    private long serialNumber;

    @ExcelColumn(bodyStyle = GrayColumnStyleConfig.class)
    private String name;

    /* ... */
}
```

You can also decorate the header and body with annotations.

Look [here](https://github.com/javaxcel/javaxcel-styler) for how to configure styles.

<br>

`ExcelColumn#headerStyle()` takes precedence over `ExcelModel#headerStyle()`.

`ExcelColumn#bodyStyle()` takes precedence over `ExcelModel#bodyStyle()`.

<br>

`serialNumber` will be applied with `RedColumnStyleConfig`. and `DefaultBodyStyleConfig`.

`name` will be applied with `DefaultHeaderStyleConfig` and `GrayColumnStyleConfig`.

<br><br>

## Expression

### writer:

```java
class Product {
    @ExcelWriteExpression("T(io.github.imsejin.common.util.StringUtils).formatComma(#serialNumber)")
    private long serialNumber;

    @ExcelWriteExpression("#name.toUpperCase()")
    private String name;

    @ExcelWriteExpression("#accessId.replaceAll('\\d+', '')")
    private String accessId;

    @ExcelWriteExpression("T(String).valueOf(#width).replaceAll('\\.0+$', '')")
    private Double width;

    @ExcelWriteExpression("#depth + 'cm'")
    private double depth;

    private double height;

    @ExcelWriteExpression("T(Math).ceil(#weight)")
    private Double weight;
}

@ExcelModel(includeSuper = true)
class EducationalProduct extends Product {
    @ExcelWriteExpression("T(java.util.Arrays).stream(#targetAges)" +
            ".boxed()" +
            ".collect(T(java.util.stream.Collectors).toList())" +
            ".toString()" +
            ".replaceAll('[\\[\\]]', '')")
    private int[] targetAges;

    // Fixed value
    @ExcelWriteExpression("'none'")
    private String goals;

    private Product related;

    // Refers other field
    @ExcelWriteExpression("T(java.time.LocalDateTime).of(#date, #time)")
    private LocalDate date;

    private LocalTime time;

    private LocalDateTime dateTime;    
}
```

| serialNumber | name                             | accessId | width | depth | height | weight | targetAges       | goals | related                             | date                    | time     | dateTime            |
| ------------ | -------------------------------- | -------- | ----- | ----- | ------ | ------ | ---------------- | ----- | ----------------------------------- | ----------------------- | -------- | ------------------- |
| 10,001       | MATHEMATICS PUZZLE TOYS FOR KIDS | a--ab-e  | 18    | 6.0cm | 20.0   | 341.0  | 4, 5, 6, 7, 8, 9 | none  | com.github.javaxcel.Product@b1a58a3 | 2020-09-13T11:54:26.176 | 11:54:26 | 2020-09-13 11:54:26 |

You can pre-process field value with `@ExcelWriteExpression` before set value into a cell.

The expression language is `SpEL(Spring Expression Language)`.

Look [here](https://github.com/spring-projects/spring-framework/tree/master/spring-expression) for more details about this.

<br>

If you want to refer field in an expression, write `#` and the `field name`. (e.g. #targetAges)

Also you can refer other field. We call this as `variable`.

<br>

Field you can refer is only targeted field.

It means you cannot refer the field that is annotated with `@ExcelIgnore` (or `ExcelModel#explicit()`).

If type of expression result is not `String`, the converter will invoke `Object#toString()`.

<br>

### reader:

```java
class Product {
    @ExcelReadExpression("T(Long).parseLong(#serialNumber.replace(',', ''))")
    private long serialNumber;

    @ExcelReadExpression("#name.charAt(0) + #name.substring(1).toLowerCase()")
    private String name;

    @ExcelReadExpression("#accessId.replaceAll('-', '0')")
    private String accessId;

    private Double width;

    // This string will be parsed as double.
    @ExcelReadExpression("#depth.replace('cm', '')")
    private double depth;

    private double height;

    @ExcelReadExpression("T(Double).parseDouble(#weight) - 0.93")
    private Double weight;
}

@ExcelModel(includeSuper = true)
class EducationalProduct extends Product {
    // Custom converter method
    @ExcelReadExpression("T(com.github.javaxcel.Converter).toPowerIntArray(#targetAges.split(', ')")
    private int[] targetAges;

    // Fixed value
    @ExcelReadExpression("'Develop intelligence'")
    private String goals;

    @ExcelReadExpression("new com.github.javaxcel.Product()")
    private Product related;

    @ExcelReadExpression("T(java.time.LocalDate).parse(#date.substring(0, 10))")
    private LocalDate date;

    private LocalTime time;

    private LocalDateTime dateTime;
}

// com.github.javaxcel.Converter
public class Converter {
    public static int[] toPowerIntArray(String[] strs) {
        return Arrays.stream(strs).mapToInt(Integer::parseInt).map(i -> i + 1).toArray();
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
    "targetAges": [16, 25, 36, 49, 64, 81],
    "goals": "Develop intelligence",
    "related": {
      "serialNumber": 0,
      "name": null,
      "accessId": null,
      "width": null,
      "depth": 0.0,
      "height": 0.0,
      "weight": null
    },
    "date": "2020-09-13",
    "time": "11:54:26.0",
    "dateTime": "2020-09-13T11:54:26.0"
  }
]
```

You can support not basic supported type with `@ExcelReadExpression`.

The type of `variable` is `String`. It is value in cell.

<br><br>

## Enum value constraint

### writer:

```java
@ExcelColumn(enumDropdown = true)
private java.nio.file.AccessMode accessMode;

@ExcelColumn(enumDropdown = true, dropdownItems = {"days", "hrs", "min", "sec", "ms", "us", "ns"})
private java.util.concurrent.TimeUnit timeUnit;
```

If you specify enable `ExcelColumn#enumDropdown()` to field whose type is `Enum`,
this column will have constraint. In other words, it will have dropdown items as `Enum#name()`.

If also specify `ExcelColumn#dropdownItems()` to enum field,
it wll have dropdown items as `ExcelColumn#dropdownItems()`.

```java
Javaxcel.newInstance()
        .writer(workbook, Product.class)
        .options(new EnumDropdown())
        .write(out, products);
```

Even if `enumDropdown()` is `false` in `ExcelModel` or `ExcelColumn`,
you can enable it on this moment with option `ExcelWriteStrategy.EnumDropdown`.

<br><br>

## Limitation of reading rows

### reader:

```java
List<Product> products = Javaxcel.newInstance()
        .reader(workbook, Product.class)
        .options(new Limit(100))
        .read();
        
assert products.size() <= 100;
```

You can read the Excel file, but not all the rows. Just read with option `ExcelReadStrategy.Limit`.

<br><br>

## Parallel reading

### reader:

```java
List<Product> products = Javaxcel.newInstance()
        .reader(workbook, Product.class)
        .options(new Parallel())
        .read();
```

With option `ExcelReadStrategy.Parallel`, you can read the Excel file faster than without the option.

<br><br>

## Resolution of constructor and method

### reader:

##### Default constructor:

```java
class Item {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}
```

`ModelReader` will resolve the constructor `public Item()`.

##### Multiple constructors:

```java
class Item {
    private Long id;
    private String title;
    private LocalDateTime createdAt;

    @ExcelModelCreator
    private Item(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Item(String title) {
        this.id = new Random().nextLong();
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }
}
```

`ModelReader` will resolve the constructor `private Item(Long, String, LocalDateTime)` annotated with `@ExcelModelCreator`.

##### Method and constructor(s):

```java
class Item {
    private Long id;
    private String title;
    private LocalDateTime createdAt;

    private Item(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    @ExcelModelCreator
    public static Item from(String title) {
        return new Item(new Random().nextLong(), title, LocalDateTime.now());
    }
}
```

`ModelReader` will resolve the method `public static Item from(String)` annotated with `@ExcelModelCreator`.

##### Parameter rules:

```java
class Item {
    private String id;
    private String title;
    private LocalDateTime createdAt;

    public Item(String $title, LocalDateTime $createdAt) {
        this.id = String.valueOf(new Random().nextLong());
        this.title = $title;
        this.createdAt = $createdAt;
    }
}
```

If the class has multiple field types that are the same, resolver checks if field name and parameter name are the same.
If not same, resolver fails to do. You can fix it with this solution when you can't change the parameter name(s).

```java
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;

public Item(@FieldName("title") String $title, LocalDateTime $createdAt) {
    this.id = String.valueOf(new Random().nextLong());
    this.title = $title;
    this.createdAt = $createdAt;
}
```

Both names of parameter and field whose type is `LocalDateTime` are different,
but their type is unique, so resolver can handle the parameter.
The rule of method is also like this.

<br>

If you want to know more rules, see `AbstractExcelModelExecutableResolver`.

<br><br>

## Add handler for custom type

```java
class Item {
    private UUID uuid;
    private String name;
    private Item[] items;
}
```

`Item` is custom class and not basically supported type. You can add handler for the type.

<br>

```java
public class ItemTypeHandler extends AbstractExcelTypeHandler<Item> {

    public ItemTypeHandler() {
        super(Item.class);
    }

    @Override
    protected String writeInternal(Item value, Object... args) {
        // Convert Item to String.
    }

    @Override
    public Item read(String value, Object... args) {
        // Convert String to Item.
    }

}

/* ... */

ExcelTypeHandlerRegistry registry = new ExcelTypeHandlerRegistryImpl();
registry.add(Item.class, new ItemTypeHandler());

Javaxcel javaxcel = Javaxcel.newInstance(registry);
```

If you want to override the supported type by default,
just make a handler class for the type and add it to registry.  

<br><br>

## Support java.util.Map

### writer:

```java
List<Map<String, Object>> maps = new ArrayList<>();
maps.add(Map.of("serialNumber", 10010, "productName", "Alpha"));
maps.add(Map.of("serialNumber", 10011, "productName", "Beta"));

Javaxcel.newInstance()
        .writer(workbook)
        .write(out, maps);
```

The result is

| productName | serialNumber |
| ----------- | ------------ |
| Alpha       | 10010        |
| Beta        | 10011        |

The column order is not guaranteed except for `java.util.LinkedHashMap`.  There is option for this issue.

<br>

```java
List<String> keyOrders = Arrays.asList("serialNumber", "productName");

Javaxcel.newInstance()
        .writer(workbook)
        .options(new KeyNames(keyOrders))
        .write(out, maps);
```

The result is

| serialNumber | productName |
| ------------ | ----------- |
| 10010        | Alpha       |
| 10011        | Beta        |

If you rearrange the column order of `Map`, use option `ExcelWriteStrategy.KeyNames`.
If you want to rename the header names, use that option like this.

<br>

```java
List<String> keyOrders = Arrays.asList("serialNumber", "productName");
List<String> newKeyNames = Arrays.asList("SERIAL_NUMBER", "PRODUCT_NAME");

Javaxcel.newInstance()
        .writer(workbook)
        .options(new KeyNames(keyOrders, newKeyNames))
        .write(out, maps);
```

The result is

| SERIAL_NUMBER | PRODUCT_NAME |
| ------------- | ------------ |
| 10010         | Alpha        |
| 10011         | Beta         |

<br>

### reader:

```java
Javaxcel.newInstance()
        .reader(workbook)
        .read();
```

```json
[
  {
    "SERIAL_NUMBER": "10010",
    "PRODUCT_NAME": "Alpha"
  },
  {
    "SERIAL_NUMBER": "10011",
    "PRODUCT_NAME": "Beta"
  }
]
```

If you want to rename the keys of `Map`, use option `ExcelReadStrategy.KeyNames`.

<br>

```java
List<String> newKeyNames = Arrays.asList("serialNumber", "productName");

Javaxcel.newInstance()
        .reader(workbook)
        .options(new KeyNames(newKeyNames))
        .read();
```

The result is

```json
[
  {
    "serialNumber": "10010",
    "productName": "Alpha"
  },
  {
    "serialNumber": "10011",
    "productName": "Beta"
  }
]
```
