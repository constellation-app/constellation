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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import static au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities.TABLE_LOCK;
import au.gov.asd.tac.constellation.views.tableview2.factory.TableCellFactory;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.Column;
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
 * Representation of the table. This wraps the JavaFX UI {@link TableView} component
 * and provides methods for performing updates based on the current state and
 * graph.
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
    
    private final CopyOnWriteArrayList<Column> columnIndex;
    
    /**
     * Cache strings used in table cells to significantly reduce memory used by
     * the same string repeated in columns and rows.
     */
    private final ImmutableObjectCache displayTextCache;
    
    private final UpdateColumnsTask updateColumnsTask;
    
    /**
     * 
     * @param tableTopComponent
     * @param tablePane
     * @param tableService 
     */
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
    
    /**
     * Gets the underlying JavaFX table component.
     *
     * @return the JavaFX table
     */
    public final TableView<ObservableList<String>> getTableView() {
        return tableView;
    }
    
    /**
     * Gets the currently selected row in the table.
     *
     * @return the currently selected row
     */
    public final ReadOnlyObjectProperty<ObservableList<String>> getSelectedProperty() {
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

                // Clear current columnIndex, but cache the column objects for reuse
                final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap =
                        getColumnIndex().stream()
                                .collect(
                                        Collectors.toMap(
                                                column -> column.getTableColumn().getText(),
                                                column -> column.getTableColumn(),
                                                (e1, e2) -> e1
                                        )
                                );
                getColumnIndex().clear();

                // Update columnIndex based on graph attributes
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    // Creates "source." columns from vertex attributes
                    getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
                            GraphRecordStoreUtilities.SOURCE, columnReferenceMap));

                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        // Creates "transaction." columns from transaction attributes
                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION,
                                GraphRecordStoreUtilities.TRANSACTION, columnReferenceMap));
                        
                        // Creates "destination." columns from vertex attributes
                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
                                GraphRecordStoreUtilities.DESTINATION, columnReferenceMap));
                    }
                } finally {
                    readableGraph.release();
                }

                // If there are no visible columns specified, write the key columns to the state
                if (state.getColumnAttributes() == null) {
                    openColumnVisibilityMenu();
                    return;
                }

                // Sort columns in columnIndex by state, prefix and attribute name
                getColumnIndex().sort(new ColumnIndexSort(state));

                // Style and format columns in columnIndex
                getColumnIndex().forEach(columnTuple -> {
                    final TableColumn<ObservableList<String>, String> column = columnTuple.getTableColumn();

                    // assign cells to columns
                    column.setCellValueFactory(cellData -> {
                        final int cellIndex = tableView.getColumns().indexOf(cellData.getTableColumn());
                        if (cellIndex < cellData.getValue().size()) {
                            return new SimpleStringProperty(cellData.getValue().get(cellIndex));
                        } else {
                            return null;
                        }
                    });

                    // Assign values and styles to cells
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

                // Set progress indicator
                Platform.runLater(() -> tablePane.setCenter(progressBar.getProgressPane()));

                // Clear the current row and element mappings
                tableService.getElementIdToRowIndex().clear();
                tableService.getRowToElementIdIndex().clear();

                // Build table data based on attribute values on the graph
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
                                rows.add(getRowDataForTransaction(readableGraph, transactionId));
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
                                rows.add(getRowDataForVertex(readableGraph, vertexId));
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
     * Update the table selection using the graph and state. The selection will
     * only be updated if the graph and state are not null.
     * <p/>
     * The table selection will only be updated if it <b>IS NOT</b> in "Selected Only Mode".
     * <p/>
     * An illegal state will be created if this method is called by either the JavaFX
     * or Swing Event threads.
     * <p/>
     * The entire method is synchronized to ensure thread safety.
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
                    final List<Integer> selectedIds = getSelectedIds(graph, state);

                    // update table selection
                    final int[] selectedIndices = selectedIds.stream()
                            .map(id-> tableService.getElementIdToRowIndex().get(id))
                            .map(row -> getTableView().getItems().indexOf(row))
                            .mapToInt(i -> i)
                            .toArray();

                    Platform.runLater(() -> {
                        getSelectedProperty().removeListener(tableSelectionListener);
                        getTableView().getSelectionModel().getSelectedItems().removeListener(selectedOnlySelectionListener);
                        getTableView().getSelectionModel().clearSelection();
                        if (!selectedIds.isEmpty()) {
                            getTableView().getSelectionModel().selectIndices(selectedIndices[0], selectedIndices);
                        }
                        getTableView().getSelectionModel().getSelectedItems().addListener(selectedOnlySelectionListener);
                        getSelectedProperty().addListener(tableSelectionListener);
                    });
                }
            }
        }
    }
    
    /**
     * If the sort has been saved in the currently loaded table preferences, then
     * re-apply it to the table.
     *
     */
    public void updateSortOrder() {
        final Pair<String, TableColumn.SortType> sortPreference
                = tableService.getTablePreferences().getSort();
        
        if (sortPreference != null && !sortPreference.getLeft().isBlank()) {
            // Iterate through the table columns and find the one with a matching column name
            for (final TableColumn<ObservableList<String>, ?> column : getTableView().getColumns()) {
                if (column.getText().equals(sortPreference.getLeft())) {
                    column.setSortType(sortPreference.getRight());
                    getTableView().getSortOrder().setAll(column);
                    return;
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
    
    /**
     * For a given vertex on the graph construct a row for the table give the
     * current column settings. If the column is a transaction column then a
     * null value will be inserted for that element of the row.
     *
     * @param readableGraph the graph to build the row from
     * @param vertexId the ID of the vertex in the graph to build the row from
     * @return the built row
     */
    protected ObservableList<String> getRowDataForVertex(final ReadableGraph readableGraph,
                                                         final int vertexId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();
                                
        getColumnIndex().forEach(column -> {
            final int attributeId = readableGraph
                    .getAttribute(column.getAttribute().getElementType(),
                            column.getAttribute().getName());
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction
                    .getInteraction(column.getAttribute().getAttributeType());
            final Object attributeValue = readableGraph
                    .getObjectValue(attributeId, vertexId);
            final String displayableValue = interaction.getDisplayText(attributeValue);

            rowData.add(displayableValue);
        });

        tableService.getElementIdToRowIndex().put(vertexId, rowData);
        tableService.getRowToElementIdIndex().put(rowData, vertexId);

        return rowData;
    }
    
    /**
     * For a given transaction on the graph construct a row for the table given
     * the current column settings. In the case of source and destination columns
     * the value entered will be sourced from the source and destination vertices
     * respectively.
     * <p/>
     * During this the {@link TableService#elementIdToRowIndex} and
     * {@link TableService#rowToElementIdIndex} maps are populated.
     *
     * @param readableGraph the graph to build the row from
     * @param transactionId the ID of the transaction in the graph to build the row from
     * @return the built row
     */
    protected ObservableList<String> getRowDataForTransaction(final ReadableGraph readableGraph,
                                                              final int transactionId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();
        
        getColumnIndex().forEach(column -> {
            final int attributeId = readableGraph.getAttribute(
                    column.getAttribute().getElementType(),
                    column.getAttribute().getName()
            );
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction
                    .getInteraction(column.getAttribute().getAttributeType());
            
            final Object attributeValue;
            switch (column.getAttributeNamePrefix()) {
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
        
        return rowData;
    }
    
    /**
     * For the specified element type (vertex or transaction), iterates through
     * that element types attributes in the graph and generates columns for each one.
     * <p/>
     * The column name will be the attribute name prefixed by the passed {@code attributeNamePrefix}
     * parameter. This parameter will be one of "source.", "destination." or "transaction.".
     * <p/>
     * The column reference map contains previously generated columns and is used as
     * a reference so that new column objects are not created needlessly.
     *
     * @param readableGraph the graph to extract the attributes from
     * @param elementType the type of elements that the attributes will be extracted from,
     *     {@link GraphElementType#VERTEX} or {@link GraphElementType#TRANSACTION}
     * @param attributeNamePrefix the string that will prefix the attribute name in the
     *     column name, will be one of "source.", "destination." or "transaction."
     * @param columnReferenceMap a map of existing columns that can be used instead of
     *     creating new ones if the column names match up
     */
    protected CopyOnWriteArrayList<Column> createColumnIndexPart(final ReadableGraph readableGraph,
                                                                 final GraphElementType elementType,
                                                                 final String attributeNamePrefix,
                                                                 final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap) {
        final CopyOnWriteArrayList<Column> tmpColumnIndex = new CopyOnWriteArrayList<>();
        
        final int attributeCount = readableGraph.getAttributeCount(elementType);

        IntStream.range(0, attributeCount).forEach(attributePosition -> {
            final int attributeId = readableGraph
                    .getAttribute(elementType, attributePosition);
            final String attributeName = attributeNamePrefix
                    + readableGraph.getAttributeName(attributeId);
            final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(attributeName)
                    ? columnReferenceMap.get(attributeName) : createColumn(attributeName);

            tmpColumnIndex.add(new Column(
                    attributeNamePrefix,
                    new GraphAttribute(readableGraph, attributeId),
                    column
            ));

        });

        return tmpColumnIndex;
    }
    
    /**
     * Creates a new column with the given name. This has been primarily created for
     * unit testing to allow the insertion of mocked versions into the calling code.
     *
     * @param attributeName the name of the column
     * @return the newly created column
     */
    protected TableColumn<ObservableList<String>, String> createColumn(final String attributeName) {
        return new TableColumn<>(attributeName);
    }

    /**
     * Gets a list representing the current column setup of the table and how it relates
     * to the graph. The list describes how each column relates to either a source vertex,
     * destination vertex or transaction.
     * 
     * @return the table column representation
     * @see Column
     */
    public final List<Column> getColumnIndex() {
        return columnIndex;
    }
    
    /**
     * Gets the context menu describing the columns that make a vertex or transaction
     * unique. In other words the primary columns. Then manually triggers a click
     * event causing those columns to be made visible.
     *
     * @see ColumnVisibilityContextMenu#getShowPrimaryColumnsMenu() 
     */
    protected void openColumnVisibilityMenu() {
        final ColumnVisibilityContextMenu columnVisibilityContextMenu
                = new ColumnVisibilityContextMenu(tableTopComponent, this, tableService);
        columnVisibilityContextMenu.init();
                    
        final MenuItem keyColumns = columnVisibilityContextMenu.getShowPrimaryColumnsMenu();
        keyColumns.fire();
    }
}
