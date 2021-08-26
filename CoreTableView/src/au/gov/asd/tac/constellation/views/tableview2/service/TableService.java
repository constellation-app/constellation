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
package au.gov.asd.tac.constellation.views.tableview2.service;

import au.gov.asd.tac.constellation.views.tableview2.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview2.UpdateMethod;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;

/**
 *
 * @author formalhaunt
 */
public class TableService {
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
     * table.
     */
    private final Set<ObservableList<String>> selectedOnlySelectedRows;
    
    private TableViewPageFactory pageFactory;
    
    private SortedList<ObservableList<String>> sortedRowList;
    
    /**
     * The current pagination for the table. This is used by the table to determine
     * which rows to display on each page.
     */
    private Pagination pagination;
    
    /**
     * Holds the current user preferences set for the table.
     */
    private TablePreferences tablePreferences = new TablePreferences();
    
    /**
     * This flag is shared by listeners that respond to sort change events on
     * the table. This flag is to ensure that multiple pagination updates do not
     * happen if multiple sort event occur in close succession.
     */    
    private volatile boolean sortingListenerActive = false;
    
    /**
     * Create a new table service.
     *
     * @param sortedRowList
     * @param elementIdToRowIndex a map of graph IDs to table rows
     * @param rowToElementIdIndex a map of table rows to graph IDs
     */
    public TableService(final SortedList<ObservableList<String>> sortedRowList,
                        final Map<Integer, ObservableList<String>> elementIdToRowIndex,
                        final Map<ObservableList<String>, Integer> rowToElementIdIndex) {
        this.sortedRowList = sortedRowList;
        
        this.elementIdToRowIndex = elementIdToRowIndex;
        this.rowToElementIdIndex = rowToElementIdIndex;
        
        this.selectedOnlySelectedRows = new HashSet<>();
        
        this.pagination = new Pagination();
    }
    
    /**
     * Updates the pagination object based on the max rows per page and the current
     * row list.
     *
     * @param maxRowsPerPage the maximum number of rows per page that can be displayed
     *     in the table
     * @return the newly created {@link Pagination}
     * @see #updatePagination(int, java.util.List)
     */
    public Pagination updatePagination(final int maxRowsPerPage) {
        return updatePagination(maxRowsPerPage, sortedRowList);
    }
    
    /**
     * Updates the pagination object based on the max rows per page and the size
     * of the new row list.
     * </p>
     * Determines how many pages will be needed to display all the passed rows. Creates
     * a new {@link Pagination} with the calculated page count. Then updates the current
     * {@link TableViewPageFactory} with this new information and passes it to 
     * the newly created {@link Pagination}.
     * <p/>
     * This method cannot be called if the {@link #pageFactory} variable has not
     * been set.
     *
     * @param maxRowsPerPage the maximum number of rows per page that can be displayed
     *     in the table
     * @param newRowList the rows to be displayed in the table
     * @return the newly created {@link Pagination}
     */
    public Pagination updatePagination(final int maxRowsPerPage,
                                       final List<ObservableList<String>> newRowList) {
        Objects.requireNonNull(getPageFactory(), "The page factory must be set before "
                + "the pagination can be updated");
        
        final int numberOfPages = newRowList == null || newRowList.isEmpty()
                ? 1 : (int) Math.ceil(newRowList.size() / (double) maxRowsPerPage);
        pagination = new Pagination(numberOfPages);
        // TODO Should this create a copy of page factory first then update?
        pageFactory.update(newRowList, elementIdToRowIndex, maxRowsPerPage);
        pagination.setPageFactory(pageFactory);
        
        return pagination;
    }
    
    /**
     * 
     * @param graph
     * @param state
     * @param columnAttributes
     * @param updateState 
     */
    public void updateVisibleColumns(final Graph graph,
                                     final TableViewState state,
                                     final List<Tuple<String, Attribute>> columnAttributes,
                                     final UpdateMethod updateState) {
        if (graph != null && state != null) {
            final TableViewState newState = new TableViewState(state);

            final List<Tuple<String, Attribute>> newColumnAttributes = new ArrayList<>();
            switch (updateState) {
                case ADD:
                    if (newState.getColumnAttributes() != null) {
                        newColumnAttributes.addAll(newState.getColumnAttributes());
                    }
                    newColumnAttributes.addAll(columnAttributes);
                    break;
                case REMOVE:
                    if (newState.getColumnAttributes() != null) {
                        newColumnAttributes.addAll(newState.getColumnAttributes());
                    }
                    newColumnAttributes.removeAll(columnAttributes);
                    break;
                case REPLACE:
                    newColumnAttributes.addAll(columnAttributes);
                    break;
            }

            newState.setColumnAttributes(newColumnAttributes);
            PluginExecution.withPlugin(
                    new TableViewUtilities.UpdateStatePlugin(newState)
            ).executeLater(graph);
        }
    }
    
    /**
     * Save current sort order details, in the table preferences for
     * future reference.
     * <p/>
     * This required as the bespoke data loading in tables is
     * causing sort ordering to be removed - i.e. when users update column order.
     * </p>
     * By storing this sort information the values can be used to restore the
     * sort order within {@link Table#updateSortOrder()}.
     *
     * @param columnName The name of the column sorting is being done on
     * @param sortType Direction of sorting
     */
    public void saveSortDetails(final String columnName,
                                final TableColumn.SortType sortType) {
        getTablePreferences().setSortByColumn(Map.of(columnName, sortType));
    }
    
    public Pagination getPagination() {
        return pagination;
    }

    public boolean isSortingListenerActive() {
        return sortingListenerActive;
    }

    public void setSortingListenerActive(boolean sortingListenerActive) {
        this.sortingListenerActive = sortingListenerActive;
    }

    public Map<Integer, ObservableList<String>> getElementIdToRowIndex() {
        return elementIdToRowIndex;
    }

    public Map<ObservableList<String>, Integer> getRowToElementIdIndex() {
        return rowToElementIdIndex;
    }

    public SortedList<ObservableList<String>> getSortedRowList() {
        return sortedRowList;
    }

    public void setSortedRowList(SortedList<ObservableList<String>> sortedRowList) {
        this.sortedRowList = sortedRowList;
    }

    public TableViewPageFactory getPageFactory() {
        return pageFactory;
    }

    public void setPageFactory(TableViewPageFactory pageFactory) {
        this.pageFactory = pageFactory;
    }

    public Set<ObservableList<String>> getSelectedOnlySelectedRows() {
        return selectedOnlySelectedRows;
    }

    public TablePreferences getTablePreferences() {
        return tablePreferences;
    }

    public void setTablePreferences(TablePreferences tablePreferences) {
        this.tablePreferences = tablePreferences;
    }
}
