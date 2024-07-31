/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessState;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.swing.SwingUtilities;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessUtilitiesNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessUtilitiesNGTest.class.getName());

    private static MockedStatic<SwingUtilities> swingUtilitiesStaticMock;
    private static MockedStatic<WindowManager> windowManagerStaticMock;

    @BeforeClass
    public static void setUpClass() throws Exception {
        swingUtilitiesStaticMock = Mockito.mockStatic(SwingUtilities.class);
        windowManagerStaticMock = Mockito.mockStatic(WindowManager.class);

        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        swingUtilitiesStaticMock.close();
        windowManagerStaticMock.close();

        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        swingUtilitiesStaticMock.reset();
        windowManagerStaticMock.reset();
    }

    @Test
    public void testGetDataAccessPaneCalledByEventDispatchThread() {
        final WindowManager windowManager = mock(WindowManager.class);
        final DataAccessViewTopComponent topComponent = mock(DataAccessViewTopComponent.class);
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(topComponent);
        when(topComponent.isOpened()).thenReturn(false);
        when(topComponent.getDataAccessPane()).thenReturn(dataAccessPane);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        verify(topComponent, times(1)).open();
        verify(topComponent, times(1)).requestVisible();

        assertSame(actual, dataAccessPane);
    }

    @Test
    public void testGetDataAccessPaneCalledByEventDispatchThreadTopComponentNull() {
        final WindowManager windowManager = mock(WindowManager.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(null);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertNull(actual);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThread() {
        final WindowManager windowManager = mock(WindowManager.class);
        final DataAccessViewTopComponent topComponent = mock(DataAccessViewTopComponent.class);
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);

        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenAnswer(invocation -> {
                    final Runnable r = invocation.getArgument(0);
                    r.run();
                    return null;
                });

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(topComponent);
        when(topComponent.isOpened()).thenReturn(false);
        when(topComponent.getDataAccessPane()).thenReturn(dataAccessPane);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertSame(actual, dataAccessPane);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThreadError() {
        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);
        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenThrow(new InvocationTargetException(new RuntimeException("Something Bad")));

        final DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertNull(actual);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThreadInterruptError() {
        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);
        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenThrow(new InterruptedException());

        final DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertTrue(Thread.interrupted());
    }

    @Test
    public void testsaveDataAccessState() throws Exception {
        System.out.println("testsaveDataAccessState");

        // mock Tab
        final Tab tab = mock(Tab.class);

        ObservableList<Tab> observableArrayList
                = FXCollections.observableArrayList(tab);

        // mock TabPane
        final TabPane tabPane = mock(TabPane.class);
        when(tabPane.getTabs()).thenReturn(observableArrayList);

        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);
        final PluginParameter pluginParameter = mock(PluginParameter.class);

        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);
        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(pluginParameters);
        when(pluginParameter.getStringValue()).thenReturn("something");

        final String someKey = "someKey";
        final Map<String, PluginParameter<?>> map = Map.of(someKey, pluginParameter);
        when(pluginParameters.getParameters()).thenReturn(map);

        // mock graph
        final Graph graph = mock(Graph.class);
        final WritableGraph wGraph = mock(WritableGraph.class);
        when(graph.getWritableGraph("Update Data Access State", true)).thenReturn(wGraph);

        DataAccessUtilities.saveDataAccessState(tabPane, graph);

        final DataAccessState expectedTab = new DataAccessState();
        expectedTab.newTab();
        expectedTab.add("someKey", "something");

        assertEquals(expectedTab.getState().size(), 1);
        verify(wGraph).setObjectValue(0, 0, expectedTab);
    }

    @Test
    public void loadDataAccessState() {
        // Create current data access view state and set some parameters
        // The code currenly only looks at the first tab so parameter2
        // value will be ignored
        final DataAccessState currentState = new DataAccessState();
        currentState.newTab();
        currentState.add("parameter1", "parameter1_new_value");
        currentState.newTab();
        currentState.add("parameter2", "parameter2_new_value");

        // mock graph
        final Graph graph = mock(Graph.class);
        final ReadableGraph rGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rGraph);

        // mock data access state attribute in graph
        when(rGraph.getAttribute(GraphElementType.META, "dataaccess_state")).thenReturn(2);
        when(rGraph.getObjectValue(2, 0)).thenReturn(currentState);

        // mock tab pane
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final TabPane tabPane = mock(TabPane.class);
        final Tab currentTab = mock(Tab.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);
        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(currentTab, mock(Tab.class)));

        try (
                final MockedStatic<DataAccessTabPane> daTabPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

            daTabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(currentTab))
                    .thenReturn(queryPhasePane);

            final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
            final PluginParameters globalPluginParameters = mock(PluginParameters.class);

            final PluginParameter pluginParameter1 = mock(PluginParameter.class);
            final PluginParameter pluginParameter2 = mock(PluginParameter.class);

            when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
            when(globalParametersPane.getParams()).thenReturn(globalPluginParameters);
            when(globalPluginParameters.getParameters()).thenReturn(Map.of(
                    "parameter1", pluginParameter1,
                    "parameter2", pluginParameter2
            ));

            DataAccessUtilities.loadDataAccessState(dataAccessPane, graph);

            verify(pluginParameter1).setStringValue("parameter1_new_value");
            verify(pluginParameter2, never()).setStringValue(anyString());

            verify(rGraph).release();
        }
    }

    @Test
    public void loadDataAccessState_attribute_not_found() {
        // mock graph
        final Graph graph = mock(Graph.class);
        final ReadableGraph rGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rGraph);

        // mock data access state attribute in graph
        when(rGraph.getAttribute(GraphElementType.META, "dataaccess_state")).thenReturn(Graph.NOT_FOUND);

        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);

        DataAccessUtilities.loadDataAccessState(dataAccessPane, graph);

        verify(rGraph, never()).getObjectValue(anyInt(), anyInt());

        verify(rGraph).release();
    }

    @Test
    public void loadDataAccessState_graph_is_null() {
        DataAccessUtilities.loadDataAccessState(mock(DataAccessPane.class), null);
        // No exception....pass
    }

    @Test
    public void loadDataAccessState_no_tabs() {
        // mock graph
        final Graph graph = mock(Graph.class);

        // mock tab pane
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(null);

        DataAccessUtilities.loadDataAccessState(dataAccessPane, graph);

        verifyNoInteractions(graph);
    }

    @Test
    public void loadDataAccessState_null_state() {
        // mock graph
        final Graph graph = mock(Graph.class);
        final ReadableGraph rGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rGraph);

        // mock data access state attribute in graph
        when(rGraph.getAttribute(GraphElementType.META, "dataaccess_state")).thenReturn(2);
        when(rGraph.getObjectValue(2, 0)).thenReturn(null);

        // mock tab pane
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);

        try (
                final MockedStatic<DataAccessTabPane> daTabPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            DataAccessUtilities.loadDataAccessState(dataAccessPane, graph);

            daTabPaneMockedStatic.verifyNoInteractions();

            verify(rGraph).release();
        }
    }

    @Test
    public void loadDataAccessState_empty_state() {
        // mock graph
        final Graph graph = mock(Graph.class);
        final ReadableGraph rGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rGraph);

        // mock data access state attribute in graph
        when(rGraph.getAttribute(GraphElementType.META, "dataaccess_state")).thenReturn(2);
        when(rGraph.getObjectValue(2, 0)).thenReturn(new DataAccessState());

        // mock tab pane
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);

        try (
                final MockedStatic<DataAccessTabPane> daTabPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            DataAccessUtilities.loadDataAccessState(dataAccessPane, graph);

            daTabPaneMockedStatic.verifyNoInteractions();

            verify(rGraph).release();
        }
    }
}
