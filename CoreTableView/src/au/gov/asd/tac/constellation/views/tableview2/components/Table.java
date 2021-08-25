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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import static au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities.TABLE_LOCK;
import au.gov.asd.tac.constellation.views.tableview2.factory.TableCellFactory;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.tasks.UpdateColumnsTask;
import au.gov.asd.tac.constellation.views.tableview2.tasks.UpdateDataTask;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author formalhaunt
 */
public class Table {
    private static final Logger LOGGER = Logger.getLogger(Table.class.getName());
    
    private static final String ATTEMPT_PROCESS_JAVAFX = "Attempting to process on the JavaFX Application Thread";
    private static final String ATTEMPT_PROCESS_EDT = "Attempting to process on the EDT";

    private final TableViewTopComponent tableTopComponent;
    private final TableViewPane tablePane;
    private final TableView<ObservableList<String>> tableView;
    
    private final TableService tableService;
    
    private final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columnIndex;
    
    /**
     * Cache strings used in table cells to significantly reduce memory used by
     * the same string repeated in columns and rows.
     */
    private final ImmutableObjectCache displayTextCache;
    
    private final UpdateColumnsTask updateColumnsTask;
    
    public Table(final TableViewTopComponent tableTopComponent,
                 final TableViewPane tablePane,
                 final TableService tableService) {
        this.tableTopComponent = tableTopComponent;
        this.tablePane = tablePane;
        this.tableService = tableService;
        
        this.tableView = new TableView<>();
        this.tableView.itemsProperty().addListener((v, o, n) -> tableView.refresh());
        this.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.tableView.setPadding(new Insets(5));
        this.tableView.setCache(false); // TODO: experiment with caching
        
        this.columnIndex = new CopyOnWriteArrayList<>();
        
        this.displayTextCache = new ImmutableObjectCache();
        
        this.updateColumnsTask = new UpdateColumnsTask(tableTopComponent, tableView, columnIndex,
                tableService);
    }
    
    public TableView<ObservableList<String>> getTableView() {
        return tableView;
    }
    
    public ReadOnlyObjectProperty<ObservableList<String>> getSelectedProperty() {
        return tableView.getSelectionModel().selectedItemProperty();
    }
    
