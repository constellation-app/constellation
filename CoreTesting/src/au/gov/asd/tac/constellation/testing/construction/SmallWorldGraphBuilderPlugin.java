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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A plugin that builds a random graph using the small world model.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("SmallWorldGraphBuilderPlugin=Small World Graph Builder")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.CREATE})
public class SmallWorldGraphBuilderPlugin extends SimpleEditPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(SmallWorldGraphBuilderPlugin.class.getName());

    public static final String N_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "n");
    public static final String K_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "k");
    public static final String P_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "p");
    public static final String T_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "t");
    public static final String BUILD_MODE_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "build_mode");
    public static final String RANDOM_WEIGHTS_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "random_weights");
    public static final String NODE_TYPES_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "node_types");
    public static final String TRANSACTION_TYPES_PARAMETER_ID = PluginParameter.buildId(SmallWorldGraphBuilderPlugin.class, "transaction_types");

    private static final String CONNECTED = "connected";

    private final SecureRandom r = new SecureRandom();

    @Override
    public String getDescription() {
        return "Builds a random graph using the small world model.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> n = IntegerParameterType.build(N_PARAMETER_ID);
        n.setName("Number of nodes");
        n.setDescription("The number of nodes on the graph");
        n.setIntegerValue(10);
        IntegerParameterType.setMinimum(n, 0);
        params.addParameter(n);

        final PluginParameter<IntegerParameterValue> k = IntegerParameterType.build(K_PARAMETER_ID);
        k.setName("Nearest neighbours to attach");
        k.setDescription("The number of nearest neighbours to connect each node to");
        k.setIntegerValue(4);
        IntegerParameterType.setMinimum(k, 0);
        params.addParameter(k);

        final PluginParameter<FloatParameterValue> p = FloatParameterType.build(P_PARAMETER_ID);
        p.setName("Rewiring probability");
        p.setDescription("Probability of re-wiring each edge (low for a long shortest paths lattice structure, high for higher clustering coefficient random graph)");
        p.setFloatValue(0.5f);
        FloatParameterType.setMinimum(p, 0f);
        FloatParameterType.setMaximum(p, 1f);
        params.addParameter(p);

        final List<String> modes = new ArrayList<>();
        modes.add("Default");
        modes.add("Newman");
        modes.add(CONNECTED);

        final PluginParameter<SingleChoiceParameterValue> buildMode = SingleChoiceParameterType.build(BUILD_MODE_PARAMETER_ID);
        buildMode.setName("Build mode");
        buildMode.setDescription("Newman: Adds edges instead of rewiring. Connected: Attempts to build a connected graph.");
        SingleChoiceParameterType.setOptions(buildMode, modes);
        SingleChoiceParameterType.setChoice(buildMode, modes.get(0));
        params.addParameter(buildMode);

        final PluginParameter<IntegerParameterValue> t = IntegerParameterType.build(T_PARAMETER_ID);
        t.setName("Number of attempts to build connected graph");
        t.setDescription("Number of attempts to build a connected graph");
        t.setIntegerValue(100);
        IntegerParameterType.setMinimum(t, 1);
        t.setEnabled(false);
        params.addParameter(t);

        final PluginParameter<BooleanParameterValue> randomWeights = BooleanParameterType.build(RANDOM_WEIGHTS_PARAMETER_ID);
        randomWeights.setName("Random edge weight/direction");
        randomWeights.setDescription("Edges have a random number of transactions going in random directions");
        randomWeights.setBooleanValue(true);
        params.addParameter(randomWeights);

        final PluginParameter<MultiChoiceParameterValue> nodeTypes = MultiChoiceParameterType.build(NODE_TYPES_PARAMETER_ID);
        nodeTypes.setName("Node types");
        nodeTypes.setDescription("Node types to add to the graph");
        params.addParameter(nodeTypes);

        final PluginParameter<MultiChoiceParameterValue> transactionTypes = MultiChoiceParameterType.build(TRANSACTION_TYPES_PARAMETER_ID);
        transactionTypes.setName("Transaction types");
        transactionTypes.setDescription("Transaction types to add to the graph");
        params.addParameter(transactionTypes);

        params.addController(BUILD_MODE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String mode = master.getStringValue();
                parameters.get(T_PARAMETER_ID).setEnabled(mode.equals(CONNECTED));
            }
        });

        return params;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> nAttributes = new ArrayList<>();
        final List<String> tAttributes = new ArrayList<>();
        final List<String> nChoices = new ArrayList<>();
        final List<String> tChoices = new ArrayList<>();
        if (graph != null) {
            final List<SchemaVertexType> nodeTypes = graph.getSchema().getFactory().getRegisteredVertexTypes();
            for (int i = 0; i < nodeTypes.size(); i++) {
                final SchemaVertexType type = nodeTypes.get(i);
                nAttributes.add(type.getName());
            }
            nAttributes.sort(String::compareTo);

            final List<SchemaTransactionType> transactionTypes = graph.getSchema().getFactory().getRegisteredTransactionTypes();
            for (int i = 0; i < transactionTypes.size(); i++) {
                final SchemaTransactionType type = transactionTypes.get(i);
                tAttributes.add(type.getName());
            }
            tAttributes.sort(String::compareTo);
            
            nChoices.add(nAttributes.get(0));
            tChoices.add(tAttributes.get(0));
        }

        if (parameters != null && parameters.getParameters() != null) {
            @SuppressWarnings("unchecked") //NODE_TYPES_PARAMETER will always be of type MultiChoiceParameter
            final PluginParameter<MultiChoiceParameterValue> nAttribute = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(NODE_TYPES_PARAMETER_ID);
            @SuppressWarnings("unchecked") //TRANSACTION_TYPES_PARAMETER will always be of type MultiChoiceParameter
            final PluginParameter<MultiChoiceParameterValue> tAttribute = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);
            MultiChoiceParameterType.setOptions(nAttribute, nAttributes);
            MultiChoiceParameterType.setOptions(tAttribute, tAttributes);
            MultiChoiceParameterType.setChoices(nAttribute, nChoices);
            MultiChoiceParameterType.setChoices(tAttribute, tChoices);
        }
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        final Map<String, PluginParameter<?>> params = parameters.getParameters();

        final int n = params.get(N_PARAMETER_ID).getIntegerValue();
        final int k = params.get(K_PARAMETER_ID).getIntegerValue();
        final float p = params.get(P_PARAMETER_ID).getFloatValue();
        final int t = params.get(T_PARAMETER_ID).getIntegerValue();
        final String buildMode = params.get(BUILD_MODE_PARAMETER_ID).getStringValue();
        final boolean randomWeights = params.get(RANDOM_WEIGHTS_PARAMETER_ID).getBooleanValue();
        final List<String> nodeTypes = params.get(NODE_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();
        final List<String> transactionTypes = params.get(TRANSACTION_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();

        assert k < n : String.format("[Number of attached neighbours '%s' must be smaller than the number of nodes '%s']", k, n);

        // Random countries to put in the graph
        final List<String> countries = new ArrayList<>();
        countries.add("Australia");
        countries.add("Brazil");
        countries.add("China");
        countries.add("France");
        countries.add("Japan");
        countries.add("New Zealand");
        countries.add("South Africa");
        countries.add("United Arab Emirates");
        countries.add("United Kingdom");
        countries.add("United States");

        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);

        final int vxIsGoodAttr = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "isGood", null, false, null);
        final int vxCountryAttr = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);
        final int vxPinnedAttr = VisualConcept.VertexAttribute.PINNED.ensure(graph);

        final int txIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int txTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final VertexDecorators decorators;
        decorators = new VertexDecorators(graph.getAttributeName(vxCountryAttr), graph.getAttributeName(vxPinnedAttr), null, graph.getAttributeName(vxIsGoodAttr));
        final int decoratorsAttr = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        graph.setObjectValue(decoratorsAttr, 0, decorators);

        // Create transactions between the nodes.
        final Date d = new Date();
        final int fourDays = 4 * 24 * 60 * 60 * 1000;

        int initialComponents = 0;
        if (buildMode.equals(CONNECTED)) {
            initialComponents = componentCount(graph);
        }

        for (int s = 1; s <= t; s++) {

            final int[] vxIds = new int[n];
            final Set<Integer> transactions = new HashSet<>();
            int vx = 0;

            while (vx < n) {
                final int vxId = graph.addVertex();
                final String label = "Node_" + vxId;

                graph.setStringValue(vxIdentifierAttr, vxId, label);
                graph.setStringValue(vxTypeAttr, vxId, nodeTypes.get(r.nextInt(nodeTypes.size())));
                graph.setBooleanValue(vxIsGoodAttr, vxId, r.nextInt(10) == 0);
                graph.setStringValue(vxCountryAttr, vxId, countries.get(r.nextInt(countries.size())));
                if (graph.getSchema() != null) {
                    graph.getSchema().completeVertex(graph, vxId);
                }

                vxIds[vx] = vxId;
                vx++;

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }

            for (int j = 1; j < (k / 2) + 1; j++) {
                final List<Integer> destinations = new ArrayList<>();
                for (int i = j; i < n; i++) {
                    destinations.add(vxIds[i]);
                }
                for (int i = 0; i < j; i++) {
                    destinations.add(vxIds[i]);
                }
                for (int z = 0; z < n; z++) {
                    final int e = graph.addTransaction(vxIds[z], destinations.get(z), true);
                    transactions.add(e);
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }

            for (int j = 1; j < (k / 2) + 1; j++) {
                final List<Integer> destinations = new ArrayList<>();
                for (int i = j; i < n; i++) {
                    destinations.add(vxIds[i]);
                }
                for (int i = 0; i < j; i++) {
                    destinations.add(vxIds[i]);
                }
                for (int z = 0; z < n; z++) {
                    final int u = vxIds[z];
                    final int v = destinations.get(z);
                    if (r.nextDouble() < p) {
                        int w = vxIds[r.nextInt(n)];
                        boolean skip = false;
                        while (w == u || graph.getLink(u, w) != Graph.NOT_FOUND) {
                            w = vxIds[r.nextInt(n)];
                            if (graph.getVertexNeighbourCount(u) >= n - 1) {
                                skip = true;
                                break;
                            }
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                        if (!skip) {
                            if ("Newman".equals(buildMode)) {
                                final int exId = graph.getLinkTransaction(graph.getLink(u, v), 0);
                                graph.removeTransaction(exId);
                                transactions.remove(exId);
                            }
                            final int e = graph.addTransaction(u, w, true);
                            transactions.add(e);
                        }
                    }
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }

            for (final int txId : transactions) {
                final int reciprocity = r.nextInt(3);
                int numTimes = 1;
                if (randomWeights) {
                    numTimes = r.nextInt(1 + r.nextInt(100));
                }
                for (int i = 0; i < numTimes; i++) {
                    int sxId = graph.getTransactionSourceVertex(txId);
                    int dxId = graph.getTransactionDestinationVertex(txId);
                    if (randomWeights) {
                        switch (reciprocity) {
                            case 0:
                                final boolean random0 = r.nextBoolean();
                                if (random0) {
                                    sxId = graph.getTransactionDestinationVertex(txId);
                                    dxId = graph.getTransactionSourceVertex(txId);
                                }
                                break;
                            case 1:
                                final int random1 = r.nextInt(5);
                                if (random1 == 0) {
                                    sxId = graph.getTransactionDestinationVertex(txId);
                                    dxId = graph.getTransactionSourceVertex(txId);
                                }
                                break;
                            default:
                                final int randomDefault = r.nextInt(5);
                                if (randomDefault != 0) {
                                    sxId = graph.getTransactionDestinationVertex(txId);
                                    dxId = graph.getTransactionSourceVertex(txId);
                                }
                                break;
                        }
                    }
                    final int e = graph.addTransaction(sxId, dxId, true);
                    graph.setLongValue(txDateTimeAttr, e, d.getTime() - r.nextInt(fourDays));
                    graph.setStringValue(txTypeAttr, e, transactionTypes.get(r.nextInt(transactionTypes.size())));
                    graph.setIntValue(txIdAttr, e, e);
                    if (graph.getSchema() != null) {
                        graph.getSchema().completeTransaction(graph, e);
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
                graph.removeTransaction(txId);
            }

            if (buildMode.equals(CONNECTED) && componentCount(graph) != initialComponents + 1 && s != t) {
                for (final int vxId : vxIds) {
                    graph.removeVertex(vxId);
                }
            } else {
                break;
            }
        }

        try {
            if (n < 10000) {
                // Do a trees layout.
                PluginExecutor.startWith(ArrangementPluginRegistry.TREES)
                        .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
            } else {
                // Do a grid layout.
                PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                        .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
            }
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        interaction.setProgress(1, 0, "Completed successfully", true);
    }

    private static int componentCount(final GraphWriteMethods graph) {
        final int vertexCount = graph.getVertexCount();
        final BitSet[] traversal = new BitSet[vertexCount];

        final BitSet update = new BitSet(vertexCount);
        final BitSet[] sendFails = new BitSet[vertexCount];
        final BitSet[] sendBuffer = new BitSet[vertexCount];
        final BitSet[] exclusions = new BitSet[vertexCount];
        final BitSet newUpdate = new BitSet(vertexCount);
        final BitSet turn = new BitSet(vertexCount);

        // initialise variables
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            traversal[vertexPosition] = new BitSet(vertexCount);

            // only update nodes with neighbours
            final int vxId = graph.getVertex(vertexPosition);
            if (graph.getVertexNeighbourCount(vxId) > 0) {
                update.set(vertexPosition);
            }

            sendFails[vertexPosition] = new BitSet(vertexCount);
            sendBuffer[vertexPosition] = new BitSet(vertexCount);
            exclusions[vertexPosition] = new BitSet(vertexCount);
        }

        while (!update.isEmpty()) {
            // update the information of each node with messages
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                traversal[vertexPosition].or(sendBuffer[vertexPosition]);
                traversal[vertexPosition].set(vertexPosition);
                sendFails[vertexPosition].clear();
                sendFails[vertexPosition].or(sendBuffer[vertexPosition]);
                sendBuffer[vertexPosition].clear();
            }

            // for each neighbour, check if there is any new information it needs to receive
            for (int vertexPosition = update.nextSetBit(0); vertexPosition >= 0; vertexPosition = update.nextSetBit(vertexPosition + 1)) {
                final int vertexId = graph.getVertex(vertexPosition);

                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (!traversal[vertexPosition].equals(traversal[neighbourPosition])) {
                        turn.set(neighbourPosition, true);

                        final BitSet diff = (BitSet) traversal[vertexPosition].clone();
                        diff.andNot(traversal[neighbourPosition]);
                        sendBuffer[neighbourPosition].or(diff);
                        sendFails[vertexPosition].andNot(diff);
                        newUpdate.set(neighbourPosition);
                    }
                }
                for (int neighbourPosition = sendFails[vertexPosition].nextSetBit(0); neighbourPosition >= 0; neighbourPosition = sendFails[vertexPosition].nextSetBit(neighbourPosition + 1)) {
                    exclusions[neighbourPosition].set(vertexPosition, true);
                }
            }

            turn.clear();
            update.clear();
            update.or(newUpdate);
            newUpdate.clear();
        }

        final Set<BitSet> connectedComponents = new HashSet<>();
        int numComponents = 0;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final BitSet subgraph = traversal[vertexPosition];
            if (subgraph.cardinality() <= 1) {
                numComponents += 1;
            } else {
                connectedComponents.add(subgraph);
            }
        }

        numComponents += connectedComponents.size();
        return numComponents;
    }
}
