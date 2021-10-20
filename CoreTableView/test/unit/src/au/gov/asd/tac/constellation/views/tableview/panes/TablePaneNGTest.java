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
package au.gov.asd.tac.constellation.views.tableview.panes;

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.components.ProgressBar;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.components.TableToolbar;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
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
public class TablePaneNGTest {
    private static final Logger LOGGER = Logger.getLogger(TablePaneNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;

    private TablePane tablePane;

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
        when(tableViewTopComponent.getExecutorService())
                .thenReturn(Executors.newSingleThreadExecutor());

        tablePane = spy(new TablePane(tableViewTopComponent));
    }

    @Test
    public void init() throws InterruptedException {
        assertNotNull(tablePane.getTable());

        assertNotNull(tablePane.getActiveTableReference());
        assertNotNull(tablePane.getActiveTableReference().getPageFactory());
        assertNotNull(tablePane.getActiveTableReference().getPagination());

        assertNotNull(tablePane.getLeft());
        assertTrue(tablePane.getLeft() instanceof ToolBar);

//        TODO This is randomly failing for some reason. Something is consuming
//             the pagination and setting it back to null
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        Platform.runLater(() -> latch.countDown());
//        latch.await();
//
//        final Pagination pagination = tablePane.getActiveTableReference().getPagination();
//
//        assertEquals(pagination, tablePane.centerProperty().get());
    }

    @Test
    public void updateTable() {
        final Graph graph = mock(Graph.class);
        final TableViewState tableViewState = new TableViewState();

        final Table table = mock(Table.class);

        final ProgressBar progressBar = mock(ProgressBar.class);
        final TableToolbar tableToolbar = mock(TableToolbar.class);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getProgressBar()).thenReturn(progressBar);
        when(tablePane.getTableToolbar()).thenReturn(tableToolbar);

        tablePane.updateTable(graph, tableViewState);

        try {
            tablePane.getFuture().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            fail("The update thread did not finish as expected within the allowed time. "
                    + "Something went wrong");
        }

        verify(tableToolbar).updateToolbar(tableViewState);

        verify(table).updateColumns(graph, tableViewState);
        verify(table).updateData(graph, tableViewState, progressBar);
        verify(table).updateSelection(graph, tableViewState);

        // The future finished but maybe not the JavaFX thread
        WaitForAsyncUtils.waitForFxEvents();

        verify(table).updateSortOrder();
    }

    @Test
    public void updateTable_interrupt() {
        final Graph graph = mock(Graph.class);
        final TableViewState tableViewState = new TableViewState();

        final Table table = mock(Table.class);

        final ProgressBar progressBar = mock(ProgressBar.class);
        final TableToolbar tableToolbar = mock(TableToolbar.class);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getProgressBar()).thenReturn(progressBar);
        when(tablePane.getTableToolbar()).thenReturn(tableToolbar);

        doAnswer(mockitoInvocation -> {
            System.out.println("Interrupt: " + Thread.currentThread().getName());
            Thread.currentThread().interrupt();
            return null;
        }).when(table).updateColumns(graph, tableViewState);

        tablePane.updateTable(graph, tableViewState);

        try {
            tablePane.getFuture().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            fail("The update thread did not finish as expected within the allowed time. "
                    + "Something went wrong");
        }

        verify(tableToolbar).updateToolbar(tableViewState);

        // Update columns is called
        verify(table).updateColumns(graph, tableViewState);

        // Verify everything after that is not called
        verify(table, times(0)).updateData(graph, tableViewState, progressBar);
        verify(table, times(0)).updateSelection(graph, tableViewState);

        // The future finished but maybe not the JavaFX thread
        WaitForAsyncUtils.waitForFxEvents();

        verify(table, times(0)).updateSortOrder();
    }

    @Test
    public void updateTableGraphIsNull() throws InterruptedException, ExecutionException, TimeoutException {
        final Table table = mock(Table.class);
        final TableView<ObservableList<String>> tableView = mock(TableView.class);

        final TableViewState tableViewState = new TableViewState();

        final TableToolbar tableToolbar = mock(TableToolbar.class);

        final List<TableColumn<ObservableList<String>, String>> tableColumns = new ArrayList<>();
        tableColumns.add(mock(TableColumn.class));
        tableColumns.add(mock(TableColumn.class));

        final ObservableList<TableColumn<ObservableList<String>, String>> columns
                = FXCollections.observableList(tableColumns);

        when(tablePane.getTable()).thenReturn(table);
        when(table.getTableView()).thenReturn(tableView);
        doReturn(columns).when(tableView).getColumns();
        when(tablePane.getTableToolbar()).thenReturn(tableToolbar);

        tablePane.updateTable(null, tableViewState);

        try {
            tablePane.getFuture().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            fail("The update thread did not finish as expected within the allowed time. "
                    + "Something went wrong");
        }

        verify(tableToolbar).updateToolbar(tableViewState);

        verify(table, times(0)).updateColumns(any(Graph.class), any(TableViewState.class));
        verify(table, times(0)).updateData(any(Graph.class), any(TableViewState.class), any(ProgressBar.class));
        verify(table, times(0)).updateSelection(any(Graph.class), any(TableViewState.class));

        // The future finished but maybe not the JavaFX thread
        WaitForAsyncUtils.waitForFxEvents();

        verify(table, times(0)).updateSortOrder();
        assertTrue(columns.isEmpty());
    }
}
