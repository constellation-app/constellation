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
package au.gov.asd.tac.constellation.views.tableview2.factory;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author formalhaunt
 */
public class TableViewPageFactory implements Callback<Integer, Node> {
    private static final Logger LOGGER = Logger.getLogger(TableViewPageFactory.class.getName());
    
    private final TableViewTopComponent tableTopComponent;
    
    private final Table table;
    
    private final SortedList<ObservableList<String>> sortedRowList;
    
    private final Set<ObservableList<String>> selectedOnlySelectedRows;
    
    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ChangeListener<? super Comparator<? super ObservableList<String>>> tableComparatorListener;
    private ChangeListener<? super TableColumn.SortType> tableSortTypeListener;
    
    private ListChangeListener selectedOnlySelectionListener;
    
    private List<ObservableList<String>> newRowList;
    private List<ObservableList<String>> lastRowList;
    
    private Map<Integer, ObservableList<String>> elementIdToRowIndex;
    
    private int maxRowsPerPage;
    
    public TableViewPageFactory(final SortedList<ObservableList<String>> sortedRowList,
                                final Set<ObservableList<String>> selectedOnlySelectedRows,
                                final TableViewTopComponent tableTopComponent,
                                final Table table) {
        this.sortedRowList = sortedRowList;
        this.selectedOnlySelectedRows = selectedOnlySelectedRows;
        this.tableTopComponent = tableTopComponent;
        this.table = table;
    }
    
    public void update(final List<ObservableList<String>> newRowList,
                       final Map<Integer, ObservableList<String>> elementIdToRowIndex,
                       final int maxRowsPerPage) {
        this.newRowList = newRowList;
        this.elementIdToRowIndex = elementIdToRowIndex;
        this.maxRowsPerPage = maxRowsPerPage;
    }
    
    @Override
    public Node call(Integer pageIndex) {
        if (newRowList != null) {
            // if the list of rows making up pages changes, we need to clear the list of selected rows
            if (lastRowList == null || lastRowList != newRowList) {
                selectedOnlySelectedRows.clear();
                lastRowList = newRowList;
            }

            final int fromIndex = pageIndex * maxRowsPerPage;
            final int toIndex = Math.min(fromIndex + maxRowsPerPage, newRowList.size());

            table.getSelectedProperty().removeListener(tableSelectionListener);
            table.getTableView().getSelectionModel().getSelectedItems().removeListener(selectedOnlySelectionListener);
            sortedRowList.comparatorProperty().removeListener(tableComparatorListener);

            //get the previous sort details so that we don't lose it upon switching pages
            TableColumn<ObservableList<String>, ?> sortCol = null;
            TableColumn.SortType sortType = null;
            if (!table.getTableView().getSortOrder().isEmpty()) {
                sortCol = table.getTableView().getSortOrder().get(0);
                sortType = sortCol.getSortType();
                sortCol.sortTypeProperty().removeListener(tableSortTypeListener);
            }

            sortedRowList.comparatorProperty().unbind();

            table.getTableView().setItems(FXCollections.observableArrayList(newRowList.subList(fromIndex, toIndex)));

            //restore the sort details
            if (sortCol != null) {
                table.getTableView().getSortOrder().add(sortCol);
                sortCol.setSortType(sortType);
                sortCol.sortTypeProperty().addListener(tableSortTypeListener);
            }

            sortedRowList.comparatorProperty().bind(table.getTableView().comparatorProperty());
            updateSelectionFromFXThread(tableTopComponent.getCurrentGraph(), tableTopComponent.getCurrentState());
            if (tableTopComponent.getCurrentState() != null 
                    && tableTopComponent.getCurrentState().isSelectedOnly()) {
                final int[] selectedIndices = selectedOnlySelectedRows.stream()
                        .map(row -> table.getTableView().getItems().indexOf(row))
                        .mapToInt(i -> i)
                        .toArray();
                if (!selectedOnlySelectedRows.isEmpty()) {
                    table.getTableView().getSelectionModel()
                            .selectIndices(selectedIndices[0], selectedIndices);
                }
            }

            sortedRowList.comparatorProperty().addListener(tableComparatorListener);
            table.getTableView().getSelectionModel().getSelectedItems().addListener(selectedOnlySelectionListener);
            table.getSelectedProperty().addListener(tableSelectionListener);
        }

        return table.getTableView();
    }
    
