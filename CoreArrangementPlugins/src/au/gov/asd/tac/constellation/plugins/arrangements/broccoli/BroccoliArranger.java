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
package au.gov.asd.tac.constellation.plugins.arrangements.broccoli;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.ArrayList;

/**
 * Given a vertex with neighbours of degree 1, arrange those neighbours in a
 * broccoli shape fanned away from the centre of the graph.
 *
 * @author algol
 */
public final class BroccoliArranger implements Arranger {

    private static final Vector3f Y_VECTOR = new Vector3f(0, 1, 0);
    private static final Vector3f ZERO_VECTOR = new Vector3f(0, 0, 0);

    private GraphWriteMethods wg;
    private int vxCount;
    private boolean noneSelected;
    private int xId;
    private int yId;
    private int zId;
    private int nradiusId;
    private int selectedId;

    private void set(final GraphWriteMethods wg) {
        this.wg = wg;
        vxCount = wg.getVertexCount();
        selectedId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        noneSelected = noneSelected();

        xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        nradiusId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
    }

    private boolean noneSelected() {
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final boolean selected = wg.getBooleanValue(selectedId, vxId);
            if (selected) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        set(wg);

        final Vector3f centre = getCentre();

        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            if (wg.getVertexNeighbourCount(vxId) > 1 && (noneSelected || wg.getBooleanValue(selectedId, vxId))) {
                arrangeVertex(centre, vxId);
            }
        }
    }

    public void arrange(final GraphWriteMethods wg, final int vxId) {
        set(wg);

        final Vector3f centre = getCentre();
        arrangeVertex(centre, vxId);
    }

    /**
     * Arrange the neighbours of the given vertex with degree 1 into a fan
     * relative to the centre.
     *
     * @param centre
     * @param vid
     */
    private void arrangeVertex(final Vector3f centre, final int vxId) {
        float maxRadius = 0;
        final ArrayList<Integer> deg1 = new ArrayList<>();
        final int vncount = wg.getVertexNeighbourCount(vxId);
        for (int i = 0; i < vncount; i++) {
            final int vnId = wg.getVertexNeighbour(vxId, i);
            if (wg.getVertexNeighbourCount(vnId) == 1) {
                deg1.add(vnId);
                final float nradius = nradiusId != Graph.NOT_FOUND ? wg.getFloatValue(nradiusId, vnId) : 1;
                if (nradius > maxRadius) {
                    maxRadius = nradius;
                }
            }
        }

        if (!deg1.isEmpty()) {
            final Vector3f xyz = new Vector3f(wg.getFloatValue(xId, vxId), wg.getFloatValue(yId, vxId), wg.getFloatValue(zId, vxId));
            final int size = deg1.size();

            final int sideLen = (int) Math.floor(Math.sqrt(size - 1.0)) + 1;
            final float sideLen1 = sideLen - 1F;

            // Generate a suitable up vector for lookAt.
            final Vector3f tmpv = new Vector3f();
            tmpv.crossProduct(xyz, Y_VECTOR);
            final Vector3f up = new Vector3f();
            up.crossProduct(tmpv, xyz);
            up.normalize();

            // Create a rotation matrix that will rotate the positions we're about to create
            // to the parent vertex.
            final Frame frame = new Frame(xyz, ZERO_VECTOR, up);
            final Matrix44f rm = new Matrix44f();
            frame.getMatrix(rm, true);
            final Matrix33f rotm = new Matrix33f();
            rm.getRotationMatrix(rotm);

            // Layout the degree 1 vertices on the surface of a sphere.
            for (int i = 0; i < size; i++) {
                final int vx1Id = deg1.get(i);

                // A position in the unit square.
                final float xs = sideLen1 > 0 ? (((float) i / sideLen) / sideLen1) * 2 - 1 : 0;
                final float ys = sideLen1 > 0 ? ((i % sideLen) / sideLen1) * 2 - 1 : 0;
                final Vector3f v = new Vector3f(xs, ys, 1);

                // Map the square onto the surface of the unit sphere.
                v.normalize();

                // Rotate the normalized square relative to the centre, resize it,
                // and move it to its parent.
                final Vector3f vrot = new Vector3f();
                vrot.rotate(v, rotm);
                vrot.scale(sideLen * 2 * maxRadius);
                vrot.add(xyz);

                final float x = vrot.getX();
                final float y = vrot.getY();
                final float z = vrot.getZ();

                wg.setFloatValue(xId, vx1Id, x);
                wg.setFloatValue(yId, vx1Id, y);
                wg.setFloatValue(zId, vx1Id, z);
            }
        }
    }

    @Override
    public void setMaintainMean(boolean b) {
        // Required for Arranger, intentionally left blank
    }

    private Vector3f getCentre() {
        final BBoxf box = new BBoxf();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            final float z = wg.getFloatValue(zId, vxId);
            box.add(x, y, z);
        }

        final float[] c = box.getCentre();

        return new Vector3f(c[BBoxf.X], c[BBoxf.Y], c[BBoxf.Z]);
    }
}
