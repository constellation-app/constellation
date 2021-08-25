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
import au.gov.asd.tac.constellation.views.tableview2.listeners.SelectedOnlySelectionListener;
import au.gov.asd.tac.constellation.views.tableview2.listeners.TableComparatorListener;
import au.gov.asd.tac.constellation.views.tableview2.listeners.TableSelectionListener;
import au.gov.asd.tac.constellation.views.tableview2.listeners.TableSortTypeListener;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;

/**
 * Table View Pane.
 *
 * TODO: some javafx classes no are longer supported, fix it.
 *
 * @author elnath
 * @author cygnus_x-1
 * @author antares
 */
public final class TableViewPane extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(TableViewPane.class.getName());

    private final Table table;
    private final TableToolbar tableToolbar;
    private final ProgressBar progressBar;

    private final TableService tableService;
    
    private final ChangeListener<ObservableList<String>> tableSelectionListener;
    private final ListChangeListener selectedOnlySelectionListener;

    private final ChangeListener<? super Comparator<? super ObservableList<String>>> tableComparatorListener;
    private final ChangeListener<? super TableColumn.SortType> tableSortTypeListener;

    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;

    public TableViewPane(final TableViewTopComponent tableTopComponent) {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        
        final SortedList<ObservableList<String>> sortedRowList
                = new SortedList<>(FXCollections.observableArrayList());
        
        //
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();
        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        
        tableService = new TableService(
                sortedRowList,
                elementIdToRowIndex,
                rowToElementIdIndex);
        
        progressBar = new ProgressBar();
        
        // Create table UI component
        table = new Table(tableTopComponent, this, tableService);
        
        tableService.getSortedRowList().comparatorProperty()
                .bind(table.getTableView().comparatorProperty());
        
        // Setup the table level listeners
        this.tableSelectionListener = new TableSelectionListener(tableTopComponent, table.getTableView(),
                rowToElementIdIndex);
        this.selectedOnlySelectionListener = new SelectedOnlySelectionListener(tableTopComponent,
                table.getTableView(), tableService.getSelectedOnlySelectedRows());
        this.tableComparatorListener = new TableComparatorListener(this, tableService);
        this.tableSortTypeListener = new TableSortTypeListener(this, tableService);
        
        table.getSelectedProperty().addListener(tableSelectionListener);
        table.getTableView().getSelectionModel().getSelectedItems()
                .addListener(selectedOnlySelectionListener);

        // Set up the page factory used by the pagination
        final TableViewPageFactory pageFactory = new TableViewPageFactory(
                tableService.getSortedRowList(),
                tableService.getSelectedOnlySelectedRows(),
                tableTopComponent,
                table);
        
        pageFactory.setTableSelectionListener(tableSelectionListener);
        pageFactory.setTableComparatorListener(tableComparatorListener);
        pageFactory.setTableSortTypeListener(tableSortTypeListener);
        pageFactory.setSelectedOnlySelectionListener(selectedOnlySelectionListener);
        
        tableService.setPageFactory(pageFactory);
        
        // Setup the UI components
        this.tableToolbar = new TableToolbar(tableTopComponent, this, table, tableService);
        this.tableToolbar.init();
        
        setLeft(tableToolbar.getToolbar());

        // Initiate table update and initialisation
        tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage());
        Platform.runLater(() -> {
            setCenter(tableService.getPagination());
        });
    }

    /**
     * Update the whole table using the graph.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateTable(final Graph graph, final TableViewState state) {
        final Thread thread = new Thread("Table View: Update Table") {
            @Override
            public void run() {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }

                scheduledFuture = scheduledExecutorService.schedule(() -> {
                    tableToolbar.updateToolbar(state);
                    if (graph != null) {
                        getTable().updateColumns(graph, state, getTableSelectionListener(), getSelectedOnlySelectionListener());
                        getTable().updateData(graph, state, getProgressBar(), getTableSelectionListener(), getSelectedOnlySelectionListener());
                        getTable().updateSelection(graph, state, getTableSelectionListener(), getSelectedOnlySelectionListener());
                        Platform.runLater(() -> {
                            getTable().updateSortOrder();
                        });
                    } else {
                        Platform.runLater(() -> {
                            getTable().getTableView().getColumns().clear();
                        });
                    }
                }, 0, TimeUnit.MILLISECONDS);
            }
        };
        thread.start();
    }

    public ChangeListener<ObservableList<String>> getTableSelectionListener() {
        return tableSelectionListener;
    }

    public ListChangeListener getSelectedOnlySelectionListener() {
        return selectedOnlySelectionListener;
    }

    public ChangeListener<? super Comparator<? super ObservableList<String>>> getTableComparatorListener() {
        return tableComparatorListener;
    }

    public ChangeListener<? super TableColumn.SortType> getTableSortTypeListener() {
        return tableSortTypeListener;
    }

    public Table getTable() {
        return table;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TableService getTableService() {
        return tableService;
    }
}
