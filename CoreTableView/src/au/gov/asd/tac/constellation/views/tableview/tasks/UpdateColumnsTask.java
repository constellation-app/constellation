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
package au.gov.asd.tac.constellation.views.tableview.tasks;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

/**
 * Updates the visible columns on the table based on the current state. It also
 * adds the listener that is called when columns are updated or removed.
 *
 * @author formalhaunt
 */
public class UpdateColumnsTask implements Runnable {

    private final Table table;

    private Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap;

    private TableViewState state;

    /**
     * Used to track what table column changes are being done. Ensures that two
     * changes in a row that are the same are not handled twice.
     */
    private ListChangeListener.Change<? extends TableColumn<ObservableList<String>, ?>> lastChange;

    /**
     * Creates a new update table columns task.
     *
     * @param table the table that will be updated
     */
    public UpdateColumnsTask(final Table table) {
        this.table = table;
    }

    /**
     * Because this task maintains state the same one is used repeatedly and so
     * this is used to update the state and column reference map with the
     * current values before the task is run again.
     * <p/>
     * The column reference map contains all columns that are currently in the
     * table view but they may not all be in the column index as that may have
     * been refreshed.
     *
     * @param columnReferenceMap map of column name/text to column where some or
     * all of the columns may no longer be in the column index
     * @param state the state that will be used to update the table columns
     */
    public void reset(final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap,
            final TableViewState state) {
        this.columnReferenceMap = columnReferenceMap;
        this.state = state;
    }

    /**
     * Gets all the available columns and updates the visibility based on the
     * current table state. Clears and re-adds the columns with the updated
     * visibility setting to the actual table view and adds the column sort
     * listener.
     */
    @Override
    public void run() {
        Objects.requireNonNull(columnReferenceMap, "Update columns was started "
                + "before reset was called.");

        table.getTableView().getSelectionModel().selectedItemProperty().removeListener(
                table.getTableSelectionListener()
        );
        table.getTableView().getSelectionModel().getSelectedItems().removeListener(
                table.getSelectedOnlySelectionListener()
        );

        // clear all the existing columns in the table view so that they can be
        // redrawn when the column index is re-applied below
        columnReferenceMap.forEach((columnName, column) -> column.setGraphic(null));

        // set column visibility true in columnIndex if the column is in the
        // current table state for visible columns
        getActiveTableReference().getColumnIndex().forEach(column
                -> column.getTableColumn().setVisible(state.getColumnAttributes().stream()
                        .anyMatch(columnAttr
                                -> columnAttr.getFirst().equals(column.getAttributeNamePrefix())
                        && columnAttr.getSecond().equals(column.getAttribute())
                        )
                )
        );

        // clear all columns from the table view and re-add them from the
        // authoritive column index source that has just had its visibilities
        // updated from the table state
        table.getTableView().getColumns().clear();
        table.getTableView().getColumns().addAll(
                getActiveTableReference().getColumnIndex().stream()
                        .map(column -> column.getTableColumn())
                        .collect(Collectors.toList())
        );

        // add a listener that responds to column additions and removals
        table.getTableView().getColumns().addListener((final ListChangeListener.Change<? extends TableColumn<ObservableList<String>, ?>> change) -> {
            if (lastChange == null || !lastChange.equals(change)) {
                while (change.next()) {
                    if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                        saveSortDetails();

                        final List<TableColumn<ObservableList<String>, String>> columnIndexColumns
                                = getActiveTableReference().getColumnIndex().stream()
                                        .map(column -> column.getTableColumn())
                                        .collect(Collectors.toList());

                        final List<Tuple<String, Attribute>> orderedColumns
                                = change.getAddedSubList().stream()
                                        .map(c -> getActiveTableReference().getColumnIndex().get(columnIndexColumns.indexOf(c)))
                                        .map(column -> Tuple.create(
                                        column.getAttributeNamePrefix(),
                                        column.getAttribute()
                                ))
                                        .filter(columnAttr
                                                -> getTableViewTopComponent().getCurrentState().getColumnAttributes()
                                                .contains(columnAttr)
                                        )
                                        .collect(Collectors.toList());

                        getActiveTableReference().updateVisibleColumns(
                                getTableViewTopComponent().getCurrentGraph(),
                                getTableViewTopComponent().getCurrentState(),
                                orderedColumns,
                                UpdateMethod.REPLACE
                        );
                    }
                }
                lastChange = change;
            }
        });

        table.getTableView().getSelectionModel().getSelectedItems().addListener(
                table.getSelectedOnlySelectionListener()
        );
        table.getTableView().getSelectionModel().selectedItemProperty().addListener(
                table.getTableSelectionListener()
        );
    }

    /**
     * Extract any current table sort information and save it in the
     * preferences. This will persist sort preferences in cases where an update
     * causes them to be dropped from the UI.
     *
     * @see ActiveTableReference#saveSortDetails(java.lang.String,
     * javafx.scene.control.TableColumn.SortType)
     */
    protected void saveSortDetails() {
        if (table.getTableView().getSortOrder() != null && !table.getTableView().getSortOrder().isEmpty()) {
            // A column was selected to sort by, save its name and direction
            getActiveTableReference().saveSortDetails(table.getTableView().getSortOrder().get(0).getText(),
                    table.getTableView().getSortOrder().get(0).getSortType());
        } else {
            // no column is selected, clear any previously stored information.
            getActiveTableReference().saveSortDetails("", TableColumn.SortType.ASCENDING);
        }
    }

    /**
     * Convenience method for accessing the active table reference.
     *
     * @return the active table reference
     */
    private ActiveTableReference getActiveTableReference() {
        return table.getParentComponent().getActiveTableReference();
    }

    /**
     * Convenience method for accessing the table view top component.
     *
     * @return the table view top component
     */
    private TableViewTopComponent getTableViewTopComponent() {
        return table.getParentComponent().getParentComponent();
    }
}
