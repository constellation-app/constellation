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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * A plug-in to alter the size and opacity of blazes on the graph.
 *
 * @author sirius
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
@NbBundle.Messages("UpdateBlazeSizeOpacityPlugin=Blazes: Update Size and Opacity")
public class UpdateBlazeSizeOpacityPlugin extends SimpleEditPlugin {

    public static final String SIZE_PARAMETER_ID = PluginParameter.buildId(UpdateBlazeSizeOpacityPlugin.class, "size");
    public static final String OPACITY_PARAMETER_ID = PluginParameter.buildId(UpdateBlazeSizeOpacityPlugin.class, "opacity");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> sizeParameter = FloatParameterType.build(SIZE_PARAMETER_ID);
        sizeParameter.setName("Size");
        sizeParameter.setDescription("The size of the blaze");
        FloatParameterType.setMinimum(sizeParameter, 0F);
        FloatParameterType.setMaximum(sizeParameter, 1F);
        parameters.addParameter(sizeParameter);

        final PluginParameter<FloatParameterValue> opacityParameter = FloatParameterType.build(OPACITY_PARAMETER_ID);
        opacityParameter.setName("Opacity");
        opacityParameter.setDescription("The opacity of the blaze");
        FloatParameterType.setMinimum(opacityParameter, 0F);
        FloatParameterType.setMaximum(opacityParameter, 1F);
        parameters.addParameter(opacityParameter);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int blazeSizeAttributeId = VisualConcept.GraphAttribute.BLAZE_SIZE.get(graph);
        if (blazeSizeAttributeId != Graph.NOT_FOUND) {
            final float blazeSize = graph.getFloatValue(blazeSizeAttributeId, 0);
            if (blazeSize != parameters.getParameters().get(SIZE_PARAMETER_ID).getFloatValue()) {
                graph.setFloatValue(blazeSizeAttributeId, 0, parameters.getParameters().get(SIZE_PARAMETER_ID).getFloatValue());
            }
        }
        final int blazeOpacityAttributeId = VisualConcept.GraphAttribute.BLAZE_OPACITY.get(graph);
        if (blazeOpacityAttributeId != Graph.NOT_FOUND) {
            final float blazeOpacity = graph.getFloatValue(blazeOpacityAttributeId, 0);
            if (blazeOpacity != parameters.getParameters().get(OPACITY_PARAMETER_ID).getFloatValue()) {
                graph.setFloatValue(blazeOpacityAttributeId, 0, parameters.getParameters().get(OPACITY_PARAMETER_ID).getFloatValue());
            }
        }
    }
}
