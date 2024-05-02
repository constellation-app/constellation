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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Contains the references to the tables current state and how it relates to the
 * graph.
 *
 * @author formalhaunt
 */
public class ActiveTableReference {

    /**
     * Holds a map of graph element IDs to table rows.
     */
    private final Map<Integer, ObservableList<String>> elementIdToRowIndex;

    /**
     * Holds a map of table rows to graph element IDs.
     */
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;

    /**
     * Holds a set of rows from the table that are currently selected. This set
     * is only populated though when the "Selected Only" mode is active on the
     * table. This will be a sub-set of {@link #sortedRowList}.
     */
    private final Set<ObservableList<String>> selectedOnlySelectedRows;

    /**
     * A page factory that is used to determine what rows from the table data
     * will be currently displayed in the table or in other words what rows
     * constitute the current page.
     */
    private final TableViewPageFactory pageFactory;

    /**
     * A sorted list of all the rows in the table.
     */
    private SortedList<ObservableList<String>> sortedRowList;

    /**
     * A list representing the current column setup of the table and how it
     * relates to the graph
     */
    private final List<Column> columnIndex;

    /**
     * The current pagination for the table. This is used by the table to
     * determine which rows to display on each page.
     */
    private Pagination pagination;

    /**
     * Holds the current user preferences set for the table.
     */
    private UserTablePreferences userTablePreferences;

    /**
     * This flag is shared by listeners that respond to sort change events on
     * the table. This flag is to ensure that multiple pagination updates do not
     * happen if multiple sort events occur in close succession.
     */
    private volatile boolean sortingListenerActive = false;

    /**
     * Create a new table reference.
     *
     * @param pageFactory the table page factory that determines which rows will
     * be displayed on the current page
     */
    public ActiveTableReference(final TableViewPageFactory pageFactory) {
        this.sortedRowList = new SortedList<>(FXCollections.observableArrayList());

        this.columnIndex = new CopyOnWriteArrayList<>();

        this.elementIdToRowIndex = new HashMap<>();
        this.rowToElementIdIndex = new HashMap<>();

        this.pageFactory = pageFactory;

        this.selectedOnlySelectedRows = new HashSet<>();

        this.pagination = new Pagination();

        this.userTablePreferences = new UserTablePreferences();
        this.userTablePreferences.setMaxRowsPerPage(500);
    }

    /**
     * Updates the pagination object based on the max rows per page and the
     * current row list.
     *
     * @param maxRowsPerPage the maximum number of rows per page that can be
     * displayed in the table
     * @param tablePane the pane that the table is rendered on
     * @return the newly created {@link Pagination}
     * @see #updatePagination(int, java.util.List)
     */
    public Pagination updatePagination(final int maxRowsPerPage, final TablePane tablePane) {
        return updatePagination(maxRowsPerPage, getSortedRowList(), tablePane);
    }

    /**
     * Updates the pagination object based on the max rows per page and the size
     * of the new row list.
     * </p>
     * Determines how many pages will be needed to display all the passed rows.
     * Creates a new {@link Pagination} with the calculated page count. Then
     * updates the current {@link TableViewPageFactory} with this new
     * information and passes it to the newly created {@link Pagination}.
     * <p/>
     * This method cannot be called if the {@link #pageFactory} variable has not
     * been set.
     *
     * @param maxRowsPerPage the maximum number of rows per page that can be
     * displayed in the table
     * @param newRowList the rows to be displayed in the table
     * @param tablePane the pane that the table is rendered on
     * @return the newly created {@link Pagination}
     */
    public Pagination updatePagination(final int maxRowsPerPage, final List<ObservableList<String>> newRowList, 
            final TablePane tablePane) {
        Objects.requireNonNull(getPageFactory(), "The page factory must be set before the pagination can be updated");

        final int numberOfPages = newRowList == null || newRowList.isEmpty()
                ? 1 : (int) Math.ceil(newRowList.size() / (double) maxRowsPerPage);
        pagination = new Pagination(numberOfPages);

        getPageFactory().update(newRowList, maxRowsPerPage);
        pagination.setPageFactory(getPageFactory());

        Platform.runLater(() -> tablePane.setCenter(getPagination()));

        return pagination;
    }

