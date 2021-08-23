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
    private final Map<Integer, ObservableList<String>> elementIdToRowIndex;
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;
    
    private final Set<ObservableList<String>> selectedOnlySelectedRows;
    
    private TableViewPageFactory pageFactory;
    
    private SortedList<ObservableList<String>> sortedRowList;
    
    private Pagination pagination;
    
    // This flag is shared by several listeners and so needs to be thread safe
    private volatile boolean sortingListenerActive = false;
    
    // Store details of sort order changes made upon column order change or table
    // preference loading - these are used to reinstate the sorting after data update
    private volatile String sortByColumnName = "";
    private volatile TableColumn.SortType sortByType = TableColumn.SortType.ASCENDING;
    
    public TableService(final SortedList<ObservableList<String>> sortedRowList,
                        final Map<Integer, ObservableList<String>> elementIdToRowIndex,
                        final Map<ObservableList<String>, Integer> rowToElementIdIndex) {
        this.sortedRowList = sortedRowList;
        
        this.elementIdToRowIndex = elementIdToRowIndex;
        this.rowToElementIdIndex = rowToElementIdIndex;
        
        this.selectedOnlySelectedRows = new HashSet<>();
        
        this.pagination = new Pagination();
    }
    
    public Pagination updatePagination(final int maxRowsPerPage) {
        return updatePagination(maxRowsPerPage, sortedRowList);
    }
    
    public Pagination updatePagination(final int maxRowsPerPage,
                                       final List<ObservableList<String>> newRowList) {
        Objects.requireNonNull(getPageFactory(), "The page factory must be set before "
                + "the pagination can be updated");
        
        final int numberOfPages = newRowList == null || newRowList.isEmpty()
                ? 1 : (int) Math.ceil(sortedRowList.size() / (double) maxRowsPerPage);
        pagination = new Pagination(numberOfPages);
        pageFactory.update(newRowList, elementIdToRowIndex, maxRowsPerPage);
        pagination.setPageFactory(pageFactory);
        
        return pagination;
    }
    
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
     * Save current sort order details, i.e. sort column name and order for
     * future reference. This required as the bespoke data loading in tables is
     * causing sort ordering to be removed - ie when users update column order.
     * By storing this sort information the values can be used to refresh the
     * sort order within updateSortOrder().
     *
     * @param columnName The name of the column sorting is being done on
     * @param sortType Direction of sorting
     */
    public void saveSortDetails(final String columnName,
                                final TableColumn.SortType sortType) {
        setSortByColumnName(columnName);
        setSortByType(sortType);
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

    public String getSortByColumnName() {
        return sortByColumnName;
    }

    public void setSortByColumnName(String sortByColumnName) {
        this.sortByColumnName = sortByColumnName;
    }

    public TableColumn.SortType getSortByType() {
        return sortByType;
    }

    public void setSortByType(TableColumn.SortType sortByType) {
        this.sortByType = sortByType;
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
}
