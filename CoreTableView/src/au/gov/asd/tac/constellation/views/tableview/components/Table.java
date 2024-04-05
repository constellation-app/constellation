/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import static au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent.TABLE_LOCK;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.factory.TableCellFactory;
import au.gov.asd.tac.constellation.views.tableview.listeners.SelectedOnlySelectionListener;
import au.gov.asd.tac.constellation.views.tableview.listeners.TableSelectionListener;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview.tasks.UpdateColumnsTask;
import au.gov.asd.tac.constellation.views.tableview.tasks.UpdateDataTask;
import au.gov.asd.tac.constellation.views.tableview.utilities.ColumnIndexSort;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * Representation of the table. This wraps the JavaFX UI {@link TableView}
 * component and provides methods for performing updates based on the current
 * state and graph.
 *
 * @author formalhaunt
 */
public class Table {

    private static final Logger LOGGER = Logger.getLogger(Table.class.getName());

    private static final String ATTEMPT_PROCESS_JAVAFX = "Attempting to process on the JavaFX Application Thread";
    private static final String ATTEMPT_PROCESS_EDT = "Attempting to process on the EDT";

    private final TablePane tablePane;
    private final TableView<ObservableList<String>> tableView;

    private final ChangeListener<ObservableList<String>> tableSelectionListener;
    private final ListChangeListener selectedOnlySelectionListener;

    /**
     * Cache strings used in table cells to significantly reduce memory used by
     * the same string repeated in columns and rows.
     */
    private final ImmutableObjectCache displayTextCache;

    private final UpdateColumnsTask updateColumnsTask;

    /**
     * Creates a new table.
     *
     * @param tablePane the pane that contains this table
     */
    public Table(final TablePane tablePane) {
        this.tablePane = tablePane;

        this.tableView = new TableView<>();
        this.tableView.itemsProperty().addListener((v, o, n) -> tableView.refresh());
        this.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.tableView.setPadding(new Insets(5));
        this.tableView.setCache(false);

        this.displayTextCache = new ImmutableObjectCache();

        this.updateColumnsTask = new UpdateColumnsTask(this);

        this.tableSelectionListener = new TableSelectionListener(this);
        this.selectedOnlySelectionListener = new SelectedOnlySelectionListener(this);

        this.tableView.getSelectionModel().selectedItemProperty()
                .addListener(tableSelectionListener);
        this.tableView.getSelectionModel().getSelectedItems()
                .addListener(selectedOnlySelectionListener);
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
     * Update the columns in the table using the graph and state. This will
     * clear and refresh the column index and then trigger a refresh of the
     * table view, populating from the new column index.
     * <p/>
     * If the table's state has an element type of VERTEX then all the columns
     * will be prefixed with ".source".
     * <p/>
     * If the element type is TRANSACTION then the attributes belonging to
     * transactions will be prefixed with ".transaction". The vertex attributes
     * will also be added as columns in this case. When the state's element type
     * is TRANSACTION the vertex attributes will be prefixed with both ".source"
     * and ".destination" so that it is distinguishable on which end of the
     * transaction those values are present.
     * <p/>
     * Note that column references are reused where possible to ensure certain
     * toolbar/menu operations to work correctly.
     * <p/>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateColumns(final Graph graph,
            final TableViewState state) {
        synchronized (TABLE_LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_JAVAFX);
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_EDT);
                }

