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
package au.gov.asd.tac.constellation.plugins.arrangements.hierarchical;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.circle.CircleArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Hierarchical layout (Sugiyama based).
 * <p>
 * The hierarchy is not based on the direction of transactions between vertices.
 * Instead, one or more roots are specified. These are placed at level zero.
 * Vertices directly connected to either of these roots are at level one,
 * vertices directly connected to level one are at level two, etc, irregardless
 * of transaction direction.
 * <p>
 * Therefore, a parent vertex is not a vertex at the source end of an incoming
 * transaction, it is a connected vertex at level-1 (ie the layer immediately
 * above).
 *
 * @author algol
 */
public class HierarchicalArranger implements Arranger {

    private static final int MIN_PENDANTS = 3;
    private static final int MAX_SWAPS = 10;

    private final Set<Integer> roots;
    private boolean maintainMean;

    public HierarchicalArranger(final Set<Integer> roots) {
        this.roots = new HashSet<>(roots);
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int vxCount = wg.getVertexCount();
        if (roots.isEmpty() || vxCount < 3) {
            return;
        }

        // Which non-root vertices are pendants?
        // We don't want pendants in the hierarchical graph, because if there
        // are too many of them they make the tree ridiculously wide.
        final Map<Integer, Set<Integer>> pendantSets = new HashMap<>();
        findPendants(wg, roots, MIN_PENDANTS, pendantSets);

        // Collect the pendants into a Set for easy checking.
        final Set<Integer> pendants = new HashSet<>();
        for (final Set<Integer> value : pendantSets.values()) {
            for (final Integer i : value) {
                pendants.add(i);
            }
        }

        // Find out how far away each vertex is from each root.
        // Vertex vxId is at level levels[vxId].
        final int[] levels = new int[wg.getVertexCapacity()];
        Arrays.fill(levels, Integer.MAX_VALUE);

        int maxLevel = 0;
        for (final int root : roots) {
            // Is this root in this graph?
            // If this an inclusion graph, it may very well not be.
            // If none of the roots are in this graph, then nothing will happen.
            if (wg.vertexExists(root)) {
                maxLevel = Math.max(maxLevel, assignLevels(wg, root, pendants, levels));
            }

        }

        // If none of the roots were in this graph, invent a root and do it.
        // We don't want to have to make the user select something in every component.
        if (maxLevel == 0) {
            maxLevel = Math.max(maxLevel, assignLevels(wg, wg.getVertex(0), pendants, levels));
        }

        // Now build a structure that holds a per-level list of vertices: ie vxLevels.get(i) contains the vertices at level i.
        // We couldn't build this before, because multiple roots will almost certainly cause vertices to change levels.
        final ArrayList<ArrayList<Integer>> vxLevels = new ArrayList<>();
        for (int i = 0; i <= maxLevel; i++) {
            vxLevels.add(new ArrayList<>());
        }

        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int level = levels[vxId];
            if (level >= 0 && level < Integer.MAX_VALUE) {
                vxLevels.get(level).add(vxId);
            }
        }

        // This is the part where line crossing minimisation is done.
        // if you want to fancy up the algorithm, this is where to concentrate.
        final float[] weights = new float[wg.getVertexCapacity()];
        Arrays.fill(weights, 100);
        for (int i = 0; i < MAX_SWAPS; i++) {
            boolean upChange = false;
            boolean downChange = false;
            for (int level = 1; level <= maxLevel; level++) {
                upChange = calculateAndSortWeights(wg, vxLevels, level, weights);
            }
            for (int level = maxLevel; level >= 1; level--) {
                downChange = calculateAndSortWeights(wg, vxLevels, level, weights);
            }

            if (!upChange && !downChange) {
                break;
            }
        }

        arrangeVertices(wg, vxLevels);

