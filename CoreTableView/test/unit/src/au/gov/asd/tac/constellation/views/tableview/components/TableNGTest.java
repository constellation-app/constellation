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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview.tasks.UpdateColumnsTask;
import au.gov.asd.tac.constellation.views.tableview.tasks.UpdateDataTask;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableNGTest {

    private static final Logger LOGGER = Logger.getLogger(TableNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private ActiveTableReference activeTableReference;
    private Graph graph;

    private Table table;

    private static final String MULTI_VALUE = "<Multiple Values>";

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tableViewTopComponent = mock(TableViewTopComponent.class);
        tablePane = mock(TablePane.class);
        activeTableReference = mock(ActiveTableReference.class);
        graph = mock(Graph.class);

        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        table = spy(new Table(tablePane));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void init() {
        assertNotNull(table.getTableView());

        assertEquals(SelectionMode.MULTIPLE, table.getTableView().getSelectionModel().getSelectionMode());
        assertEquals(new Insets(5), table.getTableView().getPadding());
        assertFalse(table.getTableView().isCache());

        assertNotNull(table.getColumnIndex());
        assertTrue(table.getColumnIndex().isEmpty());

        assertNotNull(table.getSelectedProperty());
        assertSame(table.getTableView().getSelectionModel().selectedItemProperty(), table.getSelectedProperty());

        assertNotNull(table.getTableSelectionListener());
        assertNotNull(table.getSelectedOnlySelectionListener());
    }

    @Test
    public void updateSelectionGraphNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateSelection(null, new TableViewState());

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void updateSelectionStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateSelection(graph, null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateSelectionOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            table.updateSelection(graph, new TableViewState());
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateSelectionOnSwingThread() {
        try (final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

            table.updateSelection(graph, new TableViewState());
        }
    }

    @Test
    public void updateSelectionInSelectedOnlyMode() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            final TableViewState tableViewState = new TableViewState();
            tableViewState.setSelectedOnly(true);

            table.updateSelection(graph, tableViewState);

            platformMockedStatic.verify(() -> Platform.runLater(any(Runnable.class)), times(0));
        }
    }

    @Test
    public void updateSelectionThreadInterrupted() {
        try (
                MockedStatic<TableViewUtilities> tableUtilsMockedStatic = Mockito.mockStatic(TableViewUtilities.class); final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class);) {
            final TableViewState tableViewState = new TableViewState();
            tableViewState.setSelectedOnly(false);

            when(activeTableReference.getElementIdToRowIndex()).thenReturn(Map.of());

            tableUtilsMockedStatic.when(() -> TableViewUtilities.getSelectedIds(graph, tableViewState))
                    .thenReturn(List.of());

            Thread.currentThread().interrupt();

            table.updateSelection(graph, tableViewState);

            assertTrue(Thread.currentThread().isInterrupted());

            // Clears the current threads interrupt status
            Thread.interrupted();

            platformMockedStatic.verify(() -> Platform.runLater(any(Runnable.class)), times(0));
        }
    }

    @Test
    public void updateSelectionNotInSelectedOnlyMode() throws InterruptedException {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setSelectedOnly(false);
        tableViewState.setElementType(GraphElementType.VERTEX);

        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);

        doReturn(tableSelectionListener).when(table).getTableSelectionListener();
        doReturn(selectedOnlySelectionListener).when(table).getSelectedOnlySelectionListener();

        final ObservableList<String> vertex1 = FXCollections.observableList(List.of("Vertex1Attr1", "Vertex1Attr2"));
        final ObservableList<String> vertex2 = FXCollections.observableList(List.of("Vertex2Attr1", "Vertex2Attr2"));
        final ObservableList<String> vertex3 = FXCollections.observableList(List.of("Vertex3Attr1", "Vertex3Attr2"));

        when(activeTableReference.getElementIdToRowIndex()).thenReturn(Map.of(
                100, vertex1,
                102, vertex2,
                103, vertex3
        ));

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);

        // Order is important here. Should match on vertex 1 and 2, so indicies 0 and 2.
        when(tableView.getItems()).thenReturn(FXCollections.observableList(List.of(vertex1, vertex3, vertex2)));

        final TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableViewSelectionModel.class);
        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        final ObservableList<ObservableList<String>> selectedItems = mock(ObservableList.class);
        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        final ReadOnlyObjectProperty<ObservableList<String>> selectedProperty = mock(ReadOnlyObjectProperty.class);
        when(table.getSelectedProperty()).thenReturn(selectedProperty);

        try (MockedStatic<TableViewUtilities> tableUtilsMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            tableUtilsMockedStatic.when(() -> TableViewUtilities.getSelectedIds(graph, tableViewState)).thenReturn(List.of(100, 102));

            table.updateSelection(graph, tableViewState);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(selectedProperty).removeListener(tableSelectionListener);
        verify(selectedItems).removeListener(selectedOnlySelectionListener);

        verify(selectionModel).clearSelection();
        verify(selectionModel).selectIndices(0, 0, 2);

        verify(selectedProperty).addListener(tableSelectionListener);
        verify(selectedItems).addListener(selectedOnlySelectionListener);
    }

    @Test
    public void updateSelectionNotInSelectedOnlyModeAndNothingSelected() throws InterruptedException {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setSelectedOnly(false);
        tableViewState.setElementType(GraphElementType.VERTEX);

        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);

        doReturn(tableSelectionListener).when(table).getTableSelectionListener();
        doReturn(selectedOnlySelectionListener).when(table).getSelectedOnlySelectionListener();

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);

        final TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableViewSelectionModel.class);
        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        final ObservableList<ObservableList<String>> selectedItems = mock(ObservableList.class);
        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        final ReadOnlyObjectProperty<ObservableList<String>> selectedProperty = mock(ReadOnlyObjectProperty.class);
        when(table.getSelectedProperty()).thenReturn(selectedProperty);

        try (MockedStatic<TableViewUtilities> tableUtilsMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            tableUtilsMockedStatic.when(() -> TableViewUtilities.getSelectedIds(graph, tableViewState)).thenReturn(List.of());

            table.updateSelection(graph, tableViewState);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(selectedProperty).removeListener(tableSelectionListener);
        verify(selectedItems).removeListener(selectedOnlySelectionListener);

        verify(selectionModel).clearSelection();
        verify(selectionModel, times(0)).selectIndices(anyInt(), any(int[].class));

        verify(selectedProperty).addListener(tableSelectionListener);
        verify(selectedItems).addListener(selectedOnlySelectionListener);
    }

    @Test
    public void updateSortOrderPrefSortNull() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setSortByColumn(null);

        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);

        table.updateSortOrder();

        verifyNoInteractions(tableView);
    }

    @Test
    public void updateSortOrderPrefSortEmpty() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setSortByColumn(ImmutablePair.of("", TableColumn.SortType.DESCENDING));

        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);

        table.updateSortOrder();

        verifyNoInteractions(tableView);
    }

    @Test
    public void updateSortOrder() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setSortByColumn(ImmutablePair.of("COLUMN_B", TableColumn.SortType.DESCENDING));

        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);

        final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);
        when(column1.getText()).thenReturn("COLUMN_A");
        final TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);
        when(column2.getText()).thenReturn("COLUMN_B");

        when(tableView.getColumns()).thenReturn(FXCollections.observableList(List.of(column1, column2)));

        final ObservableList<TableColumn<ObservableList<String>, ?>> sortOrder = FXCollections.observableArrayList();
        when(tableView.getSortOrder()).thenReturn(sortOrder);

        table.updateSortOrder();

        verify(column2).setSortType(TableColumn.SortType.DESCENDING);

        assertEquals(FXCollections.observableArrayList(column2), sortOrder);
    }

    @Test
    public void updateColumnsGraphNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateColumns(null, new TableViewState());

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void updateColumnsStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateColumns(graph, null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateColumnsOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            table.updateColumns(graph, new TableViewState());
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateColumnsOnSwingThread() {
        try (final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

            table.updateColumns(graph, new TableViewState());
        }
    }

    @Test
    public void updateColumnsThreadInterrupted() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        when(activeTableReference.getColumnIndex()).thenReturn(new ArrayList<>());

        // Set up the table state
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.TRANSACTION);
        tableViewState.setColumnAttributes(new ArrayList<>());

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            Thread.currentThread().interrupt();

            table.updateColumns(graph, tableViewState);

            assertTrue(Thread.currentThread().isInterrupted());

            platformMockedStatic.verify(() -> Platform.runLater(any(Runnable.class)), times(0));
        }

        // Clears the interrupted state
        Thread.interrupted();
    }

    @Test
    public void updateColumns() {
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);

        doReturn(tableSelectionListener).when(table).getTableSelectionListener();
        doReturn(selectedOnlySelectionListener).when(table).getSelectedOnlySelectionListener();

        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        // Set up the initial column index. Column 4 will not be found in the graph and
        // dropped in the column index created by the update call
        final String columnType1 = "source.";
        final Attribute attribute1 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);
        when(column1.getText()).thenReturn("source.COLUMN_A");

        final String columnType2 = "destination.";
        final Attribute attribute2 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);
        when(column2.getText()).thenReturn("destination.COLUMN_A");

        final String columnType3 = "transaction.";
        final Attribute attribute3 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column3 = mock(TableColumn.class);
        when(column3.getText()).thenReturn("transaction.COLUMN_B");

        final String columnType4 = "source.";
        final Attribute attribute4 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column4 = mock(TableColumn.class);
        when(column4.getText()).thenReturn("source.COLUMN_C");

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(columnType1, attribute1, column1));
        columnIndex.add(new Column(columnType2, attribute2, column2));
        columnIndex.add(new Column(columnType3, attribute3, column3));
        columnIndex.add(new Column(columnType4, attribute4, column4));

        when(activeTableReference.getColumnIndex()).thenReturn(columnIndex);

        // This is a reference of the old column index that will be used whilst the new
        // index is being created. Because that creation is mocked this is used only as a
        // vertification that the parameter is being correctly constructed.
        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = Map.of(
                "source.COLUMN_A", column1,
                "destination.COLUMN_A", column2,
                "transaction.COLUMN_B", column3,
                "source.COLUMN_C", column4
        );

        // Mock out the re-population of the column index from the graph. This excludes column 4.
        final CopyOnWriteArrayList<Column> sourceColumnIndex
                = new CopyOnWriteArrayList<>();
        sourceColumnIndex.add(new Column(columnType1, attribute1, column1));

        final CopyOnWriteArrayList<Column> destinationColumnIndex
                = new CopyOnWriteArrayList<>();
        destinationColumnIndex.add(new Column(columnType2, attribute2, column2));

        final CopyOnWriteArrayList<Column> transactionColumnIndex
                = new CopyOnWriteArrayList<>();
        transactionColumnIndex.add(new Column(columnType3, attribute3, column3));

        doReturn(sourceColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "source.", columnReferenceMap);
        doReturn(destinationColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "destination.", columnReferenceMap);
        doReturn(transactionColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap);

        // Set up the table state
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.TRANSACTION);

        // This is used by the sort comparator. This will order the columnIndex
        // in a certain way that we can then verify below
        tableViewState.setColumnAttributes(List.of(
                Tuple.create("source.", attribute1),
                Tuple.create("transaction.", attribute3),
                Tuple.create("destination.", attribute2)
        ));

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .then(mockInvocation -> {
                        assertTrue(mockInvocation.getArgument(0) instanceof UpdateColumnsTask);
                        return null;
                    });

            table.updateColumns(graph, tableViewState);
        }

        // Verify the new column index
        final CopyOnWriteArrayList<Column> expectedColumnIndex
                = new CopyOnWriteArrayList<>();
        expectedColumnIndex.add(new Column(columnType1, attribute1, column1));
        expectedColumnIndex.add(new Column(columnType3, attribute3, column3));
        expectedColumnIndex.add(new Column(columnType2, attribute2, column2));

        assertEquals(expectedColumnIndex, columnIndex);

        verify(column1, times(1)).setCellValueFactory(any(Callback.class));
        verify(column2, times(1)).setCellValueFactory(any(Callback.class));
        verify(column3, times(1)).setCellValueFactory(any(Callback.class));

        verify(column1, times(1)).setCellFactory(any(Callback.class));
        verify(column2, times(1)).setCellFactory(any(Callback.class));
        verify(column3, times(1)).setCellFactory(any(Callback.class));
    }

    @Test
    public void updateColumnsLink() {
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);

        doReturn(tableSelectionListener).when(table).getTableSelectionListener();
        doReturn(selectedOnlySelectionListener).when(table).getSelectedOnlySelectionListener();

        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        // Set up the initial column index. Column 4 will not be found in the graph and
        // dropped in the column index created by the update call
        final String columnType1 = "low.";
        final Attribute attribute1 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);
        when(column1.getText()).thenReturn("low.COLUMN_A");

        final String columnType2 = "high.";
        final Attribute attribute2 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);
        when(column2.getText()).thenReturn("high.COLUMN_A");

        final String columnType3 = "transaction.";
        final Attribute attribute3 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column3 = mock(TableColumn.class);
        when(column3.getText()).thenReturn("transaction.COLUMN_B");

        final String columnType4 = "source.";
        final Attribute attribute4 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column4 = mock(TableColumn.class);
        when(column4.getText()).thenReturn("source.COLUMN_C");

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(columnType1, attribute1, column1));
        columnIndex.add(new Column(columnType2, attribute2, column2));
        columnIndex.add(new Column(columnType3, attribute3, column3));
        columnIndex.add(new Column(columnType4, attribute4, column4));

        when(activeTableReference.getColumnIndex()).thenReturn(columnIndex);

        // This is a reference of the old column index that will be used whilst the new
        // index is being created. Because that creation is mocked this is used only as a
        // vertification that the parameter is being correctly constructed.
        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = Map.of(
                "low.COLUMN_A", column1,
                "high.COLUMN_A", column2,
                "transaction.COLUMN_B", column3,
                "source.COLUMN_C", column4
        );

        // Mock out the re-population of the column index from the graph. This excludes column 4.
        final CopyOnWriteArrayList<Column> sourceColumnIndex
                = new CopyOnWriteArrayList<>();
        sourceColumnIndex.add(new Column(columnType1, attribute1, column1));

        final CopyOnWriteArrayList<Column> destinationColumnIndex
                = new CopyOnWriteArrayList<>();
        destinationColumnIndex.add(new Column(columnType2, attribute2, column2));

        final CopyOnWriteArrayList<Column> transactionColumnIndex
                = new CopyOnWriteArrayList<>();
        transactionColumnIndex.add(new Column(columnType3, attribute3, column3));

        doReturn(sourceColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "low.", columnReferenceMap);
        doReturn(destinationColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "high.", columnReferenceMap);
        doReturn(transactionColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap);

        // Set up the table state
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.LINK);

        // This is used by the sort comparator. This will order the columnIndex
        // in a certain way that we can then verify below
        tableViewState.setColumnAttributes(List.of(
                Tuple.create("low.", attribute1),
                Tuple.create("transaction.", attribute3),
                Tuple.create("high.", attribute2)
        ));

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .then(mockInvocation -> {
                        assertTrue(mockInvocation.getArgument(0) instanceof UpdateColumnsTask);
                        return null;
                    });

            table.updateColumns(graph, tableViewState);
        }

        // Verify the new column index
        final CopyOnWriteArrayList<Column> expectedColumnIndex
                = new CopyOnWriteArrayList<>();
        expectedColumnIndex.add(new Column(columnType1, attribute1, column1));
        expectedColumnIndex.add(new Column(columnType3, attribute3, column3));
        expectedColumnIndex.add(new Column(columnType2, attribute2, column2));

        assertEquals(expectedColumnIndex, columnIndex);

        verify(column1, times(1)).setCellValueFactory(any(Callback.class));
        verify(column2, times(1)).setCellValueFactory(any(Callback.class));
        verify(column3, times(1)).setCellValueFactory(any(Callback.class));

        verify(column1, times(1)).setCellFactory(any(Callback.class));
        verify(column2, times(1)).setCellFactory(any(Callback.class));
        verify(column3, times(1)).setCellFactory(any(Callback.class));
    }

    @Test
    public void updateColumnsStateColumnsNotSet() {
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);

        doReturn(tableSelectionListener).when(table).getTableSelectionListener();
        doReturn(selectedOnlySelectionListener).when(table).getSelectedOnlySelectionListener();

        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        // Set up the initial column index.
        final String columnType1 = "source.";
        final Attribute attribute1 = mock(Attribute.class);
        final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);
        when(column1.getText()).thenReturn("source.COLUMN_A");

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(columnType1, attribute1, column1));

        when(activeTableReference.getColumnIndex()).thenReturn(columnIndex);

        // This is a reference of the old column index that will be used whilst the new
        // index is being created. Because that creation is mocked this is used only as a
        // vertification that the parameter is being correctly constructed.
        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = Map.of(
                "source.COLUMN_A", column1
        );

        // Mock out the re-population of the column index from the graph
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> sourceColumnIndex
                = new CopyOnWriteArrayList<>();
        sourceColumnIndex.add(ThreeTuple.create(columnType1, attribute1, column1));

        doReturn(sourceColumnIndex).when(table)
                .createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "source.", columnReferenceMap);

        // Set up the table state
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.VERTEX);

        // When this is null, the update is interrupted and the user is asked to select
        // which columns they want visible
        tableViewState.setColumnAttributes(null);

        // Don't want it trying to open the menu to select which columns to show
        doNothing().when(table).openColumnVisibilityMenu();

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .then(mockInvocation -> {
                        assertTrue(mockInvocation.getArgument(0) instanceof UpdateColumnsTask);
                        return null;
                    });

            table.updateColumns(graph, tableViewState);
        }

        // Verify the new column index
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> expectedColumnIndex
                = new CopyOnWriteArrayList<>();
        expectedColumnIndex.add(ThreeTuple.create(columnType1, attribute1, column1));

        assertEquals(expectedColumnIndex, columnIndex);

        // Because the state element type is set to vertex, only the source columns are populated
        verify(table, times(0)).createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "destination.", columnReferenceMap);
        verify(table, times(0)).createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap);

        // Verify the menu open method was called
        verify(table).openColumnVisibilityMenu();

        // Verify that the new column index did not have its factories set
        verify(column1, times(0)).setCellValueFactory(any(Callback.class));
        verify(column1, times(0)).setCellFactory(any(Callback.class));

    }

    @Test
    public void createColumnIndexPart() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        // Build the graph that the columns will be extracted from. There are two
        // verticies and one transaction. When new columns are created for verticies,
        // a source version and a destination version is created. Trasactions attributes
        // have a 1:1 relationship with the columns created.
        when(readableGraph.getAttributeCount(GraphElementType.VERTEX)).thenReturn(2);
        when(readableGraph.getAttributeCount(GraphElementType.TRANSACTION)).thenReturn(1);

        when(readableGraph.getAttribute(GraphElementType.VERTEX, 0)).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_A");
        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);
        final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);

        when(readableGraph.getAttribute(GraphElementType.VERTEX, 1)).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_B");
        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);
        final TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, 0)).thenReturn(103);
        when(readableGraph.getAttributeName(103)).thenReturn("COLUMN_C");
        final Attribute attribute3 = new GraphAttribute(readableGraph, 103);
        final TableColumn<ObservableList<String>, String> column3 = mock(TableColumn.class);

        // This is a reference to the old column index
        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = Map.of(
                "source.COLUMN_A", column1,
                "destination.COLUMN_B", column2,
                "transaction.COLUMN_C", column3
        );

        // When the column being added is not in the old column index reference a new one
        // needs to be created
        final TableColumn<ObservableList<String>, String> newColumn1 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, String> newColumn2 = mock(TableColumn.class);

        when(table.createColumn("source.COLUMN_B")).thenReturn(newColumn1);
        when(table.createColumn("destination.COLUMN_A")).thenReturn(newColumn2);

        // Create the expected outputs for source, destination and trasaction column creations
        final CopyOnWriteArrayList<Column> sourceColumnIndex
                = new CopyOnWriteArrayList<>();

        sourceColumnIndex.add(new Column("source.", attribute1, column1));
        sourceColumnIndex.add(new Column("source.", attribute2, newColumn1));

        final CopyOnWriteArrayList<Column> destinationColumnIndex
                = new CopyOnWriteArrayList<>();

        destinationColumnIndex.add(new Column("destination.", attribute1, newColumn2));
        destinationColumnIndex.add(new Column("destination.", attribute2, column2));

        final CopyOnWriteArrayList<Column> transactionColumnIndex
                = new CopyOnWriteArrayList<>();

        transactionColumnIndex.add(new Column("transaction.", attribute3, column3));

        assertEquals(sourceColumnIndex, table.createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "source.", columnReferenceMap));
        assertEquals(destinationColumnIndex, table.createColumnIndexPart(readableGraph, GraphElementType.VERTEX, "destination.", columnReferenceMap));
        assertEquals(transactionColumnIndex, table.createColumnIndexPart(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap));
    }

    @Test
    public void updateDataGraphNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateData(null, new TableViewState(), null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void updateDataStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateData(graph, null, null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateDataOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            table.updateData(graph, new TableViewState(), null);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateDataOnSwingThread() {
        try (final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

            table.updateData(graph, new TableViewState(), null);
        }
    }

    @Test
    public void updateDataThreadInterrupted() {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.TRANSACTION);
        tableViewState.setSelectedOnly(false);

        final BorderPane progressPane = mock(BorderPane.class);

        final ProgressBar progressBar = mock(ProgressBar.class);
        when(progressBar.getProgressPane()).thenReturn(progressPane);

        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        doReturn(new HashMap<>()).when(activeTableReference).getElementIdToRowIndex();
        doReturn(new HashMap<>()).when(activeTableReference).getRowToElementIdIndex();

        when(readableGraph.getTransactionCount()).thenReturn(0);

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            Thread.currentThread().interrupt();

            table.updateData(graph, tableViewState, progressBar);

            assertTrue(Thread.currentThread().isInterrupted());

            platformMockedStatic.verify(() -> Platform.runLater(any(UpdateDataTask.class)), times(0));
        }

        // Clears interrupt status
        Thread.interrupted();
    }

    @Test
    public void updateDataTransactionStateNotSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.TRANSACTION, false, row1, row2, null, null, null, null, null, null, List.of(row1, row2));
    }

    @Test
    public void updateDataTransactionStateSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.TRANSACTION, true, row1, row2, null, null, null, null, null, null, List.of(row2));
    }

    @Test
    public void updateDataVertexStateNotSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.VERTEX, false, null, null, row1, row2, null, null, null, null, List.of(row1, row2));
    }

    @Test
    public void updateDataVertexStateSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.VERTEX, true, null, null, row1, row2, null, null, null, null, List.of(row2));
    }

    @Test
    public void updateDataEdgeStateNotSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.EDGE, false, null, null, null, null, row1, row2, null, null, List.of(row1, row2));
    }

    @Test
    public void updateDataEdgeStateSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.EDGE, true, null, null, null, null, row1, row2, null, null, List.of(row1, row2));
    }

    @Test
    public void updateDataLinkStateNotSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.LINK, false, null, null, null, null, null, null, row1, row2, List.of(row1, row2));
    }

    @Test
    public void updateDataLinkStateSelectedOnly() {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        testUpdateData(GraphElementType.LINK, true, null, null, null, null, null, null, row1, row2, List.of(row1, row2));
    }

    @Test
    public void getRowDataForVertex() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int vertexId = 42;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_A")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_A");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Object objectValue1 = new Object();
        when(readableGraph.getObjectValue(101, vertexId)).thenReturn(objectValue1);

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(null, attribute1, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(objectValue1)).thenReturn("column1Value");

            assertEquals(FXCollections.observableArrayList("column1Value"),
                    table.getRowDataForVertex(readableGraph, vertexId));

            assertEquals(Map.of(vertexId, FXCollections.observableArrayList("column1Value")), elementIdToRowIndex);
            assertEquals(Map.of(FXCollections.observableArrayList("column1Value"), vertexId), rowToElementIdIndex);
        }
    }

    @Test
    public void getRowDataForTransaction() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int transactionId = 42;

        final int sourceVertexId = 52;
        final int destinationVertexId = 62;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_1")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_1");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "COLUMN_2")).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_2");
        when(readableGraph.getAttributeElementType(102)).thenReturn(GraphElementType.TRANSACTION);
        when(readableGraph.getAttributeType(102)).thenReturn("string");

        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);

        // There are 2 columns. One called COLUMN_1 and the other COLUMN_2. COLUMN_1
        // is present on source and destination verticies. COLUMN_2 is present on transactions
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("source.", attribute1, null));
        columnIndex.add(new Column("destination.", attribute1, null));
        columnIndex.add(new Column("transaction.", attribute2, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        // When looking at a source vertex column, it gets the source vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object sourceVertexCoulmnValue = new Object();
        when(readableGraph.getTransactionSourceVertex(transactionId)).thenReturn(sourceVertexId);
        when(readableGraph.getObjectValue(101, sourceVertexId)).thenReturn(sourceVertexCoulmnValue);

        // When looking at a destination vertex column, it gets the destination vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object destinationVertexCoulmnValue = new Object();
        when(readableGraph.getTransactionDestinationVertex(transactionId)).thenReturn(destinationVertexId);
        when(readableGraph.getObjectValue(101, destinationVertexId)).thenReturn(destinationVertexCoulmnValue);

        // When looking at a transaction column, it extracts the value from the passed transaction
        final Object transactionCoulmnValue = new Object();
        when(readableGraph.getObjectValue(102, transactionId)).thenReturn(transactionCoulmnValue);

        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(sourceVertexCoulmnValue)).thenReturn("sourceVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(destinationVertexCoulmnValue)).thenReturn("destinationVertext_COLUMN_1_Value");
            when(interaction.getDisplayText(transactionCoulmnValue)).thenReturn("transaction_COLUMN_2_Value");

            assertEquals(
                    FXCollections.observableArrayList(
                            "sourceVertex_COLUMN_1_Value",
                            "destinationVertext_COLUMN_1_Value",
                            "transaction_COLUMN_2_Value"
                    ),
                    table.getRowDataForTransaction(readableGraph, transactionId)
            );

            assertEquals(
                    Map.of(
                            transactionId,
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertext_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            )
                    ),
                    elementIdToRowIndex
            );

            assertEquals(
                    Map.of(
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertext_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            ),
                            transactionId
                    ),
                    rowToElementIdIndex);
        }
    }

    @Test
    public void getRowDataForEdge() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int edgeId = 42;

        final int sourceVertexId = 52;
        final int destinationVertexId = 62;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_1")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_1");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "COLUMN_2")).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_2");
        when(readableGraph.getAttributeElementType(102)).thenReturn(GraphElementType.TRANSACTION);
        when(readableGraph.getAttributeType(102)).thenReturn("string");

        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);

        // There are 2 columns. One called COLUMN_1 and the other COLUMN_2. COLUMN_1
        // is present on source and destination verticies. COLUMN_2 is present on transactions
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("source.", attribute1, null));
        columnIndex.add(new Column("destination.", attribute1, null));
        columnIndex.add(new Column("transaction.", attribute2, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        // When looking at a source vertex column, it gets the source vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object sourceVertexCoulmnValue = new Object();
        when(readableGraph.getEdgeSourceVertex(edgeId)).thenReturn(sourceVertexId);
        when(readableGraph.getObjectValue(101, sourceVertexId)).thenReturn(sourceVertexCoulmnValue);

        // When looking at a destination vertex column, it gets the destination vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object destinationVertexCoulmnValue = new Object();
        when(readableGraph.getEdgeDestinationVertex(edgeId)).thenReturn(destinationVertexId);
        when(readableGraph.getObjectValue(101, destinationVertexId)).thenReturn(destinationVertexCoulmnValue);

        // When looking at a transaction column, it extracts the value from the passed transaction
        final Object transactionColumnValue = new Object();
        when(readableGraph.getObjectValue(102, edgeId)).thenReturn(transactionColumnValue);
        when(readableGraph.getObjectValue(102, 0)).thenReturn(transactionColumnValue);

        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(sourceVertexCoulmnValue)).thenReturn("sourceVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(destinationVertexCoulmnValue)).thenReturn("destinationVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(transactionColumnValue)).thenReturn("transaction_COLUMN_2_Value");

            assertEquals(
                    table.getRowDataForEdge(readableGraph, edgeId),
                    FXCollections.observableArrayList(
                            "sourceVertex_COLUMN_1_Value",
                            "destinationVertex_COLUMN_1_Value",
                            "transaction_COLUMN_2_Value"
                    )
            );

            assertEquals(
                    elementIdToRowIndex,
                    Map.of(
                            edgeId,
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertex_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            )
                    )
            );

            assertEquals(
                    rowToElementIdIndex,
                    Map.of(
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertex_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            ),
                            edgeId
                    )
            );
        }
    }

    @Test
    public void getRowDataForEdgeMultipleValues() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int edgeId = 42;

        final int sourceVertexId = 52;
        final int destinationVertexId = 62;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_1")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_1");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "COLUMN_2")).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_2");
        when(readableGraph.getAttributeElementType(102)).thenReturn(GraphElementType.TRANSACTION);
        when(readableGraph.getAttributeType(102)).thenReturn("string");

        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);

        // There are 2 columns. One called COLUMN_1 and the other COLUMN_2. COLUMN_1
        // is present on source and destination verticies. COLUMN_2 is present on transactions
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("source.", attribute1, null));
        columnIndex.add(new Column("destination.", attribute1, null));
        columnIndex.add(new Column("transaction.", attribute2, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        // When looking at a source vertex column, it gets the source vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object sourceVertexCoulmnValue = new Object();
        when(readableGraph.getEdgeSourceVertex(edgeId)).thenReturn(sourceVertexId);
        when(readableGraph.getObjectValue(101, sourceVertexId)).thenReturn(sourceVertexCoulmnValue);

        // When looking at a destination vertex column, it gets the destination vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object destinationVertexCoulmnValue = new Object();
        when(readableGraph.getEdgeDestinationVertex(edgeId)).thenReturn(destinationVertexId);
        when(readableGraph.getObjectValue(101, destinationVertexId)).thenReturn(destinationVertexCoulmnValue);

        // When looking at a transaction column, it extracts the value from the passed transaction
        final Object transactionColumnValue1 = new Object();
        final Object transactionColumnValue2 = new Object();
        when(readableGraph.getObjectValue(102, 0)).thenReturn(transactionColumnValue1);
        when(readableGraph.getObjectValue(102, 1)).thenReturn(transactionColumnValue2);

        when(readableGraph.getEdgeTransactionCount(edgeId)).thenReturn(2);
        
        when(readableGraph.getEdgeTransaction(edgeId, 0)).thenReturn(0);
        when(readableGraph.getEdgeTransaction(edgeId, 1)).thenReturn(1);

        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(sourceVertexCoulmnValue)).thenReturn("sourceVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(destinationVertexCoulmnValue)).thenReturn("destinationVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(transactionColumnValue1)).thenReturn("transaction_COLUMN_2_Value1");
            when(interaction.getDisplayText(transactionColumnValue2)).thenReturn("transaction_COLUMN_2_Value2");
            ObservableList<String> l = table.getRowDataForEdge(readableGraph, edgeId);
            System.out.println(l);
            assertEquals(
                    l,
                    FXCollections.observableArrayList(
                            "sourceVertex_COLUMN_1_Value",
                            "destinationVertex_COLUMN_1_Value",
                            MULTI_VALUE
                    )
            );

            assertEquals(
                    elementIdToRowIndex,
                    Map.of(
                            edgeId,
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertex_COLUMN_1_Value",
                                    MULTI_VALUE
                            )
                    )
            );

            assertEquals(
                    rowToElementIdIndex,
                    Map.of(
                            FXCollections.observableArrayList(
                                    "sourceVertex_COLUMN_1_Value",
                                    "destinationVertex_COLUMN_1_Value",
                                    MULTI_VALUE
                            ),
                            edgeId
                    )
            );
        }
    }
    
    @Test
    public void getRowDataForLink() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int linkId = 42;

        final int lowVertexId = 52;
        final int highVertexId = 62;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_1")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_1");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "COLUMN_2")).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_2");
        when(readableGraph.getAttributeElementType(102)).thenReturn(GraphElementType.TRANSACTION);
        when(readableGraph.getAttributeType(102)).thenReturn("string");

        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);

        // There are 2 columns. One called COLUMN_1 and the other COLUMN_2. COLUMN_1
        // is present on source and destination verticies. COLUMN_2 is present on transactions
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("low.", attribute1, null));
        columnIndex.add(new Column("high.", attribute1, null));
        columnIndex.add(new Column("transaction.", attribute2, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        // When looking at a source vertex column, it gets the source vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object lowVertexCoulmnValue = new Object();
        when(readableGraph.getLinkLowVertex(linkId)).thenReturn(lowVertexId);
        when(readableGraph.getObjectValue(101, lowVertexId)).thenReturn(lowVertexCoulmnValue);

        // When looking at a destination vertex column, it gets the destination vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object highVertexCoulmnValue = new Object();
        when(readableGraph.getLinkHighVertex(linkId)).thenReturn(highVertexId);
        when(readableGraph.getObjectValue(101, highVertexId)).thenReturn(highVertexCoulmnValue);

        // When looking at a transaction column, it extracts the value from the passed transaction
        final Object transactionColumnValue = new Object();
        when(readableGraph.getObjectValue(102, linkId)).thenReturn(transactionColumnValue);
        when(readableGraph.getObjectValue(102, 0)).thenReturn(transactionColumnValue);

        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(lowVertexCoulmnValue)).thenReturn("lowVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(highVertexCoulmnValue)).thenReturn("highVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(transactionColumnValue)).thenReturn("transaction_COLUMN_2_Value");

            assertEquals(
                    table.getRowDataForLink(readableGraph, linkId),
                    FXCollections.observableArrayList(
                            "lowVertex_COLUMN_1_Value",
                            "highVertex_COLUMN_1_Value",
                            "transaction_COLUMN_2_Value"
                    )
            );

            assertEquals(
                    elementIdToRowIndex,
                    Map.of(
                            linkId,
                            FXCollections.observableArrayList(
                                    "lowVertex_COLUMN_1_Value",
                                    "highVertex_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            )
                    )
            );

            assertEquals(
                    rowToElementIdIndex,
                    Map.of(
                            FXCollections.observableArrayList(
                                    "lowVertex_COLUMN_1_Value",
                                    "highVertex_COLUMN_1_Value",
                                    "transaction_COLUMN_2_Value"
                            ),
                            linkId
                    )
            );
        }
    }

    @Test
    public void getRowDataForLinkMultipleValues() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        final int linkId = 42;

        final int lowVertexId = 52;
        final int highVertexId = 62;

        // Set up the attributes for each column
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "COLUMN_1")).thenReturn(101);
        when(readableGraph.getAttributeName(101)).thenReturn("COLUMN_1");
        when(readableGraph.getAttributeElementType(101)).thenReturn(GraphElementType.VERTEX);
        when(readableGraph.getAttributeType(101)).thenReturn("string");

        final Attribute attribute1 = new GraphAttribute(readableGraph, 101);

        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "COLUMN_2")).thenReturn(102);
        when(readableGraph.getAttributeName(102)).thenReturn("COLUMN_2");
        when(readableGraph.getAttributeElementType(102)).thenReturn(GraphElementType.TRANSACTION);
        when(readableGraph.getAttributeType(102)).thenReturn("string");

        final Attribute attribute2 = new GraphAttribute(readableGraph, 102);

        // There are 2 columns. One called COLUMN_1 and the other COLUMN_2. COLUMN_1
        // is present on source and destination verticies. COLUMN_2 is present on transactions
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("low.", attribute1, null));
        columnIndex.add(new Column("high.", attribute1, null));
        columnIndex.add(new Column("transaction.", attribute2, null));

        when(table.getColumnIndex()).thenReturn(columnIndex);

        // When looking at a source vertex column, it gets the source vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object lowVertexCoulmnValue = new Object();
        when(readableGraph.getLinkLowVertex(linkId)).thenReturn(lowVertexId);
        when(readableGraph.getObjectValue(101, lowVertexId)).thenReturn(lowVertexCoulmnValue);

        // When looking at a destination vertex column, it gets the destination vertex of
        // the transaction and extracts the value for the column from that vertex
        final Object highVertexCoulmnValue = new Object();
        when(readableGraph.getLinkHighVertex(linkId)).thenReturn(highVertexId);
        when(readableGraph.getObjectValue(101, highVertexId)).thenReturn(highVertexCoulmnValue);

        // When looking at a transaction column, it extracts the value from the passed transaction       
        final Object transactionColumnValue1 = new Object();
        final Object transactionColumnValue2 = new Object();
        when(readableGraph.getObjectValue(102, 0)).thenReturn(transactionColumnValue1);
        when(readableGraph.getObjectValue(102, 1)).thenReturn(transactionColumnValue2);

        when(readableGraph.getLinkTransactionCount(linkId)).thenReturn(2);
        
        when(readableGraph.getLinkTransaction(linkId, 0)).thenReturn(0);
        when(readableGraph.getLinkTransaction(linkId, 1)).thenReturn(1);
        
        try (final MockedStatic<AbstractAttributeInteraction> attrInteractionMockedStatic
                = Mockito.mockStatic(AbstractAttributeInteraction.class)) {
            final AbstractAttributeInteraction interaction = mock(AbstractAttributeInteraction.class);
            attrInteractionMockedStatic.when(() -> AbstractAttributeInteraction.getInteraction("string")).thenReturn(interaction);

            when(interaction.getDisplayText(lowVertexCoulmnValue)).thenReturn("lowVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(highVertexCoulmnValue)).thenReturn("highVertex_COLUMN_1_Value");
            when(interaction.getDisplayText(transactionColumnValue1)).thenReturn("transaction_COLUMN_2_Value1");
            when(interaction.getDisplayText(transactionColumnValue2)).thenReturn("transaction_COLUMN_2_Value2");
            
            assertEquals(
                    table.getRowDataForLink(readableGraph, linkId),
                    FXCollections.observableArrayList(
                            "lowVertex_COLUMN_1_Value",
                            "highVertex_COLUMN_1_Value",
                            MULTI_VALUE
                    )
            );

            assertEquals(
                    elementIdToRowIndex,
                    Map.of(
                            linkId,
                            FXCollections.observableArrayList(
                                    "lowVertex_COLUMN_1_Value",
                                    "highVertex_COLUMN_1_Value",
                                    MULTI_VALUE
                            )
                    )
            );

            assertEquals(
                    rowToElementIdIndex,
                    Map.of(
                            FXCollections.observableArrayList(
                                    "lowVertex_COLUMN_1_Value",
                                    "highVertex_COLUMN_1_Value",
                                    MULTI_VALUE
                            ),
                            linkId
                    )
            );
        }
    }

    @Test
    public void createColumn() {
        final TableColumn<ObservableList<String>, String> column = table.createColumn("COLUMN_A");
        assertEquals("COLUMN_A", column.getText());
    }

    /**
     * Tests the update data method. If the initial state's element type is vertex, then the parameters transaction row
     * 1 and 2 can be null. And vice versa.
     *
     * @param stateElementType the initial element type in the table state
     * @param isSelectedOnlyMode true if the table's initial state is in selected only mode, false otherwise
     * @param transactionRow1 row 1 that represents a transaction element in the graph
     * @param transactionRow2 row 2 that represents a transaction element in the graph
     * @param vertexRow1 row 1 that represents a vertex element in the graph
     * @param vertexRow2 row 2 that represents a vertex element in the graph
     * @param expectedRows the expected rows that will be added to the table
     */
    private void testUpdateData(final GraphElementType stateElementType,
            final boolean isSelectedOnlyMode,
            final ObservableList<String> transactionRow1,
            final ObservableList<String> transactionRow2,
            final ObservableList<String> vertexRow1,
            final ObservableList<String> vertexRow2,
            final ObservableList<String> edgeRow1,
            final ObservableList<String> edgeRow2,
            final ObservableList<String> linkRow1,
            final ObservableList<String> linkRow2,
            final List<ObservableList<String>> expectedRows) {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(stateElementType);
        tableViewState.setSelectedOnly(isSelectedOnlyMode);

        final BorderPane progressPane = mock(BorderPane.class);

        final ProgressBar progressBar = mock(ProgressBar.class);
        when(progressBar.getProgressPane()).thenReturn(progressPane);

        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);

        // Initialize row and element ID mappers and place some fake data in
        // to ensure that it is cleared during the update
        final Map<Integer, ObservableList<String>> elementIdToRowIndex = new HashMap<>();
        elementIdToRowIndex.put(42, FXCollections.observableArrayList());

        final Map<ObservableList<String>, Integer> rowToElementIdIndex = new HashMap<>();
        rowToElementIdIndex.put(FXCollections.observableArrayList(), 42);

        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();

        // Mock graph to extract transaction element IDs
        when(readableGraph.getAttribute(stateElementType, "selected")).thenReturn(22);

        when(readableGraph.getTransactionCount()).thenReturn(2);
        when(readableGraph.getVertexCount()).thenReturn(2);
        when(readableGraph.getEdgeCount()).thenReturn(2);
        when(readableGraph.getLinkCount()).thenReturn(2);

        when(readableGraph.getTransaction(0)).thenReturn(101);
        when(readableGraph.getTransaction(1)).thenReturn(102);

        when(readableGraph.getVertex(0)).thenReturn(201);
        when(readableGraph.getVertex(1)).thenReturn(202);

        when(readableGraph.getEdge(0)).thenReturn(301);
        when(readableGraph.getEdge(1)).thenReturn(302);

        when(readableGraph.getLink(0)).thenReturn(401);
        when(readableGraph.getLink(1)).thenReturn(402);

        when(readableGraph.getBooleanValue(22, 101)).thenReturn(false);
        when(readableGraph.getBooleanValue(22, 102)).thenReturn(true);

        when(readableGraph.getBooleanValue(22, 201)).thenReturn(false);
        when(readableGraph.getBooleanValue(22, 202)).thenReturn(true);

        when(readableGraph.getBooleanValue(22, 301)).thenReturn(false);
        when(readableGraph.getBooleanValue(22, 302)).thenReturn(true);

        when(readableGraph.getBooleanValue(22, 401)).thenReturn(false);
        when(readableGraph.getBooleanValue(22, 402)).thenReturn(true);

        // Mock the transaction row creation
        doReturn(transactionRow1).when(table).getRowDataForTransaction(readableGraph, 101);
        doReturn(transactionRow2).when(table).getRowDataForTransaction(readableGraph, 102);

        doReturn(vertexRow1).when(table).getRowDataForVertex(readableGraph, 201);
        doReturn(vertexRow2).when(table).getRowDataForVertex(readableGraph, 202);

        doReturn(edgeRow1).when(table).getRowDataForEdge(readableGraph, 301);
        doReturn(edgeRow2).when(table).getRowDataForEdge(readableGraph, 302);

        doReturn(linkRow1).when(table).getRowDataForLink(readableGraph, 401);
        doReturn(linkRow2).when(table).getRowDataForLink(readableGraph, 402);

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .then(mockitoInvocation -> {
                        final Runnable runnable = (Runnable) mockitoInvocation.getArgument(0);

                        if (runnable instanceof UpdateDataTask updateDataTask) {
                            // If this is not called then the test will halt forever
                            updateDataTask.getUpdateDataLatch().countDown();
                            assertEquals(updateDataTask.getRows(), expectedRows);
                        } else {
                            // Progress Bar
                            runnable.run();
                        }

                        return null;
                    });

            table.updateData(graph, tableViewState, progressBar);
        }

        assertTrue(elementIdToRowIndex.isEmpty());
        assertTrue(rowToElementIdIndex.isEmpty());

        verify(tablePane).setCenter(progressPane);
    }

}
