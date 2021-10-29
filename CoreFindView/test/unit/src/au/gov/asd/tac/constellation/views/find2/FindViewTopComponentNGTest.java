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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.views.find2.components.BasicFindTab;
import au.gov.asd.tac.constellation.views.find2.components.FindViewPane;
import au.gov.asd.tac.constellation.views.find2.components.FindViewTabs;
import au.gov.asd.tac.constellation.views.find2.components.ReplaceTab;
import au.gov.asd.tac.constellation.views.find2.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResultsList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
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
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
        }
    }

    /**
     * Test of createContent method, of class FindViewTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");

        setUpTests();

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

        assertEquals("resources/find-view.css", findViewTopComponent.createStyle());
    }

    /**
     * Test of handleComponentClosed method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleComponentClosed() {
        System.out.println("handleComponentClosed");
        setUpTests();
        setupGraph();

        topComponent = mock(FindViewTopComponent.class);
        doNothing().when(topComponent).UpdateUI();
        doNothing().when(topComponent).disableFindView();

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
        setUpTests();
        setupGraph();

        topComponent = mock(FindViewTopComponent.class);
        doNothing().when(topComponent).UpdateUI();
        doNothing().when(topComponent).disableFindView();
        doNothing().when(topComponent).focusFindTextField();

        // Open the component
        doCallRealMethod().when(topComponent).handleComponentOpened();
        topComponent.handleComponentOpened();

        // verify the 3 methods were all called once
        verify(topComponent, times(1)).UpdateUI();
        verify(topComponent, times(1)).disableFindView();
        verify(topComponent, times(1)).focusFindTextField();

    }

    /**
     * Test of handleGraphOpened method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphOpened() {
        System.out.println("handleGraphOpened");
        setUpTests();
        setupGraph();

        topComponent = mock(FindViewTopComponent.class);
        doNothing().when(topComponent).disableFindView();

        // call handle graph opened
        doCallRealMethod().when(topComponent).handleGraphOpened(graph);
        topComponent.handleGraphOpened(graph);

        // verify that the disableFindView function was called once
        verify(topComponent, times(1)).disableFindView();
    }

    /**
     * Test of handleGraphClosed method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleGraphClosed() {
        System.out.println("handleGraphClosed");
        setUpTests();
        setupGraph();

        topComponent = mock(FindViewTopComponent.class);
        doNothing().when(topComponent).disableFindView();

        // call handle graph closed
        doCallRealMethod().when(topComponent).handleGraphClosed(any(Graph.class));
        topComponent.handleGraphClosed(graph);

        // verify the disableFindViewFunction was called
        verify(topComponent, times(1)).disableFindView();

    }

    /**
     * Test of handleNewGraph method, of class FindViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("handleNewGraph");

        setUpTests();
        setupGraph();

        topComponent = mock(FindViewTopComponent.class);
        doNothing().when(topComponent).UpdateUI();

        // Call handle new graph
        doCallRealMethod().when(topComponent).handleNewGraph(graph);
        topComponent.handleNewGraph(graph);

        // verify updateUI was called
        verify(topComponent, times(1)).UpdateUI();
    }

    /**
     * Test of disableFindView method, of class FindViewTopComponent.
     */
    @Test
    public void testDisableFindView() {
        System.out.println("disableFindView");
        setUpTests();

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
            assertEquals(topComponent.getFindViewPane().isDisabled(), true);

            /**
             * SetUp the graph then repeat the same process. Verify that the
             * pane is no longer disabled
             */
            setupGraph();
            topComponent.disableFindView();
            assertEquals(topComponent.getFindViewPane().isDisabled(), false);

        }
    }

    /**
     * Test of setInitialDimensions method, of class FindViewTopComponent.
     */
//    @Test
//    public void testSetInitialDimensions() {
//        System.out.println("setInitialDimensions");
//        FindViewTopComponent instance = new FindViewTopComponent();
//        instance.setInitialDimensions();
//        // TODO review the generated test code and remove the default call to fail.
//    }
    /**
     * Test of focusFindTextField method, of class FindViewTopComponent.
     */
    @Test
    public void testFocusFindTextField() {
//        System.out.println("focusFindTextField");
//        setUpTests();
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
        setUpTests();

        //Create spys for all UI components
        spyTopComponent = spy(topComponent);

        pane = new FindViewPane(topComponent);
        spyPane = spy(pane);

        tabs = new FindViewTabs(spyPane);
        spyTabs = spy(tabs);

        basicFindTab = new BasicFindTab(spyTabs);
        spyBasicFindTab = spy(basicFindTab);

        replaceTab = new ReplaceTab(spyTabs);
        spyReplaceTab = spy(replaceTab);

        when(spyTopComponent.getFindViewPane()).thenReturn(spyPane);
        when(spyPane.getTabs()).thenReturn(spyTabs);
        when(spyTabs.getBasicFindTab()).thenReturn(spyBasicFindTab);
        when(spyTabs.getReplaceTab()).thenReturn(spyReplaceTab);

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

    public void setUpTests() {
        topComponent = new FindViewTopComponent();
        pane = mock(FindViewPane.class);
        tabs = mock(FindViewTabs.class);

        basicFindTab = mock(BasicFindTab.class);
        replaceTab = mock(ReplaceTab.class);

        when(pane.getTabs()).thenReturn(tabs);
        when(tabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(tabs.getReplaceTab()).thenReturn(replaceTab);
    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap.put(graph.getId(), graph);
        try {
            WritableGraph wg = graph.getWritableGraph("", true);
            final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);

            ArrayList<Attribute> attributeList = new ArrayList<>();
            BasicFindReplaceParameters parameters = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, false, false, false);
            FindResultsList foundResult = new FindResultsList(2, parameters, graph.getId());

            wg.setObjectValue(stateId, 0, foundResult);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
