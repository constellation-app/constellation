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

import au.gov.asd.tac.constellation.graph.value.OperatorRegistry;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.constants.StringConstant;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.Expression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.Operator;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.OperatorExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.StringExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.VariableExpression;
import au.gov.asd.tac.constellation.graph.value.operations.And;
import au.gov.asd.tac.constellation.graph.value.operations.Assign;
import au.gov.asd.tac.constellation.graph.value.operations.Contains;
import au.gov.asd.tac.constellation.graph.value.operations.Difference;
import au.gov.asd.tac.constellation.graph.value.operations.EndsWith;
import au.gov.asd.tac.constellation.graph.value.operations.Equals;
import au.gov.asd.tac.constellation.graph.value.operations.ExclusiveOr;
import au.gov.asd.tac.constellation.graph.value.operations.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.operations.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.operations.LessThan;
import au.gov.asd.tac.constellation.graph.value.operations.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.operations.Modulus;
import au.gov.asd.tac.constellation.graph.value.operations.Negative;
import au.gov.asd.tac.constellation.graph.value.operations.Not;
import au.gov.asd.tac.constellation.graph.value.operations.NotEquals;
import au.gov.asd.tac.constellation.graph.value.operations.Or;
import au.gov.asd.tac.constellation.graph.value.operations.Positive;
import au.gov.asd.tac.constellation.graph.value.operations.Product;
import au.gov.asd.tac.constellation.graph.value.operations.Quotient;
import au.gov.asd.tac.constellation.graph.value.operations.StartsWith;
import au.gov.asd.tac.constellation.graph.value.operations.Sum;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.util.EnumMap;
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
public class ExpressionCompiler {

    private static final Logger LOGGER = Logger.getLogger(ExpressionCompiler.class.getName());
    private static final Map<Operator, String> OPERATOR_CLASSES = new EnumMap<>(Operator.class);
    private static final Map<Operator, String> CONVERTER_CLASSES = new EnumMap<>(Operator.class);
    private static final String QUERY_ERROR = "Query Error";
    private static final String MALFORMED_QUERY = "Malformed Query";
    private static final String RECHECK_QUERY = "Ensure capitalisation, spelling and quotation is correct.";
    private static final String PARSE_STRING = "There was an issue parsing %s which resulted in %s being returned.\n";
    private static final String UNEXPECTED_QUERY_ERROR = "This error is unexpected and should be reported.";

    // Parse error messages
    private static final String TERM_PARSE_ERROR = "Error Parsing Terms";
    private static final String SINGLE_PARSE_ERROR_MSG = "The term below could not correctly be parsed.\n%s" + RECHECK_QUERY;
    private static final String DOUBLE_PARSE_ERROR_MSG = "The terms below could not correctly be parsed.\n%s%s" + RECHECK_QUERY;

    // Operator application error messages
    private static final String OPERATOR_NOT_APPLIED = "Operator could not be applied";
    private static final String OPERATOR_NOT_APPLIED_MSG = "There was a query error: Operator %s could not be applied to the argument type.\n"
            + PARSE_STRING + RECHECK_QUERY;
    private static final String OPERATORS_NOT_APPLIED_MSG = "There was a query error: Operator %s could not be applied to any argument types.\n"
            + PARSE_STRING + PARSE_STRING + RECHECK_QUERY;

    // Operator does not exist error
    private static final String OPERATOR_NOT_FOUND = "Operator Not Found";
    private static final String OPERATOR_ERROR_MSG = "The operator %s cannot be found.\n"
            + "Refer to The Constellation Expressions Framework Help if you need assistance with the query language.";

    static {
        OPERATOR_CLASSES.put(Operator.ADD, Sum.NAME);
        OPERATOR_CLASSES.put(Operator.SUBTRACT, Difference.NAME);
        OPERATOR_CLASSES.put(Operator.MULTIPLY, Product.NAME);
        OPERATOR_CLASSES.put(Operator.DIVIDE, Quotient.NAME);
        OPERATOR_CLASSES.put(Operator.MODULO, Modulus.NAME);
        OPERATOR_CLASSES.put(Operator.EQUALS, Equals.NAME);
        OPERATOR_CLASSES.put(Operator.NOT_EQUALS, NotEquals.NAME);
        OPERATOR_CLASSES.put(Operator.GREATER_THAN, GreaterThan.NAME);
        OPERATOR_CLASSES.put(Operator.GREATER_THAN_OR_EQUALS, GreaterThanOrEquals.NAME);
        OPERATOR_CLASSES.put(Operator.LESS_THAN, LessThan.NAME);
        OPERATOR_CLASSES.put(Operator.LESS_THAN_OR_EQUALS, LessThanOrEquals.NAME);
        OPERATOR_CLASSES.put(Operator.AND, And.NAME);
        OPERATOR_CLASSES.put(Operator.AND_AND, And.NAME);
        OPERATOR_CLASSES.put(Operator.OR, Or.NAME);
        OPERATOR_CLASSES.put(Operator.OR_OR, Or.NAME);
        OPERATOR_CLASSES.put(Operator.EXCLUSIVE_OR, ExclusiveOr.NAME);
        OPERATOR_CLASSES.put(Operator.CONTAINS, Contains.NAME);
        OPERATOR_CLASSES.put(Operator.STARTS_WITH, StartsWith.NAME);
        OPERATOR_CLASSES.put(Operator.ENDS_WITH, EndsWith.NAME);
        OPERATOR_CLASSES.put(Operator.ASSIGN, Assign.NAME);

        CONVERTER_CLASSES.put(Operator.SUBTRACT, Negative.NAME);
        CONVERTER_CLASSES.put(Operator.ADD, Positive.NAME);
        CONVERTER_CLASSES.put(Operator.NOT, Not.NAME);
    }

