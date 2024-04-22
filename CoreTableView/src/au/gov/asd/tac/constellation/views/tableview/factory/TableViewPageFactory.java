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
package au.gov.asd.tac.constellation.views.tableview.factory;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.listeners.TableComparatorListener;
import au.gov.asd.tac.constellation.views.tableview.listeners.TableSortTypeListener;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Factory that updates the table with its new items after a pagination change.
 * It calculates from overall list of rows, which ones should be displayed. It
 * also ensures that sort and selection are maintained between page changes.
 *
 * @author formalhaunt
 */
public class TableViewPageFactory implements Callback<Integer, Node> {

    private final TablePane tablePane;

    private final ChangeListener<? super Comparator<? super ObservableList<String>>> tableComparatorListener;
    private final ChangeListener<? super TableColumn.SortType> tableSortTypeListener;

    private List<ObservableList<String>> allTableRows;

    /**
     * The previous list of rows that were used when calculating the new page of
     * data to display in the table. This is to track when graph selection
     * changes if the table is in selected only mode.
     * <p/>
     * If the graph is in selected only mode the full list of table rows is all
     * the nodes selected in the graph. So as that graph selection changes, so
     * does the list of table rows.
     */
    private List<ObservableList<String>> lastAllTableRows;

    private int maxRowsPerPage;

    /**
     * Creates a new table page factory.
     *
     * @param tablePane the pane that the table has been added to
     */
    public TableViewPageFactory(final TablePane tablePane) {
        this.tablePane = tablePane;

        this.tableComparatorListener = new TableComparatorListener(tablePane);
        this.tableSortTypeListener = new TableSortTypeListener(tablePane);
    }

    /**
     * Updates the factory with new data that is transient and changes between
     * pagination updates. Because this factory is stateful the same instance is
     * used between pagination updates and this method <b>MUST</b> be called if
     * any of these values have changed.
     * <p/>
     * <ul>
     * <li>
     * The full list of rows that the table is displaying. For example, changes
     * to the graph, selection, etc.
     * </li>
     * <li>
     * Changes to the page size through the preference selection
     * </li>
     * </ul>
     *
     * @param allTableRows all the rows that the table can currently display
     * @param maxRowsPerPage the maximum number of rows per page in the table
     */
    public void update(final List<ObservableList<String>> allTableRows, final int maxRowsPerPage) {
        this.allTableRows = allTableRows;
        this.maxRowsPerPage = maxRowsPerPage;
    }

    /**
     * Updates the table's data with the required subset of rows based on the
     * calculated page. The row subset is determined based on the passed page
     * index and the current max rows per page preference.
     * <p/>
     * The table will reset selection, sorting, etc. and trigger change events
     * when the data is updated with the new page of rows. In order to maintain
     * the tables state, all listers are removed, sort and selection backed up,
     * so that after the row update they can be restored.
     *
     * @param pageIndex the row index that the new table data should start from
     * @return the updated table
     */
    @Override
    public Node call(Integer pageIndex) {
        if (allTableRows != null) {

            // This action only effects selected only mode variables but runs no
            // matter if the table is in selected only mode or not.
            // The reasoning is that in selected only mode, if the row list changes
            // then that means the selection on the graph has changed. In this case
            // the expected behaviour is to clear the current table selection.
            if (lastAllTableRows == null || lastAllTableRows != allTableRows) {
                tablePane.getActiveTableReference().getSelectedOnlySelectedRows().clear();
                lastAllTableRows = allTableRows;
            }

            // Remove listeners so they are not triggered whilst the page is updated
            removeListeners();

            // Backup the current sort settings so that they can be restored after the page update
            final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> sortBackup = getCurrentSort();

            // Unbind the sorted row list comparator property
            tablePane.getActiveTableReference().getSortedRowList().comparatorProperty().unbind();

            // Calculate the rows that will be displayed in the table given the
            // passed index and max rows per page
            final int fromIndex = pageIndex * maxRowsPerPage;
            final int toIndex = Math.min(fromIndex + maxRowsPerPage, allTableRows.size());

            // Set the new page
            tablePane.getTable().getTableView()
                    .setItems(FXCollections.observableArrayList(allTableRows.subList(fromIndex, toIndex)));

            // Restore the sort details
            restoreSort(sortBackup);

            // Re-bind the sorted row list comparator property
            tablePane.getActiveTableReference().getSortedRowList().comparatorProperty()
                    .bind(tablePane.getTable().getTableView().comparatorProperty());

            // Restore the selection
            if (tablePane.getParentComponent().getCurrentState() != null) {
                if (tablePane.getParentComponent().getCurrentState().isSelectedOnly()) {
                    // The table IS IN selected only mode which means that the row selection
                    // is not based on what is selected in the graph but what rows are in
                    // the list electedOnlySelectedRows. Restore the selection from that list.
                    if (!tablePane.getActiveTableReference().getSelectedOnlySelectedRows().isEmpty()) {
                        final int[] selectedIndices = tablePane.getActiveTableReference().getSelectedOnlySelectedRows().stream()
                                .map(row -> tablePane.getTable().getTableView().getItems().indexOf(row))
                                .mapToInt(i -> i)
                                .toArray();

                        tablePane.getTable().getTableView().getSelectionModel()
                                .selectIndices(selectedIndices[0], selectedIndices);
                    }
                } else {
                    // The table IS NOT in selected only mode which means the row selection
                    // is based on the selection in the graph.
                    restoreSelectionFromGraph(tablePane.getParentComponent().getCurrentGraph(), 
                            tablePane.getParentComponent().getCurrentState(),
                            tablePane.getActiveTableReference().getElementIdToRowIndex());
                }
            }

            // Restore the listeners
            restoreListeners();
        }

        return tablePane.getTable().getTableView();
    }

