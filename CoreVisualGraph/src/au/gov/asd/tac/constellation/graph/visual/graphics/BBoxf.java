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
package au.gov.asd.tac.constellation.graph.visual.graphics;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;

/**
 * Simple bounding box.
 *
 * @author algol
 */
public class BBoxf {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    private final float[] min;
    private final float[] max;

    public BBoxf() {
        min = new float[3];
        Arrays.fill(min, Float.MAX_VALUE);
        max = new float[3];
        Arrays.fill(max, -Float.MAX_VALUE);
    }

    public void add(final float x, final float y, final float z) {
        if (x < min[X]) {
            min[X] = x;
        }
        if (y < min[Y]) {
            min[Y] = y;
        }
        if (z < min[Z]) {
            min[Z] = z;
        }

        if (x > max[X]) {
            max[X] = x;
        }
        if (y > max[Y]) {
            max[Y] = y;
        }
        if (z > max[Z]) {
            max[Z] = z;
        }
    }

    /**
     * Return true if nothing has been added to the bounding box, false
     * otherwise.
     *
     * @return True if nothing has been added to the bounding box, false
     * otherwise.
     */
    public boolean isEmpty() {
        return min[X] > max[X];
    }

    /**
     * Return the centre of the bounding box.
     *
     * @return The centre of the bounding box.
     */
    public float[] getCentre() {
        final float[] c = new float[3];
        for (int i = 0; i < 3; i++) {
            c[i] = min[i] + (max[i] - min[i]) / 2F;
        }

        return c;
    }

    /**
     * Return the minimum x,y,z values.
     *
     * @return The minimum x,y,z values.
     */
    public float[] getMin() {
        return Arrays.copyOf(min, 3);
    }

    /**
     * Return the maximum x,y,z values.
     *
     * @return The maximum x,y,z values.
     */
    public float[] getMax() {
        return Arrays.copyOf(max, 3);
    }

    @Override
    public String toString() {
        return String.format("BB[%s,%s]", Arrays.toString(min), Arrays.toString(max));
    }

    /**
     * Get the bounding box of this graph.
     * <p>
     * Only the x,y,z values are used.
     *
     * @param rg The graph to create a bounding box from.
     *
     * @return The bounding box of all vertices in the graph.
     */
    public static BBoxf getGraphBoundingBox(final GraphReadMethods rg) {
        final int xId = VisualConcept.VertexAttribute.X.get(rg);
        final int yId = VisualConcept.VertexAttribute.Y.get(rg);
        final int zId = VisualConcept.VertexAttribute.Z.get(rg);
        final int vxCount = rg.getVertexCount();
        final BBoxf box = new BBoxf();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = rg.getVertex(position);

            final float x = rg.getFloatValue(xId, vxId);
            final float y = rg.getFloatValue(yId, vxId);
            final float z = rg.getFloatValue(zId, vxId);
            box.add(x, y, z);
        }

        return box;
    }
}
