/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.operations.SetBooleanValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;

/**
 * Select elements by rectangular area.
 *
 * @author sirius
 */
@Messages("BoxSelectionPlugin=Select in Box")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public final class BoxSelectionPlugin extends SimpleEditPlugin {

    private final boolean isAdd;
    private final boolean isToggle;
    private final Camera camera;
    private final float[] box; // left, right, top, bottom in camera coordinates

    public BoxSelectionPlugin(final boolean isAdd, final boolean isToggle, final Camera camera, final float[] box) {
        this.isAdd = isAdd;
        this.isToggle = isToggle;
        this.camera = camera;
        this.box = box;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final float mix = camera.getMix();
        final float inverseMix = 1.0F - mix;
        final Vector3f centre = new Vector3f(camera.lookAtCentre);

        final float left = box[0];
        final float right = box[1];
        final float top = box[2];
        final float bottom = box[3];

        // Look up all the required attributes.
        int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        int zAttr = VisualConcept.VertexAttribute.Z.get(graph);
        final int x2Attr = VisualConcept.VertexAttribute.X2.get(graph);
        final int y2Attr = VisualConcept.VertexAttribute.Y2.get(graph);
        final int z2Attr = VisualConcept.VertexAttribute.Z2.get(graph);
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        final int vxVisibilityAttr = VisualConcept.VertexAttribute.VISIBILITY.get(graph);
        final int txVisibilityAttr = VisualConcept.TransactionAttribute.VISIBILITY.get(graph);
        final int vxLayerVisibilityAttr = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(graph);
        final int txLayerVisibilityAttr = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(graph);

        final SetBooleanValuesOperation selectVerticesOperation = new SetBooleanValuesOperation(graph, GraphElementType.VERTEX, vxSelectedAttr);
        final SetBooleanValuesOperation selectTransactionsOperation = new SetBooleanValuesOperation(graph, GraphElementType.TRANSACTION, txSelectedAttr);

        final float visibilityHigh = camera.getVisibilityHigh();
        final float visibilityLow = camera.getVisibilityLow();

        // Get a copy of the current rotation matrix.
        final Vector3f diff = Vector3f.subtract(camera.lookAtEye, camera.lookAtCentre);
        final float cameraDistance = diff.getLength();

        // Get the inverse eye rotation to match the object frame rotation.
        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        final Matrix44f objectFrameMatrix = new Matrix44f();
        frame.getMatrix(objectFrameMatrix, true);
        final Matrix44f rotationMatrixt = new Matrix44f();
        objectFrameMatrix.getRotationMatrix(rotationMatrixt);
        final Matrix44f rotationMatrixti = new Matrix44f();
        rotationMatrixti.invert(rotationMatrixt);
        final Matrix33f rotationMatrix = new Matrix33f();
        rotationMatrixti.getRotationMatrix(rotationMatrix);

        // Do the vertex positions need mixing?
        boolean requiresMix = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;
        boolean requiresVertexVisibility = vxVisibilityAttr != Graph.NOT_FOUND;
        boolean requiresTransactionVisibility = txVisibilityAttr != Graph.NOT_FOUND;

        // If the mix value is either 0 or 1 then no mixing is required
        if (requiresMix && mix == 0.0F) {
            requiresMix = false;
        } else if (requiresMix && mix == 1.0F) {
            xAttr = x2Attr;
            yAttr = y2Attr;
            zAttr = z2Attr;
            requiresMix = false;
        } else {
            // Do nothing
        }

        final BitSet vxIncluded = new BitSet();

        final int vxCount = graph.getVertexCount();

        // Select the correct vertices.
        for (int position = 0; position != vxCount; position++) {
            final int vxId = graph.getVertex(position);

            if (requiresVertexVisibility) {
                final float visibility = graph.getFloatValue(vxVisibilityAttr, vxId);
                final float layerVisibility = graph.getFloatValue(vxLayerVisibilityAttr, vxId);
                if (layerVisibility == 0.0F || visibility <= 1.0F && (visibility > visibilityHigh || visibility < visibilityLow)) {
                    continue;
                }
            }

            // Get the main location of the vertex.
            float x = xAttr != Graph.NOT_FOUND ? graph.getFloatValue(xAttr, vxId) : VisualGraphDefaults.getDefaultX(vxId);
            float y = yAttr != Graph.NOT_FOUND ? graph.getFloatValue(yAttr, vxId) : VisualGraphDefaults.getDefaultY(vxId);
            float z = zAttr != Graph.NOT_FOUND ? graph.getFloatValue(zAttr, vxId) : VisualGraphDefaults.getDefaultZ(vxId);

            // If mixing is required then mix the main location with the alternative location.
            if (requiresMix) {
                x = inverseMix * x + mix * graph.getFloatValue(x2Attr, vxId);
                y = inverseMix * y + mix * graph.getFloatValue(y2Attr, vxId);
                z = inverseMix * z + mix * graph.getFloatValue(z2Attr, vxId);
            }

            // Convert world coordinates to camera coordinates.
            final Vector3f sceneLocation = convertWorldToScene(x, y, z, centre, rotationMatrix, cameraDistance);
            final int rAttr = VisualConcept.VertexAttribute.NODE_RADIUS.get(graph);
            final float r = graph.getFloatValue(rAttr, vxId);

            if (sceneLocation.getZ() < 0) {

                final float leftMostPoint = (sceneLocation.getX() - r) / -sceneLocation.getZ();
                final float rightMostPoint = (sceneLocation.getX() + r) / -sceneLocation.getZ();
                final float bottomMostPoint = (sceneLocation.getY() - r) / -sceneLocation.getZ();
                final float topMostPoint = (sceneLocation.getY() + r) / -sceneLocation.getZ();

                final boolean vertexLeftOfBox = rightMostPoint < left;
                final boolean vertexRightOfBox = right < leftMostPoint;
                final boolean vertexBelowBox = topMostPoint < bottom;
                final boolean vertexAboveBox = top < bottomMostPoint;

                if (!vertexLeftOfBox && !vertexRightOfBox && !vertexBelowBox && !vertexAboveBox) {
                    vxIncluded.set(vxId);
                }
            }
        }

        final BitSet txIncluded = new BitSet();

        if (vxIncluded.isEmpty()) {
            // There were no vertices in the selection box,
            // so now we check if any lines overlapped. If they do, include the vertices at the ends of
            // those lines.
            // Note: we're checking the actual connections, not what is drawn on the display.
            // This could be confusing for the user, but checking against what is actually displayed would be
            // a very different kettle of lines.
            final int linkCount = graph.getLinkCount();
            for (int position = 0; position < linkCount; position++) {
                final int linkId = graph.getLink(position);

                final int vxLo = graph.getLinkLowVertex(linkId);
                final int vxHi = graph.getLinkHighVertex(linkId);

                // Get the main location of the lo vertex.
                float xLo = xAttr != Graph.NOT_FOUND ? graph.getFloatValue(xAttr, vxLo) : VisualGraphDefaults.getDefaultX(vxLo);
                float yLo = yAttr != Graph.NOT_FOUND ? graph.getFloatValue(yAttr, vxLo) : VisualGraphDefaults.getDefaultY(vxLo);
                float zLo = zAttr != Graph.NOT_FOUND ? graph.getFloatValue(zAttr, vxLo) : VisualGraphDefaults.getDefaultZ(vxLo);

                // Get the main location of the lo vertex.
                float xHi = xAttr != Graph.NOT_FOUND ? graph.getFloatValue(xAttr, vxHi) : VisualGraphDefaults.getDefaultX(vxHi);
                float yHi = yAttr != Graph.NOT_FOUND ? graph.getFloatValue(yAttr, vxHi) : VisualGraphDefaults.getDefaultY(vxHi);
                float zHi = zAttr != Graph.NOT_FOUND ? graph.getFloatValue(zAttr, vxHi) : VisualGraphDefaults.getDefaultZ(vxHi);

                if (requiresMix) {
                    xLo = inverseMix * xLo + mix * graph.getFloatValue(x2Attr, vxLo);
                    yLo = inverseMix * yLo + mix * graph.getFloatValue(y2Attr, vxLo);
                    zLo = inverseMix * zLo + mix * graph.getFloatValue(z2Attr, vxLo);

                    xHi = inverseMix * xHi + mix * graph.getFloatValue(x2Attr, vxHi);
                    yHi = inverseMix * yHi + mix * graph.getFloatValue(y2Attr, vxHi);
                    zHi = inverseMix * zHi + mix * graph.getFloatValue(z2Attr, vxHi);
                }

                // Convert world coordinates to camera coordinates.
                final Vector3f worldLocationLo = new Vector3f(xLo, yLo, zLo);
                worldLocationLo.subtract(centre);
                final Vector3f lo = new Vector3f();
                lo.rotate(worldLocationLo, rotationMatrix);
                lo.setZ(lo.getZ() - cameraDistance);

                final Vector3f worldLocationHi = new Vector3f(xHi, yHi, zHi);
                worldLocationHi.subtract(centre);
                final Vector3f hi = new Vector3f();
                hi.rotate(worldLocationHi, rotationMatrix);
                hi.setZ(hi.getZ() - cameraDistance);

                // If at least one of the end-points is in front of the eye...
                if (lo.getZ() < 0 || hi.getZ() < 0) {
                    // Project the 3d eye coordinates of the ends of the line onto the 2D viewing plane.
                    // The algorithm to do this depends on where the points are.
                    // If a point is in front of the eye (z<0), project to the z==0 plane.
                    // See 2.13 Coordinate Transformations in "OpenGL 3.3 (Core Profile)".
                    // If a point is behind the eye (z>0), the line connecting the points must be clipped at z==0.
                    // Project x,y by using the equation for findnig where a line crosses the plane z==0.
                    // See 2.19.1 Clipping Shader Varying Outputs in "OpenGL 3.3 (Core Profile)".
                    final float horizontalOffsetLo;
                    final float horizontalOffsetHi;
                    final float verticalOffsetLo;
                    final float verticalOffsetHi;
                    if (lo.getZ() < 0) {
                        final float loz = -Math.abs(lo.getZ());
                        horizontalOffsetLo = lo.getX() / -loz;
                        verticalOffsetLo = lo.getY() / -loz;
                    } else if (lo.getZ() > 0) {
                        horizontalOffsetLo = lo.getX() + (lo.getZ() * (hi.getX() - lo.getX())) / (lo.getZ() - hi.getZ());
                        verticalOffsetLo = lo.getY() + (lo.getZ() * (hi.getY() - lo.getY()) / (lo.getZ() - hi.getZ()));
                    } else {
                        horizontalOffsetLo = lo.getX();
                        verticalOffsetLo = lo.getY();
                    }

                    if (hi.getZ() < 0) {
                        final float hiz = -Math.abs(hi.getZ());
                        horizontalOffsetHi = hi.getX() / -hiz;
                        verticalOffsetHi = hi.getY() / -hiz;
                    } else if (hi.getZ() > 0) {
                        horizontalOffsetHi = lo.getX() + (lo.getZ() * (hi.getX() - lo.getX())) / (lo.getZ() - hi.getZ());
                        verticalOffsetHi = lo.getY() + (lo.getZ() * (hi.getY() - lo.getY()) / (lo.getZ() - hi.getZ()));
                    } else {
                        horizontalOffsetHi = hi.getX();
                        verticalOffsetHi = hi.getY();
                    }

                    final boolean intersects = lineSegmentIntersectsRectangle(
                            horizontalOffsetLo, verticalOffsetLo,
                            horizontalOffsetHi, verticalOffsetHi,
                            left, bottom, right, top);

                    if (intersects) {
                        final int linkTxCount = graph.getLinkTransactionCount(linkId);
                        for (int linkPos = 0; linkPos < linkTxCount; linkPos++) {
                            final int txId = graph.getLinkTransaction(linkId, linkPos);
                            txIncluded.set(txId);
                        }
                    }
                }
            }
        }

        final int txCount = graph.getTransactionCount();

        if (txIncluded.isEmpty()) {
            if (isAdd) {

                if (vxSelectedAttr != Graph.NOT_FOUND) {
                    for (int vxId = vxIncluded.nextSetBit(0); vxId >= 0; vxId = vxIncluded.nextSetBit(vxId + 1)) {
                        if (!graph.getBooleanValue(vxSelectedAttr, vxId)) {
                            selectVerticesOperation.setValue(vxId, true);
                        }
                    }
                }

                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < txCount; position++) {
                        final int txId = graph.getTransaction(position);
                        if (vxIncluded.get(graph.getTransactionSourceVertex(txId)) && vxIncluded.get(graph.getTransactionDestinationVertex(txId)) && !graph.getBooleanValue(txSelectedAttr, txId)) {
                            if (requiresTransactionVisibility) {
                                final float visibility = graph.getFloatValue(txVisibilityAttr, txId);
                                final float layerVisibility = graph.getFloatValue(txLayerVisibilityAttr, txId);
                                if (layerVisibility == 0.0F || visibility <= 1.0F && (visibility > visibilityHigh || visibility < visibilityLow)) {
                                    continue;
                                }
                            }
                            selectTransactionsOperation.setValue(txId, true);
                        }
                    }
                }

            } else if (isToggle) {

                if (vxSelectedAttr != Graph.NOT_FOUND) {
                    for (int vertex = vxIncluded.nextSetBit(0); vertex >= 0; vertex = vxIncluded.nextSetBit(vertex + 1)) {
                        selectVerticesOperation.setValue(vertex, !graph.getBooleanValue(vxSelectedAttr, vertex));
                    }
                }

                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < txCount; position++) {
                        final int txId = graph.getTransaction(position);
                        if (vxIncluded.get(graph.getTransactionSourceVertex(txId)) && vxIncluded.get(graph.getTransactionDestinationVertex(txId))) {
                            if (requiresTransactionVisibility) {
                                final float visibility = graph.getFloatValue(txVisibilityAttr, txId);
                                final float layerVisibility = graph.getFloatValue(txLayerVisibilityAttr, txId);
                                if (layerVisibility == 0.0F || visibility <= 1.0F && (visibility > visibilityHigh || visibility < visibilityLow)) {
                                    continue;
                                }
                            }
                            selectTransactionsOperation.setValue(txId, !graph.getBooleanValue(txSelectedAttr, txId));
                        }
                    }
                }

            } else {

                if (vxSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < vxCount; position++) {
                        final int vxId = graph.getVertex(position);
                        final boolean included = vxIncluded.get(vxId);
                        if (included != graph.getBooleanValue(vxSelectedAttr, vxId)) {
                            selectVerticesOperation.setValue(vxId, included);
                        }
                    }
                }

                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < txCount; position++) {
                        final int txId = graph.getTransaction(position);
                        boolean included = vxIncluded.get(graph.getTransactionSourceVertex(txId)) && vxIncluded.get(graph.getTransactionDestinationVertex(txId));
                        if (requiresTransactionVisibility) {
                            final float visibility = graph.getFloatValue(txVisibilityAttr, txId);
                            final float layerVisibility = graph.getFloatValue(txLayerVisibilityAttr, txId);
                            if (layerVisibility == 0.0F || visibility <= 1.0F && (visibility > visibilityHigh || visibility < visibilityLow)) {
                                included = false;
                            }
                        }
                        if (included != graph.getBooleanValue(txSelectedAttr, txId)) {
                            selectTransactionsOperation.setValue(txId, included);
                        }
                    }
                }
            }
        } else {
            // Note that txIncluded can only be non-empty if vxIncluded is empty.

            // TODO: figure out if we also want to select the associated vertices.
            if (isAdd) {
                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int txId = txIncluded.nextSetBit(0); txId >= 0; txId = txIncluded.nextSetBit(txId + 1)) {
                        if (!graph.getBooleanValue(txSelectedAttr, txId)) {
                            selectTransactionsOperation.setValue(txId, true);
                        }
                    }
                }
            } else if (isToggle) {
                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int txId = txIncluded.nextSetBit(0); txId >= 0; txId = txIncluded.nextSetBit(txId + 1)) {
                        selectTransactionsOperation.setValue(txId, !graph.getBooleanValue(txSelectedAttr, txId));
                    }
                }
            } else {
                if (txSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < txCount; position++) {
                        final int txId = graph.getTransaction(position);

                        final boolean included = txIncluded.get(txId);
                        if (included != graph.getBooleanValue(txSelectedAttr, txId)) {
                            selectTransactionsOperation.setValue(txId, included);
                        }
                    }
                }

                // Deselect any selected vertices.
                if (vxSelectedAttr != Graph.NOT_FOUND) {
                    for (int position = 0; position < vxCount; position++) {
                        final int vxId = graph.getVertex(position);

                        final boolean selected = graph.getBooleanValue(vxSelectedAttr, vxId);
                        if (selected) {
                            selectVerticesOperation.setValue(vxId, false);
                        }
                    }
                }
            }
        }

        graph.executeGraphOperation(selectVerticesOperation);
        graph.executeGraphOperation(selectTransactionsOperation);
    }

    /**
     * Does the given line segment intersect the given axis-aligned rectangle?
     * <p>
     * We assume that the ends of the line segment are outside of the rectangle.
     *
     *
     * @param x1 x-coordinate of first end of line segment
     * @param y1 y-coordinate of first end line segment
     * @param x2 x-coordinate of second end of line segment
     * @param y2 y-coordinate of second end of line segment
     * @param minX min x-coordinate of rectangle
     * @param minY min y-coordinate of rectangle
     * @param maxX max x-coordinate of rectangle
     * @param maxY max y-coordinate of rectangle
     * @return
     */
    private static boolean lineSegmentIntersectsRectangle(
            final float x1, final float y1,
            final float x2, final float y2,
            final float minX, final float minY, final float maxX, final float maxY) {
        // Completely outside.
        if ((x1 <= minX && x2 <= minX) || (y1 <= minY && y2 <= minY) || (x1 >= maxX && x2 >= maxX) || (y1 >= maxY && y2 >= maxY)) {
            return false;
        }

//        // Either end completely inside.
//        // Not required if it is assumed that endpoints are outside the rectangle.
//        //        if((x1 > minX && x1 < maxX && y1 > minY && y1 < maxY) || (x2 > minX && x2 < maxX && y2 > minY && y2 < maxY))
//        {
//            return true;
//        }
        // Vertical line.
        // We know now that the end-points are outside the rectangle, so we only have to worry
        // that the line is between the sides.
        if (x1 == x2) {
            // At this point minX <= x1 is always true
            return x1 <= maxX;
        }

        // Slope of line segment.
        final float m = (y2 - y1) / (x2 - x1);

        float y = m * (minX - x1) + y1;
        if (y > minY && y < maxY) {
            return true;
        }

        y = m * (maxX - x1) + y1;
        if (y > minY && y < maxY) {
            return true;
        }

        float x = (minY - y1) / m + x1;
        if (x > minX && x < maxX) {
            return true;
        }

        x = (maxY - y1) / m + x1;

        return x > minX && x < maxX;
    }

    /*
     * Takes the x,y,z coordinate of a point in the world and translates it into
     * the equivilant coordinate in the current scene.
     */
    private Vector3f convertWorldToScene(final float x, final float y, final float z, final Vector3f centre, final Matrix33f rotationMatrix, final float cameraDistance) {
        // Convert world coordinates to camera coordinates.
        final Vector3f worldLocation = new Vector3f();
        final Vector3f sceneLocation = new Vector3f();
        worldLocation.set(x, y, z);
        worldLocation.subtract(centre);
        sceneLocation.rotate(worldLocation, rotationMatrix);
        sceneLocation.setZ(sceneLocation.getZ() - cameraDistance);
        return sceneLocation;
    }
}
