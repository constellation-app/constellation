/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.animation.Animation;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.visual.camera.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.graph.visual.camera.CameraUtilities;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginInfo;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.PluginType;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.visual.camera.BoundingBox;
import au.gov.asd.tac.constellation.visual.camera.Camera;
import java.util.List;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Reset the camera.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {"LOW LEVEL"})
@Messages("ZoomToVerticesPlugin=Zoom to Vertices")
public final class ZoomToVerticesPlugin extends SimpleEditPlugin {

    public static final String VERTICES_PARAMETER_ID = PluginParameter.buildId(ZoomToVerticesPlugin.class, "vertices");
    public static final String VERTEX_PARAMETER_ID = PluginParameter.buildId(ZoomToVerticesPlugin.class, "vertex_id");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> verticesParameter = ObjectParameterType.build(VERTICES_PARAMETER_ID);
        verticesParameter.setName("Vertex Ids");
        verticesParameter.setDescription("An array or list of vertex ids to zoom onto");
        parameters.addParameter(verticesParameter);

        final PluginParameter<IntegerParameterType.IntegerParameterValue> vxParameter = IntegerParameterType.build(VERTEX_PARAMETER_ID);
        vxParameter.setName("Vertex Id");
        vxParameter.setDescription("A vertex id to zoom to");
        vxParameter.setObjectValue(Graph.NOT_FOUND);
        parameters.addParameter(vxParameter);

        return parameters;
    }

    private static int[] verticesParam(final PluginParameters parameters) {
        final int vxId = parameters.getIntegerValue(VERTEX_PARAMETER_ID);
        if (vxId != Graph.NOT_FOUND) {
            return new int[]{vxId};
        } else {
            // The vertices parameter was originally int[], but we want to allow List<Integer>, so handle both.
            final Object vParam = parameters.getObjectValue(VERTICES_PARAMETER_ID);
            final int[] vertices;
            if (vParam.getClass() == int[].class) {
                vertices = (int[]) vParam;
            } else {
                final List<Integer> vertexList = (List<Integer>) vParam;
                vertices = vertexList.stream().mapToInt(i -> i).toArray();
            }

            return vertices;
        }
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int[] vertices = verticesParam(parameters);
        final Camera oldCamera = VisualGraphUtilities.getCamera(graph);
        final BoundingBox box = new BoundingBox();
        final Camera camera = new Camera(oldCamera);
        BoundingBoxUtilities.encompassSpecifiedElements(box, graph, vertices);
        CameraUtilities.zoomToBoundingBox(camera, box);
        Animation.startAnimation(new PanAnimation("Zoom to Vertices", oldCamera, camera, true));
    }
}
