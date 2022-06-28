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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.ActionPane;
import au.gov.asd.tac.constellation.plugins.importexport.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.RefreshRequest;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author sirius
 */
public class DelimitedImportController extends ImportController {

    private static final Logger LOGGER = Logger.getLogger(DelimitedImportController.class.getName());

    private final RefreshRequest refreshRequest = this::updateSampleData;
    private final List<File> files;
    private File sampleFile;
    private ImportFileParser importFileParser;
    private boolean filesIncludeHeaders;

    protected ActionPane actionPane;

    public DelimitedImportController() {
        super();
        files = new ArrayList<>();
        importFileParser = ImportFileParser.DEFAULT_PARSER;
        schemaInitialised = true;
        filesIncludeHeaders = true;
    }

    public void setImportPane(final DelimitedImportPane importPane) {
        this.importPane = importPane;
        super.setImportPane(importPane);
    }

    public void setfilesIncludeHeaders(final boolean filesIncludeHeaders) {
        if (this.filesIncludeHeaders != filesIncludeHeaders) {
            this.filesIncludeHeaders = filesIncludeHeaders;
            updateSampleData();
            validateFileStructure(files);
        }
    }
    
    protected List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public boolean isFilesIncludeHeadersEnabled() {
        return filesIncludeHeaders;
    }

    public boolean hasFiles() {
        return !files.isEmpty();
    }

    public void setFiles(final List<File> files, final File sampleFile) {
        this.files.clear();
        this.files.addAll(files);

        if (currentParameters != null) {
            final List<InputSource> inputSources = new ArrayList<>();
            files.forEach(file -> inputSources.add(new InputSource(file)));
            importFileParser.updateParameters(currentParameters, inputSources);
        }

        if (sampleFile == null && CollectionUtils.isNotEmpty(files)) {
            this.sampleFile = files.get(0);
        } else {
            this.sampleFile = sampleFile;
        }

        if (sampleFile == null && CollectionUtils.isEmpty(files)) {
            clearFilters();
        }
        updateSampleData();
    }

    public void clearFilters() {
        configurationPane.clearFilters();
    }

