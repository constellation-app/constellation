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
package au.gov.asd.tac.constellation.utilities.query;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;

/**
 * Evaluates a query expression made up of a chain of operations of the form (a
 * op1 b op2 c ...).
 * <p>
 * Based on
 * https://www.geeksforgeeks.org/operatorStack-set-4-evaluation-postfix-expression/
 *
 * @author aldebaran30701
 */
public class QueryEvaluator {

    private static final String PARENTHESES_REGEX = "(?<!(?<![^\\\\]\\\\(?:\\\\"
            + "{2}){0,10})\\\\)\\)|(?<!(?<![^\\\\]\\\\(?:\\\\{2}){0,10})\\\\)\\(";

    private QueryEvaluator() {
    }

    public static List<String> retrieveAttributeNames(final String input) {
        List<String> attributes = new ArrayList<>();
        for (String querySegment : tokeniser(input)) {
            // get portions of the string.
            attributes.add(querySegment);
        }
        return attributes;
    }

    /**
     * Tokeniser takes input from the user and breaks it into terms such as
     * <ul>
     * <li>color == #ffffff</li>
     * <li>selected == true</li>
     * <li>||</li>
     * <li>&&</li>
     * <li>(</li>
     * <li>)</li>
     * </ul>
     * <p>
     * A backslash can be used to escape one of the terms. eg.
     * <ul>
     * <li>color == \(#ffffff\) - Input by user</li>
     * <li>color == (#ffffff) - Evaluated as</li>
     * </ul>
     * <p>
     * iterate all chars in input - check if moreToEscape if (, add term to list
     * of tokens if |, and previousChar was | then add || to list of tokens if
     * &, and previousChar was & then add && to list of tokens if ), add term to
     * list of tokens else, an escaped character or normal input so add to
     * currentString
     * <p>
     * @param input the query as written by the user to tokenise.
     * @return List<String> stringTokens is the items within the input string,
     * stored as a list.
     */
    public static List<String> tokeniser(final String input) {
        final List<String> stringTokens = new ArrayList<>();
        String currentString = "";
        char prevChar = Character.UNASSIGNED;

        if (StringUtils.isBlank(input)) {
            return Collections.emptyList();
        }

        for (final char c : input.toCharArray()) {
            if (c == '(' && prevChar != '\\') {
                if (StringUtils.isBlank(currentString)) {
                    currentString = "(";
                }
                currentString = StringUtils.trim(currentString);
                stringTokens.add(currentString);
                currentString = "";
            } else if (c == '|' && prevChar == '|') {
                currentString = currentString.substring(0, currentString.lastIndexOf(prevChar));
                if (StringUtils.isNotBlank(currentString)) {
                    currentString = StringUtils.trim(currentString);
                    stringTokens.add(currentString);
                }
                stringTokens.add("||");
                currentString = "";
            } else if (c == '&' && prevChar == '&') {
                currentString = currentString.substring(0, currentString.lastIndexOf(prevChar));
                if (StringUtils.isNotBlank(currentString)) {
                    currentString = StringUtils.trim(currentString);
                    stringTokens.add(currentString);
                }
                stringTokens.add("&&");
                currentString = "";
            } else if (c == ')' && prevChar != '\\') {
                if (StringUtils.isBlank(currentString)) {
                    currentString = ")";
                } else {
                    currentString = StringUtils.trim(currentString);
                    stringTokens.add(currentString);
                    currentString = ")";
                }
                currentString = StringUtils.trim(currentString);
                stringTokens.add(currentString);
                currentString = "";
            } else {
                if (c != '\\') {
                    currentString += c;
                }
            }
            prevChar = c;
        }
        if (StringUtils.isNotBlank(currentString)) {
            stringTokens.add(StringUtils.trim(currentString));
        }

        return convertToPostfix(stringTokens);
    }

    /**
     * Gets the operator precedence - || = 1 - && = 2 - else = 0
     *
     * @param operator the String representation of || and &&.
     * @return 0, 1 or 2 depending on the operator.
     */
    private static int getPrecedence(final String operator) {
        if ("||".equals(operator)) {
            return 1;
        }
        return "&&".equals(operator) ? 2 : 0;
    }

