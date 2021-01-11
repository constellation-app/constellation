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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.VertexListInclusionGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"IMPORT"})
@NbBundle.Messages("ImportJDBCPlugin=Import from JDBC Sources")
public class ImportJDBCPlugin extends SimpleEditPlugin {

    /**
     * When an attribute is not assigned to a column, the value is -145355 so
     * its easier to track down if there is an error.
     */
    public static final int ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN = -145355;

    public static final String QUERY_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "query");
    public static final String USERNAME_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "username");
    public static final String PASSWORD_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "password");
    public static final String CONNECTION_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "connection");
    public static final String SCHEMA_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "schema");
    public static final String DEFINITIONS_PARAMETER_ID = PluginParameter.buildId(ImportJDBCPlugin.class, "definitions");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<ObjectParameterValue> connectionParam = ObjectParameterType.build(CONNECTION_PARAMETER_ID);
        connectionParam.setName("Connection");
        connectionParam.setDescription("The connection to use");
        connectionParam.setObjectValue(null);
        params.addParameter(connectionParam);

        final PluginParameter<StringParameterValue> queryParam = StringParameterType.build(QUERY_PARAMETER_ID);
        queryParam.setName("Query");
        queryParam.setDescription("The query to run");
        params.addParameter(queryParam);

        final PluginParameter<StringParameterValue> usernameParam = StringParameterType.build(USERNAME_PARAMETER_ID);
        usernameParam.setName("Username");
        params.addParameter(usernameParam);

        final PluginParameter<PasswordParameterValue> passwordParam = PasswordParameterType.build(PASSWORD_PARAMETER_ID);
        passwordParam.setName("Password");
        passwordParam.setDescription("Password");
        params.addParameter(passwordParam);

        final PluginParameter<BooleanParameterValue> schemaParam = BooleanParameterType.build(SCHEMA_PARAMETER_ID);
        schemaParam.setName("Complete with Schema");
        schemaParam.setDescription("True if the graph should run the schema rules, default is True");
        schemaParam.setBooleanValue(true);
        params.addParameter(schemaParam);

        final PluginParameter<ObjectParameterValue> definitionParam = ObjectParameterType.build(DEFINITIONS_PARAMETER_ID);
        definitionParam.setName("Definitions");
        definitionParam.setDescription("The list of definitions that extend ImportDefinition");
        definitionParam.setObjectValue(null);
        params.addParameter(definitionParam);

        return params;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final JDBCConnection connection = (JDBCConnection) parameters.getParameters().get(CONNECTION_PARAMETER_ID).getObjectValue();
        final String query = parameters.getParameters().get(QUERY_PARAMETER_ID).getStringValue();
        final List<ImportDefinition> definitions = (List<ImportDefinition>) parameters.getParameters().get(DEFINITIONS_PARAMETER_ID).getObjectValue();
        final Boolean initialiseWithSchema = parameters.getParameters().get(SCHEMA_PARAMETER_ID).getBooleanValue();
        final List<Integer> newVertices = new ArrayList<>();
        boolean positionalAtrributesExist = false;

        final String username = parameters.getParameters().get(USERNAME_PARAMETER_ID).getStringValue();
        final String password = parameters.getParameters().get(PASSWORD_PARAMETER_ID).getStringValue();

        if (connection != null && query != null && !query.isBlank()) {
            final List<String[]> data = new ArrayList<>();
            try {
                try (final Connection dbConnection = connection.getConnection(username, password)) {
                    try (final PreparedStatement ps = dbConnection.prepareStatement(query)) {
                        try (final ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                final String[] d = new String[ps.getMetaData().getColumnCount()];
                                for (int i = 0; i < ps.getMetaData().getColumnCount(); i++) {
                                    d[i] = rs.getString(i + 1);
                                }
                                data.add(d);
                            }
                        }
                    }
                }
            } catch (final MalformedURLException | ClassNotFoundException | SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return;
            }

            for (final ImportDefinition definition : definitions) {
                if (definition.getDefinitions(AttributeType.SOURCE_VERTEX).isEmpty()) {
                    if (!definition.getDefinitions(AttributeType.DESTINATION_VERTEX).isEmpty()) {
                        processVertices(definition, graph, data, AttributeType.DESTINATION_VERTEX, initialiseWithSchema, interaction, newVertices);
                    }
                } else if (definition.getDefinitions(AttributeType.DESTINATION_VERTEX).isEmpty()) {
                    processVertices(definition, graph, data, AttributeType.SOURCE_VERTEX, initialiseWithSchema, interaction, newVertices);
                } else {
                    processTransactions(definition, graph, data, initialiseWithSchema, interaction);
                }

                // Determine if a positional attribute has been defined, if so update the overall flag
                final boolean isPositional = attributeDefintionIsPositional(definition.getDefinitions(AttributeType.SOURCE_VERTEX), definition.getDefinitions(AttributeType.DESTINATION_VERTEX));
                positionalAtrributesExist = (positionalAtrributesExist || isPositional);
            }

            // If at least one positional attribute has been received for either the src or destination vertex we will assume that the user is trying to import positions and won't auto arrange
            // the graph. This does mean some nodes could sit on top of each other if multiple nodes have the same coordinates.
            if (!positionalAtrributesExist) {
                interaction.setProgress(1, 1, "Arranging", true);

                graph.validateKey(GraphElementType.VERTEX, true);
                graph.validateKey(GraphElementType.TRANSACTION, true);

                // unfortunately need to arrange with pendants and uncollide because grid arranger works based on selection
                final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(graph, AbstractInclusionGraph.Connections.NONE, newVertices);
                PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                        .followedBy(ArrangementPluginRegistry.PENDANTS)
                        .followedBy(ArrangementPluginRegistry.UNCOLLIDE)
                        .executeNow(vlGraph.getInclusionGraph());
                vlGraph.retrieveCoords();
            }
        }
    }

    // If src or destination attribute definitions have been supplied, check them and return true if any of the positional attributes ('x', 'y', or 'z') are included. Positional
    // arguments allow the import to define where nodes will be placed on graph. If src or destination definitons do not exist then an empty list should be supplied.
    private static boolean attributeDefintionIsPositional(final List<ImportAttributeDefinition> srcAttributeDefinitions, final List<ImportAttributeDefinition> destAttributeDefinitions) {
        // Check if srcAttributeDefintions or destAttributeDefintions contain positional attributes
        return srcAttributeDefinitions.stream().map(attribute -> attribute.getAttribute().getName()).anyMatch(name -> (VisualConcept.VertexAttribute.X.getName().equals(name) || VisualConcept.VertexAttribute.Y.getName().equals(name) || VisualConcept.VertexAttribute.Z.getName().equals(name)))
                || destAttributeDefinitions.stream().map(attribute -> attribute.getAttribute().getName()).anyMatch(name -> (VisualConcept.VertexAttribute.X.getName().equals(name) || VisualConcept.VertexAttribute.Y.getName().equals(name) || VisualConcept.VertexAttribute.Z.getName().equals(name)));
    }

    private static void processVertices(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final AttributeType attributeType, final boolean initialiseWithSchema, final PluginInteraction interaction, final List<Integer> newVertices) throws InterruptedException {
        final List<ImportAttributeDefinition> attributeDefinitions = definition.getDefinitions(attributeType);

        addAttributes(graph, GraphElementType.VERTEX, attributeDefinitions);

        int currentRow = 0;
        final int totalRows = data.size() - definition.getFirstRow();

        final RowFilter filter = definition.getRowFilter();

        for (int i = definition.getFirstRow(); i < data.size(); i++) {
            interaction.setProgress(++currentRow, totalRows, "Importing Vertices", true);

            final String[] row = data.get(i);
            if (filter == null || filter.passesFilter(i, row)) {
                final int vertexId = graph.addVertex();
                newVertices.add(vertexId);

                for (final ImportAttributeDefinition attributeDefinition : attributeDefinitions) {
                    attributeDefinition.setValue(graph, vertexId, row, (i - 1));
                }

                if (initialiseWithSchema && graph.getSchema() != null) {
                    graph.getSchema().completeVertex(graph, vertexId);
                }
            }
        }
    }

    private static void processTransactions(final ImportDefinition definition, final GraphWriteMethods graph, final List<String[]> data, final boolean initialiseWithSchema, final PluginInteraction interaction) throws InterruptedException {
        final List<ImportAttributeDefinition> sourceVertexDefinitions = definition.getDefinitions(AttributeType.SOURCE_VERTEX);
        final List<ImportAttributeDefinition> destinationVertexDefinitions = definition.getDefinitions(AttributeType.DESTINATION_VERTEX);
        final List<ImportAttributeDefinition> transactionDefinitions = definition.getDefinitions(AttributeType.TRANSACTION);

        int directedIx = ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN;
        for (int i = 0; i < transactionDefinitions.size(); i++) {
            if (transactionDefinitions.get(i).getAttribute().getName().equals(ImportController.DIRECTED)) {
                directedIx = transactionDefinitions.get(i).getColumnIndex();
                break;
            }
        }

        addAttributes(graph, GraphElementType.VERTEX, sourceVertexDefinitions);
        addAttributes(graph, GraphElementType.VERTEX, destinationVertexDefinitions);
        addAttributes(graph, GraphElementType.TRANSACTION, transactionDefinitions);

        int currentRow = 0;
        final int totalRows = data.size() - definition.getFirstRow();

        final RowFilter filter = definition.getRowFilter();

        for (int i = definition.getFirstRow(); i < data.size(); i++) {
            interaction.setProgress(++currentRow, totalRows, "Importing Transactions", true);

            final String[] row = data.get(i);

            if (filter == null || filter.passesFilter(i, row)) {
                final int sourceVertexId = graph.addVertex();
                for (final ImportAttributeDefinition attributeDefinition : sourceVertexDefinitions) {
                    attributeDefinition.setValue(graph, sourceVertexId, row, (i - 1));
                }
                if (initialiseWithSchema && graph.getSchema() != null) {
                    graph.getSchema().completeVertex(graph, sourceVertexId);
                }

                final int destinationVertexId = graph.addVertex();
                for (final ImportAttributeDefinition attributeDefinition : destinationVertexDefinitions) {
                    attributeDefinition.setValue(graph, destinationVertexId, row, (i - 1));
                }
                if (initialiseWithSchema && graph.getSchema() != null) {
                    graph.getSchema().completeVertex(graph, destinationVertexId);
                }

                final boolean isDirected = directedIx == ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN || Boolean.parseBoolean(row[directedIx]);
                final int transactionId = graph.addTransaction(sourceVertexId, destinationVertexId, isDirected);
                for (final ImportAttributeDefinition attributeDefinition : transactionDefinitions) {
                    if (attributeDefinition.getOverriddenAttributeId() != Graph.NOT_FOUND) {
                        attributeDefinition.setValue(graph, transactionId, row, (i - 1));
                    }
                }
                if (initialiseWithSchema && graph.getSchema() != null) {
                    graph.getSchema().completeTransaction(graph, transactionId);
                }
            }
        }
    }

    /**
     * Add the attribute to the graph
     *
     * @param graph
     * @param elementType Graph element type
     * @param attributeDefinitions
     */
    private static void addAttributes(final GraphWriteMethods graph, final GraphElementType elementType, final List<ImportAttributeDefinition> attributeDefinitions) {
        attributeDefinitions.forEach(attributeDefinition -> {
            final Attribute attribute = attributeDefinition.getAttribute();
            // If the attribute is not assigned to a column but has a default
            // value defined or, the attribute is assigned to a column; add
            // the attribute to the graph and store the attribute id
            if ((attributeDefinition.getColumnIndex() == ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN && attributeDefinition.getDefaultValue() != null)
                    || (attributeDefinition.getColumnIndex() != ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)) {
                int attributeId = graph.getSchema() != null
                        ? graph.getSchema().getFactory().ensureAttribute(graph, elementType, attribute.getName())
                        : Graph.NOT_FOUND;
                if (attributeId == Graph.NOT_FOUND) {
                    attributeId = graph.addAttribute(elementType, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(),
                            attribute.getDefaultValue(), attribute.getAttributeMerger() == null ? null : attribute.getAttributeMerger().getId());
                }
                attributeDefinition.setOverriddenAttributeId(attributeId);
            }
        });
    }
}
