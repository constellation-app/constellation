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
package au.gov.asd.tac.constellation.views.dataaccess.listeners;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.create.NewDefaultSchemaGraphAction;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessPreferencesIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.swing.Icon;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ExecuteListenerNGTest {
    private static final String GRAPH_ID = "graphId";
    
    private static MockedStatic<GraphManager> graphManagerMockedStatic;
    private static MockedStatic<DataAccessTabPane> dataAccessTabPaneMockedStatic;
    private static MockedStatic<PluginExecution> pluginExecutionMockedStatic;
    private static MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic;
    private static MockedStatic<StatusDisplayer> statusDisplayerMockedStatic;
    private static MockedStatic<DataAccessPreferencesIoProvider> daIOProviderMockedStatic;
    private static MockedStatic<CompletableFuture> completableFutureMockedStatic;
    
    private static MockedConstruction<NewDefaultSchemaGraphAction> graphActionMockedConstruction;
    
    private DataAccessViewTopComponent dataAccessViewTopComponent;
    private DataAccessPane dataAccessPane;
    private DataAccessTabPane dataAccessTabPane;
    
    private TabPane tabPane;
    
    private GraphManager graphManager;
    private Graph activeGraph;
    
    private NotificationDisplayer notificationDisplayer;
    
    private StatusDisplayer statusDisplayer;
    
    private PluginExecution pluginExecution;
    
    private ExecuteListener executeListener;
    
    public ExecuteListenerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);
        dataAccessTabPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
        pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class);
        notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);
        statusDisplayerMockedStatic = Mockito.mockStatic(StatusDisplayer.class);
        daIOProviderMockedStatic = Mockito.mockStatic(DataAccessPreferencesIoProvider.class);
        completableFutureMockedStatic = Mockito.mockStatic(CompletableFuture.class, Mockito.CALLS_REAL_METHODS);
        
        graphActionMockedConstruction = Mockito.mockConstruction(NewDefaultSchemaGraphAction.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        graphManagerMockedStatic.close();
        dataAccessTabPaneMockedStatic.close();
        pluginExecutionMockedStatic.close();
        notificationDisplayerMockedStatic.close();
        statusDisplayerMockedStatic.close();
        daIOProviderMockedStatic.close();
        completableFutureMockedStatic.close();
        
        graphActionMockedConstruction.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        FxToolkit.registerPrimaryStage();
        
        dataAccessViewTopComponent = mock(DataAccessViewTopComponent.class);
        dataAccessPane = mock(DataAccessPane.class);
        dataAccessTabPane = mock(DataAccessTabPane.class);
        tabPane = mock(TabPane.class);
        graphManager = mock(GraphManager.class);
        activeGraph = mock(Graph.class);
        
        when(dataAccessPane.getParentComponent()).thenReturn(dataAccessViewTopComponent);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        when(dataAccessViewTopComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());
        
        executeListener = new ExecuteListener(dataAccessPane);
        
        graphManagerMockedStatic.when(GraphManager::getDefault)
                .thenReturn(graphManager);
        when(graphManager.getActiveGraph()).thenReturn(activeGraph);
        when(activeGraph.getId()).thenReturn(GRAPH_ID);

        // Intercept the plugin execution run calls and run the plugins manually
        // so that it executes within the same thread and sequentially for the test
        pluginExecution = mock(PluginExecution.class);
        
        pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(SimplePlugin.class)))
                .thenAnswer(iom -> {
                    final SimplePlugin plugin = iom.getArgument(0);

                    final PluginGraphs graphs = mock(PluginGraphs.class);
                    when(graphs.getGraph()).thenReturn(null);
                    
                    final PluginInteraction pluginInteraction = mock(PluginInteraction.class);
                    final PluginParameters pluginParameters = mock(PluginParameters.class);
                    
                    // This will call the execute method of the simple plugin
                    plugin.run(graphs, pluginInteraction, pluginParameters);

                    return pluginExecution;
                });
        
        // Intercept calls to start the wait for tasks so that they don't run
        completableFutureMockedStatic.when(() -> 
                CompletableFuture.runAsync(
                        any(WaitForQueriesToCompleteTask.class),
                        any(ExecutorService.class))
        ).thenReturn(null);
        
        notificationDisplayer = mock(NotificationDisplayer.class);
        notificationDisplayerMockedStatic.when(NotificationDisplayer::getDefault)
                .thenReturn(notificationDisplayer);
        when(notificationDisplayer.notify(anyString(), any(Icon.class), anyString(), isNull()))
                .thenReturn(null);
        
        statusDisplayer = mock(StatusDisplayer.class);
        statusDisplayerMockedStatic.when(StatusDisplayer::getDefault).thenReturn(statusDisplayer);
        
        DataAccessPaneState.clearState();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.cleanupStages();
        
        graphManagerMockedStatic.reset();
        dataAccessTabPaneMockedStatic.reset();
        pluginExecutionMockedStatic.reset();
        notificationDisplayerMockedStatic.reset();
        statusDisplayerMockedStatic.reset();
        daIOProviderMockedStatic.reset();
        completableFutureMockedStatic.reset();
    }
    
    @Test
    public void handle_sunny_days() {
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DataAccessPreferenceUtilities.setDataAccessResultsDir(tmpDir);
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(true);
        
        when(dataAccessTabPane.hasActiveAndValidPlugins()).thenReturn(true);
        
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(tab1, tab2));
        
        final QueryPhasePane queryPhasePane1 = mock(QueryPhasePane.class);
        final QueryPhasePane queryPhasePane2 = mock(QueryPhasePane.class);
        
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.getQueryPhasePane(tab1)).thenReturn(queryPhasePane1);
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.getQueryPhasePane(tab2)).thenReturn(queryPhasePane2);

        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.tabHasEnabledPlugins(tab1)).thenReturn(false);
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.tabHasEnabledPlugins(tab2)).thenReturn(true);
        
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        when(queryPhasePane1.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1));
        
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane3 = mock(DataSourceTitledPane.class);
        when(queryPhasePane2.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane2, dataSourceTitledPane3));
        
        final List<Future<?>> barrier1 = List.of(CompletableFuture.completedFuture("First tab run"));
        
        doReturn(barrier1).when(queryPhasePane1).runPlugins(null);
        doReturn(null).when(queryPhasePane2).runPlugins(barrier1);
        
        final ActionEvent event = mock(ActionEvent.class);
        
        try (
                MockedConstruction<WaitForQueriesToCompleteTask> waitForMockedConstruction =
                        Mockito.mockConstruction(WaitForQueriesToCompleteTask.class, (waitForMock, cntxt) -> {
                            assertEquals(cntxt.arguments(), List.of(dataAccessPane, GRAPH_ID));
                        });
        ) {
            executeListener.handle(event);

            assertEquals(graphActionMockedConstruction.constructed().size(), 1);
            
            verify(dataAccessPane).setExecuteButtonToStop();
            verify(statusDisplayer).setStatusText("Data access results will be written to " + tmpDir.getAbsolutePath());

            daIOProviderMockedStatic.verify(() -> DataAccessPreferencesIoProvider.saveDataAccessState(tabPane, activeGraph));

            verify(pluginExecution).executeLater(null);

            verify(queryPhasePane1).runPlugins(or(anyList(), isNull()));
            verify(queryPhasePane2).runPlugins(or(anyList(), isNull()));
            verify(queryPhasePane1).runPlugins(null);
            verify(queryPhasePane2).runPlugins(barrier1);

            assertEquals(waitForMockedConstruction.constructed().size(), 1);
            
            completableFutureMockedStatic.verify(() -> CompletableFuture
                    .runAsync(
                            same(waitForMockedConstruction.constructed().get(0)),
                            any(ExecutorService.class)
                    )
            );
            
            verifyNoInteractions(dataSourceTitledPane1);
            verify(dataSourceTitledPane2).validityChanged(false);
            verify(dataSourceTitledPane3).validityChanged(false);
            
            verifyNoInteractions(event);
            
            assertTrue(DataAccessPaneState.isQueriesRunning());
        }
    }
    
    @Test
    public void handle_results_dir_does_not_exist() {
        // Because the DataAccessPreferenceUtilities setter has null and non-dir
        // checks we have to manually add this into the preferences.
        final File tmpDir = new File("/SOMETHING_THAT_WOULD_NEVER_EXIST");
        Preferences prefs = NbPreferences.forModule(DataAccessPreferenceUtilities.class);
        prefs.put("saveDataDir", tmpDir.getAbsolutePath());
        
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        
        final Tab tab1 = mock(Tab.class);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(tab1));
        
        final QueryPhasePane queryPhasePane1 = mock(QueryPhasePane.class);
        
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.getQueryPhasePane(tab1)).thenReturn(queryPhasePane1);
        
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.tabHasEnabledPlugins(tab1)).thenReturn(true);
        
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        when(queryPhasePane1.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1));
        
        final ActionEvent event = mock(ActionEvent.class);
        
        try (
                MockedConstruction<WaitForQueriesToCompleteTask> waitForMockedConstruction =
                        Mockito.mockConstruction(WaitForQueriesToCompleteTask.class, (waitForMock, cntxt) -> {
                            assertEquals(cntxt.arguments(), List.of(dataAccessPane, GRAPH_ID));
                        });
        ) {
            executeListener.handle(event);
            
            verify(notificationDisplayer).notify(
                    "Save raw results",
                    UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                    "Results directory /SOMETHING_THAT_WOULD_NEVER_EXIST does not exist",
                    null
            );
            
            // No need to verify everything. Just make sure the main parts are still
            // executed to show that the plugins are still run after the error
            
            verify(pluginExecution).executeLater(null);
            verify(queryPhasePane1).runPlugins(null);
         
            completableFutureMockedStatic.verify(() -> CompletableFuture
                    .runAsync(
                            same(waitForMockedConstruction.constructed().get(0)),
                            any(ExecutorService.class)
                    )
            );
            
            verifyNoInteractions(dataSourceTitledPane1);
        }
    }
    
    @Test
    public void handle_no_tabs() {
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        DataAccessPaneState.updateExecuteButtonIsGo(true);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList());
        
        final Future plugin1Future = mock(Future.class);
        final Future plugin2Future = mock(Future.class);
        
        DataAccessPaneState.addRunningPlugin(plugin1Future, "Plugin 1");
        DataAccessPaneState.addRunningPlugin(plugin2Future, "Plugin 2");
        
        final ActionEvent event = mock(ActionEvent.class);
        
        executeListener.handle(event);
        
        verify(plugin1Future).cancel(true);
        verify(plugin2Future).cancel(true);
        
        verify(dataAccessPane).setExecuteButtonToGo();
    }
    
    @Test
    public void handle_execute_button_is_not_go() {
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        DataAccessPaneState.updateExecuteButtonIsGo(false);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(mock(Tab.class)));
        
        final Future plugin1Future = mock(Future.class);
        final Future plugin2Future = mock(Future.class);
        
        DataAccessPaneState.addRunningPlugin(plugin1Future, "Plugin 1");
        DataAccessPaneState.addRunningPlugin(plugin2Future, "Plugin 2");
        
        final ActionEvent event = mock(ActionEvent.class);
        
        executeListener.handle(event);
        
        verify(plugin1Future).cancel(true);
        verify(plugin2Future).cancel(true);
        
        verify(dataAccessPane).setExecuteButtonToGo();
    }
}
