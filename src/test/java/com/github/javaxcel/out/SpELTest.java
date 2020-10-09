package com.github.javaxcel.out;

import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import io.github.imsejin.util.StringUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpELTest {

    private static String convert(IntStream stream) {
        return stream.mapToObj(String::valueOf)
                .collect(() -> new StringJoiner(","), StringJoiner::add, (s1, s2) -> { }).toString();
    }

    @Test
    @DisplayName("SpEL: List -> String")
    public void stringifiedList() {
        // given
        String exp = "T(java.util.Arrays).asList('Hello', \"world!\").toString()";

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(exp);

        // then
        assertThat(expression.getValue(String.class), is("[Hello, world!]"));
    }

    @Test
    @DisplayName("SpEL: Product#apiId = Product#name")
    public void setApiIdWithName() {
        // given
        Product product = new Product();
        product.setName("Mint Chocolate");

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression("apiId = name.toUpperCase()");

        // then
        assertThat(expression.getValue(product, String.class), is("MINT CHOCOLATE"));
        assertThat(product.getApiId(), is("MINT CHOCOLATE"));
    }

    @Test
    @DisplayName("SpEL: Product#apiId = UUID#randomUUID()#toString()")
    public void setValueIntoProperty() {
        // given
        Product product = new Product();
        product.setApiId(UUID.randomUUID().toString());

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        String uuid = UUID.randomUUID().toString();
        parser.parseExpression("apiId").setValue(product, uuid);

        // then
        assertThat(product.getApiId(), is(uuid));
    }

    @Test
    public void expressionThatHasVariable() {
        // given
        Product product = new Product();
        product.setName("Caramel macchiato");

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(product);
        context.setVariable("name", "Cafe mocha");
        String value = parser.parseExpression("name = #name").getValue(context, String.class);

        // then
        assertThat(product.getName(), is("Cafe mocha"));
        assertEquals(product.getName(), value);
    }

    @Test
    public void inputValueToTemplateAndParseExpression() {
        // given
        Product product = new Product();
        product.setName("Milk Tea");
        String exp = "'#{name}'.replace(' ', '_').toLowerCase()";

        // when #1
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(exp, ParserContext.TEMPLATE_EXPRESSION);
        String convertedTemplate = expression.getValue(product, String.class);

        // then #1
        assertEquals(convertedTemplate, "'Milk Tea'.replace(' ', '_').toLowerCase()");

        // when #2
        Expression expression1 = parser.parseExpression(convertedTemplate);
        String value = expression1.getValue(product, String.class);

        // then #2
        assertEquals(value, "milk_tea");
    }

    @Test
    public void parseVariables() {
        // given
        List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17);
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("primes", primes);
        }};

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(map);
        List<Integer> list = parser.parseExpression("#primes.?[#this > 7]").getValue(context, List.class);

        // then
        list.forEach(System.out::println);
    }

    @Test
    public void parseVariable() {
        // given
        EducationToy toy = new EducationToy();
        toy.setTargetAges(new int[]{2, 3, 4, 5, 6});
        String fieldName = "targetAges";
        String exp = "T(java.util.Arrays).stream(#" + fieldName + ").boxed().collect(T(java.util.stream.Collectors).toList()).toString()";

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext(toy);
        context.setVariable(fieldName, toy.getTargetAges());
        String value = StringUtils.ifNullOrEmpty(
                (String) parser.parseExpression(exp).getValue(context), "");

        // then
        assertEquals(value, "[2, 3, 4, 5, 6]");
    }

    @Test
    @SneakyThrows
    public void parseVaribleWithMethod() {
        // given
        EducationToy toy = new EducationToy();
        toy.setTargetAges(new int[]{2, 3, 4, 5, 6});
        String fieldName = "targetAges";
        String converterName = "convert";
        String exp = "#" + converterName + "(T(java.util.Arrays).stream(#" + fieldName + "))";

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext(toy);
        context.registerFunction(converterName, SpELTest.class.getDeclaredMethod(converterName, IntStream.class));
        context.setVariable(fieldName, toy.getTargetAges());
        String value = StringUtils.ifNullOrEmpty(
                (String) parser.parseExpression(exp).getValue(context), "");

        // then
        assertEquals(value, "2,3,4,5,6");
    }

    @Test
    public void parseToRealModel() {
        // given
        Map<String, Object> map = new HashMap<>();
        String key = "agesFromBirthToPuberty";
        map.put(key, "2, 3, 4, 5, 6");

        String exp = "T(com.github.javaxcel.Converter).toIntArray(#" + key + ".split(', '))";

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(map);

        int[] ints = parser.parseExpression(exp).getValue(context, int[].class);

        // then
        assertArrayEquals(ints, new int[]{2, 3, 4, 5, 6});
    }

    @Test
    public void parseConstructor() {
        // given
        BigInteger bigInt = new BigInteger("123456789");

//        String exp = "T(java.math.BigInteger).valueOf('123456789')";
        String exp = "new java.math.BigInteger('123456789')";

        // when
        SpelExpressionParser parser = new SpelExpressionParser();
        BigInteger bigInteger = parser.parseExpression(exp).getValue(BigInteger.class);

        // then
        assertEquals(bigInt, bigInteger);
    }

}
