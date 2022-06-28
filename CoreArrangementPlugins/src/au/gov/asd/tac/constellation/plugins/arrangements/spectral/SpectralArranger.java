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
package au.gov.asd.tac.constellation.plugins.arrangements.spectral;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTruss;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTruss.KTrussResultHandler;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
public class SpectralArranger implements Arranger {

    private boolean maintainMean = false;

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

    // A method to spread vertices out after an arrangement to reduce overlaps whilst preserving the structure of the graph
    // and avoiding over-exapnsion.
    // This method should probably be refactored so that it can be used by the whole arrangement framework.
    public void disperseVertices(final GraphWriteMethods wg/*, final boolean rootDispersion*/) {

        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);

        final List<Double> xValues = new ArrayList<>();
        final List<Double> yValues = new ArrayList<>();

        for (int i = 0; i < wg.getVertexCount(); i++) {
            final int vxID = wg.getVertex(i);
            xValues.add(wg.getDoubleValue(xAttr, vxID));
            yValues.add(wg.getDoubleValue(yAttr, vxID));
        }
        xValues.sort(null);
        yValues.sort(null);
        double averageOverlap = 0;
        final Iterator<Double> xValIter = xValues.iterator();
        final Iterator<Double> yValIter = yValues.iterator();
        double currentX;
        double nextX = xValIter.next();
        double currentY;
        double nextY = yValIter.next();
        while (xValIter.hasNext()) {
            currentX = nextX;
            currentY = nextY;
            nextX = xValIter.next();
            nextY = yValIter.next();
            final double distanceX = nextX - currentX;
            final double distanceY = nextY - currentY;
            averageOverlap += (distanceX < 2) ? (2 - distanceX) : 0;
            averageOverlap += (distanceY < 2) ? (2 - distanceY) : 0;
        }

        averageOverlap /= (wg.getVertexCount() * 2);

        final double dispersionFactor = Math.sqrt(2 / (2 - averageOverlap));

        for (int i = 0; i < wg.getVertexCount(); i++) {
            final int vxID = wg.getVertex(i);
            wg.setDoubleValue(xAttr, vxID, wg.getDoubleValue(xAttr, vxID) * dispersionFactor);
            wg.setDoubleValue(yAttr, vxID, wg.getDoubleValue(yAttr, vxID) * dispersionFactor);
            wg.setDoubleValue(zAttr, vxID, wg.getDoubleValue(zAttr, vxID) * dispersionFactor);
        }

    }

    private static class GetTrussResultHandler implements KTrussResultHandler {

        private final Set<Integer> verticesInHighestTruss = new HashSet<>();
        private final Set<Integer> otherVertices = new HashSet<>();
        private int highestK;

        @Override
        public void initialise(final BitSet currentLinksCopy) {
            // Required for KTrussResultHandler, intentionally left blank
        }

        @Override
        public void recordTransactionCluster(final int txID, final int clusterNum) {
            // Required for KTrussResultHandler, intentionally left blank
        }

        @Override
        public void recordVertexCluster(final int vxID, final int clusterNum) {
            verticesInHighestTruss.add(vxID);
        }

        @Override
        public boolean nextK(final int lastK, final boolean clustersModified, final BitSet currentLinksCopy) {
            otherVertices.addAll(verticesInHighestTruss);
            verticesInHighestTruss.clear();
            return true;
        }

        @Override
        public void finalise(final int highestK, final BitSet currentLinksCopy) {
            this.highestK = highestK;
        }

    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {

        // Do nothing if the graph has no nodes.
        final int vxCount = wg.getVertexCount();
        if (vxCount == 0) {
            return;
        }
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        // Compute the trusses in the graph.
        final GetTrussResultHandler handler = new GetTrussResultHandler();
        KTruss.run(wg, handler);

        // If there are no trusses, fall back to a grid arrangement.
        if (handler.highestK < 3) {
            GridArranger gridArranger = new GridArranger();
            gridArranger.arrange(wg);
            return;
        }

        // Otherwise calculate the spectral (eigenvector) embedding of the most interconnected truss
        final Map<Integer, double[]> vertexToCoordinates = GraphSpectrumEmbedder.spectralEmbedding(wg, handler.verticesInHighestTruss);
        if (vertexToCoordinates.isEmpty()) {
            return;
        }

        // Get the z, y and z attributes of the graph
        final int xAttr = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(wg);

        // Position vertices in the most interconnected truss by their spectra.
        for (final Map.Entry<Integer, double[]> entry : vertexToCoordinates.entrySet()) {
            wg.setDoubleValue(xAttr, entry.getKey(), entry.getValue()[0]);
            wg.setDoubleValue(yAttr, entry.getKey(), entry.getValue()[1]);
            wg.setDoubleValue(zAttr, entry.getKey(), 0);
        }

        // Position all the other vertices at a level (z-position) corresponding to their distance from the most interconnected truss, and at the centre (x,y-position) of their neighbours on the level above them.
        int level = 0;
        while (!handler.otherVertices.isEmpty()) {
            level++;
            final Set<Integer> verticesPlacedThisLevel = new HashSet<>();
            final Map<Set<Integer>, List<Integer>> significantNeighbourSets = new HashMap<>();
            for (final int vxID : handler.otherVertices) {
                double xPos = 0;
                double yPos = 0;
                int significantNeighbourCount = 0;
                final Set<Integer> significantNeighbourSet = new HashSet<>();
                for (int j = 0; j < wg.getVertexNeighbourCount(vxID); j++) {
                    final int neighbourID = wg.getVertexNeighbour(vxID, j);
                    if (handler.otherVertices.contains(neighbourID)) {
                        continue;
                    }
                    significantNeighbourSet.add(neighbourID);
                    significantNeighbourCount++;
                    xPos += wg.getDoubleValue(xAttr, neighbourID);
                    yPos += wg.getDoubleValue(yAttr, neighbourID);
                }
                if (significantNeighbourCount == 0) {
                    continue;
                }
                verticesPlacedThisLevel.add(vxID);
                if (!significantNeighbourSets.containsKey(significantNeighbourSet)) {
                    final List<Integer> newList = new ArrayList<>();
                    newList.add(vxID);
                    significantNeighbourSets.put(significantNeighbourSet, newList);
                } else {
                    significantNeighbourSets.get(significantNeighbourSet).add(vxID);
                }
                wg.setDoubleValue(xAttr, vxID, xPos / significantNeighbourCount);
                wg.setDoubleValue(yAttr, vxID, yPos / significantNeighbourCount);
                wg.setDoubleValue(zAttr, vxID, -15 * level);
            }

            handler.otherVertices.removeAll(verticesPlacedThisLevel);

            // Spread out vertices that clash a litle bit
            for (final Entry<Set<Integer>, List<Integer>> set : significantNeighbourSets.entrySet()) {
                final List<Integer> colocatedNodes = set.getValue();
                final int colocatedSize = colocatedNodes.size();
                if (colocatedSize > 1) {
                    final int firstNodeID = colocatedNodes.get(0);
                    final double xCentre = wg.getDoubleValue(xAttr, firstNodeID);
                    final double yCentre = wg.getDoubleValue(yAttr, firstNodeID);
                    double currentAngle = 0;
                    for (int vxID : set.getValue()) {
                        wg.setDoubleValue(xAttr, vxID, xCentre + (Math.cos(currentAngle) / 2));
                        wg.setDoubleValue(yAttr, vxID, yCentre + (Math.sin(currentAngle) / 2));
                        currentAngle += (2 * Math.PI) / colocatedSize;
                    }
                }
            }
        }

        // Disperse the vertices a little
        disperseVertices(wg);

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }
}