    public void setTableSelectionListener(ChangeListener<ObservableList<String>> tableSelectionListener) {
        this.tableSelectionListener = tableSelectionListener;
    }

    public void setTableComparatorListener(ChangeListener<? super Comparator<? super ObservableList<String>>> tableComparatorListener) {
        this.tableComparatorListener = tableComparatorListener;
    }

    public void setTableSortTypeListener(ChangeListener<? super TableColumn.SortType> tableSortTypeListener) {
        this.tableSortTypeListener = tableSortTypeListener;
    }

    public void setSelectedOnlySelectionListener(ListChangeListener selectedOnlySelectionListener) {
        this.selectedOnlySelectionListener = selectedOnlySelectionListener;
    }
    
    /**
     * A version of the updateSelection(Graph, TableViewState) function which is
     * to be run on the JavaFX Application Thread
     *
     * @param graph the graph to read selection from
     * @param state the current table view state
     */
    protected void updateSelectionFromFXThread(final Graph graph, final TableViewState state) {
        if (graph != null && state != null) {

            if (!Platform.isFxApplicationThread()) {
                throw new IllegalStateException("Not processing on the JavaFX Application Thread");
            }

            // get graph selection
            if (!state.isSelectedOnly()) {
                final List<Integer> selectedIds = new ArrayList<>();
                final int[][] selectedIndices = new int[1][1];
                final Thread selectedIdsThread = new Thread("Update Selection from FX Thread: Get Selected Ids") {
                    @Override
                    public void run() {
                        selectedIds.addAll(getSelectedIds(graph, state));

                        // update table selection
                        selectedIndices[0] = selectedIds.stream()
                                .map(id -> elementIdToRowIndex.get(id))
                                .map(row -> table.getTableView().getItems().indexOf(row))
                                .mapToInt(i -> i)
                                .toArray();
                    }
                };
                selectedIdsThread.start();
                try {
                    selectedIdsThread.join();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "InterruptedException encountered while updating table selection");
                    Thread.currentThread().interrupt();
                }

                table.getTableView().getSelectionModel().clearSelection();
                if (!selectedIds.isEmpty()) {
                    table.getTableView().getSelectionModel().selectIndices(selectedIndices[0][0], selectedIndices[0]);
                }
            }
        }
    }
    
    /**
     * Based on the tables current element type (vertex or transaction) get all
     * selected elements of that type in the graph and return their element IDs.
     *
     * @param graph the graph to read from
     * @param state the current table state
     * @return the IDs of the selected elements
     */
    protected List<Integer> getSelectedIds(final Graph graph,
                                           final TableViewState state) {
        final List<Integer> selectedIds = new ArrayList<>();
        final ReadableGraph readableGraph = graph.getReadableGraph();
        try {
            final boolean isVertex = state.getElementType() == GraphElementType.VERTEX;
            final int selectedAttributeId = isVertex
                    ? VisualConcept.VertexAttribute.SELECTED.get(readableGraph)
                    : VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
            final int elementCount = isVertex
                    ? readableGraph.getVertexCount()
                    : readableGraph.getTransactionCount();
            for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                final int elementId = isVertex
                        ? readableGraph.getVertex(elementPosition)
                        : readableGraph.getTransaction(elementPosition);
                if (selectedAttributeId != Graph.NOT_FOUND
                        && readableGraph.getBooleanValue(selectedAttributeId, elementId)) {
                    selectedIds.add(elementId);
                }
            }
            return selectedIds;
        } finally {
            readableGraph.release();
        }
    }
}
