/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDestination;
import au.gov.asd.tac.constellation.plugins.importexport.NewAttribute;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public class ImportController {

    /**
     * Pseudo-attribute to indicate directed transactions.
     */
    public static final String DIRECTED = "__directed__";

    /**
     * Limit the number of rows shown in the preview.
     */
    private static final int PREVIEW_ROW_LIMIT = 100;

    private final JDBCImporterStage stage;
    private JDBCConnection connection;
    private String query;
    private String username;
    private String password;
    private List<String[]> currentData = new ArrayList<>();
    private String[] currentColumns = new String[0];
    private ConfigurationPane configurationPane;
    private boolean schemaInitialised;
    private String attributeFilter = "";

    private ImportDestination<?> currentDestination;
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

    public ImportController(final JDBCImporterStage stage) {
        this.stage = stage;
        schemaInitialised = true;

        autoAddedVertexAttributes = new HashMap<>();
        autoAddedTransactionAttributes = new HashMap<>();

        manuallyAddedVertexAttributes = new HashMap<>();
        manuallyAddedTransactionAttributes = new HashMap<>();
        clearManuallyAdded = true;

        displayedVertexAttributes = new HashMap<>();
        displayedTransactionAttributes = new HashMap<>();

        keys = new HashSet<>();
    }

    public JDBCImporterStage getStage() {
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

    public boolean hasDBConnection() {
        return !(connection == null);
    }

    public void setDBConnection(final JDBCConnection connection) {
        this.connection = connection;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
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
        if (destination != null) {
            currentDestination = destination;
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

        final boolean showSchemaAttributes = true;
        loadAllSchemaAttributes(currentDestination, showSchemaAttributes);

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
        final int attributeCount = rg.getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            final int attributeId = rg.getAttribute(elementType, i);
            final Attribute attribute = new GraphAttribute(rg, attributeId);
            attributes.put(attribute.getName(), attribute);
        }

        // Add attributes from the schema
        if (showSchemaAttributes && rg.getSchema() != null) {
            final SchemaFactory factory = rg.getSchema().getFactory();
            factory.getRegisteredAttributes(elementType).values().stream().map(sattr -> new GraphAttribute(elementType, sattr.getAttributeType(), sattr.getName(), sattr.getDescription())).filter(attribute -> (!attributes.containsKey(attribute.getName()))).forEachOrdered(attribute -> {
                attributes.put(attribute.getName(), attribute);
            });
        }

        // Add pseudo-attributes
        if (elementType == GraphElementType.TRANSACTION) {
            final Attribute attribute = new GraphAttribute(elementType, BooleanAttributeDescription.ATTRIBUTE_NAME, DIRECTED, "Is this transaction directed?");
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

            displayedVertexAttributes = createDisplayedAttributes(autoAddedVertexAttributes, manuallyAddedVertexAttributes);
            displayedTransactionAttributes = createDisplayedAttributes(autoAddedTransactionAttributes, manuallyAddedTransactionAttributes);

            configurationPane.getAllocatedAttributes().forEach(attribute -> {
                if (attribute.getElementType() == GraphElementType.VERTEX) {
                    if (!displayedVertexAttributes.containsKey(attribute.getName())) {
                        final Attribute newAttribute = new NewAttribute(attribute);
                        displayedVertexAttributes.put(newAttribute.getName(), newAttribute);
                    }
                } else if (!displayedTransactionAttributes.containsKey(attribute.getName())) {
                    final Attribute newAttribute = new NewAttribute(attribute);
                    displayedTransactionAttributes.put(newAttribute.getName(), newAttribute);
                }
            });

            configurationPane.setDisplayedAttributes(displayedVertexAttributes, displayedTransactionAttributes, keys);
        }
    }

    private Map<String, Attribute> createDisplayedAttributes(final Map<String, Attribute> autoAddedAttributes, final Map<String, Attribute> manuallyAddedAttributes) {
        final Map<String, Attribute> displayedAttributes = new HashMap<>();
        if (attributeFilter != null && attributeFilter.length() > 0) {
            autoAddedAttributes.keySet().stream().filter(attributeName -> (attributeName.toLowerCase().contains(attributeFilter.toLowerCase()))).forEachOrdered(attributeName -> {
                displayedAttributes.put(attributeName, autoAddedAttributes.get(attributeName));
            });
            manuallyAddedAttributes.keySet().stream().filter(attributeName -> (attributeName.toLowerCase().contains(attributeFilter.toLowerCase()))).forEachOrdered(attributeName -> {
                displayedAttributes.put(attributeName, manuallyAddedAttributes.get(attributeName));
            });
        } else {
            displayedAttributes.putAll(manuallyAddedAttributes);
            displayedAttributes.putAll(autoAddedAttributes);
        }
        return displayedAttributes;
    }

    public void createManualAttribute(final Attribute attribute) {
        final Map<String, Attribute> attributes = attribute.getElementType() == GraphElementType.VERTEX ? manuallyAddedVertexAttributes : manuallyAddedTransactionAttributes;

        if (!attributes.containsKey(attribute.getName())) {
            attributes.put(attribute.getName(), attribute);

            if (configurationPane != null) {
                updateDisplayedAttributes();
            }
        }
    }

    public String showSetDefaultValueDialog(final String attributeName, final String currentDefaultValue) {
        final DefaultAttributeValueDialog dialog = new DefaultAttributeValueDialog(stage, attributeName, currentDefaultValue);
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
                    if (graph == importGraph && !opened) {
                        opened = true;
                        GraphManager.getDefault().removeGraphManagerListener(this);
                        PluginExecutor.startWith(ImportJDBCPlugin.class.getName(), false)
                                .set(ImportJDBCPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                                .set(ImportJDBCPlugin.CONNECTION_PARAMETER_ID, connection)
                                .set(ImportJDBCPlugin.SCHEMA_PARAMETER_ID, schema)
                                .set(ImportJDBCPlugin.QUERY_PARAMETER_ID, query)
                                .set(ImportJDBCPlugin.USERNAME_PARAMETER_ID, username)
                                .set(ImportJDBCPlugin.PASSWORD_PARAMETER_ID, password)
                                .executeWriteLater(importGraph);
                    }
                }

            });
            GraphOpener.getDefault().openGraph(importGraph, "graph");
        } else {
            PluginExecutor.startWith(ImportJDBCPlugin.class.getName(), false)
                    .set(ImportJDBCPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                    .set(ImportJDBCPlugin.CONNECTION_PARAMETER_ID, connection)
                    .set(ImportJDBCPlugin.QUERY_PARAMETER_ID, query)
                    .set(ImportJDBCPlugin.USERNAME_PARAMETER_ID, username)
                    .set(ImportJDBCPlugin.PASSWORD_PARAMETER_ID, password)
                    .set(ImportJDBCPlugin.SCHEMA_PARAMETER_ID, schema)
                    .executeWriteLater(importGraph);
        }
    }

    public void cancelImport() {
        stage.close();
    }

    public void updateSampleData() {
        if (connection == null || query == null || query.isBlank()) {
            currentColumns = new String[0];
            currentData = new ArrayList<>();
        } else {
            try {
                try (final Connection dbConnection = connection.getConnection(username, password)) {
                    if (!query.toLowerCase().contains(" limit ") && query.toLowerCase().startsWith("select ")) {
                        query += " limit " + PREVIEW_ROW_LIMIT;
                    }
                    try (final PreparedStatement ps = dbConnection.prepareStatement(query)) {
                        try (final ResultSet rs = ps.executeQuery()) {
                            int count = 0;
                            currentData.clear();
                            while (rs.next() && count < PREVIEW_ROW_LIMIT) {
                                count++;
                                final String[] d = new String[ps.getMetaData().getColumnCount()];
                                for (int i = 0; i < ps.getMetaData().getColumnCount(); i++) {
                                    d[i] = rs.getString(i + 1);
                                }
                                currentData.add(d);
                            }
                            currentColumns = new String[ps.getMetaData().getColumnCount() + 1];
                            currentColumns[0] = "Row";
                            final ResultSetMetaData rsmd = ps.getMetaData();
                            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                                currentColumns[i + 1] = rsmd.getColumnName(i + 1);
                            }
                        }
                    }
                }
            } catch (final MalformedURLException | ClassNotFoundException | SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                final Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Query error");
                alert.setResizable(true);
                final TextArea ta = new TextArea(ex.getMessage());
                ta.setEditable(false);
                ta.setWrapText(true);
                alert.getDialogPane().setContent(ta);
                alert.showAndWait();
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

    public boolean isSchemaInitialised() {
        return schemaInitialised;
    }

    public void setSchemaInitialised(final boolean schemaInitialised) {
        this.schemaInitialised = schemaInitialised;
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

    public Attribute showNewAttributeDialog(final GraphElementType elementType) {
        final NewAttributeDialog dialog = new NewAttributeDialog(stage, elementType);
        dialog.showAndWait();
        return dialog.getAttribute();
    }

    public Set<Integer> getKeys() {
        return Collections.unmodifiableSet(keys);
    }
}
