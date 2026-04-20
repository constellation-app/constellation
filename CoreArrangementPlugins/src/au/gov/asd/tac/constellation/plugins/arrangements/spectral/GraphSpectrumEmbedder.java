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
package au.gov.asd.tac.constellation.plugins.arrangements.spectral;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

/**
 *
 * @author twilight_sparkle
 */
public class GraphSpectrumEmbedder {

    public static MutableIntObjectMap<double[]> spectralEmbedding(final GraphReadMethods rg, final IntIterable includedVertices) {
        final MutableIntObjectMap<double[]> vertexPositions = new IntObjectHashMap<>();

        // Don't position anything if there are fewer than 3 vertices to embedd - this embedding shouldn't be used in these cases.
        if (includedVertices.size() <= 2) {
            return vertexPositions;
        }

        final GraphMatrix l = GraphMatrix.adjacencyFromGraph(rg, includedVertices, new IntHashSet());

        final EigenDecomposition e = new EigenDecomposition(MatrixUtils.createRealMatrix(l.laplacianMatrix));
        final int numVectors = e.getRealEigenvalues().length;

        for (int i = 0; i < l.dimension; i++) {
            double xPos = 0;
            double yPos = 0;
            double norm = 0;
            for (int j = 0; j < numVectors - 1; j++) {
                xPos += e.getRealEigenvalue(j) * e.getEigenvector(j).getEntry(i);
                yPos += e.getRealEigenvalue(j) * e.getEigenvector(j).getEntry(i) * (j % 2 == 0 ? -1 : 1);
                norm += Math.abs(e.getRealEigenvalue(j)) * (Math.pow(e.getEigenvector(j).getEntry(i), 2));
            }
            norm = Math.sqrt(norm);
            vertexPositions.put(l.matrixPositionToID.get(i), new double[]{norm * xPos, norm * yPos});
        }

        return vertexPositions;

    }

    private static class GraphMatrix {

        private final MutableIntIntMap matrixPositionToID;
        private final double[][] laplacianMatrix;
        private final int dimension;

        private GraphMatrix(final double[][] laplacianMatrix, final IntIntMap matrixPositionToID) {
            this.laplacianMatrix = laplacianMatrix;
            this.matrixPositionToID = new IntIntHashMap(matrixPositionToID);
            this.dimension = laplacianMatrix.length;
        }

        public static GraphMatrix adjacencyFromGraph(final GraphReadMethods rg, final IntIterable includedVertices, final IntIterable excludedLinks) {
            final int numVertices = includedVertices.size();
            final double[][] matrixEntries = new double[numVertices][];
            for (int i = 0; i < numVertices; i++) {
                matrixEntries[i] = new double[numVertices];
            }
            final MutableIntIntMap idToMatrixPosition = new IntIntHashMap();
            final MutableIntIntMap matrixPositionToID = new IntIntHashMap();

            int skips = 0;
            for (int i = 0; i < rg.getVertexCount(); i++) {
                final int vxID = rg.getVertex(i);
                if (!includedVertices.contains(vxID)) {
                    skips++;
                    continue;
                }
                idToMatrixPosition.put(vxID, i - skips);
                matrixPositionToID.put(i - skips, vxID);
            }

            for (int i = 0; i < numVertices; i++) {
                final int vxID = matrixPositionToID.get(i);
                for (int j = 0; j < rg.getVertexNeighbourCount(vxID); j++) {
                    final int neighbourID = rg.getVertexNeighbour(vxID, j);
                    if (excludedLinks.contains(rg.getLink(vxID, neighbourID))
                            || !includedVertices.contains(neighbourID)
                            || idToMatrixPosition.get(neighbourID) == i) {
                        continue;
                    }
                    matrixEntries[i][idToMatrixPosition.get(neighbourID)] = 1;
                }
                matrixEntries[i][i] = 0;
            }
            return new GraphMatrix(matrixEntries, matrixPositionToID);
        }
    }
}
