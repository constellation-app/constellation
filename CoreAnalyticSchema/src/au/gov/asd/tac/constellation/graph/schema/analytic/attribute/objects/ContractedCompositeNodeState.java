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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ContractedCompositeNodeState objects store the details of the nodes contained
 * within a composite node which is currently contracted.
 * <p>
 * They are primarily used to allow expansion of these nodes on demand.
 *
 * @author twilight_sparkle
 */
public class ContractedCompositeNodeState {

    private final RecordStore constituentNodeStore;
    private final List<String> expandedIds;
    private final List<String> affectedExpandedIds;
    private final float[] mean;

    public RecordStore getConstituentNodeStore() {
        return constituentNodeStore;
    }

    public List<String> getExpandedIds() {
        return Collections.unmodifiableList(expandedIds);
    }

    public List<String> getAffectedExpandedIds() {
        return Collections.unmodifiableList(affectedExpandedIds);
    }

    /**
     * Get the number of nodes contained with the composite this state
     * represents.
     *
     * @return The number of nodes in the composite
     */
    public int getNumberOfNodes() {
        return expandedIds.size();
    }

    /**
     * Get the original mean position of the nodes contained within this
     * composite.
     *
     * @return An array {x,y,z} containing the coordinates of the mean position.
     */
    public float[] getMean() {
        return this.mean;
    }

    /**
     * Create a new ContractedCompositeNodeStore
     *
     * @param compositeNodeStore The {@link RecordStore} holding the nodes
     * contained within the composite.
     * @param nodeRecordStoreIds A list of RecordStore IDs for the nodes
     * contained within the composite.
     * @param affectedNodeRecordStoreIds A list of RecordStore IDs for the
     * 'affecting' nodes contained within the composite. Affecting nodes will
     * create and receive transactions when contracting and expanding the
     * composite, respectively.
     * @param mean The x,y,z coordinates of the original mean position of the
     * nodes contained within this composite.
     */
    public ContractedCompositeNodeState(final RecordStore compositeNodeStore, final List<String> nodeRecordStoreIds, final List<String> affectedNodeRecordStoreIds, final float[] mean) {
        this.constituentNodeStore = compositeNodeStore;
        this.expandedIds = nodeRecordStoreIds;
        this.affectedExpandedIds = affectedNodeRecordStoreIds;
        this.mean = mean;
    }

    /**
     * Expand the composite represented by this state, using the supplied graph
     * write lock.
     *
     * @param wg The graph write lock with which to perform the expansion.
     * @param vxId The Graph ID of the node that is being expanded. The caller
     * must ensure that this is the state object corresponding to this node!
     * @return A list of graph IDs of the constituent nodes of the now expanded
     * composite.
     */
    public List<Integer> expand(final GraphWriteMethods wg, final int vxId) {
        // Create rows in the expansion record store for transactions that are currently connected to the composite node and should be connected to expnaded nodes.
        final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(wg);
        final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(wg);
        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);

        // Clear the composite state we are currently expanding!
        wg.setObjectValue(compositeStateAttr, vxId, null);

        // Create the cotnraction store and get the stored id of the contracted composite ndoe
        final String[] contractedId = new String[1];
        final RecordStore contractionStore = GraphRecordStoreUtilities.copySpecifiedVertex(wg, null, vxId, contractedId);

        final RecordStore addToGraphStore = new GraphRecordStore();
        addToGraphStore.add(constituentNodeStore);

        // Copy the relevant transactions from the composite to the constituent node record store
        GraphRecordStoreUtilities.copyTransactionsFromComposite(wg, addToGraphStore, vxId, contractedId[0], affectedExpandedIds, new GraphAttribute(wg, uniqueIdAttr));

        // Add the now complete expanded composite node store to the graph (do not initialise or complete with schema)
        final Map<String, Integer> vertexMap = new HashMap<>();
        final List<Integer> expandedVerts = GraphRecordStoreUtilities.addRecordStoreToGraph(wg, addToGraphStore, false, false, null, vertexMap, null);

        // Create the expanded composite state for each expanded node and add it to those nodes.
        vertexMap.entrySet().forEach(entry -> {
            final int id = entry.getValue();
            final ExpandedCompositeNodeState expansionState = new ExpandedCompositeNodeState(
                    contractionStore,
                    contractedId[0],
                    affectedExpandedIds.contains(entry.getKey()),
                    expandedVerts.size()
            );

            wg.setObjectValue(compositeStateAttr, id, new CompositeNodeState(id, expansionState));
        });

        // Correct the x,y,z coordinates of the nodes in the zonsitutentNodeStore in case the composite node has been moved.
        final float x = wg.getFloatValue(xAttr, vxId);
        final float y = wg.getFloatValue(yAttr, vxId);
        final float z = wg.getFloatValue(zAttr, vxId);
        final boolean selected = wg.getBooleanValue(selectedAttr, vxId);
        vertexMap.values().forEach(id -> {
            final float currentX = wg.getFloatValue(xAttr, id);
            final float currentY = wg.getFloatValue(yAttr, id);
            final float currentZ = wg.getFloatValue(zAttr, id);
            wg.setFloatValue(xAttr, id, currentX - mean[0] + x);
            wg.setFloatValue(yAttr, id, currentY - mean[1] + y);
            wg.setFloatValue(zAttr, id, currentZ - mean[2] + z);
            wg.setBooleanValue(selectedAttr, id, selected);
        });

        // Delete the vertex represented by this composite node state
        wg.removeVertex(vxId);

        return new ArrayList<>(vertexMap.values());
    }
}
