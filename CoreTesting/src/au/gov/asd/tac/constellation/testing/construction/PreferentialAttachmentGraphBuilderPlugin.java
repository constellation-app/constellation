/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that builds a random graph using the preferential
 * attachment model.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("PreferentialAttachmentGraphBuilderPlugin=Preferential Attachment Graph Builder")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.CREATE})
public class PreferentialAttachmentGraphBuilderPlugin extends SimpleEditPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(PreferentialAttachmentGraphBuilderPlugin.class.getName());

    public static final String N_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "n");
    public static final String M_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "m");
    public static final String RANDOM_WEIGHTS_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "random_weights");
    public static final String NODE_TYPES_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "node_types");
    public static final String TRANSACTION_TYPES_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentGraphBuilderPlugin.class, "transaction_types");

    private final SecureRandom r = new SecureRandom();

    @Override
    public String getDescription() {
        return "Builds a random graph using the preferential attachment model.";
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

        final PluginParameter<IntegerParameterValue> m = IntegerParameterType.build(M_PARAMETER_ID);
        m.setName("Number of edges to attach");
        m.setDescription("The number of edges to attach to each node");
        m.setIntegerValue(1);
        IntegerParameterType.setMinimum(m, 0);
        params.addParameter(m);

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
            final Set<Class<? extends SchemaConcept>> concepts = graph.getSchema().getFactory().getRegisteredConcepts();
            
            final Collection<SchemaVertexType> nodeTypes = SchemaVertexTypeUtilities.getTypes(concepts);
            for (final SchemaVertexType type : nodeTypes) {
                nAttributes.add(type.getName());
            }
            nAttributes.sort(String::compareTo);

            final Collection<SchemaTransactionType> transactionTypes = SchemaTransactionTypeUtilities.getTypes(concepts);
            for (final SchemaTransactionType type : transactionTypes) {
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
        final int m = params.get(M_PARAMETER_ID).getIntegerValue();
        final boolean randomWeights = params.get(RANDOM_WEIGHTS_PARAMETER_ID).getBooleanValue();
        final List<String> nodeTypes = params.get(NODE_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();
        final List<String> transactionTypes = params.get(TRANSACTION_TYPES_PARAMETER_ID).getMultiChoiceValue().getChoices();

        assert m >= 1 && m < n : String.format("[Number of edges to attach '%s' must be at least 1 and less than the number of nodes '%s']", m, n);

        // Random countries to put in the graph
        final List<String> countries = Arrays.asList("Australia", "Brazil", "China", "France", "Japan", "New Zealand", 
                "South Africa", "United Arab Emirates", "United Kingdom", "United States");

        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);

        final int vxIsGoodAttr = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "isGood", null, false, null);
        final int vxCountryAttr = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);
        final int vxPinnedAttr = VisualConcept.VertexAttribute.PINNED.ensure(graph);

        final int txIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int txTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final int[] startVxIds = new int[m];

        final VertexDecorators decorators = new VertexDecorators(graph.getAttributeName(vxCountryAttr), 
                graph.getAttributeName(vxPinnedAttr), null, graph.getAttributeName(vxIsGoodAttr));
        final int decoratorsAttr = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        graph.setObjectValue(decoratorsAttr, 0, decorators);

        int vx = 0;
        while (vx < m) {
            final int vxId = graph.addVertex();
            final String label = "Node_" + vxId;

            graph.setStringValue(vxIdentifierAttr, vxId, label);
            graph.setStringValue(vxTypeAttr, vxId, nodeTypes.get(r.nextInt(nodeTypes.size())));
            graph.setBooleanValue(vxIsGoodAttr, vxId, r.nextInt(10) == 0);
            graph.setStringValue(vxCountryAttr, vxId, countries.get(r.nextInt(countries.size())));
            if (graph.getSchema() != null) {
                graph.getSchema().completeVertex(graph, vxId);
            }

            startVxIds[vx] = vxId;
            vx++;

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        // Create transactions between the nodes.
        final Date d = new Date();
        final int fourDays = 4 * 24 * 60 * 60 * 1000;

        Set<Integer> destinations = Arrays.stream(startVxIds).boxed().collect(Collectors.toSet());
        final List<Integer> repeats = new ArrayList<>();
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
            final int reciprocity = r.nextInt(3);
            for (final int destination : destinations) {
                int numTimes = 1;
                if (randomWeights) {
                    numTimes = r.nextInt(1 + r.nextInt(100));
                }
                for (int i = 0; i < numTimes; i++) {
                    int sxId = vxId;
                    int dxId = destination;
                    if (randomWeights) {
                        if (reciprocity == 0) {
                            final boolean random0 = r.nextBoolean();
                            if (random0) {
                                sxId = destination;
                                dxId = vxId;
                            }
                        } else {
                            final int randomDefault = r.nextInt(5);
                            if (randomDefault != 0) {
                                sxId = destination;
                                dxId = vxId;
                            }
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
                repeats.add(vxId);
                repeats.add(destination);
            }
            destinations = generateDestinations(repeats, m, r);
            vx++;

            if (Thread.interrupted()) {
                throw new InterruptedException();
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

    /**
     * Returns a random set of destinations
     *
     * @param repeats A list of possible destinations
     * @param nVxStart The number of destinations to return.
     * @param r SecureRandom
     *
     * @return A random set of destinations
     */
    private static Set<Integer> generateDestinations(final List<Integer> repeats, final int nVxStart, final SecureRandom r) {
        final Set<Integer> destinations = new HashSet<>();
        while (destinations.size() < nVxStart) {
            destinations.add(repeats.get(r.nextInt(repeats.size())));
        }
        return destinations;
    }
}
