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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.BitSet;

/**
 * The centre and side radius of a set of vertices.
 *
 * Since vertex icons are square, the radius is the vertical (or horizontal,
 * same thing for a square) distance from the vertex point to the edge of the
 * icon. This is a side radius.
 * <p>
 * The minimal circle that encompasses the icon passes through the corners of
 * the square, and therefore has a radius of Math.sqrt(2) (Pythagoras rocks!).
 * When using vertices to represent underlying components, don't forget to take
 * this into account.
 * <p>
 * Both nradius and lradius are made available.
 *
 * @author algol
 */
public class Extent {

    private final float x;
    private final float y;
    private final float z;
    private final float nradius;
    private final float lradius;

    protected Extent(final float[] xyz, final float nradius, final float lradius) {
        x = xyz[0];
        y = xyz[1];
        z = xyz[2];
        this.nradius = nradius;
        this.lradius = lradius;
    }

    public float getNRadius() {
        return nradius;
    }

    public float getLRadius() {
        return lradius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("Extent[%f,%f,%f;nr=%f;lr=%f]", x, y, z, nradius, lradius);
    }

    /**
     * Determine the extent of the specified vertices, taking into account the
     * radius of each vertex.
     *
     * @param rg The GraphReadMethods containing the vertices.
     * @param vertices The vertices to include in the extent.
     *
     * @return the extent of the specified vertices.
     */
    public static Extent getExtent(final GraphReadMethods rg, final BitSet vertices) {
        final int xAttr = VisualConcept.VertexAttribute.X.get(rg);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(rg);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(rg);
        final int nradiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.get(rg);
        final int lradiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(rg);

        float nminx = Float.MAX_VALUE;
        float nminy = Float.MAX_VALUE;
        float nminz = Float.MAX_VALUE;
        float nmaxx = -Float.MAX_VALUE;
        float nmaxy = -Float.MAX_VALUE;
        float nmaxz = -Float.MAX_VALUE;

        float lminx = Float.MAX_VALUE;
        float lminy = Float.MAX_VALUE;
        float lminz = Float.MAX_VALUE;
        float lmaxx = -Float.MAX_VALUE;
        float lmaxy = -Float.MAX_VALUE;
        float lmaxz = -Float.MAX_VALUE;

        for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
            final float x = rg.getFloatValue(xAttr, vxId);
            final float y = rg.getFloatValue(yAttr, vxId);
            final float z = rg.getFloatValue(zAttr, vxId);
            final float nradius = nradiusAttr != Graph.NOT_FOUND ? rg.getFloatValue(nradiusAttr, vxId) : 1;
            final float lradius = lradiusAttr != Graph.NOT_FOUND ? rg.getFloatValue(lradiusAttr, vxId) : 1;

            nminx = Math.min(nminx, x - nradius);
            nminy = Math.min(nminy, y - nradius);
            nminz = Math.min(nminz, z - nradius);
            nmaxx = Math.max(nmaxx, x + nradius);
            nmaxy = Math.max(nmaxy, y + nradius);
            nmaxz = Math.max(nmaxz, z + nradius);

            lminx = Math.min(lminx, x - lradius);
            lminy = Math.min(lminy, y - lradius);
            lminz = Math.min(lminz, z - lradius);
            lmaxx = Math.max(lmaxx, x + lradius);
            lmaxy = Math.max(lmaxy, y + lradius);
            lmaxz = Math.max(lmaxz, z + lradius);
        }

//        Debug.debug("x %f %f (%f); y %f %f (%f); z %f %f (%f)\n", minx, maxx, (maxx-minx)/2, miny, maxy, (maxy-miny)/2, minz, maxz, (maxz-minz)/2);
        final float nextentRadius = Math.max(nmaxx - nminx, Math.max(nmaxy - nminy, nmaxz - nminz)) / 2;
        final float lextentRadius = Math.max(lmaxx - lminx, Math.max(lmaxy - lminy, lmaxz - lminz)) / 2;

        final float[] centre = new float[]{(nmaxx + nminx) / 2, (nmaxy + nminy) / 2, (nmaxz + nminz) / 2};

        return new Extent(centre, nextentRadius, lextentRadius);
    }
}
