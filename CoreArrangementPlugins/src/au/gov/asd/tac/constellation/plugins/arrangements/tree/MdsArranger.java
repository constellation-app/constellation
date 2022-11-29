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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.security.SecureRandom;
import java.util.BitSet;
import org.openide.util.NotImplementedException;

/**
 * Perform MDS (multidimensional scaling) to two dimensions.
 * <p>
 * Supports using old coordinates as starting locations, partial vertex set
 * arrangement, and arbitrary vertices of influence. Only uses and moves
 * vertices.
 *
 * Supports updating the progress display.
 *
 * MDS uses an incremental arrangement method, beginning with a subset,
 * arranging that subset with multiple starts, picking the arrangement with
 * minimum stress, introducing more of the graph, and repeating. Each
 * minimization uses steepest descent of semiproportional stress.
 *
 * Target distances: distance between A and B is minimum number of edges between
 * A and B times scaleFactor. The value of scaleFactor is 100 * scaleSetting
 * (which may be set).
 *
 * @author algol
 * @author sol
 */
public class MdsArranger implements Arranger {

    // Vertex radii are measured in square sides, visible radii are measured in circle radii.
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final float CIRC_RADIUS = (float) Math.sqrt(2);
    private static final float RADIUS_INFLATION_AT_100_PERCENT = 1.5F;
    private static final float EXTENTS_SIZE_INFLATION = 1.2F;

    private final MDSChoiceParameters params;

    public MdsArranger(final MDSChoiceParameters params) {
        this.params = params;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        if (wg.getVertexCount() == 0) {
            return;
        }

        final BitSet verticesToArrange = ArrangementUtilities.vertexBits(wg);

        // Parameter setup.
        int maxTrialsPerStage = params.maxTrialsPerStage;
        int minTrialsPerStage = params.minTrialsPerStage;
        int iterationsPerStageTrial = params.iterationsPerStageTrial;
        boolean usingExtents = params.linkWeight == LinkWeight.USE_EXTENTS;

        boolean showIterations = false;
        int startSetSizeMax = 8;
        int smallGraphSize = 20;
        int iterationsPerStageSmallGraph = 25;
        int numTrialsForSmallGraph = 4;

        final boolean setMinByRadii = params.tryToAvoidOverlap;
        final float radiusInflation = RADIUS_INFLATION_AT_100_PERCENT * (params.overlapAvoidance / 100.0F);

        if (iterationsPerStageTrial < 0) {
            iterationsPerStageTrial = -iterationsPerStageTrial;
            showIterations = true;
        }

        // Set scale from extents of things being arranged.
        final float scaleFactor;
        final float perturbationSize;
        if (usingExtents) {
            scaleFactor = params.scale;
            perturbationSize = (scaleFactor * 3F * ArrangementUtilities.FUNDAMENTAL_SIZE) / 2F;
        } else {
            final float minSpacing;

            final int nradiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(wg);
            if (nradiusAttr == Graph.NOT_FOUND) {
                // Default radius is 1.
                minSpacing = Math.max(1, 3 * ArrangementUtilities.FUNDAMENTAL_SIZE);
            } else {
                float min = 0;
                for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
                    final float r = wg.getFloatValue(nradiusAttr, vxId);
                    if (r > min) {
                        min = r;
                    }
                }

                minSpacing = Math.max(min, 3 * ArrangementUtilities.FUNDAMENTAL_SIZE);
            }

            scaleFactor = 3F * minSpacing * params.scale;
            perturbationSize = scaleFactor / 2F;
        }

        // Make complete list of vertices and maps between them and one-up indices.
        // Also build 3 vectors to start things off
        //  vxsToInfluence: first numVxsToInfluence entries will act on vertices
        //  vxsToArrange:    first numVxsToArrange entries will be moved
        //  vxsToArrangeLater:    numVxsLater entries to be done after others done
        // and 2 more vectors for the whole thing
        //  totVxsToArrange: first numTotVxsToArrange entries will act on vertices
        //  totVxsToInfluence: first numTotVxsToInfluence entries will act on vertices
        final int[] vxsToArrange = new int[wg.getVertexCapacity()];
        final int[] vxsToInfluence = new int[wg.getVertexCapacity()];
        final int[] vxsToArrangeLater = new int[wg.getVertexCapacity()];

