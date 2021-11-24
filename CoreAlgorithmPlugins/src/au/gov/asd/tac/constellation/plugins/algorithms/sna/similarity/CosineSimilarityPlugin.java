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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates cosine similarity for each pair of vertices. This similarity
 * measure does not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("CosineSimilarityPlugin=Cosine Similarity")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class CosineSimilarityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute COSINE_SIMILARITY_ATTRIBUTE = SnaConcept.TransactionAttribute.COSINE_SIMILARITY;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(CosineSimilarityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(CosineSimilarityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID = PluginParameter.buildId(CosineSimilarityPlugin.class, "treat_undirected_bidirectional");
    public static final String MINIMUM_COMMON_FEATURES_PARAMETER_ID = PluginParameter.buildId(CosineSimilarityPlugin.class, "minimum_common_features");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(CosineSimilarityPlugin.class, "selected_only");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> includeConnectionsInParameter = BooleanParameterType.build(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        includeConnectionsInParameter.setName("Include Incoming");
        includeConnectionsInParameter.setDescription("Include incoming connections");
        includeConnectionsInParameter.setBooleanValue(true);
        parameters.addParameter(includeConnectionsInParameter);

        final PluginParameter<BooleanParameterValue> includeConnectionsOutParameter = BooleanParameterType.build(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        includeConnectionsOutParameter.setName("Include Outgoing");
        includeConnectionsOutParameter.setDescription("Include outgoing connections");
        includeConnectionsOutParameter.setBooleanValue(true);
        parameters.addParameter(includeConnectionsOutParameter);

        final PluginParameter<BooleanParameterValue> treatUndirectedBidirectionalParameter = BooleanParameterType.build(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        treatUndirectedBidirectionalParameter.setName("Include Undirected");
        treatUndirectedBidirectionalParameter.setDescription("Treat undirected connections as bidirectional connections");
        treatUndirectedBidirectionalParameter.setBooleanValue(true);
        parameters.addParameter(treatUndirectedBidirectionalParameter);

        final PluginParameter<IntegerParameterValue> minCommonFeatures = IntegerParameterType.build(MINIMUM_COMMON_FEATURES_PARAMETER_ID);
        minCommonFeatures.setName("Minimum Common Features");
        minCommonFeatures.setDescription("Only calculate similarity between nodes that share at least this many features");
        minCommonFeatures.setIntegerValue(3);
        IntegerParameterType.setMinimum(minCommonFeatures, 1);
        parameters.addParameter(minCommonFeatures);

        final PluginParameter<BooleanParameterValue> selectedOnlyParameter = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selectedOnlyParameter.setName("Selected Only");
        selectedOnlyParameter.setDescription("Calculate using only selected elements");
        selectedOnlyParameter.setBooleanValue(false);
        parameters.addParameter(selectedOnlyParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        final int minCommonFeatures = parameters.getParameters().get(MINIMUM_COMMON_FEATURES_PARAMETER_ID).getIntegerValue();
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        // map each vertex to its neighbour count
        final int vertexCount = graph.getVertexCount();
        final List<VertexWithNeighbours> verticiesWithNeighbours = new ArrayList<>();

        VertexWithNeighbours currentVertexWithNeighbour;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) { //For each vertex
            currentVertexWithNeighbour = null;
            final int vertexId = graph.getVertex(vertexPosition);

            int vertexNeighbourCount = 0;
            if (graph.getVertexNeighbourCount(vertexId) >= minCommonFeatures) { //Quick defeat, if there arnt enough potential neighbours to achieve the minimal common features then dont process the vertex.
                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) { //For each neighbour
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);

                    if (vertexPosition == neighbourPosition) {
                        // Exclude self
                        continue;
                    }

                    final int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        final boolean isRequestedDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED
                                || includeConnectionsIn && graph.getEdgeDestinationVertex(edgeId) == vertexId
                                || includeConnectionsOut && graph.getEdgeSourceVertex(edgeId) == vertexId);
                        if (isRequestedDirection) { // If it is a neighbour we are interested in, based on whetehr we are interest in incoing or outoing neighbours.
                            // neighbour weight vertex-neighbour = Number of transaction of correct direction between them - number of transacions of type similarity.
                            if (currentVertexWithNeighbour == null) {
                                currentVertexWithNeighbour = new VertexWithNeighbours(vertexId, vertexCount, graph.getBooleanValue(vertexSelectedAttributeId, vertexId));
                            }

                            final int weight = graph.getEdgeTransactionCount(edgeId) - SimilarityUtilities.countEdgeSimilarityTransactions(graph, edgeId);
                            currentVertexWithNeighbour.addNeighbour(neighbourPosition, weight);
                            vertexNeighbourCount += 1; // Found  valid neighbour so track this.
                        }
                    }
                }
            }
            if (vertexNeighbourCount >= minCommonFeatures) {
                // Add vertex to list of verticies with neighbours IFF it has enough neighbours to potentially pass the minCommonFeatures test later.
                verticiesWithNeighbours.add(currentVertexWithNeighbour);
            }
        }
        // At this point:
        // neighbourweights[vertexPosition][neighbourposition] = number of relevant transactions between them - number of transaction of type similarity between them.
        // neighbours[vertexPosition] = bitset representing whetehr each vertex is a neighbour or not
        // update = bitset representing whether the vertex has any neighbours at all.

        SimilarityUtilities.setGraphAndEnsureAttributes(graph, COSINE_SIMILARITY_ATTRIBUTE);
        // calculate cosine similarity for every pair of vertices on the graph
        for (int leftIndex = 0; leftIndex < verticiesWithNeighbours.size() - 1; leftIndex++) {
            final VertexWithNeighbours leftVertexWithNeighbours = verticiesWithNeighbours.get(leftIndex);
            for (int rightIndex = leftIndex + 1; rightIndex < verticiesWithNeighbours.size(); rightIndex++) {
                final VertexWithNeighbours rightVertexWithNeighbours = verticiesWithNeighbours.get(rightIndex);
                if (!selectedOnly || leftVertexWithNeighbours.selected || rightVertexWithNeighbours.selected) { // if we care about selected ensure that one of the verticies is selected.
                    // Get a bitset that tells you which vertexPositions had both verticies as a neighbour.
                    final BitSet commonNeighbours = getCommonNeighbours(leftVertexWithNeighbours.neighbours, rightVertexWithNeighbours.neighbours);

                    if (commonNeighbours.cardinality() >= minCommonFeatures) {
                        // If passes minCommonFeatures condition
                        final float neighbourDotProduct = getNeighbourDotProduct(leftVertexWithNeighbours, rightVertexWithNeighbours, commonNeighbours);
                        final float neighboursMagnitude = leftVertexWithNeighbours.getMagnitude() * rightVertexWithNeighbours.getMagnitude();
                        final float cosineSimilarity = neighboursMagnitude == 0 ? 0 : neighbourDotProduct / neighboursMagnitude;
                        SimilarityUtilities.addScoreToGraph(leftVertexWithNeighbours.vertexId, rightVertexWithNeighbours.vertexId, cosineSimilarity);
                    }
                }
            }
        }
        // complete with schema
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }

    private class VertexWithNeighbours {

        private final int vertexId;
        private final Map<Integer, Integer> neighbourWeightsMap;
        private BitSet neighbours;
        private final boolean selected;
        private float magnitude;
        private boolean recalculateMagnitude = true;

        public VertexWithNeighbours(final int vertexId, final int vertexCount, final boolean selected) {
            this.neighbourWeightsMap = new HashMap<>();
            this.vertexId = vertexId;
            this.selected = selected;
            this.neighbours = new BitSet(vertexCount);
        }

        private void addNeighbour(final int neighbourPosition, final int additionalWeight) {
            final int currentWeight = neighbourWeightsMap.getOrDefault(neighbourPosition, 0);
            neighbourWeightsMap.put(neighbourPosition, currentWeight + additionalWeight);
            neighbours.set(neighbourPosition, true);
            recalculateMagnitude = true;
        }

        private float getMagnitude() {
            if (recalculateMagnitude) {
                magnitude = calculateMagnitude();
            }
            return magnitude;
        }

        private float calculateMagnitude() {
            float mag = 0;
            for (final int neighbourWeight : neighbourWeightsMap.values()) {
                mag += Math.pow(neighbourWeight, 2);
            }
            recalculateMagnitude = false;
            return (float) Math.sqrt(mag);
        }

    }

    BitSet getCommonNeighbours(final BitSet leftVertexNeighbours, final BitSet rightVertexNeighbours) {
        final BitSet intersection = (BitSet) leftVertexNeighbours.clone();
        intersection.and(rightVertexNeighbours);
        return intersection;
    }

    float getNeighbourDotProduct(final VertexWithNeighbours vertex1, final VertexWithNeighbours vertex2, final BitSet intersection) {
        float dot = 0;
        for (int index = intersection.nextSetBit(0); index >= 0; index = intersection.nextSetBit(index + 1)) {
            dot += vertex1.neighbourWeightsMap.get(index) * vertex2.neighbourWeightsMap.get(index);
        }
        return dot;
    }

}
