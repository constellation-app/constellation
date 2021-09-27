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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessTabPaneNGTest {
    private DataAccessViewTopComponent topComponent;
    private DataAccessPane dataAccessPane;
    private Map<String, List<DataAccessPlugin>> plugins;
    
    private DataAccessTabPane dataAccessTabPane;
    
    public DataAccessTabPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        FxToolkit.registerPrimaryStage();
        
        dataAccessPane = mock(DataAccessPane.class);
        topComponent = mock(DataAccessViewTopComponent.class);
        
        when(dataAccessPane.getParentComponent()).thenReturn(topComponent);
        when(topComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());
        
        plugins = Map.of();
        
        dataAccessTabPane = spy(new DataAccessTabPane(dataAccessPane, plugins));
        
        DataAccessPaneState.clearState();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.cleanupStages();
    }
    
    @Test
    public void init() {
        assertSame(dataAccessTabPane.getDataAccessPane(), dataAccessPane);
        assertSame(dataAccessTabPane.getPlugins(), plugins);
        
        assertNotNull(dataAccessTabPane.getTabPane());
        assertEquals(dataAccessTabPane.getTabPane().getSide(), Side.TOP);
    }
    
    @Test
    public void newTab() {
        try (final MockedConstruction<QueryPhasePane> mockedQueryPhasePane =
                Mockito.mockConstruction(QueryPhasePane.class, (queryPhasePaneMock, cntxt) -> {
                    final List<Object> expectedArgs = new ArrayList<>();
                    expectedArgs.add(plugins);
                    expectedArgs.add(dataAccessPane);
                    expectedArgs.add(null);
                    
                    assertEquals(cntxt.arguments(), expectedArgs);
        })) {
            doNothing().when(dataAccessTabPane).newTab(any(QueryPhasePane.class));
            
            final QueryPhasePane pane = dataAccessTabPane.newTab();
            
            assertNotNull(pane);
            
            assertEquals(mockedQueryPhasePane.constructed().size(), 1);
            assertSame(mockedQueryPhasePane.constructed().get(0), pane);
            
            verify(dataAccessTabPane).newTab(pane);
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
    public void runTabs() {
        final String graphId = "graphId";
        
        try (
                final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
                final MockedStatic<CompletableFuture> futureMockedStatic = Mockito.mockStatic(CompletableFuture.class, Mockito.CALLS_REAL_METHODS);
        ) {
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph graph = mock(Graph.class);
            
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            when(graph.getId()).thenReturn(graphId);
            
            doNothing().when(dataAccessTabPane).storeParameterValues();
            
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final Tab tab2 = mock(Tab.class);
            final Tab tab3 = mock(Tab.class);
            final Tab tab4 = mock(Tab.class);
            final Tab tab5 = mock(Tab.class);
            
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            
            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(any(Tab.class))).thenReturn(queryPhasePane);
            
            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1,
                    tab2,
                    tab3,
                    tab4,
                    tab5
            ));

            final List<Future<?>> barrier1 = List.of(CompletableFuture.completedFuture("Future 1 Complete"));
            final List<Future<?>> barrier2 = List.of(CompletableFuture.completedFuture("Future 2 Complete"));
            final List<Future<?>> barrier3 = List.of(CompletableFuture.completedFuture("Future 3 Complete"));
            
            when(queryPhasePane.runPlugins(null)).thenReturn(barrier1);
            when(queryPhasePane.runPlugins(barrier1)).thenReturn(barrier2);
            when(queryPhasePane.runPlugins(barrier2)).thenReturn(barrier3);
            
            futureMockedStatic.when(() -> CompletableFuture.runAsync(any(Runnable.class), any(ExecutorService.class)))
                    .thenAnswer(iom ->{
                        WaitForQueriesToCompleteTask task = iom.getArgument(0);
                        
                        assertEquals(task.getDataAccessPane(), dataAccessPane);
                        assertEquals(task.getGraphId(), graphId);
                        
                        return null;
            });
            
            dataAccessTabPane.runTabs(1, 3);

            verify(dataAccessPane).setExecuteButtonToStop();
            verify(dataAccessTabPane).storeParameterValues();
            
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab1), never());
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab2));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab3));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab4));
            datPaneMockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(tab5), never());
            
            verify(queryPhasePane, times(1)).runPlugins(isNull());
            verify(queryPhasePane, times(2)).runPlugins(any(List.class));
            
            verify(queryPhasePane).runPlugins(null);
            verify(queryPhasePane).runPlugins(barrier1);
            verify(queryPhasePane).runPlugins(barrier2);
            
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
    public void updateTabMenus_test_different_logic_paths() {
        // Second param to updateTabMenu will only be true if the execute button
        // "isGo" and is NOT disabled. And only for the cases where a tab has enabled plugins
        CompletableFuture.runAsync(() -> updateTabMenus(true, false, true));
        
        // All other combinations result in the second parameter being false.
        CompletableFuture.runAsync(() -> updateTabMenus(true, true, false));
        CompletableFuture.runAsync(() -> updateTabMenus(false, true, false));
        CompletableFuture.runAsync(() -> updateTabMenus(false, false, false));
    }
    
    @Test
    public void updateTabMenus_all_valid() {
        // This runAsync is a bit of a hack I found on the ticket to fix static
        // mockito not being passed to sunsequent threads. Wrapping the code in
        // this apparently fixes it.
        CompletableFuture.runAsync(() -> {
            DataAccessPaneState.setCurrentGraphId("graphId");
            DataAccessPaneState.updateExecuteButtonIsGo(true);

            final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
            final Button executeButton = mock(Button.class);

            when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
            when(buttonToolbar.getExecuteButton()).thenReturn(executeButton);
            when(executeButton.isDisabled()).thenReturn(false);

            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1
            ));

            try (
                    final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
            ) {
                setupTabStubs(datPaneMockedStatic, tab1, true, true, true);

                doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());

                // All tabs are valid. So should be true
                assertTrue(dataAccessTabPane.updateTabMenus());

                verify(dataAccessTabPane).updateTabMenu(tab1, true, true);
            }
        });
    }
    
    @Test
    public void storeParameterValues_verify_global_params_stored() {
        CompletableFuture.runAsync(() -> {
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final ScrollPane scrollPane = mock(ScrollPane.class);
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
            final PluginParameters pluginParameters = mock(PluginParameters.class);

            final PluginParameter pluginParameter1 = mock(PluginParameter.class);
            final PluginParameter pluginParameter2 = mock(PluginParameter.class);
            final PluginParameter pluginParameter3 = mock(PluginParameter.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1
            ));
            when(tab1.getContent()).thenReturn(scrollPane);
            when(scrollPane.getContent()).thenReturn(queryPhasePane);

            when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
            when(globalParametersPane.getParams()).thenReturn(pluginParameters);
            when(pluginParameters.getParameters()).thenReturn(Map.of(
                    "plugin1", pluginParameter1,
                    "plugin2", pluginParameter2,
                    "plugin3", pluginParameter3

            ));

            when(pluginParameter1.getStringValue()).thenReturn(null);
            when(pluginParameter2.getStringValue()).thenReturn(" ");
            when(pluginParameter3.getStringValue()).thenReturn("PluginParam3 String Value");

            try (
                    final MockedStatic<RecentParameterValues> recentParamValsMockedStatic = Mockito.mockStatic(RecentParameterValues.class);
            ) {
                dataAccessTabPane.storeParameterValues();

                recentParamValsMockedStatic.verify(() -> 
                        RecentParameterValues.storeRecentValue("plugin3", "PluginParam3 String Value"));
            }
        });
    }
    
    @Test
    public void storeParameterValues_verify_plugin_params_stored() {
        CompletableFuture.runAsync(() -> {
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final ScrollPane scrollPane = mock(ScrollPane.class);
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
            final PluginParameters globalPluginParameters = mock(PluginParameters.class);
            
            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1
            ));
            when(tab1.getContent()).thenReturn(scrollPane);
            when(scrollPane.getContent()).thenReturn(queryPhasePane);
            
            // This just needs to be set up to prevent null pointers
            when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
            when(globalParametersPane.getParams()).thenReturn(globalPluginParameters);
            when(globalPluginParameters.getParameters()).thenReturn(Map.of());
            
            final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
            final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
            final DataSourceTitledPane dataSourceTitledPane3 = mock(DataSourceTitledPane.class);
            
            final List<DataSourceTitledPane> dataSourceTitledPanes = new ArrayList<>();
            dataSourceTitledPanes.add(dataSourceTitledPane1);
            dataSourceTitledPanes.add(dataSourceTitledPane2);
            dataSourceTitledPanes.add(dataSourceTitledPane3);
            
            when(queryPhasePane.getDataAccessPanes()).thenReturn(dataSourceTitledPanes);
            
            final PluginParameters pane2PluginParameters = mock(PluginParameters.class);
            final PluginParameters pane3PluginParameters = mock(PluginParameters.class);
            
            when(dataSourceTitledPane1.getParameters()).thenReturn(null);
            when(dataSourceTitledPane2.getParameters()).thenReturn(pane2PluginParameters);
            when(dataSourceTitledPane3.getParameters()).thenReturn(pane3PluginParameters);
            
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
            
            when(pluginParameter1.getObjectValue()).thenReturn(null);
            
            when(pluginParameter2.getType()).thenReturn(LocalDateParameterType.INSTANCE);
            when(pluginParameter2.getObjectValue()).thenReturn("PluginParam2 Object Value");
            when(pluginParameter2.getStringValue()).thenReturn("PluginParam2 String Value");
            
            when(pluginParameter3.getObjectValue()).thenReturn("PluginParam3 Object Value");
            
            try (
                    final MockedStatic<RecentParameterValues> recentParamValsMockedStatic = Mockito.mockStatic(RecentParameterValues.class);
            ) {
                dataAccessTabPane.storeParameterValues();
            
                recentParamValsMockedStatic.verify(() -> RecentParameterValues.storeRecentValue("plugin2", "PluginParam2 String Value"));
                recentParamValsMockedStatic.verify(() -> RecentParameterValues.storeRecentValue("plugin3", "PluginParam3 Object Value"));
            }
        });
    }
    
    @Test
    public void updateTabMen() {
        final Tab tab = mock(Tab.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        
        try (
                    final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
            ) {
            datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab)).thenReturn(queryPhasePane);
            
            dataAccessTabPane.updateTabMenu(tab, true, true);
            
            verify(queryPhasePane).enableGraphDependentMenuItems(true);
            verify(queryPhasePane).enablePluginDependentMenuItems(true);
        }        
    }
    
    @Test
    public void isTabPaneExecutable_true() {
        CompletableFuture.runAsync(() -> {
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final Tab tab2 = mock(Tab.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1,
                    tab2
            ));

            try (
                    final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
            ) {
                setupTabStubs(datPaneMockedStatic, tab1, true, true, true);
                setupTabStubs(datPaneMockedStatic, tab2, true, true, true);

                assertTrue(dataAccessTabPane.isTabPaneExecutable());
            }
        });
    }
    
    @Test
    public void isTabPaneExecutable_false() {
        CompletableFuture.runAsync(() -> {
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final Tab tab2 = mock(Tab.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1,
                    tab2
            ));

            try (
                    final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
            ) {
                setupTabStubs(datPaneMockedStatic, tab1, true, true, true);
                setupTabStubs(datPaneMockedStatic, tab2, false, true, true);

                assertFalse(dataAccessTabPane.isTabPaneExecutable());
            }
        });
    }
    
    @Test
    public void hasActiveAndValidPlugins() {
        hasActiveAndValidPlugins(true, true, true, true, false);
        hasActiveAndValidPlugins(true, false, true, false, true);
        hasActiveAndValidPlugins(true, false, true, true, true);
        hasActiveAndValidPlugins(true, true, true, false, true);
        hasActiveAndValidPlugins(false, false, false, false, false);
        hasActiveAndValidPlugins(false, false, true, false, false);
        hasActiveAndValidPlugins(true, false, false, false, false);
    }
    
    @Test
    public void tabHasEnabledPlugins() {
        CompletableFuture.runAsync(() -> {
            final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
            final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);

            final Tab tab = mock(Tab.class);
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

            try (
                        final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class, Mockito.CALLS_REAL_METHODS);
                ) {
                datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab)).thenReturn(queryPhasePane);

                when(queryPhasePane.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
                when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
                when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);

                assertTrue(DataAccessTabPane.tabHasEnabledPlugins(tab));
            } 
        });
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
    
    public void validateTabEnabledPlugins(final String error1,
                                          final String error2,
                                          final boolean expected) {
        CompletableFuture.runAsync(() -> {
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
                        final MockedStatic<DataAccessTabPane> datPaneMockedStatic =
                                Mockito.mockStatic(DataAccessTabPane.class, Mockito.CALLS_REAL_METHODS);
                ) {
                datPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab))
                        .thenReturn(queryPhasePane);

                when(queryPhasePane.getDataAccessPanes()).thenReturn(
                        List.of(dataSourceTitledPane1, dataSourceTitledPane2)
                );
                when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
                when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);
                when(dataSourceTitledPane3.isQueryEnabled()).thenReturn(true);
                when(dataSourceTitledPane4.isQueryEnabled()).thenReturn(true);

                when(dataSourceTitledPane2.getParameters()).thenReturn(null);
                when(dataSourceTitledPane3.getParameters()).thenReturn(pane3PluginParameters);
                when(dataSourceTitledPane4.getParameters()).thenReturn(pane4PluginParameters);
                
                when(pane3PluginParameters.getParameters()).thenReturn(Map.of(
                        "plugin1", pluginParameter1
                ));
                when(pane4PluginParameters.getParameters()).thenReturn(Map.of(
                        "plugin2", pluginParameter2
                ));
                
                when(pluginParameter1.getError()).thenReturn(error1);
                when(pluginParameter2.getError()).thenReturn(error2);
                
                assertEquals(DataAccessTabPane.validateTabEnabledPlugins(tab), expected);
            } 
        });
    }
    
    public void hasActiveAndValidPlugins(final boolean tab1HasEnabledPlugins,
                                         final boolean validTab1EnabledPlugins,
                                         final boolean tab2HasEnabledPlugins,
                                         final boolean validTab2EnabledPlugins,
                                         final boolean expectActiveAndValidPlugins) {
        CompletableFuture.runAsync(() -> {
            final TabPane tabPane = mock(TabPane.class);
            final Tab tab1 = mock(Tab.class);
            final Tab tab2 = mock(Tab.class);

            doReturn(tabPane).when(dataAccessTabPane).getTabPane();
            when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                    tab1,
                    tab2
            ));

            try (
                    final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
            ) {
                datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(tab1))
                        .thenReturn(tab1HasEnabledPlugins);
                datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(tab1))
                        .thenReturn(validTab2EnabledPlugins);
                
                datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(tab2))
                        .thenReturn(tab2HasEnabledPlugins);
                datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(tab2))
                        .thenReturn(validTab2EnabledPlugins);

                assertEquals(dataAccessTabPane.hasActiveAndValidPlugins(), expectActiveAndValidPlugins);
            }
        });
    }
    
    private void updateTabMenus(final boolean isExecuteButtonIsGo,
                                final boolean isExecuteButtonDisabled,
                                final boolean expected) {
        DataAccessPaneState.setCurrentGraphId("graphId");
        DataAccessPaneState.updateExecuteButtonIsGo(isExecuteButtonIsGo);
        
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
        final Button executeButton = mock(Button.class);
        
        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
        when(buttonToolbar.getExecuteButton()).thenReturn(executeButton);
        when(executeButton.isDisabled()).thenReturn(isExecuteButtonDisabled);
        
        final TabPane tabPane = mock(TabPane.class);
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);
        final Tab tab3 = mock(Tab.class);
        final Tab tab4 = mock(Tab.class);
        final Tab tab5 = mock(Tab.class);
        final Tab tab6 = mock(Tab.class);
        final Tab tab7 = mock(Tab.class);
        final Tab tab8 = mock(Tab.class);

        doReturn(tabPane).when(dataAccessTabPane).getTabPane();
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(
                tab1,
                tab2,
                tab3,
                tab4,
                tab5,
                tab6,
                tab7,
                tab8
        ));
        
        try (
                final MockedStatic<DataAccessTabPane> datPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
        ) {
            setupTabStubs(datPaneMockedStatic, tab1, true, true, true);
            setupTabStubs(datPaneMockedStatic, tab2, true, false, false);
            setupTabStubs(datPaneMockedStatic, tab3, true, false, true);
            setupTabStubs(datPaneMockedStatic, tab4, true, true, false);
            setupTabStubs(datPaneMockedStatic, tab5, false, false, true);
            setupTabStubs(datPaneMockedStatic, tab6, false, true, false);
            setupTabStubs(datPaneMockedStatic, tab7, false, false, true);
            setupTabStubs(datPaneMockedStatic, tab8, false, false, false);
            
            doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());
            
            // This will always be false because every combination is tested and
            // that involves tabs that will fail validation
            assertFalse(dataAccessTabPane.updateTabMenus());
 
            verify(dataAccessTabPane).updateTabMenu(tab1, expected, true);
            verify(dataAccessTabPane).updateTabMenu(tab2, expected, true);
            verify(dataAccessTabPane).updateTabMenu(tab3, expected, true);
            verify(dataAccessTabPane).updateTabMenu(tab4, expected, true);
            
            // The following will always have false in the first param as
            // hasEnabledPlugins has been set to false for them
            verify(dataAccessTabPane).updateTabMenu(tab5, false, false);
            verify(dataAccessTabPane).updateTabMenu(tab6, false, false);
            verify(dataAccessTabPane).updateTabMenu(tab7, false, false);
            verify(dataAccessTabPane).updateTabMenu(tab8, false, false);
        }
    }
    
    private void setupTabStubs(final MockedStatic<DataAccessTabPane> datPaneMockedStatic,
                               final Tab tab,
                               final boolean tabHasEnabledPlugins,
                               final boolean validTabEnabledPlugins,
                               final boolean validTabTimeRange) {
        datPaneMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(tab)).thenReturn(tabHasEnabledPlugins);
        datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabEnabledPlugins(tab)).thenReturn(validTabEnabledPlugins);
        datPaneMockedStatic.when(() -> DataAccessTabPane.validateTabTimeRange(tab)).thenReturn(validTabTimeRange);
    }
    
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
        
        final TabPane tabPane = mock(TabPane.class);
        final ObservableList<Tab> tabs = FXCollections.observableArrayList();
        
        doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());
        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        when(tabPane.getTabs()).thenReturn(tabs);
        
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
        final Button executeButton = mock(Button.class);
        
        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
        when(buttonToolbar.getExecuteButton()).thenReturn(executeButton);
        when(executeButton.isDisabled()).thenReturn(isExecuteButtonDisabled);
        
        try (
                final MockedConstruction<Tab> mockedTab =
                        Mockito.mockConstruction(Tab.class, (tabMock, cntxt) -> {
                            final List<Object> expectedArgs = new ArrayList<>();
                            expectedArgs.add("Step 1");

                            assertEquals(cntxt.arguments(), expectedArgs);

                            when(tabMock.getOnClosed()).thenReturn(onCloseEventHandler);
                });
                final MockedConstruction<TabContextMenu> mockedTabContextMenu =
                        Mockito.mockConstruction(TabContextMenu.class, (contextMenuMock, cntxt) -> {
                            final List<Object> expectedArgs = new ArrayList<>();
                            expectedArgs.add(dataAccessTabPane);
                            expectedArgs.add(mockedTab.constructed().get(0));

                            assertEquals(cntxt.arguments(), expectedArgs);
                            
                            when(contextMenuMock.getContextMenu()).thenReturn(contextMenu);
                            
                            when(contextMenuMock.getRunMenuItem()).thenReturn(runMenuItem);
                            when(contextMenuMock.getRunFromHereMenuItem()).thenReturn(runFromHereMenuItem);
                            when(contextMenuMock.getRunToHereMenuItem()).thenReturn(runToHereMenuItem);
                            when(contextMenuMock.getDeactivateAllPluginsMenuItem()).thenReturn(deactivateAllPluginsMenuItem);
                });
                final MockedStatic<DataAccessPaneState> dapMockedStatic =
                        Mockito.mockStatic(DataAccessPaneState.class);
                final MockedStatic<DataAccessTabPane> datpStateMockedStatic =
                        Mockito.mockStatic(DataAccessTabPane.class);
                
            ) {
            
            dapMockedStatic.when(DataAccessPaneState::isExecuteButtonIsGo).thenReturn(isExecuteButtonIsGo);
            datpStateMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(any(Tab.class))).thenReturn(tabHasEnabledPlugins);
            
            dataAccessTabPane.newTab(queryPhasePane);
            
            assertEquals(mockedTab.constructed().size(), 1);
            assertEquals(mockedTabContextMenu.constructed().size(), 1);

            final TabContextMenu newTabContextMenu = mockedTabContextMenu.constructed().get(0);
            
            verify(newTabContextMenu).init();
            
            verify(queryPhasePane).addGraphDependentMenuItems(runMenuItem, runFromHereMenuItem, runToHereMenuItem);
            verify(queryPhasePane).addPluginDependentMenuItems(deactivateAllPluginsMenuItem);
            
            final Tab newTab = mockedTab.constructed().get(0);
            
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

            verify(dataAccessTabPane).updateTabMenu(newTab, expectGraphDependentMenuItemsEnabled, expectPluginDependentMenuItemsEnabled);
            
            assertEquals(tabs, FXCollections.observableArrayList(newTab));
            
            final ArgumentCaptor<EventHandler<Event>> onCloseCaptor = ArgumentCaptor.forClass(EventHandler.class);
            verify(newTab).setOnClosed(onCloseCaptor.capture());
            
            final Event event = mock(Event.class);
            onCloseCaptor.getValue().handle(event);
            
            verify(newTab).setText("Step 1");
            verify(onCloseEventHandler).handle(event);
        }
    }
}
