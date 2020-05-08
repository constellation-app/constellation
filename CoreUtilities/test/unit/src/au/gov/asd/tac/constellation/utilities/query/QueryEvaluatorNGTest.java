/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test class for QueryEvaluator.java
 *
 * @author aldebaran30701
 */
public class QueryEvaluatorNGTest {

    static String SEPARATOR = "";

    // test conversion of single attribute
    @Test
    public void querySingleAttribute() {

        // Test single attribute
        List<String> results = new ArrayList();
        String result1 = "color == #ffffff";
        results.add(result1);
        String infix = "color == #ffffff";

        assertEquals(QueryEvaluator.tokeniser(infix).get(0), result1);
        assertEquals(QueryEvaluator.tokeniser(infix).size(), results.size());
        results.clear();

        // Test with brackets (part of attribute text)
        result1 = "(color == #ffffff)";
        results.add(result1);
        infix = "\\(color == #ffffff\\)";

        assertEquals(QueryEvaluator.tokeniser(infix).get(0), result1);
        assertEquals(QueryEvaluator.tokeniser(infix).size(), results.size());
        results.clear();

        // Test with brackets () used as precedence
        result1 = "color == #ffffff";
        results.add(result1);

        infix = "( color == #ffffff )";
        List<String> returning = QueryEvaluator.tokeniser(infix);
        assertEquals(returning.get(0), result1);
        assertEquals(returning.size(), results.size());
    }

    @Test
    public void queryTwoAttributes() {
        // Test multiple attributes ||
        List<String> resultArray = new ArrayList();
        List<String> results = new ArrayList();
        String result1 = "dim == True";
        String result2 = "color == #ffffff";
        String result3 = "||";
        results.add(result1);
        results.add(result2);
        results.add(result3);

        String infix = "dim == True" + SEPARATOR + "||" + SEPARATOR + "color == #ffffff";
        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();

        // Test multiple attributes &&
        result1 = "color == #ffffff";
        result2 = "dim == True";
        result3 = "&&";
        results.add(result1);
        results.add(result2);
        results.add(result3);

        infix = "color == #ffffff" + SEPARATOR + "&&" + SEPARATOR + "dim == True";
        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();

        // Test multiple attributes && in multiple () parenthesis
        result1 = "dim == True";
        result2 = "color == #ffffff";
        result3 = "&&";
        results.add(result1);
        results.add(result2);
        results.add(result3);

        infix = "(" + SEPARATOR + "dim == True" + SEPARATOR + ")"
                + SEPARATOR + "&&" + SEPARATOR + "("
                + SEPARATOR + "color == #ffffff" + SEPARATOR + ")";

        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();

        // Test multiple attributes && in single () parenthesis
        result1 = "dim == True";
        result2 = "color == #ffffff";
        result3 = "&&";
        results.add(result1);
        results.add(result2);
        results.add(result3);

        infix = "(" + SEPARATOR + "dim == True" + SEPARATOR + "&&"
                + SEPARATOR + "color == #ffffff" + SEPARATOR + ")";

        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();
    }

    @Test
    public void queryThreeNestedAttributes() {
        List<String> resultArray = new ArrayList();
        List<String> results = new ArrayList();
        String result1 = "dim == True";
        String result2 = "color == #ffffff";
        String result3 = "||";
        String result4 = "Visibility > 0";
        String result5 = "&&";
        results.add(result1);
        results.add(result2);
        results.add(result3);
        results.add(result4);
        results.add(result5);

        String infix = "(" + SEPARATOR + "dim == True" + SEPARATOR + "||"
                + SEPARATOR + "color == #ffffff" + SEPARATOR + ")"
                + SEPARATOR + "&&" + SEPARATOR + "Visibility > 0";

        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.get(3), result4);
        assertEquals(resultArray.get(4), result5);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();
    }

    @Test
    public void queryMultipleNestedAttributes() {
        List<String> resultArray = new ArrayList();
        List<String> results = new ArrayList();
        String result1 = "1";
        String result2 = "2";
        String result3 = "&&";
        String result4 = "3";
        String result5 = "4";
        String result6 = "&&";
        String result7 = "||";
        String result8 = "6";
        String result9 = "||";
        results.add(result1);
        results.add(result2);
        results.add(result3);
        results.add(result4);
        results.add(result5);
        results.add(result6);
        results.add(result7);
        results.add(result8);
        results.add(result9);

        String infix = "(" + SEPARATOR + "1" + SEPARATOR + "&&" + SEPARATOR + "2"
                + SEPARATOR + "||" + SEPARATOR + "3" + SEPARATOR + "&&"
                + SEPARATOR + "4" + SEPARATOR + ")" + SEPARATOR + "||"
                + SEPARATOR + "6";

        resultArray = QueryEvaluator.tokeniser(infix);
        assertEquals(resultArray.get(0), result1);
        assertEquals(resultArray.get(1), result2);
        assertEquals(resultArray.get(2), result3);
        assertEquals(resultArray.get(3), result4);
        assertEquals(resultArray.get(4), result5);
        assertEquals(resultArray.get(5), result6);
        assertEquals(resultArray.get(6), result7);
        assertEquals(resultArray.get(7), result8);
        assertEquals(resultArray.get(8), result9);
        assertEquals(resultArray.size(), results.size());
        results.clear();
        resultArray.clear();
    }

    @Test
    public void noInputTest() {
        assertEquals(QueryEvaluator.tokeniser(""), Collections.EMPTY_LIST);
        assertEquals(QueryEvaluator.tokeniser(null), Collections.EMPTY_LIST);
    }

    @Test
    public void evaluatePostfixTest() {
        // || case
        String postfix = "true false ||";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "false true ||";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "true true ||";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "false false ||";
        assertFalse(QueryEvaluator.evaluatePostfix(postfix));

        // &&  case
        postfix = "true false &&";
        assertFalse(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "false true &&";
        assertFalse(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "true true &&";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "false false &&";
        assertFalse(QueryEvaluator.evaluatePostfix(postfix));

        // multiple cases
        postfix = "true false && true ||";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "true false && true &&";
        assertFalse(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "true false || true ||";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
        postfix = "true false || true &&";
        assertTrue(QueryEvaluator.evaluatePostfix(postfix));
    }
}
