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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

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
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Toggle the draw flag via a plugin.
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ToggleDrawFlagPlugin=Toggle Draw Flag")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.LOW_LEVEL})
public class ToggleDrawFlagPlugin extends SimpleEditPlugin {

    public static final String FLAG_PARAMETER_ID = PluginParameter.buildId(ToggleDrawFlagPlugin.class, "flag");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> flagParam = IntegerParameterType.build(FLAG_PARAMETER_ID);
        flagParam.setName("Draw Flag");
        flagParam.setDescription("The draw flag to toggle");
        parameters.addParameter(flagParam);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int flag = parameters.getIntegerValue(FLAG_PARAMETER_ID);

        final int drawFlagsAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.get(graph);

        if (drawFlagsAttribute != Graph.NOT_FOUND) {
            int drawFlags = graph.getIntValue(drawFlagsAttribute, 0);
            drawFlags ^= flag;
            graph.setIntValue(drawFlagsAttribute, 0, drawFlags);
        }
    }

}
