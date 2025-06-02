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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A representation of a grouping operation on a graph.
 * <p>
 * A taxonomy is a way of representing subsets of a graph. An example is a
 * taxonomy representing the components of a graph. Another example is a
 * taxonomy representing clusters.
 * <p>
 * Each subset is called a taxon (plural taxa).
 * <p>
 * The taxa are stored in a <tt>Map&lt;Integer, Set&lt;Integer&gt;&gt;</tt>. The
 * values are the vertex ids in the individual taxa; each key is one of the
 * vertex ids in the taxon. The keys may or may not have meaning; in a
 * clustering taxonomy, the key may represent a "root" vertex, but in a
 * component taxonomy, the key may just be a random member of the taxon.
 * <p>
 * Two kinds of taxa can be treated specially. Singleton taxa are taxa that
 * contain a single vertex. Doublet taxa are taxa that contain a linked pair of
 * vertices. Provision is made for merging these, so multiple singletons can be
 * merged into one taxon, and multiple doublets can be merged into one taxon.
 * <p>
 * It is useful to create a new graph using the keys of the taxonomy. This new
 * graph is called a condensation. A condensation has the same number of
 * vertices as there are taxa, where each vertex in the condensation represents
 * a key. Each transaction in the condensation represents the transactions
 * between the taxa represented by the vertices; intra-taxon transactions are
 * not added to the condensation.
 * <p>
 * A mapping from condensation vertex id to taxon key is maintained so the
 * result of manipulations of the condensation can be transferred back to the
 * taxonomy.
 * <p>
 * A condensation is typically used to do an "outer" arrangement. Therefore the
 * condensation copies the x,y,z values of the taxa keys into the new graph, and
 * can reposition the vertices in the taxa based on the x,y,z values in the
 * condensation. (This is why the taxonomy requires a GraphWriteMethods graph:
 * so a potential reposition() can write the new x,y,z values.)
 *
 * @author algol
 */
public final class GraphTaxonomy {

    // Record the original x,y,z of the condensed graph.
    private static final String X_ORIG = "xorig";
    private static final String Y_ORIG = "yorig";
    private static final String Z_ORIG = "zorig";

    private final GraphWriteMethods wg;

    // A collection of taxa keyed by one of the vertices in each taxon.
    // The key may be meaningful (such as the root vertex if the taxon is a
    // tree), or meaningless (if for example a taxon is a graph component).
    private final Map<Integer, Set<Integer>> taxa;
    private final Map<Integer, Integer> nodeToTaxa;

    // A set of keys that indicates these taxa are to be arranged in a grid.
    private final Set<Integer> arrangeRectangularly;

    // The keys of the singleton and doublet taxa (if they exist).
    private int singletonKey;
    private int doubletKey;

    public GraphTaxonomy(final GraphWriteMethods wg, final Map<Integer, Set<Integer>> taxa) {
        this(wg, taxa, null);
    }

    public GraphTaxonomy(final GraphWriteMethods wg, final Map<Integer, Set<Integer>> taxa, final Map<Integer, Integer> nodeToTaxa) {
        this(wg, taxa, nodeToTaxa, Graph.NOT_FOUND, Graph.NOT_FOUND);
    }

    public GraphTaxonomy(final GraphWriteMethods wg, final Map<Integer, Set<Integer>> taxa, final Map<Integer, Integer> nodeToTaxa, final int singletonKey, final int doubletKey) {
        this.wg = wg;
        this.taxa = new HashMap<>(taxa);
        this.nodeToTaxa = nodeToTaxa == null ? null : new HashMap<>(nodeToTaxa);
        arrangeRectangularly = new HashSet<>();
        this.singletonKey = singletonKey;
        this.doubletKey = doubletKey;
    }

    public GraphWriteMethods getGraph() {
        return wg;
    }

    public Map<Integer, Set<Integer>> getTaxa() {
        return taxa;
    }

    public Map<Integer, Integer> getNodeToTaxa() {
        return Collections.unmodifiableMap(nodeToTaxa);
    }

    public int getSingletonKey() {
        return singletonKey;
    }

    public int getDoubletKey() {
        return doubletKey;
    }

