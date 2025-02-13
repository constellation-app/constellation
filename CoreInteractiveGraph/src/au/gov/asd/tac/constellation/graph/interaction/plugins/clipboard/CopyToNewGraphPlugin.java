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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.namedselection.state.NamedSelectionState;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * framework plugin for copying elements to a new graph
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("CopyToNewGraphPlugin=Copy To New Graph")
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class CopyToNewGraphPlugin extends SimpleReadPlugin {

    public static final String COPY_ALL_PARAMETER_ID = PluginParameter.buildId(CopyToNewGraphPlugin.class, "copy_all");
    public static final String COPY_KEYS_PARAMETER_ID = PluginParameter.buildId(CopyToNewGraphPlugin.class, "copy_keys");
    public static final String NEW_SCHEMA_NAME_PARAMETER_ID = PluginParameter.buildId(CopyToNewGraphPlugin.class, "new_schema");
    public static final String NEW_GRAPH_OUTPUT_PARAMETER_ID = PluginParameter.buildId(CopyToNewGraphPlugin.class, "new_graph");

    private Graph copy = null;
    
    private static final Pattern DIGITS_REGEX = Pattern.compile("\\d+");

    @Override
    protected void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final String newSchemaName = parameters.getParameters().get(NEW_SCHEMA_NAME_PARAMETER_ID).getStringValue();
        final boolean copyAll = parameters.getParameters().get(COPY_ALL_PARAMETER_ID).getBooleanValue();
        final boolean copyKeys = parameters.getParameters().get(COPY_KEYS_PARAMETER_ID).getBooleanValue();

        final GraphNode gnode = GraphNode.getGraphNode(rg.getId());
        final String name;
        if (gnode != null) {
            name = nameWithoutNumericSuffix(gnode.getDisplayName());
        } else {
            name = "";
        }

        copy = makeGraph(rg, newSchemaName, copyKeys, copyAll);
        parameters.getParameters().get(NEW_GRAPH_OUTPUT_PARAMETER_ID).setObjectValue(copy);

        GraphOpener.getDefault().openGraph(copy, name);
    }

    public Graph getCopy() {
        return copy;
    }

    /**
     * Remove a numeric suffix after a '_' from a string.
     * <p>
     * When creating a new graph, we want to use the same name. The new graph
     * adds "_%d" to the end, so we remove that bit to avoid a copy of a copy of
     * a copy being called "name_1_2_3".
     *
     * @param name The name to have a possible suffix removed.
     *
     * @return The name with the numeric suffix removed (if present).
     */
    private static String nameWithoutNumericSuffix(String name) {
        final int p = name.lastIndexOf('_');
        if (p != -1) {
            final String suffix = name.substring(p + 1);
            boolean isDigits = !suffix.isEmpty() && DIGITS_REGEX.matcher(suffix).matches();
            if (isDigits) {
                name = name.substring(0, p);
            }
        }

        return name;
    }

    /**
     * Created a new graph containing the selected elements of an existing
     * graph.
     *
     * @param original The graph to copy elements from.
     * @param newSchemaName If not null, create the new graph with this schema;
     * otherwise, use the schema of the original graph.
     * @param copyKeys If true, copy the keys.
     * @param copyAll If true, copy everything regardless of whether it is
     * selected or not.
     *
     * @return The new graph.
     *
     * @throws java.lang.InterruptedException if the process is canceled while
     * it is running.
     */
    public static Graph makeGraph(final GraphReadMethods original, final String newSchemaName, final boolean copyKeys, final boolean copyAll) throws InterruptedException {
        final Schema schema = StringUtils.isNotBlank(newSchemaName) ? SchemaFactoryUtilities.getSchemaFactory(newSchemaName).createSchema() : original.getSchema();
        final Graph dualGraph = new DualGraph(schema == null ? null : schema.getFactory().createSchema());

        final WritableGraph graph = dualGraph.getWritableGraph("Make Graph", true);

        try {

            int vertexSelected = original.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
            int transactionSelected = original.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());

            int[] attributeTranslation = new int[1024];

            // Copy the attributes.
            for (GraphElementType type : GraphElementType.values()) {
                int attributeCount = original.getAttributeCount(type);
                for (int attributePosition = 0; attributePosition < attributeCount; attributePosition++) {
                    int originalAttributeId = original.getAttribute(type, attributePosition);
                    Attribute attribute = new GraphAttribute(original, originalAttributeId);
                    int newAttributeId = graph.addAttribute(type, attribute.getAttributeType(), attribute.getName(), attribute.getDescription(), attribute.getDefaultValue(), null);

                    if (originalAttributeId >= attributeTranslation.length) {
                        attributeTranslation = Arrays.copyOf(attributeTranslation, originalAttributeId * 2);
                    }
                    attributeTranslation[originalAttributeId] = newAttributeId;

                    if (type == GraphElementType.GRAPH) {
                        graph.setObjectValue(newAttributeId, 0, original.getObjectValue(originalAttributeId, 0));
                    }
                }

                if (copyKeys) {
                    final int[] keyAttributes = original.getPrimaryKey(type);
                    if (keyAttributes != null && keyAttributes.length > 0) {
                        for (int i = 0; i < keyAttributes.length; i++) {
                            keyAttributes[i] = attributeTranslation[keyAttributes[i]];
                        }
                        graph.setPrimaryKey(type, keyAttributes);
                    }
                }
            }

            // Copy the named selection state.
            final int namedSelectionAttr = original.getAttribute(GraphElementType.META, NamedSelectionState.ATTRIBUTE_NAME);
            if (namedSelectionAttr != Graph.NOT_FOUND) {
                final Object possibleState = original.getObjectValue(namedSelectionAttr, 0);
                if (possibleState instanceof NamedSelectionState namedSelectionState) {
                    final NamedSelectionState state = new NamedSelectionState(namedSelectionState);
                    graph.setObjectValue(attributeTranslation[namedSelectionAttr], 0, state);
                }
            }

            // Copy the vertices.
            int[] vertexTranslation = new int[original.getVertexCapacity()];
            for (int position = 0; position < original.getVertexCount(); position++) {
                int originalVertex = original.getVertex(position);

                if (copyAll || vertexSelected == Graph.NOT_FOUND || original.getBooleanValue(vertexSelected, originalVertex)) {

                    int newVertex = graph.addVertex();
                    vertexTranslation[originalVertex] = newVertex;

                    for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.VERTEX); attributePosition++) {
                        int originalAttributeId = original.getAttribute(GraphElementType.VERTEX, attributePosition);
                        int newAttributeId = attributeTranslation[originalAttributeId];
                        graph.setObjectValue(newAttributeId, newVertex, original.getObjectValue(originalAttributeId, originalVertex));
                    }
                }
            }

            // Copy the transactions.
            for (int position = 0; position < original.getTransactionCount(); position++) {
                int originalTransaction = original.getTransaction(position);

                if (!copyAll) {
                    if (transactionSelected != Graph.NOT_FOUND && !original.getBooleanValue(transactionSelected, originalTransaction)) {
                        continue;
                    }
                    if (vertexSelected != Graph.NOT_FOUND) {
                        if (!original.getBooleanValue(vertexSelected, original.getTransactionSourceVertex(originalTransaction))) {
                            continue;
                        }
                        if (!original.getBooleanValue(vertexSelected, original.getTransactionDestinationVertex(originalTransaction))) {
                            continue;
                        }
                    }
                }

                int sourceVertex = vertexTranslation[original.getTransactionSourceVertex(originalTransaction)];
                int destinationVertex = vertexTranslation[original.getTransactionDestinationVertex(originalTransaction)];
                boolean directed = original.getTransactionDirection(originalTransaction) < 2;

                int newTransaction = graph.addTransaction(sourceVertex, destinationVertex, directed);

                for (int attributePosition = 0; attributePosition < original.getAttributeCount(GraphElementType.TRANSACTION); attributePosition++) {
                    int originalAttributeId = original.getAttribute(GraphElementType.TRANSACTION, attributePosition);
                    int newAttributeId = attributeTranslation[originalAttributeId];
                    graph.setObjectValue(newAttributeId, newTransaction, original.getObjectValue(originalAttributeId, originalTransaction));
                }
            }
        } finally {
            graph.commit();
        }

        return dualGraph;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        // Specify a new schema.
        // If null, use the schema of the input graph.
        final PluginParameter<StringParameterValue> schemaName = StringParameterType.build(NEW_SCHEMA_NAME_PARAMETER_ID);
        schemaName.setName("New Schema Name");
        schemaName.setDescription("The new schema name, if null use the input schema. The default is null.");
        schemaName.setStringValue("");
        parameters.addParameter(schemaName);

        // If true, copy everything, selected or not.
        final PluginParameter<BooleanParameterValue> copyAll = BooleanParameterType.build(COPY_ALL_PARAMETER_ID);
        copyAll.setName("Copy All");
        copyAll.setDescription("If True, copy everything regardless of selection. The default is False.");
        copyAll.setBooleanValue(false);
        parameters.addParameter(copyAll);

        // If true, copy the keys.
        final PluginParameter<BooleanParameterValue> copyKeys = BooleanParameterType.build(COPY_KEYS_PARAMETER_ID);
        copyKeys.setName("Copy Keys");
        copyKeys.setDescription("If True, copy the keys. The default is True.");
        copyKeys.setBooleanValue(true);
        parameters.addParameter(copyKeys);

        // Output parameter.
        final PluginParameter<ObjectParameterValue> newGraph = ObjectParameterType.build(NEW_GRAPH_OUTPUT_PARAMETER_ID);
        newGraph.setName("New Graph (output)");
        newGraph.setDescription("This parameter is used to store the copied graph");
        parameters.addParameter(newGraph);

        return parameters;
    }
}
