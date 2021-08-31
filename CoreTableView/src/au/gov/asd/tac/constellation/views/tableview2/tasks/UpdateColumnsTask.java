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
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.Column;
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
 * TODO Make this description better
 * 
 * A runnable that will update the table with the current column settings in
 * the backing object structure.
 *
 * @author formalhaunt
 */
public class UpdateColumnsTask implements Runnable {
    private final TableViewTopComponent parent;
    private final TableView<ObservableList<String>> tableView;
    
    private final CopyOnWriteArrayList<Column> columnIndex;

    private final TableService tableService;
    
    private Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap;
    
    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ListChangeListener selectedOnlySelectionListener;
    
    private TableViewState state;
    
    private ListChangeListener.Change<? extends TableColumn<ObservableList<String>, ?>> lastChange;
    
    /**
     * 
     * @param parent
     * @param tableView
     * @param columnIndex
     * @param tableService 
     */
    public UpdateColumnsTask(final TableViewTopComponent parent,
                             final TableView<ObservableList<String>> tableView,
                             final CopyOnWriteArrayList<Column> columnIndex,
                             final TableService tableService) {
        this.parent = parent;
        this.tableView = tableView;
        this.columnIndex = columnIndex;
        this.tableService = tableService;
    }
    
    /**
     * TODO Figure out a good description
     * 
     * @param columnReferenceMap TODO what is this??
     * @param state the state that will be used to update the table columns
     * @param tableSelectionListener
     * @param selectedOnlySelectionListener 
     */
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
        if (columnReferenceMap == null) {
            throw new RuntimeException("Update columns was started before reset was called.");
        }
        
        tableView.getSelectionModel().selectedItemProperty()
                .removeListener(tableSelectionListener);
        tableView.getSelectionModel().getSelectedItems()
                .removeListener(selectedOnlySelectionListener);

        columnReferenceMap.forEach((columnName, column) -> column.setGraphic(null));

        // set column visibility in columnIndex based on the state
        columnIndex.forEach(column -> {
            column.getTableColumn().setVisible(state.getColumnAttributes().stream()
                    .anyMatch(columnAttr -> 
                            columnAttr.getFirst().equals(column.getAttributeNamePrefix())
                                    && columnAttr.getSecond().equals(column.getAttribute())
                    )
            );
        });

        // add columns to table
        tableView.getColumns().clear();
        tableView.getColumns().addAll(
                columnIndex.stream()
                        .map(column -> column.getTableColumn())
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
                                        .map(column -> column.getTableColumn())
                                        .collect(Collectors.toList());
                        
                        final List<Tuple<String, Attribute>> orderedColumns
                                = change.getAddedSubList().stream()
                                        .map(c -> columnIndex.get(columnIndexColumns.indexOf(c)))
                                        .map(column -> Tuple.create(
                                                column.getAttributeNamePrefix(), 
                                                column.getAttribute()
                                        ))
                                        .filter(columnAttr -> 
                                                parent.getCurrentState().getColumnAttributes()
                                                        .contains(columnAttr)
                                        )
                                        .collect(Collectors.toList());
                        
                        tableService.updateVisibleColumns(
                                parent.getCurrentGraph(),
                                parent.getCurrentState(),
                                orderedColumns,
                                UpdateMethod.REPLACE
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
     * Extract any current table sort information and save it in the preferences. This
     * will persist sort preferences in cases where an update causes them to be dropped
     * from the UI.
     * 
     * @see TableService#saveSortDetails(java.lang.String, javafx.scene.control.TableColumn.SortType)
     */
    protected void saveSortDetails() {
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