    /**
     * How many taxa are there?
     *
     * @return the number of taxa.
     */
    public int size() {
        return taxa.keySet().size();
    }

    /**
     * Add the other taxa to this taxonomy.
     *
     * @param otherTaxa the new taxa to add.
     */
    public void add(final GraphTaxonomy otherTaxa) {
        for (final Integer k : otherTaxa.getTaxa().keySet()) {
            taxa.put(k, otherTaxa.getTaxa().get(k));
            if (otherTaxa.arrangeRectangularly.contains(k)) {
                arrangeRectangularly.add(k);
            }
        }
        if (otherTaxa.nodeToTaxa != null) {
            nodeToTaxa.putAll(otherTaxa.nodeToTaxa);
        }
    }

    /**
     * The taxa specified by the keys are to be arranged in a grid.
     *
     * @param keys the keys of the taxa to be arranged in a grid.
     */
    public void setArrangeRectangularly(final Iterable<Integer> keys) {
        arrangeRectangularly.clear();
        for (final Integer k : keys) {
            arrangeRectangularly.add(k);
        }
    }

    public boolean isArrangeRectangularly(final int key) {
        return arrangeRectangularly.contains(key);
    }

    /**
     * Find all taxa consisting of a single member and merge them into one new
     * taxon.
     *
     * @return The key of the taxon. (It doesn't matter which vertex is the key,
     * so any vertex will do.)
     */
    public int mergeSingletonTaxa() {
        final Set<Integer> singletons = new HashSet<>();

        for (final Iterator<Integer> ii = taxa.keySet().iterator(); ii.hasNext();) {
            final Integer k = ii.next();
            final Set<Integer> members = taxa.get(k);
            if (members.size() == 1) {
                singletons.add(k);
                ii.remove();
            }
        }

        if (!singletons.isEmpty()) {
            // We don't care which vertex id we use for the key.
            singletonKey = singletons.iterator().next();
            taxa.put(singletonKey, singletons);

            return singletonKey;
        }

        return Graph.NOT_FOUND;
    }

    /**
     * Find all taxa consisting of two connected members and merge them into one
     * new taxon.
     *
     * @return A member of the doublet as the key of the new taxon.
     */
    public int mergeDoubletTaxa() {
        final Set<Integer> doublets = new HashSet<>();

        for (final Iterator<Integer> ii = taxa.keySet().iterator(); ii.hasNext();) {
            final Integer k = ii.next();
            final Set<Integer> members = taxa.get(k);
            if (members.size() == 2) {
                // Check if the members are actually linked and that we don't
                // have a taxa containing two singletons.
                final Integer[] twoVerts = new Integer[2];
                members.toArray(twoVerts);
                if (wg.getVertexNeighbourCount(twoVerts[0]) == 0) {
                    continue;
                }

                for (final Integer m : members) {
                    doublets.add(m);
                }
                ii.remove();
            }
        }

        if (!doublets.isEmpty()) {
            // We don't care which vertex id we use for the key.
            doubletKey = doublets.iterator().next();
            taxa.put(doubletKey, doublets);

            return doubletKey;
        }

        return Graph.NOT_FOUND;
    }

