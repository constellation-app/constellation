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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.animation.Animation;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rotate the camera.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("RotateCameraPlugin=Rotate Camera")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
public final class RotateCameraPlugin extends SimpleEditPlugin {

    public static final String X_PARAMETER_ID = PluginParameter.buildId(RotateCameraPlugin.class, "x");
    public static final String Y_PARAMETER_ID = PluginParameter.buildId(RotateCameraPlugin.class, "y");
    public static final String Z_PARAMETER_ID = PluginParameter.buildId(RotateCameraPlugin.class, "z");
    public static final String ANIMATE_PARAMETER_ID = PluginParameter.buildId(RotateCameraPlugin.class, "animate");

    @Override
    public String getDescription() {
        return "Spins the camera.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> xaxisParam = FloatParameterType.build(X_PARAMETER_ID);
        xaxisParam.setName("xAxis");
        xaxisParam.setDescription("Rotation in degrees around the x axis");
        xaxisParam.setFloatValue(0f);
        parameters.addParameter(xaxisParam);

        final PluginParameter<FloatParameterValue> yaxisParam = FloatParameterType.build(Y_PARAMETER_ID);
        yaxisParam.setName("yAxis");
        yaxisParam.setDescription("Rotation in degrees around the y axis");
        yaxisParam.setFloatValue(0f);
        parameters.addParameter(yaxisParam);

        final PluginParameter<FloatParameterValue> zaxisParam = FloatParameterType.build(Z_PARAMETER_ID);
        zaxisParam.setName("zAxis");
        zaxisParam.setDescription("Rotation in degrees around the z axis");
        zaxisParam.setFloatValue(0f);
        parameters.addParameter(zaxisParam);

        final PluginParameter<BooleanParameterValue> animateParam = BooleanParameterType.build(ANIMATE_PARAMETER_ID);
        animateParam.setName("animate");
        animateParam.setDescription("Animate the rotation asynchronously");
        animateParam.setBooleanValue(false);
        parameters.addParameter(animateParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final float xrot = parameters.getFloatValue(X_PARAMETER_ID);
        final float yrot = parameters.getFloatValue(Y_PARAMETER_ID);
        final float zrot = parameters.getFloatValue(Z_PARAMETER_ID);
        final boolean animate = parameters.getBooleanValue(ANIMATE_PARAMETER_ID);

        // Get a copy of the graph's current camera.
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
        if (cameraAttribute != Graph.NOT_FOUND) {
            final Camera oldCamera = graph.getObjectValue(cameraAttribute, 0);
            final Camera camera = new Camera(oldCamera);

            CameraUtilities.rotate(camera, xrot, yrot, zrot);

            if (animate) {
                Animation.startAnimation(new PanAnimation("Rotate camera", oldCamera, camera, true));
            } else {
                // Don't do an animation; we don't want to be asynchronous.
                // Just set the camera value back on the graph.
                graph.setObjectValue(cameraAttribute, 0, camera);
            }
        }
    }
}
