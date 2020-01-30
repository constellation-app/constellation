/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all AnalyticQuestion currently active in the Analytic View.
 *
 * @author cygnus_x-1
 */
public class AnalyticViewState {

    private int currentAnalyticQuestionIndex;
    private final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions;
    private final List<List<SelectableAnalyticPlugin>> activeSelectablePlugins;

    public AnalyticViewState() {
        this(0, new ArrayList<>(), new ArrayList<>());
    }

    public AnalyticViewState(final AnalyticViewState state) {
        this.currentAnalyticQuestionIndex = state.getCurrentAnalyticQuestionIndex();
        this.activeAnalyticQuestions = new ArrayList<>(state.getActiveAnalyticQuestions());
        this.activeSelectablePlugins = new ArrayList<>(state.getActiveSelectablePlugins());
    }

    public AnalyticViewState(final int currentQuestionIndex, final List<AnalyticQuestionDescription<?>> activeQuestions, final List<List<SelectableAnalyticPlugin>> activePlugins) {
        this.currentAnalyticQuestionIndex = currentQuestionIndex;
        this.activeAnalyticQuestions = activeQuestions;
        this.activeSelectablePlugins = activePlugins;
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

    public List<List<SelectableAnalyticPlugin>> getActiveSelectablePlugins() {
        return activeSelectablePlugins;
    }

    public void addAnalyticQuestion(final AnalyticQuestionDescription<?> question, final List<SelectableAnalyticPlugin> selectablePlugins) {
        if(!activeAnalyticQuestions.isEmpty()){
            if(question != activeAnalyticQuestions.get(currentAnalyticQuestionIndex)){
                // When that question isn't within the list, add it to the next index 
                // and increment the Analytic Question Index.
                activeAnalyticQuestions.add(currentAnalyticQuestionIndex + 1, question);
                activeSelectablePlugins.get(currentAnalyticQuestionIndex + 1).addAll(selectablePlugins);
                setCurrentAnalyticQuestionIndex(currentAnalyticQuestionIndex + 1);
            }else{
                activeSelectablePlugins.get(currentAnalyticQuestionIndex).addAll(selectablePlugins);
            }
        }else{
            // is empty
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
     * Check the currently selected Question index of plugins for other plugins 
     * matching the selected category
     * @param currentCategory the currently selected plugin category to remove
     * from
     */
    public void removePluginsMatchingCategory(String currentCategory) {
        if(!activeSelectablePlugins.isEmpty()){
            activeSelectablePlugins.get(currentAnalyticQuestionIndex).removeIf(plugin -> (
                plugin.getPlugin().getClass().getAnnotation(AnalyticInfo.class).analyticCategory().equals(currentCategory)
                )
            );
        }
    }
}