    /**
     * takes a query split into a list and converts the ordering into postfix.
     *
     * @param queryAsList the query split into a list
     * @return the query reordered into postfix order.
     */
    public static List<String> convertToPostfix(final List<String> queryAsList) {
        // list of queries to return in postfix order
        final List<String> orderedInPostfix = new ArrayList<>();
        // initializing empty Stack to hold operators
        final Deque<String> operatorStack = new ArrayDeque<>();

        for (final String token : queryAsList) {
            // If the scanned character is an operand, add it to output.
            if (!token.contains("||") && !token.contains("&&")
                    && !token.matches(PARENTHESES_REGEX)) {
                boolean moreToEscape = true;
                String trimmedToken = token;

                while (trimmedToken.contains("\\") && moreToEscape) {
                    if (trimmedToken.length() >= trimmedToken.indexOf('\\')) {
                        final StringBuilder sb = new StringBuilder(trimmedToken);
                        sb.deleteCharAt(trimmedToken.indexOf('\\'));
                        trimmedToken = sb.toString();
                        // when the trimmedToken has another \. meaning more to escape
                        moreToEscape = trimmedToken.contains("\\") && moreToEscape;
                    }
                }
                if (StringUtils.isNotBlank(trimmedToken)) {
                    orderedInPostfix.add(trimmedToken);
                }
            } else if ("(".contains(token)) {
                // If the scanned character is an '(', push it to the operatorStack.
                operatorStack.push(token);
            } else if (")".contains(token)) {
                // If the scanned character is an ')', pop and output from the operatorStack
                while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
                    // until an '(' is encountered.
                    orderedInPostfix.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
                    return Collections.emptyList();
                } else if (StringUtils.isNotBlank(operatorStack.peek())) {
                    orderedInPostfix.add(operatorStack.pop());
                } else {
                    // Do nothing
                }
            } else { // operator encountered
                while (!operatorStack.isEmpty() && getPrecedence(token)
                        <= getPrecedence(operatorStack.peek())) {
                    if ("(".equals(operatorStack.peek())) {
                        return Collections.emptyList();
                    } else if (StringUtils.isNotBlank(operatorStack.peek())) {
                        orderedInPostfix.add(operatorStack.pop());
                    } else {
                        // Do nothing
                    }
                }
                operatorStack.push(token);
            }
        }

        // pop all the operators from operatorStack
        while (!operatorStack.isEmpty()) {
            if ("(".equals(operatorStack.peek())) {
                return Collections.emptyList();
            } else if (StringUtils.isNotBlank(operatorStack.peek())) {
                orderedInPostfix.add(operatorStack.pop());
            } else {
                // Do nothing
            }
        }
        // remove unnecessary braces
        orderedInPostfix.removeIf(query -> "(".equals(query) || ")".equals(query));
        return orderedInPostfix;
    }

    /**
     * Accepts a postfix string containing the following operators and operands:
     * - && - || Operands - true - false
     *
     * This evaluates the posfix representation in a way that maintains the
     * correct evaluation order to achieve the correct boolean result.
     *
     * @param postfix the String representation of the postfix query
     * @return true or false depending on what it evaluated to.
     */
    public static Boolean evaluatePostfix(final String postfix) {

        final String t = String.valueOf(true);
        final String f = String.valueOf(false);

        // create a operatorStack
        final Stack<String> stack = new Stack<>();

        // scan all characters one by one
        final String[] expressionComponents = postfix.split(" ");
        for (final String expressionComponent : expressionComponents) {
            if (expressionComponent.equals(t) || expressionComponent.equals(f)) {
                // if the scanned character is T or F, push it to the operatorStack.
                stack.push(expressionComponent);
            } else {
                // if the scanned character is an operator, pop two elements
                // from operatorStack apply the operator
                if (stack.size() < 2) {
                    return false;
                }

                final String value1 = stack.pop();
                final String value2 = stack.pop();
                switch (expressionComponent) {
                    // and case
                    case "&&": {
                        if (value1.equals(t) && value2.equals(f)) {
                            stack.push(f);
                        } else if (value1.equals(f) && value2.equals(t)) {
                            stack.push(f);
                        } else if (value1.equals(f) && value2.equals(f)) {
                            stack.push(f);
                        } else {
                            stack.push(t);
                        }
                        break;
                    }
                    case "||": {
                        if (value1.equals(t) && value2.equals(f)) {
                            stack.push(t);
                        } else if (value1.equals(f) && value2.equals(t)) {
                            stack.push(t);
                        } else if (value1.equals(f) && value2.equals(f)) {
                            stack.push(f);
                        } else {
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
