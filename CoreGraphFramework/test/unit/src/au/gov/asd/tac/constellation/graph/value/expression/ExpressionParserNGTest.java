/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value.expression;

import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.Operator;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class ExpressionParserNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        ExpressionParser.hideErrorPrompts(true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of parse method for EQUALS, of class ExpressionParser.
     */
    @Test
    public void testEqualsParse() {
        System.out.println("Testing parse of query strings on EQUALS");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label equals 'Vertex #0<Unknown>'");
        expressionStrings.add("Type equals 'Communication'");
        expressionStrings.add("Label == 'Vertex #0<Unknown>'");
        expressionStrings.add("Type == 'Communication'");
        expressionStrings.add("color == '#8b14f0'");
        expressionStrings.add("selected == 'True'");
        expressionStrings.add("Source == 'Manually Created'");
        expressionStrings.add("x == '1'");
        expressionStrings.add("x2 == '2'");
        expressionStrings.add("Label == Vertex #0<Unknown>'");// missing start quote - raises error
        expressionStrings.add("Label == 'Vertex #1<Unknown>");// missing end quote - raises error
        expressionStrings.add("Label == 'Vertex #2<Unknown>''");// extraneous quote - raises error
        expressionStrings.add("Label == ''Vertex #3<Unknown>'"); // extraneous quote - raises error
        expressionStrings.add("'Label' == 'Vertex #4<Unknown>'"); // works because first instance is parsed as a string not a variable
        expressionStrings.add("'Label == 'Vertex #5<Unknown>'"); // no end quote - raises error
        expressionStrings.add("Label' == 'Vertex #6<Unknown>'"); // no start quote - raises error

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.EQUALS));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.EQUALS));
        expectedExpressions.add(createExpression("selected", "True", Operator.EQUALS));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.EQUALS));
        expectedExpressions.add(null);
        expectedExpressions.add(null);
        expectedExpressions.add(null);
        expectedExpressions.add(null);

        SequenceExpression se = new SequenceExpression(null);
        se.addChild(new ExpressionParser.StringExpression(se, "Label".toCharArray(), "Label".length()));
        se.addChild(new ExpressionParser.OperatorExpression(se, Operator.EQUALS));
        se.addChild(new ExpressionParser.StringExpression(se, "Vertex #4<Unknown>".toCharArray(), "Vertex #4<Unknown>".length()));

        expectedExpressions.add(se);
        expectedExpressions.add(null);
        expectedExpressions.add(null);

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for NOT_EQUALS, of class ExpressionParser.
     */
    @Test
    public void testNotEqualsParse() {
        System.out.println("Testing parse of query strings on NOT_EQUALS");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label notequals 'Vertex #0<Unknown>'");
        expressionStrings.add("Type notequals 'Communication'");
        expressionStrings.add("Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0'");
        expressionStrings.add("selected != 'True'");
        expressionStrings.add("Source != 'Manually Created'");
        expressionStrings.add("x != '1'");
        expressionStrings.add("x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("selected", "True", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.NOT_EQUALS));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for ASSIGN, of class ExpressionParser.
     */
    @Test
    public void testAssignParse() {
        System.out.println("Testing parse of query strings on ASSIGN");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label assign 'Vertex #0<Unknown>'");
        expressionStrings.add("Type assign 'Communication'");
        expressionStrings.add("Label = 'Vertex #0<Unknown>'");
        expressionStrings.add("Type = 'Communication'");
        expressionStrings.add("color = '#8b14f0'");
        expressionStrings.add("selected = 'True'");
        expressionStrings.add("Source = 'Manually Created'");
        expressionStrings.add("x = '1'");
        expressionStrings.add("x2 = '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.ASSIGN));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.ASSIGN));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.ASSIGN));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.ASSIGN));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.ASSIGN));
        expectedExpressions.add(createExpression("selected", "True", Operator.ASSIGN));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.ASSIGN));
        expectedExpressions.add(createExpression("x", "1", Operator.ASSIGN));
        expectedExpressions.add(createExpression("x2", "2", Operator.ASSIGN));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for DIVIDE, of class ExpressionParser.
     */
    @Test
    public void testDivideParse() {
        System.out.println("Testing parse of query strings on DIVIDE");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label divide 'Vertex #0<Unknown>'");
        expressionStrings.add("Type divide 'Communication'");
        expressionStrings.add("Label / 'Vertex #0<Unknown>'");
        expressionStrings.add("Type / 'Communication'");
        expressionStrings.add("Label / 'Vertex #0<Unknown>'");
        expressionStrings.add("Type / 'Communication'");
        expressionStrings.add("color / '#8b14f0'");
        expressionStrings.add("selected / 'True'");
        expressionStrings.add("Source / 'Manually Created'");
        expressionStrings.add("x / '1'");
        expressionStrings.add("x2 / '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.DIVIDE));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.DIVIDE));
        expectedExpressions.add(createExpression("selected", "True", Operator.DIVIDE));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.DIVIDE));
        expectedExpressions.add(createExpression("x", "1", Operator.DIVIDE));
        expectedExpressions.add(createExpression("x2", "2", Operator.DIVIDE));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for MULTIPLY, of class ExpressionParser.
     */
    @Test
    public void testMultiplyParse() {
        System.out.println("Testing parse of query strings on MULTIPLY");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label multiply 'Vertex #0<Unknown>'");
        expressionStrings.add("Type multiply 'Communication'");
        expressionStrings.add("Label * 'Vertex #0<Unknown>'");
        expressionStrings.add("Type * 'Communication'");
        expressionStrings.add("Label * 'Vertex #0<Unknown>'");
        expressionStrings.add("Type * 'Communication'");
        expressionStrings.add("color * '#8b14f0'");
        expressionStrings.add("selected * 'True'");
        expressionStrings.add("Source * 'Manually Created'");
        expressionStrings.add("x * '1'");
        expressionStrings.add("x2 * '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("selected", "True", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("x", "1", Operator.MULTIPLY));
        expectedExpressions.add(createExpression("x2", "2", Operator.MULTIPLY));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for ADD, of class ExpressionParser.
     */
    @Test
    public void testAddParse() {
        System.out.println("Testing parse of query strings on ADD");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label add 'Vertex #0<Unknown>'");
        expressionStrings.add("Type add 'Communication'");
        expressionStrings.add("Label + 'Vertex #0<Unknown>'");
        expressionStrings.add("Type + 'Communication'");
        expressionStrings.add("Label + 'Vertex #0<Unknown>'");
        expressionStrings.add("Type + 'Communication'");
        expressionStrings.add("color + '#8b14f0'");
        expressionStrings.add("selected + 'True'");
        expressionStrings.add("Source + 'Manually Created'");
        expressionStrings.add("x + '1'");
        expressionStrings.add("x2 + '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.ADD));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.ADD));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.ADD));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.ADD));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.ADD));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.ADD));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.ADD));
        expectedExpressions.add(createExpression("selected", "True", Operator.ADD));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.ADD));
        expectedExpressions.add(createExpression("x", "1", Operator.ADD));
        expectedExpressions.add(createExpression("x2", "2", Operator.ADD));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for SUBTRACT, of class ExpressionParser.
     */
    @Test
    public void testSubtractParse() {
        System.out.println("Testing parse of query strings on SUBTRACT");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label subtract 'Vertex #0<Unknown>'");
        expressionStrings.add("Type subtract 'Communication'");
        expressionStrings.add("Label - 'Vertex #0<Unknown>'");
        expressionStrings.add("Type - 'Communication'");
        expressionStrings.add("Label - 'Vertex #0<Unknown>'");
        expressionStrings.add("Type - 'Communication'");
        expressionStrings.add("color - '#8b14f0'");
        expressionStrings.add("selected - 'True'");
        expressionStrings.add("Source - 'Manually Created'");
        expressionStrings.add("x - '1'");
        expressionStrings.add("x2 - '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("selected", "True", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("x", "1", Operator.SUBTRACT));
        expectedExpressions.add(createExpression("x2", "2", Operator.SUBTRACT));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for MODULO, of class ExpressionParser.
     */
    @Test
    public void testModuloParse() {
        System.out.println("Testing parse of query strings on MODULO");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label mod 'Vertex #0<Unknown>'");
        expressionStrings.add("Type mod 'Communication'");
        expressionStrings.add("Label % 'Vertex #0<Unknown>'");
        expressionStrings.add("Type % 'Communication'");
        expressionStrings.add("Label % 'Vertex #0<Unknown>'");
        expressionStrings.add("Type % 'Communication'");
        expressionStrings.add("color % '#8b14f0'");
        expressionStrings.add("selected % 'True'");
        expressionStrings.add("Source % 'Manually Created'");
        expressionStrings.add("x % '1'");
        expressionStrings.add("x2 % '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MODULO));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MODULO));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MODULO));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MODULO));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.MODULO));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.MODULO));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.MODULO));
        expectedExpressions.add(createExpression("selected", "True", Operator.MODULO));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.MODULO));
        expectedExpressions.add(createExpression("x", "1", Operator.MODULO));
        expectedExpressions.add(createExpression("x2", "2", Operator.MODULO));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for contains, of class ExpressionParser.
     */
    @Test
    public void testContainsParse() {
        System.out.println("Testing parse of query strings on contains");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label contains 'Vertex #0'");
        expressionStrings.add("Type contains 'Commun'");
        expressionStrings.add("color contains '#8b'");
        expressionStrings.add("selected contains 'T'");
        expressionStrings.add("Source contains 'Manually '");
        expressionStrings.add("x contains '1'");
        expressionStrings.add("x2 contains '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0", Operator.CONTAINS));
        expectedExpressions.add(createExpression("Type", "Commun", Operator.CONTAINS));
        expectedExpressions.add(createExpression("color", "#8b", Operator.CONTAINS));
        expectedExpressions.add(createExpression("selected", "T", Operator.CONTAINS));
        expectedExpressions.add(createExpression("Source", "Manually ", Operator.CONTAINS));
        expectedExpressions.add(createExpression("x", "1", Operator.CONTAINS));
        expectedExpressions.add(createExpression("x2", "2", Operator.CONTAINS));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for ENDS_WITH, of class ExpressionParser.
     */
    @Test
    public void testEndsWithParse() {
        System.out.println("Testing parse of query strings on ENDS_WITH");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label endswith '>'");
        expressionStrings.add("Type endswith 'ion'");
        expressionStrings.add("color endswith 'f0'");
        expressionStrings.add("selected endswith 'ue'");
        expressionStrings.add("Source endswith 'Created'");
        expressionStrings.add("x endswith '1'");
        expressionStrings.add("x2 endswith '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", ">", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("Type", "ion", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("color", "f0", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("selected", "ue", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("Source", "Created", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("x", "1", Operator.ENDS_WITH));
        expectedExpressions.add(createExpression("x2", "2", Operator.ENDS_WITH));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for STARTS_WITH, of class ExpressionParser.
     */
    @Test
    public void testStartsWithParse() {
        System.out.println("Testing parse of query strings on STARTS_WITH");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label startswith 'Vert'");
        expressionStrings.add("Type startswith 'Commu'");
        expressionStrings.add("color startswith '#'");
        expressionStrings.add("selected startswith 'T'");
        expressionStrings.add("Source startswith 'Manu'");
        expressionStrings.add("x startswith '1'");
        expressionStrings.add("x2 startswith '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vert", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("Type", "Commu", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("color", "#", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("selected", "T", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("Source", "Manu", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("x", "1", Operator.STARTS_WITH));
        expectedExpressions.add(createExpression("x2", "2", Operator.STARTS_WITH));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for GREATER_THAN, of class ExpressionParser.
     */
    @Test
    public void testGreaterThanParse() {
        System.out.println("Testing parse of query strings on GREATER_THAN");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("x > '1'");
        expressionStrings.add("x2 > '2'");
        expressionStrings.add("x gt '1'");
        expressionStrings.add("x2 gt '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.GREATER_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.GREATER_THAN));
        expectedExpressions.add(createExpression("x", "1", Operator.GREATER_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.GREATER_THAN));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for GREATER_THAN_OR_EQUALS, of class
     * ExpressionParser.
     */
    @Test
    public void testGreaterThanEqualParse() {
        System.out.println("Testing parse of query strings on GREATER_THAN_OR_EQUALS");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("x >= '1'");
        expressionStrings.add("x2 >= '2'");
        expressionStrings.add("x gteq '1'");
        expressionStrings.add("x2 gteq '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.GREATER_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.GREATER_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.GREATER_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.GREATER_THAN_OR_EQUALS));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for LESS_THAN, of class ExpressionParser.
     */
    @Test
    public void testLessThanParse() {
        System.out.println("Testing parse of query strings on LESS_THAN");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("x < '1'");
        expressionStrings.add("x2 < '2'");
        expressionStrings.add("x lt '1'");
        expressionStrings.add("x2 lt '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.LESS_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.LESS_THAN));
        expectedExpressions.add(createExpression("x", "1", Operator.LESS_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.LESS_THAN));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for LESS_THAN_OR_EQUALS, of class ExpressionParser.
     */
    @Test
    public void testLessThanEqualParse() {
        System.out.println("Testing parse of query strings on LESS_THAN_OR_EQUALS");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("x <= '1'");
        expressionStrings.add("x2 <= '2'");
        expressionStrings.add("x lteq '1'");
        expressionStrings.add("x2 lteq '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.LESS_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.LESS_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.LESS_THAN_OR_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.LESS_THAN_OR_EQUALS));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for AND, of class ExpressionParser.
     */
    @Test
    public void testAndParse() {
        System.out.println("Testing parse of query strings on AND");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label != 'Vertex #0<Unknown>' and Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' and Type != 'Communication'");
        expressionStrings.add("Label != 'Vertex #0<Unknown>' && Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' && Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0' && color != '#8b14f0'");
        expressionStrings.add("selected != 'True' && selected != 'True'");
        expressionStrings.add("Source != 'Manually Created' && Source != 'Manually Created'");
        expressionStrings.add("x != '1' && x != '1'");
        expressionStrings.add("x2 != '2' && x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("color", "#8b14f0", Operator.NOT_EQUALS, "color", "#8b14f0", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("selected", "True", Operator.NOT_EQUALS, "selected", "True", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("Source", "Manually Created", Operator.NOT_EQUALS, "Source", "Manually Created", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("x", "1", Operator.NOT_EQUALS, "x", "1", Operator.NOT_EQUALS, Operator.AND_AND));
        expectedExpressions.add(createNestedExpression("x2", "2", Operator.NOT_EQUALS, "x2", "2", Operator.NOT_EQUALS, Operator.AND_AND));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for BITWISE_AND, of class ExpressionParser.
     */
    @Test
    public void testBitwiseAndParse() {
        System.out.println("Testing parse of query strings on BITWISE_AND");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label != 'Vertex #0<Unknown>' & Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' & Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0' & color != '#8b14f0'");
        expressionStrings.add("selected != 'True' & selected != 'True'");
        expressionStrings.add("Source != 'Manually Created' & Source != 'Manually Created'");
        expressionStrings.add("x != '1' & x != '1'");
        expressionStrings.add("x2 != '2' & x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("color", "#8b14f0", Operator.NOT_EQUALS, "color", "#8b14f0", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("selected", "True", Operator.NOT_EQUALS, "selected", "True", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("Source", "Manually Created", Operator.NOT_EQUALS, "Source", "Manually Created", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("x", "1", Operator.NOT_EQUALS, "x", "1", Operator.NOT_EQUALS, Operator.AND));
        expectedExpressions.add(createNestedExpression("x2", "2", Operator.NOT_EQUALS, "x2", "2", Operator.NOT_EQUALS, Operator.AND));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for OR_OR, of class ExpressionParser.
     */
    @Test
    public void testOrParse() {
        System.out.println("Testing parse of query strings on OR_OR");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label != 'Vertex #0<Unknown>' or Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' or Type != 'Communication'");
        expressionStrings.add("Label != 'Vertex #0<Unknown>' || Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' || Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0' || color != '#8b14f0'");
        expressionStrings.add("selected != 'True' || selected != 'True'");
        expressionStrings.add("Source != 'Manually Created' || Source != 'Manually Created'");
        expressionStrings.add("x != '1' || x != '1'");
        expressionStrings.add("x2 != '2' || x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("color", "#8b14f0", Operator.NOT_EQUALS, "color", "#8b14f0", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("selected", "True", Operator.NOT_EQUALS, "selected", "True", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("Source", "Manually Created", Operator.NOT_EQUALS, "Source", "Manually Created", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("x", "1", Operator.NOT_EQUALS, "x", "1", Operator.NOT_EQUALS, Operator.OR_OR));
        expectedExpressions.add(createNestedExpression("x2", "2", Operator.NOT_EQUALS, "x2", "2", Operator.NOT_EQUALS, Operator.OR_OR));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for BITWISE_OR, of class ExpressionParser.
     */
    @Test
    public void testBitwiseOrParse() {
        System.out.println("Testing parse of query strings on BITWISE_OR");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label != 'Vertex #0<Unknown>' | Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' | Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0' | color != '#8b14f0'");
        expressionStrings.add("selected != 'True' | selected != 'True'");
        expressionStrings.add("Source != 'Manually Created' | Source != 'Manually Created'");
        expressionStrings.add("x != '1' | x != '1'");
        expressionStrings.add("x2 != '2' | x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("color", "#8b14f0", Operator.NOT_EQUALS, "color", "#8b14f0", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("selected", "True", Operator.NOT_EQUALS, "selected", "True", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("Source", "Manually Created", Operator.NOT_EQUALS, "Source", "Manually Created", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("x", "1", Operator.NOT_EQUALS, "x", "1", Operator.NOT_EQUALS, Operator.OR));
        expectedExpressions.add(createNestedExpression("x2", "2", Operator.NOT_EQUALS, "x2", "2", Operator.NOT_EQUALS, Operator.OR));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for XOR, of class ExpressionParser.
     */
    @Test
    public void testXorParse() {
        System.out.println("Testing parse of query strings on XOR");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("Label != 'Vertex #0<Unknown>' xor Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' xor Type != 'Communication'");
        expressionStrings.add("Label != 'Vertex #0<Unknown>' ^ Label != 'Vertex #0<Unknown>'");
        expressionStrings.add("Type != 'Communication' ^ Type != 'Communication'");
        expressionStrings.add("color != '#8b14f0' ^ color != '#8b14f0'");
        expressionStrings.add("selected != 'True' ^ selected != 'True'");
        expressionStrings.add("Source != 'Manually Created' ^ Source != 'Manually Created'");
        expressionStrings.add("x != '1' ^ x != '1'");
        expressionStrings.add("x2 != '2' ^ x2 != '2'");

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, "Label", "Vertex #0<Unknown>", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("Type", "Communication", Operator.NOT_EQUALS, "Type", "Communication", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("color", "#8b14f0", Operator.NOT_EQUALS, "color", "#8b14f0", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("selected", "True", Operator.NOT_EQUALS, "selected", "True", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("Source", "Manually Created", Operator.NOT_EQUALS, "Source", "Manually Created", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("x", "1", Operator.NOT_EQUALS, "x", "1", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));
        expectedExpressions.add(createNestedExpression("x2", "2", Operator.NOT_EQUALS, "x2", "2", Operator.NOT_EQUALS, Operator.EXCLUSIVE_OR));

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    /**
     * Test of parse method for restricted characters, of class
     * ExpressionParser.
     */
    @Test
    public void testRestrictedCharactersParse() {
        System.out.println("Testing parse of query strings with restricted characters");

        List<String> expressionStrings = new ArrayList<>();
        expressionStrings.add("background_icon != 'Vertex #0<Unknown>'");
        expressionStrings.add("{Type} != 'Communication'"); // curly braces are allowed - does not have to be a complete set.
        expressionStrings.add("(color) != '#8b14f0'"); // parenthesis not interpreted as part of attribute
        expressionStrings.add("sele'cted != 'True'"); // quote restricted within term
        expressionStrings.add("Sour\"ce != 'Manually Created'"); // double quote restricted within term

        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("background_icon", "Vertex #0<Unknown>", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("{Type}", "Communication", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.NOT_EQUALS));
        expectedExpressions.add(null);
        expectedExpressions.add(null);

        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for (final String expression : expressionStrings) {
            expExpression = expectedExpressions.get(count);
            actualExpression = ExpressionParser.parse(expression);
            assertEquals(actualExpression, expExpression);
            count++;
        }
    }

    private SequenceExpression createExpression(final String left, final String right, final Operator operator) {
        SequenceExpression se = new SequenceExpression(null);
        se.addChild(new ExpressionParser.VariableExpression(se, left.toCharArray(), left.length()));
        se.addChild(new ExpressionParser.OperatorExpression(se, operator));
        se.addChild(new ExpressionParser.StringExpression(se, right.toCharArray(), right.length()));
        return se;
    }

    /**
     * Creates a nested expression of one level.
     *
     * @param left1
     * @param right1
     * @param operator1
     * @param left2
     * @param right2
     * @param operator2
     * @param middle
     * @return
     */
    private SequenceExpression createNestedExpression(final String left1, final String right1, final Operator operator1, final String left2, final String right2, final Operator operator2, final Operator middle) {
        // The base expression to add all children to.
        SequenceExpression root = new SequenceExpression(null);

        // The inner left expression to add all arguments to.
        SequenceExpression se1 = new SequenceExpression(root);
        se1.addChild(new ExpressionParser.VariableExpression(root, left1.toCharArray(), left1.length()));
        se1.addChild(new ExpressionParser.OperatorExpression(root, operator1));
        se1.addChild(new ExpressionParser.StringExpression(root, right1.toCharArray(), right1.length()));

        // The inner right expression to add all arguments to.
        SequenceExpression se2 = new SequenceExpression(root);
        se2.addChild(new ExpressionParser.VariableExpression(root, left2.toCharArray(), left2.length()));
        se2.addChild(new ExpressionParser.OperatorExpression(root, operator2));
        se2.addChild(new ExpressionParser.StringExpression(root, right2.toCharArray(), right2.length()));

        // The outer left argument to add to the base expression
        root.addChild(se1);

        // The operator to use in the base expression
        root.addChild(new ExpressionParser.OperatorExpression(root, middle));

        // The outer right argument to add to the base expression
        root.addChild(se2);

        return root;
    }
}
