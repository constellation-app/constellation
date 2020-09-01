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
package au.gov.asd.tac.constellation.views.layers.layer;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttributeUtilities;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.query.QueryEvaluator;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;

enum Operator {
    EQUALS,
    NOTEQUALS,
    GREATERTHAN,
    LESSTHAN,
    GREATERTHANOREQ,
    LESSTHANOREQ,
    NOTFOUND
}

/**
 *
 * @author aldebaran30701
 */
public class LayerEvaluator {

    /**
     * Evaluates a string representation of a query in relation to a single
     * element on the graph returns true when the node satisfies the condition,
     * false otherwise.
     * <p>
     * A query should be structured as follows:
     * vertex_color:<=:0,0,0 vertex_selected:==:true vertex_color:>:255,255,255
     *
     * @param elementType the type of the element
     * @param elementId the id of the element
     * @param query the query expression
     * @return a boolean determining whether the element satisfied the
     * constraints.
     */
    public static boolean evaluateLayerQuery(final GraphWriteMethods graph, final GraphElementType elementType, final int elementId, final List<String> queries) {
        // exit condition for show all
        if (queries.isEmpty()) {
            return true;
        }

        // iterate over rules
        String evaluatedResult = "";
        Operator currentOperand = Operator.NOTFOUND;
        boolean finalResult = true;
        boolean ignoreResult = false;
        for (final String rule : queries) {
            String[] ruleSegments = rule.split(" == | != | < | > ");
            if (rule.contains(" == ")) {
                currentOperand = Operator.EQUALS;
            } else if (rule.contains(" != ")) {
                currentOperand = Operator.NOTEQUALS;
            } else if (rule.contains(" > ")) {
                currentOperand = Operator.GREATERTHAN;
            } else if (rule.contains(" < ")) {
                currentOperand = Operator.LESSTHAN;
            }

            // iterate over each segment and grab the values
            AttributeDescription attributeDescription = null;
            int attributeId = 0;
            //int attributeId = graph.getAttribute(elementType, ruleSegment);
            int segmentCount = 0;
            for (final String ruleSegment : ruleSegments) {
                segmentCount++;
                switch (segmentCount) {
                    case 1: {
                        attributeId = graph.getAttribute(elementType, ruleSegment);
                        // if (attributeId != Graph.NOT_FOUND) {
                        //graph.getStringValue(attributaeId, elementId)
                        //attributeDescription = graph.getAttributeDataType(attributaeId);
                        //}
                        break;
                    }
                    case 2: {
                        switch (currentOperand) {
                            case EQUALS: {
                                if (attributeId == Graph.NOT_FOUND) {
                                    //NotifyDisplayer.display("Not a valid rule.", NotifyDescriptor.WARNING_MESSAGE);
                                } else {
                                    final String value = graph.getStringValue(attributeId, elementId);
                                    finalResult = (StringUtils.isNotEmpty(value) && value.equals(ruleSegment));
                                }

//                                finalResult = (attributeDescription != null && attributeDescription.getString(elementId) != null
//                                        && (attributeDescription.getString(elementId)).equals(ruleSegment));
                                break;
                            }
                            case NOTEQUALS: {
                                if (attributeId == Graph.NOT_FOUND) {
                                    //NotifyDisplayer.display("Not a valid rule.", NotifyDescriptor.WARNING_MESSAGE);
                                } else {
                                    final String value = graph.getStringValue(attributeId, elementId);
                                    finalResult = (StringUtils.isNotEmpty(value) && !value.equals(ruleSegment));
//                                finalResult = (attributeDescription != null && attributeDescription.getString(elementId) != null
//                                        && !(attributeDescription.getString(elementId)).equals(ruleSegment));
                                }
                                break;
                            }
                            case GREATERTHAN: {
                                if (attributeId == Graph.NOT_FOUND) {
                                    //NotifyDisplayer.display("Not a valid rule.", NotifyDescriptor.WARNING_MESSAGE);
                                } else {
                                    final String value = graph.getStringValue(attributeId, elementId);
                                    finalResult = (StringUtils.isNotEmpty(value) && value.compareTo(ruleSegment) > 0);
//                                finalResult = (attributeDescription != null && attributeDescription.getString(elementId) != null
//                                        && (attributeDescription.getString(elementId)).compareTo(ruleSegment) > 0);
                                }
                                break;
                            }
                            case LESSTHAN: {
                                if (attributeId == Graph.NOT_FOUND) {
                                    //NotifyDisplayer.display("Not a valid rule.", NotifyDescriptor.WARNING_MESSAGE);
                                } else {
                                    final String value = graph.getStringValue(attributeId, elementId);
                                    finalResult = (StringUtils.isNotEmpty(value) && value.compareTo(ruleSegment) < 0);
//                                finalResult = (attributeDescription != null && attributeDescription.getString(elementId) != null
//                                        && (attributeDescription.getString(elementId)).compareTo(ruleSegment) < 0);
                                }
                                break;
                            }
                            case NOTFOUND: {
                                NotifyDisplayer.display("Not a valid Operand", NotifyDescriptor.WARNING_MESSAGE);
                                finalResult = false;
                                break;
                            }
                            default:
                                break;
                        }
                        break;
                    }
                    default:
                        break;
                }

                // build the string to evaluate
                if (ruleSegment.equals("&&")) {
                    evaluatedResult += "&&" + SeparatorConstants.COLON;
                    ignoreResult = true;
                } else if (ruleSegment.equals("||")) {
                    evaluatedResult += "||" + SeparatorConstants.COLON;
                    ignoreResult = true;
                }
            }

            // when you need to ignore the result and just use an operator || &&
            if (ignoreResult) {
                ignoreResult = false;
            } else {
                evaluatedResult += finalResult + SeparatorConstants.COLON;
            }
        }

        evaluatedResult = evaluatedResult.length() == 0 ? evaluatedResult
                : evaluatedResult.substring(0, evaluatedResult.length() - 1);
        final String[] resultComponents = evaluatedResult.split(SeparatorConstants.COLON);

        String expression = "";
        for (final String resultComponent : resultComponents) {
            expression += resultComponent + " ";
        }
        expression = expression.substring(0, expression.length() - 1);

        return QueryEvaluator.evaluatePostfix(expression);
    }

    public static List<SchemaAttribute> getQueryAttributes(final GraphWriteMethods graph, final String query) {
        final List<SchemaAttribute> attributeList = new ArrayList<>();

        for (String attribute : QueryEvaluator.retrieveAttributeNames(query)) {

            String[] ruleSegments = attribute.split(" == | != | < | > ");

            // iterate over each segment and grab the values
            int segmentCount = 0;
            for (final String ruleSegment : ruleSegments) {
                segmentCount++;
                switch (segmentCount) {
                    case 1: {
                        if (!ruleSegment.contains("||") && !ruleSegment.contains("&&")) {
                            attributeList.add(SchemaAttributeUtilities.getAttribute(GraphElementType.VERTEX, ruleSegment));
                            attributeList.add(SchemaAttributeUtilities.getAttribute(GraphElementType.TRANSACTION, ruleSegment));
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        // remove nulls
        attributeList.removeIf(item -> item == null);
        return attributeList;
    }

}
