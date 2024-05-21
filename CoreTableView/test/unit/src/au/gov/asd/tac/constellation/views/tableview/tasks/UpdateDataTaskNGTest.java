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
package au.gov.asd.tac.constellation.views.tableview.tasks;

import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import org.controlsfx.control.table.TableFilter;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.clearInvocations;
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
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class UpdateDataTaskNGTest {
    private static final Logger LOGGER = Logger.getLogger(UpdateDataTaskNGTest.class.getName());

    private TablePane tablePane;
    private Table table;
    private TableView<ObservableList<String>> tableView;
    private ActiveTableReference activeTableReference;

    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ListChangeListener selectedOnlySelectionListener;

    private List<ObservableList<String>> rows;

    private TableView.TableViewSelectionModel<ObservableList<String>> selectionModel;
    private ReadOnlyObjectProperty<ObservableList<String>> selectedItemProperty;
    private ObservableList<ObservableList<String>> selectedItems;

    private Pagination pagination;

    private UpdateDataTask updateDataTask;

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
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        tableView = mock(TableView.class);

        final SortedList<ObservableList<String>> existingRowList = mock(SortedList.class);
        final ObjectProperty<Comparator<? super ObservableList<String>>> existingRowComparatorProperty
                = mock(ObjectProperty.class);

        when(existingRowList.comparatorProperty()).thenReturn(existingRowComparatorProperty);

        activeTableReference = spy(new ActiveTableReference(null));
        activeTableReference.setSortedRowList(existingRowList);

        pagination = mock(Pagination.class);

        doReturn(pagination).when(activeTableReference).updatePagination(anyInt(), any(List.class), any(TablePane.class));
        doReturn(pagination).when(activeTableReference).getPagination();

        tableSelectionListener = mock(ChangeListener.class);
        selectedOnlySelectionListener = mock(ListChangeListener.class);

        rows = List.of(
                FXCollections.observableList(List.of("Row1Column1", "Row1Column2")),
                FXCollections.observableList(List.of("Row2Column1", "Row2Column2"))
        );

        selectionModel = mock(TableView.TableViewSelectionModel.class);
        selectedItemProperty = mock(ReadOnlyObjectProperty.class);
        selectedItems = mock(ObservableList.class);

        when(table.getSelectedProperty()).thenReturn(selectedItemProperty);
        when(table.getTableView()).thenReturn(tableView);
        when(table.getTableSelectionListener()).thenReturn(tableSelectionListener);
        when(table.getSelectedOnlySelectionListener()).thenReturn(selectedOnlySelectionListener);
        when(table.getParentComponent()).thenReturn(tablePane);

        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        updateDataTask = spy(new UpdateDataTask(table, rows));
    }

    @Test
    public void runTableUpdated() throws InterruptedException {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setMaxRowsPerPage(42);

        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

        final TableFilter.Builder filterBuilder = mock(TableFilter.Builder.class);
        final TableFilter<ObservableList<String>> filter = mock(TableFilter.class);

        final FilteredList<ObservableList<String>> filteredList = mock(FilteredList.class);
        final ObjectProperty<Predicate<? super ObservableList<String>>> filterPredicateProperty
                = mock(ObjectProperty.class);

        when(filter.getFilteredList()).thenReturn(filteredList);
        when(filteredList.predicateProperty()).thenReturn(filterPredicateProperty);

        try (final MockedStatic<TableFilter> tableFilterMockedStatic
                = Mockito.mockStatic(TableFilter.class)) {
            tableFilterMockedStatic.when(() -> TableFilter.forTableView(tableView))
                    .thenReturn(filterBuilder);

            when(filterBuilder.lazy(true)).thenReturn(filterBuilder);
            when(filterBuilder.apply()).thenReturn(filter);

            updateDataTask.run();
        }

        // Wait for any JavaFX work to complete
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> countDownLatch.countDown());
        countDownLatch.await();

        // Verify listeners removed and the new rows are added to the table
        verify(selectedItemProperty).removeListener(tableSelectionListener);
        verify(selectedItems).removeListener(selectedOnlySelectionListener);

        final SortedList<ObservableList<String>> newRowList
                = new SortedList<>(FXCollections.observableList(rows));

        assertEquals(newRowList, activeTableReference.getSortedRowList());

        verify(tableView).setItems(newRowList);

        // Verify a new filter is created for the table
        final ArgumentCaptor<BiPredicate<String, String>> filterStrategyCaptor = ArgumentCaptor.forClass(BiPredicate.class);
        verify(filter).setSearchStrategy(filterStrategyCaptor.capture());

        assertTrue(filterStrategyCaptor.getValue().test("ABC", "abcdefg"));
        assertFalse(filterStrategyCaptor.getValue().test("DEF", "abcdefg"));

        // Verify the table is updated with changes and listeners re-applied
        verify(activeTableReference).updatePagination(42, newRowList, tablePane);

        verify(selectedItemProperty).addListener(tableSelectionListener);
        verify(selectedItems).addListener(selectedOnlySelectionListener);

        // Verify the count down latch passed into the constructor has counted down
        assertEquals(0, updateDataTask.getUpdateDataLatch().getCount());

        // Extract the filter listener and verify its behaviour
        final ArgumentCaptor<ChangeListener<? super Predicate<? super ObservableList<String>>>> filterListenerCaptor
                = ArgumentCaptor.forClass(ChangeListener.class);
        verify(filterPredicateProperty).addListener(filterListenerCaptor.capture());

        // Clear the mock invocation counts for clarity and simplicity
        clearInvocations(activeTableReference, tablePane);

        final Predicate predicate = mock(Predicate.class);
        when(filteredList.getPredicate()).thenReturn(predicate);

        // Invoke the listener
        filterListenerCaptor.getValue().changed(null, null, null);

        // Wait for any JavaFX work to complete
        final CountDownLatch listenerCountDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> listenerCountDownLatch.countDown());
        listenerCountDownLatch.await();

        verify(activeTableReference).setSortedRowList(new SortedList<>(
                FXCollections.observableArrayList(
                        new FilteredList<>(FXCollections.observableArrayList(rows),
                                predicate)
                )
        ));

        verify(activeTableReference).updatePagination(42, new SortedList<>(
                FXCollections.observableArrayList(
                        new FilteredList<>(FXCollections.observableArrayList(rows),
                                predicate)
                )
        ), tablePane);
    }

    @Test
    public void runThreadInterrupted() throws InterruptedException {
        updateDataTask.setInterrupted(true);
        updateDataTask.run();

        verifyNoInteractions(table);
    }

    @Test
    public void runThreadInterruptedPartWayThrough() throws InterruptedException {
        final TableFilter.Builder filterBuilder = mock(TableFilter.Builder.class);
        final TableFilter<ObservableList<String>> filter = mock(TableFilter.class);

        final FilteredList<ObservableList<String>> filteredList = mock(FilteredList.class);
        final ObjectProperty<Predicate<? super ObservableList<String>>> filterPredicateProperty
                = mock(ObjectProperty.class);

        when(filter.getFilteredList()).thenReturn(filteredList);
        when(filteredList.predicateProperty()).thenAnswer(mock -> {
            // When this method is called also set the state to interrupted
            // before returning the mock.
            updateDataTask.setInterrupted(true);

            return filterPredicateProperty;
        });

        try (final MockedStatic<TableFilter> tableFilterMockedStatic
                = Mockito.mockStatic(TableFilter.class)) {
            tableFilterMockedStatic.when(() -> TableFilter.forTableView(tableView))
                    .thenReturn(filterBuilder);

            when(filterBuilder.lazy(true)).thenReturn(filterBuilder);
            when(filterBuilder.apply()).thenReturn(filter);

            updateDataTask.run();

            // Because the task is now 'interrupted' it should not run the update
            // pagination
            verify(activeTableReference, times(0))
                    .updatePagination(anyInt(), any(List.class), any(TablePane.class));
        }
    }
}
