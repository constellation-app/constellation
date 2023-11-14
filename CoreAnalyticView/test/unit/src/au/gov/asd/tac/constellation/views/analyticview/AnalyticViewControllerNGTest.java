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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.questions.BestConnectsNetworkQuestion;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewConcept;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewState;
import au.gov.asd.tac.constellation.views.analyticview.translators.GraphVisualisationTranslator;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.SizeVisualisation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test class for AnalyticViewController
 * 
 * @author Delphinus8821
 */
public class AnalyticViewControllerNGTest {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewControllerNGTest.class.getName());
    private final AnalyticViewTopComponent topComponent = new AnalyticViewTopComponent();
    private final Graph graph = mock(Graph.class);
    private final GraphManager graphManager = spy(GraphManager.class);
    
    
    public AnalyticViewControllerNGTest() {
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
     * Test of init method, of class AnalyticViewController.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        final AnalyticViewTopComponent parent = topComponent;
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        final AnalyticViewController result = instance.init(parent);
        assertEquals(result, instance);

    }

    /**
     * Test of getParent method, of class AnalyticViewController.
     */
    @Test
    public void testGetParent() {
        System.out.println("getParent");
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        final AnalyticViewTopComponent result = instance.getParent();
        assertEquals(result, topComponent);

    }

    /**
     * Test of setActiveCategory method, of class AnalyticViewController.
     */
    @Test
    public void testSetActiveCategory() {
        System.out.println("setActiveCategory");
        final String activeCategory = "Centrality";
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.setActiveCategory(activeCategory);
        final String newCategory = instance.getActiveCategory();
        assertEquals(activeCategory, newCategory);
    }

    /**
     * Test of setCurrentQuestion method, of class AnalyticViewController.
     */
    @Test
    public void testSetCurrentQuestion() {
        System.out.println("setCurrentQuestion");
        final AnalyticQuestionDescription<?> currentQuestion = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.setCurrentQuestion(currentQuestion);
        final AnalyticQuestionDescription<?> result = instance.getCurrentQuestion();
        assertEquals(result, currentQuestion);
    }

    /**
     * Test of setQuestion method, of class AnalyticViewController.
     */
    @Test
    public void testSetQuestion() {
        System.out.println("setQuestion");
        final AnalyticQuestionDescription<?> currentQuestion = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticQuestion question = new AnalyticQuestion(currentQuestion);
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.setQuestion(question);
        final AnalyticQuestion result = instance.getQuestion();
        assertEquals(question, result);
    }

    /**
     * Test of setCategoriesVisible method, of class AnalyticViewController.
     */
    @Test
    public void testSetCategoriesVisible() {
        System.out.println("setCategoriesVisible");
        final boolean categoriesVisible = true;
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.setCategoriesVisible(categoriesVisible);
        final boolean result = instance.isCategoriesVisible();
        assertEquals(result, categoriesVisible);
    }

    /**
     * Test of setGraphVisualisations method, of class AnalyticViewController.
     */
    @Test
    public void testSetGraphVisualisations() {
        System.out.println("setGraphVisualisations");
        final String translatorName = "Multi-Score -> Size Visualisation";
        final GraphVisualisationTranslator<?, ?> visualisation = AnalyticUtilities.lookupGraphVisualisationTranslator(translatorName); 
        final SizeVisualisation sizeVisualisation = (SizeVisualisation) visualisation.buildControl();
        final Map<GraphVisualisation, Boolean> graphVisualisations = new HashMap<>();
        
        graphVisualisations.put(sizeVisualisation, true);
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.setGraphVisualisations(graphVisualisations);
        final Map<GraphVisualisation, Boolean> result = instance.getGraphVisualisations();
        assertEquals(graphVisualisations, result);
    }

    /**
     * Test of updateGraphVisualisations method, of class AnalyticViewController.
     */
    @Test
    public void testUpdateGraphVisualisations() {
        System.out.println("updateGraphVisualisations");
        final String translatorName = "Multi-Score -> Size Visualisation";
        final GraphVisualisationTranslator<?, ?> visualisation = AnalyticUtilities.lookupGraphVisualisationTranslator(translatorName); 
        final SizeVisualisation sizeVisualisation = (SizeVisualisation) visualisation.buildControl();
        boolean activated = false;
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.updateGraphVisualisations(sizeVisualisation, activated);
        Map<GraphVisualisation, Boolean> result = instance.getGraphVisualisations();
        assertFalse(result.get(sizeVisualisation));
        
        // Update the graph visualisation to be true
        activated = true;
        instance.updateGraphVisualisations(sizeVisualisation, activated);
        result = instance.getGraphVisualisations();
        assertTrue(result.get(sizeVisualisation));
    }

    /**
     * Test of updateResults method, of class AnalyticViewController.
     */
    @Test
    public void testUpdateResults() {
        try {
            System.out.println("updateResults");
            AnalyticResult<?> newResults = null;
            final AnalyticViewController instance = AnalyticViewController.getDefault();
            instance.updateResults(newResults);
            assertFalse(instance.isResultsVisible());
            
            // Update results with non null value
            final AnalyticConfigurationPane configurationPane = new AnalyticConfigurationPane();
            final AnalyticQuestion<?> currentQuestion = configurationPane.answerCurrentQuestion();
            currentQuestion.answer(graph);
            newResults = currentQuestion.getResult();
            instance.updateResults(newResults);
            assertTrue(instance.isResultsVisible());
            assertEquals(newResults, instance.getResult());
            
        } catch (final AnalyticException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }

    /**
     * Test of removeAnalyticQuestion method, of class AnalyticViewController.
     */
    @Test
    public void testRemoveAnalyticQuestion() {
        System.out.println("removeAnalyticQuestion");
        final AnalyticQuestionDescription<?> question = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        instance.addAnalyticQuestion(question, selectablePlugins);
        List<AnalyticQuestionDescription<?>> questions = instance.getActiveAnalyticQuestions();
        assertFalse(questions.isEmpty());
        
        // Remove a question 
        instance.removeAnalyticQuestion(question);
        questions = instance.getActiveAnalyticQuestions();
        assertTrue(questions.isEmpty());
    }

    /**
     * Test of removePluginsMatchingCategory method, of class AnalyticViewController.
     */
    @Test
    public void testRemovePluginsMatchingCategory() {
        System.out.println("removePluginsMatchingCategory");
        String currentCategory = "";
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.removePluginsMatchingCategory(currentCategory);
        assertTrue(instance.getActiveAnalyticQuestions().isEmpty());
        
        final AnalyticQuestionDescription<?> question = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        // Add a question
        instance.addAnalyticQuestion(question, selectablePlugins);
        assertFalse(instance.getActiveAnalyticQuestions().isEmpty());
        currentCategory = "Centrality";
        instance.removePluginsMatchingCategory(currentCategory);
    }

    /**
     * Test of addAnalyticQuestion method, of class AnalyticViewController.
     */
    @Test
    public void testAddAnalyticQuestion() {
        System.out.println("addAnalyticQuestion");
        final AnalyticQuestionDescription<?> question = AnalyticUtilities.lookupAnalyticQuestionDescription(BestConnectsNetworkQuestion.class);
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        final List<AnalyticConfigurationPane.SelectableAnalyticPlugin> selectablePlugins = new ArrayList<>();
        List<AnalyticQuestionDescription<?>> questions = instance.getActiveAnalyticQuestions();
        assertTrue(questions.isEmpty());
        
        // Add a question
        instance.addAnalyticQuestion(question, selectablePlugins);
        questions = instance.getActiveAnalyticQuestions();
        assertFalse(questions.isEmpty());
    }

    /**
     * Test of deactivateResultUpdates method, of class AnalyticViewController.
     */
    @Test
    public void testDeactivateResultUpdates() {
        System.out.println("deactivateResultUpdates");
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        final String translatorName = "Multi-Score -> Size Visualisation";
        final GraphVisualisationTranslator<?, ?> visualisation = AnalyticUtilities.lookupGraphVisualisationTranslator(translatorName); 
        final SizeVisualisation sizeVisualisation = (SizeVisualisation) visualisation.buildControl();
        final boolean activated = true;
        
        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            final AnalyticViewState currentState = mock(AnalyticViewState.class);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            final ReadableGraph rg = mock(ReadableGraph.class);
            when(graph.getReadableGraph()).thenReturn(rg);
            final int stateAttributeId = AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.get(rg);
            when(rg.getObjectValue(stateAttributeId, 0)).thenReturn(currentState);
            
            instance.updateGraphVisualisations(sizeVisualisation, activated);
            instance.writeState();
            instance.deactivateResultUpdates(graph);
            instance.readState();
            
            final Map<GraphVisualisation, Boolean> newVisualisations = new HashMap<>();
            newVisualisations.put(sizeVisualisation, false);
            when(currentState.getGraphVisualisations()).thenReturn(instance.getGraphVisualisations());
             
            final Map<GraphVisualisation, Boolean> visualisations = currentState.getGraphVisualisations();
            assertEquals(visualisations, instance.getGraphVisualisations());
        }

    }

    /**
     * Test of updateState method, of class AnalyticViewController.
     */
    @Test
    public void testUpdateState() {
        System.out.println("updateState");
        final boolean pluginWasSelected = true;
        final ListView<AnalyticConfigurationPane.SelectableAnalyticPlugin> pluginList = mock(ListView.class);
        final AnalyticViewController instance = mock(AnalyticViewController.class);
        final AnalyticViewPane pane = mock(AnalyticViewPane.class);
        final AnalyticConfigurationPane configPane = mock(AnalyticConfigurationPane.class);
        final ListView<String> categoryList = new ListView<>();
        instance.updateState(pluginWasSelected, pluginList);
        
        when(pane.getConfigurationPane()).thenReturn(configPane);
        when(pane.getConfigurationPane().getCategoryList()).thenReturn(categoryList);
        verify(pane, times(1)).getConfigurationPane();    
    }

    /**
     * Test of selectOnInternalVisualisations method, of class AnalyticViewController.
     */
    @Test
    public void testSelectOnInternalVisualisations() {
        System.out.println("selectOnInternalVisualisations");
        GraphElementType elementType = GraphElementType.VERTEX;
        final AnalyticViewController instance = AnalyticViewController.getDefault();
        instance.selectOnInternalVisualisations(elementType, graph);
        // TO DO 
    }
    
}
