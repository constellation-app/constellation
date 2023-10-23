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
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.InternalVisualisation;
import java.util.HashMap;
import java.util.List;
import javafx.scene.Node;

/**
 * Write the current state to the graph.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
public final class AnalyticStateWriterPlugin extends SimpleEditPlugin {

    private final int currentAnalyticQuestionIndex;
    private final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions;
    private final List<List<AnalyticConfigurationPane.SelectableAnalyticPlugin>> activeSelectablePlugins;
    private final AnalyticResult<?> result;
    private final boolean resultsVisible;
    private final boolean categoriesVisible;
    private final AnalyticQuestionDescription<?> currentQuestion;
    private final AnalyticQuestion question;
    private final String activeCategory;
    private final HashMap<GraphVisualisation, Boolean> graphVisualisations;
    private final HashMap<InternalVisualisation, Node> internalVisualisations;

    public AnalyticStateWriterPlugin(final int currentQuestionIndex, final List<AnalyticQuestionDescription<?>> activeQuestions,
            final List<List<AnalyticConfigurationPane.SelectableAnalyticPlugin>> activePlugins, final AnalyticResult<?> result,
            final boolean resultsVisible, final AnalyticQuestionDescription<?> currentQuestion, final AnalyticQuestion question,
            final boolean categoriesVisible, final String activeCategory, final HashMap<GraphVisualisation, Boolean> graphVisualisations,
            final HashMap<InternalVisualisation, Node> internalVisualisations) {
        this.currentAnalyticQuestionIndex = currentQuestionIndex;
        this.activeAnalyticQuestions = activeQuestions;
        this.activeSelectablePlugins = activePlugins;
        this.result = result;
        this.resultsVisible = resultsVisible;
        this.currentQuestion = currentQuestion;
        this.question = question;
        this.categoriesVisible = categoriesVisible;
        this.activeCategory = activeCategory;
        this.graphVisualisations = graphVisualisations;
        this.internalVisualisations = internalVisualisations;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (graph == null) {
            return;
        }

        final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.ensure(graph);
        AnalyticViewState currentState = graph.getObjectValue(stateAttributeId, 0);
        if (currentState == null) {
            currentState = new AnalyticViewState();
        } 
        
        // set all of the values into the current state 
        currentState.setCategoriesPaneVisible(categoriesVisible);
        currentState.setResultsPaneVisible(resultsVisible);
        currentState.setCurrentAnalyticQuestionIndex(currentAnalyticQuestionIndex);
        currentState.setActiveAnalyticQuestions(activeAnalyticQuestions);
        currentState.setActiveSelectablePlugins(activeSelectablePlugins);
        currentState.updateResults(result);
        currentState.setCurrentQuestion(currentQuestion);
        currentState.setQuestion(question);
        currentState.setCategoriesPaneVisible(categoriesVisible);
        currentState.setActiveCategory(activeCategory);
        currentState.setGraphVisualisations(graphVisualisations);
        currentState.setInternalVisualisations(internalVisualisations);

        graph.setObjectValue(stateAttributeId, 0, currentState);
    }

    @Override
    protected boolean isSignificant() {
        return false;
    }

    @Override
    public String getName() {
        return "Analytic View: Write State";
    }
}
