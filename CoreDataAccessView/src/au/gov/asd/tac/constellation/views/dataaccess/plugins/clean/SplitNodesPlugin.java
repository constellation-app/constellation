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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
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
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The Split Nodes Plugin replaces a node with a set of new replicated nodes 
 * containing identifier that comprises the originals nodes identifier. 
 * The Plugin takes a selection of Nodes and a String representing a sub-string contained in the nodes identifier. 
 * The selected node is then replicated so that the new nodes contain the identifiers resulting from 
 * splitting the original identifier with the sub-string.
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@NbBundle.Messages("SplitNodesPlugin=Split Nodes Based on Identifier")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class SplitNodesPlugin extends SimpleEditPlugin implements DataAccessPlugin {

    public static final String SPLIT_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "split");
    public static final String DUPLICATE_TRANSACTIONS_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "split_level");
    public static final String TRANSACTION_TYPE_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "transaction_type");
    public static final String ALL_OCCURRENCES_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "all_occurances");
    public static final String COMPLETE_WITH_SCHEMA_OPTION_ID = PluginParameter.buildId(SplitNodesPlugin.class, "complete_schema");

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
        params.addParameter(split);

        final PluginParameter<BooleanParameterValue> duplicateTransactionsParameter = BooleanParameterType.build(DUPLICATE_TRANSACTIONS_PARAMETER_ID);
        duplicateTransactionsParameter.setName("Duplicate Transactions");
        duplicateTransactionsParameter.setDescription("Duplicate transactions for split nodes");
        params.addParameter(duplicateTransactionsParameter);

        final PluginParameter<SingleChoiceParameterValue> transactionType = SingleChoiceParameterType.build(TRANSACTION_TYPE_PARAMETER_ID);
        transactionType.setName("Transaction Type");
        transactionType.setDescription("Set the type of transaction between nodes");
        params.addParameter(transactionType);

        final PluginParameter<BooleanParameterValue> allOccurrences = BooleanParameterType.build(ALL_OCCURRENCES_PARAMETER_ID);
        allOccurrences.setName("Split on All Occurrences");
        allOccurrences.setDescription("Choose to split on all instances of the character(s) rather than just the first instance");
        params.addParameter(allOccurrences);

        final PluginParameter<BooleanParameterValue> completeSchema = BooleanParameterType.build(COMPLETE_WITH_SCHEMA_OPTION_ID);
        completeSchema.setName("Complete with Schema");
        completeSchema.setDescription("Choose to apply the type schema to the graph");
        completeSchema.setBooleanValue(true);
        params.addParameter(completeSchema);

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
            if (!types.isEmpty() && transactionType.getSingleChoice() == null) {
                final String correlationTypeName = AnalyticConcept.TransactionType.CORRELATION.getName();
                // set the transaction type to Correlation if it exists and the first option otherwise
                SingleChoiceParameterType.setChoice(transactionType, types.contains(correlationTypeName) ? correlationTypeName : types.get(0));
            }
            transactionType.suppressEvent(false, new ArrayList<>());

            transactionType.setObjectValue(parameters.getObjectValue(TRANSACTION_TYPE_PARAMETER_ID));

        }
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {        
        
        // Retrieve PluginParameter values 
        final String character = parameters.getParameters().get(SPLIT_PARAMETER_ID) != null && parameters.getParameters().get(SPLIT_PARAMETER_ID).getStringValue() != null ? parameters.getParameters().get(SPLIT_PARAMETER_ID).getStringValue() : "";
        final ParameterValue transactionTypeChoice = parameters.getParameters().get(TRANSACTION_TYPE_PARAMETER_ID).getSingleChoice();
        final boolean allOccurrences = parameters.getParameters().get(ALL_OCCURRENCES_PARAMETER_ID).getBooleanValue();
        final boolean splitIntoSameLevel = parameters.getParameters().get(DUPLICATE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        final boolean completeWithSchema = parameters.getParameters().get(COMPLETE_WITH_SCHEMA_OPTION_ID).getBooleanValue();
        
        final String linkType = transactionTypeChoice != null ? transactionTypeChoice.toString() : AnalyticConcept.TransactionType.CORRELATION.getName();        
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);

        // Local process-tracking varables (Process is indeteminate until quantity of merged nodes is known)
        int currentProcessStep = 0;
        int totalProcessSteps = -1; 
        
        final GraphRecordStore allSelectedNodes = GraphRecordStoreUtilities.getSelectedVertices(graph);            
        interaction.setProgressTimestamp(true);
        interaction.setProgress(
                currentProcessStep, 
                totalProcessSteps, 
                "Splitting nodes...", 
                true,
                parameters,
                allSelectedNodes.size()
        );
        
        //Determine the positions of the selected nodes
        final List<Integer> selectedVerticies = new ArrayList<>();    
        final int graphVertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < graphVertexCount; vertexPosition++) {
            final int currentVertexId = graph.getVertex(vertexPosition);
            if (graph.getBooleanValue(vertexSelectedAttributeId, currentVertexId)) {
                selectedVerticies.add(vertexPosition);
            }
        }
        
        totalProcessSteps = selectedVerticies.size();
        
        // Loop through the selected vertices and determine how many new verticies need to be added to the graph
        final List<Integer> newVertices = new ArrayList<>();
        for (final int position : selectedVerticies) {
            interaction.setProgress(
                    ++currentProcessStep, 
                    totalProcessSteps, 
                    true
            );
            
            // Check that the current vertex contains the split string being searched for.
            final int currentVertexId = graph.getVertex(position);
            final String identifier = graph.getStringValue(vertexIdentifierAttributeId, currentVertexId);
            if (identifier != null && identifier.contains(character) && identifier.indexOf(character) <= identifier.length() - character.length()) {
                int createdNodesCount = 0;
                String leftNodeIdentifier = ""; // Represents the string to the left of the split. The original node will be renamed to this value
                
                // Split the identifier for each time the split string is seen.
                if (allOccurrences) {
                    final String[] substrings = Arrays.stream(identifier.split(character))
                            .filter(value
                                    -> value != null && value.length() > 0
                            )
                            .toArray(size -> new String[size]);
                    
                    if (substrings.length <= 0) {
                        continue;
                    }
                    
                    leftNodeIdentifier = substrings[0];

                    for (int i = 1; i < substrings.length; i++) {
                        newVertices.add(createNewNode(graph, position, substrings[i], linkType, splitIntoSameLevel));
                        createdNodesCount++;
                    }

                // Split the identifier for the first time the split string is seen.
                } else {
                    final int i = identifier.indexOf(character);
                    leftNodeIdentifier = identifier.substring(0, i);

                    // There was text found on the left side of the split so ignore it and set the node for the right side
                    if (StringUtils.isNotBlank(leftNodeIdentifier)) {
                        leftNodeIdentifier = identifier.substring(0, i);newVertices.add(createNewNode(graph, position, identifier.substring(i + 1), linkType, splitIntoSameLevel));
                        createdNodesCount++;

                    // There was no text on the left side of the split so set the leftNodeIdentifier to the right side. 
                    } else {
                        leftNodeIdentifier = identifier.substring(i + 1);
                        createdNodesCount++;
                    }
                }
                
                // Lastly rename the selected node to the leftNodeIdentifier
                if (StringUtils.isNotBlank(leftNodeIdentifier)) {
                    graph.setStringValue(vertexIdentifierAttributeId, currentVertexId, leftNodeIdentifier);
                    newVertices.add(currentVertexId);
                    interaction.setProgress(currentProcessStep, 
                            totalProcessSteps, 
                            String.format("Node %s split into %s", 
                                    identifier,
                                    PluginReportUtilities.getNodeCountString(++createdNodesCount)
                            ), 
                            true
                    );
                }
            }
        }
        
        //New vertexes were made so add them to the graph
        if (!newVertices.isEmpty()) {
            graph.validateKey(GraphElementType.VERTEX, true);
            graph.validateKey(GraphElementType.TRANSACTION, true);
            final PluginExecutor arrangement = completionArrangement();
            
            if (arrangement != null) {
                // run the arrangement
                final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(graph, AbstractInclusionGraph.Connections.NONE, newVertices);
                arrangement.executeNow(vlGraph.getInclusionGraph());
                vlGraph.retrieveCoords();
            }
            
            if (completeWithSchema) {
                PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
            }
            
            PluginExecutor.startWith(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        }
        
        // Set process to complete
        totalProcessSteps = 0;
        interaction.setProgress(currentProcessStep, 
                totalProcessSteps, 
                String.format("Created %s.", 
                        PluginReportUtilities.getNodeCountString(newVertices.size())
                ), 
                true
        );
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
                } else {
                    // Do nothing
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
