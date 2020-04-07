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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that builds a complete graph.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("CompleteGraphBuilderPlugin=Complete Graph Builder")
public class CompleteGraphBuilderPlugin extends SimpleEditPlugin {

    public static final String N_PARAMETER_ID = PluginParameter.buildId(CompleteGraphBuilderPlugin.class, "n");
    public static final String RANDOM_WEIGHTS_PARAMETER_ID = PluginParameter.buildId(CompleteGraphBuilderPlugin.class, "random_weights");
    public static final String NODE_TYPES_PARAMETER_ID = PluginParameter.buildId(CompleteGraphBuilderPlugin.class, "node_types");
    public static final String TRANSACTION_TYPES_PARAMETER_ID = PluginParameter.buildId(CompleteGraphBuilderPlugin.class, "transaction_types");
    
    private final SecureRandom r = new SecureRandom();

    @Override
    public String getDescription() {
        return "Builds a complete graph.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> n = IntegerParameterType.build(N_PARAMETER_ID);
        n.setName("Number of nodes");
        n.setDescription("The number of nodes on the graph");
        n.setIntegerValue(5);
        IntegerParameterType.setMinimum(n, 0);
        params.addParameter(n);

        final PluginParameter<BooleanParameterValue> randomWeights = BooleanParameterType.build(RANDOM_WEIGHTS_PARAMETER_ID);
        randomWeights.setName("Random edge weight/direction");
        randomWeights.setDescription("Edges have a random number of transactions going in random directions");
        randomWeights.setBooleanValue(true);
        params.addParameter(randomWeights);

        final PluginParameter<MultiChoiceParameterValue> nodeTypes = MultiChoiceParameterType.build(NODE_TYPES_PARAMETER_ID);
        nodeTypes.setName("Node Types");
        nodeTypes.setDescription("Node types to add to the graph");
        params.addParameter(nodeTypes);

        final PluginParameter<MultiChoiceParameterValue> transactionTypes = MultiChoiceParameterType.build(TRANSACTION_TYPES_PARAMETER_ID);
        transactionTypes.setName("Transaction Types");
        transactionTypes.setDescription("Transaction types to add to the graph");
        params.addParameter(transactionTypes);

        return params;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> nAttributes = new ArrayList<>();
        final List<String> tAttributes = new ArrayList<>();
        final List<String> nChoices = new ArrayList<>();
        final List<String> tChoices = new ArrayList<>();
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final List<SchemaVertexType> nodeTypes = GraphManager.getDefault().getActiveGraph().getSchema().getFactory().getRegisteredVertexTypes();

                for (int i = 0; i < nodeTypes.size(); i++) {
                    SchemaVertexType type = nodeTypes.get(i);
                    nAttributes.add(type.getName());
                }
                nAttributes.sort(String::compareTo);

                final List<SchemaTransactionType> transactionTypes = GraphManager.getDefault().getActiveGraph().getSchema().getFactory().getRegisteredTransactionTypes();
                for (int i = 0; i < transactionTypes.size(); i++) {
                    SchemaTransactionType type = transactionTypes.get(i);
                    tAttributes.add(type.getName());
                }
                tAttributes.sort(String::compareTo);
            } finally {
                readableGraph.release();
            }
            nChoices.add(nAttributes.get(0));
            tChoices.add(tAttributes.get(0));
        }
        if (parameters != null && parameters.getParameters() != null) {
            final PluginParameter nAttribute = parameters.getParameters().get(NODE_TYPES_PARAMETER_ID);
            final PluginParameter tAttribute = parameters.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);
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
        final boolean randomWeights = params.get(RANDOM_WEIGHTS_PARAMETER_ID).getBooleanValue();
        final List<String> nodeTypes = params.get(NODE_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();
        final List<String> transactionTypes = params.get(TRANSACTION_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();

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

        final int vxIsGoodAttr = graph.addAttribute(GraphElementType.VERTEX, "boolean", "isGood", null, false, null);
        final int vxCountryAttr = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);

        final int txIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int txTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final VertexDecorators decorators;
        decorators = new VertexDecorators(null, graph.getAttributeName(vxCountryAttr), null, graph.getAttributeName(vxIsGoodAttr));
        final int decoratorsAttr = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        graph.setObjectValue(decoratorsAttr, 0, decorators);

        final int[] vxIds = new int[n];
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

        // Create transactions between the nodes.
        final Date d = new Date();
        final int fourDays = 4 * 24 * 60 * 60 * 1000;

        for (int x : vxIds) {
            for (int y : vxIds) {
                if (x == y) {
                    continue;
                }
                final int reciprocity = r.nextInt(3);
                int numTimes = 1;
                if (randomWeights) {
                    numTimes = r.nextInt(1 + r.nextInt(100));
                }
                for (int i = 0; i < numTimes; i++) {
                    int sxId = x;
                    int dxId = y;
                    if (randomWeights) {
                        switch (reciprocity) {
                            case 0:
                                boolean random0 = r.nextBoolean();
                                if (random0) {
                                    sxId = y;
                                    dxId = x;
                                }
                                break;
                            case 1:
                                int random1 = r.nextInt(5);
                                if (random1 == 0) {
                                    sxId = y;
                                    dxId = x;
                                }
                                break;
                            default:
                                int randomDefault = r.nextInt(5);
                                if (randomDefault != 0) {
                                    sxId = y;
                                    dxId = x;
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
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        if (!PreferenceUtilites.isGraphViewFrozen()) {
            if (n < 10000) {
                // Do a trees layout.
                try {
                    PluginExecutor.startWith(ArrangementPluginRegistry.TREES)
                            .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
                } catch (PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Do a grid layout.
                try {
                    PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                            .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
                } catch (PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        }

        interaction.setProgress(1, 0, "Completed successfully", true);
    }
}
