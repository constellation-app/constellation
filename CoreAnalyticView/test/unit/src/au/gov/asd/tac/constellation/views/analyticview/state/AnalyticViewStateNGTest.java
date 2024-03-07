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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTestListener;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.questions.BestConnectsNetworkQuestion;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.translators.GraphVisualisationTranslator;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.SizeVisualisation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test class for AnalyticViewState
 * 
 * @author Delphinus8821
 */
@Listeners(ConstellationTestListener.class)
public class AnalyticViewStateNGTest {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewStateNGTest.class.getName());
    
    public AnalyticViewStateNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setGraphVisualisations method, of class AnalyticViewState.
     */
    @Test
    public void testSetGraphVisualisations() {
        System.out.println("setGraphVisualisations");
        final String translatorName = "Multi-Score -> Size Visualisation";
        final GraphVisualisationTranslator<?, ?> visualisation = AnalyticUtilities.lookupGraphVisualisationTranslator(translatorName); 
        final SizeVisualisation sizeVisualisation = (SizeVisualisation) visualisation.buildControl();
        final Map<GraphVisualisation, Boolean> graphVisualisations = new HashMap<>();
        graphVisualisations.put(sizeVisualisation, true);
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setGraphVisualisations(graphVisualisations);
        assertEquals(graphVisualisations, instance.getGraphVisualisations());
    }


    /**
     * Test of setActiveCategory method, of class AnalyticViewState.
     */
    @Test
    public void testSetActiveCategory() {
        System.out.println("setActiveCategory");
        final String activeCategory = "Centrality";
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setActiveCategory(activeCategory);
        final String newCategory = instance.getActiveCategory();
        assertEquals(activeCategory, newCategory);
    }

    /**
     * Test of setQuestion method, of class AnalyticViewState.
     */
    @Test
    public void testSetQuestion() {
        System.out.println("setQuestion");
        final AnalyticQuestionDescription<?> currentQuestion = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticQuestion question = new AnalyticQuestion(currentQuestion);
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setQuestion(question);
        final AnalyticQuestion<?> result = instance.getQuestion();
        assertEquals(result, question);
    }

    /**
     * Test of setCurrentAnalyticQuestionIndex method, of class AnalyticViewState.
     */
    @Test
    public void testSetCurrentAnalyticQuestionIndex() {
        System.out.println("setCurrentAnalyticQuestionIndex");
        final int currentAnalyticQuestionIndex = 2;
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setCurrentAnalyticQuestionIndex(currentAnalyticQuestionIndex);
        assertEquals(currentAnalyticQuestionIndex, instance.getCurrentAnalyticQuestionIndex());
    }

    /**
     * Test of setActiveSelectablePlugins method, of class AnalyticViewState.
     */
    @Test
    public void testSetActiveSelectablePlugins() {
        System.out.println("setActiveSelectablePlugins");
        final List<List<AnalyticConfigurationPane.SelectableAnalyticPlugin>> activeSelectablePlugins = new ArrayList<>();
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setActiveSelectablePlugins(activeSelectablePlugins);
        assertEquals(activeSelectablePlugins, instance.getActiveSelectablePlugins());
    }

    /**
     * Test of setActiveAnalyticQuestions method, of class AnalyticViewState.
     */
    @Test
    public void testSetActiveAnalyticQuestions() {
        System.out.println("setActiveAnalyticQuestions");
        List<AnalyticQuestionDescription<?>> activeAnalyticQuestions = new ArrayList<>();
        AnalyticViewState instance = new AnalyticViewState();
        instance.setActiveAnalyticQuestions(activeAnalyticQuestions);
        assertEquals(activeAnalyticQuestions, instance.getActiveAnalyticQuestions());
    }


    /**
     * Test of setResultsPaneVisible method, of class AnalyticViewState.
     */
    @Test
    public void testSetResultsPaneVisible() {
        System.out.println("setResultsPaneVisible");
        final boolean resultsVisible = false;
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setResultsPaneVisible(resultsVisible);
        assertEquals(resultsVisible, instance.isResultsPaneVisible());
    }


    /**
     * Test of setCategoriesPaneVisible method, of class AnalyticViewState.
     */
    @Test
    public void testSetCategoriesPaneVisible() {
        System.out.println("setCategoriesPaneVisible");
        final boolean categoriesVisible = false;
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setCategoriesPaneVisible(categoriesVisible);
        assertEquals(categoriesVisible, instance.isCategoriesPaneVisible());
    }

    /**
     * Test of setCurrentQuestion method, of class AnalyticViewState.
     */
    @Test
    public void testSetCurrentQuestion() {
        System.out.println("setCurrentQuestion");
        final AnalyticQuestionDescription<?> currentQuestion = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticViewState instance = new AnalyticViewState();
        instance.setCurrentQuestion(currentQuestion);
        assertEquals(currentQuestion, instance.getCurrentQuestion());
    }

    /**
     * Test of updateResults method, of class AnalyticViewState.
     */
    @Test
    public void testUpdateResults() {
        try {
            System.out.println("updateResults");
            AnalyticResult<?> newResults = null;
            final AnalyticViewState instance = new AnalyticViewState();
            instance.updateResults(newResults);
            final Graph graph = mock(Graph.class);
            
            // Update results with non null value
            final AnalyticConfigurationPane configurationPane = new AnalyticConfigurationPane();
            final AnalyticQuestion<?> currentQuestion = configurationPane.answerCurrentQuestion();
            currentQuestion.answer(graph);
            newResults = currentQuestion.getResult();
            assertEquals(newResults, instance.getResult());
        } catch (final AnalyticException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }
    
}
