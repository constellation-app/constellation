/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.circle;

import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphUtilities;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Random;

/**
 * Arrange in a sphere.
 * <p>
 * The arrangement is actually done by laying out six grids to form a cube (with
 * a bump to avoid overlaps), then normalising the distance of each vertex from
 * the centre.
 * <p>
 * The cube can be seen in the alternate x,y,z.
 *
 * @author algol
 */
public class SphereArranger implements Arranger {

    private boolean maintainMean = false;

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final int vxCount = wg.getVertexCount();
        if (vxCount < 2) {
            return;
        }

        final float[] oldMean = maintainMean ? GraphUtilities.getXyzMean(wg) : null;

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int x2Id = wg.addAttribute(GraphElementType.VERTEX, "float", "x2", "x2", 0, null);
        final int y2Id = wg.addAttribute(GraphElementType.VERTEX, "float", "y2", "y2", 0, null);
        final int z2Id = wg.addAttribute(GraphElementType.VERTEX, "float", "z2", "z2", 0, null);

        // If we have less than 2 vertices, we'll get divide-by-zero NaNs.
        // Pretend we have at least 2 vertices for sizing purposes.
        final int sideLen = Math.max((int) Math.sqrt(vxCount / 6.0) + (vxCount % 6 == 0 ? 0 : 1), 2);

        // We should be taking actual vertex radiuses into account...
        float radius = sideLen * 3f;
        final int sideLen2 = sideLen * sideLen;
        final float sideLen1 = (float) (sideLen - 1);

        // If we lay the vertices out on the sides of a (unit) cube, then the rows/columns along the edges of each side
        // will overlap, which results in "missing" icons.
        // Therefore we "bump" each side of the cube out from the centre, so the edges don't overlap.
        final float bump = 1f + 1f / sideLen;

        // Don't use the position directly; if the number of vertices on a cube face isn't square,
        // the leftover blank spots will be at the end and look ugly.
        // Instead, select from shuffled positions, so the blank spots are randomly distributed across the faces.
        final int[] shuffled = shuffled(sideLen2);
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int face = position % 6;

            // A (slightly bumped) point on the surface of the unit cube.
            final int p = shuffled[(position / 6) % sideLen2];
            final float x;
            final float y;
            final float z;
            switch (face) {
                default:
                case 0:
                    x = -bump;
                    y = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    z = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
                case 1:
                    x = bump;
                    y = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    z = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
                case 2:
                    y = -bump;
                    x = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    z = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
                case 3:
                    y = bump;
                    x = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    z = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
                case 4:
                    z = -bump;
                    x = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    y = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
                case 5:
                    z = bump;
                    x = (((float) p / sideLen) / sideLen1) * 2 - 1;
                    y = ((p % sideLen) / sideLen1) * 2 - 1;
                    break;
            }

            // Normalise to the surface of a sphere.
            final float f = 1 / (float) Math.sqrt(x * x + y * y + z * z);
            final float x0 = x * f;
            final float y0 = y * f;
            final float z0 = z * f;

            wg.setFloatValue(xId, vxId, radius * x0);
            wg.setFloatValue(yId, vxId, radius * y0);
            wg.setFloatValue(zId, vxId, radius * z0);
            wg.setFloatValue(x2Id, vxId, radius * x);
            wg.setFloatValue(y2Id, vxId, radius * y);
            wg.setFloatValue(z2Id, vxId, radius * z);
        }

        if (maintainMean) {
            GraphUtilities.moveMean(wg, oldMean);
        }
    }

    private static int[] shuffled(final int n) {
        final Random random = new Random();
        final int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }

        // Shuffle the array.
        for (int i = n; i > 1; i--) {
            final int ix = random.nextInt(i);
            final int t = a[i - 1];
            a[i - 1] = a[ix];
            a[ix] = t;
        }

        return a;
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

}
