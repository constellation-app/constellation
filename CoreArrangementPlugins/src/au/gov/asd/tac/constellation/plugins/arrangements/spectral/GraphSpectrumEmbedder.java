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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;

/**
 *
 * @author twilight_sparkle
 */
public class GraphSpectrumEmbedder {

    public static Map<Integer, double[]> spectralEmbedding(final GraphReadMethods rg, final Set<Integer> includedVertices) {

        final Map<Integer, double[]> vertexPositions = new HashMap<>();

        // Don't position anything if there are fewer than 3 vertices to embedd - this embedding shouldn't be used in these cases.
        if (includedVertices.size() <= 2) {
            return vertexPositions;
        }

        final GraphMatrix l = GraphMatrix.adjacencyFromGraph(rg, includedVertices, new HashSet<>());

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

    private enum MatrixType {

        ADJACENCY_MATRIX,
        LAPLACIAN_MATRIX,;
    }

    private static class GraphMatrix {

        private final Map<Integer, Integer> matrixPositionToID;
        private final double[][] laplacianMatrix;
        private final int dimension;

        private GraphMatrix(final double[][] laplacianMatrix, final Map<Integer, Integer> matrixPositionToID) {
            this.laplacianMatrix = laplacianMatrix;
            this.matrixPositionToID = matrixPositionToID;
            this.dimension = laplacianMatrix.length;
        }

        public static GraphMatrix adjacencyFromGraph(final GraphReadMethods rg, final Set<Integer> includedVertices, final Set<Integer> excludedLinks) {
            return matrixFromGraph(rg, includedVertices, excludedLinks, MatrixType.ADJACENCY_MATRIX);
        }

        public static GraphMatrix laplacianFromGraph(final GraphReadMethods rg, final Set<Integer> includedVertices, final Set<Integer> excludedLinks) {
            return matrixFromGraph(rg, includedVertices, excludedLinks, MatrixType.LAPLACIAN_MATRIX);
        }

        public static GraphMatrix matrixFromGraph(final GraphReadMethods rg, final Set<Integer> includedVertices, final Set<Integer> excludedLinks, final MatrixType type) {

            final int numVertices = includedVertices.size();
            final double[][] matrixEntries = new double[numVertices][];
            for (int i = 0; i < numVertices; i++) {
                matrixEntries[i] = new double[numVertices];
            }
            final Map<Integer, Integer> idToMatrixPosition = new HashMap<>();
            final Map<Integer, Integer> matrixPositionToID = new HashMap<>();

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
                int neighbourCount = 0;
                for (int j = 0; j < rg.getVertexNeighbourCount(vxID); j++) {
                    final int neighbourID = rg.getVertexNeighbour(vxID, j);
                    if (excludedLinks.contains(rg.getLink(vxID, neighbourID))) {
                        continue;
                    } else if (!includedVertices.contains(neighbourID)) {
                        neighbourCount++;
                        continue;
                    } else if (idToMatrixPosition.get(neighbourID) == i) {
                        continue;
                    } else {
                        // Do nothing
                    }
                    neighbourCount++;
                    if (type != null) {
                        switch (type) {
                            case LAPLACIAN_MATRIX:
                                matrixEntries[i][idToMatrixPosition.get(neighbourID)] = -1;
                                break;
                            case ADJACENCY_MATRIX:
                                matrixEntries[i][idToMatrixPosition.get(neighbourID)] = 1;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (type != null) {
                    switch (type) {
                        case LAPLACIAN_MATRIX:
                            matrixEntries[i][i] = neighbourCount;
                            break;
                        case ADJACENCY_MATRIX:
                            matrixEntries[i][i] = 0;
                            break;
                        default:
                            break;
                    }
                }
            }
            return new GraphMatrix(matrixEntries, matrixPositionToID);
        }
    }
}
