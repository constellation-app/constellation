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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.VertexListInclusionGraph;
import au.gov.asd.tac.constellation.plugins.importexport.AttributeType;
import au.gov.asd.tac.constellation.plugins.importexport.ImportAttributeDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportConstants;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.RowFilter;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * An ImportDelimitedPlugin is a plugin that actually does the work of importing
 * data from a table into a graph.
 *
 * @author sirius
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@NbBundle.Messages("ImportDelimitedPlugin=Import From File")
public class ImportDelimitedPlugin extends SimpleEditPlugin {

    private static final Logger LOGGER = Logger.getLogger(ImportDelimitedPlugin.class.getName());

    public static final String PARSER_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "parser");
    public static final String FILES_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "files");
    public static final String DEFINITIONS_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "definitions");
    public static final String SCHEMA_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "schema");
    public static final String PARSER_PARAMETER_IDS_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "parser_parameters");
    public static final String FILES_INCLUDE_HEADERS_PARAMETER_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "files_Include_Headers");
    public static final String SKIP_INVALID_ROWS_ID = PluginParameter.buildId(ImportDelimitedPlugin.class, "skip_invalid_rows");

    public static final String IMPORTED_ROWS = "IMPORTED_ROWS";
    public static final String SKIPPED_ROWS = "SKIPPED_ROWS";

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<ObjectParameterValue> parserParam = ObjectParameterType.build(PARSER_PARAMETER_ID);
        parserParam.setName("Parser");
        parserParam.setDescription("The type of parser which is one that extends ImportFileParser");
        parserParam.setObjectValue(null);
        params.addParameter(parserParam);

        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build(FILES_PARAMETER_ID);
        fileParam.setName("Files");
        fileParam.setDescription("The list of file names to import");
        fileParam.setObjectValue(null);
        params.addParameter(fileParam);

        final PluginParameter<ObjectParameterValue> definitionParam = ObjectParameterType.build(DEFINITIONS_PARAMETER_ID);
        definitionParam.setName("Definitions");
        definitionParam.setDescription("The list of definitions that extend ImportDefinition");
        definitionParam.setObjectValue(null);
        params.addParameter(definitionParam);

        final PluginParameter<BooleanParameterValue> schemaParam = BooleanParameterType.build(SCHEMA_PARAMETER_ID);
        schemaParam.setName("Complete with Schema");
        schemaParam.setDescription("True if the graph should run the schema rules");
        schemaParam.setBooleanValue(true);
        params.addParameter(schemaParam);

        final PluginParameter<ObjectParameterValue> parserParameters = ObjectParameterType.build(PARSER_PARAMETER_IDS_PARAMETER_ID);
        parserParameters.setName("Parser Parameters");
        parserParameters.setDescription("The PluginParameters used by the parser");
        params.addParameter(parserParameters);

        final PluginParameter<BooleanParameterValue> filesIncludeHeadersParam = BooleanParameterType.build(FILES_INCLUDE_HEADERS_PARAMETER_ID);
        filesIncludeHeadersParam.setName("Files Include Headers");
        filesIncludeHeadersParam.setDescription("True if the files include headers in the first row");
        filesIncludeHeadersParam.setBooleanValue(true);
        params.addParameter(filesIncludeHeadersParam);

        final PluginParameter<BooleanParameterValue> skipInvalidRows = BooleanParameterType.build(SKIP_INVALID_ROWS_ID);
        skipInvalidRows.setName("Skip invalid rows");
        skipInvalidRows.setDescription("True if the skip invalid rows");
        skipInvalidRows.setBooleanValue(true);
        params.addParameter(skipInvalidRows);

        return params;
    }

    /**
     * Build up an import summary dialog detailing successful and unsuccessful
     * file imports.
     *
     * @param title Title to add to status dialog
     * @param importedRows Number of rows successfully imported (from
     * validFilenames)
     * @param validFilenames List of valid filenames that were imported from
     * @param emptyFilenames List of filenames that were requested to import
     * that contained no data rows
     * @param invalidFilenames List of files that couldn't be opened/parsed. We
     * try to limit this possibility by pre-screening files during the initial
     * file selection
     * @param emptyRunConfigs List of run config names that were found to not
     * have vertex of transaction mappings defined
     */
    private void displaySummaryAlert(final int importedObjects, final int importedRows, final int skippedRows, final List<String> validFilenames,
            final List<String> emptyFilenames, final List<String> invalidFilenames,
            final List<String> emptyRunConfigs) {
        Platform.runLater(() -> {
            final StringBuilder sbHeader = new StringBuilder();
            final StringBuilder sbMessage = new StringBuilder();

            if (importedObjects > 0) {
                // At least 1 object was successfully imported. List all successful file imports, as well as any files
                // that there were issues for. If there were any files with issues use a warning dialog.
                final String fileFiles = (validFilenames.size() == 1) ? "file" : "files";
                final String rowRows = skippedRows == 1 ? "row" : "rows";
                final String skippedRowsMsg = (skippedRows > 0 ? String.format("Skipped %d %s due to import error.", skippedRows, rowRows) : "");
                
                final String importedRowRows = importedRows == 1 ? "row" : "rows";
                sbMessage.append(String.format("Extracted data from %d %s in %d %s. %s",
                        importedRows, importedRowRows, validFilenames.size(), fileFiles, skippedRowsMsg));
                
                sbMessage.append(" Files with data: ");
                for (int i = 0; i < validFilenames.size(); i++) {
                    if (i > 0) {
                        sbMessage.append(", ");
                    }
                    sbMessage.append(validFilenames.get(i));
                }
                sbMessage.append(".");
            } else {
                // No rows were imported list all files that resulted in failures.
                sbMessage.append("No data found.\n");
            }

            if (!emptyFilenames.isEmpty()) {
                // empty files were found.
                sbMessage.append(" Files without data: ");
                for (int i = 0; i < emptyFilenames.size(); i++) {
                    if (i > 0) {
                        sbMessage.append(", ");
                    }
                    sbMessage.append(emptyFilenames.get(i));
                }
                sbMessage.append(".");
            }
            if (!invalidFilenames.isEmpty()) {
                // invalid files were found.
                sbMessage.append(" Invalid files: ");
                for (int i = 0; i < invalidFilenames.size(); i++) {
                    if (i > 0) {
                        sbMessage.append(", ");
                    }
                    sbMessage.append(invalidFilenames.get(i));
                }
                sbMessage.append(".");
            }
            if (!emptyRunConfigs.isEmpty()) {
                // invalid run configs were found.
                sbMessage.append(" Empty Run configs: ");
                for (int i = 0; i < emptyRunConfigs.size(); i++) {
                    if (i > 0) {
                        sbMessage.append(", ");
                    }
                    sbMessage.append(emptyRunConfigs.get(i));
                }
                sbMessage.append(".");
            }
            NotificationDisplayer.getDefault().notify("File Import", UserInterfaceIconProvider.UPLOAD.buildIcon(16, ConstellationColor.BLUE.getJavaColor()), sbMessage.toString(), null, NotificationDisplayer.Priority.HIGH);
        });
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final ImportFileParser parser = (ImportFileParser) parameters.getParameters().get(PARSER_PARAMETER_ID).getObjectValue();
        @SuppressWarnings("unchecked") //files will be a list of file which extends from object type
        final List<File> files = (List<File>) parameters.getParameters().get(FILES_PARAMETER_ID).getObjectValue();
        @SuppressWarnings("unchecked") //definitions will be a list of import defintions which extends from object type
        final List<ImportDefinition> definitions = (List<ImportDefinition>) parameters.getParameters().get(DEFINITIONS_PARAMETER_ID).getObjectValue();
        final boolean initialiseWithSchema = parameters.getParameters().get(SCHEMA_PARAMETER_ID).getBooleanValue();
        final PluginParameters parserParameters = (PluginParameters) parameters.getParameters().get(PARSER_PARAMETER_IDS_PARAMETER_ID).getObjectValue();
        final boolean filesIncludeHeaders = parameters.getParameters().get(FILES_INCLUDE_HEADERS_PARAMETER_ID).getBooleanValue();
        boolean positionalAtrributesExist = false;

        final boolean skipInvalidRows = parameters.getParameters().get(SKIP_INVALID_ROWS_ID).getBooleanValue();

        final List<String> validFiles = new ArrayList<>();
        final List<String> emptyFiles = new ArrayList<>();
        final List<String> invalidFiles = new ArrayList<>();
        final List<String> emptyRunConfigs = new ArrayList<>();
        Map<String, Integer> results;
        int totalRows = 0;
        int totalImportedRows = 0;
        int totalSkippedRows = 0;
        int dataSize = 0;

        // Loop through import definitions looking for those that don't have either a source or destination vertex (as
        // a minimum) defined
        for (final ImportDefinition definition : definitions) {
            if (definition.getDefinitions(AttributeType.SOURCE_VERTEX).isEmpty()
                    && definition.getDefinitions(AttributeType.DESTINATION_VERTEX).isEmpty()) {
                emptyRunConfigs.add(definition.getDefinitionName());
            }
        }

        for (final File file : files) {
            interaction.setProgress(0, 0, "Reading File: " + file.getName(), true);
            List<String[]> data = null;
            int importedRowsPerFile = 0;
            int skippedRowsPerFile = 0;

            try {
                data = parser.parse(new InputSource(file), parserParameters);
                dataSize = filesIncludeHeaders ? data.size() - 1 : data.size();
                totalRows = totalRows + Integer.max(0, dataSize);

                if (dataSize > 0) {
                    if (validFiles.isEmpty()) {
                        validFiles.add(file.getName().concat(" (").concat(Integer.toString(dataSize)).concat(" rows)"));
                    } else {
                        validFiles.add(file.getName().concat(" (").concat(Integer.toString(dataSize)).concat(")"));
                    }
                } else {
                    emptyFiles.add(file.getName());
                }
            } catch (FileNotFoundException ex) {
                final String errorMsg = file.getPath() + " could not be found. Ignoring file during import.";
                LOGGER.log(Level.INFO, errorMsg);
                invalidFiles.add(file.getName());
            } catch (IOException ex) {
                final String errorMsg = file.getPath() + " could not be parsed. Removing file during import.";
                LOGGER.log(Level.INFO, errorMsg);
                invalidFiles.add(file.getName());
            }

            if (data != null) {
                for (final ImportDefinition definition : definitions) {

                    if (definition.getDefinitions(AttributeType.SOURCE_VERTEX).isEmpty()) {
                        // No source vertex definitions are set, the only option left is destination vertexes being mapped.
                        // Process destination vertexes if defintions are defined, otherwise there is nothing to do.
                        if (!definition.getDefinitions(AttributeType.DESTINATION_VERTEX).isEmpty()) {
                            results = processDestinationVertices(definition, graph, data, initialiseWithSchema, skipInvalidRows, interaction, file.getName());
                            importedRowsPerFile += results.get(IMPORTED_ROWS);
                            skippedRowsPerFile += results.get(SKIPPED_ROWS);
                        }
                    } else if (definition.getDefinitions(AttributeType.DESTINATION_VERTEX).isEmpty()) {
                        // Source defintions exist, but no destination definitions exist. Process the source definitions.
                        results = processSourceVertices(definition, graph, data, initialiseWithSchema, skipInvalidRows, interaction, file.getName());
                        importedRowsPerFile += results.get(IMPORTED_ROWS);
                        skippedRowsPerFile += results.get(SKIPPED_ROWS);
                    } else {
                        // Both source and destination defintions exist, process them. 
                        results = processTransactions(definition, graph, data, initialiseWithSchema, skipInvalidRows, interaction, file.getName());
                        importedRowsPerFile += results.get(IMPORTED_ROWS);
                        skippedRowsPerFile += results.get(SKIPPED_ROWS);
                    }

                    // Determine if a positional attribute has been defined, if so update the overall flag
                    final boolean isPositional = attributeDefintionIsPositional(definition.getDefinitions(AttributeType.SOURCE_VERTEX), definition.getDefinitions(AttributeType.DESTINATION_VERTEX));
                    positionalAtrributesExist = (positionalAtrributesExist || isPositional);
                }
            }
            totalImportedRows += importedRowsPerFile;
            totalSkippedRows += skippedRowsPerFile;

            LOGGER.log(Level.INFO, "Imported {0} rows of data from file {1} containing {2} total rows. Skipped {3} rows due to error.", new Object[]{importedRowsPerFile, file.getPath(), dataSize, skippedRowsPerFile});
        }

        displaySummaryAlert(graph.getVertexCount() + graph.getTransactionCount(), totalImportedRows, totalSkippedRows, validFiles, emptyFiles, invalidFiles, emptyRunConfigs);

        ConstellationLoggerHelper.importPropertyBuilder(
                this,
                GraphRecordStoreUtilities.getVertices(graph, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                files,
                ConstellationLoggerHelper.SUCCESS
        );
        LOGGER.log(Level.INFO, "Auto arrangement use={0}", (!positionalAtrributesExist));

        // If at least one positional attribute has been received for either the src or destination vertex we will assume that the user is trying to import positions and won't auto arrange
        // the graph. This does mean some nodes could sit on top of each other if multiple nodes have the same coordinates.
        if (!positionalAtrributesExist) {
            interaction.setProgress(1, 1, "Arranging", true);

            graph.validateKey(GraphElementType.VERTEX, true);
            graph.validateKey(GraphElementType.TRANSACTION, true);

            // unfortunately need to arrange with pendants and uncollide because grid arranger works based on selection
            final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(graph, AbstractInclusionGraph.Connections.NONE, new ArrayList<>());

            PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                    .followedBy(ArrangementPluginRegistry.PENDANTS)
                    .followedBy(ArrangementPluginRegistry.UNCOLLIDE)
                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .executeNow(vlGraph.getInclusionGraph());
            vlGraph.retrieveCoords();
        }
    }

    // If src or destination attribute definitions have been supplied, check them and return true if any of the positional attributes ('x', 'y', or 'z') are included. Positional
    // arguments allow the import to define where nodes will be placed on graph. If src or destination definitons do not exist then an empty list should be supplied.
    private static boolean attributeDefintionIsPositional(List<ImportAttributeDefinition> srcAttributeDefinitions, List<ImportAttributeDefinition> destAttributeDefinitions) {

        // Check if srcAttributeDefintions contain positional attributes
        if (srcAttributeDefinitions.stream().map(attribute -> attribute.getAttribute().getName()).anyMatch(name -> (VisualConcept.VertexAttribute.X.getName().equals(name) || VisualConcept.VertexAttribute.Y.getName().equals(name) || VisualConcept.VertexAttribute.Z.getName().equals(name)))) {
            return true;
        }
        // Check if destAttributeDefintions contain positional attributes
        return destAttributeDefinitions.stream().map(attribute -> attribute.getAttribute().getName()).anyMatch(name -> (VisualConcept.VertexAttribute.X.getName().equals(name) || VisualConcept.VertexAttribute.Y.getName().equals(name) || VisualConcept.VertexAttribute.Z.getName().equals(name)));
    }

    private static Map<String, Integer> processSourceVertices(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final boolean initialiseWithSchema,
            final boolean skipInvalidRows, final PluginInteraction interaction, final String source) throws InterruptedException, PluginException {
        return processVertices(definition, graph, data, AttributeType.SOURCE_VERTEX, initialiseWithSchema, skipInvalidRows, interaction, source);
    }

    private static Map<String, Integer> processDestinationVertices(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final boolean initialiseWithSchema,
            final boolean skipInvalidRows, final PluginInteraction interaction, final String source) throws InterruptedException, PluginException {
        return processVertices(definition, graph, data, AttributeType.DESTINATION_VERTEX, initialiseWithSchema, skipInvalidRows, interaction, source);
    }

    private static Map<String, Integer> processVertices(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final AttributeType attributeType,
            final boolean initialiseWithSchema, final boolean skipInvalidRows, final PluginInteraction interaction, final String source) throws InterruptedException, PluginException {
        final List<ImportAttributeDefinition> attributeDefinitions = definition.getDefinitions(attributeType);

        addAttributes(graph, GraphElementType.VERTEX, attributeDefinitions);

        int currentRow = 0;
        int importedRows = 0;
        int skippedRow = 0;
        final Map<String, Integer> results = new HashMap<>();
        final int totalRows = data.size() - definition.getFirstRow();

        final RowFilter filter = definition.getRowFilter();

        for (int i = definition.getFirstRow(); i < data.size(); i++) {
            interaction.setProgress(++currentRow, totalRows, "Importing Vertices: " + source, true);

            final String[] row = data.get(i);
            int vertexId = -1;
            
            try {
                
                if (filter == null || filter.passesFilter(i - 1, row)) {

                    vertexId = graph.addVertex();
                    for (final ImportAttributeDefinition attributeDefinition : attributeDefinitions) {
                        attributeDefinition.setValue(graph, vertexId, row, (i - 1));
                    }

                    if (initialiseWithSchema && graph.getSchema() != null) {
                        graph.getSchema().completeVertex(graph, vertexId);
                    }

                    // Count the number of processed rows to notify in the status message
                    ++importedRows;

                }
            } catch (final DateTimeException | IllegalArgumentException | SecurityException ex) {
                if (skipInvalidRows) {
                    if(vertexId != -1) {
                        graph.removeVertex(vertexId);
                    }                    
                    ++skippedRow;
                } else {
                    final PluginException plugEx = new PluginException(PluginNotificationLevel.ERROR, "Unable to complete import due to error with data. " + ex.getMessage() +" The file can be imported if you select Skip invalid rows checkbox next to the Import button.");
                    plugEx.setStackTrace(ex.getStackTrace());
                    throw plugEx;
                }
            }
        }
        results.put(IMPORTED_ROWS, importedRows);
        results.put(SKIPPED_ROWS, skippedRow);
        return results;
    }

    private static Map<String, Integer> processTransactions(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final boolean initialiseWithSchema, final boolean skipInvalidRows, final PluginInteraction interaction, final String source) throws InterruptedException, PluginException {
        final List<ImportAttributeDefinition> sourceVertexDefinitions = definition.getDefinitions(AttributeType.SOURCE_VERTEX);
        final List<ImportAttributeDefinition> destinationVertexDefinitions = definition.getDefinitions(AttributeType.DESTINATION_VERTEX);
        final List<ImportAttributeDefinition> transactionDefinitions = definition.getDefinitions(AttributeType.TRANSACTION);

        int directedIx = ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN;
        for (int i = 0; i < transactionDefinitions.size(); i++) {
            if (transactionDefinitions.get(i).getAttribute().getName().equals(DelimitedImportController.DIRECTED)) {
                directedIx = transactionDefinitions.get(i).getColumnIndex();
                break;
            }
        }

        addAttributes(graph, GraphElementType.VERTEX, sourceVertexDefinitions);
        addAttributes(graph, GraphElementType.VERTEX, destinationVertexDefinitions);
        addAttributes(graph, GraphElementType.TRANSACTION, transactionDefinitions);

        int currentRow = 0;
        int importedRows = 0;
        int skippedRow = 0;
        final Map<String, Integer> results = new HashMap<>();
        final int totalRows = data.size() - definition.getFirstRow();

        final RowFilter filter = definition.getRowFilter();

        for (int i = definition.getFirstRow(); i < data.size(); i++) {
            interaction.setProgress(++currentRow, totalRows, "Importing Transactions: " + source, true);

            final String[] row = data.get(i);

            if (filter == null || filter.passesFilter(i - 1, row)) {

                final int sourceVertexId = graph.addVertex();
                final int destinationVertexId = graph.addVertex();

                final boolean isDirected = directedIx == ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN || Boolean.parseBoolean(row[directedIx]);
                final int transactionId = graph.addTransaction(sourceVertexId, destinationVertexId, isDirected);

                try {
                    for (final ImportAttributeDefinition attributeDefinition : transactionDefinitions) {
                        if (attributeDefinition.getOverriddenAttributeId() != Graph.NOT_FOUND) {
                            attributeDefinition.setValue(graph, transactionId, row, (i - 1));
                        }
                    }

                    for (final ImportAttributeDefinition attributeDefinition : destinationVertexDefinitions) {
                        attributeDefinition.setValue(graph, destinationVertexId, row, (i - 1));
                    }

                    for (final ImportAttributeDefinition attributeDefinition : sourceVertexDefinitions) {
                        attributeDefinition.setValue(graph, sourceVertexId, row, (i - 1));
                    }
                    if (initialiseWithSchema && graph.getSchema() != null) {
                        graph.getSchema().completeVertex(graph, sourceVertexId);
                        graph.getSchema().completeVertex(graph, destinationVertexId);
                        graph.getSchema().completeTransaction(graph, transactionId);
                    }

                    // Count the number of processed rows to notify in the status message
                    ++importedRows;

                } catch (final DateTimeException | IllegalArgumentException | SecurityException ex) {
                    if (skipInvalidRows) {
                        graph.removeTransaction(transactionId);
                        graph.removeVertex(sourceVertexId);
                        graph.removeVertex(destinationVertexId);
                        ++skippedRow;
                    } else {
                        final PluginException plugEx = new PluginException(PluginNotificationLevel.ERROR, "Unable to complete import due to error with data. " + ex.getMessage() +" The file can be imported if you select Skip invalid rows checkbox next to the Import button.");
                        plugEx.setStackTrace(ex.getStackTrace());
                        throw plugEx;
                    }
                }
            }

        }

        results.put(IMPORTED_ROWS, importedRows);
        results.put(SKIPPED_ROWS, skippedRow);

        return results;
    }

    /**
     * Add the attribute to the graph
     *
     * @param graph
     * @param elementType Graph element type
     * @param attributeDefinitions
     */
    private static void addAttributes(GraphWriteMethods graph, GraphElementType elementType, List<ImportAttributeDefinition> attributeDefinitions) {
        for (final ImportAttributeDefinition attributeDefinition : attributeDefinitions) {
            final Attribute attribute = attributeDefinition.getAttribute();

            // If the attribute is not assigned to a column but has a default
            // value defined or, the attribute is assigned to a column; add
            // the attribute to the graph and store the attribute id
            if ((attributeDefinition.getColumnIndex() == ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN && attributeDefinition.getDefaultValue() != null)
                    || (attributeDefinition.getColumnIndex() != ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)) {
                int attributeId = graph.getSchema() != null
                        ? graph.getSchema().getFactory().ensureAttribute(graph, elementType, attribute.getName())
                        : Graph.NOT_FOUND;
                if (attributeId == Graph.NOT_FOUND) {
                    attributeId = graph.addAttribute(elementType, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(),
                            attribute.getDefaultValue(), attribute.getAttributeMerger() == null ? null : attribute.getAttributeMerger().getId());
                }
                attributeDefinition.setOverriddenAttributeId(attributeId);
            }

        }
    }
}
