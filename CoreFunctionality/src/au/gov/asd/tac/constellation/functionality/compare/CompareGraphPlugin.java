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
package au.gov.asd.tac.constellation.functionality.compare;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.graph.utilities.PrimaryKeyUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Compare two graphs
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("CompareGraphPlugin=Compare Graph")
@PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
public class CompareGraphPlugin extends SimpleReadPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(CompareGraphPlugin.class.getName());

    // plugin parameters
    public static final String ORIGINAL_GRAPH_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "original_graph_name");
    public static final String COMPARE_GRAPH_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "compare_graph_name");
    public static final String IGNORE_VERTEX_ATTRIBUTES_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "ignore_vertex_attribites");
    public static final String IGNORE_TRANSACTION_ATTRIBUTES_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "ignore_transaction_attribites");
    public static final String ADDED_COLOUR_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "added_colour");
    public static final String REMOVED_COLOUR_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "removed_colour");
    public static final String CHANGED_COLOUR_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "changed_colour");
    public static final String UNCHANGED_COLOUR_PARAMETER_ID = PluginParameter.buildId(CompareGraphPlugin.class, "unchanged_colour");

    public static final String GLOBAL_MODIFICATION_COUNT = "Global Modification Count";
    public static final String NODES_COUNT = "Nodes Count";
    public static final String TRANSACTIONS_COUNT = "Transactions Count";
    public static final String NODE_ATTRIBUTE_COUNT = "Node Attribute Count";
    public static final String TRANSACTION_ATTRIBUTE_COUNT = "Transaction Attribute Count";

    // attribute
    public static final String COMPARE_ATTRIBUTE = "Compare";

    // attribute values
    public static final String ADDED = "Added";
    public static final String REMOVED = "Removed";
    public static final String CHANGED = "Changed";
    public static final String UNCHANGED = "Unchanged";

    // messages
    private static final String GRAPH_NOT_FOUND_ERROR = "Graph %s not found.";

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> originalGraph = SingleChoiceParameterType.build(ORIGINAL_GRAPH_PARAMETER_ID);
        originalGraph.setName("Original Graph");
        originalGraph.setDescription("The graph used as the starting point for the comparison");
        parameters.addParameter(originalGraph);
        
        // Controller listens for value change so that the compare graph cannot be compared against itself
        parameters.addController(ORIGINAL_GRAPH_PARAMETER_ID, (PluginParameter<?> master, Map<String, PluginParameter<?>> params, ParameterChange change) -> {
            // When value has changed, remove choice from the comparison graph dialog
            if (change == ParameterChange.VALUE) {
                final String originalGraphName = params.get(ORIGINAL_GRAPH_PARAMETER_ID).getStringValue();
                if (originalGraphName != null) {
                    
                    final List<String> graphNames = new ArrayList<>();
                    final Map<String, Graph> allGraphs = GraphNode.getAllGraphs();
                    if (allGraphs != null) {
                        for (final String graphId : allGraphs.keySet()) {
                            graphNames.add(GraphNode.getGraphNode(graphId).getDisplayName());
                        }
                    }
                    // remove the current original graph selection from the list of graphs allowed to compare with
                    graphNames.remove(originalGraphName);
                    
                    // sort drop down list
                    graphNames.sort(String::compareTo);
                    
                          
                    @SuppressWarnings("unchecked") //COMPARE_GRAPH_PARAMETER_ID will always be of type SingleChoiceParameterValue
                    final PluginParameter<SingleChoiceParameterValue> compareParamter = (PluginParameter<SingleChoiceParameterValue>) params.get(COMPARE_GRAPH_PARAMETER_ID);
                    SingleChoiceParameterType.setOptions(compareParamter, graphNames);
                }
            }
        });

        final PluginParameter<SingleChoiceParameterValue> compareGraph = SingleChoiceParameterType.build(COMPARE_GRAPH_PARAMETER_ID);
        compareGraph.setName("Compare With Graph");
        compareGraph.setDescription("The graph used to compare against the original graph");
        parameters.addParameter(compareGraph);
        
        final PluginParameter<MultiChoiceParameterValue> ignoreVertexAttributes = MultiChoiceParameterType.build(IGNORE_VERTEX_ATTRIBUTES_PARAMETER_ID);
        ignoreVertexAttributes.setName("Ignore Node Attributes");
        ignoreVertexAttributes.setDescription("Ignore these attributes when comparing nodes");
        parameters.addParameter(ignoreVertexAttributes);

        final PluginParameter<MultiChoiceParameterValue> ignoreTransactionAttributes = MultiChoiceParameterType.build(IGNORE_TRANSACTION_ATTRIBUTES_PARAMETER_ID);
        ignoreTransactionAttributes.setName("Ignore Transaction Attributes");
        ignoreTransactionAttributes.setDescription("Ignore these attributes when comparing transactions");
        parameters.addParameter(ignoreTransactionAttributes);

        final PluginParameter<ColorParameterValue> addedColour = ColorParameterType.build(ADDED_COLOUR_PARAMETER_ID);
        addedColour.setName("Added Color");
        addedColour.setDescription("The colour to indicate a node/transaction addition");
        addedColour.setColorValue(ConstellationColor.GREEN);
        parameters.addParameter(addedColour);

        final PluginParameter<ColorParameterValue> removedColour = ColorParameterType.build(REMOVED_COLOUR_PARAMETER_ID);
        removedColour.setName("Removed Color");
        removedColour.setDescription("The colour to indicate a node/transaction removal");
        removedColour.setColorValue(ConstellationColor.RED);
        parameters.addParameter(removedColour);

        final PluginParameter<ColorParameterValue> changedColour = ColorParameterType.build(CHANGED_COLOUR_PARAMETER_ID);
        changedColour.setName("Changed Color");
        changedColour.setDescription("The colour to indicate a node/transaction change");
        changedColour.setColorValue(ConstellationColor.YELLOW);
        parameters.addParameter(changedColour);

        final PluginParameter<ColorParameterValue> unchangedColour = ColorParameterType.build(UNCHANGED_COLOUR_PARAMETER_ID);
        unchangedColour.setName("Unchanged Color");
        unchangedColour.setDescription("The colour to indicate no node/transaction change");
        unchangedColour.setColorValue(ConstellationColor.GREY);
        parameters.addParameter(unchangedColour);

        return parameters;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> graphNames = new ArrayList<>();
        final Map<String, Graph> allGraphs = GraphNode.getAllGraphs();
        if (allGraphs != null) {
            for (final String graphId : allGraphs.keySet()) {
                graphNames.add(GraphNode.getGraphNode(graphId).getDisplayName());
            }
        }

        // sort drop down list
        graphNames.sort(String::compareTo);

        // make a list of attributes that should be ignored.
        final ReadableGraph rg = graph.getReadableGraph();
        final Set<String> registeredVertexAttributes;
        final Set<String> registeredTransactionAttributes;
        try {
            registeredVertexAttributes = AttributeUtilities.getRegisteredAttributeIdsFromGraph(rg, GraphElementType.VERTEX).keySet();
            registeredTransactionAttributes = AttributeUtilities.getRegisteredAttributeIdsFromGraph(rg, GraphElementType.TRANSACTION).keySet();
        } finally {
            rg.release();
        }

        // ignore lowercase attributes
        final List<String> ignoredVertexAttributes = new ArrayList<>();
        for (final String attribute : registeredVertexAttributes) {
            if (attribute.substring(0, 1).matches("[a-z]")) {
                ignoredVertexAttributes.add(attribute);
            }
        }

        final List<String> ignoredTransactionAttributes = new ArrayList<>();
        for (final String attribute : registeredTransactionAttributes) {
            if (attribute.substring(0, 1).matches("[a-z]")) {
                ignoredTransactionAttributes.add(attribute);
            }
        }

        @SuppressWarnings("unchecked") //ORIGINAL_GRAPH_PARAMETER will always be of type SingleChoiceParameter
        final PluginParameter<SingleChoiceParameterValue> originalGraph = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ORIGINAL_GRAPH_PARAMETER_ID);
        SingleChoiceParameterType.setOptions(originalGraph, graphNames);
        SingleChoiceParameterType.setChoice(originalGraph, GraphNode.getGraphNode(graph.getId()).getDisplayName());