    /**
     * Gets a listener that deals with sort order and sort column changes. The
     * listener will update the table and cause it to refresh.
     *
     * @return the sort change listener
     * @see TableComparatorListener
     */
    public ChangeListener<? super Comparator<? super ObservableList<String>>> getTableComparatorListener() {
        return tableComparatorListener;
    }

    /**
     * Gets a listener that deals with sort order and sort column changes. The
     * listener will update the table and cause it to refresh.
     *
     * @return the sort change listener
     * @see TableSortTypeListener
     */
    public ChangeListener<? super TableColumn.SortType> getTableSortTypeListener() {
        return tableSortTypeListener;
    }

    /**
     * Get the current sort that is set on the table. If no sort is set, then
     * both left and right parts of the pair will be null.
     * <p/>
     * Also removes the table sort listener from the sort column if sort is
     * active.
     *
     * @return a pair with the left being the column that sort is set on, and
     * the left being the direction
     */
    protected Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> getCurrentSort() {
        TableColumn<ObservableList<String>, ?> sortCol = null;
        TableColumn.SortType sortType = null;

        if (!tablePane.getTable().getTableView().getSortOrder().isEmpty()) {
            sortCol = tablePane.getTable().getTableView().getSortOrder().get(0);
            sortType = sortCol.getSortType();
            sortCol.sortTypeProperty().removeListener(getTableSortTypeListener());
        }

        return ImmutablePair.of(sortCol, sortType);
    }

    /**
     * Restores the sort on the table to the passed sort settings. If the sort
     * column (left) is null then no sort is restored.
     * <p/>
     * Restores the table sort listener to the sort column if sorting is
     * present.
     *
     * @param sortBackup the sort back up to restore in the table
     */
    protected void restoreSort(final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> sortBackup) {
        if (sortBackup.getLeft() != null) {
            tablePane.getTable().getTableView().getSortOrder().add(sortBackup.getLeft());
            sortBackup.getLeft().setSortType(sortBackup.getRight());
            sortBackup.getLeft().sortTypeProperty().addListener(getTableSortTypeListener());
        }
    }

    /**
     * Restore listeners after the table items have been updated so that future
     * events are responded to appropriately.
     */
    protected void restoreListeners() {
        tablePane.getTable().getTableView().getSelectionModel().getSelectedItems()
                .addListener(tablePane.getTable().getSelectedOnlySelectionListener());
        tablePane.getTable().getSelectedProperty().addListener(tablePane.getTable().getTableSelectionListener());
        tablePane.getActiveTableReference().getSortedRowList().comparatorProperty()
                .addListener(getTableComparatorListener());
    }

    /**
     * Remove listeners on the table so that item changes due to pagination
     * updates do not trigger random actions.
     */
    protected void removeListeners() {
        tablePane.getTable().getTableView().getSelectionModel().getSelectedItems()
                .removeListener(tablePane.getTable().getSelectedOnlySelectionListener());
        tablePane.getTable().getSelectedProperty().removeListener(tablePane.getTable().getTableSelectionListener());
        tablePane.getActiveTableReference().getSortedRowList()
                .comparatorProperty().removeListener(getTableComparatorListener());
    }

    /**
     * Restores the table selection from the current element selection in the
     * graph.
     * <p/>
     * It is expected that this method will be called on the FX Application
     * thread.
     *
     * @param graph the graph to read selection from
     * @param state the current table view state
     */
    protected void restoreSelectionFromGraph(final Graph graph, final TableViewState state,
            final Map<Integer, ObservableList<String>> elementIdToRowIndex) {
        // Double check that the table is not in selected only mode
        if (graph != null && state != null && !state.isSelectedOnly()) {
            if (!Platform.isFxApplicationThread()) {
                throw new IllegalStateException("Not processing on the JavaFX Application Thread");
            }

            // Gets the selected element IDs from the graph, maps them to table
            // rows and then maps the rows to the row index
            final int[] selectedIndices = getSelectedIds(graph, state).stream()
                    .map(id -> elementIdToRowIndex.get(id))
                    .map(row -> tablePane.getTable().getTableView().getItems().indexOf(row))
                    .mapToInt(i -> i)
                    .toArray();

            tablePane.getTable().getTableView().getSelectionModel().clearSelection();
            if (selectedIndices.length != 0) {
                tablePane.getTable().getTableView().getSelectionModel()
                        .selectIndices(selectedIndices[0], selectedIndices);
            }
        }
    }

    /**
     * Based on the tables current element type (vertex or transaction) get all
     * selected elements of that type in the graph and return their element IDs.
     * <p/>
     * This simply wraps a call to
     * {@link TableViewUtilities#getSelectedIds(Graph, TableViewState)} and used
     * primarily for testing purposes.
     *
     * @param graph the graph to read from
     * @param state the current table state
     * @return the IDs of the selected elements
     */
    protected List<Integer> getSelectedIds(final Graph graph, final TableViewState state) {
        return TableViewUtilities.getSelectedIds(graph, state);
    }
}