    /**
     * Create a graph that represents the relationship between the taxa.
     * <p>
     * Vertices in the new graph are the taxa keys. A transaction in the new
     * graph from vertex A to vertex B represents all transactions from any
     * vertex in taxon A to any vertex in taxon B.
     * <p>
     * @throws java.lang.InterruptedException If the thread is interrupted.
     *
     * @return A Condensation representing the relationship between the taxa.
     *
     * TODO: sometimes its not worth adding the transactions.
     */
    public Condensation getCondensedGraph() throws InterruptedException {
        final Map<Integer, Integer> cVxIdToTaxonKey = new HashMap<>();
        final Map<Integer, Integer> taxonKeyToVxId = new HashMap<>();

        final GraphWriteMethods condensedGraph = new StoreGraph(wg.getSchema());
        final int cxAttr = VisualConcept.VertexAttribute.X.ensure(condensedGraph);
        final int cyAttr = VisualConcept.VertexAttribute.Y.ensure(condensedGraph);
        final int czAttr = VisualConcept.VertexAttribute.Z.ensure(condensedGraph);
        final int cxOrigId = condensedGraph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, X_ORIG, X_ORIG, null, null);
        final int cyOrigId = condensedGraph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, Y_ORIG, Y_ORIG, null, null);
        final int czOrigId = condensedGraph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, Z_ORIG, Z_ORIG, null, null);
        final int cnRadiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(condensedGraph);
        final int cRadiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.ensure(condensedGraph);

        // Add the vertices.
        // TODO: do these need to be sorted?
        for (final Integer k : getSortedTaxaKeys()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (taxa.get(k).isEmpty()) {
                continue;
            }

            final int cVxId = condensedGraph.addVertex();

            final BitSet vertices = new BitSet();
            for (final Integer member : taxa.get(k)) {
                vertices.set(member);
            }

            // Figure out where and how big the new vertex will be.
            final Extent extent = Extent.getExtent(wg, vertices);

            // The condensation has the positions of the extents and the positions of the taxon key vertices.
            // The x,y,z will be modified, so remember them for repositioning later.
            condensedGraph.setFloatValue(cxAttr, cVxId, extent.getX());
            condensedGraph.setFloatValue(cyAttr, cVxId, extent.getY());
            condensedGraph.setFloatValue(czAttr, cVxId, extent.getZ());
            condensedGraph.setFloatValue(cxOrigId, cVxId, extent.getX());
            condensedGraph.setFloatValue(cyOrigId, cVxId, extent.getY());
            condensedGraph.setFloatValue(czOrigId, cVxId, extent.getZ());
            condensedGraph.setFloatValue(cnRadiusAttr, cVxId, extent.getNRadius());
            condensedGraph.setFloatValue(cRadiusAttr, cVxId, extent.getLRadius());
            cVxIdToTaxonKey.put(cVxId, k);
            taxonKeyToVxId.put(k, cVxId);
        }

        // Add the transactions.
        // We search through all of the taxa to find sources,
        // but only look in the remaining taxa for destinations,
        // otherwise we end up with two transactions between each vertex.
        final ArrayDeque<Integer> sources = new ArrayDeque<>();
        sources.addAll(taxa.keySet());

        while (!sources.isEmpty()) {
            // If we already have a transaction from the current source to a particular destination,
            // don't add another one.
            final Set<Integer> found = new HashSet<>();

            final Integer src = sources.removeFirst();
            found.add(src);
            final Set<Integer> members = taxa.get(src);
            for (final Integer mm : members) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int m = mm;
                final int nNeighbours = wg.getVertexNeighbourCount(m);
                for (int position = 0; position < nNeighbours; position++) {
                    final int nbId = wg.getVertexNeighbour(m, position);
                    final Integer dst = nodeToTaxa != null ? nodeToTaxa.get(nbId) : findTaxonContainingVertex(sources, nbId);
                    // Found the test for null was required to avoid issues
                    if (dst != null && dst != Graph.NOT_FOUND && !found.contains(dst)) {
                        condensedGraph.addTransaction(taxonKeyToVxId.get(src), taxonKeyToVxId.get(dst), true);
                        found.add(dst);
                    }
                }
            }
        }

        return new Condensation(condensedGraph, cVxIdToTaxonKey);
    }

    /**
     * Reposition the taxa with respect to the condensed graph.
     * <p>
     * Each vertex in the condensed graph represents a taxon in the taxonomy in
     * that each vertex in the condensation is the key of a taxon. The condensed
     * graph has been arranged, now we want to arrange the original graph to
     * match.
     * <p>
     * For each taxon, move the member vertices by the same distance that the
     * key has been moved.
     *
     * @param c The condensed graph and condensation data.
     *
     * @throws java.lang.InterruptedException If the thread was interrupted.
     */
    public void reposition(final Condensation c) throws InterruptedException {
        final GraphReadMethods crg = c.wg;

        // The x,y,z attributes in the original graph; these values will be updated
        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);

        // The post-arrangement x,y,z attributes of the vertices in the condensed graph.
        final int cxAttr = VisualConcept.VertexAttribute.X.get(crg);
        final int cyAttr = VisualConcept.VertexAttribute.Y.get(crg);
        final int czAttr = VisualConcept.VertexAttribute.Z.get(crg);

        // The x,y,z attributes containing the original values of the vertices in the condensed graph.
        final int cxOrigId = crg.getAttribute(GraphElementType.VERTEX, X_ORIG);
        final int cyOrigId = crg.getAttribute(GraphElementType.VERTEX, Y_ORIG);
        final int czOrigId = crg.getAttribute(GraphElementType.VERTEX, Z_ORIG);

        // For each taxon, reposition the original vertices to match the repositioning of the condensed graph vertices.
        final int cVxCount = crg.getVertexCount();
        for (int position = 0; position < cVxCount; position++) {
            final int cVxId = crg.getVertex(position);

            final float cx = crg.getFloatValue(cxAttr, cVxId);
            final float cy = crg.getFloatValue(cyAttr, cVxId);
            final float cz = crg.getFloatValue(czAttr, cVxId);

            final float cxorig = crg.getFloatValue(cxOrigId, cVxId);
            final float cyorig = crg.getFloatValue(cyOrigId, cVxId);
            final float czorig = crg.getFloatValue(czOrigId, cVxId);

            // How far did the taxon key vertex move? delta = newposition - oldposition.
            final float dx = cx - cxorig;
            final float dy = cy - cyorig;
            final float dz = cz - czorig;

            final int k = c.cVxIdToTaxonKey.get(cVxId);
            for (final int vxId : taxa.get(k)) {
                final float x = wg.getFloatValue(xAttr, vxId);
                final float y = wg.getFloatValue(yAttr, vxId);
                final float z = wg.getFloatValue(zAttr, vxId);

                wg.setFloatValue(xAttr, vxId, x + dx);
                wg.setFloatValue(yAttr, vxId, y + dy);
                wg.setFloatValue(zAttr, vxId, z + dz);
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    /**
     * A Condensation is a representation of a graph that has been been divided
     * into taxa.
     * <p>
     * A taxon is a collection of vertices: a vertex appears in one taxon. A
     * taxon is represented by a key. The key is used as a reference to an
     * extent representing the size of the taxon in the original graph.
     */
    public static class Condensation {

        public final GraphWriteMethods wg;
        public final Map<Integer, Integer> cVxIdToTaxonKey;

        /**
         * Construct a new Condensation instance.
         *
         * @param graph The graph that this condensation was built from.
         * @param cVxIdToTaxonKey A mapping of vertexId to taxon.
         */
        public Condensation(final GraphWriteMethods graph, final Map<Integer, Integer> cVxIdToTaxonKey) {
            this.wg = graph;
            this.cVxIdToTaxonKey = cVxIdToTaxonKey;
        }
    }

    /**
     * Return the taxa keys sorted in order of number of members.
     * <p>
     * The singleton taxon is always first, the doublet taxon is always second,
     * the rest are ordered by size.
     *
     * @return An ArrayList containing taxa sorted as described above.
     */
    private ArrayList<Integer> getSortedTaxaKeys() {
        final ArrayList<Integer> keys = new ArrayList<>(taxa.size());
        for (final Integer k : taxa.keySet()) {
            keys.add(k);
        }

        Collections.sort(keys, (final Integer key1, final Integer key2) -> {
            if (key1 == singletonKey) {
                return -1;
            } else if (key2 == singletonKey) {
                return 1;
            } else if (key1 == doubletKey) {
                return -1;
            } else if (key2 == doubletKey) {
                return 1;
            } else {
                return taxa.get(key1).size() - taxa.get(key2).size();
            }
        });

        return keys;
    }

    /**
     * Look through the taxa specified by dstKeys to find the taxon containing
     * vxId.
     *
     * Returning Graph.NOT_FOUND is valid because we won't find neighbours that
     * are already in the tlGraph.
     *
     * @param dstKeys A List of taxon keys.
     * @param vxId A vertex id from the original graph to search for.
     *
     * @return The key of the taxon that contains the given vertex.
     */
    private Integer findTaxonContainingVertex(final ArrayDeque<Integer> dstKeys, final Integer vxId) {
        for (final Integer k : dstKeys) {
            final Set<Integer> members = taxa.get(k);
            if (members.contains(vxId)) {
                return k;
            }
        }

        return Graph.NOT_FOUND;
    }
}
