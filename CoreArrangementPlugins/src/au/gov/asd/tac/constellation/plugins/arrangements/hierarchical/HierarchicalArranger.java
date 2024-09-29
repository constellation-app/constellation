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
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
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

        // Find out how far away each vertex is from each root.
        // Vertex vxId is at level levels[vxId].
        Map<Integer, Integer> levels = new HashMap<>();
        for (int i = 0; i < vxCount; i++) {
            levels.put(wg.getVertex(i), Integer.MAX_VALUE);
        }

        int maxLevel = 0;
        for (final int root : roots) {
            // Is this root in this graph?
            // If this an inclusion graph, it may very well not be.
            // If none of the roots are in this graph, then nothing will happen.
            if (wg.vertexExists(root)) {
                maxLevel = Math.max(maxLevel, assignLevels(wg, root, levels));
            }
        }

        // If none of the roots were in this graph, invent a root and do it.
        // We don't want to have to make the user select something in every component.
        if (maxLevel == 0) {
            maxLevel = Math.max(maxLevel, assignLevels(wg, wg.getVertex(0), levels));
        }

        // Now build a structure that holds a per-level list of vertices: ie vxLevels.get(i) contains the vertices at level i.
        // We couldn't build this before, because multiple roots will almost certainly cause vertices to change levels.
        final ArrayList<ArrayList<Integer>> vxLevels = new ArrayList<>();
        for (int i = 0; i <= maxLevel; i++) {
            vxLevels.add(new ArrayList<>());
        }

        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int level = levels.get(vxId);
            if (level >= 0 && level < Integer.MAX_VALUE) {
                vxLevels.get(level).add(vxId);
            }
        }
        
        // This is the part where line crossing minimisation is done.
        // if you want to fancy up the algorithm, this is where to concentrate.
        
        Map<Integer, Float> weights = new HashMap<>();
        for (int i = 0; i < vxCount; i++) {
            weights.put(wg.getVertex(i), 100.0F);
        }
        
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

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
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
    private static int assignLevels(final GraphWriteMethods wg, final int root, final Map<Integer, Integer> levels) {
        int maxLevel = 0;
        levels.put(root, 0);
        final ArrayDeque<Integer> neighbourQueue = new ArrayDeque<>();
        neighbourQueue.addLast(root);

        while (!neighbourQueue.isEmpty()) {
            final int currentVxId = neighbourQueue.removeFirst();
            for (int lposition = 0; lposition < wg.getVertexLinkCount(currentVxId); lposition++) {
                final int linkId = wg.getVertexLink(currentVxId, lposition);

                final int neighbourVxId = GraphElementType.LINK.getOtherVertex(wg, linkId, currentVxId);
                if (levels.get(currentVxId) + 1 < levels.get(neighbourVxId)) {
                    levels.put(neighbourVxId, levels.get(currentVxId) + 1);
                    neighbourQueue.addLast(neighbourVxId);
                    maxLevel = Math.max(maxLevel, levels.get(neighbourVxId));
                }
            }
        }

        return maxLevel;
    }

    private static boolean calculateAndSortWeights(final GraphReadMethods rg, final ArrayList<ArrayList<Integer>> vxLevels, final int level, final Map<Integer, Float> weights) {
        boolean reordered = false;
        final ArrayList<Integer> vxLevel = vxLevels.get(level);
        final ArrayList<Integer> vxLevelCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
        final ArrayList<Integer> vxParentLevel = vxLevels.get(level - 1);
        for (final int vxId : vxLevelCopy) {
            float weight = 0;
            int nParents = 0;
            int nChildren = 0;
            for (int lposition = 0; lposition < rg.getVertexNeighbourCount(vxId); lposition++) {
                final int nId = rg.getVertexNeighbour(vxId, lposition);
                if (vxParentLevel.contains(nId)) {
                    nParents++;
                    weight += 100;
                } else if (!vxLevelCopy.contains(nId)) {
                    nChildren++;
                    weight += 200;
                }
            }

            final float prevWeight = weights.get(vxId);
            if (weight != prevWeight) {
                reordered = true;
            }

            weights.put(vxId, weight);
            sortLevelByWeight(vxLevel, weights);
            busyCentreOrder(vxLevel);
        }

        return reordered;
    }

    private static void sortLevelByWeight(final ArrayList<Integer> vxLevel, final Map<Integer, Float> weights) {
        Collections.sort(vxLevel, (vxId1, vxId2) -> {
            final float weight1 = weights.get(vxId1);
            final float weight2 = weights.get(vxId2);
            return Float.compare(weight1, weight2);
        });
    }

    private static void busyCentreOrder(final ArrayList<Integer> vxLevel) {
        final ArrayList<Integer> vxLevelCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
        int vxSize = vxLevel.size();
        vxLevel.clear();
        boolean toggle = true;
        for (int i = vxSize - 1; i > -1; i--) {
            if (toggle) {
                vxLevel.add(vxLevelCopy.get(i));
            } else {
                vxLevel.add(0, vxLevelCopy.get(i));
            }
            toggle = !toggle;
        }

        // this code makes the outer segments the busier ones, which seems to produce better arrangements in post-processing
        for (int i = 0; i < vxSize/2; i++) {
            vxLevel.add(vxLevel.get(0));
            vxLevel.remove(0);
        }
    }

    /**
     * Arrange the vertices in a simple tree with the roots at the top.
     *
     * @param wg
     * @param vxLevels
     */
    private static void arrangeVertices(final GraphWriteMethods wg, final ArrayList<ArrayList<Integer>> vxLevels) {

        int maxLevelVertices = 0;
        int totalVertices = wg.getVertexCount();
        for (ArrayList<Integer> vxLevel : vxLevels) {
            maxLevelVertices = Math.max(maxLevelVertices, vxLevel.size());
        }
        final int maxNodesPerRow = (int) Math.max(12, 12*Math.log(totalVertices));
        if (maxLevelVertices > maxNodesPerRow) {
            maxLevelVertices = maxNodesPerRow;
        }
        final float xgap = 10;
        final float ygap = 10 + (float) (5 * Math.log(maxLevelVertices));

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        double xMinAdj = Math.max(Math.min(Math.log(maxLevelVertices)*2, 9), 0);
        
        double displayLevel = -1.0;
        int verticesRemaining;
        int verticesForRow;
        int vertexCounter;
        int virtualRowLevel = -1;
        final double xMinDefaultAdj = Math.max(Math.min(Math.log(maxNodesPerRow)*2, 9), 0);
        double yStep;
        double yAdj;
        double xAdj;
        double yDir;
        final int totalLevels = vxLevels.size();
        int prevVertices = 0;
        for (int level = 0; level < totalLevels; level++) {
            vertexCounter = -1;
            displayLevel += 1.0;
            virtualRowLevel++;
            final ArrayList<Integer> vxLevel = vxLevels.get(level);
            final int levelVertices = vxLevel.size();
            displayLevel += Math.max(2 * Math.log(prevVertices + levelVertices) - 5 , 0);
            verticesRemaining = levelVertices;

            while (vertexCounter < levelVertices - 1) {
                verticesForRow = verticesRemaining > maxNodesPerRow ? maxNodesPerRow : verticesRemaining;
                yStep = 0;
                yAdj = 0;
                xAdj = 0;
                yDir = 0;
                if (verticesForRow > 4) {
                    yAdj = Math.max(0, Math.min((ygap/2)*(Math.log10(verticesForRow)-1), ygap/2));
                    yStep = yAdj;
                    yDir = 2 * yAdj / verticesForRow;
                    xAdj = Math.max(Math.min(Math.log(verticesForRow)*2, 9), 0);
                }
 
                final float xMaxOffset = maxLevelVertices <= maxNodesPerRow ? maxLevelVertices * (xgap - (float) xMinAdj) / 2F : maxNodesPerRow * (xgap - (float) xMinDefaultAdj) / 2F;
                final float xLevelOffset = verticesForRow <= maxNodesPerRow ? xMaxOffset - verticesForRow * (xgap - (float) xAdj) / 2F : maxNodesPerRow * (xgap - (float) xMinDefaultAdj) / 2F;

                float xSpacer[] = new float[verticesForRow];
                Arrays.fill(xSpacer, (verticesForRow/2) * ((float) xAdj / 8));
                for (int i = 0; i < verticesForRow/2; i++) {
                    xSpacer[i] = ((float) xAdj / 8) * i;
                }
                for (int i = verticesForRow - 1; i > verticesForRow/2; i--) {
                    xSpacer[i] = ((float) xAdj / 8) * (verticesForRow - i);
                }
                float xSpacerInc[] = new float[verticesForRow];
                float accumulation = 0.0F;
                for (int i = 0; i < verticesForRow; i++) {
                    accumulation += xSpacer[i];
                    xSpacerInc[i] = accumulation;
                }
                for (int i = 0; i < verticesForRow; i++) {
                    vertexCounter++;
                    final int vxId = vxLevel.get(vertexCounter);
                    
                    wg.setFloatValue(xId, vxId, xLevelOffset + i * (xgap - (float) xAdj) + xSpacerInc[i] - xSpacerInc[verticesForRow-1]/2);
                    wg.setFloatValue(yId, vxId, (float)(-displayLevel * ygap - (yStep > 0 ? -yStep : yStep )));
                    wg.setFloatValue(zId, vxId, 0);
                    yStep = yStep - yDir;
                }
                
                // split up on maxNodesPerRow
                if (verticesRemaining > maxNodesPerRow) {
                    verticesRemaining -= maxNodesPerRow;
                    displayLevel += 0.5;
                    virtualRowLevel++;
                }

            }
            prevVertices = levelVertices;
        }
        
        // the initial placement of nodes on each level is somewhat random
        // we can use smoothing to move the children and parent nodes to be closer aligned in the arrangement
        // this becomes time consuming with large graphs.
        // will we stop doing any post-processing when we reach 10,000 graph elements
        
        final int graphSize = totalVertices + wg.getTransactionCount();        
        final int passes;
        // multiple passes are used to smooth out the arrangement and reduce overlapping lines.
        if (graphSize > 10000) {
            // large interconnected graphs will have a very crowded arrangement and will take a long time to process, so
            // we do not try to apply any smoothing over the initial arrangement.
            passes = 0;
        } else if (graphSize > 1000) {
            // this is the point where the graph arrangement starts to become crowded, and the time to smooth out the arrangement becomes noticeable
            passes = (int) (30D * Math.pow(((11000D - (double)graphSize) / 10000D), 2)) + 1;
        } else {
            // smaller graphs can have more passes to produce an improved arrangement
            passes = 30; 
        }
        
        if (passes > 0) {            
            boolean finalAdjustment = false;
            long startTime = System.currentTimeMillis();
            long cutoffTime = startTime + 15000;
            long defaultProcessingTime = 3000; // gives small graphs enough time to produce a nicely laid out arrangement
            long currentTime = System.currentTimeMillis();
            long timeLimit; // limits the time spent on doing an individual smoothing task
            int parentChangeAmount; // the number of parent nodes moved on the final iteration of the task
            int childChangeAmount; // the number of child nodes moved on the final iteration of the task
            int totalChanges;
            int modVal = 1;
            int modInc = 2;
            int modAmount = -1;
            for (int n = 0; (n < passes && modAmount > 0) || (currentTime < startTime + defaultProcessingTime && modAmount != 0) ; n++) {
                parentChangeAmount = -1;
                totalChanges = 0;
                currentTime = System.currentTimeMillis();
                if (finalAdjustment || (n % modVal == 0 && currentTime < cutoffTime - defaultProcessingTime)) {
                    if (n > 0) {
                        modVal += modInc++; // increasing increment to avoid potential repetition cycle
                    }                    
                    minimiseTransactionDistances(wg, vxLevels);
                }
                currentTime = System.currentTimeMillis();
                if (currentTime > cutoffTime) {
                    break;
                }                
                timeLimit = currentTime + defaultProcessingTime/4;
                for (int k = 0; (k < 1 + passes/2 && parentChangeAmount != 0) || (currentTime < timeLimit && parentChangeAmount != 0); k++) { // multi-pass adjustments to move children below parents
                    parentChangeAmount = adjustArrangment(wg, vxLevels, false);
                    totalChanges += parentChangeAmount;
                    currentTime = System.currentTimeMillis();
                    if (currentTime > timeLimit) {
                        break;
                    }
                }
                if (currentTime > cutoffTime) {
                    break;
                }
                modAmount = totalChanges;
                totalChanges = 0;
                childChangeAmount = -1;
                currentTime = System.currentTimeMillis();
                timeLimit = currentTime + defaultProcessingTime/4;
                for (int k = 0; (k < 1 + passes/2 && childChangeAmount != 0) || (currentTime < timeLimit && childChangeAmount != 0); k++) { // multi-pass adjustments to move children below parents
                    childChangeAmount = adjustArrangment(wg, vxLevels, true);
                    totalChanges += childChangeAmount;
                    currentTime = System.currentTimeMillis();
                    if (currentTime > timeLimit) {
                        break;
                    }
                }
                if (finalAdjustment || currentTime > cutoffTime) {
                    break;
                }
                modAmount += totalChanges;
                if (n > 2 && (childChangeAmount == 0 || parentChangeAmount == 0)) {
                   finalAdjustment = true; 
                }
            }
        }

    }
    
    private static int adjustArrangment(final GraphWriteMethods wg, final ArrayList<ArrayList<Integer>> vxLevels, final boolean topDownScan){
        // when topDownScan = true : the parent nodes are being shifted to positions that are closer to their children on the next level
        // when topDownScan = false : the child nodes are being shifted to positions that are closer to their parents on the previous level
        
        int swapsMade = 0;
        final int rangeStart = topDownScan ? 0 : vxLevels.size() - 1;
        final int rangeEnd = topDownScan ? vxLevels.size() - 2 : 1;
        final int rangeIncrement = topDownScan ? 1 : -1;
        for (int parentLevel = rangeStart; ((parentLevel <= rangeEnd && topDownScan) || (parentLevel >= rangeEnd && !topDownScan) ); parentLevel += rangeIncrement) {
            final int scanLevel = topDownScan ? parentLevel + 1 : parentLevel - 1;
            final ArrayList<Integer> vxLevel = vxLevels.get(scanLevel);
            final ArrayList<Integer> vxLevelCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
            final ArrayList<Integer> vxLevelTempCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
            
            final ArrayList<Integer> vxParentLevel = vxLevels.get(parentLevel);
            final ArrayList<Integer> vxCurrentParentIds = new ArrayList<>();
            final ArrayList<Integer> vxTempParentIds = new ArrayList<>();
            double smallestDistance = 1000000;
            
            for (final int vxId : vxLevelCopy) {
                double comparisonDistance;
                double initialDistance = 0;
                vxCurrentParentIds.clear();
                for (int lposition = 0; lposition < wg.getVertexNeighbourCount(vxId); lposition++) {
                    final int nId = wg.getVertexNeighbour(vxId, lposition);
                    if (vxParentLevel.contains(nId)) {
                        vxCurrentParentIds.add(nId);
                        initialDistance += calculateDistance(wg, vxId, nId);
                    }
                }
                int pCount = vxCurrentParentIds.size();
                if (pCount > 1) {
                    initialDistance = initialDistance / pCount;
                }

                int swapTargetId = -1;
                for (final int vxTempId : vxLevelTempCopy) {
                    if (vxTempId == vxId) {
                        continue;
                    }

                    if (vxTempId == vxId) continue;
                    double tempInitialDistance = 0;
                    vxTempParentIds.clear();
                    for (int lposition = 0; lposition < wg.getVertexNeighbourCount(vxTempId); lposition++) {
                        final int nId = wg.getVertexNeighbour(vxTempId, lposition);
                        if (vxParentLevel.contains(nId)) {
                            vxTempParentIds.add(nId);
                            tempInitialDistance += calculateDistance(wg, vxTempId, nId);
                        }
                    }
                    int tpCount = vxTempParentIds.size();
                    if (tpCount > 1) {
                        tempInitialDistance = tempInitialDistance / tpCount;
                    }
                    comparisonDistance = initialDistance + tempInitialDistance;

                    double leftTempDistance = 0;                    
                    for (final int leftDx : vxCurrentParentIds) {
                        leftTempDistance += calculateDistance(wg, leftDx, vxTempId);
                    }
                    if (pCount > 1) {
                        leftTempDistance = leftTempDistance / pCount;
                    }

                    double rightTempDistance = 0;                    
                    for (final int rightDx : vxTempParentIds) {
                        rightTempDistance += calculateDistance(wg, rightDx, vxId);
                    }
                    if (tpCount > 1) {
                        rightTempDistance = rightTempDistance / tpCount;
                    }

                    if (leftTempDistance + rightTempDistance < comparisonDistance && comparisonDistance < smallestDistance) {
                        smallestDistance = leftTempDistance + rightTempDistance;
                        swapTargetId = vxTempId;
                    }
                }

                if (swapTargetId > -1) {
                    swapsMade++;
                    swapVertexPositions(wg, swapTargetId, vxId);
                }
            }
        }
        return swapsMade;
    }
    
    private static int minimiseTransactionDistances(final GraphWriteMethods wg, final ArrayList<ArrayList<Integer>> vxLevels){
        // check each node on a level and see if switching positions with another node reduces the total length 
        // of transactions to all neightbours on different levels for both nodes being switched.
        
        int swapsMade = 0;
        final int rangeStart = 0 ;
        final int rangeEnd = vxLevels.size();
        
        for (int scanLevel = rangeStart; scanLevel < rangeEnd; scanLevel++) {
            final ArrayList<Integer> vxLevel = vxLevels.get(scanLevel);
            final ArrayList<Integer> vxLevelCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
            final ArrayList<Integer> vxLevelTempCopy = new ArrayList<>(vxLevel); // avoid ConcurrentModificationException
            final ArrayList<Integer> tempNeighbours = new ArrayList<>();
            final ArrayList<Integer> targetNeighbours = new ArrayList<>();
            final ArrayList<Integer> lockedVertex = new ArrayList<>();

            for (final int vxId : vxLevelCopy) {
                // for each node on the current level, calculate the distance to its parents and children for each position in the current level.
                // switch positions with the smallest location, then lock that position.
                double initialDistance = 0;
                tempNeighbours.clear();
                for (int lposition = 0; lposition < wg.getVertexNeighbourCount(vxId); lposition++) {
                    final int nId = wg.getVertexNeighbour(vxId, lposition);
                    if (!vxLevel.contains(nId)) {
                        tempNeighbours.add(nId);
                        initialDistance += calculateDistance(wg, vxId, nId);
                    }
                }

                int swapTargetId = -1;
                for (final int vxTempId : vxLevelTempCopy) {
                    if (lockedVertex.contains(vxTempId)) {
                        continue;
                    }

                    if (vxTempId == vxId) continue;
                    double tempInitialDistance = 0;                    
                    for (final int neighbourId : tempNeighbours) {
                        tempInitialDistance += calculateDistance(wg, vxTempId, neighbourId);
                    }

                    targetNeighbours.clear();
                    double tnDistance = 0;
                    for (int lposition = 0; lposition < wg.getVertexNeighbourCount(vxTempId); lposition++) {
                        final int nId = wg.getVertexNeighbour(vxTempId, lposition);
                        if (!vxLevel.contains(nId)) {
                            targetNeighbours.add(nId);
                            tnDistance += calculateDistance(wg, vxTempId, nId);
                        }
                    }
                    double tnSwapDistance = 0;
                    for (final int neighbourId : targetNeighbours) {
                        tnSwapDistance += calculateDistance(wg, vxId, neighbourId);
                    }

                    if (tempInitialDistance < initialDistance && tnSwapDistance <= tnDistance) {
                        swapTargetId = vxTempId;
                    }
                }

                if (swapTargetId > -1) {
                    swapsMade++;
                    swapVertexPositions(wg, swapTargetId, vxId);
                    lockedVertex.add(vxId);
                }
            }
        }
        
        return swapsMade;
    }
        
    private static double calculateDistance(final GraphWriteMethods wg, final int vxId1, final int vxId2) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        
        double x2Dist = Math.pow(wg.getFloatValue(xId, vxId1) - wg.getFloatValue(xId, vxId2), 2);
        double y2Dist = Math.pow(wg.getFloatValue(yId, vxId1) - wg.getFloatValue(yId, vxId2), 2);
        
        return Math.sqrt(x2Dist + y2Dist);
    }
    
    private static void swapVertexPositions(final GraphWriteMethods wg, final int vxId1, final int vxId2){
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());

        final float xVal1 = wg.getFloatValue(xId, vxId1);
        final float xVal2 = wg.getFloatValue(xId, vxId2);
        
        final float yVal1 = wg.getFloatValue(yId, vxId1);
        final float yVal2 = wg.getFloatValue(yId, vxId2);
        
        wg.setFloatValue(xId, vxId1, xVal2);
        wg.setFloatValue(yId, vxId1, yVal2);
        
        wg.setFloatValue(xId, vxId2, xVal1);
        wg.setFloatValue(yId, vxId2, yVal1);
    }
    
    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

}
