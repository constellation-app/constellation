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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewPane;

/**
 * Read the current state from the graph.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
public final class AnalyticStateReaderPlugin extends SimpleReadPlugin {

    private final AnalyticViewPane pane;

    public AnalyticStateReaderPlugin(final AnalyticViewPane pane) {
        this.pane = pane;
    }

    @Override
    public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (graph == null || pane == null) {
            return;
        }

        final int analyticViewStateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.get(graph);
        if (analyticViewStateAttributeId == Graph.NOT_FOUND) {
            return;
        }

        final AnalyticViewState currentState = graph.getObjectValue(analyticViewStateAttributeId, 0);
        if (currentState == null) {
            return;
        }
        
        // update pane with current question/category/results/effects
        pane.updateView(currentState);
    }

    @Override
    public String getName() {
        return "Analytic View: Read State";
    }
}
