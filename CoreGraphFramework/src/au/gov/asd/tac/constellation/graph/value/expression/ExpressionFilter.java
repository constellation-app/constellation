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

import au.gov.asd.tac.constellation.graph.value.IndexedReadable;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.Expression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.Operator;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.OperatorExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.StringExpression;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.VariableExpression;
import au.gov.asd.tac.constellation.graph.value.readables.And;
import au.gov.asd.tac.constellation.graph.value.readables.Contains;
import au.gov.asd.tac.constellation.graph.value.readables.Difference;
import au.gov.asd.tac.constellation.graph.value.readables.EndsWith;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.ExclusiveOr;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Modulus;
import au.gov.asd.tac.constellation.graph.value.readables.Negative;
import au.gov.asd.tac.constellation.graph.value.readables.Not;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Or;
import au.gov.asd.tac.constellation.graph.value.readables.Positive;
import au.gov.asd.tac.constellation.graph.value.readables.Product;
import au.gov.asd.tac.constellation.graph.value.readables.Quotient;
import au.gov.asd.tac.constellation.graph.value.readables.StartsWith;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class ExpressionFilter {

    private static final Map<Operator, Class<?>> OPERATOR_CLASSES = new EnumMap<>(Operator.class);
    private static final Map<Operator, Class<?>> CONVERTER_CLASSES = new EnumMap<>(Operator.class);

    static {
        OPERATOR_CLASSES.put(Operator.ADD, Sum.class);
        OPERATOR_CLASSES.put(Operator.SUBTRACT, Difference.class);
        OPERATOR_CLASSES.put(Operator.MULTIPLY, Product.class);
        OPERATOR_CLASSES.put(Operator.DIVIDE, Quotient.class);
        OPERATOR_CLASSES.put(Operator.MODULO, Modulus.class);
        OPERATOR_CLASSES.put(Operator.EQUALS, Equals.class);
        OPERATOR_CLASSES.put(Operator.NOT_EQUALS, NotEquals.class);
        OPERATOR_CLASSES.put(Operator.GREATER_THAN, GreaterThan.class);
        OPERATOR_CLASSES.put(Operator.GREATER_THAN_OR_EQUALS, GreaterThanOrEquals.class);
        OPERATOR_CLASSES.put(Operator.LESS_THAN, LessThan.class);
        OPERATOR_CLASSES.put(Operator.LESS_THAN_OR_EQUALS, LessThanOrEquals.class);
        OPERATOR_CLASSES.put(Operator.AND, And.class);
        OPERATOR_CLASSES.put(Operator.OR, Or.class);
        OPERATOR_CLASSES.put(Operator.EXCLUSIVE_OR, ExclusiveOr.class);
        OPERATOR_CLASSES.put(Operator.CONTAINS, Contains.class);
        OPERATOR_CLASSES.put(Operator.STARTS_WITH, StartsWith.class);
        OPERATOR_CLASSES.put(Operator.ENDS_WITH, EndsWith.class);

        CONVERTER_CLASSES.put(Operator.SUBTRACT, Negative.class);
        CONVERTER_CLASSES.put(Operator.ADD, Positive.class);
        CONVERTER_CLASSES.put(Operator.NOT, Not.class);
    }

    private ExpressionFilter() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static IndexedReadable<?> createExpressionReadable(final SequenceExpression expression, final IndexedReadableProvider indexedReadableProvider, 
            final ConverterRegistry converterRegistry) {
        final List<Expression> children = expression.getUnmodifiableChildren();
        switch (children.size()) {
            case 1 -> {
                final IndexedReadable<?> indexedReadable = createIndexedReadable(children.get(0), indexedReadableProvider, converterRegistry);
                if (indexedReadable == null) {
                    throw new IllegalArgumentException("Invalid expression size: " + children.size());
                }
                return indexedReadable;
            }
            case 2 -> {
                final OperatorExpression operator = (OperatorExpression) children.get(0);
                final Expression right = children.get(1);
                final Class converterClass = CONVERTER_CLASSES.get(operator.getOperator());
                final IndexedReadable<?> rightIndexedReadable = createIndexedReadable(right, indexedReadableProvider, converterRegistry);
                if (rightIndexedReadable == null) {
                    throw new IllegalArgumentException("Unable to perform unary operation on constant");
                }
                return Filter.createFilter(rightIndexedReadable, converterClass, converterRegistry);
            }
            case 3 -> {
                final Expression left = children.get(0);
                final OperatorExpression operator2 = (OperatorExpression) children.get(1);
                final Expression right2 = children.get(2);

                final Class operatorClass = OPERATOR_CLASSES.get(operator2.getOperator());

                final IndexedReadable<?> leftIndexedReadable = createIndexedReadable(left, indexedReadableProvider, converterRegistry);
                final IndexedReadable<?> rightIndexedReadable2 = createIndexedReadable(right2, indexedReadableProvider, converterRegistry);

                if (leftIndexedReadable == null) {
                    final String leftContent = ((StringExpression) left).getContent();
                    if (rightIndexedReadable2 == null) {
                        throw new IllegalArgumentException("Unable to perform operator on 2 constants");
                    }
                    return Filter.createFilter(leftContent, rightIndexedReadable2, operatorClass, converterRegistry);
                } else if (rightIndexedReadable2 == null) {
                    final String rightContent = ((StringExpression) right2).getContent();
                    return Filter.createFilter(leftIndexedReadable, rightContent, operatorClass, converterRegistry);
                } else {
                    return Filter.createFilter(leftIndexedReadable, rightIndexedReadable2, operatorClass, converterRegistry);
                }
            }
            default -> throw new IllegalArgumentException("Invalid expression size: " + children.size());
        }
    }

    private static IndexedReadable<?> createIndexedReadable(final Expression expression, final IndexedReadableProvider indexedReadableProvider, 
            final ConverterRegistry converterRegistry) {
        return switch (expression) {
            case SequenceExpression sequenceExpression -> 
                createExpressionReadable(sequenceExpression, indexedReadableProvider, converterRegistry);
            case VariableExpression variableExpression -> {
                final String variableName = variableExpression.getContent();
                final IndexedReadable<?> indexedReadable = indexedReadableProvider.getIndexedReadable(variableName);
                if (indexedReadable == null) {
                    throw new IllegalArgumentException("Unknown variable: " + variableName);
                }
                yield indexedReadable;
            }
            default -> null;
        };
    }
}
