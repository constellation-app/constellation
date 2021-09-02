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
package au.gov.asd.tac.constellation.views.tableview2.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

/**
 * This is the main pane that creates a CONSTELLATION table and adds it.
 *
 * @author elnath
 * @author cygnus_x-1
 * @author antares
 */
public final class TableViewPane extends BorderPane {
    private final TableViewTopComponent tableTopComponent;
    private final Table table;
    private final TableToolbar tableToolbar;
    private final ProgressBar progressBar;

    private final TableService tableService;

    private Future<?> future;

    /**
     * Creates a new pane with a Constellation Table initialized within it.
     *
     * @param tableTopComponent the top component for the table plugin
     */
    public TableViewPane(final TableViewTopComponent tableTopComponent) {
        this.tableTopComponent = tableTopComponent;
        
        // Because the page factory doesn't start getting used until this
        // constructor is complete, it gets passed a reference to 'this' with
        // the assumption everything will be intiailized once its called which
        // will not happen until update paginate is called which happens
        // at the end of this constructor.
        // #dodgycode
        tableService = new TableService(new TableViewPageFactory(this));
        
        progressBar = new ProgressBar();
        
        // Create table UI component
        table = new Table(tableTopComponent, this, tableService);
        
        tableService.getSortedRowList().comparatorProperty()
                .bind(table.getTableView().comparatorProperty());
        
        // Setup the UI components
        this.tableToolbar = new TableToolbar(tableTopComponent, this, table, tableService);
        this.tableToolbar.init();
        
        setLeft(tableToolbar.getToolbar());
        
        // Initiate table update and initialisation
        tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage());
        Platform.runLater(() -> setCenter(tableService.getPagination()));
    }

    /**
     * Update the whole table using the graph.
     *
     * @param graph the graph to retrieve data from
     * @param state the current table view state
     */
    public void updateTable(final Graph graph, final TableViewState state) {
        if (future != null) {
            future.cancel(true);
        }

        future = getTableTopComponent().getExecutorService().submit(() -> {
            getTableToolbar().updateToolbar(state);
            if (graph != null) {
                getTable().updateColumns(graph, state);
                getTable().updateData(graph, state, getProgressBar());
                getTable().updateSelection(graph, state);
                Platform.runLater(() -> getTable().updateSortOrder());
            } else {
                Platform.runLater(() -> getTable().getTableView().getColumns().clear());
            }
        });
    }

    /**
     * Gets the table that has been created and added to this pane.
     *
     * @return the created table
     */
    public Table getTable() {
        return table;
    }

    /**
     * 
     * @return 
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * 
     * @return 
     */
    public TableService getTableService() {
        return tableService;
    }

    /**
     * A future representing the future status of the table based on the
     * latest call to {@link #updateTable(Graph, TableViewState)}. If the table has
     * not yet been updated, then it will return null.
     *
     * @return the current future or null if the table has not been updated
     */
    public Future<?> getFuture() {
        return future;
    }

    /**
     * Get the tool bar that has been attached to this table.
     *
     * @return the table tool bar
     */
    public TableToolbar getTableToolbar() {
        return tableToolbar;
    }

    public TableViewTopComponent getTableTopComponent() {
        return tableTopComponent;
    }
}
