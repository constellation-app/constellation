/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * Copy Graph Utilities
 *
 * @author arcturus
 */
public class CopyGraphUtilities {
    
    private CopyGraphUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Copy the primary keys from a source graph and copy it to a new graph.
     *
     * @param original Original graph
     * @param graph New graph
     * @param elementType The graph element type
     */
    public static void copyPrimaryKeys(final GraphReadMethods original, final StoreGraph graph, final GraphElementType elementType) {
        final int[] originalPrimaryKeys = original.getPrimaryKey(elementType);
        final int[] newPrimaryKeys = new int[originalPrimaryKeys.length];
        for (int i = 0; i < originalPrimaryKeys.length; i++) {
            final Attribute attribute = new GraphAttribute(original, originalPrimaryKeys[i]);
            int newAttributeId = graph.getAttribute(elementType, attribute.getName());
            if (newAttributeId == Graph.NOT_FOUND) {
                newAttributeId = graph.addAttribute(elementType, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(), attribute.getDefaultValue(), null);
            }
            newPrimaryKeys[i] = newAttributeId;
        }
        graph.setPrimaryKey(elementType, newPrimaryKeys);
    }

    /**
     * Copy a graph attribute and its values to a new graph
     *
     * @param original Original graph
     * @param graph New graph
     */
    public static void copyGraphTypeElements(final GraphReadMethods original, final GraphWriteMethods graph) {
        final int attributeCount = original.getAttributeCount(GraphElementType.GRAPH);

        for (int attributePosition = 0; attributePosition < attributeCount; attributePosition++) {
            int originalAttributeId = original.getAttribute(GraphElementType.GRAPH, attributePosition);
            final Attribute attribute = new GraphAttribute(original, originalAttributeId);

            int newAttributeId = graph.getAttribute(GraphElementType.GRAPH, attribute.getName());
            if (newAttributeId == Graph.NOT_FOUND) {
                newAttributeId = graph.addAttribute(GraphElementType.GRAPH, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(), attribute.getDefaultValue(), null);
            }
            graph.setObjectValue(newAttributeId, 0, original.getObjectValue(originalAttributeId, 0));
        }
    }

