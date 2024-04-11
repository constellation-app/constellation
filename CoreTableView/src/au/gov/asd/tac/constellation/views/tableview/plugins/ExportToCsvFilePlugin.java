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
import static au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities.getTableData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;

/**
 * Plugin that exports the contents of the table as a CSV. It is possible to
 * select with only selected rows are exported or the whole table.
 *
 * @author formalhaunt
 */
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class ExportToCsvFilePlugin extends SimplePlugin {

    private static final String EXPORT_TO_DELIMITED_FILE_PLUGIN = "Table View: Export to Delimited File";

    private final File file;
    private final TableView<ObservableList<String>> table;
    private final Pagination pagination;
    private final boolean selectedOnly;

    /**
     * Creates a new export table rows to CSV plugin.
     *
     * @param file the file to write the CSV to
     * @param table the table to create the CSV from
     * @param pagination the current pagination of the table
     * @param selectedOnly true if the export should only include selected rows,
     * false otherwise
     */
    public ExportToCsvFilePlugin(final File file, final TableView<ObservableList<String>> table,
            final Pagination pagination, final boolean selectedOnly) {
        this.file = file;
        this.table = table;
        this.pagination = pagination;
        this.selectedOnly = selectedOnly;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, 
            final PluginParameters parameters) throws InterruptedException, PluginException {

        // Extract all the rows from the table as CSV
        final String csvData = getTableData(table, pagination, true, selectedOnly);

        // Write the CSV data to file
        final Thread outputThread = new Thread("Export to CSV File: Writing File") {
            @Override
            public void run() {
                try (final FileWriter fileWriter = new FileWriter(getFile(), StandardCharsets.UTF_8)) {
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

    /**
     * Get the file that the CSV data will be written to.
     *
     * @return the CSV export file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the table that the CSV will be generated from.
     *
     * @return the table
     */
    public TableView<ObservableList<String>> getTable() {
        return table;
    }

    /**
     * Get the current pagination of the table being exported.
     *
     * @return the table pagination
     */
    public Pagination getPagination() {
        return pagination;
    }

    /**
     * Get the flag indicating if only selected rows will be included in the
     * export.
     *
     * @return true if only selected rows will be included, false otherwise
     */
    public boolean isSelectedOnly() {
        return selectedOnly;
    }
}
