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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.VertexListInclusionGraph;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Split nodes based on
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@NbBundle.Messages("SplitNodesPlugin=Split Nodes Based on Identifier")
public class SplitNodesPlugin extends SimpleEditPlugin implements DataAccessPlugin {

    public static final String SPLIT_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "split");
    public static final String DUPLICATE_TRANSACTIONS_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "split_level");
    public static final String TRANSACTION_TYPE_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "transaction_type");
    public static final String ALL_OCCURRENCES_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "all_occurances");

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 10000;
    }

    @Override
    public String getDescription() {
        return "Split nodes from character(s) in identifier.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> split = StringParameterType.build(SPLIT_PARAMETER_ID);
        split.setName("Split Character(s)");
        split.setDescription("A new term will be extracted from the first instance of this character(s) in the Identifier");
        split.setStringValue(null);
        params.addParameter(split);

        final PluginParameter<BooleanParameterValue> duplicateTransactionsParameter = BooleanParameterType.build(DUPLICATE_TRANSACTIONS_PARAMETER_ID);
        duplicateTransactionsParameter.setName("Duplicate transactions");
        duplicateTransactionsParameter.setDescription("Duplicate transactions for split nodes");
        duplicateTransactionsParameter.setBooleanValue(false);
        params.addParameter(duplicateTransactionsParameter);

        final PluginParameter<SingleChoiceParameterValue> transactionType = SingleChoiceParameterType.build(TRANSACTION_TYPE_PARAMETER_ID);
        transactionType.setName("Transaction Type");
        transactionType.setDescription("Set the type of transaction between nodes");
        params.addParameter(transactionType);

        final PluginParameter<BooleanParameterValue> allOccurrences = BooleanParameterType.build(ALL_OCCURRENCES_PARAMETER_ID);
        allOccurrences.setName("Split on All Occurrences");
        allOccurrences.setDescription("Choose to split on all instances of the character(s) rather than just the first instance");
        allOccurrences.setBooleanValue(false);
        params.addParameter(allOccurrences);

        params.addController(DUPLICATE_TRANSACTIONS_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final boolean splitIntoSameLevel = master.getBooleanValue();
                parameters.get(TRANSACTION_TYPE_PARAMETER_ID).setEnabled(!splitIntoSameLevel);
            }
        });

        return params;

    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        if (parameters != null && parameters.getParameters() != null && !parameters.getParameters().get(DUPLICATE_TRANSACTIONS_PARAMETER_ID).getBooleanValue()) {
            final List<String> types = new ArrayList<>();
            if (graph != null && graph.getSchema() != null) {
                for (final SchemaTransactionType type : SchemaTransactionTypeUtilities.getTypes(graph.getSchema().getFactory().getRegisteredConcepts())) {
                    types.add(type.getName());
                }
                if (!types.isEmpty()) {
                    types.sort(String::compareTo);
                }
            }
            @SuppressWarnings("unchecked") //TRANSACTION_TYPE_PARAMETER is always of type SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> transactionType = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(TRANSACTION_TYPE_PARAMETER_ID);

            SingleChoiceParameterType.setOptions(transactionType, types);
            transactionType.suppressEvent(true, new ArrayList<>());
            if (transactionType.getSingleChoice() == null && types.contains(AnalyticConcept.TransactionType.CORRELATION.getName())) {
                SingleChoiceParameterType.setChoice(transactionType, AnalyticConcept.TransactionType.CORRELATION.getName());
            }
            transactionType.suppressEvent(false, new ArrayList<>());

            transactionType.setObjectValue(parameters.getObjectValue(TRANSACTION_TYPE_PARAMETER_ID));

        }
    }

    private int createNewNode(final GraphWriteMethods graph, final int selectedNode, final String newNodeIdentifier, final String linkType, final boolean splitIntoSameLevel) {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);

        // Add new node
        final int newVertexId = graph.addVertex();

        //Loops through all of the Node attributes and copy them to the new node
        for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
            final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);
            final String vertexAttributeName = graph.getAttributeName(vertexAttributeId);

            if (vertexAttributeName.equals(VisualConcept.VertexAttribute.IDENTIFIER.getName())) {
                graph.setStringValue(vertexIdentifierAttributeId, newVertexId, newNodeIdentifier);
            } else if ((VisualConcept.VertexAttribute.X.getName()).equals(vertexAttributeName)) {
                //The X coordinate is skipped so that the second node is not created in the exact same location
                //as the first node. Copying the Y & Z for a better arrangement as it looks less cluttered.
            } else {
                graph.getNativeAttributeType(vertexAttributeId).copyAttributeValue(graph, vertexAttributeId, selectedNode, newVertexId);
            }
        }

        // Add Transactions
        if (splitIntoSameLevel) {
            final int transactionCount = graph.getVertexTransactionCount(selectedNode);
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int originalTransactionId = graph.getVertexTransaction(selectedNode, transactionPosition);

                final int sourceVertex = graph.getTransactionSourceVertex(originalTransactionId);
                final int destinationVertex = graph.getTransactionDestinationVertex(originalTransactionId);
                int newTransactionId = 0;
                final Boolean directed = graph.getBooleanValue(transactionDirectedAttribute, originalTransactionId);

                if (sourceVertex == selectedNode && destinationVertex == selectedNode) {
                    newTransactionId = graph.addTransaction(newVertexId, newVertexId, directed);
                } else if (sourceVertex == selectedNode) {
                    newTransactionId = graph.addTransaction(newVertexId, destinationVertex, directed);
                } else if (destinationVertex == selectedNode) {
                    newTransactionId = graph.addTransaction(sourceVertex, newVertexId, directed);
                }

                //Loops through all the transaction attributes and copy them to the new transaction
                final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
                for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                    final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);
                    graph.getNativeAttributeType(transactionAttributeId).copyAttributeValue(graph, transactionAttributeId, originalTransactionId, newTransactionId);
                }
            }
        } else {
            //Add a transaction of the type 'linkType' between the selected node and new node
            final int newTransactionId = graph.addTransaction(selectedNode, newVertexId, false);
            graph.setStringValue(transactionTypeAttributeId, newTransactionId, linkType);
        }
        return newVertexId;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Map<String, PluginParameter<?>> splitParameters = parameters.getParameters();
        final String character = splitParameters.get(SPLIT_PARAMETER_ID) != null && splitParameters.get(SPLIT_PARAMETER_ID).getStringValue() != null ? splitParameters.get(SPLIT_PARAMETER_ID).getStringValue() : "";
        final ParameterValue transactionTypeChoice = splitParameters.get(TRANSACTION_TYPE_PARAMETER_ID).getSingleChoice();
        final String linkType = transactionTypeChoice != null ? transactionTypeChoice.toString() : AnalyticConcept.TransactionType.CORRELATION.getName();
        final boolean allOccurrences = splitParameters.get(ALL_OCCURRENCES_PARAMETER_ID).getBooleanValue();
        final boolean splitIntoSameLevel = splitParameters.get(DUPLICATE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final List<Integer> newVertices = new ArrayList<>();
        final int graphVertexCount = graph.getVertexCount();

        for (int position = 0; position < graphVertexCount; position++) {
            final int currentVertexId = graph.getVertex(position);

            if (graph.getBooleanValue(vertexSelectedAttributeId, currentVertexId)) {
                final String identifier = graph.getStringValue(vertexIdentifierAttributeId, currentVertexId);

                if (identifier != null && identifier.contains(character) && identifier.indexOf(character) < identifier.length() - character.length()) {
                    String leftNodeIdentifier = "";
                    if (allOccurrences) {
                        final String[] substrings = identifier.split(character);
                        if (substrings.length <= 0) {
                            continue;
                        }

                        leftNodeIdentifier = substrings[0];

                        for (int i = 1; i < substrings.length; i++) {
                            newVertices.add(createNewNode(graph, position, substrings[i], linkType, splitIntoSameLevel));
                        }

                    } else {
                        final int i = identifier.indexOf(character);
                        leftNodeIdentifier = identifier.substring(0, i);
                        newVertices.add(createNewNode(graph, position, identifier.substring(i + 1), linkType, splitIntoSameLevel));
                    }
                    // Rename the selected node
                    graph.setStringValue(vertexIdentifierAttributeId, currentVertexId, leftNodeIdentifier);
                }
            }
        }
        if (!newVertices.isEmpty()) {
            // Reset the view
            graph.validateKey(GraphElementType.VERTEX, true);
            graph.validateKey(GraphElementType.TRANSACTION, true);

            final PluginExecutor arrangement = completionArrangement();
            if (arrangement != null) {
                // run the arrangement
                final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(graph, AbstractInclusionGraph.Connections.NONE, newVertices);
                arrangement.executeNow(vlGraph.getInclusionGraph());
                vlGraph.retrieveCoords();
            }

            PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
            PluginExecutor.startWith(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        }
    }

    /**
     * The arrangement to be done after the plugin completes.
     * <p>
     * Some plugins result in new nodes being created in a graph. In order to
     * make these nodes visible to the user, the new nodes should undergo a
     * default arrangement.
     * <p>
     * Be cautious when specifying no arrangement. Due to various graphics
     * card/driver quirks, we can't leave the nodes at (0,0,0). It's easy to
     * crash the display this way. Also, we don't want to crush the new nodes
     * too close together: the camera will zoom in and make them bigger and
     * slower to draw. Therefore it is highly recommended that plugins do not
     * specify no arrangement unless they know what they're doing.
     * <p>
     * Note: PluginExecutors should be arrangements. Do the sensible thing when
     * overriding.
     *
     * @return A PluginExecutor that does an arrangement, or null if no
     * arrangement is to be done.
     */
    public PluginExecutor completionArrangement() {
        return PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                .followedBy(ArrangementPluginRegistry.PENDANTS)
                .followedBy(ArrangementPluginRegistry.UNCOLLIDE);
    }
}
