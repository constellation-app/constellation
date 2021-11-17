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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Set the camera low and high visibility range.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
@Messages("SetCameraVisibilityRange=Set Camera Visibility Range")
public final class SetCameraVisibilityRange extends SimpleEditPlugin {

    public static final String VISIBILITY_LOW_ID = PluginParameter.buildId(SetCameraVisibilityRange.class, "visibilityLow");
    public static final String VISIBILITY_HIGH_ID = PluginParameter.buildId(SetCameraVisibilityRange.class, "visibilityHigh");

    @Override
    public String getDescription() {
        return "Sets the camera low and high visibility range.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> visibilityLowParam = FloatParameterType.build(VISIBILITY_LOW_ID);
        visibilityLowParam.setName("visibilityLow");
        visibilityLowParam.setDescription("Low boundary of visibility");
        visibilityLowParam.setFloatValue(0f);
        parameters.addParameter(visibilityLowParam);

        final PluginParameter<FloatParameterValue> visibilityHighParam = FloatParameterType.build(VISIBILITY_HIGH_ID);
        visibilityHighParam.setName("visibilityHigh");
        visibilityHighParam.setDescription("High boundary of visibility");
        visibilityHighParam.setFloatValue(1f);
        parameters.addParameter(visibilityHighParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final float visibilityLow = parameters.getFloatValue(VISIBILITY_LOW_ID);
        final float visibilityHigh = parameters.getFloatValue(VISIBILITY_HIGH_ID);

        // Get a copy of the graph's current camera.
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
        if (cameraAttribute != Graph.NOT_FOUND) {
            final Camera oldCamera = graph.getObjectValue(cameraAttribute, 0);
            final Camera camera = new Camera(oldCamera);

            camera.setVisibilityLow(visibilityLow);
            camera.setVisibilityHigh(visibilityHigh);
            graph.setObjectValue(cameraAttribute, 0, camera);
        }
    }
}
