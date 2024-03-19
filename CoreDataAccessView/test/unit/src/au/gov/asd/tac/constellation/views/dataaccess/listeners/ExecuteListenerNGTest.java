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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
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
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ExecuteListenerNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(ExecuteListenerNGTest.class.getName());
    
    private static final String GRAPH_ID = "graphId";
    
    private static MockedStatic<GraphManager> graphManagerMockedStatic;
    private static MockedStatic<GraphNode> graphNodeMockedStatic;
    private static MockedStatic<DataAccessTabPane> dataAccessTabPaneMockedStatic;
    private static MockedStatic<PluginExecution> pluginExecutionMockedStatic;
    private static MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic;
    private static MockedStatic<StatusDisplayer> statusDisplayerMockedStatic;
    private static MockedStatic<DataAccessUtilities> utilitiesMockedStatic;
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
        graphNodeMockedStatic = Mockito.mockStatic(GraphNode.class);
        dataAccessTabPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
        pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class);
        notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);
        statusDisplayerMockedStatic = Mockito.mockStatic(StatusDisplayer.class);
        utilitiesMockedStatic = Mockito.mockStatic(DataAccessUtilities.class);
        completableFutureMockedStatic = Mockito.mockStatic(CompletableFuture.class, Mockito.CALLS_REAL_METHODS);
        
        graphActionMockedConstruction = Mockito.mockConstruction(NewDefaultSchemaGraphAction.class);
        
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        graphManagerMockedStatic.close();
        graphNodeMockedStatic.close();
        dataAccessTabPaneMockedStatic.close();
        pluginExecutionMockedStatic.close();
        notificationDisplayerMockedStatic.close();
        statusDisplayerMockedStatic.close();
        utilitiesMockedStatic.close();
        completableFutureMockedStatic.close();
        
        graphActionMockedConstruction.close();
        
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
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
        graphManagerMockedStatic.reset();
        graphNodeMockedStatic.reset();
        dataAccessTabPaneMockedStatic.reset();
        pluginExecutionMockedStatic.reset();
        notificationDisplayerMockedStatic.reset();
        statusDisplayerMockedStatic.reset();
        utilitiesMockedStatic.reset();
        completableFutureMockedStatic.reset();
    }
    
    @Test
    public void handle_sunny_days() {
        // Set the results directory
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DataAccessPreferenceUtilities.setDataAccessResultsDir(tmpDir);
        
        // Setting this to true ensures that active plugins are de-selected at the end
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(true);
        
        // We don't set the current graph ID in the state but ensure that there
        // active and valid plugins in the tab pane so that a new graph is created
        // on the fly
        when(dataAccessTabPane.hasActiveAndValidPlugins()).thenReturn(true);
        
        graphNodeMockedStatic.when(() -> GraphNode.getGraph(anyString())).thenReturn(activeGraph);
        
        // Set up our fake tab pane
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(tab1, tab2));
        
        final QueryPhasePane queryPhasePane1 = mock(QueryPhasePane.class);
        final QueryPhasePane queryPhasePane2 = mock(QueryPhasePane.class);
        
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.getQueryPhasePane(tab1)).thenReturn(queryPhasePane1);
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.getQueryPhasePane(tab2)).thenReturn(queryPhasePane2);

        // Tab 1 does not have enabled plugins and tab 2 does. This is testing the deselect
        // plugin code. The deselection should not happen on tab 1 because it has no enabled
        // plugins
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.tabHasEnabledPlugins(tab1)).thenReturn(false);
        dataAccessTabPaneMockedStatic.when(() -> 
                DataAccessTabPane.tabHasEnabledPlugins(tab2)).thenReturn(true);
        
        // Because tab 1 is not enabled de-selection should happen on pane 2 and 3, but
        // not pane 1.
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        when(queryPhasePane1.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1));
        
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane3 = mock(DataSourceTitledPane.class);
        when(queryPhasePane2.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane2, dataSourceTitledPane3));
        
        // Set up the plugin running. Tab 1 is run first which returns a future. That future
        // is then passed in when tab 2 is run.
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

            // There was not current graph set during the test setup. So we
            // should see one construction of the graph action to create on.
            assertEquals(graphActionMockedConstruction.constructed().size(), 1);
            
            // Verify the execute button is changed to "Stop" and the status
            // displayer is sent some text describing the action success
            verify(dataAccessPane).setExecuteButtonToStop(false);
            verify(statusDisplayer).setStatusText("Data access results will be written to " + tmpDir.getAbsolutePath());

            // Verify the current state is saved before the plugins are run
            utilitiesMockedStatic.verify(() -> DataAccessUtilities
                    .saveDataAccessState(tabPane, activeGraph));

            verify(pluginExecution).executeLater(null);

            // Verify the plugins are run
            verify(queryPhasePane1).runPlugins(or(anyList(), isNull()));
            verify(queryPhasePane2).runPlugins(or(anyList(), isNull()));
            verify(queryPhasePane1).runPlugins(null);
            verify(queryPhasePane2).runPlugins(barrier1);

            // Verify that the wait and clean up task is started
            assertEquals(waitForMockedConstruction.constructed().size(), 1);
            
            completableFutureMockedStatic.verify(() -> CompletableFuture
                    .runAsync(
                            same(waitForMockedConstruction.constructed().get(0)),
                            any(ExecutorService.class)
                    )
            );
            
            // Verify that the correct plugins are de-selected
            verifyNoInteractions(dataSourceTitledPane1);
            verify(dataSourceTitledPane2).validityChanged(false);
            verify(dataSourceTitledPane3).validityChanged(false);
            
            verifyNoInteractions(event);
            
            // Verify that the state for the current graph is that queries are running
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
        
        // Set this preference to false so that plugins are not de-selected at the end
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(false);
        
        // Set the current graph ID so the code doesn't try to create one
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        
        // Set up our fake tab pane
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
            
            // So the directory the results are meant to be written to doesn't exist,
            // verify that the notifier is displayed.
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
            
            // Verify that the plugins are not de-selected
            verifyNoInteractions(dataSourceTitledPane1);
        }
    }
    
    @Test
    public void handle_no_tabs() {
        // Execute button is in a "Go" state but there are no tabs
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        DataAccessPaneState.updateExecuteButtonIsGo(true);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList());
        
        // Add some existing running plugins for the current graph.
        final Future plugin1Future = mock(Future.class);
        final Future plugin2Future = mock(Future.class);
        
        DataAccessPaneState.addRunningPlugin(plugin1Future, "Plugin 1");
        DataAccessPaneState.addRunningPlugin(plugin2Future, "Plugin 2");
        
        final ActionEvent event = mock(ActionEvent.class);
        
        executeListener.handle(event);
        
        // Verify that all the plugins are cancelled
        verify(plugin1Future).cancel(true);
        verify(plugin2Future).cancel(true);
        
        // Nothing is running, so make sure the execute button is now in the "Go" state
        verify(dataAccessPane).setExecuteButtonToGo(false);
    }
    
    @Test
    public void handle_execute_button_is_not_go() {
        // Execute button is in a "Go" state but there are no tabs
        DataAccessPaneState.setCurrentGraphId(GRAPH_ID);
        DataAccessPaneState.updateExecuteButtonIsGo(false);
        
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(mock(Tab.class)));
        
        // Add some existing running plugins for the current graph.
        final Future plugin1Future = mock(Future.class);
        final Future plugin2Future = mock(Future.class);
        
        DataAccessPaneState.addRunningPlugin(plugin1Future, "Plugin 1");
        DataAccessPaneState.addRunningPlugin(plugin2Future, "Plugin 2");
        
        final ActionEvent event = mock(ActionEvent.class);
        
        executeListener.handle(event);
        
        // Verify that all the plugins are cancelled
        verify(plugin1Future).cancel(true);
        verify(plugin2Future).cancel(true);
        
        // Nothing is running, so make sure the execute button is now in the "Go" state
        verify(dataAccessPane).setExecuteButtonToGo(false);
    }
}
