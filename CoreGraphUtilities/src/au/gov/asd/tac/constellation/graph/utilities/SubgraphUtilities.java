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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.utilities.io.CopyGraphUtilities;
import java.util.Set;

/**
 * Provides functionality for creating subgraphs of a given graph
 *
 * @author cygnus_x-1
 * @author arcturus
 */
public class SubgraphUtilities {
    
    private SubgraphUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Make an exact copy of the given graph.
     *
     * @param graph The source graph
     * @return An exact copy of the graph
     */
    public static StoreGraph copyGraph(final GraphReadMethods graph) {
        final StoreGraph subgraph = new StoreGraph(graph.getSchema());

        // add the graph attributes
        CopyGraphUtilities.copyGraphTypeElements(graph, subgraph);

        // add vertex attributes
        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
            final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);

            if (subgraph.getAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeName(vertexAttributeId)) == Graph.NOT_FOUND) {
                subgraph.addAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeType(vertexAttributeId),
                        graph.getAttributeName(vertexAttributeId), graph.getAttributeDescription(vertexAttributeId), graph.getAttributeDefaultValue(vertexAttributeId), null);
            }
        }

        // for each vertex
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);

            // copy vertex
            final int newVertexId = subgraph.vertexExists(vertexId) ? vertexId : subgraph.addVertex(vertexId);

            // copy vertex attribute values
            for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
                final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);

                final int newVertexAttributeId = subgraph.getAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeName(vertexAttributeId));

                final Object sourceVertexAttributeValue = graph.getObjectValue(vertexAttributeId, vertexId);
                subgraph.setObjectValue(newVertexAttributeId, newVertexId, sourceVertexAttributeValue);
            }
        }

        // add transaction attributes
        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
            final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);

            if (subgraph.getAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeName(transactionAttributeId)) == Graph.NOT_FOUND) {
                subgraph.addAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeType(transactionAttributeId),
                        graph.getAttributeName(transactionAttributeId), graph.getAttributeDescription(transactionAttributeId), graph.getAttributeDefaultValue(transactionAttributeId), null);
            }
        }

        // for each transaction
        final int transactionCount = graph.getTransactionCount();
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            final int transactionId = graph.getTransaction(transactionPosition);
            final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
            final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);

            // copy transaction
            final boolean transactionDirected = graph.getTransactionDirection(transactionId) != Graph.FLAT;
            final int newTransactionId = subgraph.addTransaction(transactionId, sourceVertexId, destinationVertexId, transactionDirected);

            // copy transaction attribute values
            for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);

                final int newTransactionAttributeId = subgraph.getAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeName(transactionAttributeId));

                final Object transactionAttributeValue = graph.getObjectValue(transactionAttributeId, transactionId);
                subgraph.setObjectValue(newTransactionAttributeId, newTransactionId, transactionAttributeValue);
            }
        }

        // copy the primary keys over to the new sub graph
        CopyGraphUtilities.copyPrimaryKeys(graph, subgraph, GraphElementType.VERTEX);
        CopyGraphUtilities.copyPrimaryKeys(graph, subgraph, GraphElementType.TRANSACTION);

        return subgraph;
    }

    /**
     * Return a new graph which represents a subgraph of the given graph
     * filtered by transaction type.
     *
     * @param graph
     * @param schema
     * @param types
     * @param isExclusive
     * @return
     */
    public static StoreGraph getSubgraph(final GraphReadMethods graph, final Schema schema, final Set<SchemaTransactionType> types, final boolean isExclusive) {
        final StoreGraph subgraph = new StoreGraph(schema);

        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        assert transactionTypeAttributeId != GraphConstants.NOT_FOUND : "The transaction 'Type' attribute does not exist on this graph";

        // for each transaction
        final int transactionCount = graph.getTransactionCount();
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            final int transactionId = graph.getTransaction(transactionPosition);

            // check transaction is of desired type
            final SchemaTransactionType transactionType = graph.getObjectValue(transactionTypeAttributeId, transactionId);
            for (SchemaTransactionType type : types) {
                if ((!isExclusive && ((transactionType == null && type == null) || (transactionType != null && transactionType.isSubTypeOf(type))))
                        || (isExclusive && !((transactionType == null && type == null) || (transactionType != null && transactionType.isSubTypeOf(type))))) {
                    final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                    final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);

                    // copy vertices
                    final int newSourceVertexId = subgraph.vertexExists(sourceVertexId) ? sourceVertexId : subgraph.addVertex(sourceVertexId);
                    final int newDestinationVertexId = subgraph.vertexExists(destinationVertexId) ? destinationVertexId : subgraph.addVertex(destinationVertexId);

                    // copy vertex attributes
                    final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
                    for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
                        final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);

                        final int newVertexAttributeId;
                        if (subgraph.getAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeName(vertexAttributeId)) != Graph.NOT_FOUND) {
                            newVertexAttributeId = subgraph.getAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeName(vertexAttributeId));
                        } else {
                            newVertexAttributeId = subgraph.addAttribute(graph.getAttributeElementType(vertexAttributeId), graph.getAttributeType(vertexAttributeId),
                                    graph.getAttributeName(vertexAttributeId), graph.getAttributeDescription(vertexAttributeId), graph.getAttributeDefaultValue(vertexAttributeId), null);
                        }

                        final Object sourceVertexAttributeValue = graph.getObjectValue(vertexAttributeId, sourceVertexId);
                        subgraph.setObjectValue(newVertexAttributeId, newSourceVertexId, sourceVertexAttributeValue);

                        final Object destinationVertexAttributeValue = graph.getObjectValue(vertexAttributeId, destinationVertexId);
                        subgraph.setObjectValue(newVertexAttributeId, newDestinationVertexId, destinationVertexAttributeValue);
                    }

                    // copy transaction
                    final boolean transactionDirected = graph.getTransactionDirection(transactionId) != Graph.FLAT;
                    final int newTransactionId = subgraph.addTransaction(transactionId, newSourceVertexId, newDestinationVertexId, transactionDirected);

                    // copy transaction attributes
                    final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
                    for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                        final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);

                        final int newTransactionAttributeId;
                        if (subgraph.getAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeName(transactionAttributeId)) != Graph.NOT_FOUND) {
                            newTransactionAttributeId = subgraph.getAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeName(transactionAttributeId));
                        } else {
                            newTransactionAttributeId = subgraph.addAttribute(graph.getAttributeElementType(transactionAttributeId), graph.getAttributeType(transactionAttributeId),
                                    graph.getAttributeName(transactionAttributeId), graph.getAttributeDescription(transactionAttributeId), graph.getAttributeDefaultValue(transactionAttributeId), null);
                        }

                        final Object transactionAttributeValue = graph.getObjectValue(transactionAttributeId, transactionId);
                        subgraph.setObjectValue(newTransactionAttributeId, newTransactionId, transactionAttributeValue);
                    }

                    // since this transaction was of a desired transaction
                    // type, we don't need to check the other desired types
                    break;
                }
            }
        }

        return subgraph;
    }
}
