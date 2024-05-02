/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import java.util.Objects;
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
    private static boolean isHeadless = false;
    private static final String QUERY_ERROR = "Query Error";
    private static final String MALFORMED_QUERY = "Malformed Query";
    private static final String QUERY_ERROR_MSG = "There was a query error: {0}";
    private static final String NULL_INPUT_ERROR = "Input String was null";
    private static final String NESTED_PARENTHESIS_ERROR = "Invalid nesting of parenthesis";
    private static final String ENDS_WITH_OPERATOR_ERROR = "An expression cannot end with an operator";
    private static final String END_OF_QUOTED_STRING_ERROR = "Unexpected end of expression while in quoted string.\nCould not find the pair to: %s";
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
        NOT('!', 2),
        MULTIPLY('*', 3),
        DIVIDE('/', 3),
        MODULO('%', 3),
        ADD('+', 4),
        SUBTRACT('-', 4),
        CONTAINS(NO_TOKEN, 4),
        STARTS_WITH(NO_TOKEN, 4),
        ENDS_WITH(NO_TOKEN, 4),
        GREATER_THAN('>', 6),
        LESS_THAN('<', 6),
        GREATER_THAN_OR_EQUALS(NO_TOKEN, 6),
        LESS_THAN_OR_EQUALS(NO_TOKEN, 6),
        EQUALS(NO_TOKEN, 7),
        NOT_EQUALS(NO_TOKEN, 7),
        AND_AND(NO_TOKEN, 11),
        AND('&', 8, null, AND_AND),
        OR_OR(NO_TOKEN, 12),
        EXCLUSIVE_OR('^', 9),
        OR('|', 10, null, OR_OR),
        ASSIGN('=', 14, null, EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, NOT, NOT_EQUALS);

        private final char token;
        private final int precedence;
        private final Map<Operator, Operator> combinations = new HashMap<>();

        private Operator(final char token, final int precedence, final Operator... combinations) {
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
        WORD_OPERATORS.put("xor", Operator.EXCLUSIVE_OR);
        WORD_OPERATORS.put("or", Operator.OR_OR);
        WORD_OPERATORS.put("and", Operator.AND_AND);
        WORD_OPERATORS.put("equals", Operator.EQUALS);
        WORD_OPERATORS.put("notequals", Operator.NOT_EQUALS);
        WORD_OPERATORS.put("not", Operator.NOT);
        WORD_OPERATORS.put("add", Operator.ADD);
        WORD_OPERATORS.put("subtract", Operator.SUBTRACT);
        WORD_OPERATORS.put("gt", Operator.GREATER_THAN);
        WORD_OPERATORS.put("gteq", Operator.GREATER_THAN_OR_EQUALS);
        WORD_OPERATORS.put("lt", Operator.LESS_THAN);
        WORD_OPERATORS.put("lteq", Operator.LESS_THAN_OR_EQUALS);
        WORD_OPERATORS.put("assign", Operator.ASSIGN);
        WORD_OPERATORS.put("mod", Operator.MODULO);
        WORD_OPERATORS.put("multiply", Operator.MULTIPLY);
        WORD_OPERATORS.put("divide", Operator.DIVIDE);
    }

    private ExpressionParser() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static void hideErrorPrompts(final boolean hide) {
        isHeadless = hide;
    }

    public abstract static class Expression {

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.parent);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return Objects.equals(this.parent, ((Expression) obj).parent);
        }

        private SequenceExpression parent;

        private Expression(final SequenceExpression parent) {
            this.parent = parent;
        }

        public SequenceExpression getParent() {
            return parent;
        }

        protected abstract void print(final String prefix, final StringBuilder out);

        @Override
        public String toString() {
            final StringBuilder out = new StringBuilder();
            print("", out);
            return out.toString();
        }
    }

    public static class VariableExpression extends Expression {

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.content);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return Objects.equals(this.content, ((VariableExpression) obj).content);
        }

        private final String content;

        protected VariableExpression(final SequenceExpression parent, final char[] content, final int contentLength) {
            super(parent);
            this.content = new String(content, 0, contentLength);
        }

        public String getContent() {
            return content;
        }

        @Override
        protected void print(final String prefix, final StringBuilder out) {
            out.append(prefix).append("VARIABLE: ").append(content).append("\n");
        }
    }

    public static class StringExpression extends Expression {

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.content);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return Objects.equals(this.content, ((StringExpression) obj).content);
        }

        private final String content;

        protected StringExpression(final SequenceExpression parent, final char[] content, final int contentLength) {
            super(parent);
            this.content = new String(content, 0, contentLength);
        }

        public String getContent() {
            return content;
        }

        @Override
        protected void print(final String prefix, final StringBuilder out) {
            out.append(prefix).append("STRING: ").append(content).append("\n");
        }
    }

    public static class OperatorExpression extends Expression {

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.operator);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return this.operator == ((OperatorExpression) obj).operator;
        }

        private Operator operator;

        OperatorExpression(final SequenceExpression parent, final Operator operator) {
            super(parent);
            this.operator = operator;
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        protected void print(final String prefix, final StringBuilder out) {
            out.append(prefix).append("OPERATOR").append(": ").append(operator).append("\n");
        }
    }

    public static class SequenceExpression extends Expression {

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 73 * hash + Objects.hashCode(this.children);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return Objects.equals(this.children, ((SequenceExpression) obj).children);
        }

        private final List<Expression> children = new ArrayList<>();
        private final List<Expression> unmodifiableChildren = Collections.unmodifiableList(children);

        protected SequenceExpression(SequenceExpression parent) {
            super(parent);
        }

        public List<Expression> getUnmodifiableChildren() {
            return unmodifiableChildren;
        }

        protected void addChild(Expression expression) {

            if (expression instanceof SequenceExpression tokenSequence) {
                switch (tokenSequence.children.size()) {
                    case 0 -> {
                        return;
                    }
                    case 1 ->
                        expression = tokenSequence.children.get(0);
                    default -> {
                        if (tokenSequence.children.get(tokenSequence.children.size() - 1) instanceof OperatorExpression) {
                            Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                                    MALFORMED_QUERY, ENDS_WITH_OPERATOR_ERROR, Alert.AlertType.ERROR));
                            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, ENDS_WITH_OPERATOR_ERROR);
                        }
                    }
                }
            }

            if (expression instanceof OperatorExpression tokenOperator && !children.isEmpty()) {
                final Expression lastChild = children.get(children.size() - 1);
                if (lastChild instanceof OperatorExpression lastChildOperator) {
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
                    if (expression instanceof VariableExpression tokenVariable) {
                        final Operator wordOperator = WORD_OPERATORS.get(tokenVariable.content.toLowerCase());
                        if (wordOperator != null) {
                            children.add(new OperatorExpression(this, wordOperator));
                            return;
                        }
                    }
                    if (!isHeadless) {
                        Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                                MALFORMED_QUERY, TWO_NON_OPERATORS_SEQUENCE, Alert.AlertType.ERROR));
                    }
                    LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, TWO_NON_OPERATORS_SEQUENCE);
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
                if (child instanceof SequenceExpression sequenceExpression) {
                    sequenceExpression.normalize();
                }
            }
        }

        @Override
        protected void print(final String prefix, final StringBuilder out) {
            out.append(prefix).append("(\n");
            children.forEach(child -> child.print(prefix + "  ", out));
            out.append(prefix).append(")\n");
        }
    }

    private static class ParseInfoHolder {

        private ParseState state = ParseState.READING_WHITESPACE;
        private int contentLength = 0;
        private SequenceExpression rootExpression = new SequenceExpression(null);
        private SequenceExpression currentExpression = rootExpression;
    }

    public static SequenceExpression parse(final String expression) {
        if (expression == null) {
            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, NULL_INPUT_ERROR);
            return null;
        }

        final char[] content = new char[expression.length()];
        ParseInfoHolder info = new ParseInfoHolder();

        for (int i = 0; i <= expression.length(); i++) {
            final char c = i < expression.length() ? expression.charAt(i) : 0;

            switch (info.state) {
                case READING_WHITESPACE -> {
                    if (!parseReadingWhitespace(c, content, info)) {
                        return null;
                    }
                }
                case READING_VARIABLE -> {
                    if (!parseReadingVariable(c, content, info)) {
                        return null;
                    }
                }
                case READING_SINGLE_STRING -> {
                    if (!parseReadingSingleString(c, content, info)) {
                        return null;
                    }
                }
                case READING_DOUBLE_STRING -> {
                    if (!parseReadingDoubleString(c, content, info)) {
                        return null;
                    }
                }
                case READING_SINGLE_ESCAPED -> {
                    if (!parseReadingStringEscaped(c, content, info)) {
                        return null;
                    }
                }
                case READING_DOUBLE_ESCAPED -> {
                    if (!parseReadingDoubleEscaped(c, content, info)) {
                        return null;
                    }
                }
            }
        }
        //We do want to check if the two variables are NOT the same object
        if (info.currentExpression != info.rootExpression) {
            if (!isHeadless) {
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                        MALFORMED_QUERY, NESTED_PARENTHESIS_ERROR, Alert.AlertType.ERROR));
            }
            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, NESTED_PARENTHESIS_ERROR);
            return null;
        }
        if (info.rootExpression.children.size() == 1) {
            final Expression onlyChild = info.rootExpression.children.get(0);
            if (onlyChild instanceof SequenceExpression sequenceExpression) {
                info.rootExpression = sequenceExpression;
            }
        }
        if (!info.currentExpression.children.isEmpty() && info.currentExpression.children.get(info.currentExpression.children.size() - 1) instanceof OperatorExpression) {
            if (!isHeadless) {
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                        MALFORMED_QUERY, ENDS_WITH_OPERATOR_ERROR, Alert.AlertType.ERROR));
            }
            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, ENDS_WITH_OPERATOR_ERROR);
            return null;
        }

        info.rootExpression.normalize();
        return info.rootExpression;
    }

    private static boolean isLetter(final char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isAllowable(final char c) {
        return (c == '_' || c == '{' || c == '}');
    }

    private static boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean parseReadingWhitespace(final char c, final char[] content, final ParseInfoHolder info) {
        if (c != ' ' && c != 0) {
            if (isLetter(c) || isDigit(c) || isAllowable(c)) {
                content[info.contentLength++] = c;
                info.state = ParseState.READING_VARIABLE;
            } else if (c == '\'') {
                info.state = ParseState.READING_SINGLE_STRING;
            } else if (c == '"') {
                info.state = ParseState.READING_DOUBLE_STRING;
            } else if (c == '(') {
                info.currentExpression = new SequenceExpression(info.currentExpression);
            } else if (c == ')') {
                //Preventing sonar check, as we do want to check if the two variables are the same object
                if (info.currentExpression == info.rootExpression) {//NOSONAR
                    final String errorMessage = """
                                                            Found closing parenthesis ) within variable.
                                                            parentheses must be used in pairs to enclose variables.""";
                    if (!isHeadless) {
                        Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                                MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
                    }
                    LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, errorMessage);
                    return false;
                }
                final SequenceExpression parentExpression = info.currentExpression.getParent();
                parentExpression.addChild(info.currentExpression);
                info.currentExpression = parentExpression;
            } else if (Operator.OPERATOR_TOKENS.containsKey(c)) {
                info.currentExpression.addChild(new OperatorExpression(info.currentExpression, Operator.OPERATOR_TOKENS.get(c)));
            } else {
                final String errorMessage = String.format(UNEXPECTED_CHARACTER_ERROR + "\nEnsure values are surrounded with Single or Double quotes.", c);
                if (!isHeadless) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                            MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
                }
                LOGGER.log(Level.WARNING, errorMessage);
                return false;
            }
        }
        // Success
        return true;
    }

    private static boolean parseReadingVariable(final char c, final char[] content, final ParseInfoHolder info) {
        if (c == ' ' || c == 0) {
            info.currentExpression.addChild(new VariableExpression(info.currentExpression, content, info.contentLength));
            info.contentLength = 0;
            info.state = ParseState.READING_WHITESPACE;
        } else if (isLetter(c) || isDigit(c) || isAllowable(c)) {
            content[info.contentLength++] = c;
        } else if (c == '(') {
            info.currentExpression.addChild(new VariableExpression(info.currentExpression, content, info.contentLength));
            info.contentLength = 0;
            info.currentExpression = new SequenceExpression(info.currentExpression);
        } else if (c == ')') {
            //Preventing sonar check, as we do want to check if the two variables are the same object
            if (info.currentExpression == info.rootExpression) {//NOSONAR
                final String errorMessage = """
                                                        Found closing parenthesis ) within variable.
                                                        parentheses must be used in pairs to enclose variables.""";
                if (!isHeadless) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                            MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
                }
                LOGGER.log(Level.WARNING, errorMessage);
                return false;
            }
            info.currentExpression.addChild(new VariableExpression(info.currentExpression, content, info.contentLength));
            info.contentLength = 0;
            final SequenceExpression parentExpression = info.currentExpression.getParent();
            parentExpression.addChild(info.currentExpression);
            info.currentExpression = parentExpression;
            info.state = ParseState.READING_WHITESPACE;
        } else if (Operator.OPERATOR_TOKENS.containsKey(c)) {
            info.currentExpression.addChild(new VariableExpression(info.currentExpression, content, info.contentLength));
            info.contentLength = 0;
            info.currentExpression.addChild(new OperatorExpression(info.currentExpression, Operator.OPERATOR_TOKENS.get(c)));
            info.state = ParseState.READING_WHITESPACE;
        } else {
            final String errorMessage = String.format(UNEXPECTED_CHARACTER_ERROR + "\nEnsure values are surrounded with Single or Double quotes.", c);
            if (!isHeadless) {
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                        MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
            }
            LOGGER.log(Level.WARNING, errorMessage);
            return false;
        }
        // Success
        return true;
    }

    private static boolean parseReadingSingleString(final char c, final char[] content, final ParseInfoHolder info) {
        switch (c) {
            case '\'' -> {
                info.currentExpression.addChild(new StringExpression(info.currentExpression, content, info.contentLength));
                info.contentLength = 0;
                info.state = ParseState.READING_WHITESPACE;
            }
            case '\\' ->
                info.state = ParseState.READING_SINGLE_ESCAPED;
            case 0 -> {
                final String errorMessage = String.format(END_OF_QUOTED_STRING_ERROR, "'");
                if (!isHeadless) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                            MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
                }
                LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, errorMessage);
                return false;
            }
            default ->
                content[info.contentLength++] = c;
        }
        // Success
        return true;
    }

    private static boolean parseReadingDoubleString(final char c, final char[] content, final ParseInfoHolder info) {
        switch (c) {
            case '"' -> {
                info.currentExpression.addChild(new StringExpression(info.currentExpression, content, info.contentLength));
                info.contentLength = 0;
                info.state = ParseState.READING_WHITESPACE;
            }
            case '\\' ->
                info.state = ParseState.READING_DOUBLE_ESCAPED;
            case 0 -> {
                final String errorMessage = String.format(END_OF_QUOTED_STRING_ERROR, '"');
                if (!isHeadless) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                            MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
                }
                LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, errorMessage);
                return false;
            }
            default ->
                content[info.contentLength++] = c;
        }
        // Success
        return true;
    }

    private static boolean parseReadingStringEscaped(final char c, final char[] content, final ParseInfoHolder info) {
        if (c == 0) {
            final String errorMessage = "Found escaped character ' at end of expression.";
            if (!isHeadless) {
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                        MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
            }
            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, errorMessage);
            return false;
        } else {
            content[info.contentLength++] = c;
            info.state = ParseState.READING_SINGLE_STRING;
        }
        // Success
        return true;
    }

    private static boolean parseReadingDoubleEscaped(final char c, final char[] content, final ParseInfoHolder info) {
        if (c == 0) {
            final String errorMessage = "Found escaped character \" at end of expression.";
            if (!isHeadless) {
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR,
                        MALFORMED_QUERY, errorMessage, Alert.AlertType.ERROR));
            }
            LOGGER.log(Level.WARNING, QUERY_ERROR_MSG, errorMessage);
            return false;
        } else {
            content[info.contentLength++] = c;
            info.state = ParseState.READING_DOUBLE_STRING;
        }
        // Success
        return true;
    }
}
