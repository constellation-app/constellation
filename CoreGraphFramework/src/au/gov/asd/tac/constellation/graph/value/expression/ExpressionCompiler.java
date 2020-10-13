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
package au.gov.asd.tac.constellation.graph.value.expression;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class ExpressionCompiler {

    private static final Map<Operator, String> OPERATOR_CLASSES = new EnumMap<>(Operator.class);
    private static final Map<Operator, String> CONVERTER_CLASSES = new EnumMap<>(Operator.class);

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
        final List<Expression> children = expression.getUnmodifiableChildren();
        switch (children.size()) {
            case 1:
                return compileExpression(children.get(0), variableProvider, indexReadable, operators);

            case 2: {
                final OperatorExpression operator = (OperatorExpression) children.get(0);
                final Object right = compileExpression(children.get(1), variableProvider, indexReadable, operators);
                final String operatorName = CONVERTER_CLASSES.get(operator.getOperator());
                final Object result = operators.getRegistry(operatorName).apply(right);
                if (result == null) {
                    throw new ExpressionException("Unable to find implementation of operator: " + operatorName + " (" + right.getClass().getCanonicalName() + ")");
                }
                return result;
            }

            case 3: {
                final Object left = compileExpression(children.get(0), variableProvider, indexReadable, operators);
                final OperatorExpression operator = (OperatorExpression) children.get(1);
                final Object right = compileExpression(children.get(2), variableProvider, indexReadable, operators);
                final String operatorName = OPERATOR_CLASSES.get(operator.getOperator());
                final Object result = operators.getRegistry(operatorName).apply(left, right);
                if (result == null) {
                    throw new ExpressionException("Unable to find implementation of operator: " + operatorName + " (" + left.getClass().getCanonicalName() + ", " + right.getClass().getCanonicalName() + ")");
                }
                return result;
            }

            default:
                throw new IllegalArgumentException("Invalid expression size: " + children.size());
        }
    }

    private static Object compileExpression(Expression expression, VariableProvider variableProvider, IntReadable indexReadable, Operators operators) {
        if (expression instanceof SequenceExpression) {
            return compileSequenceExpression((SequenceExpression) expression, variableProvider, indexReadable, operators);
        } else if (expression instanceof VariableExpression) {
            final String variableName = ((VariableExpression) expression).getContent();
            final Object variable = variableProvider.getVariable(variableName, indexReadable);
            if (variable == null) {
                throw new ExpressionException("Unknown variable: " + variableName);
            }
            return variable;
        } else if (expression instanceof StringExpression) {
            final String content = ((StringExpression) expression).getContent();
            return (StringConstant) () -> content;
        } else {
            throw new IllegalArgumentException("Should never get here");
        }
    }
}