        // We don't know what to do with the pendants.
        // We'll try arranging them separately until someone comes up with a better idea.
        arrangePendants(wg, pendantSets);

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }

    /**
     * Find the pendants in a graph.
     * <p>
     * Not just find the pendants, but build a taxonomy where each taxon
     * contains the pendants of a single vertex. This will be convenient for
     * arranging them separately later.
     *
     * @param rg The graph to inspect.
     * @param roots A vertex is not a pendant if it is a root.
     * @param minPendants If there are less than this many pendants, teat them
     * like normal vertices.
     * @param pendantSets The sets of pendants.
     */
    private static void findPendants(final GraphReadMethods rg, final Set<Integer> roots, final int minPendants, final Map<Integer, Set<Integer>> pendantSets) {
        final int vxCount = rg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = rg.getVertex(position);

            if (!roots.contains(vxId)) {
                final int nCount = rg.getVertexNeighbourCount(vxId);
                if (nCount >= minPendants) {
                    final Set<Integer> pendants = new HashSet<>();
                    for (int lposition = 0; lposition < nCount; lposition++) {
                        final int nId = rg.getVertexNeighbour(vxId, lposition);
                        if (rg.getVertexNeighbourCount(nId) == 1) {
                            pendants.add(nId);
                        }
                    }

                    if (pendants.size() >= minPendants) {
                        pendantSets.put(pendants.iterator().next(), pendants);
                    }
                }
            }
        }
    }

    /**
     * Assign a level (distance from the root vertex) to each vertex.
     * <p>
     * The level of a vertex is the number of hops from the specified root. A
     * breadth first search is done starting at the root to assign a level to
     * each vertex.
     * <p>
     * This is called once for each root. If a vertex already has a level from a
     * previous call (because the vertex is closer to this root than any
     * previous root), the minimum level is used.
     * <p>
     * Pendants are a problem; if there are lots of them they just clutter up
     * the hierarchy. We assign them a pseudo-level of -1 so they can be dealt
     * with separately.
     *
     * @param wg
     * @param root
     * @param pendants
     * @param levels
     *
     * @return The maximum level that was assigned.
     */
    private static int assignLevels(final GraphWriteMethods wg, final int root, final Set<Integer> pendants, final int[] levels) {
        int maxLevel = 0;
        levels[root] = 0;
        final ArrayDeque<Integer> neighbourQueue = new ArrayDeque<>();
        neighbourQueue.addLast(root);

        while (!neighbourQueue.isEmpty()) {
            final int currentVxId = neighbourQueue.removeFirst();
            for (int lposition = 0; lposition < wg.getVertexLinkCount(currentVxId); lposition++) {
                final int linkId = wg.getVertexLink(currentVxId, lposition);

                final int neighbourVxId = GraphElementType.LINK.getOtherVertex(wg, linkId, currentVxId);
                final boolean isPendant = pendants.contains(neighbourVxId);
                if (isPendant) {
                    levels[neighbourVxId] = -1;
                } else if (levels[currentVxId] + 1 < levels[neighbourVxId]) {
                    levels[neighbourVxId] = levels[currentVxId] + 1;
                    neighbourQueue.addLast(neighbourVxId);
                    maxLevel = Math.max(maxLevel, levels[neighbourVxId]);
                }
            }
        }

        return maxLevel;
    }

    private static boolean calculateAndSortWeights(final GraphReadMethods rg, final ArrayList<ArrayList<Integer>> vxLevels, final int level, final float[] weights) {
        boolean reordered = false;
        final ArrayList<Integer> vxLevel = vxLevels.get(level);
        final ArrayList<Integer> vxLevelCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
        final ArrayList<Integer> vxParentLevel = vxLevels.get(level - 1);
        for (final int vxId : vxLevelCopy) {
            // Get "parent" vertices (vertices at level-1).
            float weight = 0;
            int nParents = 0;
            for (int lposition = 0; lposition < rg.getVertexNeighbourCount(vxId); lposition++) {
                final int nId = rg.getVertexNeighbour(vxId, lposition);
                if (vxParentLevel.contains(nId)) {
                    nParents++;
                    weight += nParents * weights[nId];
                }
            }

            if (nParents > 0) {
                weight = weight / nParents;
                final float prevWeight = weights[vxId];
                if (weight != prevWeight) {
                    reordered = true;
                }

                weights[vxId] = weight;
            }

            sortLevelByWeight(vxLevel, weights);
        }

        return reordered;
    }

    private static void sortLevelByWeight(final ArrayList<Integer> vxLevel, final float[] weights) {
        Collections.sort(vxLevel, (vxId1, vxId2) -> {
            final float weight1 = weights[vxId1];
            final float weight2 = weights[vxId2];
            return Float.compare(weight1, weight2);
        });
    }

    /**
     * Arrange the vertices in a simple tree with the roots at the top.
     *
     * @param wg
     * @param vxLevels
     */
    private static void arrangeVertices(final GraphWriteMethods wg, final ArrayList<ArrayList<Integer>> vxLevels) {
        final float xgap = 4;
        final float ygap = 16;

        int maxLevelVertices = 0;
        for (ArrayList<Integer> vxLevel : vxLevels) {
            maxLevelVertices = Math.max(maxLevelVertices, vxLevel.size());
        }

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        for (int level = 0; level < vxLevels.size(); level++) {
            final ArrayList<Integer> vxLevel = vxLevels.get(level);
            final int levelVertices = vxLevel.size();
            final float startxgap = (maxLevelVertices - levelVertices) * xgap / 2F;
            for (int i = 0; i < vxLevel.size(); i++) {
                final int vxId = vxLevel.get(i);
                wg.setFloatValue(xId, vxId, startxgap + i * xgap);
                wg.setFloatValue(yId, vxId, -level * ygap);
                wg.setFloatValue(zId, vxId, 0);
            }
        }
    }

    private static void arrangePendants(final GraphWriteMethods wg, final Map<Integer, Set<Integer>> pendantSets) throws InterruptedException {
        final ExplicitTaxonomyArranger arranger = new ExplicitTaxonomyArranger(pendantSets);
        arranger.arrange(wg);
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

    /**
     * An arranger where the taxonomy has been pre-built.
     */
    private static class ExplicitTaxonomyArranger extends GraphTaxonomyArranger {

        private final Map<Integer, Set<Integer>> taxa;

        public ExplicitTaxonomyArranger(final Map<Integer, Set<Integer>> taxa) {
            super(new GridArranger(), new CircleArranger(), Connections.NONE, InducedSubgraph.getSubgraphFactory());
            this.taxa = new HashMap<>(taxa);
        }

        @Override
        protected GraphTaxonomy getTaxonomy(final GraphWriteMethods wg) {
            return new GraphTaxonomy(wg, taxa);
        }

    }
}
