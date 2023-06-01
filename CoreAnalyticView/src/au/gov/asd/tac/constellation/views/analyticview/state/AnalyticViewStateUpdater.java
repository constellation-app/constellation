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
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewPane;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticInfo;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

/**
 * Update the display by reading and writing to/from the state attribute.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public final class AnalyticViewStateUpdater extends SimpleEditPlugin {

    private final AnalyticConfigurationPane analyticConfigurationPane;
    private final AnalyticViewPane analyticViewPane;
    private final boolean pluginWasSelected;
    private final AnalyticResult results;
    private final boolean resultsVisible;

    public AnalyticViewStateUpdater(final AnalyticViewPane analyticViewPane, final AnalyticConfigurationPane analyticConfigurationPane, final boolean pluginWasSelected, final AnalyticResult<?> results, final boolean resultsVisible) {
        this.analyticConfigurationPane = analyticConfigurationPane;
        this.pluginWasSelected = pluginWasSelected;
        this.analyticViewPane = analyticViewPane;
        this.results = results;
        this.resultsVisible = resultsVisible;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String currentCategory = analyticConfigurationPane.getCategoryList().getSelectionModel().getSelectedItem();
        final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);

        // Make a copy in case the state on the graph is currently being modified.
        final AnalyticViewState currentState = graph.getObjectValue(stateAttributeId, 0) == null
                ? new AnalyticViewState()
                : new AnalyticViewState(graph.getObjectValue(stateAttributeId, 0));

        if (pluginWasSelected) {
            // remove all plugins matching category
            currentState.removePluginsMatchingCategory(currentCategory);
            // grab all plugins from currently selected category
            final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> checkedPlugins = new ArrayList<>();
            // adding items to checkedPlugins array when they are selected
            analyticConfigurationPane.getPluginList().getItems().forEach(selectablePlugin -> {
                if (selectablePlugin.isSelected()) {
                    checkedPlugins.add(selectablePlugin);
                }
            });
            if (!checkedPlugins.isEmpty()) {
                currentState.addAnalyticQuestion(analyticConfigurationPane.getCurrentQuestion(), checkedPlugins);
            }
            analyticViewPane.saveState();
        }

        // Utilized for Question pane - TODO: when multiple tabs + saving of
        // questions is supported, link this currentquestion variable with
        // the saved/loaded question
        Platform.runLater(() -> analyticConfigurationPane.currentQuestion = currentState.getActiveAnalyticQuestions().isEmpty() ? null
                : currentState.getActiveAnalyticQuestions().get(currentState.getCurrentAnalyticQuestionIndex()));

        if (!currentState.getActiveSelectablePlugins().isEmpty()) {
            for (final AnalyticConfigurationPane.SelectableAnalyticPlugin selectedPlugin : currentState.getActiveSelectablePlugins().get(currentState.getCurrentAnalyticQuestionIndex())) {
                if (currentCategory.equals(selectedPlugin.getPlugin().getClass().getAnnotation(AnalyticInfo.class).analyticCategory())) {
                    Platform.runLater(() -> {
                        AnalyticConfigurationPane.setSuppressedFlag(true);
                        selectedPlugin.setSelected(true);
                        AnalyticConfigurationPane.setSuppressedFlag(false);
                    });
                }
            }
        }

        // Make the results pane visible if there are current results
        if (resultsVisible) {
            currentState.updateResults(results);
          // analyticViewPane.showResults();
        }
    }

    @Override
    protected boolean isSignificant() {
        return false;
    }

    @Override
    public String getName() {
        return "Analytic View: Update State";
    }
}
