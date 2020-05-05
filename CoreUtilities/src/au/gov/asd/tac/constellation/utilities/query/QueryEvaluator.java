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
import java.util.List;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;

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
    private static final String SPLIT_REGEX2 = "(?<!(?<![^\\\\]\\\\(?:\\\\{2}){0,10})\\\\)\\:";

    static int getPrecedence(String s) {
        return s.equals("||") ? 1 : s.equals("&&") ? 2 : 0;
    }

    public static List<String> infixToPostfix(String exp) {
        if (StringUtils.isBlank(exp)) {
            return Collections.emptyList();
        }
        // create list to hold individual queries
        final List<String> queries = new ArrayList<>();
        // initializing empty stack 
        Stack<String> stack = new Stack<>();

        for (String s : exp.split(SPLIT_REGEX2)) {
            // If the scanned character is an operand, add it to output. 
            if (!s.contains("||") && !s.contains("&&") && !s.contains("(") && !s.contains(")")) {
                boolean escaped = true;
                String updatedToken = s;

                while (updatedToken.contains("\\") && escaped) {
                    if (updatedToken.length() >= updatedToken.indexOf("\\")) {
                        StringBuilder sb = new StringBuilder(updatedToken);
                        sb.deleteCharAt(updatedToken.indexOf("\\"));
                        updatedToken = sb.toString();
                        // when the updatedToken has another \. meaning it was a backslash escaped.
                        escaped = updatedToken.contains("\\") ? false : escaped;
                    }
                }
                queries.add(updatedToken);
            } // If the scanned character is an '(', push it to the stack. 
            else if (s.contains("(")) {
                stack.push(s);
            } //  If the scanned character is an ')', pop and output from the stack  
            // until an '(' is encountered. 
            else if (s.contains(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    queries.add(stack.pop());
                }
                if (!stack.isEmpty() && !stack.peek().equals("(")) {
                    return Collections.emptyList();
                } else {
                    stack.pop();
                }
            } else { // operator encountered
                while (!stack.isEmpty() && getPrecedence(s) <= getPrecedence(stack.peek())) {
                    if (stack.peek().equals("(")) {
                        return Collections.emptyList();
                    }
                    queries.add(stack.pop());
                }
                stack.push(s);
            }
        }

        // pop all the operators from the stack 
        while (!stack.isEmpty()) {
            if (stack.peek().equals("(")) {
                return Collections.emptyList();
            }
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
