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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview2.plugins.ExportToCsvFilePlugin;
import au.gov.asd.tac.constellation.views.tableview2.plugins.ExportToExcelFilePlugin;
import au.gov.asd.tac.constellation.views.tableview2.plugins.SelectionToGraphPlugin;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Table View Utilities.
 *
 * @author cygnus_x-1
 */
public class TableViewUtilities {

    private static final Logger LOGGER = Logger.getLogger(TableViewUtilities.class.getName());

    private static final String EXPORT_CSV = "Export To CSV";
    private static final String EXPORT_XLSX = "Export To XLSX";
    private static final String CSV_EXT = ".csv";
    private static final String XLSX_EXT = ".xlsx";

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
    public static String getTableData(final TableView<ObservableList<String>> table,
                                      final Pagination pagination,
                                      final boolean includeHeader,
                                      final boolean selectedOnly) {
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
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        
        Clipboard.getSystemClipboard().setContent(content);
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
                PluginExecution.withPlugin(
                        new ExportToCsvFilePlugin(
                                file,
                                table,
                                pagination,
                                selectedOnly
                        )
                ).executeNow((Graph) null);
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
                PluginExecution.withPlugin(
                        new ExportToExcelFilePlugin(
                                excelFile,
                                table,
                                pagination,
                                rowsPerPage,
                                selectedOnly,
                                sheetName
                        )
                ).executeNow((Graph) null);
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
                                            final Map<ObservableList<String>, Integer> index,
                                            final GraphElementType elementType,
                                            final Graph graph) {
        PluginExecution.withPlugin(
                new SelectionToGraphPlugin(
                        table,
                        index,
                        elementType
                )
        ).executeLater(graph);
    }
}
