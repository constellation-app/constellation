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
package au.gov.asd.tac.constellation.graph.interaction.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;

/**
 *
 * The user is dragging a line. Pan the scene using the point on the line.
 * <p>
 * We'll get the vertices at either end of the line and project their
 * coordinates to the screen. This will tell us how far down the (projected)
 * line the mouse is, which we can use to find how far between the world
 * coordinates of the vertices the mouse is. We can combine that with the mouse
 * dx,dy to pan the scene accurately.
 *
 * @author algol
 */
class LineDragger {

    private final float mouseDragRatio;
    private final Vector3f mouseUnprojected;
    private final Vector4f mouseProjected;
    private float totaldx;
    private float totaldy;

    LineDragger(final Graph graph, final int txId, final Matrix44f mvpMatrix, final int[] viewport, final int mousex, final int mousey) {
        totaldx = 0;
        totaldy = 0;

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int xAttrId = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
            final int yAttrId = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
            final int zAttrId = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

            final int sVxId = rg.getTransactionSourceVertex(txId);
            final int dVxId = rg.getTransactionDestinationVertex(txId);

            final Vector3f spos = new Vector3f(rg.getFloatValue(xAttrId, sVxId), rg.getFloatValue(yAttrId, sVxId), rg.getFloatValue(zAttrId, sVxId));
            final Vector3f dpos = new Vector3f(rg.getFloatValue(xAttrId, dVxId), rg.getFloatValue(yAttrId, dVxId), rg.getFloatValue(zAttrId, dVxId));

            final Vector4f sProjected = new Vector4f();
            final Vector4f dProjected = new Vector4f();

            Graphics3DUtilities.project(spos, mvpMatrix, viewport, sProjected);
            Graphics3DUtilities.project(dpos, mvpMatrix, viewport, dProjected);

            // How far down the line (from source vertex to destination vertex) is the mouse?
            float r = (mousex - sProjected.getX()) / (dProjected.getX() - sProjected.getX());
            if (Float.isInfinite(r)) {
                // The vertices are vertically aligned (same x), so use y.
                r = (mousey - sProjected.getY()) / (dProjected.getY() - sProjected.getY());
                if (Float.isInfinite(r)) {
                    // The vertices are horizontally aligned (same y).
                    // For the user to be able to see the line, they must have a different z.
                    r = 1;
                }
            }

            mouseDragRatio = Math.abs(r);

            mouseUnprojected = new Vector3f(spos.getX() + (dpos.getX() - spos.getX()) * mouseDragRatio, spos.getY() + (dpos.getY() - spos.getY()) * mouseDragRatio, spos.getZ() + (dpos.getZ() - spos.getZ()) * mouseDragRatio);
            mouseProjected = new Vector4f();
            Graphics3DUtilities.project(mouseUnprojected, mvpMatrix, viewport, mouseProjected);

        } finally {
            rg.release();
        }
    }

    public Vector3f getTranslation(final Matrix44f mvpMatrix, final int[] viewport, final float dx, final float dy) {
        totaldx += dx;
        totaldy += dy;

        final Vector4f mpDelta = new Vector4f(mouseProjected.getX() + totaldx, mouseProjected.getY() - totaldy, mouseProjected.getZ(), mouseProjected.getW());
        final Vector3f muDelta = new Vector3f();
        Graphics3DUtilities.unproject(mpDelta, mvpMatrix, viewport, muDelta);

        return new Vector3f(muDelta.getX() - mouseUnprojected.getX(), muDelta.getY() - mouseUnprojected.getY(), muDelta.getZ() - mouseUnprojected.getZ());
    }
}
