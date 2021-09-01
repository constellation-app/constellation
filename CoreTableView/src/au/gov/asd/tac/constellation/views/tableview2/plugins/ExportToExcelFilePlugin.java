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
package au.gov.asd.tac.constellation.views.tableview2.plugins;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
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
 *
 * @author formalhaunt
 */
@PluginInfo(pluginType = PluginType.EXPORT, tags = {"EXPORT"})
public class ExportToExcelFilePlugin extends SimplePlugin {
    private static final Logger LOGGER = Logger.getLogger(ExportToExcelFilePlugin.class.getName());
    
    private static final String EXPORT_TO_EXCEL_FILE_PLUGIN = "Table View: Export to Excel File";
    
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
    public void execute(final PluginGraphs graphs,
                        final PluginInteraction interaction,
                        final PluginParameters parameters) throws InterruptedException, PluginException {
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
                            // get a copy of the table data so that users can continue working
                            final List<ObservableList<String>> data = table.getSelectionModel()
                                    .getSelectedItems();
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
                            // get a copy of the table data so that users can continue working
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
    
    /**
     * Write the table data to the Excel sheet, excluding the column headers.
     *
     * @param sheet the Excel sheet to write to
     * @param visibleIndices the visible columns
     * @param data the table rows to write
     */
    private static void writeRecords(final Sheet sheet,
                                     final List<Integer> visibleIndices,
                                     final List<ObservableList<String>> data,
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
}
