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
package au.gov.asd.tac.constellation.views.analyticview.state;

import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane.SelectableAnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticInfo;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all AnalyticQuestion currently active and current results in the Analytic View.
 *
 * @author cygnus_x-1
 */
public class AnalyticViewState {

    private int currentAnalyticQuestionIndex;
    private final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions;
    private final List<List<SelectableAnalyticPlugin>> activeSelectablePlugins;
    private AnalyticResult<?> result;
    private boolean resultsVisible;

    public AnalyticViewState() {
        this(0, new ArrayList<>(), new ArrayList<>(), null, false);
    }

    public AnalyticViewState(final AnalyticViewState state) {
        this.currentAnalyticQuestionIndex = state.getCurrentAnalyticQuestionIndex();
        this.activeAnalyticQuestions = new ArrayList<>(state.getActiveAnalyticQuestions());
        this.activeSelectablePlugins = new ArrayList<>(state.getActiveSelectablePlugins());
        this.result = state.getResult();
        this.resultsVisible = state.isResultsPaneVisible();
    }

    public AnalyticViewState(final int currentQuestionIndex, final List<AnalyticQuestionDescription<?>> activeQuestions, final List<List<SelectableAnalyticPlugin>> activePlugins, final AnalyticResult<?> result, final boolean resultsVisible) {
        this.currentAnalyticQuestionIndex = currentQuestionIndex;
        this.activeAnalyticQuestions = activeQuestions;
        this.activeSelectablePlugins = activePlugins;
        this.result = result;
        this.resultsVisible = resultsVisible;
    }

    public int getCurrentAnalyticQuestionIndex() {
        return currentAnalyticQuestionIndex;
    }

    public void setCurrentAnalyticQuestionIndex(final int currentAnalyticQuestionIndex) {
        this.currentAnalyticQuestionIndex = currentAnalyticQuestionIndex;
    }

    public List<AnalyticQuestionDescription<?>> getActiveAnalyticQuestions() {
        return activeAnalyticQuestions;
    }

    public AnalyticResult<?> getResult() {
        return result;
    }

    public List<List<SelectableAnalyticPlugin>> getActiveSelectablePlugins() {
        return activeSelectablePlugins;
    }

    public boolean isResultsPaneVisible() {
        return resultsVisible;
    }

    public void addAnalyticQuestion(final AnalyticQuestionDescription<?> question, final List<SelectableAnalyticPlugin> selectablePlugins) {
        if (activeAnalyticQuestions.contains(question)) {
            setCurrentAnalyticQuestionIndex(activeAnalyticQuestions.indexOf(question));
            selectablePlugins.forEach(plugin -> {
                if (!activeSelectablePlugins.get(currentAnalyticQuestionIndex).contains(plugin)) {
                    activeSelectablePlugins.get(currentAnalyticQuestionIndex).add(plugin);
                }
            });
        } else {
            // does not contain question
            activeAnalyticQuestions.add(currentAnalyticQuestionIndex, question);
            activeSelectablePlugins.add(currentAnalyticQuestionIndex, selectablePlugins);
        }
    }

    public void removeAnalyticQuestion(final AnalyticQuestionDescription<?> question) {
        activeSelectablePlugins.remove(activeAnalyticQuestions.indexOf(question));
        activeAnalyticQuestions.remove(question);
    }

    public void clearAnalyticQuestions() {
        activeAnalyticQuestions.clear();
        activeSelectablePlugins.clear();
    }

    /**
     * Update the results values and record whether the results pane is currently visible
     * 
     * @param newResults
     */
    public void updateResults(final AnalyticResult<?> newResults) {
        result = newResults;
        resultsVisible = result != null;
    }

    /**
     * Check the currently selected Question index of plugins for other plugins matching the selected category
     *
     * @param currentCategory the currently selected plugin category to remove from
     */
    public void removePluginsMatchingCategory(final String currentCategory) {
        if (!activeSelectablePlugins.isEmpty()) {
            activeSelectablePlugins.get(currentAnalyticQuestionIndex).removeIf(plugin
                    -> (plugin.getPlugin().getClass().getAnnotation(AnalyticInfo.class)
                            .analyticCategory().equals(currentCategory))
            );
        }
    }
}
