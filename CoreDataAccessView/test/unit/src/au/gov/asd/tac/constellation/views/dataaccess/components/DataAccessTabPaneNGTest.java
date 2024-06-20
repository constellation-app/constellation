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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.utilities.gui.RecentValue.RecentValueUtility;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.lang.reflect.Field;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessTabPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessTabPaneNGTest.class.getName());
    private static final String newStepCaption = "New Caption - Step 1";

    private DataAccessViewTopComponent topComponent;
    private DataAccessPane dataAccessPane;
    private Map<String, Pair<Integer, List<DataAccessPlugin>>> plugins;

    private DataAccessTabPane dataAccessTabPane;

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
        dataAccessPane = mock(DataAccessPane.class);
        topComponent = mock(DataAccessViewTopComponent.class);

        when(dataAccessPane.getParentComponent()).thenReturn(topComponent);
        when(topComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());

        plugins = Map.of();

        dataAccessTabPane = spy(new DataAccessTabPane(dataAccessPane, plugins));

        DataAccessPaneState.clearState();
    }

    @Test
    public void init() {
        final DataAccessTabPane realDataAccessTabPane = new DataAccessTabPane(dataAccessPane, plugins);

        assertSame(realDataAccessTabPane.getDataAccessPane(), dataAccessPane);
        assertSame(realDataAccessTabPane.getPlugins(), plugins);

        assertNotNull(realDataAccessTabPane.getTabPane());
        assertEquals(realDataAccessTabPane.getTabPane().getSide(), Side.TOP);
    }

    @Test
    public void newTab() {
        try (final MockedConstruction<QueryPhasePane> mockedQueryPhasePane
                = Mockito.mockConstruction(QueryPhasePane.class, (queryPhasePaneMock, cntxt) -> {
                    final List<Object> expectedArgs = new ArrayList<>();
                    expectedArgs.add(plugins);
                    expectedArgs.add(dataAccessPane);
                    expectedArgs.add(null);

                    assertEquals(cntxt.arguments(), expectedArgs);
                })) {
            doNothing().when(dataAccessTabPane).newTab(any(QueryPhasePane.class), any(String.class));

            final QueryPhasePane pane = dataAccessTabPane.newTab();

            assertNotNull(pane);

            assertEquals(mockedQueryPhasePane.constructed().size(), 1);
            assertSame(mockedQueryPhasePane.constructed().get(0), pane);

            verify(dataAccessTabPane).newTab(pane, "");
        }
    }

    @Test
    public void newTab_provide_query_pane() {
        verifyNewTab(true, true, true, false, true);

        verifyNewTab(true, false, false, false, true);
        verifyNewTab(true, false, true, false, true);
        verifyNewTab(true, true, false, true, true);

        verifyNewTab(false, true, true, false, false);
        verifyNewTab(false, false, true, false, false);
        verifyNewTab(false, true, false, false, false);

        verifyNewTab(false, false, false, false, false);
    }

    @Test
    public void newTab_name_specified() {
        try (final MockedConstruction<QueryPhasePane> mockedQueryPhasePane
                = Mockito.mockConstruction(QueryPhasePane.class, (queryPhasePaneMock, cntxt) -> {
                    final List<Object> expectedArgs = new ArrayList<>();
                    expectedArgs.add(plugins);
                    expectedArgs.add(dataAccessPane);
                    expectedArgs.add(null);

                    assertEquals(cntxt.arguments(), expectedArgs);
                })) {
            doNothing().when(dataAccessTabPane).newTab(any(QueryPhasePane.class), any(String.class));

            final QueryPhasePane pane = dataAccessTabPane.newTab("new name");

            assertNotNull(pane);

            assertEquals(mockedQueryPhasePane.constructed().size(), 1);
            assertSame(mockedQueryPhasePane.constructed().get(0), pane);

            verify(dataAccessTabPane).newTab(pane, "new name");
        }
    }

    @Test
    public void runTabs() {
        final String graphId = "graphId";

        try (
                final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class); final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class); final MockedStatic<CompletableFuture> futureMockedStatic
                = Mockito.mockStatic(CompletableFuture.class, Mockito.CALLS_REAL_METHODS);) {
            // Set up the active graph
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph graph = mock(Graph.class);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            when(graph.getId()).thenReturn(graphId);

            // No need to actually call, storeParameterValues. We will just verify
            // that it is called.
            doNothing().when(dataAccessTabPane).storeParameterValues();

            // Set up our fake tab pane
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final Tab tab2 = mock(Tab.class);
            final Tab tab3 = mock(Tab.class);
            final Tab tab4 = mock(Tab.class);
            final Tab tab5 = mock(Tab.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1,
                    tab2,
                    tab3,
                    tab4,
                    tab5
            ));

            // No need for a different query pane per tab, so just return the same
            // one for all of them
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(any(Tab.class)))
                    .thenReturn(queryPhasePane);

            final List<Future<?>> barrier1 = List.of(CompletableFuture.completedFuture("Future 1 Complete"));
            final List<Future<?>> barrier2 = List.of(CompletableFuture.completedFuture("Future 2 Complete"));
            final List<Future<?>> barrier3 = List.of(CompletableFuture.completedFuture("Future 3 Complete"));

            when(queryPhasePane.runPlugins(null)).thenReturn(barrier1);
            when(queryPhasePane.runPlugins(barrier1)).thenReturn(barrier2);
            when(queryPhasePane.runPlugins(barrier2)).thenReturn(barrier3);

            futureMockedStatic.when(() -> CompletableFuture
                    .runAsync(any(Runnable.class), any(ExecutorService.class))
            )
                    .thenAnswer(iom -> {
                        // We can no longer cast the task to WaitForQueriesToCompleteTask
                        // It is now being seen as a $$lambda##hashcode## class, so we need to
                        // refer to internal Field definitions of the returned class
                        Object task = iom.getArgument(0);
                        Field datpField = task.getClass().getDeclaredFields()[0];
                        datpField.setAccessible(true);
                        Field gidField = task.getClass().getDeclaredFields()[1];
                        gidField.setAccessible(true);
                        DataAccessTabPane datp = (DataAccessTabPane) datpField.get(task);
                        String gid = (String) gidField.get(task);

                        assertEquals(datp.getDataAccessPane(), dataAccessPane);
                        assertEquals(gid, graphId);

                        return null;
                    });

            dataAccessTabPane.runTabs(1, 3);

            verify(dataAccessTabPane).storeParameterValues();

            verify(dataAccessPane).setExecuteButtonToStop(false);
            verify(dataAccessTabPane).storeParameterValues();

            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab1), never());
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab2));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab3));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab4));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab5), never());

            // Verify that run plugins was called only 3 times
            verify(queryPhasePane, times(1)).runPlugins(isNull());
            verify(queryPhasePane, times(2)).runPlugins(any(List.class));

            // Verify that the futures of the last run is passed into the next
            verify(queryPhasePane).runPlugins(null);
            verify(queryPhasePane).runPlugins(barrier1);
            verify(queryPhasePane).runPlugins(barrier2);

            // Verify the query is running state has been set to true
            assertTrue(DataAccessPaneState.isQueriesRunning(graphId));
        }
    }

    @Test
    public void getQueryPhasePane() {
        final Tab tab = mock(Tab.class);
        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);

        assertSame(DataAccessTabPane.getQueryPhasePane(tab), queryPhasePane);
    }

    @Test
    public void updateTabMenus() {
        updateTabMenus(true, true, true, false, true);
        updateTabMenus(true, false, false, false, false);
        updateTabMenus(true, false, true, true, true);
        updateTabMenus(true, true, false, false, false);
        updateTabMenus(false, true, true, false, true);
        updateTabMenus(false, false, true, false, true);
        updateTabMenus(false, true, false, false, false);
        updateTabMenus(false, false, false, false, false);
    }

    @Test
    public void storeParameterValues_verify_global_params_stored() {
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);
        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        doCallRealMethod().when(queryPhasePane).storeParameterValues();

        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        final PluginParameter pluginParameter2 = mock(PluginParameter.class);
        final PluginParameter pluginParameter3 = mock(PluginParameter.class);

        // Set up our fake tab pane
        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1
        ));
        when(tab1.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);

        // Set up the global parameters
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);

        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(pluginParameters);
        when(pluginParameters.getParameters()).thenReturn(Map.of(
                "plugin1", pluginParameter1,
                "plugin2", pluginParameter2,
                "plugin3", pluginParameter3
        ));

        // Parameters for plugins 1 and 2 will be dropped because they have no value.
        when(pluginParameter1.getStringValue()).thenReturn(null);
        when(pluginParameter2.getStringValue()).thenReturn(" ");
        when(pluginParameter3.getStringValue()).thenReturn("PluginParam3 String Value");

        try (
                final MockedStatic<RecentParameterValues> recentParamValsMockedStatic
                = Mockito.mockStatic(RecentParameterValues.class);) {
            dataAccessTabPane.storeParameterValues();

            // Verify that only plugin 3 parameter was set in the store
            recentParamValsMockedStatic.verify(() -> RecentValueUtility
                    .storeRecentValue("plugin3", "PluginParam3 String Value"));
        }
    }

    @Test
    public void storeParameterValues_verify_plugin_params_stored() {
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);
        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        doCallRealMethod().when(queryPhasePane).storeParameterValues();

        // Set up our fake tab pane
        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1
        ));
        when(tab1.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);

        // This just needs to be set up to prevent null pointers
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters globalPluginParameters = mock(PluginParameters.class);

        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(globalPluginParameters);
        when(globalPluginParameters.getParameters()).thenReturn(Map.of());

        // Add three data source title panes to the query source pane
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane3 = mock(DataSourceTitledPane.class);

        final List<DataSourceTitledPane> dataSourceTitledPanes = new ArrayList<>();
        dataSourceTitledPanes.add(dataSourceTitledPane1);
        dataSourceTitledPanes.add(dataSourceTitledPane2);
        dataSourceTitledPanes.add(dataSourceTitledPane3);

        when(queryPhasePane.getDataAccessPanes()).thenReturn(dataSourceTitledPanes);

        // Stub out the data source title pane's plugin parameters
        // Pane 1 has no parameters so it will be dropped
        when(dataSourceTitledPane1.getParameters()).thenReturn(null);

        final PluginParameters pane2PluginParameters = mock(PluginParameters.class);
        final PluginParameters pane3PluginParameters = mock(PluginParameters.class);

        when(dataSourceTitledPane2.getParameters()).thenReturn(pane2PluginParameters);
        when(dataSourceTitledPane3.getParameters()).thenReturn(pane3PluginParameters);

        // Stub out the individual parameters 
        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        final PluginParameter pluginParameter2 = mock(PluginParameter.class);
        final PluginParameter pluginParameter3 = mock(PluginParameter.class);

        when(pane2PluginParameters.getParameters()).thenReturn(Map.of(
                "plugin1", pluginParameter1,
                "plugin2", pluginParameter2
        ));

        when(pane3PluginParameters.getParameters()).thenReturn(Map.of(
                "plugin3", pluginParameter3
        ));

        // Plugin 1 parameter will have no value and so will be ignored
        when(pluginParameter1.getObjectValue()).thenReturn(null);

        // Plugin 2 parameter will be a local date param type and handled differently
        when(pluginParameter2.getType()).thenReturn(LocalDateParameterType.INSTANCE);
        when(pluginParameter2.getObjectValue()).thenReturn("PluginParam2 Object Value");
        when(pluginParameter2.getStringValue()).thenReturn("PluginParam2 String Value");

        when(pluginParameter3.getType()).thenReturn(BooleanParameterType.INSTANCE);
        when(pluginParameter3.getObjectValue()).thenReturn("PluginParam3 Object Value");
        when(pluginParameter3.getStringValue()).thenReturn("PluginParam3 String Value");

        try (
                final MockedStatic<RecentValueUtility> recentParamValsMockedStatic =
                        Mockito.mockStatic(RecentValueUtility.class);
        ) {
            dataAccessTabPane.storeParameterValues();

            // Verify that parameters for plugins 2 and 3 were set to the store correctly
            recentParamValsMockedStatic.verify(() -> RecentValueUtility
                    .storeRecentValue("plugin2", "PluginParam2 Object Value"));
            recentParamValsMockedStatic.verify(() -> RecentValueUtility
                    .storeRecentValue("plugin3", "PluginParam3 String Value"));
        }
    }

    @Test
    public void updateTabMenu() {
        final Tab tab = mock(Tab.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab))
                    .thenReturn(queryPhasePane);

            dataAccessTabPane.updateTabMenu(tab, true, true);

            verify(queryPhasePane).enableGraphDependentMenuItems(true);
            verify(queryPhasePane).enablePluginDependentMenuItems(true);
        }
    }

    @Test
    public void isTabPaneExecutable() {
        isTabPaneExecutable(true, true, true, true);
        isTabPaneExecutable(true, false, false, false);
        isTabPaneExecutable(true, true, false, false);
        isTabPaneExecutable(true, false, true, false);
        isTabPaneExecutable(false, true, true, false);
        isTabPaneExecutable(false, false, true, false);
        isTabPaneExecutable(false, true, false, false);
        isTabPaneExecutable(false, false, false, false);
    }

    @Test
    public void hasActiveAndValidPlugins() {
        hasActiveAndValidPlugins(true, true, true, true, true);
        hasActiveAndValidPlugins(true, false, true, false, false);
        hasActiveAndValidPlugins(true, false, true, true, false);
        hasActiveAndValidPlugins(true, true, true, false, false);
        hasActiveAndValidPlugins(false, false, false, false, false);
        hasActiveAndValidPlugins(false, false, true, false, false);
        hasActiveAndValidPlugins(true, false, false, false, false);
    }

    @Test
    public void tabHasEnabledPlugins() {
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);

        final Tab tab = mock(Tab.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class, Mockito.CALLS_REAL_METHODS);) {
            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(same(tab)))
                    .thenReturn(queryPhasePane);

            when(queryPhasePane.getDataAccessPanes())
                    .thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
            when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
            when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);

            assertTrue(DataAccessTabPane.tabHasEnabledPlugins(tab));
        }
    }

    @Test
    public void validateTabEnabledPlugins() {
        validateTabEnabledPlugins(null, null, true);
        validateTabEnabledPlugins("An Error", null, false);
        validateTabEnabledPlugins("An Error", "Another Error", false);
    }

    @Test
    public void validateTabTimeRange() {
        final Tab tab = mock(Tab.class);
        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters globalPluginParameters = mock(PluginParameters.class);

        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);

        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(globalPluginParameters);

        final ZonedDateTime before = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime after = ZonedDateTime.of(2021, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC);

        // A tab's date range is valid if the to part of the range is after the from part.
        when(globalPluginParameters.getDateTimeRangeValue("CoreGlobalParameters.datetime_range"))
                .thenReturn(new DateTimeRange(before, after));
        assertTrue(DataAccessTabPane.validateTabTimeRange(tab));

        when(globalPluginParameters.getDateTimeRangeValue("CoreGlobalParameters.datetime_range"))
                .thenReturn(new DateTimeRange(after, before));
        assertFalse(DataAccessTabPane.validateTabTimeRange(tab));
    }

    @Test
    public void getQueryPhasePaneOfCurrentTab() {
        final Tab tab = mock(Tab.class);
        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        when(dataAccessTabPane.getCurrentTab()).thenReturn(tab);
        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);

        assertSame(dataAccessTabPane.getQueryPhasePaneOfCurrentTab(), queryPhasePane);
    }

    @Test
    public void preferenceChange() {
        // Also tests handlePluginChange()
        final DataAccessTabPane realDataAccessTabPane = new DataAccessTabPane(dataAccessPane, plugins);
        final Map<String, Pair<Integer, List<DataAccessPlugin>>> mockPlugins = mock(Map.class);

        try (final MockedStatic<DataAccessPaneState> dataAccessPaneStateStatic = Mockito.mockStatic(DataAccessPaneState.class)) {
            dataAccessPaneStateStatic.when(DataAccessPaneState::getPlugins).thenReturn(mockPlugins);

            realDataAccessTabPane.preferenceChange(null);

            assertEquals(realDataAccessTabPane.getPlugins(), mockPlugins);
        }
        
        // Tests for catching exceptions
        try (final MockedStatic<DataAccessPaneState> dataAccessPaneStateStatic = Mockito.mockStatic(DataAccessPaneState.class)) {
            dataAccessPaneStateStatic.when(DataAccessPaneState::getPlugins).thenThrow(InterruptedException.class);

            assertThrows(IllegalStateException.class, () -> {
                realDataAccessTabPane.preferenceChange(null);
            });
        }
        
        try (final MockedStatic<DataAccessPaneState> dataAccessPaneStateStatic = Mockito.mockStatic(DataAccessPaneState.class)) {
            dataAccessPaneStateStatic.when(DataAccessPaneState::getPlugins).thenThrow(ExecutionException.class);

            assertThrows(IllegalStateException.class, () -> {
                realDataAccessTabPane.preferenceChange(null);
            });
        }
    }

    @Test
    public void setPlugins() {
        final DataAccessTabPane realDataAccessTabPane = new DataAccessTabPane(dataAccessPane, plugins);
        final Map<String, Pair<Integer, List<DataAccessPlugin>>> mockPlugins = mock(Map.class);

        realDataAccessTabPane.setPlugins(mockPlugins);
        assertEquals(realDataAccessTabPane.getPlugins(), mockPlugins);
    }

    /**
     * Validates that if one enabled plugin on a tab has an error in one of its parameters then the whole tab is deemed
     * invalid. The method takes two errors that are added to plugin parameters. The errors can be null, indicating that
     * there is no error present.
     *
     * @param error1 the error for plugin parameter 1, or null if no error
     * @param error2 the error for plugin parameter 2, or null if no error
     * @param expected true if the tab was determined to be valid, false otherwise
     */
    private void validateTabEnabledPlugins(final String error1,
            final String error2,
            final boolean expected) {
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane3 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane4 = mock(DataSourceTitledPane.class);

        final PluginParameters pane3PluginParameters = mock(PluginParameters.class);
        final PluginParameters pane4PluginParameters = mock(PluginParameters.class);

        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        final PluginParameter pluginParameter2 = mock(PluginParameter.class);

        final Tab tab = mock(Tab.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class, Mockito.CALLS_REAL_METHODS);) {
            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(same(tab)))
                    .thenReturn(queryPhasePane);

            // Set up out fake query pane that exists within a tab
            when(queryPhasePane.getDataAccessPanes()).thenReturn(
                    List.of(
                            dataSourceTitledPane1,
                            dataSourceTitledPane2,
                            dataSourceTitledPane3,
                            dataSourceTitledPane4
                    )
            );

            // Pane 1 is disabled so it will be ignored during the check
            when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
            when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);
            when(dataSourceTitledPane3.isQueryEnabled()).thenReturn(true);
            when(dataSourceTitledPane4.isQueryEnabled()).thenReturn(true);

            // Pane 2 has no parameters so it will be ignored during the check
            when(dataSourceTitledPane2.getParameters()).thenReturn(null);
            when(dataSourceTitledPane3.getParameters()).thenReturn(pane3PluginParameters);
            when(dataSourceTitledPane4.getParameters()).thenReturn(pane4PluginParameters);

            when(pane3PluginParameters.getParameters()).thenReturn(Map.of(
                    "plugin1", pluginParameter1
            ));
            when(pane4PluginParameters.getParameters()).thenReturn(Map.of(
                    "plugin2", pluginParameter2
            ));

            // Set the passed error values to the plugin parameters
            when(pluginParameter1.getError()).thenReturn(error1);
            when(pluginParameter2.getError()).thenReturn(error2);

            assertEquals(DataAccessTabPane.validateTabEnabledPlugins(tab), expected);
        }
    }

    /**
     * Mocks a tab pane with two tabs. The parameters allow the control of which tabs are considered valid, and which
     * are not.
     *
     * @param tab1HasEnabledPlugins true if tab 1 is meant to have enabled plugins, false otherwise
     * @param validTab1EnabledPlugins true if all of tab 1's enabled plugins are meant to be valid, false otherwise
     * @param tab2HasEnabledPlugins true if tab 2 is meant to have enabled plugins, false otherwise
     * @param validTab2EnabledPlugins true if all of tab 2's enabled plugins are meant to be valid, false otherwise
     * @param expectActiveAndValidPlugins true if the expectation is that both tabs will be considered active and valid
     */
    private void hasActiveAndValidPlugins(final boolean tab1HasEnabledPlugins,
            final boolean validTab1EnabledPlugins,
            final boolean tab2HasEnabledPlugins,
            final boolean validTab2EnabledPlugins,
            final boolean expectActiveAndValidPlugins) {
        // Set up our fake tab pane
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);

        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1,
                tab2
        ));

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            // A tab is active and valid, if it has enabled plugins and non of them
            // has errors in their parameters.
            // All tabs must be active and valid for a result of true to be returned
            datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(same(tab1)))
                    .thenReturn(tab1HasEnabledPlugins);
            datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(same(tab1)))
                    .thenReturn(validTab1EnabledPlugins);

            datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(same(tab2)))
                    .thenReturn(tab2HasEnabledPlugins);
            datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(same(tab2)))
                    .thenReturn(validTab2EnabledPlugins);

            assertEquals(dataAccessTabPane.hasActiveAndValidPlugins(), expectActiveAndValidPlugins);
        }
    }

    /**
     *
     * @param tabHasEnabledPlugins
     * @param validTabEnabledPlugins
     * @param validTabTimeRange
     * @param expected
     */
    private void isTabPaneExecutable(final boolean tabHasEnabledPlugins,
            final boolean validTabEnabledPlugins,
            final boolean validTabTimeRange,
            final boolean expected) {
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);

        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1,
                tab2
        ));

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            setupTabStubs(datPaneMockedStatic, tab1, true, true, true);
            setupTabStubs(datPaneMockedStatic, tab2, tabHasEnabledPlugins, validTabEnabledPlugins, validTabTimeRange);

            assertEquals(dataAccessTabPane.isTabPaneExecutable(), expected);
        }
    }

    /**
     * Verifies that all the tabs are iterated through and their validation checked. Then based on this validation
     * result the menus are updated as needed.
     *
     * @param isExecuteButtonIsGo true if the execute button has the "Go" state set, false otherwise
     * @param isExecuteButtonDisabled true if the execute button is disabled, false otherwise
     * @param expected true if in the cases where a tab has enabled plugins, it is expected that the graph menu items
     * will be enabled
     */
    private void updateTabMenus(final boolean isExecuteButtonIsGo,
            final boolean isExecuteButtonDisabled,
            final boolean hasEnabledPlugins,
            final boolean expectedGraphDepMenuEnablement,
            final boolean expectedPluginDepMenuEnablement) {
        // Set a current graph ID in the state so that other state can be associated
        // with the "current graph"
        DataAccessPaneState.setCurrentGraphId("graphId");

        // Set up our fake button tool bar with an execute button
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
        final Button executeButton = mock(Button.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
        when(buttonToolbar.getExecuteButtonTop()).thenReturn(executeButton);

        // Set up the variable flags that determine what values are passed into
        // updateTabMenu.
        //
        // If the tab has enabled plugins, then the plugin menu items will be enabled 
        // If the execute button is enabled, in the "Go" state and the tab has enabled
        // plugins, then the graph menu items will be enabled
        //
        // The stubs that determine if a tab has enabled plugins is setup
        // below by the calls to setupTabStubs.
        DataAccessPaneState.updateExecuteButtonIsGo(isExecuteButtonIsGo);
        when(executeButton.isDisabled()).thenReturn(isExecuteButtonDisabled);

        // Set up our fake tab pane
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);

        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1
        ));

        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {
            // Stubs out each tab so that every combination is covered for the 3
            // variables that determine if a tab is valid or invalid.
            //
            // A tab is only considered valid if it has enabled plugs, those plugins
            // are valid and the provided date/time range is valid
            datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(tab1))
                    .thenReturn(hasEnabledPlugins);

            // We don't need to test updateTabMenu here so it will just be verified
            // that it was called
            doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());

            // This will always be false because every combination is tested and
            // that involves tabs that will fail validation
            dataAccessTabPane.updateTabMenus();

            // Verify the parameters for each tab's call to updateTabMenu
            verify(dataAccessTabPane).updateTabMenu(
                    tab1,
                    expectedGraphDepMenuEnablement,
                    expectedPluginDepMenuEnablement
            );
        }
    }

    /**
     * These three methods are often used in conjunction to determine the state of the tab. This is a convenience method
     * for setting up these repetitive stubs.
     *
     * @param datPaneMockedStatic a static mock for {@link DataAccessTabPane}
     * @param tab the tab that will be passed into these methods at run time for verification
     * @param tabHasEnabledPlugins true if the tab is meant to have enabled plugins, false otherwise
     * @param validTabEnabledPlugins true if all the tab's enabled plugins are meant to have valid parameters, false
     * otherwise
     * @param validTabTimeRange true if the tabs date range is meant to be valid, false otherwise
     */
    private void setupTabStubs(final MockedStatic<DataAccessTabPane> datPaneMockedStatic,
            final Tab tab,
            final boolean tabHasEnabledPlugins,
            final boolean validTabEnabledPlugins,
            final boolean validTabTimeRange) {
        datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(tab)).thenReturn(tabHasEnabledPlugins);
        datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(tab)).thenReturn(validTabEnabledPlugins);
        datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabTimeRange(tab)).thenReturn(validTabTimeRange);
    }

    /**
     * Verifies that creating a new tab adds it to the end of the existing tabs. That appropriate menus are added to the
     * new tab and that those menus and all the button statuses are refreshed based on the initial state.
     *
     * @param tabHasEnabledPlugins true if the new tab will have enabled plugins, false otherwise
     * @param isExecuteButtonIsGo true if the execute button has the "Go" state set, false otherwise
     * @param isExecuteButtonDisabled true if the execute button is disabled, false otherwise
     * @param expectGraphDependentMenuItemsEnabled true if after the new tab is created, the graph dependent menu items
     * should be enabled, false otherwise
     * @param expectPluginDependentMenuItemsEnabled true if after the new tab is created, the plugin dependent menu
     * items should be enabled, false otherwise
     */
    private void verifyNewTab(final boolean tabHasEnabledPlugins,
            final boolean isExecuteButtonIsGo,
            final boolean isExecuteButtonDisabled,
            final boolean expectGraphDependentMenuItemsEnabled,
            final boolean expectPluginDependentMenuItemsEnabled) {
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final EventHandler<Event> onCloseEventHandler = mock(EventHandler.class);

        final MenuItem runMenuItem = mock(MenuItem.class);
        final MenuItem runFromHereMenuItem = mock(MenuItem.class);
        final MenuItem runToHereMenuItem = mock(MenuItem.class);
        final MenuItem deactivateAllPluginsMenuItem = mock(MenuItem.class);

        final ContextMenu contextMenu = mock(ContextMenu.class);

        // We don't need to test updateTabMenu here so it will just be verified
        // that it was called
        doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());

        // Set up our fake tab pane
        final TabPane tabPane = mock(TabPane.class);
        final ObservableList<Tab> tabs = FXCollections.observableArrayList();

        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        when(tabPane.getTabs()).thenReturn(tabs);

        // Set up our fake button tool bar with an execute button
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
        final Button executeButton = mock(Button.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
        when(buttonToolbar.getExecuteButtonTop()).thenReturn(executeButton);

        final Label mockedLabel = mock(Label.class);
        final Label mockedDefaultCaptionLabel = mock(Label.class);

        try (
                // In order to ensure the correct behaviour is applied to the new
                // tab we intercept its creation and return a mock.
                final MockedConstruction<Tab> mockedTab
                = Mockito.mockConstruction(Tab.class, (tabMock, cntxt) -> {

                    when(tabMock.getOnClosed()).thenReturn(onCloseEventHandler);
                    when(tabMock.getGraphic()).thenReturn(mockedLabel);
                    when(mockedLabel.getGraphic()).thenReturn(mockedDefaultCaptionLabel);

                }); //  Intercept the Label creation and insert our own mock
                 final MockedConstruction<Label> mockedConstructionLabel = Mockito.mockConstruction(Label.class, (labelMock, cntxt) -> {
                }); // When a tab is created, it is given a new context menu. We want to intercept
                // that creation and insert our own mock
                 final MockedConstruction<TabContextMenu> mockedTabContextMenu
                = Mockito.mockConstruction(TabContextMenu.class, (contextMenuMock, cntxt) -> {
                    final List<Object> expectedArgs = new ArrayList<>();
                    expectedArgs.add(dataAccessTabPane);
                    expectedArgs.add(mockedTab.constructed().get(0));

                    assertEquals(cntxt.arguments(), expectedArgs);

                    when(contextMenuMock.getContextMenu()).thenReturn(contextMenu);

                    when(contextMenuMock.getRunMenuItem()).thenReturn(runMenuItem);
                    when(contextMenuMock.getRunFromHereMenuItem()).thenReturn(runFromHereMenuItem);
                    when(contextMenuMock.getRunToHereMenuItem()).thenReturn(runToHereMenuItem);
                    when(contextMenuMock.getDeactivateAllPluginsMenuItem())
                            .thenReturn(deactivateAllPluginsMenuItem);
                }); final MockedStatic<DataAccessPaneState> dapMockedStatic
                = Mockito.mockStatic(DataAccessPaneState.class); final MockedStatic<DataAccessTabPane> datpStateMockedStatic
                = Mockito.mockStatic(DataAccessTabPane.class);) {

            // Set up the variable flags that determine what values are passed into
            // updateTabMenu.
            //
            // If the tab has enabled plugins, then the plugin menu items will be enabled 
            // If the execute button is enabled, in the "Go" state and the tab has enabled
            // plugins, then the graph menu items will be enabled
            dapMockedStatic.when(DataAccessPaneState::isExecuteButtonIsGo).thenReturn(isExecuteButtonIsGo);
            datpStateMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(any(Tab.class)))
                    .thenReturn(tabHasEnabledPlugins);
            when(executeButton.isDisabled()).thenReturn(isExecuteButtonDisabled);

            dataAccessTabPane.newTab(queryPhasePane, newStepCaption);

            // Verify the tab and context menus were created
            assertEquals(mockedTab.constructed().size(), 1);
            assertEquals(mockedTabContextMenu.constructed().size(), 1);
            assertEquals(mockedConstructionLabel.constructed().size(), 2);

            // Verify that the context menu was created correctly
            final TabContextMenu newTabContextMenu = mockedTabContextMenu.constructed().get(0);

            verify(newTabContextMenu).init();

            verify(queryPhasePane).addGraphDependentMenuItems(runMenuItem, runFromHereMenuItem,
                    runToHereMenuItem);
            verify(queryPhasePane).addPluginDependentMenuItems(deactivateAllPluginsMenuItem);

            // Verify that the tab was created correctly
            final Tab newTab = mockedTab.constructed().get(0);

            // Verify that the label was created correctly
            final Label newLabel = mockedConstructionLabel.constructed().get(0);

            verify(newTab).setContextMenu(contextMenu);
            verify(newTab).setClosable(true);

            final ArgumentCaptor<ScrollPane> scrollPaneCaptor = ArgumentCaptor.forClass(ScrollPane.class);
            verify(newTab).setContent(scrollPaneCaptor.capture());
            assertTrue(scrollPaneCaptor.getValue().isFitToWidth());
            assertEquals(scrollPaneCaptor.getValue().getContent(), queryPhasePane);
            assertEquals(scrollPaneCaptor.getValue().getStyle(), "-fx-background-color: black;");

            final ArgumentCaptor<Tooltip> tooltipCaptor = ArgumentCaptor.forClass(Tooltip.class);
            verify(newTab).setTooltip(tooltipCaptor.capture());
            assertEquals(tooltipCaptor.getValue().getText(), "Right click for more options");

            // Ensure the tab menus states were updated
            verify(dataAccessTabPane).updateTabMenu(
                    newTab, expectGraphDependentMenuItemsEnabled, expectPluginDependentMenuItemsEnabled);

            // Verify that the tab pane has the correct tabs
            assertEquals(tabs, FXCollections.observableArrayList(newTab));

            // Verify the on close event for the new tab behaves as expected
            final ArgumentCaptor<EventHandler<Event>> onCloseCaptor
                    = ArgumentCaptor.forClass(EventHandler.class);
            verify(newTab).setOnClosed(onCloseCaptor.capture());

            final Event event = mock(Event.class);
            onCloseCaptor.getValue().handle(event);

            verify(mockedDefaultCaptionLabel).setText("Step 1");
            verify(onCloseEventHandler).handle(event);

            // Verify the on Mouse Clicked event for the new Label behaves as expected
            final ArgumentCaptor<EventHandler<Event>> onClickCaptor
                    = ArgumentCaptor.forClass(EventHandler.class);
            verify(newLabel).setOnMouseClicked(onClickCaptor.capture());

            final MouseEvent clickEvent = mock(MouseEvent.class);
            onClickCaptor.getValue().handle(clickEvent);

            verify(dataAccessTabPane).labelClickEvent(newTab, newLabel, clickEvent);
        }
    }
}
