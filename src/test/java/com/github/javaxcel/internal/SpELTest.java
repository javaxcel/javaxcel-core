/*
 * Copyright 2021 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.internal;

import com.github.javaxcel.model.product.Product;
import com.github.javaxcel.model.toy.EducationToy;
import io.github.imsejin.common.util.StringUtils;
import io.github.imsejin.expression.Expression;
import io.github.imsejin.expression.ExpressionParser;
import io.github.imsejin.expression.ParserContext;
import io.github.imsejin.expression.spel.standard.SpelExpressionParser;
import io.github.imsejin.expression.spel.support.StandardEvaluationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SpELTest {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private static String convert(IntStream stream) {
        return stream.mapToObj(String::valueOf)
                .collect(() -> new StringJoiner(","), StringJoiner::add, StringJoiner::merge).toString();
    }

    @Test
    @DisplayName("SpEL: List -> String")
    void stringifiedList() {
        // given
        String exp = "T(java.util.Arrays).asList('Hello', \"world!\").toString()";

        // when
        Expression expression = parser.parseExpression(exp);

        // then
        assertThat(expression.getValue(String.class))
                .as("Expression result is equal to stringified array")
                .isEqualTo(Arrays.asList("Hello", "world!").toString());
    }

    @Test
    @DisplayName("SpEL: Product#name = Product#apiId")
    void setApiIdWithName() {
        // given
        UUID uuid = UUID.randomUUID();
        Product product = new Product();
        product.setApiId(uuid);

        // when
        Expression expression = parser.parseExpression("name = apiId.toString().toUpperCase()");
        String actual = expression.getValue(product, String.class);

        // then
        assertThat(product.getName())
                .as("Product's name is change into the capitalized")
                .isEqualTo(uuid.toString().toUpperCase());
        assertThat(actual)
                .as("Expression result is equal to product's name")
                .isEqualTo(product.getName());
    }

    @Test
    @DisplayName("SpEL: Product#apiId = UUID#randomUUID()")
    void setValueIntoProperty() {
        // given
        UUID initialUuid = UUID.randomUUID();
        Product product = new Product();
        product.setApiId(initialUuid);

        // when
        UUID newUuid = UUID.randomUUID();
        parser.parseExpression("apiId").setValue(product, newUuid);

        // then
        assertThat(product.getApiId())
                .as("Initial api id is replaced")
                .isNotEqualTo(initialUuid);
        assertThat(product.getApiId())
                .as("Product's api id is replaced with new api id")
                .isEqualTo(newUuid);
    }

    @Test
    @DisplayName("SpEL: Product#name = #variable")
    void expressionThatHasVariable() {
        // given
        String initialName = "Caramel macchiato";
        Product product = new Product();
        product.setName(initialName);

        // when
        String newName = "Cafe mocha";
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(product);
        context.setVariable("name", newName);
        String actual = parser.parseExpression("name = #name").getValue(context, String.class);

        // then
        assertThat(product.getName())
                .as("Initial name is replaced")
                .isNotEqualTo(initialName);
        assertThat(product.getName())
                .as("Product's name is replaced with new name")
                .isEqualTo(newName);
        assertThat(actual)
                .as("Expression result is equal to product's name")
                .isEqualTo(product.getName());
    }

    @Test
    @DisplayName("SpEL: template expression #{fieldName}")
    void inputValueToTemplateAndParseExpression() {
        // given
        Product product = new Product();
        product.setName("Milk Tea");
        String exp = "'#{name}'.replace(' ', '_').toLowerCase()";

        // when
        String convertedTemplate = parser.parseExpression(exp, ParserContext.TEMPLATE_EXPRESSION)
                .getValue(product, String.class);
        String expressionResult = parser.parseExpression(convertedTemplate)
                .getValue(product, String.class);

        // then
        assertThat(convertedTemplate)
                .as("#{fieldName} is converted into field value")
                .isEqualTo(exp.replaceAll("#\\{.+}", product.getName()));
        assertThat(expressionResult)
                .as("Executes an expression as converted template")
                .isEqualTo("milk_tea");
    }

    @Test
    @DisplayName("SpEL: only variable without root object")
    void parseVariable() {
        // given
        EducationToy toy = new EducationToy();
        toy.setTargetAges(new int[][]{{2, 3, 4, 5, 6}});
        String fieldName = "targetAges";
        String exp = String.format("T(java.util.Arrays).stream(#%s[0])" +
                ".boxed()" +
                ".collect(T(java.util.stream.Collectors).toList())" +
                ".toString()" +
                ".replaceAll('[\\[\\]]', '')", fieldName);

        // when
        StandardEvaluationContext context = new StandardEvaluationContext(toy);
        context.setVariable(fieldName, toy.getTargetAges());
        String expResult = parser.parseExpression(exp).getValue(context, String.class);

        // then
        assertThat(expResult)
                .as("Stringified int array")
                .isEqualTo("2, 3, 4, 5, 6");
    }

    @Test
    @DisplayName("SpEL: only variables without root object")
    @SuppressWarnings("unchecked")
    void parseVariables() {
        // given
        List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17);
        Map<String, Object> map = Collections.singletonMap("primes", primes);

        // when
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(map);
        List<Integer> list = parser.parseExpression("#primes.?[#this > 7]")
                .getValue(context, List.class);

        // then
        list.forEach(it -> assertThat(it)
                .as("Elements in list are greater than 7")
                .isGreaterThan(7));
    }

    /**
     * @see #convert(IntStream)
     */
    @Test
    void parseVariableWithMethod() throws NoSuchMethodException {
        // given
        EducationToy toy = new EducationToy();
        toy.setTargetAges(new int[][]{{2, 3, 4, 5, 6}});
        String fieldName = "targetAges";
        String converterName = "convert";
        String exp = "#" + converterName + "(T(java.util.Arrays).stream(#" + fieldName + "[0]))";

        // when
        StandardEvaluationContext context = new StandardEvaluationContext(toy);
        context.registerFunction(converterName, getClass().getDeclaredMethod(converterName, IntStream.class));
        context.setVariable(fieldName, toy.getTargetAges());
        String value = StringUtils.ifNullOrEmpty(
                (String) parser.parseExpression(exp).getValue(context), "");

        // then
        assertThat("2,3,4,5,6").isEqualTo(value);
    }

    @Test
    void parseToRealModel() {
        // given
        Map<String, Object> map = new HashMap<>();
        String key = "agesFromBirthToPuberty";
        map.put(key, "2, 3, 4, 5, 6");

        String exp = "T(com.github.javaxcel.Converter).toIntArray(#" + key + ".split(', '))";

        // when
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(map);

        int[] ints = parser.parseExpression(exp).getValue(context, int[].class);

        // then
        assertThat(ints).containsExactly(2, 3, 4, 5, 6);
    }

    @Test
    @DisplayName("SpEL: write a constructor in expression")
    void parseConstructor() {
        // given
        BigInteger bigInt = new BigInteger("123456789");
        String exp = "new java.math.BigInteger('123456789')";

        // when
        BigInteger expResult = parser.parseExpression(exp).getValue(BigInteger.class);

        // then
        assertThat(expResult).isEqualTo(bigInt);
    }

}
