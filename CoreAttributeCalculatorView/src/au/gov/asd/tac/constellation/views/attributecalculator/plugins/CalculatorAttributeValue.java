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
package au.gov.asd.tac.constellation.views.attributecalculator.plugins;

import au.gov.asd.tac.constellation.views.attributecalculator.utilities.AbstractCalculatorValue;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import org.python.core.PyList;

/**
 *
 * @author twilight_sparkle
 */
public class CalculatorAttributeValue extends AbstractCalculatorValue {

    private final String attributeName;
    private final AttributeOf attributeOf;
    private final GraphElementType elementType;

    private enum AttributeOf {

        SOURCE_NODE("source_"),
        DEST_NODE("dest_"),
        THIS_ELEMENT("");

        private final String prefix;

        private AttributeOf(final String prefix) {
            this.prefix = prefix;
        }

        public static AttributeOf getFromPrefix(final String prefix) {
            for (AttributeOf attrOf : AttributeOf.values()) {
                if (attrOf.prefix.equals(prefix)) {
                    return attrOf;
                }
            }
            return null;
        }
    }

    // We get all attributes in their object representation, but for the benefit of
    // the user it is easier in python to work with temporal attributes as
    // integer numbers of milliseconds since epoch.
    private static Object handleSpecialAttributeTypes(Object obj) {
        if (obj != null) {
            if (obj instanceof ZonedDateTime) {
                return ((ZonedDateTime) obj).toEpochSecond() * 1000;
            } else if (obj instanceof LocalDate) {
                return ((LocalDate) obj).toEpochDay() * (24 * 3600 * 1000);
            } else if (obj instanceof LocalTime) {
                return ((LocalTime) obj).get(ChronoField.MILLI_OF_DAY);
            } else if (obj instanceof LocalDateTime) {
                return ((LocalDateTime) obj).toInstant(ZoneOffset.UTC).toEpochMilli();
            }
        }
        return obj;
    }

    public CalculatorAttributeValue(final String attributeName, final String prefix, final GraphElementType elementType) {
        this.attributeName = attributeName;
        this.attributeOf = AttributeOf.getFromPrefix(prefix);
        this.elementType = elementType;
    }

    @Override
    public void updateValue(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (!checkElementType(elementType)) {
            return;
        }
        // If the element type is a vertex or this is a source/dest attribute, set the attributeElementType to vertex. Otherwise attributeElementType is transaction.
        final GraphElementType attributeElementType = elementType == GraphElementType.VERTEX || !attributeOf.equals(AttributeOf.THIS_ELEMENT) ? GraphElementType.VERTEX : GraphElementType.TRANSACTION;
        final int attrId = graph.getAttribute(attributeElementType, attributeName);
        final boolean isTemporal = isTemporalAttribute(graph, attrId);
        switch (attributeOf) {
            case SOURCE_NODE:
                if (elementType.equals(GraphElementType.TRANSACTION)) {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getTransactionSourceVertex(elementId)) : graph.getObjectValue(attrId, graph.getTransactionSourceVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getTransactionSourceVertex(elementId))));
                } else if (elementType.equals(GraphElementType.EDGE)) {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getEdgeSourceVertex(elementId)) : graph.getObjectValue(attrId, graph.getEdgeSourceVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getEdgeSourceVertex(elementId))));
                } else {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getLinkLowVertex(elementId)) : graph.getObjectValue(attrId, graph.getLinkLowVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getLinkLowVertex(elementId))));
                }
                break;
            case DEST_NODE:
                if (elementType.equals(GraphElementType.TRANSACTION)) {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getTransactionDestinationVertex(elementId)) : graph.getObjectValue(attrId, graph.getTransactionDestinationVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getTransactionDestinationVertex(elementId))));
                } else if (elementType.equals(GraphElementType.EDGE)) {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getEdgeDestinationVertex(elementId)) : graph.getObjectValue(attrId, graph.getEdgeDestinationVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getEdgeDestinationVertex(elementId))));
                } else {
//                    val = isTemporal ? graph.getLongValue(attrId, graph.getLinkHighVertex(elementId)) : graph.getObjectValue(attrId, graph.getLinkHighVertex(elementId));
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getLinkHighVertex(elementId))));
                }
                break;
            case THIS_ELEMENT:
                if (elementType.equals(GraphElementType.TRANSACTION) || elementType.equals(GraphElementType.VERTEX)) {
//                    val = isTemporal ? graph.getLongValue(attrId, elementId) : graph.getObjectValue(attrId, elementId);
                    val = convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, elementId)));
                } else if (elementType.equals(GraphElementType.EDGE)) {
                    List<Object> transactionAttrVals = new LinkedList<>();
                    for (int i = 0; i < graph.getEdgeTransactionCount(elementId); i++) {
//                        transactionAttrVals.add(isTemporal ? graph.getLongValue(attrId, graph.getEdgeTransaction(elementId, i)) : graph.getObjectValue(attrId, graph.getEdgeTransaction(elementId, i)));
                        transactionAttrVals.add(convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getEdgeTransaction(elementId, i)))));
                    }
                    val = new PyList(transactionAttrVals);
                } else {
                    List<Object> transactionAttrVals = new LinkedList<>();
                    for (int i = 0; i < graph.getLinkTransactionCount(elementId); i++) {
//                        transactionAttrVals.add(isTemporal ? graph.getLongValue(attrId, graph.getLinkTransaction(elementId, i)) : graph.getObjectValue(attrId, graph.getLinkTransaction(elementId, i)));
                        transactionAttrVals.add(convertNullsToObliterator(handleSpecialAttributeTypes(graph.getObjectValue(attrId, graph.getLinkTransaction(elementId, i)))));
                    }
                    val = new PyList(transactionAttrVals);
                }
                break;
        }
    }

//    @Override
//    public Object val() {
//        if (val == INVALID_IN_NODE_CONTEXT) {
//            throw new RuntimeException("Attribute Calculator Error: using transaction variable in node context");
//        } else if (val == INVALID_IN_TRANSACTION_CONTEXT) {
//            throw new RuntimeException("Attribute Calculator Error: using node variable in transaction context");
//        }
//        return val;
//    }
    public boolean checkElementType(final GraphElementType elementType) {
        if (this.elementType == null) {
            return true;
        }
        if (this.elementType == GraphElementType.VERTEX && elementType != GraphElementType.VERTEX) {
            val = INVALID_IN_TRANSACTION_CONTEXT;
            return false;
        } else if (this.elementType != GraphElementType.VERTEX && elementType == GraphElementType.VERTEX) {
            val = INVALID_IN_NODE_CONTEXT;
            return false;
        }
        return true;
    }

    public boolean isTemporalAttribute(final GraphReadMethods graph, final int attrID) {
        final Class<?> type = graph.getAttributeDataType(attrID);
        if (type.equals(DateAttributeDescription.class) || type.equals(ZonedDateTimeAttributeDescription.class) || type.equals(TimeAttributeDescription.class)) {
            return true;
        }
        return false;
    }
}
