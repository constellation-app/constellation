/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview.panes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.ProgressBar;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.components.TableToolbar;
import au.gov.asd.tac.constellation.views.tableview.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

/**
 * This is the main pane that creates a CONSTELLATION table and adds it.
 *
 * @author elnath
 * @author cygnus_x-1
 * @author antares
 */
public final class TablePane extends BorderPane {

    /**
     * Runs the passed runnable in the current thread but only if the current
     * thread in <b>NOT</b> interrupted. If the thread is interrupted then
     * nothing is done.
     */
    private static final Consumer<Runnable> CHECK_INTERRUPT_AND_RUN = runnable -> {
        if (!Thread.currentThread().isInterrupted()) {
            runnable.run();
        }
    };

    /**
     * Runs the list of runnables in order on the current thread. Should the
     * thread become interrupted at some point all runnables after that point
     * will not be run.
     */
    private static final Consumer<List<Runnable>> CHECK_INTERRUPT_AND_RUN_EACH = runnables
            -> runnables.forEach(CHECK_INTERRUPT_AND_RUN);

    private final TableViewTopComponent parentComponent;

    private final Table table;
    private final TableToolbar tableToolbar;
    private final ProgressBar progressBar;

    private final ActiveTableReference activeTableReference;

    private Future<?> future;

    /**
     * Creates a new pane with a Constellation Table initialized within it.
     *
     * @param tableTopComponent the top component for the table plugin
     */
    public TablePane(final TableViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;

        // Because the page factory doesn't start getting used until this
        // constructor is complete, it gets passed a reference to 'this' with
        // the assumption everything will be intiailized once its called which
        // will not happen until update paginate is called which happens
        // at the end of this constructor.
        // #dodgycode
        activeTableReference = new ActiveTableReference(new TableViewPageFactory(this));

        progressBar = new ProgressBar();

        // Create table UI component
        table = new Table(this);

        activeTableReference.getSortedRowList().comparatorProperty()
                .bind(table.getTableView().comparatorProperty());

        // Setup the UI components
        this.tableToolbar = new TableToolbar(this);
        this.tableToolbar.init();

        setLeft(tableToolbar.getToolbar());

        // Initiate table update and initialisation
        activeTableReference.updatePagination(activeTableReference.getUserTablePreferences().getMaxRowsPerPage(), this);
    }

    /**
     * Update the whole table using the graph. This method will submit a task to
     * the executor service and return but there is also a check for an existing
     * update which could cause a race condition if two threads access this
     * method simultaneously.
     *
     * @param graph the graph to retrieve data from
     * @param state the current table view state
     */
    public synchronized void updateTable(final Graph graph, final TableViewState state) {
        if (future != null) {
            future.cancel(true);
        }

        future = getParentComponent().getExecutorService().submit(() -> {
            CHECK_INTERRUPT_AND_RUN.accept(() -> getTableToolbar().updateToolbar(state));

            if (graph != null) {
                // Executed in order and interruption status checked between each step
                CHECK_INTERRUPT_AND_RUN_EACH.accept(List.of(
                        () -> getTable().updateColumns(graph, state),
                        () -> getTable().updateData(graph, state, getProgressBar()),
                        () -> getTable().updateSelection(graph, state),
                        () -> Platform.runLater(() -> getTable().updateSortOrder())
                ));
            } else {
                CHECK_INTERRUPT_AND_RUN.accept(() -> Platform.runLater(
                        () -> getTable().getTableView().getColumns().clear()
                ));
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
     * Gets a {@link BorderPane} that represents a progress bar that is
     * activated during long running updates like table row refreshes.
     *
     * @return the table progress bar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Gets the table reference for this instance. The table reference provides
     * access to the current pagination, table preferences, current table rows
     * etc.
     *
     * @return the current table reference
     */
    public ActiveTableReference getActiveTableReference() {
        return activeTableReference;
    }

    /**
     * A future representing the future status of the table based on the latest
     * call to {@link #updateTable(Graph, TableViewState)}. If the table has not
     * yet been updated, then it will return null.
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

    /**
     * Get the top component for the table.
     *
     * @return the table top component
     */
    public TableViewTopComponent getParentComponent() {
        return parentComponent;
    }
}
