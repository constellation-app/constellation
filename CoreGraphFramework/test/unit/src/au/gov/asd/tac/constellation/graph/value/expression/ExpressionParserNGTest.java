/*
 * Copyright 2010-2021 Australian Signals Directorate
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
    
    public ExpressionParserNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method for EQUALS, of class ExpressionParser.
     */
    @Test
    public void testEqualsParse() {
        System.out.println("Testing parse of query strings on EQUALS");
        
        List<String> expressionStrings = new ArrayList<>(); 
        expressionStrings.add("Label == 'Vertex #0<Unknown>'");
        expressionStrings.add("Type == 'Communication'");
        expressionStrings.add("color == '#8b14f0'");
        expressionStrings.add("selected == 'True'");
        expressionStrings.add("Source == 'Manually Created'");
        expressionStrings.add("x == '1'");
        expressionStrings.add("x2 == '2'");
        expressionStrings.add("Label == Vertex #0<Unknown>'");
        expressionStrings.add("Label == 'Vertex #0<Unknown>");
        expressionStrings.add("Label == 'Vertex #0<Unknown>''");
        expressionStrings.add("Label == ''Vertex #0<Unknown>'");
        expressionStrings.add("'Label' == 'Vertex #0<Unknown>'");
        expressionStrings.add("'Label == 'Vertex #0<Unknown>'");
        expressionStrings.add("Label' == 'Vertex #0<Unknown>'");
        
        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Type", "Communication", Operator.EQUALS));
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.EQUALS));
        expectedExpressions.add(createExpression("selected", "True", Operator.EQUALS));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        expectedExpressions.add(createExpression("Label", "Vertex #0<Unknown>", Operator.EQUALS));
        
        
        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for(final String expression : expressionStrings){
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
        expectedExpressions.add(createExpression("color", "#8b14f0", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("selected", "True", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("Source", "Manually Created", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("x", "1", Operator.NOT_EQUALS));
        expectedExpressions.add(createExpression("x2", "2", Operator.NOT_EQUALS));
        
        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for(final String expression : expressionStrings){
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
        for(final String expression : expressionStrings){
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
        for(final String expression : expressionStrings){
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
        for(final String expression : expressionStrings){
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
        
        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.GREATER_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.GREATER_THAN));
        
        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for(final String expression : expressionStrings){
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
        
        List<SequenceExpression> expectedExpressions = new ArrayList<>();
        expectedExpressions.add(createExpression("x", "1", Operator.LESS_THAN));
        expectedExpressions.add(createExpression("x2", "2", Operator.LESS_THAN));
        
        int count = 0;
        SequenceExpression expExpression;
        SequenceExpression actualExpression;
        for(final String expression : expressionStrings){
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
}
