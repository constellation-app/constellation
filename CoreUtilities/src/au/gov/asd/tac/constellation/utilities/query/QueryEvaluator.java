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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
    
    private static final String SPLIT_REGEX = "(?<!(?<![^\\\\]\\\\(?:\\\\{2}){0,10})\\\\)\\)|(?<!(?<![^\\\\]\\\\(?:\\\\{2}){0,10})\\\\)\\(";
    
    private enum Operator {
        AND(1),
        OR(2);

        final int precedence;

        private Operator(final int precedence) {
            this.precedence = precedence;
        }
    }
    
    private static final Map<String, Operator> OPERATORS = Map.ofEntries(Map.entry("&&", Operator.AND),Map.entry("||", Operator.OR));

    private static boolean isHigerPrecedence(final String operator, final String subOperator) {
        return (OPERATORS.containsKey(subOperator) 
                && OPERATORS.get(subOperator).precedence >= OPERATORS.get(operator).precedence);
    }

    public static List<String> convertToPostfix(final String infix) {
        if (infix.isBlank()) {
            @SuppressWarnings("unchecked") //Empty list will specifically be empty list of strings
            List<String> emptyStringList = (List<String>) Collections.EMPTY_LIST;
            return emptyStringList;
        }
        
        // create a stack
        final Deque<String> stack = new LinkedList<>();
        
        // create list to hold individual queries
        final List<String> queries = new ArrayList<>();
        
        // scan all characters one by one
        for (final String token : infix.split(SPLIT_REGEX)) { // split on [ or ]
            // operator adding to stack
            if (OPERATORS.containsKey(token)) {
                while (!stack.isEmpty() && isHigerPrecedence(token, stack.peek())) {
                    queries.add(stack.pop());
                }
                stack.push(token);
            }
            else{
                // adds query here
                // eg. Label:=:Vertex #0<Unknown>
                boolean escaped = true;
                String updatedToken = token;
                
                while (updatedToken.contains("\\") && escaped){
                    StringBuilder sb = new StringBuilder(updatedToken);
                    if(updatedToken.length() >= updatedToken.indexOf("\\")){
                        sb.deleteCharAt(updatedToken.indexOf("\\"));
                        updatedToken = sb.toString();
                        if (updatedToken.contains("\\")){
                            // when the updatedToken has another \. meaning it was a backslash escaped.
                            escaped = false;
                        }
                    }
                }
                queries.add(token);
            }
        }

        // appending to output string
        while (!stack.isEmpty()) {
            queries.add(stack.pop());
        }
        
        return queries;
    }

    public static Boolean evaluatePostfix(final String postfix) {
        
        final String t = String.valueOf(true);
        final String f = String.valueOf(false);

        // create a stack
        final Stack<String> stack = new Stack<>();

        // scan all characters one by one
        final String[] expressionComponents = postfix.split(" ");
        for (final String expressionComponent : expressionComponents) {
            if (expressionComponent.equals(t) || expressionComponent.equals(f)) {
                // if the scanned character is T or F, push it to the stack.
                stack.push(expressionComponent);
            } else {
                // if the scanned character is an operator, pop two elements from stack apply the operator
                if (stack.size() < 2) {
                    return false;
                }

                final String value1 = stack.pop();
                final String value2 = stack.pop();
                switch (expressionComponent) {
                    // and case
                    case "&&": {
                        //System.out.println(val1 + "&&" + val2);
                        if (value1.equals(t) && value2.equals(f)) {
                            // T && F
                            stack.push(f);
                        } else if (value1.equals(f) && value2.equals(t)) {
                            // F && T
                            stack.push(f);
                        } else if (value1.equals(f) && value2.equals(f)) {
                            // F && F
                            stack.push(f);
                        } else {
                            // T && T
                            stack.push(t);
                        }
                        break;
                    }

                    // or case
                    case "||": {
                        //System.out.println(val1 + "||" + val2);
                        if (value1.equals(t) && value2.equals(f)) {
                            // T || F
                            stack.push(t);
                        } else if (value1.equals(f) && value2.equals(t)) {
                            // F || T
                            stack.push(t);
                        } else if (value1.equals(f) && value2.equals(f)) {
                            // F || F
                            stack.push(f);
                        } else {
                            // T || T
                            stack.push(t);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }

        if (stack.isEmpty()) {
            return false;
        }

        return Boolean.valueOf(stack.pop());
    }
}
