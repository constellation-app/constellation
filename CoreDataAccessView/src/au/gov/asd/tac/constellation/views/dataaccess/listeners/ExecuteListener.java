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

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
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
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessPreferencesIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
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
import org.apache.commons.collections4.CollectionUtils;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author formalhaunt
 */
public class ExecuteListener implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ExecuteListener.class.getName());
    
    private final DataAccessPane dataAccessPane;
    
    public ExecuteListener(final DataAccessPane dataAccessPane) {
        this.dataAccessPane = dataAccessPane;
    }
    
    @Override
    public void handle(final ActionEvent event) {
        
        // when no graph present, create new graph
        if (DataAccessPaneState.getCurrentGraphId() == null
                && dataAccessPane.getDataAccessTabPane().hasActiveAndValidPlugins()) {
            
            // Create new graph
            NewDefaultSchemaGraphAction graphAction = new NewDefaultSchemaGraphAction();
            graphAction.actionPerformed(null);
            while (GraphManager.getDefault().getActiveGraph() == null) {
                // Wait and do nothing while graph is getting made
            }
            
            DataAccessPaneState.setCurrentGraphId(GraphManager.getDefault().getActiveGraph().getId());
        }
        // run the selected queries
        final ObservableList<Tab> tabs = dataAccessPane.getDataAccessTabPane().getTabPane().getTabs();
        
        if (CollectionUtils.isNotEmpty(tabs) && DataAccessPaneState.isExecuteButtonIsGo()) {
            dataAccessPane.setExecuteButtonToStop();
            
            DataAccessPaneState.setQueriesRunning(true);

            final File outputDir = DataAccessPreferenceUtilities.getDataAccessResultsDirEx();
            if (outputDir != null && outputDir.isDirectory()) {
                final String msg = String.format("Data access results will be written to %s", outputDir.getAbsolutePath());
                StatusDisplayer.getDefault().setStatusText(msg);
            } else if (outputDir != null) {
                final String msg = String.format("Results directory %s does not exist", outputDir.getAbsolutePath());
                NotificationDisplayer.getDefault().notify("Save raw results",
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        msg,
                        null
                );
            } else {
                // Do nothing
            }

            PluginExecution.withPlugin(new SimplePlugin("Data Access View: Save State") {
                @Override
                protected void execute(final PluginGraphs graphs,
                                       final PluginInteraction interaction,
                                       final PluginParameters parameters) throws InterruptedException, PluginException {
                    DataAccessPreferencesIoProvider.saveDataAccessState(
                            dataAccessPane.getDataAccessTabPane().getTabPane(),
                            GraphManager.getDefault().getActiveGraph()
                    );
                }
            }).executeLater(null);

            List<Future<?>> barrier = null;
            for (final Tab tab : tabs) {
                LOGGER.log(Level.INFO, String.format("Running tab: %s", tab.getText()));

                barrier = DataAccessTabPane.getQueryPhasePane(tab).runPlugins(barrier);
            }

            CompletableFuture.runAsync(
                    new WaitForQueriesToCompleteTask(dataAccessPane, DataAccessPaneState.getCurrentGraphId()),
                    dataAccessPane.getParentComponent().getExecutorService()
            );
            
            LOGGER.info("Plugins run.");
        } else { // Button is a stop button
            DataAccessPaneState.getRunningPlugins().keySet().forEach(running -> {
                running.cancel(true);
            });
            dataAccessPane.setExecuteButtonToGo();
        }
        
        if (DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled()) {
            deselectAllPlugins();
        }
    }
    
    /**
     * 
     */
    private void deselectAllPlugins() {
        dataAccessPane.getDataAccessTabPane().getTabPane().getTabs().stream()
                .filter(tab -> DataAccessTabPane.tabHasEnabledPlugins(tab))
                .forEachOrdered(tab -> {
                    DataAccessTabPane.getQueryPhasePane(tab).getDataAccessPanes()
                            .forEach(updatingDataAccessPane -> updatingDataAccessPane.validityChanged(false));
        });
    }
}
