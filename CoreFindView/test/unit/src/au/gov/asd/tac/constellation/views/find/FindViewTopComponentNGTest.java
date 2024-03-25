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
import static org.mockito.Mockito.spy;
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
    private Map<String, Graph> graphMap = new HashMap<>();
    private FindViewTopComponent topComponent;
    private FindViewTopComponent spyTopComponent;
    private FindViewPane pane;
    private FindViewPane spyPane;
    private FindViewTabs tabs;
    private FindViewTabs spyTabs;
    private BasicFindTab basicFindTab;
    private BasicFindTab spyBasicFindTab;
    private ReplaceTab replaceTab;
    private ReplaceTab spyReplaceTab;
    private AdvancedFindTab advancedFindTab;
    private AdvancedFindTab spyAdvancedFindTab;
    private List<AdvancedCriteriaBorderPane> criteriaPanesList;
    private static final Logger LOGGER = Logger.getLogger(FindViewTopComponentNGTest.class.getName());

    public FindViewTopComponentNGTest() {
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
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        topComponent = mock(FindViewTopComponent.class);
        spyTopComponent = spy(topComponent);

        pane = mock(FindViewPane.class);
        spyPane = spy(pane);

        tabs = mock(FindViewTabs.class);
        spyTabs = spy(tabs);

        basicFindTab = mock(BasicFindTab.class);
        replaceTab = mock(ReplaceTab.class);
        advancedFindTab = mock(AdvancedFindTab.class);
        spyBasicFindTab = spy(basicFindTab);
        spyReplaceTab = spy(replaceTab);
        spyAdvancedFindTab = spy(advancedFindTab);

        when(spyTopComponent.getFindViewPane()).thenReturn(spyPane);
        when(spyPane.getTabs()).thenReturn(spyTabs);
        when(spyTabs.getBasicFindTab()).thenReturn(spyBasicFindTab);
        when(spyTabs.getReplaceTab()).thenReturn(spyReplaceTab);
        when(spyTabs.getAdvancedFindTab()).thenReturn(spyAdvancedFindTab);
    }

    /**
     * Test of createContent method, of class FindViewTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");

        when(spyTopComponent.createContent()).thenReturn(spyPane);
        assertEquals(spyTopComponent.createContent(), spyTopComponent.getFindViewPane());
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

        doNothing().when(spyTopComponent).UpdateUI();
        doNothing().when(spyTopComponent).disableFindView();
        doNothing().when(spyTopComponent).focusFindTextField();

        // Open the component first to set up the supers listener
        doCallRealMethod().when(spyTopComponent).handleComponentOpened();
        spyTopComponent.handleComponentOpened();

        // close the component
        doCallRealMethod().when(spyTopComponent).handleComponentClosed();
        spyTopComponent.handleComponentClosed();

        /**
         * updateUI and disable findFindView should be called twice for
         * component open and component close
         */
        verify(spyTopComponent, times(2)).UpdateUI();
        verify(spyTopComponent, times(2)).disableFindView();
    }

    /**
     * Test of handleComponentOpened method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleComponentOpened() {
        System.out.println("handleComponentOpened");

        setupGraph();

        doNothing().when(spyTopComponent).UpdateUI();
        doNothing().when(spyTopComponent).disableFindView();
        doNothing().when(spyTopComponent).focusFindTextField();

        // Open the component
        doCallRealMethod().when(spyTopComponent).handleComponentOpened();
        spyTopComponent.handleComponentOpened();

        // verify the 3 methods were all called once
        verify(spyTopComponent, times(1)).UpdateUI();
        verify(spyTopComponent, times(1)).disableFindView();
        verify(spyTopComponent, times(1)).focusFindTextField();

    }

    /**
     * Test of handleGraphOpened method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphOpened() {
        System.out.println("handleGraphOpened");

        setupGraph();

        doNothing().when(spyTopComponent).disableFindView();

        // call handle graph opened
        doCallRealMethod().when(spyTopComponent).handleGraphOpened(graph);
        spyTopComponent.handleGraphOpened(graph);

        // verify that the disableFindView function was called once
        verify(spyTopComponent, times(1)).disableFindView();
    }

    /**
     * Test of handleGraphClosed method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphClosed() {
        System.out.println("handleGraphClosed");

        setupGraph();

        doNothing().when(spyTopComponent).disableFindView();

        // call handle graph closed
        doCallRealMethod().when(spyTopComponent).handleGraphClosed(any(Graph.class));
        spyTopComponent.handleGraphClosed(graph);

        // verify the disableFindViewFunction was called
        verify(spyTopComponent, times(1)).disableFindView();

    }

    /**
     * Test of handleNewGraph method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("handleNewGraph");

        setupGraph();

        doNothing().when(spyTopComponent).UpdateUI();

        // Call handle new graph
        doCallRealMethod().when(spyTopComponent).handleNewGraph(graph);
        spyTopComponent.handleNewGraph(graph);

        // verify updateUI was called
        verify(spyTopComponent, times(1)).UpdateUI();
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
            spyTopComponent.disableFindView();
            assertEquals(spyPane.isDisabled(), false);
//            verify(spyTopComponent, times(1)).disableFindView();

//            /**
//             * SetUp the graph then repeat the same process. Verify that the
//             * pane is no longer disabled
//             */
//            spyPane.setDisable(true);
//            assertEquals(spyPane.isDisabled(), true);
//            verify(spyTopComponent, times(2)).disableFindView();
        }
    }

    /**
     * Test of focusFindTextField method, of class FindViewTopComponent.
     */
    @Test
    public void testFocusFindTextField() {
//        System.out.println("focusFindTextField");
//        setupGraph();
//
//        spyTopComponent = spy(topComponent);
//
//        pane = new FindViewPane(topComponent);
//        spyPane = spy(pane);
//
//        tabs = new FindViewTabs(spyPane);
//        spyTabs = spy(tabs);
//
//        basicFindTab = new BasicFindTab(spyTabs);
//        spyBasicFindTab = spy(basicFindTab);
//
//        replaceTab = new ReplaceTab(spyTabs);
//        spyReplaceTab = spy(replaceTab);
//
//        when(spyTopComponent.getFindViewPane()).thenReturn(spyPane);
//        when(spyPane.getTabs()).thenReturn(spyTabs);
//        when(spyTabs.getBasicFindTab()).thenReturn(spyBasicFindTab);
//
//        doCallRealMethod().when(spyTopComponent).focusFindTextField();
//
//        spyTopComponent.handleGraphOpened(graph);
//        spyTopComponent.handleComponentOpened();
//        spyTopComponent.focusFindTextField();
//        assertEquals(spyBasicFindTab.getFindTextField().isFocused(), true);
    }

    /**
     * Test of UpdateUI method, of class FindViewTopComponent.
     */
    @Test
    public void testUpdateUI() {
        System.out.println("UpdateUI");
//        final GraphElementType basicFindType = GraphElementType.getValue(getFindViewPane().getTabs().getBasicFindTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);

        when(spyTopComponent.getFindViewPane()).thenReturn(spyPane);
        when(spyPane.getTabs()).thenReturn(spyTabs);
        when(spyTabs.getBasicFindTab()).thenReturn(spyBasicFindTab);
        when(spyBasicFindTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(spyTabs.getReplaceTab()).thenReturn(spyReplaceTab);
        when(spyReplaceTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(spyTabs.getAdvancedFindTab()).thenReturn(spyAdvancedFindTab);
        when(spyAdvancedFindTab.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        criteriaPanesList = new ArrayList<>();
        when(spyAdvancedFindTab.getCorrespondingCriteriaList(GraphElementType.VERTEX)).thenReturn(criteriaPanesList);

        // Make the functions not do nothing
        doNothing().when(spyBasicFindTab).saveSelected(Mockito.any());
        doNothing().when(spyBasicFindTab).populateAttributes(Mockito.any());
        doNothing().when(spyBasicFindTab).updateSelectedAttributes(Mockito.any());
        doNothing().when(spyReplaceTab).saveSelected(Mockito.any());
        doNothing().when(spyReplaceTab).populateAttributes(Mockito.any());
        doNothing().when(spyReplaceTab).updateSelectedAttributes(Mockito.any());

        // call the update UI method
        doCallRealMethod().when(spyTopComponent).UpdateUI();
        spyTopComponent.UpdateUI();

        // verify each function was called once
        verify(spyBasicFindTab, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
        verify(spyBasicFindTab, times(1)).populateAttributes(Mockito.eq(GraphElementType.VERTEX));
        verify(spyBasicFindTab, times(1)).updateSelectedAttributes(Mockito.eq(spyBasicFindTab.getMatchingAttributeList(GraphElementType.VERTEX)));
        verify(spyReplaceTab, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
        verify(spyReplaceTab, times(1)).populateAttributes(Mockito.eq(GraphElementType.VERTEX));
        verify(spyReplaceTab, times(1)).updateSelectedAttributes(Mockito.eq(spyBasicFindTab.getMatchingAttributeList(GraphElementType.VERTEX)));

    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap.put(graph.getId(), graph);
        try {
            WritableGraph wg = graph.getWritableGraph("", true);
            final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);

            ArrayList<Attribute> attributeList = new ArrayList<>();
            BasicFindReplaceParameters parameters = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
            FindResultsList foundResult = new FindResultsList(2, parameters);

            wg.setObjectValue(stateId, 0, foundResult);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