    /**
     * Adapted from CopySelectedElementsPlugin.makeGraph
     *
     * @param original - the Graph to copy elements from
     * @param graph - the Graph to copy elements to
     * @param copyAll - whether to copy all elements or just the selected ones
     */
    public static void copyGraphToGraph(final GraphReadMethods original, final GraphWriteMethods graph, final boolean copyAll) {
        final int vertexSelected = original.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        final int transactionSelected = original.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        final int[] attributeTranslation = new int[original.getAttributeCapacity()];

        // Copy the attributes.
        for (final GraphElementType type : GraphElementType.values()) {
            final int attributeCount = original.getAttributeCount(type);
            for (int attributePosition = 0; attributePosition < attributeCount; attributePosition++) {
                int originalAttributeId = original.getAttribute(type, attributePosition);
                final Attribute attribute = new GraphAttribute(original, originalAttributeId);
                int newAttributeId = graph.getAttribute(type, attribute.getName());
                if (newAttributeId == Graph.NOT_FOUND) {
                    newAttributeId = graph.addAttribute(type, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(), attribute.getDefaultValue(), null);
                }
                attributeTranslation[originalAttributeId] = newAttributeId;
                if (type == GraphElementType.GRAPH) {
                    graph.setObjectValue(newAttributeId, 0, original.getObjectValue(originalAttributeId, 0));
                }
            }
        }

        // Copy the vertices.
        final int[] vertexTranslation = new int[original.getVertexCapacity()];
        for (int position = 0; position < original.getVertexCount(); position++) {
            final int originalVertex = original.getVertex(position);
            if (copyAll || vertexSelected == Graph.NOT_FOUND || original.getBooleanValue(vertexSelected, originalVertex)) {
                final int newVertex = graph.addVertex();
                vertexTranslation[originalVertex] = newVertex;
                for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.VERTEX); attributePosition++) {
                    final int originalAttributeId = original.getAttribute(GraphElementType.VERTEX, attributePosition);
                    final int newAttributeId = attributeTranslation[originalAttributeId];
                    graph.setObjectValue(newAttributeId, newVertex, original.getObjectValue(originalAttributeId, originalVertex));
                }
            }
        }

        // Copy the transactions.
        final int[] edgeTranslation = new int[original.getEdgeCapacity()];
        final int[] linkTranslation = new int[original.getLinkCapacity()];
        for (int position = 0; position < original.getTransactionCount(); position++) {
            final int originalTransaction = original.getTransaction(position);
            if (!copyAll && ((transactionSelected != Graph.NOT_FOUND && !original.getBooleanValue(transactionSelected, originalTransaction))
                    || (vertexSelected != Graph.NOT_FOUND
                    && (!original.getBooleanValue(vertexSelected, original.getTransactionSourceVertex(originalTransaction))
                    || !original.getBooleanValue(vertexSelected, original.getTransactionDestinationVertex(originalTransaction)))))) {
                continue;
            }
            final int sourceVertex = vertexTranslation[original.getTransactionSourceVertex(originalTransaction)];
            final int destinationVertex = vertexTranslation[original.getTransactionDestinationVertex(originalTransaction)];
            final boolean directed = original.getTransactionDirection(originalTransaction) < 2;
            final int newTransaction = graph.addTransaction(sourceVertex, destinationVertex, directed);

            // add the edge translations
            final int originalEdge = original.getTransactionEdge(originalTransaction);
            final int newEdge = graph.getTransactionEdge(newTransaction);
            edgeTranslation[originalEdge] = newEdge;

            // add the link translations
            final int originalLink = original.getTransactionLink(originalTransaction);
            final int newLink = graph.getTransactionLink(newTransaction);
            linkTranslation[originalLink] = newLink;

            for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.TRANSACTION); attributePosition++) {
                final int originalAttributeId = original.getAttribute(GraphElementType.TRANSACTION, attributePosition);
                final int newAttributeId = attributeTranslation[originalAttributeId];
                graph.setObjectValue(newAttributeId, newTransaction, original.getObjectValue(originalAttributeId, originalTransaction));
            }
        }

        // Copy the edges
        for (int position = 0; position < original.getEdgeCount(); position++) {
            final int originalEdge = original.getEdge(position);
            if (!copyAll && ((transactionSelected != Graph.NOT_FOUND && !original.getBooleanValue(transactionSelected, originalEdge))
                    || (vertexSelected != Graph.NOT_FOUND
                    && (!original.getBooleanValue(vertexSelected, original.getEdgeSourceVertex(originalEdge))
                    || !original.getBooleanValue(vertexSelected, original.getEdgeDestinationVertex(originalEdge)))))) {
                continue;
            }

            for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.EDGE); attributePosition++) {
                final int originalAttributeId = original.getAttribute(GraphElementType.EDGE, attributePosition);
                final int newAttributeId = attributeTranslation[originalAttributeId];
                graph.setObjectValue(newAttributeId, edgeTranslation[originalEdge], original.getObjectValue(originalAttributeId, originalEdge));
            }
        }

        // Copy the links
        for (int position = 0; position < original.getLinkCount(); position++) {
            final int originalLink = original.getLink(position);
            if (!copyAll && ((transactionSelected != Graph.NOT_FOUND && !original.getBooleanValue(transactionSelected, originalLink))
                    || (vertexSelected != Graph.NOT_FOUND
                    && (!original.getBooleanValue(vertexSelected, original.getEdgeSourceVertex(originalLink))
                    || !original.getBooleanValue(vertexSelected, original.getEdgeDestinationVertex(originalLink)))))) {
                continue;
            }

            for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.LINK); attributePosition++) {
                final int originalAttributeId = original.getAttribute(GraphElementType.LINK, attributePosition);
                final int newAttributeId = attributeTranslation[originalAttributeId];
                graph.setObjectValue(newAttributeId, linkTranslation[originalLink], original.getObjectValue(originalAttributeId, originalLink));
            }
        }
    }
}
