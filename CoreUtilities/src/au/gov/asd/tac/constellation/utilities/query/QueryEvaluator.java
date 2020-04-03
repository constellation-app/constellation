/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * Evaluates a query expression made up of a chain of operations of the form (a
 * op1 b op2 c ...).
 * <p>
 * Based on
 * https://www.geeksforgeeks.org/stack-set-4-evaluation-postfix-expression/
 *
 * @author aldebaran30701
 */
public class QueryEvaluator {
    
    private enum Operator {
        AND(1),
        OR(2);

        final int precedence;

        private Operator(final int precedence) {
            this.precedence = precedence;
        }
    }

    private static final Map<String, Operator> OPERATORS = new HashMap<String, Operator>() {
        {
            put("&&", Operator.AND);
            put("||", Operator.OR);
        }
    };

    private static boolean isHigerPrecedence(final String operator, final String subOperator) {
        return (OPERATORS.containsKey(subOperator) 
                && OPERATORS.get(subOperator).precedence >= OPERATORS.get(operator).precedence);
    }

    public static String convertToPostfix(final String infix) {
        if (infix.isBlank()) {
            return "";
        }
        
        // create a stack
        final Deque<String> stack = new LinkedList<>();
        
        // scan all characters one by one
        final StringBuilder postfix = new StringBuilder();
        for (final String token : infix.split("\\[|\\]")) { // split on [ or ]
            // operator adding to stack
            if (OPERATORS.containsKey(token)) {
                while (!stack.isEmpty() && isHigerPrecedence(token, stack.peek())) {
                    postfix.append(stack.pop()).append('`');
                }
                stack.push(token);
            } else if (token.equals("(")) { // left parenthesis
                stack.push(token);
            } else if (token.equals(")")) { // right parenthesis
                while (!stack.peek().equals("(")) {
                    postfix.append(stack.pop()).append('`');
                }
                stack.pop();
            } else {
                postfix.append(token).append('`');
            }
        }

        // appending to output string
        while (!stack.isEmpty()) {
            postfix.append(stack.pop()).append('`');
        }
        
        return postfix.toString();
    }

    public static Boolean evaluatePostfix(final String postfix) {

        // create a stack
        final Stack<String> stack = new Stack<>();

        // scan all characters one by one
        final String[] expressionComponents = postfix.split(" ");
        for (final String expressionComponent : expressionComponents) {
            if (expressionComponent.equals("true") || expressionComponent.equals("false")) {
                // if the scanned character is T or F, push it to the stack.
                stack.push(expressionComponent);
            } else {
                // if the scanned character is an operator, pop two elements from stack apply the operator
                if (stack.size() < 2) {
                    return null;
                }

                final String value1 = stack.pop();
                final String value2 = stack.pop();
                switch (expressionComponent) {
                    // and case
                    case "&&": {
                        //System.out.println(val1 + "&&" + val2);
                        if (value1.equals("true") && value2.equals("false")) {
                            // T && F
                            stack.push("false");
                        } else if (value1.equals("false") && value2.equals("true")) {
                            // F && T
                            stack.push("false");
                        } else if (value1.equals("false") && value2.equals("false")) {
                            // F && F
                            stack.push("false");
                        } else {
                            // T && T
                            stack.push("true");
                        }
                        break;
                    }

                    // or case
                    case "||": {
                        //System.out.println(val1 + "||" + val2);
                        if (value1.equals("true") && value2.equals("false")) {
                            // T || F
                            stack.push("true");
                        } else if (value1.equals("false") && value2.equals("true")) {
                            // F || T
                            stack.push("true");
                        } else if (value1.equals("false") && value2.equals("false")) {
                            // F || F
                            stack.push("false");
                        } else {
                            // T || T
                            stack.push("true");
                        }
                        break;
                    }
                }
            }
        }

        if (stack.isEmpty()) {
            return null;
        }

        return Boolean.valueOf(stack.pop());
    }
}
