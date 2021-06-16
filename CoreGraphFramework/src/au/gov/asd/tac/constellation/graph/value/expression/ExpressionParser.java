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

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author sirius
 */
public class ExpressionParser {

    private static final Logger LOGGER = Logger.getLogger(ExpressionParser.class.getName());
    public static final char NO_TOKEN = 0;
    private static final String NESTED_PARENTHESIS_ERROR = "Invalid nesting of parenthesis";
    private static final String ENDS_WITH_OPERATOR_ERROR = "An expression cannot end with an operator";
    private static final String END_OF_QUOTED_STRING_ERROR = "Unexpected end of expression while in quoted string";
    private static final String UNEXPECTED_CHARACTER_ERROR = "Unexpected character: ";
    private static final String TWO_NON_OPERATORS_SEQUENCE = "2 non-operator tokens in sequence";

    private enum ParseState {
        READING_WHITESPACE,
        READING_SINGLE_STRING,
        READING_DOUBLE_STRING,
        READING_VARIABLE,
        READING_SINGLE_ESCAPED,
        READING_DOUBLE_ESCAPED
    }

    public enum Operator {
        AND_AND(NO_TOKEN, 11),
        AND('&', 11, null, AND_AND),
        OR_OR(NO_TOKEN, 12),
        OR('|', 12, null, OR_OR),
        EXCLUSIVE_OR('^', 3),
        NOT('!', 2),
        ADD('+', 4),
        SUBTRACT('-', 4),
        MULTIPLY('*', 3),
        DIVIDE('/', 3),
        MODULO('%', 3),
        GREATER_THAN('>', 6),
        LESS_THAN('<', 6),
        GREATER_THAN_OR_EQUALS(NO_TOKEN, 6),
        LESS_THAN_OR_EQUALS(NO_TOKEN, 6),
        NOT_EQUALS(NO_TOKEN, 7),
        CONTAINS(NO_TOKEN, 4),
        STARTS_WITH(NO_TOKEN, 4),
        ENDS_WITH(NO_TOKEN, 4),
        EQUALS(NO_TOKEN, 7),
        ASSIGN('=', 14, null, EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, NOT, NOT_EQUALS);

        private final char token;
        private final int precedence;
        private final Map<Operator, Operator> combinations = new HashMap<>();

        private Operator(char token, int precedence, Operator... combinations) {
            this.token = token;
            this.precedence = precedence;

            for (int i = 0; i < combinations.length; i += 2) {
                if (combinations[i] == null) {
                    this.combinations.put(this, combinations[i + 1]);
                } else {
                    this.combinations.put(combinations[i], combinations[i + 1]);
                }
            }
        }

        public char getToken() {
            return token;
        }

        public int getPrecedence() {
            return precedence;
        }

        private static final Map<Character, Operator> OPERATOR_TOKENS = new HashMap<>();

        static {
            for (final Operator operator : Operator.values()) {
                if (operator.token != NO_TOKEN) {
                    OPERATOR_TOKENS.put(operator.token, operator);
                }
            }
        }
    }

    private static final Map<String, Operator> WORD_OPERATORS = new HashMap<>();

    static {
        WORD_OPERATORS.put("contains", Operator.CONTAINS);
        WORD_OPERATORS.put("startswith", Operator.STARTS_WITH);
        WORD_OPERATORS.put("endswith", Operator.ENDS_WITH);
        WORD_OPERATORS.put("or", Operator.OR);
        WORD_OPERATORS.put("and", Operator.AND);
        WORD_OPERATORS.put("equals", Operator.EQUALS);
        WORD_OPERATORS.put("notequals", Operator.NOT_EQUALS);
    }

    private ExpressionParser() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public abstract static class Expression {

        private SequenceExpression parent;

        private Expression(SequenceExpression parent) {
            this.parent = parent;
        }

        public SequenceExpression getParent() {
            return parent;
        }

        protected abstract void print(String prefix, StringBuilder out);

        @Override
        public String toString() {
            final StringBuilder out = new StringBuilder();
            print("", out);
            return out.toString();
        }
    }

    public static class VariableExpression extends Expression {

        private final String content;

        private VariableExpression(SequenceExpression parent, char[] content, int contentLength) {
            super(parent);
            this.content = new String(content, 0, contentLength);
        }

        public String getContent() {
            return content;
        }

