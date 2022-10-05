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
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.scene.control.TableView;

/**
 * Plugin that exports the contents of the analytic view results table as a CSV.
 * *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class AnalyticExportToCsvFilePlugin extends SimplePlugin {

    private static final String ANALYTIC_EXPORT_TO_CSV_PLUGIN = "Analytic View: Export to CSV";

    private final File file;
    private final TableView<ScoreResult.ElementScore> table;

    /**
     * Creates a new export table rows to CSV plugin.
     *
     * @param file the file to write the CSV to
     * @param table the table to create the CSV from
     *
     */
    public AnalyticExportToCsvFilePlugin(final File file, final TableView<ScoreResult.ElementScore> table) {
        this.file = file;
        this.table = table;
    }

    @Override
    public void execute(final PluginGraphs graphs,
            final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException, PluginException {

        // Extract all the rows from the table as CSV
        final String csvData = getTableData(table, true);

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
        return ANALYTIC_EXPORT_TO_CSV_PLUGIN;
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
    public TableView<ScoreResult.ElementScore> getTable() {
        return table;
    }

    /**
     * Export the data in the Analytic View results table to CSV
     *
     * @param table
     * @param includeHeader
     * @return
     */
    public static String getTableData(final TableView<ScoreResult.ElementScore> table, final boolean includeHeader) {
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

        for (int i = 0; i < table.getItems().size(); i++) {
            String item = table.getColumns().get(0).getCellData(i).toString();
            String itemData = table.getColumns().get(1).getCellData(i).toString();
            data.append(item + SeparatorConstants.COMMA + itemData);
            data.append(SeparatorConstants.NEWLINE);
        }

        return data.toString();
    }
}