    @Override
    public void processImport() throws PluginException {
        final List<ImportDefinition> definitions = configurationPane.createDefinitions(isFilesIncludeHeadersEnabled());
        final Graph importGraph = currentDestination.getGraph();
        final boolean schema = schemaInitialised;
        final List<File> importFiles = new ArrayList<>(files);
        final ImportFileParser parser = importFileParser;

        if (currentDestination instanceof SchemaDestination) {

            final GraphManagerListener graphManagerListener = new GraphManagerListener() {
                private boolean opened = false;

                @Override
                public void graphOpened(Graph graph) {
                    // Do nothing
                }

                @Override
                public void graphClosed(Graph graph) {
                    // Do nothing
                }

                @Override
                public synchronized void newActiveGraph(final Graph graph) {
                    if (graph == importGraph && !opened) {
                        opened = true;
                        GraphManager.getDefault().removeGraphManagerListener(this);
                        PluginExecutor.startWith(ImportExportPluginRegistry.IMPORT_DELIMITED, false)
                                .set(ImportDelimitedPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                                .set(ImportDelimitedPlugin.PARSER_PARAMETER_ID, parser)
                                .set(ImportDelimitedPlugin.FILES_PARAMETER_ID, importFiles)
                                .set(ImportDelimitedPlugin.SCHEMA_PARAMETER_ID, schema)
                                .set(ImportDelimitedPlugin.PARSER_PARAMETER_IDS_PARAMETER_ID, currentParameters)
                                .set(ImportDelimitedPlugin.FILES_INCLUDE_HEADERS_PARAMETER_ID, filesIncludeHeaders)
                                .followedBy(ArrangementPluginRegistry.GRID_COMPOSITE)
                                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                                .executeWriteLater(importGraph);
                    }
                }
            };

            GraphManager.getDefault().addGraphManagerListener(graphManagerListener);
            GraphOpener.getDefault().openGraph(importGraph, "graph");
        } else {
            PluginExecutor.startWith(ImportExportPluginRegistry.IMPORT_DELIMITED, false)
                    .set(ImportDelimitedPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                    .set(ImportDelimitedPlugin.PARSER_PARAMETER_ID, parser)
                    .set(ImportDelimitedPlugin.FILES_PARAMETER_ID, importFiles)
                    .set(ImportDelimitedPlugin.SCHEMA_PARAMETER_ID, schema)
                    .set(ImportDelimitedPlugin.FILES_INCLUDE_HEADERS_PARAMETER_ID, filesIncludeHeaders)
                    .followedBy(ArrangementPluginRegistry.GRID_COMPOSITE)
                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .executeWriteLater(importGraph);
        }
    }

    @Override
    protected void updateSampleData() {
        if (sampleFile == null) {
            currentColumns = new String[0];
            currentData = new ArrayList<>();
        } else {
            try {
                currentData = importFileParser.preview(new InputSource(sampleFile), currentParameters,
                        PREVIEW_ROW_LIMIT);
                final String[] columns = currentData.isEmpty() ? new String[0] : (String[]) currentData.get(0);
                currentColumns = new String[columns.length + 1];
                if (!isFilesIncludeHeadersEnabled()) {
                    for (int i = 1; i < columns.length + 1; i++) {
                        currentColumns[i] = "Col" + i;
                    }
                } else {
                    System.arraycopy(columns, 0, currentColumns, 1, columns.length);
                }
                currentColumns[0] = "Row";
            } catch (final FileNotFoundException ex) {
                final String warningMsg = "The following file could not be found "
                        + "and has been excluded from the import set:\n  " + sampleFile.getPath();
                LOGGER.log(Level.INFO, warningMsg, ex);
                NotifyDisplayer.displayAlert("Delimited File Import", "Invalid file selected",
                        warningMsg, Alert.AlertType.WARNING);
                files.remove(sampleFile);
                ((DelimitedSourcePane) importPane.getSourcePane()).removeFile(sampleFile);
            } catch (final IOException ex) {
                final String warningMsg = "The following file could not be parsed and has "
                        + "been excluded from the import set:\n  " + sampleFile.getPath();
                LOGGER.log(Level.INFO, warningMsg, ex);
                NotifyDisplayer.displayAlert("Delimited File Import", "Invalid file selected", warningMsg,
                        Alert.AlertType.WARNING);
                files.remove(sampleFile);
                ((DelimitedSourcePane) importPane.getSourcePane()).removeFile(sampleFile);
            }
        }

        if (configurationPane != null) {
            if (!currentData.isEmpty() && isFilesIncludeHeadersEnabled()) {
                currentData.remove(0); //Remove the first row with column headers
            }
            configurationPane.setSampleData(currentColumns, currentData);
        }
    }
    
    protected void notifyFileStructureMismatchWarning(final List<File> filesToRemove) {
        final StringBuilder warningMsg = new StringBuilder((filesToRemove.size() > 1 ? "These files have" : "This file has"))
                .append(" a different structure and will be removed: ").append(SeparatorConstants.NEWLINE);

        filesToRemove.forEach(file -> warningMsg.append(file.getPath()).append(SeparatorConstants.NEWLINE));

        LOGGER.log(Level.INFO, "Header structure mismatch. These files will be removed: {0}", filesToRemove);

        NotifyDisplayer.displayAlert("Header structure mismatch", "The header structure of the files should be the same", "If the `Files Include Headers` "
                + "option is disabled, the number of columns should be the same. Otherwise the column headers should be the same, in the same order. "
                + SeparatorConstants.NEWLINE + warningMsg,
                Alert.AlertType.WARNING);
    }

    public List<String> getColumnHeaders(final File file) {
        final List<String> headersArray = new ArrayList<>();
        try (final BufferedReader bufferReader = new BufferedReader(new FileReader(file))) {
            final String headerLine = bufferReader.readLine();
            headersArray.addAll(Arrays.asList(headerLine.split(SeparatorConstants.COMMA)));

        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        return headersArray;
    }

    // Iterates over files and do a check to make sure that file structure is the same.
    // If the `Files Include Headers` option is disabled the number of columns should be the same.
    // Otherwise the column headers should be the same in the same order.
    public List<File> validateFileStructure(final List<File> filesToValidate) {
        List<File> invalidFiles = new ArrayList<>();

        if (files.size() <= 1) {
            // There's only one file, nothing to compare
            return invalidFiles;
        }

        for (final File file : filesToValidate) {
            final List<String> referenceColumns = getColumnHeaders(files.get(0));
            final List<String> fileColumns = getColumnHeaders(file);

            if (!isFilesIncludeHeadersEnabled()) {
                if (referenceColumns.size() != fileColumns.size()) {
                    invalidFiles.add(file);
                }
            } else if (!fileColumns.equals(referenceColumns)) {
                invalidFiles.add(file);
            }
        }
        if (!invalidFiles.isEmpty()) {
            notifyFileStructureMismatchWarning(invalidFiles);

            for (final File fileToRemove : invalidFiles) {
                files.remove(fileToRemove);
                ((DelimitedSourcePane) importPane.getSourcePane()).removeFile(fileToRemove);
            }
        }
        return invalidFiles;
    }

    public void setImportFileParser(final ImportFileParser importFileParser) {
        if (this.importFileParser != importFileParser) {
            this.importFileParser = importFileParser;

            if (importFileParser == null) {
                currentParameters = null;
            } else {
                currentParameters = importFileParser.getParameters(refreshRequest);
                final List<InputSource> inputSources = new ArrayList<>();
                files.forEach(file -> inputSources.add(new InputSource(file)));
                importFileParser.updateParameters(currentParameters, inputSources);
            }
            importPane.getSourcePane().setParameters(currentParameters);
            updateSampleData();
        }
    }

    public ImportFileParser getImportFileParser() {
        return importFileParser;
    }

    // expands or shrinks the import pane based on if there is a file present
    // within the imported files list
    void openConfigPane(final boolean b) {
        importPane.expandPane(b);
    }

    // enables or disables the import button based on if there is a file present
    // within the imported files list
    void disableButton(final boolean b) {
        importPane.disableButton(b);
    }
}
