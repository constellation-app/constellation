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
package au.gov.asd.tac.constellation.views.tableview2.tasks;

import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.controlsfx.control.table.TableFilter;

/**
 * A {@link Runnable} that updates the table with the new rows and triggers an update.
 *
 * @author formalhaunt
 */
public class UpdateDataTask implements Runnable {

    private final TableViewPane tablePane;
    private final Table table;
    
    private final List<ObservableList<String>> rows;
    
    private final CountDownLatch updateDataLatch;
    
    private final TableService tableService;
    
    private final ChangeListener<ObservableList<String>> tableSelectionListener;
    private final ListChangeListener selectedOnlySelectionListener;
    
    private TableFilter<ObservableList<String>> filter;
    
    private List<ObservableList<String>> filteredRowList;
    
    /**
     * 
     * @param tablePane
     * @param table
     * @param rows the new rows to update the table with
     * @param tableSelectionListener
     * @param selectedOnlySelectionListener
     * @param updateDataLatch latch with a count of one that will track the status of the job
     * @param tableService 
     */
    public UpdateDataTask(final TableViewPane tablePane,
                          final Table table,
                          final List<ObservableList<String>> rows,
                          final ChangeListener<ObservableList<String>> tableSelectionListener,
                          final ListChangeListener selectedOnlySelectionListener,
                          final CountDownLatch updateDataLatch,
                          final TableService tableService) {
        this.tablePane = tablePane;
        this.table = table;
        this.rows = rows;
        this.tableSelectionListener = tableSelectionListener;
        this.selectedOnlySelectionListener = selectedOnlySelectionListener;
        this.updateDataLatch = updateDataLatch;
        this.tableService = tableService;
    }
    
    /**
     * 
     */
    @Override
    public void run() {
        table.getSelectedProperty().removeListener(tableSelectionListener);
        table.getTableView().getSelectionModel().getSelectedItems()
                .removeListener(selectedOnlySelectionListener);
        tableService.getSortedRowList().comparatorProperty().unbind();

        // add table data to table
        tableService.setSortedRowList(new SortedList<>(FXCollections.observableArrayList(rows)));

        //need to set the table items to the whole list here so that the filter
        //picks up the full list of options to filter before we paginate
        table.getTableView().setItems(FXCollections.observableArrayList(tableService.getSortedRowList()));

        // add user defined filter to the table
        filter = TableFilter.forTableView(table.getTableView()).lazy(true).apply();
        filter.setSearchStrategy((t, u) -> {
            try {
                return u.toLowerCase().startsWith(t.toLowerCase());
            } catch (final Exception ex) {
                return false;
            }
        });
        filter.getFilteredList().predicateProperty().addListener((v, o, n) -> {
            tableService.getSortedRowList().comparatorProperty().unbind();
            filteredRowList = new FilteredList<>(FXCollections.observableArrayList(rows),
                    filter.getFilteredList().getPredicate());
            tableService.setSortedRowList(new SortedList<>(
                    FXCollections.observableArrayList(filteredRowList)));
            
            tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage(),
                    tableService.getSortedRowList());
            Platform.runLater(() -> {
                tablePane.setCenter(tableService.getPagination());
            });
        });
        
        tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage(),
                tableService.getSortedRowList());
        Platform.runLater(() -> {
            tablePane.setCenter(tableService.getPagination());
        });
        
        updateDataLatch.countDown();

        table.getTableView().getSelectionModel().getSelectedItems()
                .addListener(selectedOnlySelectionListener);
        table.getSelectedProperty().addListener(tableSelectionListener);
    }
}
