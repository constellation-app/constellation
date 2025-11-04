/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.views.find.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find.components.BasicFindTab;
import au.gov.asd.tac.constellation.views.find.components.FindViewPane;
import au.gov.asd.tac.constellation.views.find.components.FindViewTabs;
import au.gov.asd.tac.constellation.views.find.components.ReplaceTab;
import au.gov.asd.tac.constellation.views.find.components.advanced.AdvancedCriteriaBorderPane;
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindResultsList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ChoiceBox;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewTopComponentNGTest {

    private Graph graph;
    private Map<String, Graph> graphMap;
    private FindViewTopComponent topComponent;
    private FindViewPane pane;
    private FindViewTabs tabs;
    private BasicFindTab basicFindTab;
    private ReplaceTab replaceTab;
    private AdvancedFindTab advancedFindTab;
    private List<AdvancedCriteriaBorderPane> criteriaPanesList;
    private static final Logger LOGGER = Logger.getLogger(FindViewTopComponentNGTest.class.getName());
    
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
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        topComponent = mock(FindViewTopComponent.class);
        pane = mock(FindViewPane.class);

        tabs = mock(FindViewTabs.class);
        basicFindTab = mock(BasicFindTab.class);
        replaceTab = mock(ReplaceTab.class);
        advancedFindTab = mock(AdvancedFindTab.class);

        when(topComponent.getFindViewPane()).thenReturn(pane);
        when(pane.getTabs()).thenReturn(tabs);
        when(tabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(tabs.getReplaceTab()).thenReturn(replaceTab);
        when(tabs.getAdvancedFindTab()).thenReturn(advancedFindTab);
    }

    /**
     * Test of createContent method, of class FindViewTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");

        when(topComponent.createContent()).thenReturn(pane);
        assertEquals(topComponent.createContent(), topComponent.getFindViewPane());
    }

    /**
     * Test of createStyle method, of class FindViewTopComponent.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);

        doCallRealMethod().when(findViewTopComponent).createStyle();

        assertEquals("resources/find-view-light.css", findViewTopComponent.createStyle());
    }

    /**
     * Test of handleComponentClosed method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleComponentClosed() {
        System.out.println("handleComponentClosed");

        setupGraph();

        doNothing().when(topComponent).UpdateUI();
        doNothing().when(topComponent).disableFindView();
        doNothing().when(topComponent).focusFindTextField();

        // Open the component first to set up the supers listener
        doCallRealMethod().when(topComponent).handleComponentOpened();
        topComponent.handleComponentOpened();

        // close the component
        doCallRealMethod().when(topComponent).handleComponentClosed();
        topComponent.handleComponentClosed();

        /**
         * updateUI and disable findFindView should be called twice for
         * component open and component close
         */
        verify(topComponent, times(2)).UpdateUI();
        verify(topComponent, times(2)).disableFindView();
    }

    /**
     * Test of handleComponentOpened method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleComponentOpened() {
        System.out.println("handleComponentOpened");

        setupGraph();

        doNothing().when(topComponent).UpdateUI();
        doNothing().when(topComponent).disableFindView();
        doNothing().when(topComponent).focusFindTextField();

        // Open the component
        doCallRealMethod().when(topComponent).handleComponentOpened();
        topComponent.handleComponentOpened();

        // verify the 3 methods were all called once
        verify(topComponent).UpdateUI();
        verify(topComponent).disableFindView();
        verify(topComponent).focusFindTextField();

    }

    /**
     * Test of handleGraphOpened method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphOpened() {
        System.out.println("handleGraphOpened");

        setupGraph();

        doNothing().when(topComponent).disableFindView();

        // call handle graph opened
        doCallRealMethod().when(topComponent).handleGraphOpened(graph);
        topComponent.handleGraphOpened(graph);

        // verify that the disableFindView function was called once
        verify(topComponent).disableFindView();
    }

    /**
     * Test of handleGraphClosed method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphClosed() {
        System.out.println("handleGraphClosed");

        setupGraph();

        doNothing().when(topComponent).disableFindView();

        // call handle graph closed
        doCallRealMethod().when(topComponent).handleGraphClosed(any(Graph.class));
        topComponent.handleGraphClosed(graph);

        // verify the disableFindViewFunction was called
        verify(topComponent).disableFindView();

    }

    /**
     * Test of handleNewGraph method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("handleNewGraph");

        setupGraph();

        doNothing().when(topComponent).UpdateUI();

        // Call handle new graph
        doCallRealMethod().when(topComponent).handleNewGraph(graph);
        topComponent.handleNewGraph(graph);

        // verify updateUI was called
        verify(topComponent).UpdateUI();
    }

    /**
     * Test of disableFindView method, of class FindViewTopComponent.
     */
    @Test
    public void testDisableFindView() {
        System.out.println("disableFindView");
        
        setupGraph();

        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);

        // create a static mock for the graph manager
        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            /**
             * Call to disable the view when no graphs are present. Verify that
             * the pane is disabled.
             */
            topComponent.disableFindView();
            assertEquals(pane.isDisabled(), false);
        }
    }

    /**
     * Test of UpdateUI method, of class FindViewTopComponent.
     */
    @Test
    public void testUpdateUI() {
        System.out.println("UpdateUI");

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);

        when(topComponent.getFindViewPane()).thenReturn(pane);
        when(pane.getTabs()).thenReturn(tabs);
        when(tabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(basicFindTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(tabs.getReplaceTab()).thenReturn(replaceTab);
        when(replaceTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(tabs.getAdvancedFindTab()).thenReturn(advancedFindTab);
        when(advancedFindTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        criteriaPanesList = new ArrayList<>();
        when(advancedFindTab.getCorrespondingCriteriaList(GraphElementType.VERTEX)).thenReturn(criteriaPanesList);

        // Make the functions not do nothing
        doNothing().when(basicFindTab).saveSelected(Mockito.any());
        doNothing().when(basicFindTab).populateAttributes(Mockito.any());
        doNothing().when(basicFindTab).updateSelectedAttributes(Mockito.any());
        doNothing().when(replaceTab).saveSelected(Mockito.any());
        doNothing().when(replaceTab).populateAttributes(Mockito.any());
        doNothing().when(replaceTab).updateSelectedAttributes(Mockito.any());

        // call the update UI method
        doCallRealMethod().when(topComponent).UpdateUI();
        topComponent.UpdateUI();

        // verify each function was called once
        verify(basicFindTab).saveSelected(GraphElementType.VERTEX);
        verify(basicFindTab).populateAttributes(GraphElementType.VERTEX);
        verify(basicFindTab).updateSelectedAttributes(basicFindTab.getMatchingAttributeList(GraphElementType.VERTEX));
        verify(replaceTab).saveSelected(GraphElementType.VERTEX);
        verify(replaceTab).populateAttributes(GraphElementType.VERTEX);
        verify(replaceTab).updateSelectedAttributes(basicFindTab.getMatchingAttributeList(GraphElementType.VERTEX));

    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap = new HashMap<>();
        graphMap.put(graph.getId(), graph);
        try {
            WritableGraph wg = graph.getWritableGraph("", true);
            final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);

            ArrayList<Attribute> attributeList = new ArrayList<>();
            BasicFindReplaceParameters parameters = new BasicFindReplaceParameters("label name", "", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
            FindResultsList foundResult = new FindResultsList(2, parameters);

            wg.setObjectValue(stateId, 0, foundResult);

            wg.commit();
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }
}
