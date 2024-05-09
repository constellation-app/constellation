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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
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
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author altair
 * @author antares
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("CloseGraphPlugin=Close Graph")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.LOW_LEVEL})
public class CloseGraphPlugin extends SimplePlugin {

    public static final String GRAPH_PARAMETER_ID = PluginParameter.buildId(CloseGraphPlugin.class, "graphId");
    public static final String FORCED_PARAMETER_ID = PluginParameter.buildId(CloseGraphPlugin.class, "forced");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParameter = StringParameterType.build(GRAPH_PARAMETER_ID);
        graphIdParameter.setName("graphId");
        graphIdParameter.setDescription("The Id of the graph");
        parameters.addParameter(graphIdParameter);

        final PluginParameter<BooleanParameterValue> forcedParameter = BooleanParameterType.build(FORCED_PARAMETER_ID);
        forcedParameter.setName("forced");
        forcedParameter.setDescription("Whether the graph will be force closed or not");
        forcedParameter.setBooleanValue(false);
        parameters.addParameter(forcedParameter);

        return parameters;
    }

    @Override
    protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph g = graphs.getAllGraphs().get(parameters.getStringValue(GRAPH_PARAMETER_ID));
        final GraphNode gn = GraphNode.getGraphNode(g);
        final boolean forced = parameters.getBooleanValue(FORCED_PARAMETER_ID);

        if (forced) {
            SwingUtilities.invokeLater(((VisualGraphTopComponent) gn.getTopComponent())::forceClose);
        } else {
            SwingUtilities.invokeLater(gn.getTopComponent()::close);
        }
    }

}
