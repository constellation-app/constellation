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
package au.gov.asd.tac.constellation.plugins.arrangements.hde;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Arrange by high dimension embedding.
 * <p>
 * D. Harel and Y. Koren, "Graph Drawing by High-Dimensional<br>
 * Embedding", proceedings of Graph Drawing 2002, Volume<br>
 * 2528 of Lecture Notes in Computer Science, pp. 207-219,<br>
 * Springer Verlag, 2002.
 * <p>
 * TODO: scale x,y,z after arrangement so the graph isn't cramped.
 *
 * @author algol
 */
public class HighDimensionEmbeddingArranger implements Arranger {

    /**
     * Dimensionality of embedding space.
     */
    public static final int M = 50;

    // Scale graph up so it isn't too cramped.
    private static final float SCALE = 10;

    private final int dimensions;

    // These should all be final, but can't be set until arrange().
    private GraphWriteMethods wg;
    private int[] centres;
    private double[] mean;
    private boolean[] known;
    private boolean[] pivot;
    private int[] distance;

    // Coordinates of each node relative to pivot.
    private double[][] xMatrix;

    private static final boolean PART_ONLY = false;

    private final SecureRandom random = new SecureRandom();

    public HighDimensionEmbeddingArranger(final int dimensions) {
        this.dimensions = dimensions;
    }

    private void set(final GraphWriteMethods wg) {
        this.wg = wg;
        final int vxCapacity = wg.getVertexCapacity();

        centres = new int[M];
        mean = new double[M];
        known = new boolean[vxCapacity];
        pivot = new boolean[vxCapacity];
        distance = new int[vxCapacity];

        xMatrix = new double[M][vxCapacity];
    }

    @Override
    public void arrange(final GraphWriteMethods wg) {
        set(wg);

        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            distance[vxId] = Integer.MAX_VALUE;
            pivot[vxId] = false;
            known[vxId] = false;
        }

        centres[0] = wg.getVertex(0);
        pivot[wg.getVertex(0)] = true;
        int currx = 0;

        // Find coordinates of nodes relative to first pivot.
        positionFrom(0, xMatrix[currx]);

        // For the remaining pivot points...
        for (int m = 1; m < M; m++) {
            // Find which point to use as pivot by searching
            // distances for a non-pivot point at the furthest
            // distance from that of any other pivot.
            int pdist = 0;
            int pnode = wg.getVertex(0);
            for (int npos = 0; npos < vxCount; npos++) {
                final int n = wg.getVertex(npos);

                // Choose node if its distance is at least that of the current best candidate.
                // and it is not already a pivot.
                if (distance[n] >= pdist && !pivot[n]) {
                    pnode = n;
                    pdist = distance[n];
                }

                // Clear known flag for next round.
                known[n] = false;
            }

            // Mark the chosen node as a pivot.
            centres[m] = pnode;
            pivot[pnode] = true;

            // View the graph relative to this new pivot.
            currx++;
            positionFrom(m, xMatrix[currx]);
        }

        // Centre the coordinates by subtracting the mean.
        currx = 0;
        for (int a = 0; a < M; a++) {
            mean[a] /= vxCount;
            for (int vpos = 0; vpos < vxCount; vpos++) {
                final int v = wg.getVertex(vpos);

                xMatrix[currx][v] -= mean[a];
            }

            currx++;
        }

        // Compute the covariance matrix.
        // S = *X*X^T)/n.
        final double[][] sMatrix = new double[M][M];

        for (int r = 0; r < M; r++) {
            for (int c = 0; c < M; c++) {
                sMatrix[r][c] = 0;
                for (int vpos = 0; vpos < vxCount; vpos++) {
                    final int v = wg.getVertex(vpos);

                    sMatrix[r][c] += xMatrix[r][v] * xMatrix[c][v];
                }

                // Not necessary because multiplication by a constant does not change the eigenvectors?
                // But we said we're computing the covariance matrix, so do it.
                sMatrix[r][c] /= vxCount;
            }
        }

        if (PART_ONLY) {
            return;
        }

