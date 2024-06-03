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
package au.gov.asd.tac.constellation.views.tableview.plugins;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Plugin that exports the provided table's rows as an Excel spreadsheet.
 *
 * @author formalhaunt
 */
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class ExportToExcelFilePlugin extends SimplePlugin {

    private static final Logger LOGGER = Logger.getLogger(ExportToExcelFilePlugin.class.getName());

    private static final String EXPORT_TO_EXCEL_FILE_PLUGIN = "Table View: Export to Excel File";

    private final File file;
    private final TableView<ObservableList<String>> table;
    private final Pagination pagination;
    private final int rowsPerPage;
    private final boolean selectedOnly;
    private final String sheetName;

    /**
     * Creates a new export table rows to Excel plugin.
     *
     * @param file the file to save the spreadsheet
     * @param table the table to extract the rows from
     * @param pagination the current table pagination
     * @param rowsPerPage the number of rows per page in the table (the
     * pagination)
     * @param selectedOnly true if only selected rows are to be exported, false
     * otherwise
     * @param sheetName the name of the sheet in the Excel spreadsheet
     */
    public ExportToExcelFilePlugin(final File file, final TableView<ObservableList<String>> table, 
            final Pagination pagination, final int rowsPerPage, final boolean selectedOnly, final String sheetName) {
        this.file = file;
        this.table = table;
        this.pagination = pagination;
        this.rowsPerPage = rowsPerPage;
        this.selectedOnly = selectedOnly;
        this.sheetName = sheetName;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException, PluginException {
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSFWorkbook.DEFAULT_WINDOW_SIZE)) {
            final Sheet sheet = workbook.createSheet(sheetName);

            // get the indexes of all visible columns
            final List<Integer> visibleIndices = table.getColumns().stream()
                    .filter(column -> column.isVisible())
                    .map(column -> table.getColumns().indexOf(column))
                    .toList();

            // iterate through the visible columns and print each ones name to the sheet
            final Row headerRow = sheet.createRow(0);
            visibleIndices.forEach(index -> {
                final TableColumn<ObservableList<String>, ?> column = table.getColumns().get(index);
                final Cell headerCell = headerRow.createCell(visibleIndices.indexOf(index));
                headerCell.setCellValue(column.getText());
            });

            // store the current page so we can reset it after the export
            final int currentPage = pagination.getCurrentPageIndex();

            if (selectedOnly) {
                // iterate through all the pages in the table and write the selected rows to the sheet
                for (int i = 0; i < pagination.getPageCount(); i++) {
                    pagination.getPageFactory().call(i);

                    // Calculates the start index in the sheet based on the current table page
                    // Because only selected rows are included this could leave a gap in the sheet
                    // + 1 to skip the header
                    final int startIndex = rowsPerPage * i + 1; // + 1 to skip the header

                    final Thread writeSheetThread = new Thread("Export to Excel File: Writing Sheet") {
                        @Override
                        public void run() {
                            // get a copy of the table data so that users can continue working
                            final List<ObservableList<String>> data = getTable().getSelectionModel()
                                    .getSelectedItems();

                            writeRecords(sheet, visibleIndices, data, startIndex);
                        }
                    };
                    writeSheetThread.start();
                    writeSheetThread.join();
                }
            } else {
                // iterate through all the pages in the table and write their rows to the sheet
                for (int i = 0; i < pagination.getPageCount(); i++) {
                    pagination.getPageFactory().call(i);

                    // Calculates the start index in the sheet based on the current table page
                    // + 1 to skip the header
                    final int startIndex = rowsPerPage * i + 1;

                    final Thread writeSheetThread = new Thread("Export to Excel File: Writing Sheet") {
                        @Override
                        public void run() {
                            // get a copy of the table data so that users can continue working
                            final List<ObservableList<String>> data = getTable().getItems();

                            writeRecords(sheet, visibleIndices, data, startIndex);
                        }
                    };
                    writeSheetThread.start();
                    writeSheetThread.join();
                }
            }

            // call the page factory function once more to go back to the original page index
            pagination.getPageFactory().call(currentPage);

            // The sheet has now been created. Time to write it to the file
            final Thread outputThread = new Thread("Export to Excel File: Writing File") {
                @Override
                public void run() {
                    try (final FileOutputStream fileStream = new FileOutputStream(getFile())) {
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

    /**
     * Gets the file that the exported Excel spreadsheet will be written to.
     *
     * @return the export file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the table being exported.
     *
     * @return the table
     */
    public TableView<ObservableList<String>> getTable() {
        return table;
    }

    /**
     * Gets the current pagination associated with the table being exported.
     *
     * @return the current pagination
     */
    public Pagination getPagination() {
        return pagination;
    }

    /**
     * Gets the number of rows per page in the table.
     *
     * @return the number of rows per page in the table
     */
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    /**
     * Gets the flag that determines if only selected rows are exported.
     *
     * @return true if only selected rows should be exported, false otherwise
     */
    public boolean isSelectedOnly() {
        return selectedOnly;
    }

    /**
     * Gets the name of the sheet in the exported excel file.
     *
     * @return the sheet name
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * Write the table data to the Excel sheet, excluding the column headers.
     *
     * @param sheet the Excel sheet to write to
     * @param visibleIndices the visible columns
     * @param data the table rows to write
     * @param startIndex the current index in the sheet that can be written to
     */
    private static void writeRecords(final Sheet sheet, final List<Integer> visibleIndices,
            final List<ObservableList<String>> data, final int startIndex) {
        final AtomicInteger rowIndex = new AtomicInteger(startIndex);
        data.forEach(item -> {
            final Row itemRow = sheet.createRow(rowIndex.getAndIncrement());

            visibleIndices.forEach(index -> {
                final Cell itemCell = itemRow.createCell(visibleIndices.indexOf(index));
                itemCell.setCellValue(item.get(index));
            });
        });
    }
}