        final int[] totVxsToArrange = new int[wg.getVertexCapacity()];
        final int[] totVxsToInfluence = new int[wg.getVertexCapacity()];

        int numVxsToInfluence = 0;
        int numVxsToArrange = 0;
        int numVxsLater = 0;

        int numTotVxsToArrange = 0;
        int numTotVxsToInfluence = 0;

        final int vxCount = wg.getVertexCount();
        for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            final boolean arrangeIt = true; // verticesToArrange.contains( thisVertex )
            final boolean influence = true; //verticesThatInfluence.contains( thisVertex )
            final boolean useLocs = false; //verticesToUseExistingLoc.contains( thisVertex )
            if (arrangeIt && useLocs) {
                vxsToArrange[numVxsToArrange++] = vxId;
            } else if (arrangeIt && !useLocs) {
                vxsToArrangeLater[numVxsLater++] = vxId;
            } else {
                // Do nothing
            }
            if (influence && (useLocs || !arrangeIt)) {
                vxsToInfluence[numVxsToInfluence++] = vxId;
            }
            if (arrangeIt) {
                totVxsToArrange[numTotVxsToArrange++] = vxId;
            }
            if (influence) {
                totVxsToInfluence[numTotVxsToInfluence++] = vxId;
            }
        }

        // Get all distances in a matrix represented as a vector.
        // Index 0 is source vxId, index 1 is destination (influence) vxId.
        final float[][] distanceMatrix = calcDistanceMatrix(wg, verticesToArrange, params.linkWeight, scaleFactor);
        if (setMinByRadii) {
            setMinDistancesByRadii(wg, verticesToArrange, distanceMatrix, radiusInflation);
        }

        final float[] gains = new float[wg.getVertexCapacity()];
        final float[] currentX = new float[wg.getVertexCapacity()];
        final float[] currentY = new float[wg.getVertexCapacity()];
        final float[] radii = new float[wg.getVertexCapacity()];
        final float[] bestX = new float[wg.getVertexCapacity()];
        final float[] bestY = new float[wg.getVertexCapacity()];
        final float[] startX = new float[wg.getVertexCapacity()];
        final float[] startY = new float[wg.getVertexCapacity()];
        final int[] closestVertices = new int[wg.getVertexCapacity()];
        final int[] nextClosestVertices = new int[wg.getVertexCapacity()];
        final float[] gammas = new float[wg.getVertexCapacity()];

        if (wg.getAttribute(GraphElementType.VERTEX, "x") == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, "y") == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, "z") == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", null, null);
        }
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        // Remember the old locations.
        for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
            currentX[vxId] = wg.getFloatValue(xAttr, vxId);
            currentY[vxId] = wg.getFloatValue(yAttr, vxId);
        }

        // Find first subset size and skip factor for filling in remainder.
        final int[] vxsToDo = new int[numVxsLater];
        final int initialIncrement = fillOrderToIntroduceVerticesVector(vxsToDo, vxsToArrangeLater, numVxsLater, startSetSizeMax);

        int incrementSize = initialIncrement;
        int remainingVertices = numVxsLater;
        int startOfRemainingVertices = 0;
        boolean done = false;

        // Stage loop.
        while (!done) {

            // Save location so far.
            final int numberOfLocationsSaved = numVxsToArrange;

            for (int i = 0; i < numberOfLocationsSaved; i++) {
                startX[i] = currentX[i];
                startY[i] = currentY[i];
            }

            // Number of vertices to add.
            final int numToAdd = Math.min(incrementSize, remainingVertices);

            // Put them in arrange and influence arrays.
            for (int i = 0; i < numToAdd; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int newVxId = vxsToDo[i + startOfRemainingVertices];
                vxsToArrange[numVxsToArrange++] = newVxId;

                if (true) { // verticesThatInfluence.contains(atomArray[newIndex])
                    vxsToInfluence[numVxsToInfluence++] = newVxId;
                }

                // Find info about closest vertices that are already there
                // (fill in an entry in each of indicesOfClosestVertices,
                // indicesOfNextClosestVertices, and gammas).
                initialPositioningInfoThisVertex(wg, verticesToArrange, newVxId, distanceMatrix, closestVertices, nextClosestVertices, gammas);

                remainingVertices--;
            }

            startOfRemainingVertices += numToAdd;

            // Set up gains for this subset.
            fillGainVector(distanceMatrix, gains, vxsToArrange, numVxsToArrange, vxsToInfluence, numVxsToInfluence);

            float bestStress = Float.MAX_VALUE;

            // Trial loop.
            int trialsPerStage = vxCount / numVxsToArrange;

            if (trialsPerStage > maxTrialsPerStage) {
                trialsPerStage = maxTrialsPerStage;
            }
            if (trialsPerStage < minTrialsPerStage) {
                trialsPerStage = minTrialsPerStage;
            }
            if ((numVxsToArrange <= smallGraphSize) && (trialsPerStage < numTrialsForSmallGraph)) {
                trialsPerStage = numTrialsForSmallGraph;
            }

            for (int trial = 0; trial < trialsPerStage; trial++) {
                // Restore starting locations.
                if (trial > 0) {
                    for (int i = 0; i < numberOfLocationsSaved; i++) {
                        currentX[i] = startX[i];
                        currentY[i] = startY[i];
                    }
                }

                // Set coordinates of new vertices.
                positionVertices(numVxsToArrange - numToAdd, numVxsToArrange - 1, vxsToArrange, currentX, currentY, closestVertices, nextClosestVertices, gammas, scaleFactor, perturbationSize);

                // Do the minimisation.
                int numIterations = iterationsPerStageTrial;

                if (numVxsToArrange <= smallGraphSize) {
                    numIterations = iterationsPerStageSmallGraph;
                }

                if (showIterations) {
//                    for ( int i = 0; i < numIterations; i++ )
//                    {
//                        DirectMin(
//                            currentX,
//                            currentY,
//                            distanceMatrix,
//                            vxCount,
//                            gains,
//                            radii,
//                            vxsToArrange,
//                            numIndicesToArrange,
//                            vxsToInfluence,
//                            numIndicesToInfluence,
//                            1 );
//                        ApplyNewLocations(
//                            atomArray,
//                            indicesToArrange,
//                            numIndicesToArrange,
//                            currentX,
//                            currentY,
//                            TheLayout );
//                        GenericNexus nexus = arrangementDescription.localNexus;
//                        if ( nexus != null )
//                        {
//                            ViewCanvas mainCanvas =
//                                ( (MagnifiedViewer)( nexus.getGUIFrame(  ) ) ).getMainCanvas(  );
//                            mainCanvas.paintSynchronous( arrangementDescription.streamID );
//                        }
//                    }
                } else {
                    directMin(
                            currentX,
                            currentY,
                            distanceMatrix,
                            gains,
                            radii,
                            vxsToArrange,
                            numVxsToArrange,
                            vxsToInfluence,
                            numVxsToInfluence,
                            numIterations,
                            setMinByRadii,
                            radiusInflation);
                }

                // Measure stress for this trial.
                final float stress
                        = measureStress(
                                currentX,
                                currentY,
                                distanceMatrix,
                                vxsToArrange,
                                numVxsToArrange,
                                vxsToInfluence,
                                numVxsToInfluence);

                // If this is the best stress, keep it.
                if (stress < bestStress) {
                    for (int i = 0; i < numVxsToArrange; i++) {
                        final int vxId = vxsToArrange[i];

                        bestX[vxId] = currentX[vxId];
                        bestY[vxId] = currentY[vxId];
                    }

                    bestStress = stress;
                }
            }

            // Keep the best configuration.
            for (int i = 0; i < numVxsToArrange; i++) {
                final int vxId = vxsToArrange[i];

                currentX[vxId] = bestX[vxId];
                currentY[vxId] = bestY[vxId];
            }

            if (remainingVertices == 0) {
                done = true;
            } else {
                incrementSize = 2 * incrementSize;
            }
        }

        // Apply the new coordinates.
        for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
            wg.setFloatValue(xAttr, vxId, currentX[vxId]);
            wg.setFloatValue(yAttr, vxId, currentY[vxId]);
            wg.setFloatValue(zAttr, vxId, 0);
        }
    }

    private static float[][] calcDistanceMatrix(final GraphWriteMethods graph, final BitSet verticesToArrange, final LinkWeight linkWeight, final float scaleFactor) {
        if (linkWeight == LinkWeight.USE_EXTENTS) {
            return calcDistanceMatrixByExtent(graph, verticesToArrange, scaleFactor);
        } else {
            throw new NotImplementedException(String.format("Link weight %s not implemented", linkWeight));
        }
    }

    /**
     * This version calculates distances based on graph pathlength weighting
     * edges by the sum of the extents of the vertices they join.
     * <p>
     * Usual edge weights, multiple edges, etc, are not considered.
     */
    private static float[][] calcDistanceMatrixByExtent(final GraphWriteMethods graph, final BitSet verticesToArrange, final float scaleFactor) {
        final float minRadius = 1.5F * ArrangementUtilities.FUNDAMENTAL_SIZE;

        // Record distances here.
        final float[][] distanceMatrix = new float[graph.getVertexCapacity()][graph.getVertexCapacity()];

        // Loop through each arrange vertex, recording distances to each influence vertex.
        for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
            final float[] distancesFromVertex = ArrangementUtilities.getMinDistancesToReachableVertices(graph, vxId, true, true, minRadius);

            // Loop through each influence vertex.
            for (int inflVxId = verticesToArrange.nextSetBit(0); inflVxId >= 0; inflVxId = verticesToArrange.nextSetBit(inflVxId + 1)) {
                distanceMatrix[vxId][inflVxId] = scaleFactor * distancesFromVertex[inflVxId] * EXTENTS_SIZE_INFLATION;
            }
        }

        return distanceMatrix;
    }

    /**
     * Make a list of vertices in the order in which they will be introduced.
     * The list is made by taking every kth element, then every k/2 elements
     * (skipping those already taken), then k/4, etc.
     */
    private static int fillOrderToIntroduceVerticesVector(
            final int[] vxsToDo,
            final int[] vxsToArrangeLater,
            final int numVxsLater,
            final int startSetSizeMax) {
        int skipFactor = 1;
        int numThisSubset = numVxsLater;

        while (numThisSubset > startSetSizeMax) {
            numThisSubset /= 2;
            skipFactor *= 2;
        }

        int numDone = 0;
        boolean firstTime = true;
        int currentIndex = 0;
        boolean skipThisOne = true;

        while (numDone < numVxsLater) {
            if (firstTime || !skipThisOne) {
                vxsToDo[numDone++] = vxsToArrangeLater[currentIndex];
            }

            skipThisOne = !skipThisOne;

            currentIndex += skipFactor;

            if (currentIndex >= numVxsLater) {
                skipFactor /= 2;
                skipThisOne = true;
                currentIndex = 0;
                firstTime = false;
            }
        }

        return numThisSubset;
    }

    /**
     * Alter distanceMatrix so that the distances don't encourage vertices of
     * large extent to overlap.
     * <p>
     * Go through all pairs of an influence vertex with an arrange vertex. For
     * each pair, set their target distance equal to the max of the incoming
     * value with the sum of their radii.
     *
     * @param graph
     * @param distanceMatrix
     */
    private static void setMinDistancesByRadii(final GraphWriteMethods graph, final BitSet verticesToArrange, final float[][] distanceMatrix, final float radiusInflation) {
        final int radiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(graph);

        for (int vxId = verticesToArrange.nextSetBit(0); vxId >= 0; vxId = verticesToArrange.nextSetBit(vxId + 1)) {
            final float radius = CIRC_RADIUS * (radiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(radiusAttr, vxId) : 1);

            for (int inflVxId = verticesToArrange.nextSetBit(0); inflVxId >= 0; inflVxId = verticesToArrange.nextSetBit(inflVxId + 1)) {
                final float inflRadius = CIRC_RADIUS * (radiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(radiusAttr, inflVxId) : 1);
                final float distance = distanceMatrix[vxId][inflVxId];
                final float minDistance = radiusInflation * (radius + inflRadius);
                distanceMatrix[vxId][inflVxId] = Math.max(distance, minDistance);
            }
        }
    }

    /**
     * Fill in information to initially position this vertex when it is
     * introduced.
     * <p>
     * In particular, fill in an entry in each of closestVertices,
     * nextClosestVertices, and gammas. Note that numVxsToInfluence is changing
     * each time it is called.
     */
    private static void initialPositioningInfoThisVertex(
            final GraphWriteMethods graph,
            final BitSet verticesToArrange,
            final int vxId,
            final float[][] distanceMatrix,
            final int[] closestVertices,
            final int[] nextClosestVertices,
            final float[] gammas) {
        // Find the vertex closest to vxId.
        int closestVxId = Graph.NOT_FOUND;
        int nextClosestVxId = Graph.NOT_FOUND;
        float distanceToClosest = Float.MAX_VALUE;
        float distanceToNextClosest = Float.MAX_VALUE;
        for (int inflVxId = verticesToArrange.nextSetBit(0); inflVxId >= 0; inflVxId = verticesToArrange.nextSetBit(inflVxId + 1)) {
            if (inflVxId != vxId) {
                final float thisDist = distanceMatrix[vxId][inflVxId];
                if (thisDist < distanceToClosest) {
                    nextClosestVxId = closestVxId;
                    distanceToNextClosest = distanceToClosest;

                    closestVxId = inflVxId;
                    distanceToClosest = thisDist;
                }
            }
        }

        closestVertices[vxId] = closestVxId;
        nextClosestVertices[vxId] = nextClosestVxId;

        // Gamma calculation.
        if (closestVxId != Graph.NOT_FOUND && nextClosestVxId != Graph.NOT_FOUND) {
            float tDiff = distanceMatrix[closestVxId][nextClosestVxId];
            if (tDiff > 0) {
                final float tik = distanceToNextClosest * distanceToNextClosest;
                final float tij = distanceToClosest * distanceToClosest;
                final float tjk = tDiff * tDiff;

                float gamma = (tik - (tij + tjk)) / (2 * tjk);
                if (gamma > 0.5F) {
                    gamma = 0.5F;
                }

                gammas[vxId] = gamma;
            } else {
                gammas[vxId] = -0.5F;
            }
        }
    }

    private static void fillGainVector(
            final float[][] distanceMatrix,
            final float[] gains,
            final int[] vxsToArrange,
            final int numVxsToArrange,
            final int[] vxsToInfluence,
            final int numVxsToInfluence) throws InterruptedException {
        for (int i = 0; i < numVxsToArrange; i++) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            final int vxId = vxsToArrange[i];
            float sum = 0;

            for (int j = 0; j < numVxsToInfluence; j++) {
                final int inflVxId = vxsToInfluence[j];

                if (vxId != inflVxId) {
                    final double targetDist = distanceMatrix[vxId][inflVxId];
                    if (targetDist > 0) {
                        sum += (1.0 / targetDist);
                    }
                }
            }

            if (sum > 0) {
                gains[vxId] = 1.0F / (2.0F * sum);
            }
        }
    }

    /**
     * Give a range of vertices their initial locations, based on the their two
     * nearest neighbors.
     * <p>
     * Fills in the appropriate entries in currentX and currentY, using the
     * corresponding entries of closestVertices, nextClosestVertices, and
     * gammas.
     */
    private static void positionVertices(
            final int firstOne,
            final int lastOne,
            final int[] verticesToPosition,
            final float[] currentX,
            final float[] currentY,
            final int[] closestVertices,
            final int[] nextClosestVertices,
            final float[] gammas,
            final float scaleFactor,
            final float perturbationSize) throws InterruptedException {
        for (int i = firstOne; i <= lastOne; i++) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            final int vxNew = verticesToPosition[i];
            final int vxClosest = closestVertices[vxNew];
            final int vxNextClosest = nextClosestVertices[vxNew];

            if ((vxClosest >= 0) && (vxNextClosest >= 0)) {
                final float cX = currentX[vxClosest];
                final float ncX = currentX[vxNextClosest];
                currentX[vxNew] = cX + (gammas[vxNew] * (cX - ncX)) + (perturbationSize * (RANDOM.nextFloat() - 0.5F));

                final float cY = currentY[vxClosest];
                final float ncY = currentY[vxNextClosest];
                currentY[vxNew] = cY + (gammas[vxNew] * (cY - ncY)) + (perturbationSize * (RANDOM.nextFloat() - 0.5F));
            } else if (vxClosest >= 0) {
                currentX[vxNew] = currentX[vxClosest] + (perturbationSize * (RANDOM.nextFloat() - 0.5F));
                currentY[vxNew] = currentY[vxClosest] + (perturbationSize * (RANDOM.nextFloat() - 0.5F));
            } else {
                currentX[vxNew] = scaleFactor * (RANDOM.nextFloat() - 0.5F);
                currentY[vxNew] = scaleFactor * (RANDOM.nextFloat() - 0.5F);
            }
        }
    }

    /**
     * Updates the locations in currentX and currentY with numIterations
     * iterations of MDS.
     * <p>
     * Target distances are specified in the ravelled array which is
     * numMatrixCols by numMatrixCols. The indices in the position vectors and
     * distance matrix are specified by the entries in arrays vxsToArrange and
     * vxsToInfluence, which may given overlapping lists of vertices.
     * <p>
     * Minimization of semiproportional stress is done.
     */
    private static void directMin(
            final float[] currentX,
            final float[] currentY,
            final float[][] distanceMatrix,
            final float[] gains,
            final float[] radii,
            final int[] vxsToArrange,
            final int numVxsToArrange,
            final int[] vxsToInfluence,
            final int numVxsToInfluence,
            final int numIterations,
            final boolean setMinByRadii,
            final float radiusInflation) throws InterruptedException {
        float[] newX = new float[currentX.length];
        float[] newY = new float[currentY.length];

        for (int iteration = 0; iteration < numIterations; iteration++) {
            // Begin loop for vertex to move.
            for (int i = 0; i < numVxsToArrange; i++) {
                float xInc = 0;
                float yInc = 0;
                final int vxId = vxsToArrange[i];

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // Begin vertex influence loop.
                for (int j = 0; j < numVxsToInfluence; j++) {
                    int inflvxId = vxsToInfluence[j];

                    if (vxId != inflvxId) {
                        final float xDelta = currentX[vxId] - currentX[inflvxId];
                        final float yDelta = currentY[vxId] - currentY[inflvxId];
                        final float currentDistSquared = (xDelta * xDelta) + (yDelta * yDelta);
                        final float targetDist = distanceMatrix[vxId][inflvxId];
                        if (targetDist == 0) {
                            // should not happen!
                        } else if (currentDistSquared != 0) {
                            final float currentDist = (float) Math.sqrt(currentDistSquared);
                            float mult = (1.0F / targetDist) - (1.0F / currentDist);

                            if (setMinByRadii && (mult < 0.0)) {
                                final float minDist = radiusInflation * (radii[vxId] + radii[inflvxId]);

                                if (currentDist < minDist) {
                                    float inflation = minDist / currentDist;
                                    inflation *= inflation;

                                    if (inflation > 10) {
                                        inflation = 10;
                                    }

                                    if (inflation > (numVxsToInfluence - 1)) {
                                        inflation = numVxsToInfluence - 1F;
                                    }

                                    mult *= inflation;
                                }
                            }

                            xInc += (xDelta * mult);
                            yInc += (yDelta * mult);
                        } else {
                            xInc += (RANDOM.nextDouble() - 0.5);
                            yInc += (RANDOM.nextDouble() - 0.5);
                        }
                    }
                }

                newX[vxId] = currentX[vxId] - (2.0F * gains[vxId] * xInc);
                newY[vxId] = currentY[vxId] - (2.0F * gains[vxId] * yInc);
            }

            // Update current values from new ones.
            for (int i = 0; i < numVxsToArrange; i++) {
                currentX[i] = newX[i];
                currentY[i] = newY[i];
            }
        }
    }

    /**
     * Measures proportional stress, based on current and target positions, only
     * using specified vertices.
     */
    private static float measureStress(
            final float[] currentX,
            final float[] currentY,
            final float[][] distanceMatrix,
            final int[] vxsToArrange,
            final int numVxsToArrange,
            final int[] vxsToInfluence,
            final int numVxsToInfluence) throws InterruptedException {
        float stress = 0;
        int numContributions = 0;

        for (int i = 0; i < numVxsToArrange; i++) {
            final int vxId = vxsToArrange[i];

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            for (int j = 0; j < numVxsToInfluence; j++) {
                final int inflVxId = vxsToInfluence[j];

                if (vxId != inflVxId) {
                    final float xDelta = currentX[vxId] - currentX[inflVxId];
                    final float yDelta = currentY[vxId] - currentY[inflVxId];

                    final float currentDist = (float) Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
                    final float targetDist = distanceMatrix[vxId][inflVxId];

                    if (targetDist > 0) {
                        stress += (((currentDist - targetDist) * (currentDist - targetDist)) / (targetDist * targetDist));
                    }
                    numContributions++;
                }
            }
        }

        if (numContributions == 0) {
            return 0;
        }

        return stress / numContributions;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Required for Arranger, intentionally left blank
    }
}
