/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.state;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import java.util.Map;

/**
 * Plugin to deactivate the changes done to the graph by the analytic view
 * when the view is closed.
 * 
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
public class AnalyticDeactivateStateChangesPlugin extends SimpleEditPlugin {

    
    public AnalyticDeactivateStateChangesPlugin() {
        // Empty as no parameters are required for this plugin to run
    }
    
    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (graph == null) {
            return;
        }

        final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.get(graph);
        if (stateAttributeId == Graph.NOT_FOUND) {
            return;
        }

        final AnalyticViewState currentState = graph.getObjectValue(stateAttributeId, 0);
        if (currentState == null) {
            return;
        }
        
        final Map<GraphVisualisation, Boolean> graphVisualisations = currentState.getGraphVisualisations();
        if (!graphVisualisations.isEmpty()) {
            graphVisualisations.entrySet().forEach(node -> {
                node.getKey().deactivate(node.getValue());
                node.setValue(false);
            });
            currentState.setGraphVisualisations(graphVisualisations);
            graph.setObjectValue(stateAttributeId, 0, currentState);
        }        
        
    }
    
    @Override
    protected boolean isSignificant() {
        return false;
    }
    
    @Override
    public String getName() {
        return "Analytic Deactivate State Changes";
    }
    
}
