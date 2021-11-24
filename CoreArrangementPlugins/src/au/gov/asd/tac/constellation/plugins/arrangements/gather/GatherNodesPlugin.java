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
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
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
 */
@ServiceProvider(service = Plugin.class)
@Messages("GatherNodesPlugin=Gather Selected Nodes")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public final class GatherNodesPlugin extends SimpleEditPlugin {

    public static final String VXID_PARAMETER_ID = PluginParameter.buildId(GatherNodesPlugin.class, "vertex_id");
    public static final String GATHERS_PARAMETER_ID = PluginParameter.buildId(GatherNodesPlugin.class, "gathers");

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int vxId = parameters.getParameters().get(VXID_PARAMETER_ID).getIntegerValue();
        final BitSet gathers = (BitSet) parameters.getParameters().get(GATHERS_PARAMETER_ID).getObjectValue();
        gathers.set(vxId);

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
//        final int nradiusId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(wg);

        final int selectedVertexCount = gathers.cardinality();
        if (selectedVertexCount > 1) {
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

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            final float z = wg.getFloatValue(zId, vxId);

            // If we wanted to be consistent, we'd call the arrange by grid plugin on the selected vertices
            // and rotate the result...
            final int rowLength = (int) Math.ceil(Math.sqrt(selectedVertexCount));

            // Skip the first position: when we get to vxId, we don't change it's position.
            int h = 1;
            int v = 0;
            float scalingFactor = 4; // *maxRadius;
            for (int vertex = gathers.nextSetBit(0); vertex >= 0; vertex = gathers.nextSetBit(vertex + 1)) {
                if (vertex != vxId) {
                    wg.setFloatValue(xId, vertex, x + scalingFactor * (h * left.getX() + v * up.getX()));
                    wg.setFloatValue(yId, vertex, y + scalingFactor * (h * left.getY() + v * up.getY()));
                    wg.setFloatValue(zId, vertex, z + scalingFactor * (h * left.getZ() + v * up.getZ()));

                    if (++h == rowLength) {
                        h = 0;
                        v++;
                    }
                }
            }
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> vxIdParam = IntegerParameterType.build(VXID_PARAMETER_ID);
        vxIdParam.setName("Vertex Id");
        vxIdParam.setDescription("The vertex id to gather around");
        parameters.addParameter(vxIdParam);

        final PluginParameter<ObjectParameterValue> gathersParam = ObjectParameterType.build(GATHERS_PARAMETER_ID);
        gathersParam.setName("Gather Vertex Ids");
        gathersParam.setDescription("A set of vertex ids to gather");
        parameters.addParameter(gathersParam);

        return parameters;
    }
}
