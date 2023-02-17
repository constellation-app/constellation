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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
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
 * @author cygnus_x-1, Nova
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("PagerankCentralityPlugin=Pagerank Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
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
        dampingFactorParameter.setFloatValue(0.85F);
        parameters.addParameter(dampingFactorParameter);

        final PluginParameter<IntegerParameterValue> iterationsParameter = IntegerParameterType.build(ITERATIONS_PARAMETER_ID);
        iterationsParameter.setName("Iterations");
        iterationsParameter.setDescription("The number of iterations to run before returning a result");
        iterationsParameter.setIntegerValue(100);
        parameters.addParameter(iterationsParameter);

        final PluginParameter<FloatParameterValue> epsilonParameter = FloatParameterType.build(EPSILON_PARAMETER_ID);
        epsilonParameter.setName("Epsilon");
        epsilonParameter.setDescription("The change threshold at which equilibrium can be considered reached");
        epsilonParameter.setFloatValue(1E-8F);
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
        final float dampingFactor = parameters.getFloatValue(DAMPING_FACTOR_PARAMETER_ID);
        final int maxIterations = parameters.getIntegerValue(ITERATIONS_PARAMETER_ID);
        final float epsilon = parameters.getFloatValue(EPSILON_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        PagerankController.initialiseAllPagerankVertices(graph, parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID), dampingFactor, normaliseByAvailable);

        // calculate pageranks
        for (int currentIteration = 0; currentIteration < maxIterations; currentIteration++) {
            interaction.setProgress(currentIteration, maxIterations, "Iteration " + currentIteration + " of " + maxIterations, true);

            PagerankController.updateAllPageranks();

            if (PagerankController.delta < epsilon) {
                break;
            }

        }

        // update the graph with pagerank values
        PagerankController.commitPageranksToGraph();
    }

    private static class PagerankController {

        private static final Set<PagerankVertex> sinks = new HashSet<>();
        private static boolean treatUndirectedBidirectional;
        private static boolean normaliseByAvailable;
        private static final Map<Integer, PagerankVertex> pagerankVertices = new HashMap<>();
        private static GraphWriteMethods graph;
        private static double dampingFactor = 0;
        private static int vertexCount = 0;
        private static double sinkPagerankContribution = 0;
        private static double delta = 0;
        private static int pagerankAttribute;
        private static double baseContribution;

        public static void initialiseAllPagerankVertices(final GraphWriteMethods graph, final boolean treatUndirectedBidirectional, final double dampingFactor, final boolean normaliseByAvailable) {
            PagerankController.graph = graph;
            PagerankController.vertexCount = graph.getVertexCount();
            PagerankController.treatUndirectedBidirectional = treatUndirectedBidirectional;
            PagerankController.normaliseByAvailable = normaliseByAvailable;
            PagerankController.pagerankAttribute = PAGERANK_ATTRIBUTE.ensure(graph);
            PagerankController.dampingFactor = dampingFactor;
            PagerankController.baseContribution = (1 - dampingFactor) / vertexCount;

            graph.vertexStream().forEach(id -> {
                final PagerankVertex prVertex = new PagerankVertex(id, vertexCount);
                pagerankVertices.put(id, prVertex);
            });

            calculateAllOutgoingAndNeighboursAndPagerankContribution();
            updateSinkPagerankContribution();
        }

        private static void calculateAllOutgoingAndNeighboursAndPagerankContribution() {
            for (final PagerankVertex pgVertex : pagerankVertices.values()) {
                // identify incoming connections and store connected vertices and their outgoing connection count
                final int linkCount = graph.getVertexLinkCount(pgVertex.vertexId);
                for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
                    //              For each link
                    final int linkId = graph.getVertexLink(pgVertex.vertexId, linkPosition);
                    final int linkLowId = graph.getLinkLowVertex(linkId);
                    final int linkHighId = graph.getLinkHighVertex(linkId);
                    if (linkLowId != linkHighId) {
                        // If it isnt a link to itself
                        final Integer neighbourId = pgVertex.vertexId == linkLowId ? linkHighId : linkLowId;
                        final int edgeCount = graph.getLinkEdgeCount(linkId);
                        for (int edgePosition = 0; edgePosition < edgeCount; edgePosition++) {
                            // Then for every edge in the link
                            final int edgeId = graph.getLinkEdge(linkId, edgePosition);
                            final int edgeDirection = graph.getEdgeDirection(edgeId);
                            if ((treatUndirectedBidirectional && edgeDirection == GraphConstants.FLAT)
                                    || graph.getEdgeSourceVertex(edgeId) == pgVertex.vertexId) {
                                // If egde is facing outwards, add 1 to outCount.
                                pgVertex.outCount += 1;
                            } else if ((treatUndirectedBidirectional && edgeDirection == GraphConstants.FLAT)
                                    || graph.getEdgeDestinationVertex(edgeId) == pgVertex.vertexId) {
                                // If edge is facing towards vertex add the source as a neighbour.
                                pgVertex.neighbours.add(pagerankVertices.get(neighbourId));
                            }
                        }
                    }
                }
                // If it is a sink (that is it has no outgoing transaction) then treat it as if it connect to every other vertex. Otherwise the total pagerank will gradually reduce from one to zero.
                if (pgVertex.outCount == 0) {
                    pgVertex.outCount = vertexCount - 1;
                    sinks.add(pgVertex);
                    pgVertex.isSink = true;
                }
                pgVertex.updatePagerankContribution();
            }
        }

        private static void updateSinkPagerankContribution() {
            sinkPagerankContribution = sinks.parallelStream().mapToDouble(pgVertex -> pgVertex.pagerankContribution).sum();
        }

        private static void updateAllPageranks() {
            delta = 0;
            double maxPagerank = 0;
            for (final PagerankVertex pgVertex : pagerankVertices.values()) {
                pgVertex.stageNewPagerank(sinkPagerankContribution, baseContribution, dampingFactor);
                delta += Math.abs(pgVertex.pagerank - pgVertex.stagedPagerank);
                if (pgVertex.stagedPagerank > maxPagerank) {
                    maxPagerank = pgVertex.stagedPagerank;
                }
            }
            for (final PagerankVertex pgVertex : pagerankVertices.values()) {
                if (normaliseByAvailable && maxPagerank > 0) {
                    pgVertex.pagerank = pgVertex.stagedPagerank / maxPagerank;
                } else {
                    pgVertex.pagerank = pgVertex.stagedPagerank;
                }
                pgVertex.updatePagerankContribution();
            }
            updateSinkPagerankContribution();
        }

        private static void commitPageranksToGraph() {
            for (final PagerankVertex pgVertex : pagerankVertices.values()) {
                commitPagerankToGraph(pgVertex);
            }
            reset();
        }

        private static void commitPagerankToGraph(final PagerankVertex pagerankVertex) {
            graph.setDoubleValue(pagerankAttribute, pagerankVertex.vertexId, pagerankVertex.pagerank);
        }

        private static void reset() {
            graph = null;
            sinks.clear();
            dampingFactor = 0;
            vertexCount = 0;
            sinkPagerankContribution = 0;
            delta = 0;
            pagerankVertices.clear();
        }
    }

    private static class PagerankVertex {

        private final int vertexId;
        private final Set<PagerankVertex> neighbours = new HashSet<>();
        private int outCount = 0;
        private double pagerank;
        private double pagerankContribution;
        private double stagedPagerank;
        private boolean isSink = false;

        private PagerankVertex(final int vertexId, final int vertexCount) {
            this.vertexId = vertexId;
            this.pagerank = 1.0 / vertexCount;
            this.pagerankContribution = 0;
        }

        private void updatePagerankContribution() {
            pagerankContribution = pagerank / outCount;
        }

        private void stageNewPagerank(final double sinkPagerankContribution, final double baseContribution,
                final double dampingFactor) {
            double neighbourContribution = sinkPagerankContribution;
            if (isSink) {
                neighbourContribution -= pagerankContribution;
            }

            neighbourContribution += neighbours.parallelStream().mapToDouble(pgVertex -> pgVertex.pagerankContribution).sum();

            stagedPagerank = baseContribution + (dampingFactor * neighbourContribution);
        }
    }
}
