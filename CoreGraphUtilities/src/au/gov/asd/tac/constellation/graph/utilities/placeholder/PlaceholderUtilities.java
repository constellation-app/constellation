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
package au.gov.asd.tac.constellation.graph.utilities.placeholder;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.DatumProcessor;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.ProcessingException;
import au.gov.asd.tac.constellation.graph.processing.Record;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphDirection;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphStep;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphTransaction;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphVertex;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphWrapper;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author capella
 */
public class PlaceholderUtilities {

    private static final String DEFAULT_PLACEHOLDER_LABEL = "_placeholder<" + AnalyticConcept.VertexType.PLACEHOLDER.getName() + ">";

    private PlaceholderUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Given a String, returns the corresponding name for a placeholder node.
     * This should be used to ensure the right data merges together by keeping
     * placeholder node naming consistent.
     *
     * @param name the name of the placeholder node.
     * @return the corresponding name for a placeholder node.
     */
    public static String getPlaceholderLabel(final String name) {
        return name + DEFAULT_PLACEHOLDER_LABEL;
    }

    /**
     * Adds records to a given RecordStore connecting each given node to a
     * common placeholder node. This is simply a shortcut for creating groups
     * which will later be merged using the processRecord method.
     *
     * @param groupName the name of the group.
     * @param recordStore the RecordStore to add the group to.
     * @param nodes the list of nodes to connect to.
     * @param transactionType the type of transaction to add.
     * @param enrichmentAttributes the extra attributes to add.
     */
    public static void createGroup(final String groupName, final RecordStore recordStore, final Iterable<String> nodes,
            final SchemaTransactionType transactionType, final Map<String, String> enrichmentAttributes) {
        for (final String node : nodes) {
            recordStore.add();
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, getPlaceholderLabel(groupName));
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, node);
            recordStore.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, transactionType);
            for (final Map.Entry<String, String> entry : enrichmentAttributes.entrySet()) {
                recordStore.set(GraphRecordStoreUtilities.TRANSACTION + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Processes the data in a Record, merging nodes connected by placeholder
     * nodes and cleaning up the result.
     *
     * @param schema the schema for the new graph.
     * @param newRecord the record to add to the new graph.
     * @param rowProcessor the processor that will interpret the record.
     * @param parameters the parameters that are passed to the row processor.
     * @param dominanceComparator the comparator that orders the vertices to
     * determine which get merged.
     * @param cleanupGraph specifies whether the graph should go through a clean
     * up operation.
     * @param debug should debug information be produced.
     * @return the new graph.
     * @throws PluginException if an anticipated exception occurs.
     * @throws InterruptedException if the operation is canceled.
     */
    public static StoreGraph processRecord(final Schema schema, final Record newRecord, final DatumProcessor<Record, Map<String, String>> rowProcessor,
            final Map<String, String> parameters, Comparator<GraphVertex> dominanceComparator, final boolean cleanupGraph, final boolean debug) throws PluginException, InterruptedException {
        StoreGraphRecordStore graph = new StoreGraphRecordStore(schema);
        try {
            rowProcessor.process(parameters, newRecord, graph);
        } catch (ProcessingException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, ex.getMessage());
        }
        return collapsePlaceholders(graph, rowProcessor, dominanceComparator, cleanupGraph, debug);
    }

    public static StoreGraph collapsePlaceholders(final StoreGraphRecordStore graph, final DatumProcessor<Record, ?> rowProcessor,
            final Comparator<GraphVertex> dominanceComparator, final boolean cleanupGraph, final boolean debug) throws PluginException, InterruptedException {
        graph.complete();
        graph.validateKeys();

        final GraphWrapper g = new GraphWrapper(graph);

        // remove all transactions with type 'unknown' and all nodes with identifier 'unknown'
        if (cleanupGraph) {
            g.streamTransactions()
                    .filter(transaction -> transaction.getTypeValue().equals(SchemaTransactionTypeUtilities.getDefaultType()))
                    .forEach(GraphTransaction::deferRemove);
            g.streamVertices()
                    .filter(vertex -> "unknown".equals(vertex.getStringValue(VisualConcept.VertexAttribute.IDENTIFIER)))
                    .forEach(GraphVertex::deferRemove);
            g.completeDeferred();
        }

        if (debug) {
            GraphOpener.getDefault().openGraph(new DualGraph(graph, true),
                    rowProcessor.getClass().getSimpleName() + "-debug-stage1");
        }

        // connect all nodes with type 'placeholder'
        g.streamVertices()
                .filter(v -> v.getTypeValue().equals(AnalyticConcept.VertexType.PLACEHOLDER))
                .map(v -> v.walkNeighbours(s -> s.getTransaction().getTypeValue().isSubTypeOf(AnalyticConcept.TransactionType.CORRELATION) && s.getDestinationVertex().getTypeValue().equals(AnalyticConcept.VertexType.PLACEHOLDER), true)
                .map(GraphStep::getDestinationVertex)
                .collect(Collectors.toSet()))
                .distinct()
                .forEach(c -> {
                    String newIdentifier = c.stream().map(v -> v.getStringValue(VisualConcept.VertexAttribute.IDENTIFIER)).collect(Collectors.joining(SeparatorConstants.HYPHEN));
                    c.stream().forEach(v -> {
                        v.setStringValue(VisualConcept.VertexAttribute.IDENTIFIER, newIdentifier);
                        v.completeWithSchema();
                    });
                });
        g.validateKeys();

        if (debug) {
            GraphOpener.getDefault().openGraph(new DualGraph(graph, true),
                    rowProcessor.getClass().getSimpleName() + "-debug-stage2");
        }

        // replace nodes with type 'placeholder' with the dominant correlated node
        g.streamVertices()
                .filter(v -> v.getTypeValue().equals(AnalyticConcept.VertexType.PLACEHOLDER))
                .forEach(v -> {
                    Optional<GraphVertex> dominant = v.streamNeighbours()
                            .filter(n -> n.getDirection() != GraphDirection.LOOPBACK)
                            .filter(n -> n.getTransaction().getTypeValue().isSubTypeOf(AnalyticConcept.TransactionType.CORRELATION))
                            .map(n -> n.getDestinationVertex())
                            .distinct()
                            .sorted(dominanceComparator)
                            .findFirst();
                    // TODO: needed to create a special case for 'unknown' type vertices, but not sure why...
                    if (dominant.isPresent() && !dominant.get().getTypeValue().equals(SchemaVertexTypeUtilities.getDefaultType())) {
                        v.setStringValue(VisualConcept.VertexAttribute.IDENTIFIER, dominant.get().getStringValue(VisualConcept.VertexAttribute.IDENTIFIER));
                        v.setTypeValue(dominant.get().getTypeValue());
                        v.setRawValue(dominant.get().getRawValue());
                        v.completeWithSchema();
                    } else {
                        v.deferRemove();
                    }
                });
        g.completeDeferred();
        g.validateKeys();

        if (debug) {
            GraphOpener.getDefault().openGraph(new DualGraph(graph, true),
                    rowProcessor.getClass().getSimpleName() + "-debug-stage3");
        }

        // remove transactions that correlate nodes with themselves
        g.streamTransactions()
                .filter(t -> t.getTypeValue().isSubTypeOf(AnalyticConcept.TransactionType.CORRELATION))
                .filter(t -> t.getSourceVertex().equals(t.getDestinationVertex()))
                .forEach(GraphTransaction::deferRemove);
        g.completeDeferred();

        return graph;
    }

    public static void collapsePlaceholders(final GraphWriteMethods graph,
            final Comparator<SchemaVertexType> dominanceComparator,
            final boolean debug) throws PluginException, InterruptedException {
        final List<Integer> placeholderIds = new ArrayList<>();
        final Map<Integer, List<Integer>> placeholderCorrelations = new HashMap<>();
        final Map<Integer, List<Integer>> placeholderActivity = new HashMap<>();
        final Map<Integer, Integer> placeholderNeighbours = new HashMap<>();

        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);

        // collect placeholders, their transactions and their neighbours
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final SchemaVertexType vertexType = graph.getObjectValue(vertexTypeAttributeId, vertexId);

            if (vertexType.isSubTypeOf(AnalyticConcept.VertexType.PLACEHOLDER)) {
                placeholderIds.add(vertexId);

                final List<Integer> placeholderCorrelationList = new ArrayList<>();
                final List<Integer> placeholderActivityList = new ArrayList<>();
                final int transactionCount = graph.getVertexTransactionCount(vertexId);
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getVertexTransaction(vertexId, transactionPosition);
                    final SchemaTransactionType transactionType = graph.getObjectValue(transactionTypeAttributeId, transactionId);

                    if (transactionType.isSubTypeOf(AnalyticConcept.TransactionType.CORRELATION)) {
                        placeholderCorrelationList.add(transactionId);
                    } else {
                        placeholderActivityList.add(transactionId);
                    }

                    final int neighbourId = graph.getTransactionSourceVertex(transactionId) == vertexId
                            ? graph.getTransactionDestinationVertex(transactionId)
                            : graph.getTransactionSourceVertex(transactionId);
                    placeholderNeighbours.put(transactionId, neighbourId);
                }

                placeholderCorrelations.put(vertexId, placeholderCorrelationList);
                placeholderActivity.put(vertexId, placeholderActivityList);
            }
        }

        if (debug) {
            GraphOpener.getDefault().openGraph(new DualGraph((StoreGraph) graph.copy(), true),
                    GraphNode.getGraphNode(graph.getId()).getName() + "-debug-stage1");
        }

        // choose lead vertices to replace placeholders
        placeholderIds.forEach(placeholderId -> {
            final int leadVertex;
            final List<Integer> placeholderCorrelationList = placeholderCorrelations.get(placeholderId);
            if (!placeholderCorrelationList.isEmpty()) {

                // calculate lead vertex
                final SchemaVertexType leadVertexType = placeholderCorrelationList.stream()
                        .map(placeholderNeighbours::get)
                        .map(neighbourId -> (SchemaVertexType) graph.getObjectValue(vertexTypeAttributeId, neighbourId))
                        .sorted(dominanceComparator)
                        .findFirst().get();
                leadVertex = placeholderCorrelationList.stream()
                        .map(placeholderNeighbours::get)
                        .filter(neighbourId -> graph.getObjectValue(vertexTypeAttributeId, neighbourId).equals(leadVertexType))
                        .findFirst().get();

                // move correlations from the placeholder to the lead vertex of the entity
                placeholderCorrelationList.forEach(correlationId -> {
                    if (graph.getTransactionSourceVertex(correlationId) == placeholderId
                            && graph.getTransactionDestinationVertex(correlationId) != leadVertex) {
                        graph.setTransactionSourceVertex(correlationId, leadVertex);
                    } else if (graph.getTransactionDestinationVertex(correlationId) == placeholderId
                            && graph.getTransactionSourceVertex(correlationId) != leadVertex) {
                        graph.setTransactionDestinationVertex(correlationId, leadVertex);
                    } else {
                        // Do nothing
                    }
                });

                // move activity from the placeholder to the lead vertex of the entity
                final List<Integer> placeholderActivityList = placeholderActivity.get(placeholderId);
                placeholderActivityList.forEach(activityId -> {
                    if (graph.getTransactionSourceVertex(activityId) == placeholderId) {
                        graph.setTransactionSourceVertex(activityId, leadVertex);
                    } else {
                        graph.setTransactionDestinationVertex(activityId, leadVertex);
                    }
                });
            }
        });

        if (debug) {
            GraphOpener.getDefault().openGraph(new DualGraph((StoreGraph) graph.copy(), true),
                    GraphNode.getGraphNode(graph.getId()).getName() + "-debug-stage2");
        }

        // remove all placeholders
        placeholderIds.forEach(graph::removeVertex);
    }
}