//        @SuppressWarnings("unchecked") //COMPARE_GRAPH_PARAMETER will always be of type SingleChoiceParameter
//        final PluginParameter<SingleChoiceParameterValue> compareGraph = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(COMPARE_GRAPH_PARAMETER_ID);
//        SingleChoiceParameterType.setOptions(compareGraph, graphNames);

        @SuppressWarnings("unchecked") //IGNORE_VERTEX_ATTRIBUTES_PARAMETER will always be of type MultiChoiceParameter
        final PluginParameter<MultiChoiceParameterValue> ignoreVertexAttributes = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(IGNORE_VERTEX_ATTRIBUTES_PARAMETER_ID);
        MultiChoiceParameterType.setOptions(ignoreVertexAttributes, new ArrayList<>(registeredVertexAttributes));
        MultiChoiceParameterType.setChoices(ignoreVertexAttributes, ignoredVertexAttributes);

        @SuppressWarnings("unchecked") //IGNORE_TRANSACTION_ATTRIBUTES_PARAMETER will always be of type MultiChoiceParameter
        final PluginParameter<MultiChoiceParameterValue> ignoreTransactionAttributes = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(IGNORE_TRANSACTION_ATTRIBUTES_PARAMETER_ID);
        MultiChoiceParameterType.setOptions(ignoreTransactionAttributes, new ArrayList<>(registeredTransactionAttributes));
        MultiChoiceParameterType.setChoices(ignoreTransactionAttributes, ignoredTransactionAttributes);
    }

    @Override
    public String getDescription() {
        return "Compare two graphs to visualise the differences";
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String originalGraphName = parameters.getParameters().get(ORIGINAL_GRAPH_PARAMETER_ID).getStringValue();
        final String compareGraphName = parameters.getParameters().get(COMPARE_GRAPH_PARAMETER_ID).getStringValue();

        if (originalGraphName == null || compareGraphName == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, "You must choose two graphs to compare");
        }

        final List<String> ignoreVertexAttributes = new ArrayList<>(parameters.getParameters().get(IGNORE_VERTEX_ATTRIBUTES_PARAMETER_ID).getMultiChoiceValue().getChoices());
        final List<String> ignoreTransactionAttributes = new ArrayList<>(parameters.getParameters().get(IGNORE_TRANSACTION_ATTRIBUTES_PARAMETER_ID).getMultiChoiceValue().getChoices());

        final ConstellationColor addedColour = parameters.getParameters().get(ADDED_COLOUR_PARAMETER_ID).getColorValue();
        final ConstellationColor removedColour = parameters.getParameters().get(REMOVED_COLOUR_PARAMETER_ID).getColorValue();
        final ConstellationColor changedColour = parameters.getParameters().get(CHANGED_COLOUR_PARAMETER_ID).getColorValue();
        final ConstellationColor unchangedColour = parameters.getParameters().get(UNCHANGED_COLOUR_PARAMETER_ID).getColorValue();

        final ConstellationColor addedColourValue = ConstellationColor.getColorValue(addedColour.getRed(), addedColour.getGreen(), addedColour.getBlue(), ConstellationColor.ZERO_ALPHA);
        final ConstellationColor removedColourValue = ConstellationColor.getColorValue(removedColour.getRed(), removedColour.getGreen(), removedColour.getBlue(), ConstellationColor.ZERO_ALPHA);
        final ConstellationColor changedColourValue = ConstellationColor.getColorValue(changedColour.getRed(), changedColour.getGreen(), changedColour.getBlue(), ConstellationColor.ZERO_ALPHA);
        final ConstellationColor unchangedColourValue = ConstellationColor.getColorValue(unchangedColour.getRed(), unchangedColour.getGreen(), unchangedColour.getBlue(), ConstellationColor.ZERO_ALPHA);

        final Graph originalGraph = getGraphFromName(originalGraphName);
        final Graph compareGraph = getGraphFromName(compareGraphName);

        if (originalGraph == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(GRAPH_NOT_FOUND_ERROR, originalGraphName));
        }

        if (compareGraph == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(GRAPH_NOT_FOUND_ERROR, compareGraphName));
        }

        final GraphRecordStore originalAll;
        final GraphRecordStore compareAll;

        final Set<String> vertexPrimaryKeys;
        final Set<String> transactionPrimaryKeys;

        // get a copy of the graph's record store and statistical info
        ReadableGraph rg = originalGraph.getReadableGraph();
        try {
            originalAll = GraphRecordStoreUtilities.getAll(rg, false, true);
            vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg, GraphElementType.VERTEX);
            transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg, GraphElementType.TRANSACTION);
        } finally {
            rg.release();
        }

        rg = compareGraph.getReadableGraph();
        try {
            compareAll = GraphRecordStoreUtilities.getAll(rg, false, true);
        } finally {
            rg.release();
        }

        // ignore the id attributes to avoid reporting on them
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        // graph changes
        final String title = String.format("Compare: %s <> %s", originalGraphName, compareGraphName);
        final GraphRecordStore changes = compareGraphs(title, originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, addedColourValue, removedColourValue, changedColourValue, unchangedColourValue);

        // create new graph
        createComparisonGraph(originalGraph, changes);

        interaction.setProgress(1, 0, changes + " changes were found.", true);
    }

    /**
     * Compare 2 graphs and create a {@link GraphRecordStore} to store the
     * differences.
     *
     * @param original The orignal graph
     * @param compare The graph to compare with
     * @param vertexPrimaryKeys Vertex primary keys
     * @param transactionPrimaryKeys Transaction primary keys
     * @param ignoreVertexAttributes Vertex attributes to ignore
     * @return A {@link GraphRecordStore} containing the differences
     * @throws PluginException
     */
    protected GraphRecordStore compareGraphs(final String title, final GraphRecordStore original, final GraphRecordStore compare, final Set<String> vertexPrimaryKeys, final Set<String> transactionPrimaryKeys, final List<String> ignoreVertexAttributes, final List<String> ignoreTransactionAttributes, final ConstellationColor addedColour, final ConstellationColor removedColour, final ConstellationColor changedColour, final ConstellationColor unchangedColour) throws PluginException {
        final GraphRecordStore result = new GraphRecordStore();
        original.reset();
        compare.reset();
        result.add(original);
        result.add(compare);

        final Map<Set<String>, Integer> originalVertexKeysToIndex = getVertexKeysToRecordstoreIndex(original, vertexPrimaryKeys);
        final Map<Set<String>, Integer> compareVertexKeysToIndex = getVertexKeysToRecordstoreIndex(compare, vertexPrimaryKeys);

        // make sure transaction primary keys include the source and destination
        final Map<List<String>, Integer> originalTransactionKeysToIndex = getTransactionKeysToRecordstoreIndex(original, vertexPrimaryKeys, transactionPrimaryKeys);
        final Map<List<String>, Integer> compareTransactionKeysToIndex = getTransactionKeysToRecordstoreIndex(compare, vertexPrimaryKeys, transactionPrimaryKeys);

        final Set<Set<String>> seenVertices = new HashSet<>();
        // TODO: this could be a list of sets
        final Set<List<String>> seenTransactions = new HashSet<>();
        final List<String> attributes = result.keys();

        final Map<String, String> vertexSourceRecordPrimaryValues = new HashMap<>();
        final Map<String, String> vertexDestinationRecordPrimaryValues = new HashMap<>();
        final Map<String, String> transactionRecordPrimaryValues = new HashMap<>();

        // remove ignored attributes
        for (final String attribute : ignoreVertexAttributes) {
            attributes.remove(GraphRecordStoreUtilities.SOURCE + attribute);
            attributes.remove(GraphRecordStoreUtilities.DESTINATION + attribute);
        }

        for (final String attribute : ignoreTransactionAttributes) {
            attributes.remove(GraphRecordStoreUtilities.TRANSACTION + attribute);
        }

        final InputOutput io = IOProvider.getDefault().getIO(title, true);
        io.select();

        final OutputWriter output = io.getOut();

        final GraphRecordStore changes = new GraphRecordStore();
        result.reset();
        while (result.next()) {
            // make a cache of the primary key values
            vertexSourceRecordPrimaryValues.clear();
            vertexDestinationRecordPrimaryValues.clear();
            for (final String key : vertexPrimaryKeys) {
                final String sourceValue = result.get(GraphRecordStoreUtilities.SOURCE + key);
                if (sourceValue != null) {
                    vertexSourceRecordPrimaryValues.put(key, sourceValue);
                }
                final String destinationValue = result.get(GraphRecordStoreUtilities.DESTINATION + key);
                if (destinationValue != null) {
                    vertexDestinationRecordPrimaryValues.put(key, destinationValue);
                }
            }

            transactionRecordPrimaryValues.clear();
            for (final String key : transactionPrimaryKeys) {
                final String value = result.get(GraphRecordStoreUtilities.TRANSACTION + key);
                if (value != null) {
                    transactionRecordPrimaryValues.put(key, value);
                }
            }

            // make a unique vertex
            final Set<String> vertex = new HashSet<>();
            vertex.addAll(vertexSourceRecordPrimaryValues.values());

            // make a unique transaction
            final List<String> transaction = new ArrayList<>();
            transaction.addAll(transactionRecordPrimaryValues.values());
            transaction.addAll(vertexDestinationRecordPrimaryValues.values());
            transaction.addAll(vertexSourceRecordPrimaryValues.values());

            // vertex compare
            final String originalSource = result.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL);
            if (!seenVertices.contains(vertex) && !originalVertexKeysToIndex.containsKey(vertex) && compareVertexKeysToIndex.containsKey(vertex)) { // added
                changes.add();
                changes.set(GraphRecordStoreUtilities.SOURCE + COMPARE_ATTRIBUTE, ADDED);
                changes.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.OVERLAY_COLOR, addedColour);

                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                addAttributesToRecord(result, changes, GraphRecordStoreUtilities.SOURCE, result.index());
                output.println(String.format("Added node %s", originalSource));
                seenVertices.add(vertex);
            } else if (!seenVertices.contains(vertex) && originalVertexKeysToIndex.containsKey(vertex) && !compareVertexKeysToIndex.containsKey(vertex)) { // removed
                changes.add();
                changes.set(GraphRecordStoreUtilities.SOURCE + COMPARE_ATTRIBUTE, REMOVED);
                changes.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.OVERLAY_COLOR, removedColour);

                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                addAttributesToRecord(result, changes, GraphRecordStoreUtilities.SOURCE, result.index());
                output.println(String.format("Removed node %s", originalSource));
                seenVertices.add(vertex);
            } else if (!seenVertices.contains(vertex) && originalVertexKeysToIndex.containsKey(vertex) && compareVertexKeysToIndex.containsKey(vertex)) { // changed
                boolean vertexChanged = false;
                for (final String attribute : attributes) {
                    final int dividerPosition = attribute.indexOf(SeparatorConstants.PERIOD);

                    if (dividerPosition != -1) {
                        final String keyType = attribute.substring(0, dividerPosition).toLowerCase();
                        final String keyAttribute = attribute.substring(dividerPosition + 1);

                        switch (keyType) {
                            case "source":
                                final String originalValue = original.get(originalVertexKeysToIndex.get(vertex), attribute);
                                final String compareValue = compare.get(compareVertexKeysToIndex.get(vertex), attribute);
                                if ((originalValue != null && !originalValue.equals(compareValue))
                                        || (compareValue != null && !compareValue.equals(originalValue))) {
                                    vertexChanged = true;
                                    output.println(String.format("Changed node %s, '%s' value was '%s' and now '%s'", originalSource, keyAttribute, originalValue, compareValue));
                                }
                                break;
                            case "destination":
                            case "transaction":
                                // Intentionally left blank
                                break;
                            default:
                                break;
                        }
                    }
                }

                if (vertexChanged) {
                    changes.add();
                    changes.set(GraphRecordStoreUtilities.SOURCE + COMPARE_ATTRIBUTE, CHANGED);
                    changes.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.OVERLAY_COLOR, changedColour);

                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                    addAttributesToRecord(result, changes, GraphRecordStoreUtilities.SOURCE, result.index());
                } else {
                    changes.add();
                    changes.set(GraphRecordStoreUtilities.SOURCE + COMPARE_ATTRIBUTE, UNCHANGED);
                    changes.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.OVERLAY_COLOR, unchangedColour);

                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                    addAttributesToRecord(result, changes, GraphRecordStoreUtilities.SOURCE, result.index());
                }

                seenVertices.add(vertex);
            } else {
                // Do nothing
            }

            // transaction compare
            final String originalDestination = result.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL);
            if (!seenTransactions.contains(transaction) && !originalTransactionKeysToIndex.containsKey(transaction) && compareTransactionKeysToIndex.containsKey(transaction)) { // added
                changes.add();
                changes.set(GraphRecordStoreUtilities.TRANSACTION + COMPARE_ATTRIBUTE, ADDED);
                changes.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.OVERLAY_COLOR, addedColour);

                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.DESTINATION, vertexDestinationRecordPrimaryValues);
                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.TRANSACTION, transactionRecordPrimaryValues);
                addAttributesToRecord(result, changes, GraphRecordStoreUtilities.TRANSACTION, result.index());
                seenTransactions.add(transaction);
                output.println(String.format("Added transaction connecting %s to %s", originalSource, originalDestination));
            } else if (!seenTransactions.contains(transaction) && originalTransactionKeysToIndex.containsKey(transaction) && !compareTransactionKeysToIndex.containsKey(transaction)) { // removed
                changes.add();
                changes.set(GraphRecordStoreUtilities.TRANSACTION + COMPARE_ATTRIBUTE, REMOVED);
                changes.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.OVERLAY_COLOR, removedColour);

                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.DESTINATION, vertexDestinationRecordPrimaryValues);
                addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.TRANSACTION, transactionRecordPrimaryValues);
                addAttributesToRecord(result, changes, GraphRecordStoreUtilities.TRANSACTION, result.index());
                seenTransactions.add(transaction);
                output.println(String.format("Removed transaction connecting %s to %s", originalSource, originalDestination));
            } else if (!seenTransactions.contains(transaction) && originalTransactionKeysToIndex.containsKey(transaction) && compareTransactionKeysToIndex.containsKey(transaction)) { // changed
                boolean transactionChanged = false;
                for (final String attribute : attributes) {
                    final int dividerPosition = attribute.indexOf(SeparatorConstants.PERIOD);

                    if (dividerPosition != -1) {
                        final String keyType = attribute.substring(0, dividerPosition).toLowerCase();
                        final String keyAttribute = attribute.substring(dividerPosition + 1);

                        switch (keyType) {
                            case "source":
                            case "destination":
                                break;
                            case "transaction":
                                final Integer originalTransactionIndex = originalTransactionKeysToIndex.get(transaction);
                                final Integer compareTransactionIndex = compareTransactionKeysToIndex.get(transaction);
                                final String originalTransactionValue = original.get(originalTransactionIndex, GraphRecordStoreUtilities.TRANSACTION + keyAttribute);
                                final String compareTransactionValue = compare.get(compareTransactionIndex, GraphRecordStoreUtilities.TRANSACTION + keyAttribute);
                                if ((originalTransactionValue != null && !originalTransactionValue.equals(compareTransactionValue))
                                        || (compareTransactionValue != null && !compareTransactionValue.equals(originalTransactionValue))) {
                                    transactionChanged = true;
                                    output.println(String.format("Changed transaction connecting %s to %s, attribute %s value was '%s' and now '%s'", originalSource, originalDestination, keyAttribute, originalTransactionValue, compareTransactionValue));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }

                if (transactionChanged) {
                    changes.add();
                    changes.set(GraphRecordStoreUtilities.TRANSACTION + COMPARE_ATTRIBUTE, CHANGED);
                    changes.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.OVERLAY_COLOR, changedColour);

                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.DESTINATION, vertexDestinationRecordPrimaryValues);
                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.TRANSACTION, transactionRecordPrimaryValues);
                    addAttributesToRecord(result, changes, GraphRecordStoreUtilities.TRANSACTION, result.index());
                } else {
                    changes.add();
                    changes.set(GraphRecordStoreUtilities.TRANSACTION + COMPARE_ATTRIBUTE, UNCHANGED);
                    changes.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.OVERLAY_COLOR, unchangedColour);

                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.SOURCE, vertexSourceRecordPrimaryValues);
                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.DESTINATION, vertexDestinationRecordPrimaryValues);
                    addPrimaryKeyValuesToRecord(changes, GraphRecordStoreUtilities.TRANSACTION, transactionRecordPrimaryValues);
                    addAttributesToRecord(result, changes, GraphRecordStoreUtilities.TRANSACTION, result.index());
                }

                seenTransactions.add(transaction);
            } else {
                // Do nothing
            }
        }

        return changes;
    }

    /**
     * Create the comparison graph using the original graph as the starting
     * point and add the result record store
     *
     * @param originalGraph
     * @param changes
     * @param initializeWithSchema
     * @param completeWithSchema
     * @return
     * @throws InterruptedException
     */
    protected Graph createComparisonGraph(final Graph originalGraph, final GraphRecordStore changes) throws InterruptedException {
        Graph copy;

        final ReadableGraph rg = originalGraph.getReadableGraph();
        try {
            try {
                final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
                final PluginParameters copyParams = copyGraphPlugin.createParameters();
                copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID).setBooleanValue(true);
                PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyParams).executeNow(rg);
                copy = (Graph) copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();
            } catch (final PluginException ex) {
                copy = null;
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            if (copy == null) {
                // The copy failed, drop out now.
                return null;
            }
        } finally {
            rg.release();
        }

        final List<String> vertexIdAttributes = new ArrayList<>();
        vertexIdAttributes.add(VisualConcept.VertexAttribute.LABEL.getName() + "<string>");

        final WritableGraph wgcopy = copy.getWritableGraph("Add changes", true);
        try {
            changes.reset();
            GraphRecordStoreUtilities.addRecordStoreToGraph(wgcopy, changes, false, false, null);
            final int vertexColorReferenceAttribute = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(wgcopy);
            final int transactionColorReferenceAttribute = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(wgcopy);
            wgcopy.setObjectValue(vertexColorReferenceAttribute, 0, VisualConcept.VertexAttribute.OVERLAY_COLOR.getName());
            wgcopy.setObjectValue(transactionColorReferenceAttribute, 0, VisualConcept.TransactionAttribute.OVERLAY_COLOR.getName());
        } finally {
            wgcopy.commit();
        }

        return copy; // only returning this so that this method can be tested
    }

    /**
     * Return statistics about the graph
     *
     * @param graph The graph
     * @return A Map of statistics about the graph
     */
    protected Map<String, Integer> collectStatisticsFromGraph(final GraphReadMethods graph) {
        final Map<String, Integer> statistics = new HashMap<>();
        statistics.put(GLOBAL_MODIFICATION_COUNT, Integer.valueOf(String.valueOf(graph.getGlobalModificationCounter())));
        statistics.put(NODES_COUNT, graph.getVertexCount());
        statistics.put(TRANSACTIONS_COUNT, graph.getTransactionCount());
        statistics.put(NODE_ATTRIBUTE_COUNT, AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.VERTEX).size());
        statistics.put(TRANSACTION_ATTRIBUTE_COUNT, AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.TRANSACTION).size());
        return statistics;
    }

    /**
     * Return a map of the differences between 2 statistical maps
     *
     * @param originalStatistics
     * @param compareStatistics
     * @return
     */
    protected Map<String, Integer> calculateStatisticalDifferences(final Map<String, Integer> originalStatistics, final Map<String, Integer> compareStatistics) {
        final Map<String, Integer> statisticalDifferences = new HashMap<>();
        for (final Map.Entry<String, Integer> entry : originalStatistics.entrySet()) {
            statisticalDifferences.put(entry.getKey(), compareStatistics.get(entry.getKey()) - entry.getValue());
        }
        return statisticalDifferences;
    }

    /**
     * Return a mapping of vertex primary key (as a Set) to record store index.
     * <p>
     * This implementation assumes that you use:
     * <pre>
     * GraphRecordStoreUtilities.getAll()
     * </pre>
     * <p>
     * This is because getAll treats all nodes (i.e even destination nodes) as
     * source nodes and the order of getAll is source nodes and then
     * transactions (if any exist).
     *
     * @param GraphRecordStore The record store
     * @return A mapping of keys to record store index
     */
    protected Map<Set<String>, Integer> getVertexKeysToRecordstoreIndex(final GraphRecordStore recordstore, final Set<String> vertexKeys) {
        final Map<Set<String>, Integer> keyToRecordIndex = new HashMap<>();
        recordstore.reset();
        while (recordstore.next()) {
            final Set<String> vertex = new TreeSet<>();

            for (final String key : vertexKeys) {
                if (!recordstore.hasValue(GraphRecordStoreUtilities.SOURCE + key)) {
                    break;
                }

                if (recordstore.get(GraphRecordStoreUtilities.SOURCE + key) != null) {
                    vertex.add(recordstore.get(GraphRecordStoreUtilities.SOURCE + key));
                }
            }

            if (!keyToRecordIndex.containsKey(vertex)) {
                keyToRecordIndex.put(vertex, recordstore.index());
            }
        }

        return keyToRecordIndex;
    }

    /**
     * Return a mapping of transaction primary key (as a Set) to record store
     * index.
     * <p>
     * This implementation assumes that you use:
     * <pre>
     * GraphRecordStoreUtilities.getAll()
     * </pre>
     * <p>
     * This is because getAll treats all nodes (i.e even destination nodes) as
     * source nodes and the order of getAll is source nodes and then
     * transactions (if any exist).
     *
     * @param GraphRecordStore The record store
     * @return A mapping of keys to record store index
     */
    protected Map<List<String>, Integer> getTransactionKeysToRecordstoreIndex(final GraphRecordStore recordstore, final Set<String> vertexKeys, final Set<String> transactionKeys) {
        final Map<List<String>, Integer> keyToRecordIndex = new HashMap<>();
        recordstore.reset();
        while (recordstore.next()) {//recordstore.toStringVerbose()recordstore.index()
            final List<String> transaction = new ArrayList<>();

            for (final String key : transactionKeys) {
                if (recordstore.get(GraphRecordStoreUtilities.TRANSACTION + key) != null) {
                    transaction.add(recordstore.get(GraphRecordStoreUtilities.TRANSACTION + key));
                }
            }

            for (final String key : vertexKeys) {
                if (recordstore.get(GraphRecordStoreUtilities.DESTINATION + key) != null) {
                    transaction.add(recordstore.get(GraphRecordStoreUtilities.DESTINATION + key));
                }
            }

            if (transaction.isEmpty()) {
                continue;
            }

            for (final String key : vertexKeys) {
                if (recordstore.get(GraphRecordStoreUtilities.SOURCE + key) != null) {
                    transaction.add(recordstore.get(GraphRecordStoreUtilities.SOURCE + key));
                }
            }

            if (!keyToRecordIndex.containsKey(transaction)) {
                keyToRecordIndex.put(transaction, recordstore.index());
            }
        }

        return keyToRecordIndex;
    }

    /**
     * Get the graph using the graphs name
     *
     * @param graphName The name of the graph
     * @return The Graph represented by the name
     */
    private Graph getGraphFromName(final String graphName) {
        for (final String graphId : GraphNode.getAllGraphs().keySet()) {
            if (GraphNode.getGraphNode(graphId).getDisplayName().equals(graphName)) {
                return GraphNode.getGraphNode(graphId).getGraph();
            }
        }
        return null;
    }

    /**
     * Add primary key values to the record store so that the node or
     * transaction is created with the minimum attributes for it to display
     * correctly.
     *
     * @param changes The GraphRecordStore holding the changes
     * @param type The element type
     * @param recordPrimaryValues
     */
    private void addPrimaryKeyValuesToRecord(final GraphRecordStore changes, final String type, final Map<String, String> recordPrimaryValues) {
        for (final Map.Entry<String, String> entry : recordPrimaryValues.entrySet()) {
            changes.set(type + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add attributes to the current change record so that things like the
     * visual attributes come across in the compare graph
     *
     * @param recordstore The GraphRecordStore of the changes to be processed
     * @param changes The GraphRecordStore holding the changes
     * @param type The element type
     * @param index The index of the current record
     */
    private void addAttributesToRecord(final GraphRecordStore recordstore, final GraphRecordStore changes, final String type, final int index) {
        for (final String key : recordstore.keys()) {
            if (recordstore.get(index, key) != null
                    && !changes.hasValue(key)
                    && !key.endsWith(".[id]")
                    && key.startsWith(type)) {
                changes.set(key, recordstore.get(index, key));
            }
        }
    }
}
