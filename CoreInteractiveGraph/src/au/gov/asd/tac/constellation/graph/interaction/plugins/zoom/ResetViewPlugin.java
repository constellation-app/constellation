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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Reset the camera.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ResetViewPlugin=Reset View")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
public final class ResetViewPlugin extends SimpleEditPlugin {

    public static final String AXIS_PARAMETER_ID = PluginParameter.buildId(ResetViewPlugin.class, "axis");
    public static final String NEGATIVE_PARAMETER_ID = PluginParameter.buildId(ResetViewPlugin.class, "negative");
    public static final String SIGNIFICANT_PARAMETER_ID = PluginParameter.buildId(ResetViewPlugin.class, "significant");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> axisParam = StringParameterType.build(AXIS_PARAMETER_ID);
        axisParam.setName("Axis");
        axisParam.setDescription("The x,y or z axis to focus on");
        axisParam.setStringValue("z");
        parameters.addParameter(axisParam);

        final PluginParameter<BooleanParameterValue> negativeParam = BooleanParameterType.build(NEGATIVE_PARAMETER_ID);
        negativeParam.setName("Negative");
        negativeParam.setDescription("True to reverse direction, default is False");
        negativeParam.setBooleanValue(false);
        parameters.addParameter(negativeParam);

        final PluginParameter<BooleanParameterValue> significantParam = BooleanParameterType.build(SIGNIFICANT_PARAMETER_ID);
        significantParam.setName("Significant");
        significantParam.setDescription("Significant animations will make significant edits on the graph, meaning that their results can be undone/redone atomically. Default is False.");
        significantParam.setBooleanValue(false);
        parameters.addParameter(significantParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        // Get a copy of the graph's curent camera
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
        if (cameraAttribute != Graph.NOT_FOUND) {
            final Camera oldCamera = graph.getObjectValue(cameraAttribute, 0);
            final Camera camera = new Camera(oldCamera);
            final BoundingBox boundingBox = new BoundingBox();
            BoundingBoxUtilities.recalculateFromGraph(boundingBox, graph, false);

            // Refocus the copied camera appropriately.
            final String axis = parameters.getStringValue(AXIS_PARAMETER_ID);
            final boolean negative = parameters.getBooleanValue(NEGATIVE_PARAMETER_ID);
            switch (axis.toLowerCase()) {
                case "x" -> CameraUtilities.refocusOnXAxis(camera, boundingBox, negative);
                case "y"  -> CameraUtilities.refocusOnYAxis(camera, boundingBox, negative);
                default  -> CameraUtilities.refocusOnZAxis(camera, boundingBox, negative);
            }

            // add an animation to the refocused camera so that it pans from the old position.
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null && activeGraph.getId().equals(graph.getId())) {
                // Only do the camera animation if the edited graph is currently active
                AnimationUtilities.startAnimation(new PanAnimation("Reset View", oldCamera, camera, parameters.getBooleanValue(SIGNIFICANT_PARAMETER_ID)), activeGraph.getId());
            } else {
                // Skip the animation, just set the new camera position
                graph.setObjectValue(cameraAttribute, 0, camera);
            }
        }
    }
}
