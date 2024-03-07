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
package au.gov.asd.tac.constellation.views.tableview.factory;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import static org.mockito.ArgumentMatchers.any;
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
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class TableViewPageFactoryNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(TableViewPageFactoryNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TableView<ObservableList<String>> tableView;
    private Table table;
    private TablePane tablePane;
    private ActiveTableReference activeTableReference;

    private TableViewSelectionModel selectionModel;
    private ObservableList<String> selectedItems;
    private ReadOnlyObjectProperty<ObservableList<String>> selectedProperty;

    private Graph graph;

    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ListChangeListener selectedOnlySelectionListener;
    private ChangeListener<? super Comparator<? super ObservableList<String>>> tableComparatorListener;
    private ChangeListener<? super TableColumn.SortType> tableSortTypeListener;

    private SortedList<ObservableList<String>> sortedRowList;
    private Set<ObservableList<String>> selectedOnlySelectedRows;

    private ObjectProperty<Comparator<? super ObservableList<String>>> sortedRowListComparator;
    private ObjectProperty<Comparator<ObservableList<String>>> tableViewComparator;

    private TableViewPageFactory tableViewPageFactory;

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
        tableView = mock(TableView.class);
        table = mock(Table.class);
        tablePane = mock(TablePane.class);
        activeTableReference = mock(ActiveTableReference.class);

        selectionModel = mock(TableViewSelectionModel.class);
        selectedItems = mock(ObservableList.class);
        selectedProperty = mock(ReadOnlyObjectProperty.class);

        graph = mock(Graph.class);

        tableSelectionListener = mock(ChangeListener.class);
        selectedOnlySelectionListener = mock(ListChangeListener.class);
        tableComparatorListener = mock(ChangeListener.class);
        tableSortTypeListener = mock(ChangeListener.class);

        sortedRowListComparator = mock(ObjectProperty.class);
        tableViewComparator = mock(ObjectProperty.class);

        sortedRowList = spy(new SortedList<>(FXCollections.observableArrayList()));
        selectedOnlySelectedRows = new HashSet<>();

        tableViewPageFactory = spy(new TableViewPageFactory(tablePane));

        doReturn(tableComparatorListener).when(tableViewPageFactory).getTableComparatorListener();
        doReturn(tableSortTypeListener).when(tableViewPageFactory).getTableSortTypeListener();

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        when(activeTableReference.getSelectedOnlySelectedRows()).thenReturn(selectedOnlySelectedRows);
        when(activeTableReference.getSortedRowList()).thenReturn(sortedRowList);

        when(sortedRowList.comparatorProperty()).thenReturn(sortedRowListComparator);

        when(table.getTableView()).thenReturn(tableView);
        when(table.getSelectedProperty()).thenReturn(selectedProperty);
        when(table.getTableSelectionListener()).thenReturn(tableSelectionListener);
        when(table.getSelectedOnlySelectionListener()).thenReturn(selectedOnlySelectionListener);

        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        when(tableView.comparatorProperty()).thenReturn(tableViewComparator);
        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);
        when(tableViewTopComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void init() {
        assertNotNull(tableViewPageFactory.getTableComparatorListener());
        assertNotNull(tableViewPageFactory.getTableSortTypeListener());
    }

    @Test
    public void getCurrentSort() {
        final ObservableList<TableColumn<ObservableList<String>, String>> sortOrder = mock(ObservableList.class);
        final TableColumn<ObservableList<String>, String> sortCol = mock(TableColumn.class);
        final ObjectProperty<SortType> sortTypeProperty = mock(ObjectProperty.class);

        doReturn(sortOrder).when(tableView).getSortOrder();
        when(sortOrder.isEmpty()).thenReturn(false);
        when(sortOrder.get(0)).thenReturn(sortCol);
        when(sortCol.getSortType()).thenReturn(TableColumn.SortType.DESCENDING);
        when(sortCol.sortTypeProperty()).thenReturn(sortTypeProperty);

        final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> expectedSort
                = ImmutablePair.of(sortCol, TableColumn.SortType.DESCENDING);

        assertEquals(expectedSort, tableViewPageFactory.getCurrentSort());

        verify(sortTypeProperty).removeListener(tableSortTypeListener);
    }

    @Test
    public void getCurrentSortNoSort() {
        doReturn(FXCollections.observableArrayList()).when(tableView).getSortOrder();

        final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> expectedSort
                = ImmutablePair.of(null, null);

        assertEquals(expectedSort, tableViewPageFactory.getCurrentSort());
    }

    @Test
    public void restoreSort() {
        final ObservableList<TableColumn<ObservableList<String>, String>> sortOrder = mock(ObservableList.class);
        final TableColumn<ObservableList<String>, String> sortCol = mock(TableColumn.class);
        final ObjectProperty<SortType> sortTypeProperty = mock(ObjectProperty.class);

        doReturn(sortOrder).when(tableView).getSortOrder();
        when(sortCol.getSortType()).thenReturn(TableColumn.SortType.DESCENDING);
        when(sortCol.sortTypeProperty()).thenReturn(sortTypeProperty);

        final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> currentSort
                = ImmutablePair.of(sortCol, TableColumn.SortType.DESCENDING);

        tableViewPageFactory.restoreSort(currentSort);

        verify(sortOrder).add(sortCol);

        verify(sortCol).setSortType(SortType.DESCENDING);
        verify(sortTypeProperty).addListener(tableSortTypeListener);
    }

    @Test
    public void restoreSortCurrentSortNull() {
        final ObservableList<TableColumn<ObservableList<String>, String>> sortOrder = mock(ObservableList.class);

        doReturn(sortOrder).when(tableView).getSortOrder();

        tableViewPageFactory.restoreSort(ImmutablePair.of(null, TableColumn.SortType.DESCENDING));

        verifyNoInteractions(sortOrder);
    }

    @Test
    public void restoreListeners() {
        tableViewPageFactory.restoreListeners();

        verify(sortedRowListComparator).addListener(tableComparatorListener);
        verify(selectedProperty).addListener(tableSelectionListener);
        verify(selectedItems).addListener(selectedOnlySelectionListener);
    }

    @Test
    public void removeListeners() {
        tableViewPageFactory.removeListeners();

        verify(sortedRowListComparator).removeListener(tableComparatorListener);
        verify(selectedProperty).removeListener(tableSelectionListener);
        verify(selectedItems).removeListener(selectedOnlySelectionListener);
    }

    @Test
    public void callEnsureExternalCallsMade() {
        doNothing().when(tableViewPageFactory)
                .restoreSelectionFromGraph(any(Graph.class), any(TableViewState.class), any(Map.class));

        final ObservableList<String> row1 = FXCollections.observableArrayList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableArrayList(List.of("row2Column1", "row2Column2"));
        final ObservableList<String> row3 = FXCollections.observableArrayList(List.of("row3Column1", "row3Column2"));

        final List<ObservableList<String>> newRowList = List.of(row1, row2, row3);
        final Map<Integer, ObservableList<String>> elementIdToRowIndex = Map.of();
        final int maxRowsPerPage = 2;
        final int pageIndex = 0;

        final TableColumn<ObservableList<String>, String> sortCol = mock(TableColumn.class);

        final Pair<TableColumn<ObservableList<String>, ?>, TableColumn.SortType> currentSort
                = ImmutablePair.of(sortCol, TableColumn.SortType.DESCENDING);
        doReturn(currentSort).when(tableViewPageFactory).getCurrentSort();
        doNothing().when(tableViewPageFactory).restoreSort(any(Pair.class));

        final TableViewState tableViewState = new TableViewState();
        tableViewState.setSelectedOnly(false);

        when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);

        when(activeTableReference.getElementIdToRowIndex()).thenReturn(elementIdToRowIndex);

        selectedOnlySelectedRows.add(row1);
        selectedOnlySelectedRows.add(row2);

        // This is called after the page set up so just mocking it so that it is realistic
        when(tableView.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));

        tableViewPageFactory.update(newRowList, maxRowsPerPage);

        assertEquals(tableView, tableViewPageFactory.call(pageIndex));

        verify(tableViewPageFactory).removeListeners();

        verify(tableViewPageFactory).getCurrentSort();

        verify(sortedRowListComparator).unbind();

        verify(tableView).setItems(FXCollections.observableList(List.of(row1, row2)));

        verify(sortedRowListComparator).bind(tableViewComparator);

        verify(tableViewPageFactory).restoreSort(currentSort);

        verify(tableViewPageFactory).restoreSelectionFromGraph(graph, tableViewState, elementIdToRowIndex);

        // This is the first call so selectedOnlySelectedRows will be cleared
        // and the selection is not updated
        verify(selectionModel, times(0)).selectIndices(0, 0, 1);

        verify(tableViewPageFactory).restoreListeners();
    }

    @Test
    public void callSelectedOnlyModeMultipleCalls() {
        doNothing().when(tableViewPageFactory)
                .restoreSelectionFromGraph(any(Graph.class), any(TableViewState.class), any(Map.class));

        final ObservableList<String> row1 = FXCollections.observableArrayList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableArrayList(List.of("row2Column1", "row2Column2"));
        final ObservableList<String> row3 = FXCollections.observableArrayList(List.of("row3Column1", "row3Column2"));

        final int maxRowsPerPage = 2;
        final int pageIndex = 0;

        final ObservableList<TableColumn<ObservableList<String>, String>> sortOrder
                = FXCollections.observableArrayList();
        doReturn(sortOrder).when(tableView).getSortOrder();

        final TableViewState tableViewState = new TableViewState();
        tableViewState.setSelectedOnly(true);

        when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);

        // This is called after the page set up so just mocking it so that it is realistic
        when(tableView.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));

        // =============RUN ONE=================
        tableViewPageFactory.update(List.of(row1, row2, row3), maxRowsPerPage);

        // Set the selected only mode selection to rows 1 and 2
        selectedOnlySelectedRows.add(row1);
        selectedOnlySelectedRows.add(row2);

        assertEquals(tableView, tableViewPageFactory.call(pageIndex));

        // In the first call this gets cleared
        assertTrue(selectedOnlySelectedRows.isEmpty());

        // =============RUN TWO=================
        // Re-populate the selected only selected rows set as it was cleared in run 1
        selectedOnlySelectedRows.add(row1);
        selectedOnlySelectedRows.add(row2);

        assertEquals(tableView, tableViewPageFactory.call(pageIndex));

        // The second run has the same new row list so it does not get cleared
        assertEquals(2, selectedOnlySelectedRows.size());

        // =============RUN THREE=================
        // Change the new row list
        tableViewPageFactory.update(List.of(row1, row2), maxRowsPerPage);

        assertEquals(tableView, tableViewPageFactory.call(pageIndex));

        // The third run now has a different new row list so it is cleared again
        assertTrue(selectedOnlySelectedRows.isEmpty());

        // ======================================
        verify(tableView, times(3)).setItems(FXCollections.observableList(List.of(row1, row2)));

        assertTrue(sortOrder.isEmpty());

        // This selection update only happens in the second run as it is called with
        // the same new row list and that avoids it being cleared
        verify(selectionModel, times(1)).selectIndices(0, 0, 1);
    }

    @Test
    public void updateSelectionGraphNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            tableViewPageFactory.restoreSelectionFromGraph(null, new TableViewState(), null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void updateSelectionStateNull() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            tableViewPageFactory.restoreSelectionFromGraph(graph, null, null);

            platformMockedStatic.verifyNoInteractions();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void updateSelectionNotOnFxThread() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            tableViewPageFactory.restoreSelectionFromGraph(graph, new TableViewState(), null);
        }
    }

    @Test
    public void updateSelectionInSelectedOnlyMode() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setSelectedOnly(true);

            tableViewPageFactory.restoreSelectionFromGraph(graph, tableViewState, null);

            verifyNoInteractions(table);
        }
    }

    @Test
    public void updateSelectionNotInSelectedOnlyMode() throws InterruptedException, ExecutionException {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setSelectedOnly(false);
        tableViewState.setElementType(GraphElementType.VERTEX);

        doReturn(List.of(100, 102)).when(tableViewPageFactory)
                .getSelectedIds(any(Graph.class), any(TableViewState.class));

        final ObservableList<String> vertex1 = FXCollections.observableList(
                List.of("Vertex1Attr1", "Vertex1Attr2"));
        final ObservableList<String> vertex2 = FXCollections.observableList(
                List.of("Vertex2Attr1", "Vertex2Attr2"));
        final ObservableList<String> vertex3 = FXCollections.observableList(
                List.of("Vertex3Attr1", "Vertex3Attr2"));

        final Map<Integer, ObservableList<String>> elementIdToRowIndex = Map.of(
                100, vertex1,
                102, vertex2,
                103, vertex3
        );

        // Order is important here. Should match on vertex 1 and 2, so indicies 0 and 2.
        when(tableView.getItems()).thenReturn(
                FXCollections.observableList(List.of(vertex1, vertex3, vertex2))
        );

        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            tableViewPageFactory
                    .restoreSelectionFromGraph(graph, tableViewState, elementIdToRowIndex);
        }

        verify(tableViewPageFactory).getSelectedIds(graph, tableViewState);

        verify(selectionModel).clearSelection();
        verify(selectionModel).selectIndices(0, 0, 2);
    }
}
