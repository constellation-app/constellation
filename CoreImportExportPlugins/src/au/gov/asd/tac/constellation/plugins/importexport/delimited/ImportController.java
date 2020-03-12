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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPreferenceKeys;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 *
 * @author sirius
 */
public class ImportController {

    /**
     * Pseudo-attribute to indicate directed transactions.
     */
    public static final String DIRECTED = "__directed__";

    /**
     * Limit the number of rows shown in the preview.
     */
    private static final int PREVIEW_ROW_LIMIT = 100;

    private final DelimitedFileImporterStage stage;
    private final List<File> files;
    private File sampleFile = null;
    private List<String[]> currentData = new ArrayList<>();
//    private ObservableList<TableRow> currentRows = FXCollections.emptyObservableList();
    private String[] currentColumns = new String[0];
    private ConfigurationPane configurationPane;
    private ImportDestination<?> currentDestination;
    private ImportFileParser importFileParser;
    private boolean schemaInitialised;
    private boolean showAllSchemaAttributes;
    private PluginParameters currentParameters = null;

    // Attributes that exist in the graph or schema.
    private final Map<String, Attribute> autoAddedVertexAttributes;
    private final Map<String, Attribute> autoAddedTransactionAttributes;

    // Attributes that have been manually added by the user.
    private final Map<String, Attribute> manuallyAddedVertexAttributes;
    private final Map<String, Attribute> manuallyAddedTransactionAttributes;
    private boolean clearManuallyAdded;

    private Map<String, Attribute> displayedVertexAttributes;
    private Map<String, Attribute> displayedTransactionAttributes;
    private final Set<Integer> keys;

    // preference to show or hide all graph schema attributes
    private final Preferences importExportPrefs = NbPreferences.forModule(ImportExportPreferenceKeys.class);

    private final RefreshRequest refreshRequest = () -> {
        updateSampleData();
    };

    public ImportController(final DelimitedFileImporterStage stage) {
        this.stage = stage;
        files = new ArrayList<>();
        importFileParser = ImportFileParser.DEFAULT_PARSER;
        schemaInitialised = true;
        showAllSchemaAttributes = false;

        autoAddedVertexAttributes = new HashMap<>();
        autoAddedTransactionAttributes = new HashMap<>();

        manuallyAddedVertexAttributes = new HashMap<>();
        manuallyAddedTransactionAttributes = new HashMap<>();
        clearManuallyAdded = true;

        displayedVertexAttributes = new HashMap<>();
        displayedTransactionAttributes = new HashMap<>();

        keys = new HashSet<>();
    }

    public DelimitedFileImporterStage getStage() {
        return stage;
    }

    public ConfigurationPane getConfigurationPane() {
        return configurationPane;
    }

    public void setConfigurationPane(final ConfigurationPane configurationPane) {
        this.configurationPane = configurationPane;
        if (currentDestination != null) {
            setDestination(currentDestination);
        }
    }

    public boolean hasFiles() {
        return !files.isEmpty();
    }

    public void setFiles(final List<File> files, final File sampleFile) {
        this.files.clear();
        this.files.addAll(files);

        if (currentParameters != null) {
            List<InputSource> inputSources = new ArrayList<>();
            for (File file : files) {
                inputSources.add(new InputSource(file));
            }
            importFileParser.updateParameters(currentParameters, inputSources);
        }

        if (sampleFile == null && files != null && !files.isEmpty()) {
            this.sampleFile = files.get(0);
        } else {
            this.sampleFile = sampleFile;
        }

        updateSampleData();
    }

    /**
     * Whether the ImportController should clear the manually added attributes
     * in setDestination().
     * <p>
     * Defaults to true, but when attributes have been added manually by a
     * loaded template, should be false.
     *
     * @param b True to cause the manually added attributes to be cleared, false
     * otherwise.
     */
    public void setClearManuallyAdded(final boolean b) {
        clearManuallyAdded = b;
    }

