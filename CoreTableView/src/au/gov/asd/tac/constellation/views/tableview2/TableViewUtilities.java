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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.swing.filechooser.FileFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Table View Utilities.
 *
 * @author cygnus_x-1
 */
public class TableViewUtilities {

    private static final Logger LOGGER = Logger.getLogger(TableViewUtilities.class.getName());

    private static final String EXPORT_TO_DELIMITED_FILE_PLUGIN = "Table View: Export to Delimited File";
    private static final String EXPORT_TO_EXCEL_FILE_PLUGIN = "Table View: Export to Excel File";
    private static final String UPDATE_STATE_PLUGIN = "Table View: Update State";
    private static final String EXPORT_CSV = "Export To CSV";
    private static final String EXPORT_XLSX = "Export To XLSX";
    private static final String CSV_EXT = ".csv";
    private static final String XLSX_EXT = ".xlsx";

    public static final String SELECT_ON_GRAPH_PLUGIN = "Table View: Select on Graph";
    
    // TODO find a better place for this. I don't like it being public
    public static final Object TABLE_LOCK = new Object();
    
    private TableViewUtilities() {
    }

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
    public static String getTableData(final TableView<ObservableList<String>> table, final Pagination pagination,
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

        // get the current page index so we can go back to it afterwards
        final int currentPage = pagination.getCurrentPageIndex();
        if (selectedOnly) {
            for (int i = 0; i < pagination.getPageCount(); i++) {
                final TableView<ObservableList<String>> page = (TableView<ObservableList<String>>) pagination.getPageFactory().call(i);
                page.getSelectionModel().getSelectedItems().forEach(selectedItem -> {
                    data.append(visibleIndices.stream()
                            .filter(Objects::nonNull)
                            .map(index -> selectedItem.get(index))
                            .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                            .get());
                    data.append(SeparatorConstants.NEWLINE);
                });
            }
        } else {
            for (int i = 0; i < pagination.getPageCount(); i++) {
                final TableView<ObservableList<String>> page = (TableView<ObservableList<String>>) pagination.getPageFactory().call(i);
                page.getItems().forEach(item -> {
                    data.append(visibleIndices.stream()
                            .filter(Objects::nonNull)
                            .map(index -> item.get(index))
                            .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                            .get());
                    data.append(SeparatorConstants.NEWLINE);
                });
            }
        }
        // Call the page factory function once more to go back to the original page index
        pagination.getPageFactory().call(currentPage);

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
    public static void exportToCsv(final TableView<ObservableList<String>> table, final Pagination pagination,
            final boolean selectedOnly) {
        final FileChooserBuilder fChooser = new FileChooserBuilder(EXPORT_CSV)
                .setTitle(EXPORT_CSV)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName();
                        if (pathName.isFile() && name.toLowerCase().endsWith(CSV_EXT)) {
                            return true;
                        }
                        return name.endsWith(CSV_EXT);
                    }

                    @Override
                    public String getDescription() {
                        return "CSV files (*" + CSV_EXT + ")";
                    }
                });

        final File fileName = fChooser.showSaveDialog();

        if (fileName != null && fileName.getAbsolutePath() != null) {
            final String filePath = fileName.getAbsolutePath().toLowerCase().endsWith(CSV_EXT)
                    ? fileName.getAbsolutePath()
                    : fileName.getAbsolutePath() + CSV_EXT;

            final File file = new File(filePath);
            try {
                PluginExecution.withPlugin(new ExportToCsvFilePlugin(file, table, pagination, selectedOnly)).executeNow((Graph) null);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                Thread.currentThread().interrupt();
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Write data from the given table to a XLSX file.
     *
     * @param table the table to retrieve data from.
     * @param selectedOnly if true, only the data from selected rows in the
     * table will be included in the output file.
     * @param sheetName the name of the workbook sheet in the output file.
     */
    public static void exportToExcel(final TableView<ObservableList<String>> table, final Pagination pagination,
            final int rowsPerPage, final boolean selectedOnly, final String sheetName) {
        final FileChooserBuilder fChooser = new FileChooserBuilder(EXPORT_XLSX)
                .setTitle(EXPORT_XLSX)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName();
                        if (pathName.isFile() && name.toLowerCase().endsWith(XLSX_EXT)) {
                            return true;
                        }
                        return name.endsWith(XLSX_EXT);
                    }

                    @Override
                    public String getDescription() {
                        return "Excel files (*" + XLSX_EXT + ")";
                    }
                });

        final File fileName = fChooser.showSaveDialog();

        if (fileName != null && fileName.getAbsolutePath() != null) {
            final String filePath = fileName.getAbsolutePath().toLowerCase().endsWith(XLSX_EXT)
                    ? fileName.getAbsolutePath()
                    : fileName.getAbsolutePath() + XLSX_EXT;

            final File excelFile = new File(filePath);
            try {
                PluginExecution.withPlugin(new ExportToExcelFilePlugin(excelFile, table, pagination, rowsPerPage, selectedOnly, sheetName)).executeNow((Graph) null);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                Thread.currentThread().interrupt();
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
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

    /**
     * Plugin to export to CSV file.
     */
    @PluginInfo(pluginType = PluginType.EXPORT, tags = {"EXPORT"})
    protected static class ExportToCsvFilePlugin extends SimplePlugin {

        private final File file;
        private final TableView<ObservableList<String>> table;
        private final Pagination pagination;
        private final boolean selectedOnly;

        public ExportToCsvFilePlugin(final File file, final TableView<ObservableList<String>> table, final Pagination pagination,
                final boolean selectedOnly) {
            this.file = file;
            this.table = table;
            this.pagination = pagination;
            this.selectedOnly = selectedOnly;
        }

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final String csvData = getTableData(table, pagination, true, selectedOnly);
            final Thread outputThread = new Thread("Export to CSV File: Writing File") {
                @Override
                public void run() {
                    try (final FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(csvData);
                    } catch (final IOException ex) {
                        interaction.notify(PluginNotificationLevel.ERROR, ex.getLocalizedMessage());
                    }
                }
            };
            outputThread.start();
            outputThread.join();
        }

        @Override
        public String getName() {
            return EXPORT_TO_DELIMITED_FILE_PLUGIN;
        }
    }

    /**
     * Plugin to export to Excel file.
     */
    @PluginInfo(pluginType = PluginType.EXPORT, tags = {"EXPORT"})
    protected static class ExportToExcelFilePlugin extends SimplePlugin {

        private final File file;
        private final TableView<ObservableList<String>> table;
        private final Pagination pagination;
        private final int rowsPerPage;
        private final boolean selectedOnly;
        private final String sheetName;

        public ExportToExcelFilePlugin(final File file, final TableView<ObservableList<String>> table, final Pagination pagination,
                final int rowsPerPage, final boolean selectedOnly, final String sheetName) {
            this.file = file;
            this.table = table;
            this.pagination = pagination;
            this.rowsPerPage = rowsPerPage;
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

                final int currentPage = pagination.getCurrentPageIndex();
                if (selectedOnly) {
                    for (int i = 0; i < pagination.getPageCount(); i++) {
                        pagination.getPageFactory().call(i);
                        final int startIndex = rowsPerPage * i + 1; // + 1 to skip the header
                        final Thread writeSheetThread = new Thread("Export to Excel File: Writing Sheet") {
                            @Override
                            public void run() {
                                // get a copy of the table data so that users are continue working
                                final List<ObservableList<String>> data = table.getSelectionModel().getSelectedItems();
                                writeRecords(sheet, visibleIndices, data, startIndex);
                            }
                        };
                        writeSheetThread.start();
                        writeSheetThread.join();
                    }
                } else {
                    for (int i = 0; i < pagination.getPageCount(); i++) {
                        pagination.getPageFactory().call(i);
                        final int startIndex = rowsPerPage * i + 1; // + 1 to skip the header
                        final Thread writeSheetThread = new Thread("Export to Excel File: Writing Sheet") {
                            @Override
                            public void run() {
                                // get a copy of the table data so that users are continue working
                                final List<ObservableList<String>> data = table.getItems();
                                writeRecords(sheet, visibleIndices, data, startIndex);
                            }
                        };
                        writeSheetThread.start();
                        writeSheetThread.join();
                    }
                }
                // Call the page factory function once more to go back to the original page index
                pagination.getPageFactory().call(currentPage);

                final Thread outputThread = new Thread("Export to Excel File: Writing File") {
                    @Override
                    public void run() {
                        try (final FileOutputStream fileStream = new FileOutputStream(file)) {
                            workbook.write(fileStream);
                            LOGGER.log(Level.INFO, "Table View data written to Excel file");
                        } catch (final IOException ex) {
                            interaction.notify(PluginNotificationLevel.ERROR, ex.getLocalizedMessage());
                        }
                        workbook.dispose();
                    }
                };
                outputThread.start();
                outputThread.join();
            } catch (final IOException ex) {
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
    private static void writeRecords(final Sheet sheet, final List<Integer> visibleIndices, final List<ObservableList<String>> data,
            final int startIndex) {
        final int[] rowIndex = new int[1];
        rowIndex[0] = startIndex;
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
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {"SELECT"})
    protected static class SelectionToGraphPlugin extends SimpleEditPlugin {

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
            final Set<Integer> elements = table.getItems().stream()
                    .map(item -> index.get(item)).collect(Collectors.toSet());
            final Set<Integer> selectedElements = table.getSelectionModel().getSelectedItems().stream()
                    .map(selectedItem -> index.get(selectedItem)).collect(Collectors.toSet());
            final boolean isVertex = elementType == GraphElementType.VERTEX;
            final int selectedAttributeId = isVertex ? VisualConcept.VertexAttribute.SELECTED.ensure(graph) : VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
            for (final Integer element : elements) {
                graph.setBooleanValue(selectedAttributeId, element, selectedElements.contains(element));
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
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {"LOW LEVEL"})
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
        
        public TableViewState getTableViewState() {
            return tableViewState;
        }
    }
}
