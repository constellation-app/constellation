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
package au.gov.asd.tac.constellation.plugins.arrangements.gather;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Gather selected nodes into one place so they can be farmed.
 *
 * @author algol
 *
 * @author alkaid
 *
 */
@ServiceProvider(service = Plugin.class)
@Messages("GatherNodesInGraphPlugin=Gather Selected Nodes Plugin")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public final class GatherNodesInGraphPlugin extends SimpleEditPlugin {

    public static final String XYZ_PARAMETER_ID = PluginParameter.buildId(GatherNodesInGraphPlugin.class, "xyz");
    public static final String GATHERS_PARAMETER_ID = PluginParameter.buildId(GatherNodesInGraphPlugin.class, "gathers");

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final Vector3f xyzp = (Vector3f) parameters.getParameters().get(XYZ_PARAMETER_ID).getObjectValue();
        final BitSet gathers = (BitSet) parameters.getParameters().get(GATHERS_PARAMETER_ID).getObjectValue();

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(wg);

        final int selectedVertexCount = gathers.cardinality();
        if (selectedVertexCount >= 1) {
            // This is where we want to rotate to: relative to the eye->centre direction.
            final Camera visualState = wg.getObjectValue(cameraAttribute, 0);
            final Vector3f xyz = Vector3f.subtract(visualState.lookAtCentre, visualState.lookAtEye);

            // Create a rotation matrix that will rotate the positions we're about to create.
            final Frame frame = new Frame(xyz, new Vector3f(0, 0, 0), visualState.lookAtUp);
            final Matrix44f rm = new Matrix44f();
            frame.getMatrix(rm, true);
            final Matrix33f rotationMatrix = new Matrix33f();
            rm.getRotationMatrix(rotationMatrix);

            // We want the grid to start at the top left and grow down and right.
            // Choose our up and left vectors accordingly.
            final Vector3f up = new Vector3f();
            up.rotate(new Vector3f(0, -1, 0), rotationMatrix);
            final Vector3f left = new Vector3f();
            left.rotate(new Vector3f(-1, 0, 0), rotationMatrix);

            // If we wanted to be consistent, we'd call the arrange by grid plugin on the selected vertices
            // and rotate the result...
            final int rowLength = (int) Math.ceil(Math.sqrt(selectedVertexCount));

            float x = xyzp.getX();
            float y = xyzp.getY();
            float z = xyzp.getZ();

            int h = 0;
            int v = 0;
            float scalingFactor = 4; // *maxRadius;

            for (int vxId = gathers.nextSetBit(0); vxId >= 0; vxId = gathers.nextSetBit(vxId + 1)) {
                if (h == 0 && v == 0) {

                    //This gathers the selected nodes at the distance the first selected node as measured away from the camera/eye.
                    //It does this by using a right hand triangle (hypotenuse = ray to click, adjacent = view from eye to centre,
                    //opposite = pane perpendicular to eye) and the approriate triangle properties
                    //
                    //Unit vector from eye to click point
                    final Vector3f ray = Vector3f.subtract(xyzp, visualState.lookAtEye);
                    ray.normalize();

                    //Unit vector from eye to centre
                    final Vector3f adjacentUnit = Vector3f.subtract(visualState.lookAtCentre, visualState.lookAtEye);
                    adjacentUnit.normalize();

                    //Vector from eye to node already on graph
                    final Vector3f point = new Vector3f(wg.getFloatValue(xId, vxId), wg.getFloatValue(yId, vxId), wg.getFloatValue(zId, vxId));
                    point.subtract(visualState.lookAtEye);

                    //Determining the distance along the unit vector to the centre that the node will be
                    final float adjacentLen = Vector3f.dotProduct(adjacentUnit, point);

                    //Determining the length of the hypotenuse of the right hand triangle
                    final float cosAngleBetweenVectors = (float) Math.cos(Vector3f.angleBetweenVectors(ray, adjacentUnit));

                    //cosAngleBetweenVectors should never equals 0 but adding the check just in case
                    //If it does equal 0 we will skip this section and have x,y,z equal the "click point" which was assigned when they were defined
                    if (cosAngleBetweenVectors != 0.0) {
                        final float rayScalar = adjacentLen / cosAngleBetweenVectors;
                        ray.scale(rayScalar);
                        final Vector3f destination = Vector3f.add(visualState.lookAtEye, ray);
                        x = destination.getX();
                        y = destination.getY();
                        z = destination.getZ();
                    }

                }

                wg.setFloatValue(xId, vxId, x + scalingFactor * (h * left.getX() + v * up.getX()));
                wg.setFloatValue(yId, vxId, y + scalingFactor * (h * left.getY() + v * up.getY()));
                wg.setFloatValue(zId, vxId, z + scalingFactor * (h * left.getZ() + v * up.getZ()));

                if (++h == rowLength) {
                    h = 0;
                    v++;
                }
            }
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> xParam = ObjectParameterType.build(XYZ_PARAMETER_ID);
        xParam.setName("XYZ Position");
        xParam.setDescription("The x,y,z position to gather around");
        parameters.addParameter(xParam);

        final PluginParameter<ObjectParameterValue> gathersParam = ObjectParameterType.build(GATHERS_PARAMETER_ID);
        gathersParam.setName("Gather Vertex Ids");
        gathersParam.setDescription("A set of vertex ids to gather");
        parameters.addParameter(gathersParam);

        return parameters;
    }
}