    /**
     * Updates the visible columns in the table's state. Once the new state is
     * determined the it is updated in the graph.
     * <p/>
     * The following are the different ways in which the visible columns can be
     * updated
     * <ul>
     * <li>
     * <b>ADD:</b> the passed columns are added on top of existing visible
     * columns in the current state.
     * </li>
     * <li>
     * <b>REMOVE:</b> the passed columns are removed from the visible columns in
     * the current state.
     * </li>
     * <li>
     * <b>REPLACE:</b> the passed columns become the new visible columns
     * </li>
     * </ul>
     *
     * @param graph the graph to update with the new state
     * @param state the current table state
     * @param columnAttributes the column attributes to update the state with
     * @param updateState the manner in which the state will be updated with the
     * column attributes
     * @return a {@link Future<?>} object of the plugin execution.
     */
    public Future updateVisibleColumns(final Graph graph, final TableViewState state, 
            final List<Tuple<String, Attribute>> columnAttributes, final UpdateMethod updateState) {
        if (graph != null && state != null) {
            final TableViewState newState = new TableViewState(state);

            final List<Tuple<String, Attribute>> newColumnAttributes = new ArrayList<>();
            switch (updateState) {
                case ADD -> {
                    if (newState.getColumnAttributes() != null) {
                        newColumnAttributes.addAll(newState.getColumnAttributes());
                    }
                    newColumnAttributes.addAll(columnAttributes);
                }
                case REMOVE -> {
                    if (newState.getColumnAttributes() != null) {
                        newColumnAttributes.addAll(newState.getColumnAttributes());
                    }
                    newColumnAttributes.removeAll(columnAttributes);
                }
                case REPLACE -> newColumnAttributes.addAll(columnAttributes);
            }

            newState.setColumnAttributes(newColumnAttributes);
            return PluginExecution.withPlugin(new UpdateStatePlugin(newState)).executeLater(graph);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Save current sort order details, in the table preferences for future
     * reference.
     * <p/>
     * This required as the bespoke data loading in tables is causing sort
     * ordering to be removed - i.e. when users update column order.
     * </p>
     * By storing this sort information the values can be used to restore the
     * sort order within {@link Table#updateSortOrder()}.
     *
     * @param columnName The name of the column sorting is being done on
     * @param sortType Direction of sorting
     */
    public void saveSortDetails(final String columnName, final TableColumn.SortType sortType) {
        getUserTablePreferences().setSortByColumn(ImmutablePair.of(columnName, sortType));
    }

    /**
     * Gets a list representing the current column setup of the table and how it
     * relates to the graph. The list describes how each column relates to
     * either a source vertex, destination vertex or transaction.
     *
     * @return the table column representation
     * @see Column
     */
    public final List<Column> getColumnIndex() {
        return columnIndex;
    }

    /**
     * Get the current pagination for the table. This is used by the table to
     * determine which rows to display on each page.
     *
     * @return the table pagination
     */
    public Pagination getPagination() {
        return pagination;
    }

    /**
     * Gets the flag indicating if a sort listener is currently responding to a
     * sort event. The {@link #sortingListenerActive} field is {@code volatile}.
     *
     * @return true if a sort listener is currently active, false otherwise
     */
    public boolean isSortingListenerActive() {
        return sortingListenerActive;
    }

    /**
     * Sets the flag indicating if a sort listener is currently responding to a
     * sort event. The {@link #sortingListenerActive} field is {@code volatile}.
     *
     * @param sortingListenerActive true if a sort listener is currently active,
     * false otherwise
     */
    public void setSortingListenerActive(final boolean sortingListenerActive) {
        this.sortingListenerActive = sortingListenerActive;
    }

    /**
     * Gets a map of graph element IDs to table rows.
     *
     * @return graph element to table row map
     */
    public Map<Integer, ObservableList<String>> getElementIdToRowIndex() {
        return elementIdToRowIndex;
    }

    /**
     * Gets a map of table rows to graph element IDs.
     *
     * @return table row to graph element map
     */
    public Map<ObservableList<String>, Integer> getRowToElementIdIndex() {
        return rowToElementIdIndex;
    }

    /**
     * Gets a list of all the rows in the table ignoring any pagination and
     * sorted based on the current sort settings.
     *
     * @return all the table rows
     */
    public SortedList<ObservableList<String>> getSortedRowList() {
        return sortedRowList;
    }

    /**
     * Sets the list of table rows that should be used when determining what the
     * table displays.
     *
     * @param sortedRowList the new table rows
     */
    public void setSortedRowList(final SortedList<ObservableList<String>> sortedRowList) {
        this.sortedRowList = sortedRowList;
    }

    /**
     * Gets page factory that is used to determine what rows from the table data
     * will be currently displayed in the table or in other words what rows
     * constitute the current page.
     *
     * @return the table page factory
     */
    public TableViewPageFactory getPageFactory() {
        return pageFactory;
    }

    /**
     * Gets a set of rows from the table that are currently selected. This set
     * is only populated though when the "Selected Only" mode is active on the
     * table.
     *
     * @return the currently selected table rows
     */
    public Set<ObservableList<String>> getSelectedOnlySelectedRows() {
        return selectedOnlySelectedRows;
    }

    /**
     * Get the current user preferences set for the table.
     *
     * @return the current user table preferences
     */
    public UserTablePreferences getUserTablePreferences() {
        return userTablePreferences;
    }

    /**
     * Sets the user preferences for the table.
     *
     * @param userTablePreferences the new user table preferences
     */
    public void setUserTablePreferences(final UserTablePreferences userTablePreferences) {
        this.userTablePreferences = userTablePreferences;
    }
}
