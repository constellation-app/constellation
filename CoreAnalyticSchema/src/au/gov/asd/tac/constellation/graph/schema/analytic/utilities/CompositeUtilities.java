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
package au.gov.asd.tac.constellation.graph.schema.analytic.utilities;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.mergers.PrioritySurvivingGraphElementMerger;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ContractedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ExpandedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.CompositeTransactionId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author twilight_sparkle
 */
public class CompositeUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(CompositeUtilities.class.getName());

    private CompositeUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    private static void simplifyCompositeTransactions(final GraphWriteMethods graph, final int uniqueIdAttr, final int vxId) {
        for (int t = 0; t < graph.getVertexTransactionCount(vxId); t++) {
            final int txId = graph.getVertexTransaction(vxId, t);
            final int source = graph.getTransactionSourceVertex(txId);
            final int dest = graph.getTransactionDestinationVertex(txId);
            final CompositeTransactionId uniqueId = CompositeTransactionId.fromString(graph.getStringValue(uniqueIdAttr, txId));
            if (source == vxId && uniqueId.getOriginalSourceNode() != null) {
                uniqueId.setOriginalSourceNode(null);
            }
            if (dest == vxId && uniqueId.getOriginalDestinationNode() != null) {
                uniqueId.setOriginalDestinationNode(null);
            }
            graph.setStringValue(uniqueIdAttr, txId, uniqueId.toString());
        }
    }

    /**
     * Destroys a single composite node which the specified node either is, or
     * is a constituent of.
     *
     * @param graph The graph.
     * @param compositeStateAttr The graph ID of the composite state attribute
     * for nodes.
     * @param uniqueIdAttr The graph ID of the uniqueId attribute for
     * transactions.
     * @param vxId The ID of the specified node.
     * @return A list of IDs of any newly expanded nodes. This will be an empty
     * list if no composite was destroyed, or if the destroyed composite was
     * already expanded.
     */
    public static List<Integer> destroyComposite(final GraphWriteMethods graph, final int compositeStateAttr, final int uniqueIdAttr, final int vxId) {
        final List<Integer> resultingNodes = new ArrayList<>();
        final CompositeNodeState compositeState = (CompositeNodeState) graph.getObjectValue(compositeStateAttr, vxId);
        if (compositeState != null) {
            if (compositeState.isComposite()) {
                final ContractedCompositeNodeState contractedState = compositeState.contractedState;
                resultingNodes.addAll(contractedState.expand(graph, vxId));
                while (true) {
                    try {
                        graph.validateKey(GraphElementType.VERTEX, false);
                        break;
                    } catch (final DuplicateKeyException ex) {
                        LOGGER.log(Level.INFO, "Duplicate Key has been found. Merging duplicate nodes");
                        final GraphElementMerger merger = new PrioritySurvivingGraphElementMerger();
                        merger.mergeElement(graph, GraphElementType.VERTEX, ex.getNewId(), ex.getExistingId());
                    }
                }
                resultingNodes.forEach(id -> {
                    simplifyCompositeTransactions(graph, uniqueIdAttr, id);
                    graph.setObjectValue(compositeStateAttr, id, null);
                });
            } else {
                final int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    final int nxId = graph.getVertex(i);
                    final CompositeNodeState cns = ((CompositeNodeState) graph.getObjectValue(compositeStateAttr, nxId));
                    if (cns != null && cns.expandedState != null && cns.expandedState.getCompositeId().equals(compositeState.expandedState.getCompositeId())) {
                        simplifyCompositeTransactions(graph, uniqueIdAttr, nxId);
                        graph.setObjectValue(compositeStateAttr, nxId, null);
                    }
                }
            }
        }
        return resultingNodes;
    }

    /**
     * Destroys all composite nodes in the graph.
     *
     * @param graph The graph.
     * @return True if any composites were destroyed. False otherwise (i.e.
     * there were none on the graph).
     */
    public static boolean destroyAllComposites(final GraphWriteMethods graph) {
        boolean anythingDestroyed = false;
        final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(graph);
        final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);

        if (compositeStateAttr != Graph.NOT_FOUND) {
            boolean moreComposites = true;
            while (moreComposites) {
                moreComposites = false;
                final int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    final int vxId = graph.getVertex(i);
                    moreComposites = !destroyComposite(graph, compositeStateAttr, uniqueIdAttr, vxId).isEmpty();
                    if (moreComposites) {
                        anythingDestroyed = true;
                        break;
                    }
                }
            }
        }
        return anythingDestroyed;
    }

    /**
     * Expands a single composite node.
     *
     * @param graph The graph.
     * @param compositeStateAttr The graph ID of the composite state attribute
     * for nodes.
     * @param vxId The id of the specified node.
     * @return A list of the id's of the expanded constituent nodes. This will
     * be empty if the specified node was not a composite.
     */
    public static List<Integer> expandComposite(final GraphWriteMethods graph, final int compositeStateAttr, final int vxId) {
        final List<Integer> expandedNodes = new ArrayList<>();
        final CompositeNodeState compositeState = (CompositeNodeState) graph.getObjectValue(compositeStateAttr, vxId);
        if (compositeState != null && compositeState.isComposite()) {
            final ContractedCompositeNodeState contractedState = compositeState.contractedState;
            expandedNodes.addAll(contractedState.expand(graph, vxId));
            while (true) {
                try {
                    graph.validateKey(GraphElementType.VERTEX, false);
                    break;
                } catch (final DuplicateKeyException ex) {
                    LOGGER.log(Level.INFO, "Duplicate Key has been found. Merging duplicate nodes");
                    final GraphElementMerger merger = new PrioritySurvivingGraphElementMerger();
                    merger.mergeElement(graph, GraphElementType.VERTEX, ex.getNewId(), ex.getExistingId());
                }
            }
        }
        return expandedNodes;
    }

    /**
     * Expand all composite nodes in the graph.
     *
     * @param graph The graph.
     * @return True if any composites were expanded. False otherwise (i.e. there
     * were none on the graph).
     */
    public static boolean expandAllComposites(final GraphWriteMethods graph) {
        boolean anythingExpanded = false;
        final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(graph);

        if (compositeStateAttr != Graph.NOT_FOUND) {
            boolean moreComposites = true;
            while (moreComposites) {
                moreComposites = false;
                final int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    final int vxId = graph.getVertex(i);
                    moreComposites = !expandComposite(graph, compositeStateAttr, vxId).isEmpty();
                    if (moreComposites) {
                        anythingExpanded = true;
                        break;
                    }
                }
            }
        }
        return anythingExpanded;
    }

    /**
     * Contracts a single composite constituent.
     *
     * @param graph The graph.
     * @param compositeStateAttr The graph ID of the composite state attribute
     * for nodes.
     * @param vxId The id of the specified node.
     * @return The id of the contracted composite. This will be Graph.NOT_FOUND
     * when the specified node was not a composite constituent.
     */
    public static int contractComposite(final GraphWriteMethods graph, final int compositeStateAttr, final int vxId) {
        final CompositeNodeState compositeState = (CompositeNodeState) graph.getObjectValue(compositeStateAttr, vxId);
        if (compositeState != null && compositeState.comprisesAComposite()) {
            return compositeState.expandedState.contract(graph);
        }
        return Graph.NOT_FOUND;
    }

    /**
     * Contract all composite nodes in the graph.
     *
     * @param graph The graph.
     * @return True if any composites were contracted. False otherwise (i.e.
     * there were no composite constituents on the graph).
     */
    public static boolean contractAllComposites(final GraphWriteMethods graph) {
        boolean anythingContracted = false;
        final int compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(graph);

        if (compositeAttr != Graph.NOT_FOUND) {
            boolean moreComposites = true;
            while (moreComposites) {
                moreComposites = false;
                final int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    final int vxId = graph.getVertex(i);
                    moreComposites = contractComposite(graph, compositeAttr, vxId) != Graph.NOT_FOUND;
                    if (moreComposites) {
                        anythingContracted = true;
                        break;
                    }
                }
            }
        }
        return anythingContracted;
    }

    /**
     * Make a composite node by specifying a lead node id and a set of the
     * comprising node id's
     *
     * @param graph The graph
     * @param comprisingIds A Set of the comprising node id's to be composited
     * @param leaderId The lead node id to contain the composited nodes
     */
    public static void makeComposite(final GraphWriteMethods graph, final Collection<Integer> comprisingIds, final int leaderId) {
        final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
        final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);

        // We first destroy any composites or composite constituents about to be composited.
        // For any composites, we also add any expanded ids and remove the destroyed composite id from the list of selected ids.
        final Set<Integer> addedVerts = new HashSet<>();
        final Set<Integer> removedVerts = new HashSet<>();
        comprisingIds.forEach(vxId -> {
            final List<Integer> resultingVerts = CompositeUtilities.destroyComposite(graph, compositeStateAttr, uniqueIdAttr, vxId);
            if (!resultingVerts.isEmpty()) {
                removedVerts.add(vxId);
                addedVerts.addAll(resultingVerts);
            }
        });

        // NOTE:: Remove before adding, because of id reuse!
        comprisingIds.removeAll(removedVerts);
        comprisingIds.addAll(addedVerts);

        // Make a record store representing the new composite that is about to be added
        final String[] compositeId = new String[1];
        final RecordStore newCompositeStore = new GraphRecordStore();
        GraphRecordStoreUtilities.copySpecifiedVertex(graph, newCompositeStore, leaderId, compositeId);

        // Construct and set an expanded composite node state for each of the nodes that will constitute the composite.
        comprisingIds.forEach(vxId -> {
            final ExpandedCompositeNodeState expandedState = new ExpandedCompositeNodeState(newCompositeStore, compositeId[0], true, comprisingIds.size());
            graph.setObjectValue(compositeStateAttr, vxId, new CompositeNodeState(vxId, expandedState));
        });

        // Create the composite by calling contract on the first node's expanded composite state.
        ((CompositeNodeState) graph.getObjectValue(compositeStateAttr, comprisingIds.iterator().next())).expandedState.contract(graph);
    }
}
