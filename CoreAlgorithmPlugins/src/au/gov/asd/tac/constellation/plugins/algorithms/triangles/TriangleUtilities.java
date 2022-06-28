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
package au.gov.asd.tac.constellation.plugins.algorithms.triangles;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;

/**
 *
 * @author canis_majoris
 */
public class TriangleUtilities {

    private TriangleUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /*
     This method counts the number of triangles each vertex is in
     Returning a tuple where the first entry is a list of neighbours each node
     is in a triangle with, and the second entry is count of the number of
     triangles that node is in, and also the total number of triangles
     */
    public static Tuple<Tuple<BitSet[], float[]>, Float> getTriangles(final GraphReadMethods graph) {
        final int vxCount = graph.getVertexCount();
        final BitSet[] allNeighbours = new BitSet[vxCount];
        final float[] scores = new float[vxCount];
        final BitSet update = new BitSet(vxCount);
        final BitSet[] triangleNeighbours = new BitSet[vxCount];
        float triangles = 0;

        // initialise variables
        for (int vxPosition = 0; vxPosition < vxCount; vxPosition++) {

            allNeighbours[vxPosition] = new BitSet(vxCount);
            triangleNeighbours[vxPosition] = new BitSet(vxCount);
            scores[vxPosition] = 0;

            // get the vertex ID at this position
            final int vxId = graph.getVertex(vxPosition);

            // collect neighbours
            final BitSet neighbours = new BitSet(vxCount);
            for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                final int nxId = graph.getVertexNeighbour(vxId, neighbourPosition);
                final int nxPosition = graph.getVertexPosition(nxId);
                neighbours.set(nxPosition, true);
            }

            //not interested in neighbours to themselves
            neighbours.set(vxPosition, false);

            // if at least two neighbours, store them for triangle checking
            if (neighbours.cardinality() > 1) {
                allNeighbours[vxPosition].or(neighbours);
                update.set(vxPosition, true);
            }
        }

        // checking for triangles
        for (int one = update.nextSetBit(0); one >= 0; one = update.nextSetBit(one + 1)) {
            for (int two = update.nextSetBit(one); two >= one; two = update.nextSetBit(two + 1)) {
                // are these two vertices connected?
                if (allNeighbours[one].get(two) && allNeighbours[two].get(one)) {
                    // determine common neighbours between them, each one is a triangle
                    final BitSet intersection = new BitSet(vxCount);
                    intersection.or(allNeighbours[one]);
                    intersection.and(allNeighbours[two]);
                    for (int three = intersection.nextSetBit(two); three >= two; three = intersection.nextSetBit(three + 1)) {
                        scores[one] += 1;
                        scores[two] += 1;
                        scores[three] += 1;
                        triangleNeighbours[one].set(two, true);
                        triangleNeighbours[one].set(three, true);
                        triangleNeighbours[two].set(one, true);
                        triangleNeighbours[two].set(three, true);
                        triangleNeighbours[three].set(two, true);
                        triangleNeighbours[three].set(one, true);
                        triangles += 1;
                    }
                }
            }
        }

        return new Tuple<>(new Tuple<>(triangleNeighbours, scores), triangles);
    }

    /*
     * This method counts the number of triangles
     * and the total number of triplets on the graph.
     */
    public static Tuple<Float, Float> countTrianglesTriplets(final GraphReadMethods graph) {
        final int vxCount = graph.getVertexCount();
        final BitSet[] allNeighbours = new BitSet[vxCount];
        final BitSet update = new BitSet(vxCount);
        Float triangles = 0F;
        Float triplets = 0F;

        // initialise variables
        for (int vxPosition = 0; vxPosition < vxCount; vxPosition++) {

            allNeighbours[vxPosition] = new BitSet(vxCount);

            // get the vertex ID at this position
            final int vxId = graph.getVertex(vxPosition);

            // collect neighbours
            final BitSet neighbours = new BitSet(vxCount);
            for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vxId); neighbourPosition++) {
                final int nxId = graph.getVertexNeighbour(vxId, neighbourPosition);
                final int nxPosition = graph.getVertexPosition(nxId);
                neighbours.set(nxPosition, true);
            }

            //not interested in neighbours to themselves
            neighbours.set(vxPosition, false);

            allNeighbours[vxPosition].or(neighbours);
            update.set(vxPosition, true);
        }

        // checking for triangles
        for (int one = update.nextSetBit(0); one >= 0; one = update.nextSetBit(one + 1)) {
            for (int two = update.nextSetBit(one); two >= one; two = update.nextSetBit(two + 1)) {
                // are these two vertices connected?
                if (allNeighbours[one].get(two) && allNeighbours[two].get(one)) {
                    // determine common neighbours between them, each one is a triangle
                    final BitSet intersection = new BitSet(vxCount);
                    intersection.or(allNeighbours[one]);
                    intersection.and(allNeighbours[two]);
                    for (int three = intersection.nextSetBit(two); three >= two; three = intersection.nextSetBit(three + 1)) {
                        triangles += 1;
                    }
                    final BitSet union = new BitSet(vxCount);
                    union.or(allNeighbours[one]);
                    union.or(allNeighbours[two]);
                    union.set(one, false);
                    union.set(two, false);
                    for (int three = union.nextSetBit(two); three >= two; three = union.nextSetBit(three + 1)) {
                        triplets += 1;
                    }
                }
            }
        }

        return new Tuple<>(triangles, triplets);
    }
}
