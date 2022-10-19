/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.export;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Plugin that exports the provided analytic view results table's rows as an Excel spreadsheet.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class AnalyticExportToExcelFilePlugin extends SimplePlugin {

    private static final Logger LOGGER = Logger.getLogger(AnalyticExportToExcelFilePlugin.class.getName());
    private static final String ANALYTIC_EXPORT_TO_EXCEL_PLUGIN = "Analytic View: Export to Excel";

    private final File file;
    private final TableView<ScoreResult.ElementScore> table;
    private final String sheetName;

    /**
     * Creates a new export table rows to Excel plugin.
     *
     * @param file the file to save the spreadsheet
     * @param table the table to extract the rows from
     * @param sheetName the name of the sheet in the Excel spreadsheet
     */
    public AnalyticExportToExcelFilePlugin(final File file, final TableView<ScoreResult.ElementScore> table, final String sheetName) {
        this.file = file;
        this.table = table;
        this.sheetName = sheetName;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters)
            throws InterruptedException, PluginException {
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSFWorkbook.DEFAULT_WINDOW_SIZE)) {
            final Sheet sheet = workbook.createSheet(sheetName);

            // get the indexes of all visible columns
            final List<Integer> visibleIndices = table.getColumns().stream()
                    .filter(column -> column.isVisible())
                    .map(column -> table.getColumns().indexOf(column))
                    .collect(Collectors.toList());

            // iterate through the visible columns and print each ones name to the sheet
            final Row headerRow = sheet.createRow(0);
            visibleIndices.forEach(index -> {
                final TableColumn<ScoreResult.ElementScore, ?> column = table.getColumns().get(index);
                final Cell headerCell = headerRow.createCell(visibleIndices.indexOf(index));
                headerCell.setCellValue(column.getText());
            });

            final Thread writeSheetThread = new Thread("Export to Excel File: Writing Sheet") {
                @Override
                public void run() {
                    // get a copy of the table data so that users can continue working
                    final TableView<ScoreResult.ElementScore> data = getTable();

                    if (data != null) {
                        writeRecords(sheet, data, 1);
                    }
                }
            };
            writeSheetThread.start();
            writeSheetThread.join();

            // The sheet has now been created. Time to write it to the file
            final Thread outputThread = new Thread("Export to Excel File: Writing File") {
                @Override
                public void run() {
                    try (final FileOutputStream fileStream = new FileOutputStream(getFile())) {
                        workbook.write(fileStream);
                        LOGGER.log(Level.INFO, "Analytic View data written to Excel file");
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
        return ANALYTIC_EXPORT_TO_EXCEL_PLUGIN;
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
    public TableView<ScoreResult.ElementScore> getTable() {
        return table;
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
    private static void writeRecords(final Sheet sheet, final TableView<ScoreResult.ElementScore> data, final int startIndex) {
        final AtomicInteger rowIndex = new AtomicInteger(startIndex);

        for (int i = 0; i < data.getItems().size(); i++) {
            final String item = data.getColumns().get(0).getCellData(i).toString();
            final String itemData = data.getColumns().get(1).getCellData(i).toString();

            final Row itemRow = sheet.createRow(rowIndex.getAndIncrement());
            final Cell itemCellIdentifier = itemRow.createCell(0);
            final Cell itemCellScore = itemRow.createCell(1);

            itemCellIdentifier.setCellValue(item);
            itemCellScore.setCellValue(itemData);
        }
    }
}
