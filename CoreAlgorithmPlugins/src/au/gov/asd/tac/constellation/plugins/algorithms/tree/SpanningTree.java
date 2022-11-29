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
package au.gov.asd.tac.constellation.plugins.algorithms.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Determine a graph's minimal or maximal spanning tree using Kruskal's
 * algorithm.
 * <p>
 * http://en.wikipedia.org/wiki/Kruskal's_algorithm
 * <pre>
 * KRUSKAL(G):
 * 1 A = ∅
 * 2 foreach v ∈ G.V:
 * 3   MAKE-SET(v)
 * 4 foreach (u, v) ordered by weight(u, v), increasing:
 * 5    if FIND-SET(u) ≠ FIND-SET(v):
 * 6       A = A ∪ {(u, v)}
 * 7       UNION(u, v)
 * 8 return A
 * </pre>
 * <p>
 * We use the transaction count as the weight of a link.
 * <p>
 * A new graph will be returned containing undirected transactions. The
 * origVxToTree and treeVxToOrig int arrays map the vertex ids of the graphs to
 * each other.
 * <p>
 * If a root vertex id is specified (!=Graph.NOT_FOUND), the transactions in the
 * tree will be modified to be directed with the equivalent tree vertex at the
 * root.
 *
 * @author algol
 */
public final class SpanningTree {

    private final GraphWriteMethods wg;
    private final int txSelectedId;

    private int[] origVxToTree;
    private int[] treeVxToOrig;

    public SpanningTree(final GraphWriteMethods wg) {
        this.wg = wg;
        txSelectedId = wg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
    }

    /**
     * Create a new graph representing a spanning tree of the original graph.
     *
     * @param isMinimal true for a minimal spanning tree, False for a maximal
     * spanning tree.
     * @param selectTxs if True, select the transactions in the tree in the
     * original graph.
     * @param rootVxId if specified (!=Graph.NOT_FOUND), make the tree's
     * transactions directed.
     *
     * @return A new GraphWriteMethods representing the spanning tree.
     */
    public GraphWriteMethods createSpanningTree(final boolean isMinimal, final boolean selectTxs, final int rootVxId) {
        final GraphWriteMethods tree = new StoreGraph();
        final int nradiusTreeAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(tree, false);
        final int nradiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(wg);
        final int vxCount = wg.getVertexCount();

        // Get a list of all links sorted by weight.
        final int linkCount = wg.getLinkCount();
        final ArrayList<Integer> links = new ArrayList<>(linkCount);
        for (int position = 0; position < linkCount; position++) {
            final int linkId = wg.getLink(position);
            links.add(linkId);
        }

        Collections.sort(links, new LinkSorter(wg, isMinimal));

        // Put each vertex in its own tree (where a tree is conveniently named after its root.
        final Map<Integer, Set<Integer>> treeVxs = new HashMap<>();
        final int[] vxTrees = new int[wg.getVertexCapacity()];
        Arrays.fill(vxTrees, Graph.NOT_FOUND);
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final Set<Integer> s = new HashSet<>();
            s.add(vxId);
            treeVxs.put(vxId, s);
            vxTrees[vxId] = vxId;
        }

        // Build the new graph to contain the tree we'll build from the original graph.
        origVxToTree = new int[wg.getVertexCapacity()];
        Arrays.fill(origVxToTree, Graph.NOT_FOUND);
        treeVxToOrig = new int[wg.getVertexCapacity()];
        Arrays.fill(treeVxToOrig, Graph.NOT_FOUND);

        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int treeVxId = tree.addVertex();
            final float nradius = nradiusAttr != Graph.NOT_FOUND ? wg.getFloatValue(nradiusAttr, vxId) : 1;
            tree.setFloatValue(nradiusTreeAttr, treeVxId, nradius);

            origVxToTree[vxId] = treeVxId;
            treeVxToOrig[treeVxId] = vxId;
        }

        // Iterate through the sorted links, looking for vertices in different trees.
        final Set<Integer> spanningLinks = new HashSet<>();
        for (final Integer linkId : links) {
            final int vx0Id = wg.getLinkLowVertex(linkId);
            final int vx1Id = wg.getLinkHighVertex(linkId);
            final int tree1Name = vxTrees[vx0Id];
            final int tree2Name = vxTrees[vx1Id];
            if (tree1Name != tree2Name) {
                final Set<Integer> s1 = treeVxs.get(tree1Name);
                final Set<Integer> s2 = treeVxs.get(tree2Name);
                treeVxs.remove(tree2Name);
                s1.addAll(s2);
                for (final Integer vx2 : s2) {
                    vxTrees[vx2] = tree1Name;
                }
                spanningLinks.add(linkId);
                final int tvx0Id = origVxToTree[vx0Id];
                final int tvx1Id = origVxToTree[vx1Id];
                tree.addTransaction(tvx0Id, tvx1Id, false);
            }
        }

        // Make sure we have all of the vertices.
        assert tree.getVertexCount() == wg.getVertexCount();

        // This assert will fail if the graph has more than one component.
        assert treeVxs.size() == 1;

        if (rootVxId != Graph.NOT_FOUND) {
            final int treeRootVxId = origVxToTree[rootVxId];
            rootTree(tree, treeRootVxId);
        }

        if (selectTxs) {
            for (final Iterator<Integer> e = spanningLinks.iterator(); e.hasNext();) {
                final int slinkId = e.next();

                final int ltxCount = wg.getLinkTransactionCount(slinkId);
                for (int lpos = 0; lpos < ltxCount; lpos++) {
                    final int txId = wg.getLinkTransaction(slinkId, lpos);
                    wg.setBooleanValue(txSelectedId, txId, true);
                }
            }
        }

        return tree;
    }

    public int convertTreeVxToOrig(final int vxId) {
        return treeVxToOrig[vxId];
    }

    public int convertOrigVxToTree(final int vxId) {
        return origVxToTree[vxId];
    }

    /**
     * Convert a tree with undirected transactions to a rooted tree with
     * directed transactions.
     *
     * @param tree
     * @param rootVxId
     */
    private void rootTree(final GraphWriteMethods tree, final int rootVxId) {
        // Start at the root and collect its undirected transactions.
        final int txCount = tree.getVertexTransactionCount(rootVxId, Graph.FLAT);
        final int[] txIds = new int[txCount];
        for (int position = 0; position < txCount; position++) {
            final int txId = tree.getVertexTransaction(rootVxId, Graph.FLAT, position);

            txIds[position] = txId;
        }

        // Convert the root's undirected transactions to directed transactions,
        // then recursively do the same for the children of root.
        for (final int txId : txIds) {
            final int dstVxId = GraphElementType.TRANSACTION.getOtherVertex(tree, txId, rootVxId);
            tree.removeTransaction(txId);
            tree.addTransaction(rootVxId, dstVxId, true);

            rootTree(tree, dstVxId);
        }
    }

    private static class LinkSorter implements Comparator<Integer> {

        private final GraphWriteMethods wg;
        private final int comp;

        protected LinkSorter(final GraphWriteMethods wg, final boolean isMinimal) {
            this.wg = wg;
            this.comp = isMinimal ? 1 : -1;
        }

        @Override
        public int compare(final Integer link0Id, final Integer link1Id) {
            final int weight0 = wg.getLinkTransactionCount(link0Id);
            final int weight1 = wg.getLinkTransactionCount(link1Id);

            return comp * (weight0 - weight1);
        }
    }
}