        final double[] ui = new double[M];
        final double[] uihat = new double[M];

        // Compute the first C eigenvectors of S.
        final double[][] uMatrix = new double[dimensions][M];

        for (int u = 0; u < dimensions; u++) {
            // Initialise uihat to a normalised random vector.
            double norm = 0;
            for (int r = 0; r < M; r++) {
                uihat[r] = random.nextDouble();
                norm += uihat[r] * uihat[r];
            }

            norm = Math.sqrt(norm);
            for (int s = 0; s < M; s++) {
                uihat[s] /= norm;
            }

            final double epsilon = 0.001;
            double dot = 1000;
            double prevDot;
            int counter = 100;
            do {
                prevDot = dot;
                System.arraycopy(uihat, 0, ui, 0, M);

                // Orthogonalise ui against previous eigenvectors.
                for (int j = 0; j < u; j++) {
                    // Compute dot product ui*U[j].
                    dot = 0;
                    for (int m = 0; m < M; m++) {
                        dot += ui[m] * uMatrix[j][m];
                    }

                    // Subtract ui = ui - (transpose(ui).uj)uj.
                    norm = 0;
                    for (int m = 0; m < M; m++) {
                        ui[m] -= dot * uMatrix[j][m];
                        norm += ui[m] * ui[m];
                    }

                    for (int m = 0; m < M; m++) {
                        ui[m] /= norm;
                    }
                }

                // uihat = S*ui.
                for (int i = 0; i < M; i++) {
                    uihat[i] = 0;
                    for (int j = 0; j < M; j++) {
                        uihat[i] += sMatrix[i][j] * ui[j];
                    }
                }

                // Normalise uihat amd compute uihat(normed) * ui.
                norm = 0;
                for (int r = 0; r < M; r++) {
                    norm += uihat[r] * uihat[r];
                }

                norm = Math.sqrt(norm);

                dot = 0;
                for (int r = 0; r < M; r++) {
                    uihat[r] /= norm;
                    dot += uihat[r] * ui[r];
                }
            } while ((dot < (1 - epsilon) && Math.abs(dot - prevDot) > epsilon) && --counter == 0);

            System.arraycopy(uihat, 0, uMatrix[u], 0, M);
        }

        // Now compute actual coordinates.
        // Initialise coordinates in case C<3.
        final double[] pos = new double[3];
        Arrays.fill(pos, 0);

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        // pos = X * U^T.
        for (int npos = 0; npos < vxCount; npos++) {
            final int n = wg.getVertex(npos);

            for (int c = 0; c < dimensions; c++) {
                pos[c] = 0;
                for (int m = 0; m < M; m++) {
                    pos[c] += xMatrix[m][n] * uMatrix[c][m];
                }
            }

            wg.setFloatValue(xId, n, SCALE * (float) pos[0]);
            wg.setFloatValue(yId, n, SCALE * (float) pos[1]);
            wg.setFloatValue(zId, n, SCALE * (float) pos[2]);
        }
    }

    /**
     *
     * @param axis The index in the centres array of the vertex id to use as an
     * axis.
     * @param coord
     */
    private void positionFrom(final int axis, final double[] coord) {
        Arrays.fill(coord, -1);

        int node = centres[axis];
        coord[node] = 0;
        distance[node] = 0;
        mean[axis] = 0;

        // Queue for use in breadth-first traversal.
        final int[] queue = new int[wg.getVertexCount()];
        int head = 0;
        int tail = 0;
        queue[head++] = node;

        while (head != tail) {
            node = queue[tail++];
            final int neighbours = wg.getVertexNeighbourCount(node);
            for (int npos = 0; npos < neighbours; npos++) {
                final int neighbour = wg.getVertexNeighbour(node, npos);

                if (coord[neighbour] < 0) {
                    coord[neighbour] = coord[node] + 1;
                    mean[axis] += coord[neighbour];
                    if (distance[neighbour] > coord[neighbour]) {
                        distance[neighbour] = (int) coord[neighbour];
                    }

                    queue[head++] = neighbour;
                }
            }
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Required for Arranger, intentionally left blank
    }
}
