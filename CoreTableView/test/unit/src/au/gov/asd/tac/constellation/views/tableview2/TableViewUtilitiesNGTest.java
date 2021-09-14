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
package au.gov.asd.tac.constellation.views.tableview2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewUtilitiesNGTest {

    public TableViewUtilitiesNGTest() {
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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
            pluginExecutionStaticMock.when(() -> PluginExecution.withPlugin(any(TableViewUtilities.SelectionToGraphPlugin.class)))
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
    public void exportCSV() throws IOException, InterruptedException, PluginException {
        final TableView<ObservableList<String>> table = mock(TableView.class);
        final Pagination pagination = mock(Pagination.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);

        File tmpFile = null;
        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            tmpFile = File.createTempFile("constellationTest", ".csv");
            final String csv = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

            tableViewUtilsMockedStatic.when(() -> TableViewUtilities.getTableData(table, pagination, true, true))
                    .thenReturn(csv);

            final TableViewUtilities.ExportToCsvFilePlugin plugin
                    = new TableViewUtilities.ExportToCsvFilePlugin(tmpFile, table,
                            pagination, true);
            plugin.execute(null, pluginInteraction, null);

            final String outputtedFile = new String(IOUtils.toByteArray(
                    new FileInputStream(tmpFile)), StandardCharsets.UTF_8);

            assertEquals(csv, outputtedFile);
            assertEquals(plugin.getName(), "Table View: Export to Delimited File");

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    @Test
    public void selectionToGraph() throws InterruptedException, PluginException {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        final Map<ObservableList<String>, Integer> index = new HashMap<>();

        index.put(row1, 1);
        index.put(row2, 2);

        // Two rows. Row 1 is selected
        when(table.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(FXCollections.observableList(List.of(row1)));

        final TableViewUtilities.SelectionToGraphPlugin selectionToGraph
                = new TableViewUtilities.SelectionToGraphPlugin(table, index, GraphElementType.VERTEX);

        selectionToGraph.edit(graph, null, null);

        verify(graph).setBooleanValue(0, 1, true);
        verify(graph).setBooleanValue(0, 2, false);

        assertEquals("Table View: Select on Graph", selectionToGraph.getName());
    }

    @Test
    public void updateStatePlugin() throws InterruptedException, PluginException {
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.META);

        final TableViewUtilities.UpdateStatePlugin updateStatePlugin
                = new TableViewUtilities.UpdateStatePlugin(tableViewState);

        updateStatePlugin.edit(graph, null, null);

        final ArgumentCaptor<TableViewState> captor = ArgumentCaptor.forClass(TableViewState.class);
        verify(graph).setObjectValue(eq(0), eq(0), captor.capture());

        assertEquals(tableViewState, captor.getValue());
        assertNotSame(tableViewState, captor.getValue());

        assertEquals("Table View: Update State", updateStatePlugin.getName());
    }

    @Test
    public void exportToExcelFileSelectedOnly() throws IOException, InterruptedException, PluginException {
        final boolean selectedOnly = true;
        final String sheetName = "My Sheet";

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final Pagination pagination = mock(Pagination.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);
        final Callback<Integer, Node> callback = mock(Callback.class);

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("constellationTest", ".xls");

            when(pagination.getPageFactory()).thenReturn(callback);
            when(callback.call(anyInt())).thenReturn(table);
            when(table.getSelectionModel()).thenReturn(selectionModel);

            when(pagination.getCurrentPageIndex()).thenReturn(42);
            when(pagination.getPageCount()).thenReturn(2);

            final TableColumn<ObservableList<String>, ? extends Object> column1
                    = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, ? extends Object> column2
                    = mock(TableColumn.class);

            when(column1.getText()).thenReturn("COLUMN_1");
            when(column2.getText()).thenReturn("COLUMN_2");

            when(column1.isVisible()).thenReturn(true);
            when(column2.isVisible()).thenReturn(true);

            when(table.getColumns()).thenReturn(FXCollections.observableArrayList(column1, column2));

            // Page 1
            doReturn(FXCollections.observableList(List.of(
                    FXCollections.observableList(List.of("row1Column1", "row1Column2", "row1InvisibleColumn3")))))
                    // Page 2
                    .doReturn(FXCollections.observableList(List.of(
                            FXCollections.observableList(List.of("row2Column1", "row2Column2", "row2InvisibleColumn3")))))
                    .when(selectionModel).getSelectedItems();

            final TableViewUtilities.ExportToExcelFilePlugin exportToExcel
                    = new TableViewUtilities.ExportToExcelFilePlugin(tmpFile, table,
                            pagination, 1, selectedOnly, sheetName);

            exportToExcel.execute(null, pluginInteraction, null);

            // Due to date/times etc. no two files are the same at the byte level
            // So open the saved file, iterating over it, generating a CSV that can
            // be verified.
            final String csvInFile = generateCsvFromExcelFile(tmpFile, sheetName);
            final String expected = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

            assertEquals(expected, csvInFile);

            // Verifies that it resets to the current page
            verify(callback).call(42);

            assertEquals("Table View: Export to Excel File", exportToExcel.getName());

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    @Test
    public void exportToExcelFileAllRows() throws IOException, InterruptedException, PluginException {
        final boolean selectedOnly = false;
        final String sheetName = "My Sheet";

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final Pagination pagination = mock(Pagination.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);
        final Callback<Integer, Node> callback = mock(Callback.class);

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("constellationTest", ".xls");

            when(pagination.getPageFactory()).thenReturn(callback);
            when(callback.call(anyInt())).thenReturn(table);

            when(pagination.getCurrentPageIndex()).thenReturn(42);
            when(pagination.getPageCount()).thenReturn(2);

            final TableColumn<ObservableList<String>, ? extends Object> column1
                    = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, ? extends Object> column2
                    = mock(TableColumn.class);

            when(column1.getText()).thenReturn("COLUMN_1");
            when(column2.getText()).thenReturn("COLUMN_2");

            when(column1.isVisible()).thenReturn(true);
            when(column2.isVisible()).thenReturn(true);

            when(table.getColumns())
                    .thenReturn(FXCollections.observableArrayList(column1, column2));

            // Page 1
            doReturn(FXCollections.observableList(List.of(
                    FXCollections.observableList(List.of("row1Column1", "row1Column2", "row1InvisibleColumn3")))))
                    // Page 2
                    .doReturn(FXCollections.observableList(List.of(
                            FXCollections.observableList(List.of("row2Column1", "row2Column2", "row2InvisibleColumn3")))))
                    .when(table).getItems();

            final TableViewUtilities.ExportToExcelFilePlugin exportToExcel
                    = new TableViewUtilities.ExportToExcelFilePlugin(tmpFile, table,
                            pagination, 1, selectedOnly, sheetName);

            exportToExcel.execute(null, pluginInteraction, null);

            // Due to date/times etc. no two files are the same at the byte level
            // So open the saved file, iterating over it, generating a CSV that can
            // be verified.
            final String csvInFile = generateCsvFromExcelFile(tmpFile, sheetName);
            final String expected = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

            assertEquals(expected, csvInFile);

            // Verifies that it resets to the current page
            verify(callback).call(42);

            assertEquals("Table View: Export to Excel File", exportToExcel.getName());

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    private String generateCsvFromExcelFile(File file, final String sheetName) throws IOException {
        final Workbook wb = WorkbookFactory.create(new FileInputStream(file));
        final Sheet sheet = wb.getSheet(sheetName);

        final StringBuilder output = new StringBuilder();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            final Row row = sheet.getRow(i);
            output.append(
                    IntStream.range(0, row.getLastCellNum())
                            .mapToObj(cellId -> row.getCell(cellId).toString())
                            .collect(Collectors.joining(","))
            );
            output.append("\n");
        }

        return output.toString();
    }
}
