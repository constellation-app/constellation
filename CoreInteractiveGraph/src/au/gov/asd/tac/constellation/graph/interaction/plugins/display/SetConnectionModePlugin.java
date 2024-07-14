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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Set Connection Mode
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("SetConnectionModePlugin=Set Connection Mode")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public class SetConnectionModePlugin extends SimpleEditPlugin {

    public static final String CONNECTION_MODE_PARAMETER_ID = PluginParameter.buildId(SetConnectionModePlugin.class, "mode");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> modeParam = ObjectParameterType.build(CONNECTION_MODE_PARAMETER_ID);
        modeParam.setName("Connection Mode");
        modeParam.setDescription("The mode in which to display connections on the graph (transaction, edge or link)");
        parameters.addParameter(modeParam);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final ConnectionMode mode = (ConnectionMode) parameters.getObjectValue(CONNECTION_MODE_PARAMETER_ID);

        final int connectionModeAttrId = VisualConcept.GraphAttribute.CONNECTION_MODE.get(graph);
        if (connectionModeAttrId != Graph.NOT_FOUND) {
            graph.setObjectValue(VisualConcept.GraphAttribute.CONNECTION_MODE.get(graph), 0, mode);
        }
    }
}
