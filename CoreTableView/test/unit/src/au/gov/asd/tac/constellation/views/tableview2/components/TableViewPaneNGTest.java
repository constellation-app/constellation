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
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewPaneNGTest {
    private TableViewTopComponent tableTopComponent;
    
    private TableViewPane tablePane;
    
    public TableViewPaneNGTest() {
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
        
        tablePane = spy(new TableViewPane(tableTopComponent));
    }
    
    @Test
    public void init() throws InterruptedException {
        assertNotNull(tablePane.getTable());
        
        assertNotNull(tablePane.getTableSelectionListener());
        assertNotNull(tablePane.getSelectedOnlySelectionListener());
        assertNotNull(tablePane.getTableComparatorListener());
        assertNotNull(tablePane.getTableSortTypeListener());
        
        assertNotNull(tablePane.getTableService());
        assertNotNull(tablePane.getTableService().getPageFactory());
        assertNotNull(tablePane.getTableService().getPagination());
        
        assertNotNull(tablePane.getLeft());
        assertTrue(tablePane.getLeft() instanceof ToolBar);
        
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();
        
        verify(tablePane).setCenter(tablePane.getTableService().getPagination());
    }
    
    @Test
    public void updateTable() throws InterruptedException, ExecutionException, TimeoutException {
        final Graph graph = mock(Graph.class);
        final TableViewState tableViewState = new TableViewState();
        
        final Table table = mock(Table.class);
        
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);
        final ChangeListener<Comparator<ObservableList<String>>> tableComparatorListener = mock(ChangeListener.class);
        final ChangeListener<TableColumn.SortType> tableSortTypeListener = mock(ChangeListener.class);
        final ProgressBar progressBar = mock(ProgressBar.class);
        final TableToolbar tableToolbar = mock(TableToolbar.class);
        
        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getTableSelectionListener()).thenReturn(tableSelectionListener);
        when(tablePane.getSelectedOnlySelectionListener()).thenReturn(selectedOnlySelectionListener);
        doReturn(tableComparatorListener).when(tablePane).getTableComparatorListener();
        doReturn(tableSortTypeListener).when(tablePane).getTableSortTypeListener();
        when(tablePane.getProgressBar()).thenReturn(progressBar);
        when(tablePane.getTableToolbar()).thenReturn(tableToolbar);
        
        tablePane.updateTable(graph, tableViewState);
        
        try {
            tablePane.getScheduledFuture().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            fail("The update thread did not finish as expected within the allowed time. "
                    + "Something went wrong");
        }
        
        verify(tableToolbar).updateToolbar(tableViewState);
        
        verify(table).updateColumns(graph, tableViewState, tableSelectionListener, selectedOnlySelectionListener);
        verify(table).updateData(graph, tableViewState, progressBar, tableSelectionListener, selectedOnlySelectionListener);
        verify(table).updateSelection(graph, tableViewState, tableSelectionListener, selectedOnlySelectionListener);
        
        // The future finished but maybe not the JavaFX thread
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();
        
        verify(table).updateSortOrder();
    }
    
    @Test
    public void updateTableGraphIsNull() throws InterruptedException, ExecutionException, TimeoutException {
        final Table table = mock(Table.class);
        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        
        final TableViewState tableViewState = new TableViewState();
        
        final TableToolbar tableToolbar = mock(TableToolbar.class);
        
        final ObservableList<TableColumn<ObservableList<String>, String>> columns
                = FXCollections.observableList(List.of(
                        mock(TableColumn.class),
                        mock(TableColumn.class)
                ));
        
        when(tablePane.getTable()).thenReturn(table);
        when(table.getTableView()).thenReturn(tableView);
        doReturn(columns).when(tableView).getColumns();
        when(tablePane.getTableToolbar()).thenReturn(tableToolbar);
        
        tablePane.updateTable(null, tableViewState);
        
        try {
            tablePane.getScheduledFuture().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            fail("The update thread did not finish as expected within the allowed time. "
                    + "Something went wrong");
        }
        
        verify(tableToolbar).updateToolbar(tableViewState);
        
        verify(table, times(0)).updateColumns(any(Graph.class), any(TableViewState.class), any(ChangeListener.class), any(ListChangeListener.class));
        verify(table, times(0)).updateData(any(Graph.class), any(TableViewState.class), any(ProgressBar.class), any(ChangeListener.class), any(ListChangeListener.class));
        verify(table, times(0)).updateSelection(any(Graph.class), any(TableViewState.class), any(ChangeListener.class), any(ListChangeListener.class));
        
        // The future finished but maybe not the JavaFX thread
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();
        
        verify(table, times(0)).updateSortOrder();
        assertTrue(columns.isEmpty());
    }
}