        @Override
        protected void print(String prefix, StringBuilder out) {
            out.append(prefix).append("VARIABLE: ").append(content).append("\n");
        }
    }

    public static class StringExpression extends Expression {

        private final String content;

        private StringExpression(SequenceExpression parent, char[] content, int contentLength) {
            super(parent);
            this.content = new String(content, 0, contentLength);
        }

        public String getContent() {
            return content;
        }

        @Override
        protected void print(String prefix, StringBuilder out) {
            out.append(prefix).append("STRING: ").append(content).append("\n");
        }
    }

    public static class OperatorExpression extends Expression {

        private Operator operator;

        private OperatorExpression(SequenceExpression parent, Operator operator) {
            super(parent);
            this.operator = operator;
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        protected void print(String prefix, StringBuilder out) {
            out.append(prefix).append("OPERATOR").append(": ").append(operator).append("\n");
        }
    }

    public static class SequenceExpression extends Expression {

        private final List<Expression> children = new ArrayList<>();
        private final List<Expression> unmodifiableChildren = Collections.unmodifiableList(children);

        private SequenceExpression(SequenceExpression parent) {
            super(parent);
        }

        public List<Expression> getUnmodifiableChildren() {
            return unmodifiableChildren;
        }

        private void addChild(Expression expression) {

            if (expression instanceof SequenceExpression) {
                final SequenceExpression tokenSequence = (SequenceExpression) expression;
                switch (tokenSequence.children.size()) {
                    case 0:
                        return;
                    case 1:
                        expression = tokenSequence.children.get(0);
                        break;
                    default:
                        if (tokenSequence.children.get(tokenSequence.children.size() - 1) instanceof OperatorExpression) {
                            Platform.runLater(() -> {
                                NotifyDisplayer.displayAlert("Query Error", "Malformed Query", ENDS_WITH_OPERATOR_ERROR, Alert.AlertType.ERROR);
                            });
                            LOGGER.log(Level.WARNING, "There was a query error: " + ENDS_WITH_OPERATOR_ERROR);
                        }
                }
            }

            if (expression instanceof OperatorExpression && !children.isEmpty()) {
                final Expression lastChild = children.get(children.size() - 1);
                if (lastChild instanceof OperatorExpression) {
                    final OperatorExpression tokenOperator = (OperatorExpression) expression;
                    final OperatorExpression lastChildOperator = (OperatorExpression) lastChild;
                    final Operator combinedOperator = tokenOperator.operator.combinations.get(lastChildOperator.operator);
                    if (combinedOperator != null) {
                        children.remove(children.size() - 1);
                        tokenOperator.operator = combinedOperator;
                    }
                }
            }

            if (!(expression instanceof OperatorExpression) && !children.isEmpty()) {
                final Expression lastChild = children.get(children.size() - 1);
                if (lastChild instanceof OperatorExpression) {
                    if (children.size() == 1 || children.get(children.size() - 2) instanceof OperatorExpression) {
                        final SequenceExpression childSequence = new SequenceExpression(this);
                        lastChild.parent = childSequence;
                        childSequence.children.add(lastChild);
                        expression.parent = childSequence;
                        childSequence.children.add(expression);
                        children.remove(children.size() - 1);
                        addChild(childSequence);
                        return;
                    }
                } else {
                    if (expression instanceof VariableExpression) {
                        final VariableExpression tokenVariable = (VariableExpression) expression;
                        final Operator wordOperator = WORD_OPERATORS.get(tokenVariable.content.toLowerCase());
                        if (wordOperator != null) {
                            children.add(new OperatorExpression(this, wordOperator));
                            return;
                        }
                    }
                    Platform.runLater(() -> {
                        NotifyDisplayer.displayAlert("Query Error", "Malformed Query", TWO_NON_OPERATORS_SEQUENCE, Alert.AlertType.ERROR);
                    });
                    LOGGER.log(Level.WARNING, "There was a query error: " + TWO_NON_OPERATORS_SEQUENCE);
                    return;
                }
            }

            children.add(expression);
        }

