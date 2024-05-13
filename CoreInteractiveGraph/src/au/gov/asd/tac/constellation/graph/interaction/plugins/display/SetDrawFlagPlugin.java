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
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Set the draw flag explicitly onto the graph.
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("SetDrawFlagPlugin=Set Draw Flag")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class SetDrawFlagPlugin extends SimpleEditPlugin {

    public static final String FLAG_PARAMETER_ID = PluginParameter.buildId(SetDrawFlagPlugin.class, "flag");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterType.IntegerParameterValue> flagParam = IntegerParameterType.build(FLAG_PARAMETER_ID);
        flagParam.setName("Draw Flag");
        flagParam.setDescription("The graph elements to draw");
        parameters.addParameter(flagParam);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int drawFlagsAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.get(graph);
        if (drawFlagsAttribute != Graph.NOT_FOUND) {
            final int flag = parameters.getIntegerValue(FLAG_PARAMETER_ID);
            graph.setIntValue(drawFlagsAttribute, 0, flag);
        }
    }
}