    private ExpressionCompiler() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static Object compileSequenceExpression(SequenceExpression expression, VariableProvider variableProvider, IntReadable indexReadable, Operators operators) {
        if (expression == null) {
            return null;
        }
        final List<Expression> children = expression.getUnmodifiableChildren();
        switch (children.size()) {
            case 1:
                return compileExpression(children.get(0), variableProvider, indexReadable, operators);
            case 2:
                final OperatorExpression operator = (OperatorExpression) children.get(0);
                final Object right = compileExpression(children.get(1), variableProvider, indexReadable, operators);
                final String operatorName = CONVERTER_CLASSES.get(operator.getOperator());
                final OperatorRegistry registry = operators.getRegistry(operatorName);
                Object result = null;
                final String errorMessage;
                final String errorName;
                if (registry == null) {
                    errorMessage = String.format(OPERATOR_ERROR_MSG, operatorName);
                    errorName = OPERATOR_NOT_FOUND;
                } else {
                    if (right == null) {
                        errorMessage = String.format(SINGLE_PARSE_ERROR_MSG, children.get(0).toString());
                        errorName = TERM_PARSE_ERROR;
                    } else {
                        result = registry.apply(right);
                        if (result == null) {
                            errorMessage = String.format(OPERATOR_NOT_APPLIED_MSG, operatorName, children.get(1).toString(), right);
                            errorName = OPERATOR_NOT_APPLIED;
                        } else {
                            errorMessage = null;
                            errorName = null;
                        }
                    }
                }
                if (errorMessage != null) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR, errorName, errorMessage, Alert.AlertType.ERROR));
                    LOGGER.log(Level.WARNING, errorMessage);
                }
                return result;
            case 3:
                final Object left = compileExpression(children.get(0), variableProvider, indexReadable, operators);
                final OperatorExpression secondOperator = (OperatorExpression) children.get(1);
                final Object right2 = compileExpression(children.get(2), variableProvider, indexReadable, operators);
                final String operatorName2 = OPERATOR_CLASSES.get(secondOperator.getOperator());
                final OperatorRegistry registry2 = operators.getRegistry(operatorName2);
                Object result2 = null;
                final String errorMessage2;
                final String errorName2;
                if (registry2 == null) {
                    errorMessage2 = String.format(OPERATOR_ERROR_MSG, operatorName2);
                    errorName2 = OPERATOR_NOT_FOUND;
                } else {
                    if (left == null && right2 != null) {
                        errorMessage2 = String.format(SINGLE_PARSE_ERROR_MSG, children.get(0).toString());
                        errorName2 = TERM_PARSE_ERROR;
                    } else if (right2 == null && left != null) {
                        errorMessage2 = String.format(SINGLE_PARSE_ERROR_MSG, children.get(2).toString());
                        errorName2 = TERM_PARSE_ERROR;
                    } else if (right2 == null) {
                        // left always evaluates to null here
                        errorMessage2 = String.format(DOUBLE_PARSE_ERROR_MSG, children.get(0).toString(), children.get(2).toString());
                        errorName2 = TERM_PARSE_ERROR;
                    } else {
                        result2 = registry2.apply(left, right2);
                        if (result2 == null) {
                            errorMessage2 = String.format(OPERATORS_NOT_APPLIED_MSG, operatorName2, children.get(0).toString(), left, children.get(2).toString(), right2);
                            errorName2 = OPERATOR_NOT_APPLIED;
                        } else {
                            errorMessage2 = null;
                            errorName2 = null;
                        }
                    }
                }
                if (errorMessage2 != null) {
                    Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR, errorName2, errorMessage2, Alert.AlertType.ERROR));
                    LOGGER.log(Level.WARNING, errorMessage2);
                }
                return result2;
            default:
                Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR, MALFORMED_QUERY,
                        String.format("Invalid expression size: ", children.size()), Alert.AlertType.ERROR));
                LOGGER.log(Level.WARNING, "There was a query error: Invalid expression size: {0}", children.size());
                return null;
        }
    }

    private static Object compileExpression(Expression expression, VariableProvider variableProvider, IntReadable indexReadable, Operators operators) {
        if (expression instanceof SequenceExpression) {
            return compileSequenceExpression((SequenceExpression) expression, variableProvider, indexReadable, operators);
        } else if (expression instanceof VariableExpression) {
            final String variableName = ((VariableExpression) expression).getContent();
            final Object variable = variableProvider.getVariable(variableName, indexReadable);
            if (variable == null) {
                return null;
            }
            return variable;
        } else if (expression instanceof StringExpression) {
            final String content = ((StringExpression) expression).getContent();
            return (StringConstant) () -> content;
        } else {
            Platform.runLater(() -> NotifyDisplayer.displayAlert(QUERY_ERROR, MALFORMED_QUERY, UNEXPECTED_QUERY_ERROR, Alert.AlertType.ERROR));
            LOGGER.log(Level.SEVERE, QUERY_ERROR + UNEXPECTED_QUERY_ERROR);
            return null;
        }
    }
}