        public void normalize() {
            normalizeChildren();

            while (children.size() > 3) {
                int lowestPrecedence = Integer.MAX_VALUE;
                int lowestIndex = -1;
                for (int i = 1; i < children.size(); i += 2) {
                    final int precedence = ((OperatorExpression) children.get(i)).getOperator().getPrecedence();
                    if (precedence < lowestPrecedence) {
                        lowestPrecedence = precedence;
                        lowestIndex = i;
                    }
                }

                final SequenceExpression childSequence = new SequenceExpression(this);

                final Expression left = children.remove(lowestIndex - 1);
                left.parent = childSequence;
                childSequence.addChild(left);

                final Expression operator = children.remove(lowestIndex - 1);
                operator.parent = childSequence;
                childSequence.addChild(operator);

                final Expression right = children.get(lowestIndex - 1);
                right.parent = childSequence;
                childSequence.addChild(right);

                children.set(lowestIndex - 1, childSequence);
            }
        }

        private void normalizeChildren() {
            for (int i = children.size() - 1; i >= 0; i--) {
                final Expression child = children.get(i);
                if (child instanceof SequenceExpression) {
                    ((SequenceExpression) child).normalize();
                }
            }
        }

        @Override
        protected void print(String prefix, StringBuilder out) {
            out.append(prefix).append("(\n");
            children.forEach(child -> {
                child.print(prefix + "  ", out);
            });
            out.append(prefix).append(")\n");
        }
    }

