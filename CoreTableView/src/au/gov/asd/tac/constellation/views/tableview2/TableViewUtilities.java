/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Table View Utilities.
 *
 * @author cygnus_x-1
 */
public class TableViewUtilities {

    private static final String EXPORT_TO_DELIMITED_FILE_PLUGIN = "Table View: Export to Delimited File";
    private static final String EXPORT_TO_EXCEL_FILE_PLUGIN = "Table View: Export to Excel File";
    public static final String SELECT_ON_GRAPH_PLUGIN = "Table View: Select on Graph";
    private static final String UPDATE_STATE_PLUGIN = "Table View: Update State";

    /**
     * Retrieve data from the given table as comma-separated values.
     *
     * @param table the table to retrieve data from.
     * @param includeHeader if true, the table headers will be included in the
     * output.
     * @param selectedOnly if true, only the data from selected rows in the
     * table will be included in the output.
     * @return a String of comma-separated values representing the table.
     */
    public static String getTableData(final TableView<ObservableList<String>> table,
            final boolean includeHeader, final boolean selectedOnly) {
        final List<Integer> visibleIndices = table.getVisibleLeafColumns().stream()
                .map(column -> table.getColumns().indexOf(column))
                .collect(Collectors.toList());

        final StringBuilder data = new StringBuilder();
        if (includeHeader) {
            data.append(visibleIndices.stream()
                    .filter(Objects::nonNull)
                    .map(index -> table.getColumns().get(index).getText())
                    .reduce((header1, header2) -> header1 + SeparatorConstants.COMMA + header2)
                    .get());
            data.append(SeparatorConstants.NEWLINE);
        }

        if (selectedOnly) {
            table.getSelectionModel().getSelectedItems().forEach(selectedItem -> {
                data.append(visibleIndices.stream()
                        .filter(Objects::nonNull)
                        .map(index -> selectedItem.get(index))
                        .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                        .get());
                data.append(SeparatorConstants.NEWLINE);
            });
        } else {
            table.getItems().forEach(item -> {
                data.append(visibleIndices.stream()
                        .filter(Objects::nonNull)
                        .map(index -> item.get(index))
                        .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                        .get());
                data.append(SeparatorConstants.NEWLINE);
            });
        }

        return data.toString();
    }

    /**
     * Copy the given text to the system clipboard.
     *
     * @param text the text to copy.
     */
    public static void copyToClipboard(final String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * Write data from the given table to a CSV file.
     *
     * @param table the table to retrieve data from.
     * @param selectedOnly if true, only the data from selected rows in the
     * table will be included in the output file.
     */
    public static void exportToCsv(final TableView<ObservableList<String>> table, final boolean selectedOnly) {
        Platform.runLater(() -> {
            final FileChooser fileChooser = new FileChooser();
            final ExtensionFilter csvFilter = new ExtensionFilter("CSV files", "*.csv");
            fileChooser.getExtensionFilters().add(csvFilter);
            final File csvFile = fileChooser.showSaveDialog(null);
            if (csvFile != null) {
                PluginExecution.withPlugin(new ExportToCsvFilePlugin(csvFile, table, selectedOnly)).executeLater(null);
            }
        });
    }

    /**
     * Write data from the given table to a XLSX file.
     *
     * @param table the table to retrieve data from.
     * @param selectedOnly if true, only the data from selected rows in the
     * table will be included in the output file.
     * @param sheetName the name of the workbook sheet in the output file.
     */
    public static void exportToExcel(final TableView<ObservableList<String>> table, final boolean selectedOnly, final String sheetName) {
        Platform.runLater(() -> {
            final FileChooser fileChooser = new FileChooser();
            final ExtensionFilter excelFilter = new ExtensionFilter("Excel files", "*.xlsx");
            fileChooser.getExtensionFilters().add(excelFilter);
            final File excelFile = fileChooser.showSaveDialog(null);
            if (excelFile != null) {
                PluginExecution.withPlugin(new ExportToExcelFilePlugin(excelFile, table, selectedOnly, sheetName)).executeLater(null);
            }
        });
    }

    /**
     * Copy selection information from a table to a graph.
     *
     * @param table the table to retrieve data from.
     * @param index the index of the selection.
     * @param elementType the element type of the selection.
     * @param graph the graph to copy to.
     */
    public static void copySelectionToGraph(final TableView<ObservableList<String>> table,
            final Map<ObservableList<String>, Integer> index, final GraphElementType elementType, final Graph graph) {
        PluginExecution.withPlugin(new SelectionToGraphPlugin(table, index, elementType)).executeLater(graph);
    }

    private static class ExportToCsvFilePlugin extends SimplePlugin {

        private final File file;
        private final TableView<ObservableList<String>> table;
        private final boolean selectedOnly;

        public ExportToCsvFilePlugin(final File file,
                final TableView<ObservableList<String>> table,
                final boolean selectedOnly) {
            this.file = file;
            this.table = table;
            this.selectedOnly = selectedOnly;
        }

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            try {
                final String csvData = getTableData(table, true, selectedOnly);
                try (final FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(csvData);
                }
            } catch (IOException ex) {
                throw new PluginException(PluginNotificationLevel.ERROR, ex);
            }
        }

        @Override
        public String getName() {
            return EXPORT_TO_DELIMITED_FILE_PLUGIN;
        }
    }