    /**
     * Update the columns in the table using the graph and state.
     * <p>
     * Note that column references are reused where possible to ensure certain
     * toolbar/menu operations to work correctly.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateColumns(final Graph graph,
                              final TableViewState state,
                              final ChangeListener<ObservableList<String>> tableSelectionListener,
                              final ListChangeListener selectedOnlySelectionListener) {
        synchronized (TABLE_LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_JAVAFX);
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_EDT);
                }

                // clear current columnIndex, but cache the column objects for reuse
                final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap =
                        columnIndex.stream()
                                .collect(
                                        Collectors.toMap(
                                                columnTuple -> columnTuple.getThird().getText(),
                                                c -> c.getThird(),
                                                (e1, e2) -> e1
                                        )
                                );
                columnIndex.clear();

                // update columnIndex based on graph attributes
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    stuff(readableGraph, GraphElementType.VERTEX,
                            GraphRecordStoreUtilities.SOURCE, columnReferenceMap);
                    
                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        stuff(readableGraph, GraphElementType.TRANSACTION,
                                GraphRecordStoreUtilities.TRANSACTION, columnReferenceMap);
                        stuff(readableGraph, GraphElementType.VERTEX,
                                GraphRecordStoreUtilities.DESTINATION, columnReferenceMap);
                    }
                } finally {
                    readableGraph.release();
                }

                // if there are no visible columns specified, write the key columns to the state
                if (state.getColumnAttributes() == null) {
                    final ColumnVisibilityContextMenu columnVisibilityContextMenu
                            = new ColumnVisibilityContextMenu(tableTopComponent, this,
                                    tableService);
                    columnVisibilityContextMenu.init();
                    
                    final MenuItem keyColumns = columnVisibilityContextMenu.getContextMenu()
                            .getItems().get(2);
                    keyColumns.fire();
                    
                    return;
                }

                // sort columns in columnIndex by state, prefix and attribute name
                columnIndex.sort(new ColumnIndexSort(state));

                // style and format columns in columnIndex
                columnIndex.forEach(columnTuple -> {
                    final TableColumn<ObservableList<String>, String> column = columnTuple.getThird();

                    // assign cells to columns
                    column.setCellValueFactory(cellData -> {
                        final int cellIndex = tableView.getColumns().indexOf(cellData.getTableColumn());
                        if (cellIndex < cellData.getValue().size()) {
                            return new SimpleStringProperty(cellData.getValue().get(cellIndex));
                        } else {
                            return null;
                        }
                    });

                    // assign values and styles to cells
                    column.setCellFactory(cellColumn -> new TableCellFactory(cellColumn, this));
                });

                // The update columns task holds state between executions. So we need to
                // reset some fields each time before it is run.
                updateColumnsTask.reset(columnReferenceMap, state, tableSelectionListener,
                        selectedOnlySelectionListener);
                Platform.runLater(updateColumnsTask);
            }
        }
    }
    
    /**
     * Update the data in the table using the graph and state.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateData(final Graph graph,
                           final TableViewState state,
                           final ProgressBar progressBar,
                           final ChangeListener<ObservableList<String>> tableSelectionListener,
                           final ListChangeListener selectedOnlySelectionListener) {
        synchronized (TABLE_LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_JAVAFX);
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_EDT);
                }

                // set progress indicator
                Platform.runLater(() -> {
                    tablePane.setCenter(progressBar.getProgressPane());
                });

                // update data on a new thread so as to not interrupt the progress indicator
                tableService.getElementIdToRowIndex().clear();
                tableService.getRowToElementIdIndex().clear();

                // build table data based on attribute values on the graph
                final List<ObservableList<String>> rows = new ArrayList<>();
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        final int selectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int transactionCount = readableGraph.getTransactionCount();
                        
                        IntStream.range(0, transactionCount).forEach(transactionPosition -> {
                            final int transactionId = readableGraph.getTransaction(transactionPosition);
                            boolean isSelected = false;
                            
                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                isSelected = readableGraph.getBooleanValue(selectedAttributeId, transactionId);
                            }
                            
                            if (!state.isSelectedOnly() || isSelected) {
                                extractRowDataMaybe(readableGraph, rows, transactionId);
                            }
                        });
                    } else {
                        final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
                        final int vertexCount = readableGraph.getVertexCount();
                        
                        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                            final int vertexId = readableGraph.getVertex(vertexPosition);
                            boolean isSelected = false;
                            
                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                isSelected = readableGraph.getBooleanValue(selectedAttributeId, vertexId);
                            }
                            
                            if (!state.isSelectedOnly() || isSelected) {
                                extractOtherRowDataMaybe(readableGraph, rows, vertexId);
                            }
                        }
                    }
                } finally {
                    readableGraph.release();
                }

                final CountDownLatch updateDataLatch = new CountDownLatch(1);

                Platform.runLater(new UpdateDataTask(tablePane, this, rows, tableSelectionListener,
                        selectedOnlySelectionListener, updateDataLatch, tableService));

                // Wait for the update data task to complete.
                try {
                    updateDataLatch.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "InterruptedException encountered while updating table data");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Update the table selection using the graph and state.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to read selection from.
     * @param state the current table view state.
     */
    public void updateSelection(final Graph graph,
                                final TableViewState state,
                                final ChangeListener<ObservableList<String>> tableSelectionListener,
                                final ListChangeListener selectedOnlySelectionListener) {
        synchronized (TABLE_LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_JAVAFX);
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_EDT);
                }

                // get graph selection
                if (!state.isSelectedOnly()) {
                    final List<Integer> selectedIds = new ArrayList<>();
                    final ReadableGraph readableGraph = graph.getReadableGraph();
                    addToSelectedIds(selectedIds, readableGraph, state);

                    // update table selection
                    final int[] selectedIndices = selectedIds.stream()
                            .map(id-> tableService.getElementIdToRowIndex().get(id))
                            .map(row -> tableView.getItems().indexOf(row)).mapToInt(i -> i).toArray();

                    Platform.runLater(() -> {
                        getSelectedProperty().removeListener(tableSelectionListener);
                        tableView.getSelectionModel().getSelectedItems().removeListener(selectedOnlySelectionListener);
                        tableView.getSelectionModel().clearSelection();
                        if (!selectedIds.isEmpty()) {
                            tableView.getSelectionModel().selectIndices(selectedIndices[0], selectedIndices);
                        }
                        tableView.getSelectionModel().getSelectedItems().addListener(selectedOnlySelectionListener);
                        getSelectedProperty().addListener(tableSelectionListener);
                    });
                }
            }
        }
    }
    
    /**
     * If sort details have been stored, reapply this sorting to the tableview.
     *
     */
    public void updateSortOrder() {
        // Try to find column with name matching saved sort order/type details
        final Pair<String, TableColumn.SortType> sortPreference
                = tableService.getTablePreferences().getSort();
        
        if (sortPreference != null && !sortPreference.getLeft().isBlank()) {
            for (final TableColumn<ObservableList<String>, ?> column : tableView.getColumns()) {
                if (column.getText().equals(sortPreference.getLeft())) {
                    column.setSortType(sortPreference.getRight());
                    tableView.getSortOrder().setAll(column);
                    return;
                }
            }
        }
    }
    
    /**
     * Adds vertex/transaction ids from a graph to a list of ids if the
     * vertex/transaction is selected
     *
     * @param selectedIds the list that is being added to
     * @param readableGraph the graph to read from
     * @param state the current table view state
     */
    private void addToSelectedIds(final List<Integer> selectedIds,
                                  final ReadableGraph readableGraph,
                                  final TableViewState state) {
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
        } finally {
            readableGraph.release();
        }
    }
    
    public void extractOtherRowDataMaybe(final ReadableGraph readableGraph,
                                         final List<ObservableList<String>> rows,
                                         final int vertexId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();
                                
        columnIndex.forEach(columnTuple -> {
            final int attributeId = readableGraph
                    .getAttribute(columnTuple.getSecond().getElementType(),
                            columnTuple.getSecond().getName());
            final AbstractAttributeInteraction<?> interaction= AbstractAttributeInteraction
                    .getInteraction(columnTuple.getSecond().getAttributeType());
            final Object attributeValue = readableGraph
                    .getObjectValue(attributeId, vertexId);
            final String displayableValue = interaction.getDisplayText(attributeValue);

            rowData.add(displayableValue);
        });

        tableService.getElementIdToRowIndex().put(vertexId, rowData);
        tableService.getRowToElementIdIndex().put(rowData, vertexId);

        rows.add(rowData);
    }
    
    public void extractRowDataMaybe(final ReadableGraph readableGraph,
                                    final List<ObservableList<String>> rows,
                                    final int transactionId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();
        
        columnIndex.forEach(columnTuple -> {
            final int attributeId = readableGraph.getAttribute(
                    columnTuple.getSecond().getElementType(), columnTuple.getSecond().getName());
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction
                    .getInteraction(columnTuple.getSecond().getAttributeType());
            
            final Object attributeValue;
            switch (columnTuple.getFirst()) {
                case GraphRecordStoreUtilities.SOURCE:
                    final int sourceVertexId = readableGraph.getTransactionSourceVertex(transactionId);
                    attributeValue = readableGraph.getObjectValue(attributeId, sourceVertexId);
                    break;
                case GraphRecordStoreUtilities.TRANSACTION:
                    attributeValue = readableGraph.getObjectValue(attributeId, transactionId);
                    break;
                case GraphRecordStoreUtilities.DESTINATION:
                    final int destinationVertexId = readableGraph.getTransactionDestinationVertex(transactionId);
                    attributeValue = readableGraph.getObjectValue(attributeId, destinationVertexId);
                    break;
                default:
                    attributeValue = null;
            }
            // avoid duplicate strings objects and make a massivse saving on memory use
            final String displayableValue = displayTextCache.deduplicate(interaction.getDisplayText(attributeValue));
            rowData.add(displayableValue);
        });
        
        tableService.getElementIdToRowIndex().put(transactionId, rowData);
        tableService.getRowToElementIdIndex().put(rowData, transactionId);
        
        rows.add(rowData);
    }
    
    public void stuff(final ReadableGraph readableGraph,
                      final GraphElementType elementType,
                      final String attributeNamePrefix,
                      final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap) {
        final int attributeCount = readableGraph.getAttributeCount(elementType);
                    
        IntStream.range(0, attributeCount).forEach(attributePosition -> {
            final int attributeId = readableGraph
                    .getAttribute(elementType, attributePosition);
            final String attributeName = attributeNamePrefix
                    + readableGraph.getAttributeName(attributeId);
            final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(attributeName)
                    ? columnReferenceMap.get(attributeName) : new TableColumn<>(attributeName);

            columnIndex.add(ThreeTuple.create(
                    GraphRecordStoreUtilities.SOURCE,
                    new GraphAttribute(readableGraph, attributeId),
                    column
            ));

        });
    }

    public CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> getColumnIndex() {
        return columnIndex;
    }
}