    public void setDestination(final ImportDestination<?> destination) {
        this.currentDestination = destination;

        // Clearing the manually added attributes removes them when loading a template.
        // The destination is set with clearmanuallyAdded true before loading the
        // template, so there are no other left-over attributes to clear out after
        // loading a template.
        if (clearManuallyAdded) {
            manuallyAddedVertexAttributes.clear();
            manuallyAddedTransactionAttributes.clear();
        }
        keys.clear();

        final boolean showSchemaAttributes = importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
        loadAllSchemaAttributes(destination, showSchemaAttributes);

        updateDisplayedAttributes();
    }

    /**
     * Load all the schema attributes of the graph
     *
     * @param destination the destination for the imported data.
     * @param showSchemaAttributes specifies whether schema attributes should be
     * included.
     */
    public void loadAllSchemaAttributes(final ImportDestination<?> destination, final boolean showSchemaAttributes) {
        final Graph graph = destination.getGraph();
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            updateAutoAddedAttributes(GraphElementType.VERTEX, autoAddedVertexAttributes, rg, showSchemaAttributes);
            updateAutoAddedAttributes(GraphElementType.TRANSACTION, autoAddedTransactionAttributes, rg, showSchemaAttributes);
        } finally {
            rg.release();
        }
    }

    /**
     * True if the specified attribute is known, false otherwise.
     * <p>
     * Only the auto added attributes are checked.
     *
     * @param elementType The element type of the attribute.
     * @param label The attribute label.
     *
     * @return True if the specified attribute is known, false otherwise.
     */
    public boolean hasAttribute(final GraphElementType elementType, final String label) {
        switch (elementType) {
            case VERTEX:
                return autoAddedVertexAttributes.containsKey(label);
            case TRANSACTION:
                return autoAddedTransactionAttributes.containsKey(label);
            default:
                throw new IllegalArgumentException("Element type must be VERTEX or TRANSACTION");
        }
    }

    /**
     * Get the specified attribute.
     * <p>
     * Both the auto added and manually added attributes are checked.
     *
     * @param elementType The element type of the attribute.
     * @param label The attribute label.
     *
     * @return The specified attribute, or null if the attribute is not found.
     */
    public Attribute getAttribute(final GraphElementType elementType, final String label) {
        switch (elementType) {
            case VERTEX:
                return autoAddedVertexAttributes.containsKey(label) ? autoAddedVertexAttributes.get(label) : manuallyAddedVertexAttributes.get(label);
            case TRANSACTION:
                return autoAddedTransactionAttributes.containsKey(label) ? autoAddedTransactionAttributes.get(label) : manuallyAddedTransactionAttributes.get(label);
            default:
                throw new IllegalArgumentException("Element type must be VERTEX or TRANSACTION");
        }
    }

    /**
     * Get the attributes that will automatically be added to the attribute
     * list.
     *
     * @param elementType
     * @param attributes
     * @param rg
     */
    private void updateAutoAddedAttributes(final GraphElementType elementType, final Map<String, Attribute> attributes, final GraphReadMethods rg, final boolean showSchemaAttributes) {
        attributes.clear();

        // Add attributes from the graph
        int attributeCount = rg.getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            int attributeId = rg.getAttribute(elementType, i);
            Attribute attribute = new GraphAttribute(rg, attributeId);
            attributes.put(attribute.getName(), attribute);
        }

        // Add attributes from the schema
        if (showSchemaAttributes && rg.getSchema() != null) {
            final SchemaFactory factory = rg.getSchema().getFactory();
            for (final SchemaAttribute sattr : factory.getRegisteredAttributes(elementType).values()) {
                final Attribute attribute = new GraphAttribute(elementType, sattr.getAttributeType(), sattr.getName(), sattr.getDescription());
                if (!attributes.containsKey(attribute.getName())) {
                    attributes.put(attribute.getName(), attribute);
                }
            }
        }

        // Add pseudo-attributes
        if (elementType == GraphElementType.TRANSACTION) {
            final Attribute attribute = new GraphAttribute(elementType, BooleanAttributeDescription.ATTRIBUTE_NAME, DIRECTED, "Is this transaction directed?");
            attributes.put(attribute.getName(), attribute);
        }

        // Add primary keys
        for (int key : rg.getPrimaryKey(elementType)) {
            keys.add(key);
        }
    }

    public void deleteAttribute(final Attribute attribute) {
        if (attribute.getElementType() == GraphElementType.VERTEX) {
            manuallyAddedVertexAttributes.remove(attribute.getName());
        } else {
            manuallyAddedTransactionAttributes.remove(attribute.getName());
        }

        if (configurationPane != null) {
            configurationPane.deleteAttribute(attribute);
        }
    }

    public void updateDisplayedAttributes() {
        if (configurationPane != null) {

            displayedVertexAttributes = createDisplayedAttributes(autoAddedVertexAttributes, manuallyAddedVertexAttributes);
            displayedTransactionAttributes = createDisplayedAttributes(autoAddedTransactionAttributes, manuallyAddedTransactionAttributes);

            for (Attribute attribute : configurationPane.getAllocatedAttributes()) {
                if (attribute.getElementType() == GraphElementType.VERTEX) {
                    if (!displayedVertexAttributes.containsKey(attribute.getName())) {
                        Attribute newAttribute = new NewAttribute(attribute);
                        displayedVertexAttributes.put(newAttribute.getName(), newAttribute);
                    }
                } else {
                    if (!displayedTransactionAttributes.containsKey(attribute.getName())) {
                        Attribute newAttribute = new NewAttribute(attribute);
                        displayedTransactionAttributes.put(newAttribute.getName(), newAttribute);
                    }
                }
            }

            configurationPane.setDisplayedAttributes(displayedVertexAttributes, displayedTransactionAttributes, keys);
        }
    }

    private Map<String, Attribute> createDisplayedAttributes(final Map<String, Attribute> autoAddedAttributes, final Map<String, Attribute> manuallyAddedAttributes) {
        Map<String, Attribute> displayedAttributes = new HashMap<>();
        displayedAttributes.putAll(manuallyAddedAttributes);
        displayedAttributes.putAll(autoAddedAttributes);
        return displayedAttributes;
    }

    public void createManualAttribute(final Attribute attribute) {
        Map<String, Attribute> attributes = attribute.getElementType() == GraphElementType.VERTEX ? manuallyAddedVertexAttributes : manuallyAddedTransactionAttributes;

        if (!attributes.containsKey(attribute.getName())) {
            attributes.put(attribute.getName(), attribute);

            if (configurationPane != null) {
                updateDisplayedAttributes();
            }
        }
    }

    public String showSetDefaultValueDialog(final String attributeName, final String currentDefaultValue) {
        DefaultAttributeValueDialog dialog = new DefaultAttributeValueDialog(stage, attributeName, currentDefaultValue);
        dialog.showAndWait();
        return dialog.getDefaultValue();
    }

    public ImportDestination<?> getDestination() {
        return currentDestination;
    }

    /**
     * A List&lt;ImportDefinition&gt; where each list element corresponds to a
     * RunPane tab.
     *
     * @return A List&lt;ImportDefinition&gt; where each list element
     * corresponds to a RunPane tab.
     */
    public List<ImportDefinition> getDefinitions() {
        return configurationPane.createDefinitions();
    }

    public void processImport() throws IOException, InterruptedException, PluginException {

        final List<ImportDefinition> definitions = configurationPane.createDefinitions();

        final Graph importGraph = currentDestination.getGraph();
        final boolean schema = schemaInitialised;
        final List<File> importFiles = new ArrayList<>(files);
        final ImportFileParser parser = importFileParser;

        if (currentDestination instanceof SchemaDestination) {
            GraphManager.getDefault().addGraphManagerListener(new GraphManagerListener() {
                boolean opened = false;

                @Override
                public void graphOpened(Graph graph) {
                }

                @Override
                public void graphClosed(Graph graph) {
                }

                @Override
                public synchronized void newActiveGraph(Graph graph) {
                    if (graph == importGraph) {
                        if (!opened) {
                            opened = true;
                            GraphManager.getDefault().removeGraphManagerListener(this);
                            PluginExecutor.startWith(ImportExportPluginRegistry.IMPORT_DELIMITED, false)
                                    .set(ImportDelimitedPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                                    .set(ImportDelimitedPlugin.PARSER_PARAMETER_ID, parser)
                                    .set(ImportDelimitedPlugin.FILES_PARAMETER_ID, importFiles)
                                    .set(ImportDelimitedPlugin.SCHEMA_PARAMETER_ID, schema)
                                    .set(ImportDelimitedPlugin.PARSER_PARAMETER_IDS_PARAMETER_ID, currentParameters)
                                    .executeWriteLater(importGraph);
                        }
                    }
                }

            });
            GraphOpener.getDefault().openGraph(importGraph, "graph");
        } else {
            PluginExecutor.startWith(ImportExportPluginRegistry.IMPORT_DELIMITED, false)
                    .set(ImportDelimitedPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                    .set(ImportDelimitedPlugin.PARSER_PARAMETER_ID, parser)
                    .set(ImportDelimitedPlugin.FILES_PARAMETER_ID, importFiles)
                    .set(ImportDelimitedPlugin.SCHEMA_PARAMETER_ID, schema)
                    .executeWriteLater(importGraph);
        }
    }

    public void cancelImport() {
        stage.close();
    }

    private void updateSampleData() {
        if (sampleFile == null) {
            currentColumns = new String[0];
            currentData = new ArrayList<>();
        } else {
            try {
                currentData = importFileParser.preview(new InputSource(sampleFile), currentParameters, PREVIEW_ROW_LIMIT);
                String[] columns = currentData.isEmpty() ? new String[0] : currentData.get(0);
                currentColumns = new String[columns.length + 1];
                System.arraycopy(columns, 0, currentColumns, 1, columns.length);
                currentColumns[0] = "Row";
            } catch (IOException ex) {
                final NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }

        if (configurationPane != null) {
            configurationPane.setSampleData(currentColumns, currentData);
        }
    }

    public void createNewRun() {
        if (configurationPane != null) {
            configurationPane.createNewRun(displayedVertexAttributes, displayedTransactionAttributes, keys, currentColumns, currentData);
        }
    }

    public void setImportFileParser(final ImportFileParser importFileParser) {
        if (this.importFileParser != importFileParser) {
            this.importFileParser = importFileParser;

            if (importFileParser == null) {
                currentParameters = null;
            } else {
                currentParameters = importFileParser.getParameters(refreshRequest);
                List<InputSource> inputSources = new ArrayList<>();
                for (File file : files) {
                    inputSources.add(new InputSource(file));
                }
                importFileParser.updateParameters(currentParameters, inputSources);
            }
            stage.getSourcePane().setParameters(currentParameters);

            updateSampleData();
        }
    }

    public ImportFileParser getImportFileParser() {
        return importFileParser;
    }

    public boolean isSchemaInitialised() {
        return schemaInitialised;
    }

    public void setSchemaInitialised(final boolean schemaInitialised) {
        this.schemaInitialised = schemaInitialised;
    }

    public boolean isShowAllSchemaAttributesEnabled() {
        return showAllSchemaAttributes;
    }

    public void setShowAllSchemaAttributes(final boolean showAllSchemaAttributes) {
        this.showAllSchemaAttributes = showAllSchemaAttributes;
        importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, showAllSchemaAttributes);
        // TODO: the tick box could have changed but the menu item isn't updated, fix it
    }

    public String[] getCurrentColumns() {
        return currentColumns;
    }

    public List<String[]> getCurrentData() {
        return currentData;
    }

    public Attribute showNewAttributeDialog(final GraphElementType elementType) {
        NewAttributeDialog dialog = new NewAttributeDialog(stage, elementType);
        dialog.showAndWait();
        return dialog.getAttribute();
    }

    public Set<Integer> getKeys() {
        return Collections.unmodifiableSet(keys);
    }
}