    private static class ExportToExcelFilePlugin extends SimplePlugin {

        private final File file;
        private final TableView<ObservableList<String>> table;
        private final boolean selectedOnly;
        private final String sheetName;

        public ExportToExcelFilePlugin(final File file,
                final TableView<ObservableList<String>> table,
                final boolean selectedOnly, final String sheetName) {
            this.file = file;
            this.table = table;
            this.selectedOnly = selectedOnly;
            this.sheetName = sheetName;
        }

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            try (final SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSFWorkbook.DEFAULT_WINDOW_SIZE)) {
                final Sheet sheet = workbook.createSheet(sheetName);

                final List<Integer> visibleIndices = table.getColumns().stream()
                        .filter(column -> column.isVisible())
                        .map(column -> table.getColumns().indexOf(column))
                        .collect(Collectors.toList());

                final Row headerRow = sheet.createRow(0);
                visibleIndices.forEach(index -> {
                    final TableColumn<ObservableList<String>, ?> column = table.getColumns().get(index);
                    final Cell headerCell = headerRow.createCell(visibleIndices.indexOf(index));
                    headerCell.setCellValue(column.getText());
                });

                if (selectedOnly) {
                    // get a copy of the table data so that users are continue working
                    final List<ObservableList<String>> data = table.getSelectionModel().getSelectedItems();
                    writeRecords(sheet, visibleIndices, data);
                } else {
                    // get a copy of the table data so that users are continue working
                    final List<ObservableList<String>> data = table.getItems();
                    writeRecords(sheet, visibleIndices, data);
                }

                workbook.write(new FileOutputStream(file));
                workbook.dispose();
            } catch (IOException ex) {
                throw new PluginException(PluginNotificationLevel.ERROR, ex);
            }
        }

        @Override
        public String getName() {
            return EXPORT_TO_EXCEL_FILE_PLUGIN;
        }
    }

    /**
     * Write the table data to the Excel sheet, excluding the heading
     *
     * @param sheet The Excel sheet
     * @param visibleIndices The visible columns
     * @param data The table data
     */
    private static void writeRecords(final Sheet sheet, final List<Integer> visibleIndices, final List<ObservableList<String>> data) {
        final int[] rowIndex = new int[1];
        rowIndex[0] = 1; // setting the list's index to 1 to skip the heading
        data.forEach(item -> {
            final Row itemRow = sheet.createRow(rowIndex[0]++);
            visibleIndices.forEach(index -> {
                final Cell itemCell = itemRow.createCell(visibleIndices.indexOf(index));
                itemCell.setCellValue(item.get(index));
            });
        });
    }

    /**
     * Copy the table selection to the graph.
     * <p>
     * We use an instance of this plugin to modify the graph. When we get a
     * graph change event, we can check to see if it was this plugin that did
     * the changes. If it was, we don't have to change the table.
     */
    private static class SelectionToGraphPlugin extends SimpleEditPlugin {

        private final TableView<ObservableList<String>> table;
        private final Map<ObservableList<String>, Integer> index;
        private final GraphElementType elementType;

        public SelectionToGraphPlugin(final TableView<ObservableList<String>> table,
                final Map<ObservableList<String>, Integer> index, final GraphElementType elementType) {
            this.table = table;
            this.index = index;
            this.elementType = elementType;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> selectedElements = table.getSelectionModel().getSelectedItems().stream()
                    .map(selectedItem -> index.get(selectedItem)).collect(Collectors.toSet());
            final boolean isVertex = elementType == GraphElementType.VERTEX;
            final int selectedAttributeId = isVertex ? VisualConcept.VertexAttribute.SELECTED.ensure(graph) : VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
            final int elementCount = isVertex ? graph.getVertexCount() : graph.getTransactionCount();
            for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                final int elementId = isVertex ? graph.getVertex(elementPosition) : graph.getTransaction(elementPosition);
                graph.setBooleanValue(selectedAttributeId, elementId, selectedElements.contains(elementId));
            }
        }

        @Override
        public String getName() {
            return SELECT_ON_GRAPH_PLUGIN;
        }
    }

    /**
     * Write the given TableViewState to the graph.
     */
    public static class UpdateStatePlugin extends SimpleEditPlugin {

        private final TableViewState tableViewState;

        public UpdateStatePlugin(final TableViewState tableViewState) {
            this.tableViewState = tableViewState;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int tableViewStateAttributeId = TableViewConcept.MetaAttribute.TABLE_VIEW_STATE.ensure(graph);
            final TableViewState state = new TableViewState(tableViewState);
            graph.setObjectValue(tableViewStateAttributeId, 0, state);
        }

        @Override
        protected boolean isSignificant() {
            return true;
        }

        @Override
        public String getName() {
            return UPDATE_STATE_PLUGIN;
        }
    }
}