                // Clear current columnIndex, but cache the column objects for reuse
                final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap
                        = getColumnIndex().stream()
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

//                    getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.EDGE,
//                                GraphRecordStoreUtilities.SOURCE, columnReferenceMap));
//                    getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.EDGE,
//                                GraphRecordStoreUtilities.DESTINATION, columnReferenceMap));
//                    getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
//                                GraphRecordStoreUtilities.ALL, columnReferenceMap));
//                    getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
//                                GraphRecordStoreUtilities.ID, columnReferenceMap));
                    // If Transaction
                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        LOGGER.log(Level.WARNING, "Update columns TRANSACTION");
                        // Creates "transaction." columns from transaction attributes
                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION,
                                GraphRecordStoreUtilities.TRANSACTION, columnReferenceMap));

                        // Creates "destination." columns from vertex attributes
                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
                                GraphRecordStoreUtilities.DESTINATION, columnReferenceMap));
                    } // If Edge
                    else if (state.getElementType() == GraphElementType.EDGE) {
                        LOGGER.log(Level.WARNING, "Update columns EDGE");
                        // Creates "destination." columns from vertex attributes
//                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.VERTEX,
//                                GraphRecordStoreUtilities.DESTINATION, columnReferenceMap));
                        getColumnIndex().addAll(createColumnIndexPart(readableGraph, GraphElementType.EDGE,
                                GraphRecordStoreUtilities.EDGE, columnReferenceMap));
                    } // If Link
                    else if (state.getElementType() == GraphElementType.LINK) {
                        LOGGER.log(Level.WARNING, "Update columns LINK");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "THIS WAS CAUSING ISSUE BB: {0}", e);
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
                            //LOGGER.log(Level.WARNING, "NO Problem with: {0}", columnTuple);
                            return new SimpleStringProperty(cellData.getValue().get(cellIndex));
                        } else {
                            LOGGER.log(Level.WARNING, "Problem with: {0}", columnTuple);
                            return null;
                        }
                    });

                    // Assign values and styles to cells
                    column.setCellFactory(cellColumn -> new TableCellFactory(cellColumn, this));
                });

                // If the update has been cancelled then don't update the UI with the
                // calculated column changes
                if (!Thread.currentThread().isInterrupted()) {
                    // The update columns task holds state between executions. So we need to
                    // reset some fields each time before it is run.
                    updateColumnsTask.reset(columnReferenceMap, state);
                    Platform.runLater(updateColumnsTask);
                }
            }
        }
    }

    /**
     * Update the data in the table using the graph and state.
     * <p/>
     * If the table is in "Selection Only" mode then only the elements on the
     * graph that are selected will be loaded into the table, otherwise they all
     * will.
     * <p/>
     * Which elements are loaded also depends on which element type the table
     * state is currently set to, vertex or transaction.
     * <p/>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateData(final Graph graph,
            final TableViewState state,
            final ProgressBar progressBar) {
        synchronized (TABLE_LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_JAVAFX);
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException(ATTEMPT_PROCESS_EDT);
                }

                // Set progress indicator
                Platform.runLater(() -> getParentComponent().setCenter(progressBar.getProgressPane()));

                // Clear the current row and element mappings
                getActiveTableReference().getElementIdToRowIndex().clear();
                getActiveTableReference().getRowToElementIdIndex().clear();

                // Build table data based on attribute values on the graph
                final List<ObservableList<String>> rows = new ArrayList<>();
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    LOGGER.log(Level.WARNING, "State {0}", state.getElementType());
                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        final int selectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int transactionCount = readableGraph.getTransactionCount();

                        IntStream.range(0, transactionCount).forEach(transactionPosition -> {
                            final int transactionId = readableGraph.getTransaction(transactionPosition);
                            boolean isSelected = false;

                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                isSelected = readableGraph.getBooleanValue(selectedAttributeId, transactionId);
                            }

                            // If it is not in selected only mode then just add every row but if it is
                            // in selected only mode, only add the ones that are selected in the graph
                            if (!state.isSelectedOnly() || isSelected) {
                                rows.add(getRowDataForTransaction(readableGraph, transactionId));
                            }
                        });
                    } else if (state.getElementType() == GraphElementType.EDGE) {
                        final int selectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int edgeCount = readableGraph.getEdgeCount();
                        LOGGER.log(Level.WARNING, "Edge count {0}", edgeCount);
                        IntStream.range(0, edgeCount).forEach(edgePosition -> {
                            final int edgeId = readableGraph.getEdge(edgePosition);
                            boolean isSelected = true;

                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                for (int i = 0; i < readableGraph.getEdgeTransactionCount(edgeId); i++) {
                                    // If just one of the transactions isn't selected, set to false and break loop
                                    if (!readableGraph.getBooleanValue(selectedAttributeId, readableGraph.getEdgeTransaction(edgeId, i))) {
                                        isSelected = false;
                                        break;
                                    }
                                }
                                //isSelected = readableGraph.getBooleanValue(selectedAttributeId, edgeId);
                            }
                            LOGGER.log(Level.WARNING, "IS selected {0}", isSelected);
                            // If it is not in selected only mode then just add every row but if it is
                            // in selected only mode, only add the ones that are selected in the graph
                            if (!state.isSelectedOnly() || isSelected) {
                                LOGGER.log(Level.WARNING, "Edge id {0}", edgeId);
                                rows.add(getRowDataForEdge(readableGraph, edgeId));
                            }
                        });

                    } else if (state.getElementType() == GraphElementType.LINK) {
                        final int selectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int linkCount = readableGraph.getLinkCount();
                        LOGGER.log(Level.WARNING, "Link count {0}", linkCount);
                        IntStream.range(0, linkCount).forEach(linkPosition -> {
                            final int linkId = readableGraph.getLink(linkPosition);
                            boolean isSelected = true;

                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                for (int i = 0; i < readableGraph.getLinkTransactionCount(linkId); i++) {
                                    // If just one of the transactions isn't selected, set to false and break loop
                                    if (!readableGraph.getBooleanValue(selectedAttributeId, readableGraph.getLinkTransaction(linkId, i))) {
                                        isSelected = false;
                                        break;
                                    }
                                }
                            }
                            LOGGER.log(Level.WARNING, "IS selected {0}", isSelected);
                            // If it is not in selected only mode then just add every row but if it is
                            // in selected only mode, only add the ones that are selected in the graph
                            if (!state.isSelectedOnly() || isSelected) {
                                LOGGER.log(Level.WARNING, "Link id {0}", linkId);
                                rows.add(getRowDataForLink(readableGraph, linkId));
                            }
                        });

                    } else {
                        final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
                        final int vertexCount = readableGraph.getVertexCount();

                        IntStream.range(0, vertexCount).forEach(vertexPosition -> {
                            final int vertexId = readableGraph.getVertex(vertexPosition);
                            boolean isSelected = false;

                            if (selectedAttributeId != Graph.NOT_FOUND) {
                                isSelected = readableGraph.getBooleanValue(selectedAttributeId, vertexId);
                            }
                            LOGGER.log(Level.WARNING, "IS selected VERTEX {0}", isSelected);
                            // If it is not in selected only mode then just add every row but if it is
                            // in selected only mode, only add the ones that are selected in the graph
                            if (!state.isSelectedOnly() || isSelected) {
                                rows.add(getRowDataForVertex(readableGraph, vertexId));
                            }
                        });
                    }
                } finally {
                    readableGraph.release();
                }

                // Don't want to trigger the UI update if the update has been cancelled
                // The progress bare will remain in place as a result but the next update
                // that cancelled this one will run through, complete and remove the progress
                // bar.
                if (!Thread.currentThread().isInterrupted()) {
                    final UpdateDataTask updateDataTask = new UpdateDataTask(this, rows);
                    Platform.runLater(updateDataTask);

                    try {
                        updateDataTask.getUpdateDataLatch().await();
                    } catch (final InterruptedException ex) {
                        LOGGER.log(Level.WARNING, "InterruptedException encountered while updating table data");
                        updateDataTask.setInterrupted(true);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Update the table selection using the graph and state. The selection will
     * only be updated if the graph and state are not null.
     * <p/>
     * The table selection will only be updated if it <b>IS NOT</b> in "Selected
     * Only Mode" because the selection is extracted from the graph.
     * <p/>
     * An illegal state will be created if this method is called by either the
     * JavaFX or Swing Event threads.
     * <p/>
     * The entire method is synchronized to ensure thread safety.
     *
     * @param graph the graph to read the element/row selection from
     * @param state the current table state
     */
    public void updateSelection(final Graph graph,
            final TableViewState state) {
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
                    final List<Integer> selectedIds = TableViewUtilities.getSelectedIds(graph, state);

                    // update table selection
                    final int[] selectedIndices = selectedIds.stream()
                            .map(id -> getActiveTableReference().getElementIdToRowIndex().get(id))
                            .map(row -> getTableView().getItems().indexOf(row))
                            .mapToInt(i -> i)
                            .toArray();

                    // Don't want to trigger the UI update if the update has been cancelled
                    if (!Thread.currentThread().isInterrupted()) {
                        Platform.runLater(() -> {
                            getSelectedProperty().removeListener(getTableSelectionListener());
                            getTableView().getSelectionModel().getSelectedItems()
                                    .removeListener(getSelectedOnlySelectionListener());

                            getTableView().getSelectionModel().clearSelection();
                            if (!selectedIds.isEmpty()) {
                                getTableView().getSelectionModel().selectIndices(selectedIndices[0], selectedIndices);
                            }

                            getTableView().getSelectionModel().getSelectedItems()
                                    .addListener(getSelectedOnlySelectionListener());
                            getSelectedProperty().addListener(getTableSelectionListener());
                        });
                    }
                }
            }
        }
    }

    /**
     * If the sort has been saved in the currently loaded table preferences,
     * then re-apply it to the table.
     *
     */
    public void updateSortOrder() {
        final Pair<String, TableColumn.SortType> sortPreference
                = getActiveTableReference().getUserTablePreferences().getSortByColumn();

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
     * Gets a listener that listens for table selections and updates the
     * selection in the graph if "Selected Only Mode" <b>IS NOT</b> active.
     * Otherwise this listener does nothing.
     *
     * @return the table selection listener
     * @see TableSelectionListener
     */
    public ChangeListener<ObservableList<String>> getTableSelectionListener() {
        return tableSelectionListener;
    }

    /**
     * Gets a listener that listens for table selections and updates the
     * {@link ActiveTableReference#selectedOnlySelectedRows} list with the
     * current selection. This listener only does this if the "Selected Only
     * Mode" <b>IS</>
     * active.
     *
     * @return the "Selected Only Mode" selection listener
     * @see SelectedOnlySelectionListener
     */
    public ListChangeListener getSelectedOnlySelectionListener() {
        return selectedOnlySelectionListener;
    }

    /**
     * Get the parent component for the table.
     *
     * @return the parent component
     */
    public TablePane getParentComponent() {
        return tablePane;
    }

    /**
     * Get the active table reference associated with this table.
     *
     * @return the active table reference
     */
    public ActiveTableReference getActiveTableReference() {
        return getParentComponent().getActiveTableReference();
    }

    /**
     * Get the column index associated with this table.
     *
     * @return the column index
     */
    public List<Column> getColumnIndex() {
        return getParentComponent().getActiveTableReference().getColumnIndex();
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

        getActiveTableReference().getElementIdToRowIndex().put(vertexId, rowData);
        getActiveTableReference().getRowToElementIdIndex().put(rowData, vertexId);

        return rowData;
    }

    /**
     * For a given transaction on the graph construct a row for the table given
     * the current column settings. In the case of source and destination
     * columns the value entered will be sourced from the source and destination
     * vertices respectively.
     * <p/>
     * During this the {@link ActiveTableReference#elementIdToRowIndex} and
     * {@link ActiveTableReference#rowToElementIdIndex} maps are populated.
     *
     * @param readableGraph the graph to build the row from
     * @param transactionId the ID of the transaction in the graph to build the
     * row from
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
            if (attributeId != Graph.NOT_FOUND) {
                switch (column.getAttributeNamePrefix()) {
                    case GraphRecordStoreUtilities.SOURCE:
                        //LOGGER.log(Level.WARNING, "Transaction SOURCE: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int sourceVertexId = readableGraph.getTransactionSourceVertex(transactionId);
                        attributeValue = readableGraph.getObjectValue(attributeId, sourceVertexId);
                        break;
                    case GraphRecordStoreUtilities.TRANSACTION:
                        //LOGGER.log(Level.WARNING, "Transaction TRANSACTION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        attributeValue = readableGraph.getObjectValue(attributeId, transactionId);
                        break;
                    case GraphRecordStoreUtilities.DESTINATION:
                        //LOGGER.log(Level.WARNING, "Transaction DESTINATION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int destinationVertexId = readableGraph.getTransactionDestinationVertex(transactionId);
                        attributeValue = readableGraph.getObjectValue(attributeId, destinationVertexId);
                        break;
                    default:
                        attributeValue = null;
                }
            } else {
                attributeValue = null;
            }

            // avoid duplicate strings objects and make a massivse saving on memory use
            final String displayableValue = displayTextCache.deduplicate(interaction.getDisplayText(attributeValue));
            rowData.add(displayableValue);
        });

        getActiveTableReference().getElementIdToRowIndex().put(transactionId, rowData);
        getActiveTableReference().getRowToElementIdIndex().put(rowData, transactionId);

        return rowData;
    }

    /**
     * For a given edge
     *
     * @param readableGraph the graph to build the row from
     * @param edgeId the ID of the transaction in the graph to build the row
     * from
     * @return the built row
     */
    protected ObservableList<String> getRowDataForEdge(final ReadableGraph readableGraph,
            final int edgeId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();

        getColumnIndex().forEach(column -> {

            final int attributeId = readableGraph.getAttribute(
                    column.getAttribute().getElementType(),
                    column.getAttribute().getName()
            );
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction
                    .getInteraction(column.getAttribute().getAttributeType());

            final Object attributeValue;
            if (attributeId != Graph.NOT_FOUND) {
                switch (column.getAttributeNamePrefix()) {
                    case GraphRecordStoreUtilities.SOURCE:
                        //LOGGER.log(Level.WARNING, "Edge SOURCE: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int sourceVertexId = readableGraph.getEdgeSourceVertex(edgeId);
                        attributeValue = readableGraph.getObjectValue(attributeId, sourceVertexId);
                        break;
                    case GraphRecordStoreUtilities.TRANSACTION:
                        //LOGGER.log(Level.WARNING, "Edge TRANSACTION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        attributeValue = readableGraph.getObjectValue(attributeId, edgeId);
                        break;
                    case GraphRecordStoreUtilities.DESTINATION:
                        //LOGGER.log(Level.WARNING, "Edge DESTINATION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int destinationVertexId = readableGraph.getEdgeDestinationVertex(edgeId);
                        attributeValue = readableGraph.getObjectValue(attributeId, destinationVertexId);
                        break;
                    default:
                        attributeValue = null;
                }
            } else {
                attributeValue = null;
            }

            // avoid duplicate strings objects and make a massivse saving on memory use
            final String displayableValue = displayTextCache.deduplicate(interaction.getDisplayText(attributeValue));
            rowData.add(displayableValue);
        });

        getActiveTableReference().getElementIdToRowIndex().put(edgeId, rowData);
        getActiveTableReference().getRowToElementIdIndex().put(rowData, edgeId);

        return rowData;
    }
    
    
    /**
     * For a given edge
     *
     * @param readableGraph the graph to build the row from
     * @param linkId the ID of the transaction in the graph to build the row
     * from
     * @return the built row
     */
    protected ObservableList<String> getRowDataForLink(final ReadableGraph readableGraph,
            final int linkId) {
        final ObservableList<String> rowData = FXCollections.observableArrayList();

        getColumnIndex().forEach(column -> {

            final int attributeId = readableGraph.getAttribute(
                    column.getAttribute().getElementType(),
                    column.getAttribute().getName()
            );
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction
                    .getInteraction(column.getAttribute().getAttributeType());

            final Object attributeValue;
            if (attributeId != Graph.NOT_FOUND) {
                switch (column.getAttributeNamePrefix()) {
                    case GraphRecordStoreUtilities.SOURCE:
                        //LOGGER.log(Level.WARNING, "Edge SOURCE: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int sourceVertexId = readableGraph.getEdgeSourceVertex(linkId);
                        attributeValue = readableGraph.getObjectValue(attributeId, sourceVertexId);
                        break;
                    case GraphRecordStoreUtilities.TRANSACTION:
                        //LOGGER.log(Level.WARNING, "Edge TRANSACTION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        attributeValue = readableGraph.getObjectValue(attributeId, linkId);
                        break;
                    case GraphRecordStoreUtilities.DESTINATION:
                        //LOGGER.log(Level.WARNING, "Edge DESTINATION: {0} {1}", new Object[]{ column.getAttribute().getElementType(), column.getAttribute().getName()});
                        final int destinationVertexId = readableGraph.getEdgeDestinationVertex(linkId);
                        attributeValue = readableGraph.getObjectValue(attributeId, destinationVertexId);
                        break;
                    default:
                        attributeValue = null;
                }
            } else {
                attributeValue = null;
            }

            // avoid duplicate strings objects and make a massivse saving on memory use
            final String displayableValue = displayTextCache.deduplicate(interaction.getDisplayText(attributeValue));
            rowData.add(displayableValue);
        });

        getActiveTableReference().getElementIdToRowIndex().put(linkId, rowData);
        getActiveTableReference().getRowToElementIdIndex().put(rowData, linkId);

        return rowData;
    }

    /**
     * For the specified element type (vertex or transaction), iterates through
     * that element types attributes in the graph and generates columns for each
     * one.
     * <p/>
     * The column name will be the attribute name prefixed by the passed
     * {@code attributeNamePrefix} parameter. This parameter will be one of
     * "source.", "destination." or "transaction.".
     * <p/>
     * The column reference map contains previously generated columns and is
     * used as a reference so that new column objects are not created
     * needlessly.
     *
     * @param readableGraph the graph to extract the attributes from
     * @param elementType the type of elements that the attributes will be
     * extracted from, {@link GraphElementType#VERTEX} or
     * {@link GraphElementType#TRANSACTION}
     * @param attributeNamePrefix the string that will prefix the attribute name
     * in the column name, will be one of "source.", "destination." or
     * "transaction."
     * @param columnReferenceMap a map of existing columns that can be used
     * instead of creating new ones if the column names match up
     */
    protected List<Column> createColumnIndexPart(final ReadableGraph readableGraph,
            final GraphElementType elementType,
            final String attributeNamePrefix,
            final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap) {
        final List<Column> tmpColumnIndex = new CopyOnWriteArrayList<>();

        final int attributeCount = readableGraph.getAttributeCount(elementType);
        LOGGER.log(Level.WARNING, "createColumnIndexPart attributeCount: {0}", attributeCount);
        IntStream.range(0, attributeCount).forEach(attributePosition -> {
            final int attributeId = readableGraph
                    .getAttribute(elementType, attributePosition);
            final String attributeName = attributeNamePrefix
                    + readableGraph.getAttributeName(attributeId);
            final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(attributeName)
                    ? columnReferenceMap.get(attributeName) : createColumn(attributeName);
//            if (columnReferenceMap.containsKey(attributeName)) {
//                LOGGER.log(Level.WARNING, "Existing column: {0}", attributeName);
//            } else {
//                LOGGER.log(Level.WARNING, "NEW column: {0}", attributeName);
//            }
            //LOGGER.log(Level.WARNING, "THING: {0} {1} {2}", new Object[]{attributeId, attributeName, column});
            tmpColumnIndex.add(new Column(
                    attributeNamePrefix,
                    new GraphAttribute(readableGraph, attributeId),
                    column
            ));

        });

        return tmpColumnIndex;
    }

    /**
     * Creates a new column with the given name. This has been primarily created
     * for unit testing to allow the insertion of mocked versions into the
     * calling code.
     *
     * @param attributeName the name of the column
     * @return the newly created column
     */
    protected TableColumn<ObservableList<String>, String> createColumn(final String attributeName) {
        TableColumn<ObservableList<String>, String> newColumn = new TableColumn<>(attributeName);
        newColumn.setComparator(new TableDataComparator());
        return newColumn;
    }

    /**
     * Gets the context menu describing the columns that make a vertex or
     * transaction unique. In other words the primary columns. Then manually
     * triggers a click event causing those columns to be made visible.
     *
     * @see ColumnVisibilityContextMenu#getShowPrimaryColumnsMenu()
     */
    protected void openColumnVisibilityMenu() {
        final ColumnVisibilityContextMenu columnVisibilityContextMenu
                = new ColumnVisibilityContextMenu(this);
        columnVisibilityContextMenu.init();

        final MenuItem keyColumns = columnVisibilityContextMenu.getShowPrimaryColumnsMenu();
        keyColumns.fire();
    }
}
