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
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
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
import javafx.util.Callback;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
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
    private TableViewTopComponent tableTopComponent;
    private TableViewPane tablePane;
    private TableService tableService;
    private Graph graph;
    
    private Table table;
    
    public TableNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tableTopComponent = mock(TableViewTopComponent.class);
        tablePane = mock(TableViewPane.class);
        tableService = mock(TableService.class);
        graph = mock(Graph.class);
        
        table = spy(new Table(tableTopComponent, tablePane, tableService));
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
    }
    
    @Test
    public void updateSelectionGraphNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateSelection(null, new TableViewState(), null, null);
            
            platformMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test
    public void updateSelectionStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateSelection(graph, null, null, null);
            
            platformMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void updateSelectionOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);
            
            table.updateSelection(graph, new TableViewState(), null, null);
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void updateSelectionOnSwingThread() {
        try (final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(true);
            
            table.updateSelection(graph, new TableViewState(), null, null);
        }
    }
    
    @Test
    public void updateSelectionInSelectedOnlyMode() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            final TableViewState tableViewState = new TableViewState();
            tableViewState.setSelectedOnly(true);
            
            table.updateSelection(graph, tableViewState, null, null);
            
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
        
        doReturn(List.of(100, 102)).when(table).getSelectedIds(any(Graph.class), any(TableViewState.class));
        
        final ObservableList<String> vertex1 = FXCollections.observableList(List.of("Vertex1Attr1", "Vertex1Attr2"));
        final ObservableList<String> vertex2 = FXCollections.observableList(List.of("Vertex2Attr1", "Vertex2Attr2"));
        final ObservableList<String> vertex3 = FXCollections.observableList(List.of("Vertex3Attr1", "Vertex3Attr2"));

        
        when(tableService.getElementIdToRowIndex()).thenReturn(Map.of(
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
        
        table.updateSelection(graph, tableViewState, tableSelectionListener, selectedOnlySelectionListener);
        
        verify(table).getSelectedIds(graph, tableViewState);
        
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
        
        doReturn(List.of()).when(table).getSelectedIds(any(Graph.class), any(TableViewState.class));
        
        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);
        
        final TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableViewSelectionModel.class);
        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        
        final ObservableList<ObservableList<String>> selectedItems = mock(ObservableList.class);
        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);
        
        final ReadOnlyObjectProperty<ObservableList<String>> selectedProperty = mock(ReadOnlyObjectProperty.class);
        when(table.getSelectedProperty()).thenReturn(selectedProperty);
        
        table.updateSelection(graph, tableViewState, tableSelectionListener, selectedOnlySelectionListener);
        
        verify(table).getSelectedIds(graph, tableViewState);
        
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
    public void getSelectedIdsForVertecies() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);
        
        when(readableGraph.getAttribute(GraphElementType.VERTEX, "selected")).thenReturn(5);
        when(readableGraph.getVertexCount()).thenReturn(3);
        
        when(readableGraph.getVertex(0)).thenReturn(100);
        when(readableGraph.getVertex(1)).thenReturn(101);
        when(readableGraph.getVertex(2)).thenReturn(102);

        when(readableGraph.getBooleanValue(5, 100)).thenReturn(true);
        when(readableGraph.getBooleanValue(5, 101)).thenReturn(false);
        when(readableGraph.getBooleanValue(5, 102)).thenReturn(true);
        
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.VERTEX);
        
        assertEquals(List.of(100, 102), table.getSelectedIds(graph, tableViewState));
        
        verify(readableGraph).release();
    }
    
    @Test
    public void getSelectedIdsForTransactions() {
        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(readableGraph);
        
        when(readableGraph.getAttribute(GraphElementType.TRANSACTION, "selected")).thenReturn(5);
        when(readableGraph.getTransactionCount()).thenReturn(3);
        
        when(readableGraph.getTransaction(0)).thenReturn(100);
        when(readableGraph.getTransaction(1)).thenReturn(101);
        when(readableGraph.getTransaction(2)).thenReturn(102);

        when(readableGraph.getBooleanValue(5, 100)).thenReturn(true);
        when(readableGraph.getBooleanValue(5, 101)).thenReturn(false);
        when(readableGraph.getBooleanValue(5, 102)).thenReturn(true);
        
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.TRANSACTION);
        
        assertEquals(List.of(100, 102), table.getSelectedIds(graph, tableViewState));
        
        verify(readableGraph).release();
    }
    
    @Test
    public void updateSortOrderPrefSortNull() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(null);
        
        when(tableService.getTablePreferences()).thenReturn(tablePreferences);
        
        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);
        
        table.updateSortOrder();
        
        verifyNoInteractions(tableView);
    }
    
    @Test
    public void updateSortOrderPrefSortEmpty() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(ImmutablePair.of("", TableColumn.SortType.DESCENDING));
        
        when(tableService.getTablePreferences()).thenReturn(tablePreferences);
        
        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(table.getTableView()).thenReturn(tableView);
        
        table.updateSortOrder();
        
        verifyNoInteractions(tableView);
    }
    
    @Test
    public void updateSortOrder() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(ImmutablePair.of("COLUMN_B", TableColumn.SortType.DESCENDING));
        
        when(tableService.getTablePreferences()).thenReturn(tablePreferences);
        
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
            table.updateColumns(null, new TableViewState(), null, null);
            
            platformMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test
    public void updateColumnsStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            table.updateColumns(graph, null, null, null);
            
            platformMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void updateColumnsOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);
            
            table.updateColumns(graph, new TableViewState(), null, null);
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void updateColumnsOnSwingThread() {
        try (final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(true);
            
            table.updateColumns(graph, new TableViewState(), null, null);
        }
    }
    
    @Test
    public void updateColumns() {
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);
        
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
        
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columnIndex
                = new CopyOnWriteArrayList<>();
        columnIndex.add(ThreeTuple.create(columnType1, attribute1, column1));
        columnIndex.add(ThreeTuple.create(columnType2, attribute2, column2));
        columnIndex.add(ThreeTuple.create(columnType3, attribute3, column3));
        columnIndex.add(ThreeTuple.create(columnType4, attribute4, column4));
        
        when(table.getColumnIndex()).thenReturn(columnIndex);
        
        // This is a reference of the old column index that will be used whilst the new
        // index is being created. Because that creation is mocked this is used only as a
        // vertification that the parameter is being correctly constructed.
        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = Map.of(
                "source.COLUMN_A", column1,
                "destination.COLUMN_A", column2,
                "transaction.COLUMN_B", column3,
                "source.COLUMN_C", column4
        );
        
        // Mock out the re-population of the column index from the graph
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> sourceColumnIndex
                = new CopyOnWriteArrayList<>();
        sourceColumnIndex.add(ThreeTuple.create(columnType1, attribute1, column1));
        
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> destinationColumnIndex
                = new CopyOnWriteArrayList<>();
        destinationColumnIndex.add(ThreeTuple.create(columnType2, attribute2, column2));
        
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> transactionColumnIndex
                = new CopyOnWriteArrayList<>();
        transactionColumnIndex.add(ThreeTuple.create(columnType3, attribute3, column3));
        
        doReturn(sourceColumnIndex).when(table)
                .populateColumnIndex(readableGraph, GraphElementType.VERTEX, "source.", columnReferenceMap);
        doReturn(destinationColumnIndex).when(table)
                .populateColumnIndex(readableGraph, GraphElementType.VERTEX, "destination.", columnReferenceMap);
        doReturn(transactionColumnIndex).when(table)
                .populateColumnIndex(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap);
        
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
        
        table.updateColumns(graph, tableViewState, tableSelectionListener, selectedOnlySelectionListener);
        
        // Verify the new column index
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> expectedColumnIndex
                = new CopyOnWriteArrayList<>();
        expectedColumnIndex.add(ThreeTuple.create(columnType1, attribute1, column1));
        expectedColumnIndex.add(ThreeTuple.create(columnType3, attribute3, column3));
        expectedColumnIndex.add(ThreeTuple.create(columnType2, attribute2, column2));
        
        assertEquals(expectedColumnIndex, columnIndex);
        
        verify(column1, times(1)).setCellValueFactory(any(Callback.class));
        verify(column2, times(1)).setCellValueFactory(any(Callback.class));
        verify(column3, times(1)).setCellValueFactory(any(Callback.class));
        
        verify(column1, times(1)).setCellFactory(any(Callback.class));
        verify(column2, times(1)).setCellFactory(any(Callback.class));
        verify(column3, times(1)).setCellFactory(any(Callback.class));
    }
    
    @Test
    public void populateColumnIndex() {
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
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> sourceColumnIndex
                = new CopyOnWriteArrayList<>();
        
        sourceColumnIndex.add(ThreeTuple.create("source.", attribute1, column1));
        sourceColumnIndex.add(ThreeTuple.create("source.", attribute2, newColumn1));
        
        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> destinationColumnIndex
                = new CopyOnWriteArrayList<>();
        
        destinationColumnIndex.add(ThreeTuple.create("destination.", attribute1, newColumn2));
        destinationColumnIndex.add(ThreeTuple.create("destination.", attribute2, column2));

        final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> transactionColumnIndex
                = new CopyOnWriteArrayList<>();
        
        transactionColumnIndex.add(ThreeTuple.create("transaction.", attribute3, column3));
        
        assertEquals(sourceColumnIndex, table.populateColumnIndex(readableGraph, GraphElementType.VERTEX, "source.", columnReferenceMap));
        assertEquals(destinationColumnIndex, table.populateColumnIndex(readableGraph, GraphElementType.VERTEX, "destination.", columnReferenceMap));
        assertEquals(transactionColumnIndex, table.populateColumnIndex(readableGraph, GraphElementType.TRANSACTION, "transaction.", columnReferenceMap));
    }
}
