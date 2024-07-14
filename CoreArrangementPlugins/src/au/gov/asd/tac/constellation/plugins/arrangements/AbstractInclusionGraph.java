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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.operations.SetFloatValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * A graph representing the included vertices of another graph (including
 * transactions): we call this an inclusion graph.
 * <p>
 * A popular requirement is to arrange only the vertices of a graph that are
 * selected. Rather than build this into every arrangement, we'll provide a
 * generic utility to build a new graph that includes only the selected vertices
 * of the original graph. The individual arrangements can then just operate on a
 * graph, without worrying about which vertices are and aren't selected. We keep
 * a mapping from the inclusion graph's vxIds back to the original graph's vxId,
 * so x,y,z coordinates can be moved back.
 * <p>
 * If either all vertices or no vertices (which we'll implicitly assume means
 * all vertices) are included, we just pass the graph straight through, and let
 * the arranger work directly on the original graph. We signify this by not
 * keeping the mapping.
 * <p>
 * Override {@link #isVertexIncluded isVertexIncluded()} to choose vertices
 * using different criteria.
 * <p>
 * The inclusion graph uses the addVertex(vxId) and addTransaction(txId) graph
 * methods so that the vertex and transaction ids in the inclusion graph are the
 * same as those in the original graph.
 * <p>
 * Note that this may not be the most efficient way to achieve the desired
 * behaviour. As this class is mostly separate from the logic of the
 * arrangements themselves, it may be changed to improve performance in the
 * future.
 *
 * @author algol
 */
public abstract class AbstractInclusionGraph {

    // Indicates that this vertex is a parent.
    // It must be less than zero because >=0 points to the parent vxId.
    // TODO: put this constant somewhere sensible.
    public static final int COMPOSITE_PARENT_MARKER = -314159;

    /**
     * Specify how transactions should be copied to the inclusion graph.
     */
    public enum Connections {

        /**
         * Don't copy any transactions.
         */
        NONE,
        /**
         * Each link will be copied to an undirected transaction.
         */
        LINKS,
        /**
         * Each edge in the original graph will be copied to a transaction.
         */
        EDGES,
        /**
         * Transactions will be copied one-for-one.
         */
        TRANSACTIONS
    }

    /**
     * Remember if we created an inclusion graph or just passed the original
     * graph through.
     */
    private boolean inclusionGraphIsOriginalGraph;

    // The original graph.
    protected final GraphWriteMethods wg;
    protected final Connections connections;

    // The graph containing only the vertices that were included in the
    // original graph. This may just be a reference to the original graph.
    protected GraphWriteMethods inclusionGraph;

    /**
     * What attributes should be copied from the original graph to the inclusion
     * graph.
     */
    private final ArrayList<Attribute> attributesToCopy;

    private boolean updatePositionIfExisting;

    // Attribute used to store whether a vertexes position should be pinned and not auto arranged
    private final int pinnedAttr;

    /**
     * Create a new inclusion graph.
     *
     * @param wg The original graph.
     * @param connections How to copy transactions to the inclusion graph.
     */
    protected AbstractInclusionGraph(final GraphWriteMethods wg, final Connections connections) {
        this.wg = wg;
        this.connections = connections;
        attributesToCopy = new ArrayList<>();

        inclusionGraph = null;
        updatePositionIfExisting = true;
        pinnedAttr = VisualConcept.VertexAttribute.PINNED.ensure(wg);
    }

    /**
     * Add this attribute to the set of attributes that will be copied to the
     * inclusion graph.
     * <p>
     * An inclusion graph is (possibly) a copy of the original graph, and only a
     * basic set of attributes is copied. However, some users of the inclusion
     * graph may need other attributes to be copied as well. For instance, an
     * arranger that orders vertices by color will require the color attribute
     * to be copied.
     * <p>
     * Call this method to add attributes to be copied to the inclusion graph.
     * This method must be called before getInclusionGraph() (duh).
     *
     * @param attr an attribute that should be copied to this inclusion graph.
     */
    public void addAttributeToCopy(final Attribute attr) {
        attributesToCopy.add(attr);
    }

    private void createInclusionGraph() {
        final int vxCount = wg.getVertexCount();

        // Loop through all vertexes and count the number that have been
        // explictly selected by user and those that have been marked as pinned.
        // Vertexes marked as pinned will not be 'arranged'.
        int incCount = 0;
        int pinnedCount = 0;
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);
            if (isVertexIncluded(vxId)) {
                incCount++;
            }
            if (!wg.getBooleanValue(pinnedAttr, vxId)) {
                pinnedCount++;
            }
        }

        // If every vertex is a candidate to be moved we can just return the
        // current graph.
        inclusionGraphIsOriginalGraph
                = (incCount == vxCount || incCount == 0) && (pinnedCount == 0);
        if (inclusionGraphIsOriginalGraph) {
            // All vertices are (implicitly or explicitly) selected.
            // Pass the graph straight through.
            inclusionGraph = wg;
            return;
        }

        // Store the IDs of attributes that will be read from wg.
        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);
        final int x2Attr = VisualConcept.VertexAttribute.X2.get(wg);
        final int y2Attr = VisualConcept.VertexAttribute.Y2.get(wg);
        final int z2Attr = VisualConcept.VertexAttribute.Z2.get(wg);
        final int nradiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.get(wg);
        final int lradiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(wg);

        // Are the x2, y2, z2 attributes set
        final boolean xyz2AreSet = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        // Create the inclusion graph.
        // We need to create some essential attributes, plus whatever other
        // attributes we've been told to create via calls to addAttributeToCopy().
        final StoreGraph storeGraph = new StoreGraph(wg.getSchema());
        VisualConcept.VertexAttribute.X.ensure(storeGraph);
        VisualConcept.VertexAttribute.Y.ensure(storeGraph);
        VisualConcept.VertexAttribute.Z.ensure(storeGraph);
        if (xyz2AreSet) {
            VisualConcept.VertexAttribute.X2.ensure(storeGraph);
            VisualConcept.VertexAttribute.Y2.ensure(storeGraph);
            VisualConcept.VertexAttribute.Z2.ensure(storeGraph);
        }
        if (nradiusAttr != Graph.NOT_FOUND) {
            VisualConcept.VertexAttribute.NODE_RADIUS.ensure(storeGraph);
        }
        if (lradiusAttr != Graph.NOT_FOUND) {
            VisualConcept.VertexAttribute.LABEL_RADIUS.ensure(storeGraph);
        }

        // Process any attributes specified by calls to addAttributeToCopy().
        final int[] selectionAttributes = new int[attributesToCopy.size()];
        for (int i = 0; i < attributesToCopy.size(); i++) {
            final Attribute attr = attributesToCopy.get(i);
            selectionAttributes[i] = storeGraph.addAttribute(attr.getElementType(), attr.getAttributeType(), attr.getName(), attr.getDescription(), attr.getDefaultValue(), null);
        }

        // Store the IDs of attributes that will be written to storeGraph.
        final int incXAttr = VisualConcept.VertexAttribute.X.get(storeGraph);
        final int incYAttr = VisualConcept.VertexAttribute.Y.get(storeGraph);
        final int incZAttr = VisualConcept.VertexAttribute.Z.get(storeGraph);
        final int incX2Attr = VisualConcept.VertexAttribute.X2.get(storeGraph);
        final int incY2Attr = VisualConcept.VertexAttribute.Y2.get(storeGraph);
        final int incZ2Attr = VisualConcept.VertexAttribute.Z2.get(storeGraph);
        final int incNradiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.get(storeGraph);
        final int incLradiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(storeGraph);

        // Build the inclusion graph by copying vertices, connections, and values
        // from the original graph.  We remember which vertices have been included
        // for easy future reference.
        final BitSet vertices = new BitSet();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            // A vertex goes into the inclusion graph if all vertexes are selected
            // or the explicit vertex is selected and the vertex is not marked as
            // pinned.
            final boolean allVertexesSelected = incCount == vxCount || incCount == 0;
            if ((allVertexesSelected || isVertexIncluded(vxId)) && !wg.getBooleanValue(pinnedAttr, vxId)) {
                vertices.set(vxId);

                // Create the vertex in the inclusion graph with the same vertex
                // ID as the original graph. This means we don't have to track
                // original id <-> inclusion graph id.
                final int incVxId = storeGraph.addVertex(vxId);

                storeGraph.setFloatValue(incXAttr, incVxId, wg.getFloatValue(xAttr, vxId));
                storeGraph.setFloatValue(incYAttr, incVxId, wg.getFloatValue(yAttr, vxId));
                storeGraph.setFloatValue(incZAttr, incVxId, wg.getFloatValue(zAttr, vxId));
                if (xyz2AreSet) {
                    storeGraph.setFloatValue(incX2Attr, incVxId, wg.getFloatValue(x2Attr, vxId));
                    storeGraph.setFloatValue(incY2Attr, incVxId, wg.getFloatValue(y2Attr, vxId));
                    storeGraph.setFloatValue(incZ2Attr, incVxId, wg.getFloatValue(z2Attr, vxId));
                }
                if (incNradiusAttr != Graph.NOT_FOUND) {
                    storeGraph.setFloatValue(incNradiusAttr, incVxId, wg.getFloatValue(nradiusAttr, vxId));
                }
                if (incLradiusAttr != Graph.NOT_FOUND) {
                    storeGraph.setFloatValue(incLradiusAttr, incVxId, wg.getFloatValue(lradiusAttr, vxId));
                }

                // Copy the extra attribute values.
                for (int i = 0; i < attributesToCopy.size(); i++) {
                    storeGraph.setObjectValue(selectionAttributes[i], incVxId, wg.getObjectValue(attributesToCopy.get(i).getId(), vxId));
                }
            }
        }

        if (connections == Connections.NONE) {
            // Do nothing, the inclusion graph won't have any transactions.
        } else if (connections == Connections.TRANSACTIONS) {
            addTransactionsFromTransactions(wg, storeGraph, vertices);
        } else if (connections == Connections.EDGES) {
            addTransactionsFromEdges(wg, storeGraph, vertices);
        } else {
            addTransactionsFromLinks(wg, storeGraph, vertices);
        }

        this.inclusionGraph = storeGraph;
    }

    /**
     * Is a particular vertex to be added to the inclusion graph?
     * <p>
     * Override this to use a different way of choosing vertices to add to the
     * inclusion graph.
     *
     * @param vxId A vertex in the original graph.
     *
     * @return True if the vertex is to be added to the inclusion graph, false
     * otherwise.
     */
    public abstract boolean isVertexIncluded(final int vxId);

    /**
     * Transfer connections from wg to inclusionGraph by creating a transaction
     * for each transaction.
     *
     * @param wg Original graph.
     * @param inclusionGraph Target graph.
     * @param vertices Original vertex ids of interest.
     */
    private void addTransactionsFromTransactions(final GraphWriteMethods wg, final StoreGraph inclusionGraph, final BitSet vertices) {
        final int txCount = wg.getTransactionCount();
        for (int position = 0; position < txCount; position++) {
            final int txId = wg.getTransaction(position);
            final boolean directed = wg.getTransactionDirection(txId) != Graph.FLAT;

            final int lo = wg.getTransactionSourceVertex(txId);
            final int hi = wg.getTransactionDestinationVertex(txId);

            if (vertices.get(lo) && vertices.get(hi)) {
                inclusionGraph.addTransaction(txId, lo, hi, directed);
            }
        }
    }

    /**
     * Transfer connections from wg to inclusionGraph by creating a transaction
     * for each edge.
     *
     * @param wg Original graph.
     * @param inclusionGraph Target graph.
     * @param vertices Original vertex ids of interest.
     */
    private void addTransactionsFromEdges(final GraphWriteMethods wg, final GraphWriteMethods inclusionGraph, final BitSet vertices) {
        final int edgeCount = wg.getEdgeCount();
        for (int position = 0; position < edgeCount; position++) {
            final int edgeId = wg.getEdge(position);
            final boolean directed = wg.getEdgeDirection(edgeId) != Graph.FLAT;

            final int lo = wg.getEdgeSourceVertex(edgeId);
            final int hi = wg.getEdgeDestinationVertex(edgeId);

            if (vertices.get(lo) && vertices.get(hi)) {
                inclusionGraph.addTransaction(lo, hi, directed);
            }
        }
    }

    /**
     * Transfer connections from wg to inclusionGraph by creating a transaction
     * for each link.
     *
     * @param wg Original graph.
     * @param inclusionGraph Target graph.
     * @param vertices Original vertex ids of interest.
     */
    private void addTransactionsFromLinks(final GraphWriteMethods wg, final GraphWriteMethods inclusionGraph, final BitSet vertices) {
        final int linkCount = wg.getLinkCount();
        for (int position = 0; position < linkCount; position++) {
            final int linkId = wg.getLink(position);

            final int lo = wg.getLinkLowVertex(linkId);
            final int hi = wg.getLinkHighVertex(linkId);

            if (vertices.get(lo) && vertices.get(hi)) {
                inclusionGraph.addTransaction(lo, hi, false);
            }
        }
    }

    /**
     * The inclusion graph is determined lazily, so make sure this is called
     * whenever a caller wants something.
     */
    private void ensureInclusionGraph() {
        if (inclusionGraph == null) {
            createInclusionGraph();
        }
    }

    public GraphWriteMethods getInclusionGraph() {
        ensureInclusionGraph();

        return inclusionGraph;
    }

    /**
     * Is all of the graph being arranged?
     * <p>
     * This is true if either all of the vertices were selected, or none of the
     * vertices were selected (which implicitly means arrange everything.)
     *
     * @return True if all vertices are in the inclusion graph.
     */
    public boolean isArrangingAll() {
        ensureInclusionGraph();

        return inclusionGraphIsOriginalGraph;
    }

    public void unfreeze() {
        updatePositionIfExisting = true;
    }

    public void freeze() {
        updatePositionIfExisting = false;
    }

    /**
     * Transfer x,y,z values (and x2,y2,z2 values if they exist) back to the
     * original graph.
     * <p>
     * Because vertices that weren't isVertexIncluded() and non-parent composite
     * vertices weren't included, they're ignored.
     */
    public void retrieveCoords() {
        ensureInclusionGraph();

        if (inclusionGraphIsOriginalGraph) {
            // The original graph was passed directly through,
            // so there's nothing to do.
            return;
        }

        final int incXAttr = VisualConcept.VertexAttribute.X.get(inclusionGraph);
        final int incYAttr = VisualConcept.VertexAttribute.Y.get(inclusionGraph);
        final int incZAttr = VisualConcept.VertexAttribute.Z.get(inclusionGraph);
        final int incX2Attr = VisualConcept.VertexAttribute.X2.get(inclusionGraph);
        final int incY2Attr = VisualConcept.VertexAttribute.Y2.get(inclusionGraph);
        final int incZ2Attr = VisualConcept.VertexAttribute.Z2.get(inclusionGraph);

        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);
        final int x2Attr;
        final int y2Attr;
        final int z2Attr;

        if (incX2Attr != Graph.NOT_FOUND && incY2Attr != Graph.NOT_FOUND && incZ2Attr != Graph.NOT_FOUND) {
            x2Attr = VisualConcept.VertexAttribute.X2.ensure(wg);
            y2Attr = VisualConcept.VertexAttribute.Y2.ensure(wg);
            z2Attr = VisualConcept.VertexAttribute.Z2.ensure(wg);
        } else {
            x2Attr = Graph.NOT_FOUND;
            y2Attr = Graph.NOT_FOUND;
            z2Attr = Graph.NOT_FOUND;
        }

        final boolean xyz2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        if (wg.isRecordingEdit()) {
            final SetFloatValuesOperation setXOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, xAttr);
            final SetFloatValuesOperation setYOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, yAttr);
            final SetFloatValuesOperation setZOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, zAttr);

            SetFloatValuesOperation setX2Operation = null;
            SetFloatValuesOperation setY2Operation = null;
            SetFloatValuesOperation setZ2Operation = null;
            if (xyz2) {
                setX2Operation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, x2Attr);
                setY2Operation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, y2Attr);
                setZ2Operation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, z2Attr);
            }

            final int incVxCount = inclusionGraph.getVertexCount();
            for (int position = 0; position < incVxCount; position++) {
                final int selVxId = inclusionGraph.getVertex(position);
                final int vxId = selVxId;

                if (updatePositionIfExisting) {
                    setXOperation.setValue(vxId, inclusionGraph.getFloatValue(incXAttr, selVxId));
                    setYOperation.setValue(vxId, inclusionGraph.getFloatValue(incYAttr, selVxId));
                    setZOperation.setValue(vxId, inclusionGraph.getFloatValue(incZAttr, selVxId));
                }

                if (xyz2) {
                    setX2Operation.setValue(vxId, inclusionGraph.getFloatValue(incX2Attr, selVxId));
                    setY2Operation.setValue(vxId, inclusionGraph.getFloatValue(incY2Attr, selVxId));
                    setZ2Operation.setValue(vxId, inclusionGraph.getFloatValue(incZ2Attr, selVxId));
                }
            }

            wg.executeGraphOperation(setXOperation);
            wg.executeGraphOperation(setYOperation);
            wg.executeGraphOperation(setZOperation);
            if (xyz2) {
                wg.executeGraphOperation(setX2Operation);
                wg.executeGraphOperation(setY2Operation);
                wg.executeGraphOperation(setZ2Operation);
            }
        } else {
            final int incVxCount = inclusionGraph.getVertexCount();
            for (int position = 0; position < incVxCount; position++) {
                final int selVxId = inclusionGraph.getVertex(position);
                final int vxId = selVxId;

                wg.setFloatValue(xAttr, vxId, inclusionGraph.getFloatValue(incXAttr, selVxId));
                wg.setFloatValue(yAttr, vxId, inclusionGraph.getFloatValue(incYAttr, selVxId));
                wg.setFloatValue(zAttr, vxId, inclusionGraph.getFloatValue(incZAttr, selVxId));

                if (xyz2) {
                    wg.setFloatValue(x2Attr, vxId, inclusionGraph.getFloatValue(incX2Attr, selVxId));
                    wg.setFloatValue(y2Attr, vxId, inclusionGraph.getFloatValue(incY2Attr, selVxId));
                    wg.setFloatValue(z2Attr, vxId, inclusionGraph.getFloatValue(incZ2Attr, selVxId));
                }
            }
        }
    }
}