    public static SequenceExpression parse(String expression) {

        ParseState state = ParseState.READING_WHITESPACE;
        final char[] content = new char[expression.length()];
        int contentLength = 0;

        SequenceExpression rootExpression = new SequenceExpression(null);
        SequenceExpression currentExpression = rootExpression;

        for (int i = 0; i <= expression.length(); i++) {
            final char c = i < expression.length() ? expression.charAt(i) : 0;

            switch (state) {
                case READING_WHITESPACE:
                    if (c != ' ' && c != 0) {
                        if (isLetter(c)) {
                            content[contentLength++] = c;
                            state = ParseState.READING_VARIABLE;
                        } else if (c == '\'') {
                            state = ParseState.READING_SINGLE_STRING;
                        } else if (c == '"') {
                            state = ParseState.READING_DOUBLE_STRING;
                        } else if (c == '(') {
                            currentExpression = new SequenceExpression(currentExpression);
                        } else if (c == ')') {
                            if (currentExpression == rootExpression) {
                                Platform.runLater(() -> {
                                    NotifyDisplayer.displayAlert("Query Error", "Malformed Query", NESTED_PARENTHESIS_ERROR, Alert.AlertType.ERROR);
                                });
                                LOGGER.log(Level.WARNING, "There was a query error: " + NESTED_PARENTHESIS_ERROR);
                                return null;
                            }
                            final SequenceExpression parentExpression = currentExpression.getParent();
                            parentExpression.addChild(currentExpression);
                            currentExpression = parentExpression;
                        } else if (Operator.OPERATOR_TOKENS.containsKey(c)) {
                            currentExpression.addChild(new OperatorExpression(currentExpression, Operator.OPERATOR_TOKENS.get(c)));
                        } else {
                            Platform.runLater(() -> {
                                NotifyDisplayer.displayAlert("Query Error", "Malformed Query", UNEXPECTED_CHARACTER_ERROR + c, Alert.AlertType.ERROR);
                            });
                            LOGGER.log(Level.WARNING, "There was a query error: " + UNEXPECTED_CHARACTER_ERROR + "{0}", c);
                            return null;
                        }
                    }
                    break;

                case READING_VARIABLE:
                    if (c == ' ' || c == 0) {
                        currentExpression.addChild(new VariableExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        state = ParseState.READING_WHITESPACE;
                    } else if (isLetter(c) || isDigit(c)) {
                        content[contentLength++] = c;
                    } else if (c == '(') {
                        currentExpression.addChild(new VariableExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        currentExpression = new SequenceExpression(currentExpression);
                    } else if (c == ')') {
                        if (currentExpression == rootExpression) {
                            Platform.runLater(() -> {
                                NotifyDisplayer.displayAlert("Query Error", "Malformed Query", NESTED_PARENTHESIS_ERROR, Alert.AlertType.ERROR);
                            });
                            LOGGER.log(Level.WARNING, "There was a query error: " + NESTED_PARENTHESIS_ERROR);
                            return null;
                        }
                        currentExpression.addChild(new VariableExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        final SequenceExpression parentExpression = currentExpression.getParent();
                        parentExpression.addChild(currentExpression);
                        currentExpression = parentExpression;
                        state = ParseState.READING_WHITESPACE;
                    } else if (Operator.OPERATOR_TOKENS.containsKey(c)) {
                        currentExpression.addChild(new VariableExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        currentExpression.addChild(new OperatorExpression(currentExpression, Operator.OPERATOR_TOKENS.get(c)));
                        state = ParseState.READING_WHITESPACE;
                    } else {
                        Platform.runLater(() -> {
                            NotifyDisplayer.displayAlert("Query Error", "Malformed Query", UNEXPECTED_CHARACTER_ERROR + c, Alert.AlertType.ERROR);
                        });
                        LOGGER.log(Level.WARNING, "There was a query error: " + UNEXPECTED_CHARACTER_ERROR + " {0}", c);
                        return null;
                    }
                    break;

                case READING_SINGLE_STRING:
                    if (c == '\'') {
                        currentExpression.addChild(new StringExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        state = ParseState.READING_WHITESPACE;
                    } else if (c == '\\') {
                        state = ParseState.READING_SINGLE_ESCAPED;
                    } else if (c == 0) {
                        Platform.runLater(() -> {
                            NotifyDisplayer.displayAlert("Query Error", "Malformed Query", END_OF_QUOTED_STRING_ERROR, Alert.AlertType.ERROR);
                        });
                        LOGGER.log(Level.WARNING, "There was a query error: " + END_OF_QUOTED_STRING_ERROR);
                        return null;
                    } else {
                        content[contentLength++] = c;
                    }
                    break;

                case READING_DOUBLE_STRING:
                    if (c == '"') {
                        currentExpression.addChild(new StringExpression(currentExpression, content, contentLength));
                        contentLength = 0;
                        state = ParseState.READING_WHITESPACE;
                    } else if (c == '\\') {
                        state = ParseState.READING_DOUBLE_ESCAPED;
                    } else if (c == 0) {
                        Platform.runLater(() -> {
                            NotifyDisplayer.displayAlert("Query Error", "Malformed Query", END_OF_QUOTED_STRING_ERROR, Alert.AlertType.ERROR);
                        });
                        LOGGER.log(Level.WARNING, "There was a query error: " + END_OF_QUOTED_STRING_ERROR);
                        return null;
                    } else {
                        content[contentLength++] = c;
                    }
                    break;

                case READING_SINGLE_ESCAPED:
                    if (c == 0) {
                        Platform.runLater(() -> {
                            NotifyDisplayer.displayAlert("Query Error", "Malformed Query", END_OF_QUOTED_STRING_ERROR, Alert.AlertType.ERROR);
                        });
                        LOGGER.log(Level.WARNING, "There was a query error: " + END_OF_QUOTED_STRING_ERROR);
                        return null;
                    } else {
                        content[contentLength++] = c;
                        state = ParseState.READING_SINGLE_STRING;
                    }
                    break;

                case READING_DOUBLE_ESCAPED:
                    if (c == 0) {
                        Platform.runLater(() -> {
                            NotifyDisplayer.displayAlert("Query Error", "Malformed Query", END_OF_QUOTED_STRING_ERROR, Alert.AlertType.ERROR);
                        });
                        LOGGER.log(Level.WARNING, "There was a query error: " + END_OF_QUOTED_STRING_ERROR);
                        return null;
                    } else {
                        content[contentLength++] = c;
                        state = ParseState.READING_DOUBLE_STRING;
                    }
                    break;
            }
        }

        if (currentExpression != rootExpression) {
            Platform.runLater(() -> {
                NotifyDisplayer.displayAlert("Query Error", "Malformed Query", NESTED_PARENTHESIS_ERROR, Alert.AlertType.ERROR);
            });
            LOGGER.log(Level.WARNING, "There was a query error: " + NESTED_PARENTHESIS_ERROR);
            return null;
        }
        if (rootExpression.children.size() == 1) {
            final Expression onlyChild = rootExpression.children.get(0);
            if (onlyChild instanceof SequenceExpression) {
                rootExpression = (SequenceExpression) onlyChild;
            }
        }
        if (!currentExpression.children.isEmpty() && currentExpression.children.get(currentExpression.children.size() - 1) instanceof OperatorExpression) {
            Platform.runLater(() -> {
                NotifyDisplayer.displayAlert("Query Error", "Malformed Query", ENDS_WITH_OPERATOR_ERROR, Alert.AlertType.ERROR);
            });
            LOGGER.log(Level.WARNING, "There was a query error: " + ENDS_WITH_OPERATOR_ERROR);
            return null;
        }

        rootExpression.normalize();
        return rootExpression;
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
