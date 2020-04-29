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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates pagerank centrality for each vertex. This centrality measure does
 * not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("PagerankCentralityPlugin=Pagerank Centrality")
@PluginInfo(tags = {"ANALYTIC"})
public class PagerankCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute PAGERANK_ATTRIBUTE = SnaConcept.VertexAttribute.PAGERANK_CENTRALITY;

    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID = PluginParameter.buildId(PagerankCentralityPlugin.class, "treat_undirected_bidirectional");
    public static final String DAMPING_FACTOR_PARAMETER_ID = PluginParameter.buildId(PagerankCentralityPlugin.class, "damping_factor");
    public static final String ITERATIONS_PARAMETER_ID = PluginParameter.buildId(PagerankCentralityPlugin.class, "iterations");
    public static final String EPSILON_PARAMETER_ID = PluginParameter.buildId(PagerankCentralityPlugin.class, "epsilon");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(PagerankCentralityPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> treatUndirectedBidirectionalParameter = BooleanParameterType.build(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        treatUndirectedBidirectionalParameter.setName("Include Undirected");
        treatUndirectedBidirectionalParameter.setDescription("Treat undirected connections as bidirectional connections");
        treatUndirectedBidirectionalParameter.setBooleanValue(true);
        parameters.addParameter(treatUndirectedBidirectionalParameter);

        final PluginParameter<FloatParameterValue> dampingFactorParameter = FloatParameterType.build(DAMPING_FACTOR_PARAMETER_ID);
        dampingFactorParameter.setName("Damping Factor");
        dampingFactorParameter.setDescription("The damping factor to apply at each iteration");
        dampingFactorParameter.setFloatValue(0.85f);
        parameters.addParameter(dampingFactorParameter);

        final PluginParameter<IntegerParameterValue> iterationsParameter = IntegerParameterType.build(ITERATIONS_PARAMETER_ID);
        iterationsParameter.setName("Iterations");
        iterationsParameter.setDescription("The number of iterations to run before returning a result");
        iterationsParameter.setIntegerValue(100);
        parameters.addParameter(iterationsParameter);

        final PluginParameter<FloatParameterValue> epsilonParameter = FloatParameterType.build(EPSILON_PARAMETER_ID);
        epsilonParameter.setName("Epsilon");
        epsilonParameter.setDescription("The change threshold at which equilibrium can be considered reached");
        epsilonParameter.setFloatValue(1E-8f);
        parameters.addParameter(epsilonParameter);

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseByAvailableParameter.setBooleanValue(false);
        parameters.addParameter(normaliseByAvailableParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        final float dampingFactor = parameters.getFloatValue(DAMPING_FACTOR_PARAMETER_ID);
        final int iterations = parameters.getIntegerValue(ITERATIONS_PARAMETER_ID);
        final float epsilon = parameters.getFloatValue(EPSILON_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        // identify incoming connections and store connected vertices and their outgoing connection count
        final Set<Integer> verticesWithZeroOutLinks = new HashSet<>();
        final Map<Integer, Set<Tuple<Integer, Integer>>> vertexInLinks = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            vertexInLinks.put(vertexId, new HashSet<>());

            if (graph.getVertexEdgeCount(vertexId, GraphConstants.OUTGOING) == 0) {
                verticesWithZeroOutLinks.add(vertexId);
            }

            final int linkCount = graph.getVertexLinkCount(vertexId);
            for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
                final int linkId = graph.getVertexLink(vertexId, linkPosition);
                final int linkLowId = graph.getLinkLowVertex(linkId);
                final int linkHighId = graph.getLinkHighVertex(linkId);
                if (linkLowId != linkHighId) {
                    final int neighbourId = vertexId == linkLowId ? linkHighId : linkLowId;
                    final int edgeCount = graph.getLinkEdgeCount(linkId);
                    for (int edgePosition = 0; edgePosition < edgeCount; edgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, edgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        if ((treatUndirectedBidirectional && edgeDirection == GraphConstants.FLAT)
                                || (vertexId < neighbourId && edgeDirection == GraphConstants.DOWNHILL)
                                || (vertexId > neighbourId && edgeDirection == GraphConstants.UPHILL)) {
                            int neighbourOutCount = 0;
                            final int transactionCount = graph.getVertexTransactionCount(neighbourId);
                            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                                final int transactionId = graph.getVertexTransaction(neighbourId, transactionPosition);
                                final int transactionDirection = graph.getTransactionDirection(transactionId);
                                final int transactionSourceId = graph.getTransactionSourceVertex(transactionId);
                                final int transactionDestinationId = graph.getTransactionDestinationVertex(transactionId);
                                if (transactionSourceId != transactionDestinationId) {
                                    final int otherId = neighbourId == transactionSourceId ? transactionDestinationId : transactionSourceId;
                                    if ((treatUndirectedBidirectional && transactionDirection == GraphConstants.FLAT)
                                            || (neighbourId < otherId && transactionDirection == GraphConstants.UPHILL)
                                            || (neighbourId > otherId && transactionDirection == GraphConstants.DOWNHILL)) {
                                        neighbourOutCount++;
                                    }
                                }
                            }

                            vertexInLinks.get(vertexId).add(Tuple.create(neighbourId, neighbourOutCount));
                        }
                    }
                }
            }
        }

        // handle dangling vertices by linking them to all pages
        for (final int neighbourId : verticesWithZeroOutLinks) {
            final int updatedCount = vertexInLinks.size();
            for (final int vertexId : vertexInLinks.keySet()) {
                vertexInLinks.get(vertexId).add(Tuple.create(neighbourId, updatedCount));
            }
        }

        // initialise pagerank values
        final double[] tempPageranks = new double[graph.getVertexCapacity()];
        final double[] pageranks = new double[graph.getVertexCapacity()];
        Arrays.fill(pageranks, (double) 1 / vertexInLinks.size());

        // calculate pagerank for each vertex
        for (int iteration = 0; iteration < iterations; iteration++) {
            Arrays.fill(tempPageranks, 0);

            double maxPagerank = 0;
            double delta = 0;
            for (final int vertexId : vertexInLinks.keySet()) {
                double neighbourContribution = 0.0;
                final Set<Tuple<Integer, Integer>> neighbours = vertexInLinks.get(vertexId);
                for (final Tuple<Integer, Integer> neighbour : neighbours) {
                    final int neighbourId = neighbour.getFirst();
                    final int neighbourOutCount = neighbour.getSecond();
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);
                    neighbourContribution += (pageranks[neighbourPosition] / neighbourOutCount);
                }

                final int vertexPosition = graph.getVertexPosition(vertexId);
                tempPageranks[vertexPosition] = ((1 - dampingFactor) / vertexInLinks.size()) + (dampingFactor * neighbourContribution);
                maxPagerank = Math.max(tempPageranks[vertexPosition], maxPagerank);
                delta += Math.abs(pageranks[vertexPosition] - tempPageranks[vertexPosition]);
            }

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                if (normaliseByAvailable && maxPagerank > 0) {
                    pageranks[vertexPosition] = tempPageranks[vertexPosition] / maxPagerank;
                } else {
                    pageranks[vertexPosition] = tempPageranks[vertexPosition];
                }
            }

            if (delta < epsilon) {
                break;
            }
        }

        // update the graph with pagerank values
        final int pagerankAttribute = PAGERANK_ATTRIBUTE.ensure(graph);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            graph.setFloatValue(pagerankAttribute, vertexId, (float) pageranks[vertexPosition]);
        }
    }
}
