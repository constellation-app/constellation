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
package au.gov.asd.tac.constellation.views.tableview.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.tableview.plugins.SelectionToGraphPlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewUtilitiesNGTest {
    private static final Logger LOGGER = Logger.getLogger(TableViewUtilitiesNGTest.class.getName());

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

    @Test
    public void copyToClipboard() {
        try (MockedStatic<Clipboard> clipBoardStaticMock = Mockito.mockStatic(Clipboard.class)) {
            final Clipboard clipboard = mock(Clipboard.class);
            clipBoardStaticMock.when(Clipboard::getSystemClipboard).thenReturn(clipboard);

            final String textToCopy = "some random text";

            TableViewUtilities.copyToClipboard(textToCopy);

            final ArgumentCaptor<ClipboardContent> captor = ArgumentCaptor.forClass(ClipboardContent.class);

            verify(clipboard).setContent(captor.capture());

            assertEquals(textToCopy, captor.getValue().getString());
        }
    }

    @Test
    public void copySelectionToGraph() {
        try (MockedStatic<PluginExecution> pluginExecutionStaticMock = Mockito.mockStatic(PluginExecution.class)) {
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionStaticMock.when(() -> PluginExecution.withPlugin(any(SelectionToGraphPlugin.class)))
                    .thenReturn(pluginExecution);

            final TableView<ObservableList<String>> table = mock(TableView.class);
            final Map<ObservableList<String>, Integer> index = mock(Map.class);
            final Graph graph = mock(Graph.class);

            TableViewUtilities.copySelectionToGraph(table, index, GraphElementType.META, graph);

            verify(pluginExecution).executeLater(graph);
        }
    }

    @Test
    public void getTableDataSelectedOnly() {
        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final Pagination pagination = mock(Pagination.class);

        final Callback<Integer, Node> callback = mock(Callback.class);

        when(pagination.getPageFactory()).thenReturn(callback);
        when(callback.call(anyInt())).thenReturn(table);
        when(table.getSelectionModel()).thenReturn(selectionModel);

        when(pagination.getCurrentPageIndex()).thenReturn(42);
        when(pagination.getPageCount()).thenReturn(2);

        final TableColumn<ObservableList<String>, ? extends Object> column1 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, ? extends Object> column2 = mock(TableColumn.class);

        when(column1.getText()).thenReturn("COLUMN_1");
        when(column2.getText()).thenReturn("COLUMN_2");

        when(table.getVisibleLeafColumns()).thenReturn(FXCollections.observableArrayList(column1, column2));
        when(table.getColumns()).thenReturn(FXCollections.observableArrayList(column1, column2));

        // Page 1
        doReturn(FXCollections.observableList(List.of(
                FXCollections.observableList(List.of("row1Column1", "row1Column2", "row1InvisibleColumn3")))))
                // Page 2
                .doReturn(FXCollections.observableList(List.of(
                        FXCollections.observableList(List.of("row2Column1", "row2Column2", "row2InvisibleColumn3")))))
                .when(selectionModel).getSelectedItems();

        final String tableData = TableViewUtilities.getTableData(table, pagination, true, true);

        final String expected = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

        assertEquals(expected, tableData);

        // Verifies that it resets to the current page
        verify(callback).call(42);
    }

    @Test
    public void getTableDataAllRows() {
        final TableView<ObservableList<String>> table = mock(TableView.class);
        final Pagination pagination = mock(Pagination.class);

        final Callback<Integer, Node> callback = mock(Callback.class);

        when(pagination.getPageFactory()).thenReturn(callback);
        when(callback.call(anyInt())).thenReturn(table);

        when(pagination.getCurrentPageIndex()).thenReturn(42);
        when(pagination.getPageCount()).thenReturn(2);

        final TableColumn<ObservableList<String>, ? extends Object> column1 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, ? extends Object> column2 = mock(TableColumn.class);

        when(column1.getText()).thenReturn("COLUMN_1");
        when(column2.getText()).thenReturn("COLUMN_2");

        when(table.getVisibleLeafColumns()).thenReturn(FXCollections.observableArrayList(column1, column2));
        when(table.getColumns()).thenReturn(FXCollections.observableArrayList(column1, column2));

        // Page 1
        doReturn(FXCollections.observableList(List.of(
                FXCollections.observableList(List.of("row1Column1", "row1Column2")))))
                // Page 2
                .doReturn(FXCollections.observableList(List.of(
                        FXCollections.observableList(List.of("row2Column1", "row2Column2")))))
                .when(table).getItems();

        final String tableData = TableViewUtilities.getTableData(table, pagination, false, false);

        final String expected = "row1Column1,row1Column2\nrow2Column1,row2Column2\n";

        assertEquals(expected, tableData);

        // Verifies that it resets to the current page
        verify(callback).call(42);
    }

    @Test
    public void getSelectedIdsForVertecies() {
        final Graph graph = mock(Graph.class);
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

        assertEquals(List.of(100, 102), TableViewUtilities.getSelectedIds(graph, tableViewState));

        verify(readableGraph).release();
    }

    @Test
    public void getSelectedIdsForTransactions() {
        final Graph graph = mock(Graph.class);
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

        assertEquals(List.of(100, 102), TableViewUtilities.getSelectedIds(graph, tableViewState));

        verify(readableGraph).release();
    }
}
