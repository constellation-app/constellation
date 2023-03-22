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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 *
 * @author sirius
 * @param <D>
 */
public abstract class ImportController<D> {

    private static final Logger LOGGER = Logger.getLogger(ImportController.class.getName());

    /**
     * Pseudo-attribute to indicate directed transactions.
     */
    public static final String DIRECTED = "__directed__";

    /**
     * Limit the number of rows shown in the preview.
     */
    protected static final int PREVIEW_ROW_LIMIT = 100;

    protected ImportPane importPane;
    protected List<String[]> currentData = new ArrayList<>();
    protected String[] currentColumns = new String[0];
    protected ConfigurationPane configurationPane;
    protected ImportDestination<D> currentDestination;
    protected boolean schemaInitialised;
    protected boolean showAllSchemaAttributes;
    protected PluginParameters currentParameters;
    protected String attributeFilter = "";
    private boolean skipInvalidRows = false;
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

    private static final Object LOCK = new Object();

    protected ImportController() {
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

    /**
     * Common handling of user alerts/dialogs for the Delimited File Importer.
     *
     * @param header Text to place in header bar (immediately below title bar).
     * @param message Main message to display.
     * @param alertType Type of alert being displayed, range from undefined,
     * info through to warnings and errors.
     */
    public void displayAlert(final String header, final String message, final AlertType alertType) {
        final Alert dialog;
        dialog = new Alert(alertType, "", ButtonType.OK);
        dialog.setTitle("Delimited Importer");
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    public ImportPane getImportPane() {
        return importPane;
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

    public void setDestination(final ImportDestination<D> destination) {
        if (destination != null) {
            currentDestination = destination;
            LOGGER.log(Level.SEVERE, "Destination is NOT null, it is: " + destination.toString());
        } else {
            LOGGER.log(Level.SEVERE, "Destination is null");
        }
        if (currentDestination == null) {
            return;
        }

        // Clearing the manually added attributes removes them when loading a template.
        // The destination is set with clearmanuallyAdded true before loading the
        // template, so there are no other left-over attributes to clear out after
        // loading a template.
        if (clearManuallyAdded) {
            manuallyAddedVertexAttributes.clear();
            manuallyAddedTransactionAttributes.clear();
        }
        keys.clear();

        CompletableFuture.runAsync(() -> {
            final boolean showSchemaAttributes = importExportPrefs.getBoolean(
                    ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES,
                    ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);

            loadAllSchemaAttributes(currentDestination, showSchemaAttributes);
        }).thenRun(() -> {
            if (Platform.isFxApplicationThread()) {
                updateDisplayedAttributes();
            } else {
                Platform.runLater(() -> updateDisplayedAttributes());
            }
        });
    }

    /**
     * Load all the schema attributes of the graph
     *
     * @param destination the destination for the imported data.
     * @param showSchemaAttributes specifies whether schema attributes should be
     * included.
     */
    public void loadAllSchemaAttributes(final ImportDestination<?> destination, final boolean showSchemaAttributes) {
        LOGGER.log(Level.SEVERE, "Loading Schema attributes");
        final Graph graph = destination.getGraph();
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            synchronized (LOCK) {
                updateAutoAddedAttributes(GraphElementType.VERTEX, autoAddedVertexAttributes, rg, showSchemaAttributes);
                updateAutoAddedAttributes(GraphElementType.TRANSACTION, autoAddedTransactionAttributes, rg, showSchemaAttributes);
            }
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
                return autoAddedVertexAttributes.containsKey(label) ? autoAddedVertexAttributes.get(label)
                        : manuallyAddedVertexAttributes.get(label);
            case TRANSACTION:
                return autoAddedTransactionAttributes.containsKey(label) ? autoAddedTransactionAttributes.get(label)
                        : manuallyAddedTransactionAttributes.get(label);
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
    private void updateAutoAddedAttributes(final GraphElementType elementType, final Map<String, Attribute> attributes,
            final GraphReadMethods rg, final boolean showSchemaAttributes) {
        attributes.clear();

        // Add attributes from the graph
        final int attributeCount = rg.getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            final int attributeId = rg.getAttribute(elementType, i);
            final Attribute attribute = new GraphAttribute(rg, attributeId);
            if (elementType == GraphElementType.VERTEX) {
                LOGGER.log(Level.SEVERE, "Loading: " + attribute.getName());
            }
            attributes.put(attribute.getName(), attribute);
        }

        // Add attributes from the schema
        if (showSchemaAttributes && rg.getSchema() != null) {
            final SchemaFactory factory = rg.getSchema().getFactory();
            for (final SchemaAttribute sattr : factory.getRegisteredAttributes(elementType).values()) {
                final Attribute attribute = new GraphAttribute(elementType, sattr.getAttributeType(), sattr.getName(),
                        sattr.getDescription());
                if (!attributes.containsKey(attribute.getName())) {
                    attributes.put(attribute.getName(), attribute);
                }
            }
        }

        // Add pseudo-attributes
        if (elementType == GraphElementType.TRANSACTION) {
            final Attribute attribute = new GraphAttribute(elementType, BooleanAttributeDescription.ATTRIBUTE_NAME,
                    DIRECTED, "Is this transaction directed?");
            attributes.put(attribute.getName(), attribute);
        }

        // Add primary keys
        for (final int key : rg.getPrimaryKey(elementType)) {
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

            synchronized (LOCK) {
                displayedVertexAttributes = createDisplayedAttributes(autoAddedVertexAttributes,
                        manuallyAddedVertexAttributes);
                displayedTransactionAttributes = createDisplayedAttributes(autoAddedTransactionAttributes,
                        manuallyAddedTransactionAttributes);
            }

            //This adds the previously allocated attributes if they are missing
            for (final Attribute attribute : configurationPane.getAllocatedAttributes()) {
                if (attribute.getElementType() == GraphElementType.VERTEX) {
                    if (!displayedVertexAttributes.containsKey(attribute.getName())) {
                        final Attribute newAttribute = new NewAttribute(attribute);
                        displayedVertexAttributes.put(newAttribute.getName(), newAttribute);
                    }
                } else {
                    if (!displayedTransactionAttributes.containsKey(attribute.getName())) {
                        final Attribute newAttribute = new NewAttribute(attribute);
                        displayedTransactionAttributes.put(newAttribute.getName(), newAttribute);
                    }
                }
            }

            configurationPane.setDisplayedAttributes(displayedVertexAttributes, displayedTransactionAttributes, keys);
        }
    }

    private Map<String, Attribute> createDisplayedAttributes(final Map<String, Attribute> autoAddedAttributes,
            final Map<String, Attribute> manuallyAddedAttributes) {
        final Map<String, Attribute> displayedAttributes = new HashMap<>();
        if (StringUtils.isNotBlank(attributeFilter)) {
            for (final String attributeName : autoAddedAttributes.keySet()) {
                if (attributeName.toLowerCase(Locale.ENGLISH).contains(attributeFilter.toLowerCase(Locale.ENGLISH))) {
                    displayedAttributes.put(attributeName, autoAddedAttributes.get(attributeName));
                    LOGGER.log(Level.SEVERE, "Adding to displayedAttributes map: " + attributeName);
                }
            }
            for (final String attributeName : manuallyAddedAttributes.keySet()) {
                if (attributeName.toLowerCase(Locale.ENGLISH).contains(attributeFilter.toLowerCase(Locale.ENGLISH))) {
                    displayedAttributes.put(attributeName, manuallyAddedAttributes.get(attributeName));
                }
            }
        } else {
            displayedAttributes.putAll(manuallyAddedAttributes);
            displayedAttributes.putAll(autoAddedAttributes);

            if (displayedAttributes.containsKey("Type")) {
                LOGGER.log(Level.SEVERE, "Type exists in displayedAttributes map");
            } else {
                LOGGER.log(Level.SEVERE, "Type does NOT exist in displayedAttributes map");
            }
        }
        return displayedAttributes;
    }

    public void createManualAttribute(final Attribute attribute) {
        final Map<String, Attribute> attributes = attribute.getElementType() == GraphElementType.VERTEX
                ? manuallyAddedVertexAttributes : manuallyAddedTransactionAttributes;

        if (!attributes.containsKey(attribute.getName())) {
            attributes.put(attribute.getName(), attribute);

            if (configurationPane != null) {
                updateDisplayedAttributes();
            }
        }
    }

    public String showSetDefaultValueDialog(final String attributeName, final String currentDefaultValue) {
        final DefaultAttributeValueDialog dialog = new DefaultAttributeValueDialog(importPane.getParentWindow(),
                attributeName, currentDefaultValue);
        dialog.showAndWait();
        return dialog.getDefaultValue();
    }

    public ImportDestination<D> getDestination() {
        return currentDestination;
    }

    /**
     * A List&lt;ImportDefinition&gt; where each list element corresponds to a
     * RunPane tab.
     *
     * @param isFilesIncludeHeadersEnabled When true will skip the first row and
     * when false will include the first row
     *
     * @return A List&lt;ImportDefinition&gt; where each list element
     * corresponds to a RunPane tab.
     */
    public List<ImportDefinition> getDefinitions(final boolean isFilesIncludeHeadersEnabled) {
        return configurationPane.createDefinitions(isFilesIncludeHeadersEnabled);
    }

    /**
     * Process the full list of records that will be imported to the graph
     *
     * @throws PluginException
     */
    public abstract void processImport() throws PluginException;

    public void cancelImport() {
        SwingUtilities.invokeLater(() -> importPane.close());
    }

    /**
     * Prepare the sample data used to show in the preview pane
     */
    protected abstract void updateSampleData();

    public void createNewRun() {
        if (configurationPane != null) {
            configurationPane.createNewRun(displayedVertexAttributes, displayedTransactionAttributes, keys,
                    currentColumns, currentData);
        }
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
    }

    public void setAttributeFilter(final String attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public String[] getCurrentColumns() {
        return currentColumns;
    }

    public List<String[]> getCurrentData() {
        return currentData;
    }

    public Set<Integer> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    public void setImportPane(final ImportPane importPane) {
        this.importPane = importPane;
    }

    public boolean isSkipInvalidRows() {
        return skipInvalidRows;
    }

    public void setSkipInvalidRows(final boolean skipInvalidRows) {
        this.skipInvalidRows = skipInvalidRows;
    }

}
