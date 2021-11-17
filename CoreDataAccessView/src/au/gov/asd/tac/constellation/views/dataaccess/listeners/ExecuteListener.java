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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javax.swing.Icon;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;

/**
 * Creates a event handler for when the execute button is clicked.
 *
 * @author formalhaunt
 */
public class ExecuteListener implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ExecuteListener.class.getName());
    
    // Directory Not Found Notifier
    private static final Icon ERROR_ICON = UserInterfaceIconProvider.ERROR
            .buildIcon(16, ConstellationColor.CHERRY.getJavaColor());
    private static final String RESULTS_DIR_NOT_FOUND_TITLE = "Save raw results";
    private static final String RESULTS_DIR_NOT_FOUND_MSG = "Results directory %s does not exist";
    
    // Status Message on Plugin Run
    private static final String STATUS_MESSAGE_FORMAT = "Data access results will be written to %s";
    
    private static final String SAVE_STATE_PLUGIN_NAME = "Data Access View: Save State";
    
    private final DataAccessPane dataAccessPane;
    
    /**
     * Creates a new execute button listener.
     *
     * @param dataAccessPane the data access pane that the execute button is added to
     */
    public ExecuteListener(final DataAccessPane dataAccessPane) {
        this.dataAccessPane = dataAccessPane;
    }
    
    /**
     * Handles the click of the execute button in the data access view.
     * <p/>
     * If the execute button was in the "Go" state then it will iterate through
     * all the tabs and run the enabled and valid plugins.
     * <p/>
     * If the execute button was in the "Stop" state then cancel any running plugins.
     *
     * @param event the event triggered by clicking the execute button
     */
    @Override
    public void handle(final ActionEvent event) {
        // When no graph present, create a new one
        if (DataAccessPaneState.getCurrentGraphId() == null
                && dataAccessPane.getDataAccessTabPane().hasActiveAndValidPlugins()) {
            
            // Create new graph
            final NewDefaultSchemaGraphAction graphAction = new NewDefaultSchemaGraphAction();
            graphAction.actionPerformed(null);
            
            Graph newActiveGraph = null;
            // Wait while graph is getting made
            while (newActiveGraph == null) {
                newActiveGraph = GraphManager.getDefault().getActiveGraph();
            }
            
            // Set the state's current graph ID to the ID of the new graph
            DataAccessPaneState.setCurrentGraphId(newActiveGraph.getId());
        }
        
        // Run the selected queries
        final ObservableList<Tab> tabs = dataAccessPane.getDataAccessTabPane().getTabPane().getTabs();
        
        if (CollectionUtils.isNotEmpty(tabs) && DataAccessPaneState.isExecuteButtonIsGo()) {
            // Change the execute button to "Stop" and do not disable because it is now running
            dataAccessPane.setExecuteButtonToStop(false);
            
            // Set the state for the current graph state to running queries
            DataAccessPaneState.setQueriesRunning(true);

            // Check to see if an output dir exists. Non exisiting dirs do not prevent the
            // plugins running, just triggers a notification
            final File outputDir = DataAccessPreferenceUtilities.getDataAccessResultsDirEx();
            
            if (outputDir != null && outputDir.isDirectory()) {
                StatusDisplayer.getDefault().setStatusText(
                        String.format(STATUS_MESSAGE_FORMAT, outputDir.getAbsolutePath())
                );
            } else if (outputDir != null) {
                NotificationDisplayer.getDefault().notify(
                        RESULTS_DIR_NOT_FOUND_TITLE,
                        ERROR_ICON,
                        String.format(RESULTS_DIR_NOT_FOUND_MSG, outputDir.getAbsolutePath()),
                        null
                );
            }

            // Save the current data access view state
            PluginExecution.withPlugin(new SimplePlugin(SAVE_STATE_PLUGIN_NAME) {
                @Override
                protected void execute(final PluginGraphs graphs,
                                       final PluginInteraction interaction,
                                       final PluginParameters parameters) throws InterruptedException, PluginException {
                    DataAccessUtilities.saveDataAccessState(
                            dataAccessPane.getDataAccessTabPane().getTabPane(),
                            GraphNode.getGraph(DataAccessPaneState.getCurrentGraphId())
                    );
                }
            }).executeLater(null);

            // Run the plugins from each tab. The barrier is the plugin run futures
            // from the previous tab. When the tab is run, it has the option to
            // wait for the previous tab to complete.
            List<Future<?>> barrier = null;
            for (final Tab tab : tabs) {
                LOGGER.log(Level.INFO, String.format("Running tab: %s", tab.getText()));

                barrier = DataAccessTabPane.getQueryPhasePane(tab).runPlugins(barrier);
            }

            // Asynchronously start the task that waits for all the plugins to complete.
            // Once they are complete this task will perform cleanup.
            CompletableFuture.runAsync(
                    new WaitForQueriesToCompleteTask(
                            dataAccessPane,
                            DataAccessPaneState.getCurrentGraphId()
                    ),
                    dataAccessPane.getParentComponent().getExecutorService()
            );
            
            LOGGER.info("Plugins run.");
        } else {
            // The execute button is in a "Stop" state. So cancel any running plugins.
            DataAccessPaneState.getRunningPlugins().keySet().forEach(running -> running.cancel(true));
            
            // Nothing is running now, so change the execute button to "Go".
            dataAccessPane.setExecuteButtonToGo(false);
        }
        
        // Disables all plugins in the plugin pane
        if (DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled()) {
            deselectAllPlugins();
        }
    }
    
    /**
     * Iterate through all the tabs in the tab pane and if there are enabled plugins
     * then disable them.
     */
    private void deselectAllPlugins() {
        dataAccessPane.getDataAccessTabPane().getTabPane().getTabs().stream()
                .filter(tab -> DataAccessTabPane.tabHasEnabledPlugins(tab))
                .forEachOrdered(tab ->
                    DataAccessTabPane.getQueryPhasePane(tab).getDataAccessPanes()
                            .forEach(updatingDataAccessPane ->
                                    updatingDataAccessPane.validityChanged(false)
                            )
        );
    }
}
