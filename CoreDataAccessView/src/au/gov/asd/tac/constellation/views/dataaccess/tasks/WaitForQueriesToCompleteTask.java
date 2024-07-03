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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;

/**
 * Task that can be dispatched to track the completion of queries and notifications
 * of failures.
 *
 * @author formalhaunt
 */
public class WaitForQueriesToCompleteTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(WaitForQueriesToCompleteTask.class.getName());

    private static final Icon ERROR_ICON = UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor());
    
    private static final String ERROR_FORMAT = "Data Access Plug-In '%s' Errored. Did NOT Finish.";
    private static final String CANCEL_ERROR_FORMAT = "Data Access Plug-In '%s' Cancelled. Did NOT Finish.";
    private static final String INTERRUPT_ERROR_FORMAT = "Data Access Plug-In '%s' Interrupted. Was it Cancelled? Did NOT Finish.";
    
    private final DataAccessPane dataAccessPane;
    private final String graphId;
    
    /**
     * Create a new wait for queries to complete task.
     *
     * @param dataAccessPane the data access pane
     * @param graphId the ID of the graph that the queries are running on
     */
    public WaitForQueriesToCompleteTask(final DataAccessPane dataAccessPane,
                                        final String graphId) {
        this.dataAccessPane = dataAccessPane;
        this.graphId = graphId;
    }
    
    /**
     * Gets all running queries for the provided graph ID and waits for each one
     * to finish. If any complete exceptionally then a notification is displayed
     * describing what failed.
     * <p/>
     * Once all queries have been completed, the running queries state for the graph
     * is set back to false and if it is the current graph then the data access
     * pane view is also updated.
     */
    @Override
    public void run() {
        DataAccessPaneState.getRunningPlugins(graphId).entrySet().forEach(runningPlugin -> {
            try {
                runningPlugin.getKey().get();
            } catch (ExecutionException ex) {
                LOGGER.log(Level.SEVERE, String.format(ERROR_FORMAT, runningPlugin.getValue()), ex);
                NotificationDisplayer.getDefault().notify(
                        String.format(ERROR_FORMAT, runningPlugin.getValue()),
                        ERROR_ICON,
                        ex.getCause().getMessage(),
                        null,
                        NotificationDisplayer.Priority.HIGH
                );
            } catch (final CancellationException e) {
                LOGGER.log(Level.INFO, String.format(CANCEL_ERROR_FORMAT, runningPlugin.getValue()));
            } catch (final InterruptedException e) {
                LOGGER.log(Level.INFO, String.format(INTERRUPT_ERROR_FORMAT, runningPlugin.getValue()));
                Thread.currentThread().interrupt();
            }
        });
        
        Platform.runLater(() -> {
            DataAccessPaneState.setQueriesRunning(graphId, false);
            DataAccessPaneState.removeAllRunningPlugins(graphId);
            if (graphId.equals(DataAccessPaneState.getCurrentGraphId())) {
                dataAccessPane.update();
            }
        });
    }

    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }

    public String getGraphId() {
        return graphId;
    }
    
}
