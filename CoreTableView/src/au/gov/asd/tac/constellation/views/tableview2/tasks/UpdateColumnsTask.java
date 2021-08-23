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

import au.gov.asd.tac.constellation.views.tableview2.UpdateMethod;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author formalhaunt
 */
public class UpdateColumnsTask implements Runnable {
    private final TableViewTopComponent parent;
    private final TableView<ObservableList<String>> tableView;
    
    private final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columnIndex;

    private final TableService tableService;
    
    private Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap;
    
    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ListChangeListener selectedOnlySelectionListener;
    
    private TableViewState state;
    
    private ListChangeListener.Change<? extends TableColumn<ObservableList<String>, ?>> lastChange;
    
    public UpdateColumnsTask(final TableViewTopComponent parent,
                             final TableView<ObservableList<String>> tableView,
                             final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columnIndex,
                             final TableService tableService) {
        this.parent = parent;
        this.tableView = tableView;
        this.columnIndex = columnIndex;
        this.tableService = tableService;
    }
    
    public void reset(final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap,
                      final TableViewState state,
                      final ChangeListener<ObservableList<String>> tableSelectionListener,
                      final ListChangeListener selectedOnlySelectionListener) {
        this.columnReferenceMap = columnReferenceMap;
        this.state = state;
        this.tableSelectionListener = tableSelectionListener;
        this.selectedOnlySelectionListener = selectedOnlySelectionListener;
    }
    
    @Override
    public void run() {
        tableView.getSelectionModel().selectedItemProperty()
                .removeListener(tableSelectionListener);
        tableView.getSelectionModel().getSelectedItems()
                .removeListener(selectedOnlySelectionListener);

        columnReferenceMap.forEach((columnName, column) -> column.setGraphic(null));

        // set column visibility in columnIndex based on the state
        columnIndex.forEach(columnTuple -> {
            columnTuple.getThird().setVisible(state.getColumnAttributes().stream()
                    .anyMatch(a -> a.getFirst().equals(columnTuple.getFirst())
                    && a.getSecond().equals(columnTuple.getSecond())));
        });

        // add columns to table
        tableView.getColumns().clear();
        tableView.getColumns().addAll(
                columnIndex.stream()
                        .map(t -> t.getThird())
                        .collect(Collectors.toList())
        );

        // sort data if the column ordering changes
        tableView.getColumns().addListener((final ListChangeListener.Change<? extends TableColumn<ObservableList<String>, ?>> change) -> {
            if (lastChange == null || !lastChange.equals(change)) {
                while (change.next()) {
                    if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                        saveSortDetails();
                        
                        final List<TableColumn<ObservableList<String>, String>> columnIndexColumns
                                = columnIndex.stream()
                                        .map(ci -> ci.getThird())
                                        .collect(Collectors.toList());
                        
                        final List<Tuple<String, Attribute>> orderedColumns
                                = change.getAddedSubList().stream()
                                        .map(c -> columnIndex.get(columnIndexColumns.indexOf(c)))
                                        .map(c -> Tuple.create(c.getFirst(), c.getSecond()))
                                        .filter(c -> (parent.getCurrentState().getColumnAttributes().contains(Tuple.create(c.getFirst(), c.getSecond()))))
                                        .collect(Collectors.toList());
                        
                        tableService.updateVisibleColumns(
                                parent.getCurrentGraph(), parent.getCurrentState(),
                                orderedColumns, UpdateMethod.REPLACE
                        );
                    }
                }
                lastChange = change;
            }
        });

        tableView.getSelectionModel().getSelectedItems()
                .addListener(selectedOnlySelectionListener);
        tableView.getSelectionModel().selectedItemProperty()
                .addListener(tableSelectionListener);
    }
    
    /**
     * Extract any current table sort information and save this information. See
     * other saveSortDetails for reason this is done.
     */
    private void saveSortDetails() {
        if (tableView.getSortOrder() != null && tableView.getSortOrder().size() > 0) {
            // A column was selected to sort by, save its name and direction
            tableService.saveSortDetails(tableView.getSortOrder().get(0).getText(),
                    tableView.getSortOrder().get(0).getSortType());
        } else {
            // no column is selected, clear any previously stored information.
            tableService.saveSortDetails("", TableColumn.SortType.ASCENDING);
        }
    }
}
