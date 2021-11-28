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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.awt.Dimension;
import java.util.BitSet;

/**
 * Arrange the graph in a grid.
 *
 * @author algol
 */
public class GridArranger implements Arranger {

    private final GridChoiceParameters params;
    private boolean forceEvenNumCols;
    private boolean maintainMean;
    private int topLeftVxId;

    /**
     * A grid arrangement with default parameters.
     */
    public GridArranger() {
        this(GridChoiceParameters.getDefaultParameters());
    }

    /**
     * Construct new ArrangeInGrid instance.
     *
     * @param params Parameters for the arrangement.
     */
    public GridArranger(final GridChoiceParameters params) {
        this.params = params;
        maintainMean = false;
        forceEvenNumCols = false;
        topLeftVxId = Graph.NOT_FOUND;
    }

    /**
     * Construct new ArrangeInGrid instance.
     *
     * @param params Parameters for the arrangement.
     * @param forceEvenNumCols If true, ensure that there is an even number of
     * columns in the grid.
     */
    public GridArranger(final GridChoiceParameters params, final boolean forceEvenNumCols) {
        this.params = params;
        maintainMean = false;
        this.forceEvenNumCols = forceEvenNumCols;
        topLeftVxId = Graph.NOT_FOUND;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

    /**
     * Specify whether there must be an even number of columns in the grid.
     *
     * @param b If true, ensure that there is an even number of columns in the
     * grid.
     */
    public void setForceEvenNumCols(final boolean b) {
        forceEvenNumCols = b;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {

        // Get/set the x,y,z attributes.
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", null, null);
        }

        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int radiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(wg);

        final int vxCount = wg.getVertexCount();
        if (vxCount > 0) {
            final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

            final Dimension gridDimensions = getGridSize(params.getGridChoice());
            final int nRows = gridDimensions.height;
            final int nCols = gridDimensions.width;

            int nfRows = nRows;
            int nfCols = nCols;

            if (nRows == 0 && nCols == 0) {
                nfCols = (int) Math.ceil(Math.sqrt(vxCount));
                if (forceEvenNumCols && nfCols % 2 == 1) {
                    nfCols++;
                }

                nfRows = (int) Math.ceil(vxCount / (double) nfCols);
            } else if (nRows == 0) {
                nfRows = (int) Math.ceil(vxCount / (double) nCols);
            } else {
                nfCols = (int) Math.ceil(vxCount / (double) nRows);
            }

            // Figure out the column and row sizes.
            // Node sizes depend on the nradius attribute: if it isn't present node radius=1.
            final float[] colWidths = new float[nfCols];
            final float[] rowHeights = new float[nfRows];

            // We loop through a BitSet because we might be arranging in even
            // columns, so for every left-side vertex we need to simultaneously
            // arrange a right-side vertex, which removes the right-side
            // vertex from being arranged later.
            final BitSet vertices = ArrangementUtilities.vertexBits(wg);
            int vxPos = 0;
            final int[] vxOrder = new int[vxCount];
            for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                final float diameter = radiusAttr != Graph.NOT_FOUND ? 2 * wg.getFloatValue(radiusAttr, vxId) : 2;
                final int row = vxPos / nfCols;
                final int col = vxPos - (row * nfCols);
                if (colWidths[col] < diameter) {
                    colWidths[col] = diameter;
                }
                if (rowHeights[row] < diameter) {
                    rowHeights[row] = diameter;
                }

                vxOrder[vxPos++] = vxId;
                vertices.clear(vxId);

                if (forceEvenNumCols) {
                    // We're doing doublets: find this vertex's neighbour and put it next to this one,
                    // then avoid redoing the neighbour.
                    int nbId = wg.getVertexNeighbour(vxId, 0);
                    // Double check that we haven't stumbled upon a loop
                    if (nbId == vxId) {
                        nbId = wg.getVertexNeighbour(vxId, 1);
                    }
                    if (vertices.get(nbId)) {
                        final float nbDiameter = radiusAttr != Graph.NOT_FOUND ? 2 * wg.getFloatValue(radiusAttr, nbId) : 2;
                        final int nbRow = vxPos / nfCols;
                        final int nbCol = vxPos - (nbRow * nfCols);
                        if (colWidths[nbCol] < nbDiameter) {
                            colWidths[nbCol] = nbDiameter;
                        }
                        if (rowHeights[nbRow] < nbDiameter) {
                            rowHeights[nbRow] = nbDiameter;
                        }

                        vxOrder[vxPos++] = nbId;
                        vertices.clear(nbId);
                    }
                }
            }

            // Figure out the total size.
            float totalWidth = 0;
            for (int i = 0; i < nfCols; i++) {
                totalWidth += colWidths[i] * params.getSizeGain();
            }
            totalWidth += (nfCols - 1) * params.getHorizontalGap();

            float totalHeight = 0;
            for (int i = 0; i < nfRows; i++) {
                totalHeight += rowHeights[i] * params.getSizeGain();
            }
            totalHeight += (nfRows - 1) * params.getVerticalGap();

            // Figure out the origin.
            final float xOrig = -totalWidth / 2;
            final float yOrig = -totalHeight / 2;

            // Figure out the centres of the rows and columns.
            float[] colCentres = new float[nfCols];
            float[] rowCentres = new float[nfRows];
            colCentres[0] = (float) (xOrig + ((colWidths[0] * params.getSizeGain()) / 2.0));
            rowCentres[0] = (float) (yOrig + ((rowHeights[0] * params.getSizeGain()) / 2.0));

            for (int i = 1; i < nfCols; i++) {
                colCentres[i] = colCentres[i - 1] + (float) (((colWidths[i - 1] + colWidths[i]) * params.getSizeGain()) / 2.0) + params.getHorizontalGap();
            }

            for (int i = 1; i < nfRows; i++) {
                rowCentres[i] = rowCentres[i - 1] + (float) (((rowHeights[i - 1] + rowHeights[i]) * params.getSizeGain()) / 2.0) + params.getVerticalGap();
            }

            for (int i = 0; i < vxPos; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int vxId = vxOrder[i];

                final int row = i / nfCols;
                final int col = i - row * nfCols;

                if (col == 0 && row == nfRows - 1) {
                    topLeftVxId = vxId;
                }

                float x = colCentres[col];
                float y = rowCentres[row];

                // Offset even columns vertically so side-by-side labels don't overlap.
                if (params.hasRowOffsets() && col % 2 == 0) {
                    y += rowHeights[row] / 2;
                }

                wg.setFloatValue(xAttr, vxId, x);
                wg.setFloatValue(yAttr, vxId, y);
                wg.setFloatValue(zAttr, vxId, 0);
            }

            if (maintainMean) {
                ArrangementUtilities.moveMean(wg, oldMean);
            }
        }
    }

    public int getTopLeftVxId() {
        return topLeftVxId;
    }

    /**
     * Given the choice of grid (one of the GRID_CHOICE_xxx constants), return a
     * setting from number of rows and number of columns. Zero means
     * unconstrained.
     *
     * @param gc The kind of grid to do the arrangement with.
     *
     * @return A Dimension corresponding to the choice of grid.
     */
    private static Dimension getGridSize(final GridChoice gc) {
        if (gc == null) {
            return new Dimension(0, 0);
        } else {
            switch (gc) {
                case HORIZONTAL_LINE:
                    return new Dimension(0, 1);
                case VERTICAL_LINE:
                    return new Dimension(1, 0);
                case TWO_ROWS:
                    return new Dimension(0, 2);
                case THREE_ROWS:
                    return new Dimension(0, 3);
                case FOUR_ROWS:
                    return new Dimension(0, 4);
                case TWO_COLUMNS:
                    return new Dimension(2, 0);
                case THREE_COLUMNS:
                    return new Dimension(3, 0);
                case FOUR_COLUMNS:
                    return new Dimension(4, 0);
                default:
                    // Return a SQUARE
                    return new Dimension(0, 0);
            }
        }
    }
}
