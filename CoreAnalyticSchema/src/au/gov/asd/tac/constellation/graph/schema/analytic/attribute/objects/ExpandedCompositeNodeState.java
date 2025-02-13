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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExpandedCompositeNodeState objects store the details of the expanded
 * composite node to which a number of composite constituents belong. One of
 * these state objects exists for each constituent of the expanded composite (as
 * part of the constituent's {@link CompositeNodeState}).
 * <p>
 * They are primarily used to allow contraction of these constituent nodes on
 * demand.
 *
 * @author twilight_sparkle
 */
public class ExpandedCompositeNodeState {

    private final RecordStore compositeNodeStore;
    private final String compositeId;
    private final boolean isAffecting;
    private final int numberOfNodes;

    /**
     * Get the RecordStore ID of the expanded composite node corresponding to
     * this state.
     *
     * @return The ID of the expanded composite node
     */
    public String getCompositeId() {
        return compositeId;
    }

    /**
     * Is the constituent node corresponding to this state an 'affecting node'?
     * Affecting nodes create and receive transactions when contracting and
     * expanding the composite, respectively.
     *
     * @return True if the constituent node is 'affecting', false otherwise.
     */
    public boolean isAffectingNode() {
        return isAffecting;
    }

    /**
     * Get the total number of constituents (including the one corresponding to
     * this state) that belong to the expanded composite corresponding to this
     * state.
     *
     * @return The number of constituents in the expanded composite.
     */
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public RecordStore getCompositeNodeStore() {
        return compositeNodeStore;
    }

    /**
     * Create a new ExpandedCompositeNodeStore
     *
     * @param compositeNodeStore The {@link RecordStore} holding the composite
     * node.
     * @param compositeId The RecordStore IDs for the composite node.
     * @param isAffecting Whether ot not this constituent is 'affecting'.
     * Affecting nodes will create and receive transactions when contracting and
     * expanding the composite, respectively.
     * @param numberOfNodes The total number of constituents in the expanded
     * composite.
     */
    public ExpandedCompositeNodeState(final RecordStore compositeNodeStore, final String compositeId, final boolean isAffecting, final int numberOfNodes) {
        this.compositeNodeStore = compositeNodeStore;
        this.compositeId = compositeId;
        this.isAffecting = isAffecting;
        this.numberOfNodes = numberOfNodes;
    }

    private void contractSingleVertex(final GraphWriteMethods wg, final int vxId, final RecordStore constituentNodeStore, final List<String> expandedIds, final List<String> affectedExpandedIds, final Map<Integer, String> idToCopiedId) {
        // Copy this vertex into the expansion record store inside the contracted composite state.
        final String[] copiedId = new String[1];
        GraphRecordStoreUtilities.copySpecifiedVertex(wg, constituentNodeStore, vxId, copiedId);

        // Add the current graph id to copied expansion store id to our map
        idToCopiedId.put(vxId, copiedId[0]);

        // Add to the expansion store those transactions between this vertex and all vertices already added to the expansion store
        idToCopiedId.entrySet().stream().forEach(entry
                -> GraphRecordStoreUtilities.copyTransactionsBetweenVertices(wg, constituentNodeStore, vxId, entry.getKey(), copiedId[0], entry.getValue())
        );

        // Add the copied id of the expanded vertex to the relevant lists in the contracted composite state.
        expandedIds.add(copiedId[0]);
        if (isAffecting) {
            affectedExpandedIds.add(copiedId[0]);
        }
    }

    /**
     * Contract the composite represented by this state, using the supplied
     * graph write lock.
     *
     * @param wg The graph write lock with which to perform the expansion.
     * @return The graph ID of the now contracted composite node.
     */
    public int contract(final GraphWriteMethods wg) {
        final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(wg);
        final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);
        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);

        final RecordStore constituentNodeStore = new GraphRecordStore();
        final List<String> expandedIds = new ArrayList<>();
        final List<String> affectedExpandedIds = new ArrayList<>();

        // Iterate through each vertex on the graph, and if it has an expanded composite state with the same composite id
        // as this state, perform the single vertex contraction. Keep track of x, y and z as we go so that we can set
        // the coordinates of the contracted composite to be at the centre of the expanded constituents.
        float x = 0;
        float y = 0;
        float z = 0;
        final Map<Integer, String> idToCopiedId = new HashMap<>();

        final int vertexCount = wg.getVertexCount();
        int numConstituents = 0;
        boolean constitutentsSelected = false;
        for (int i = 0; i < vertexCount; i++) {
            final int vxId = wg.getVertex(i);
            final CompositeNodeState cns = ((CompositeNodeState) wg.getObjectValue(compositeStateAttr, vxId));
            if (cns != null && cns.expandedState != null && cns.expandedState.getCompositeId().equals(compositeId)) {
                x += wg.getFloatValue(xAttr, vxId);
                y += wg.getFloatValue(yAttr, vxId);
                z += wg.getFloatValue(zAttr, vxId);
                constitutentsSelected |= wg.getBooleanValue(selectedAttr, vxId);

                // Clear the composite state we are about to expand
                wg.setObjectValue(compositeStateAttr, vxId, null);

                // Expand the composite state
                cns.expandedState.contractSingleVertex(wg, vxId, constituentNodeStore, expandedIds, affectedExpandedIds, idToCopiedId);
                numConstituents++;
            }
        }

        if (numConstituents != 0) {
            x /= numConstituents;
            y /= numConstituents;
            z /= numConstituents;
        }

        // The contracted state of this composite node, to be set on the composite node after it has been created.
        final float[] mean = new float[]{x, y, z};
        final ContractedCompositeNodeState contractionState = new ContractedCompositeNodeState(constituentNodeStore, expandedIds, affectedExpandedIds, mean);

        final RecordStore addToGraphStore = new GraphRecordStore();
        addToGraphStore.add(compositeNodeStore);

        // Add the transactions from the expanded nodes to non-constituent nodes to the contraction record store
        idToCopiedId.entrySet().forEach(entry
                -> GraphRecordStoreUtilities.copyTransactionsToComposite(wg, addToGraphStore, entry.getKey(), entry.getValue(), idToCopiedId.keySet(), compositeId, new GraphAttribute(wg, uniqueIdAttr)));
        // Remove each expanded vertex from the graph
        idToCopiedId.keySet().forEach(wg::removeVertex);

        // Add all the transactions between the expanded nodes to the expansion record store
        // Add the contraction record store to the graph, which creates the composite node and all its relevant transactions
        int contractedVert = GraphRecordStoreUtilities.addRecordStoreToGraph(wg, addToGraphStore, false, false, null).get(0);

        // Set the x,y,z and composite node state for the newly added composite node.
        wg.setFloatValue(xAttr, contractedVert, x);
        wg.setFloatValue(yAttr, contractedVert, y);
        wg.setFloatValue(zAttr, contractedVert, z);
        wg.setBooleanValue(selectedAttr, contractedVert, constitutentsSelected);
        wg.setObjectValue(compositeStateAttr, contractedVert, new CompositeNodeState(contractedVert, contractionState));

        return contractedVert;
    }
}
