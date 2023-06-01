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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import java.util.List;

/**
 * Write the given AnalyticViewState to the active graph.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public final class AnalyticViewStateWriter extends SimpleEditPlugin {

    private final AnalyticQuestionDescription<?> question;
    private final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> plugins;
    private final AnalyticResult results;
    private final boolean resultsVisible;

    public AnalyticViewStateWriter(final AnalyticQuestionDescription<?> question, final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> plugins, final AnalyticResult<?> results, final boolean resultsVisible) {
        this.question = question;
        this.plugins = plugins;
        this.results = results;
        this.resultsVisible = resultsVisible;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
        final AnalyticViewState newState = graph.getObjectValue(stateAttributeId, 0) == null ? new AnalyticViewState()
                : new AnalyticViewState(graph.getObjectValue(stateAttributeId, 0));
        newState.addAnalyticQuestion(question, plugins);
        newState.updateResults(results);
        graph.setObjectValue(stateAttributeId, 0, newState);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Analytic View: Update State";
    }
}
