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
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates the Adamic-Adar index for each pair of vertices.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("AdamicAdarIndexPlugin=Adamic-Adar Index")
@PluginInfo(tags = {"ANALYTIC"})
public class AdamicAdarIndexPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute ADAMIC_ADAR_INDEX_ATTRIBUTE = SnaConcept.TransactionAttribute.ADAMIC_ADAR_INDEX;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "treat_undirected_bidirectional");
    public static final String MINIMUM_COMMON_FEATURES_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "minimum_common_features");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "selected_only");
    public static final String COMMUNITY_PARAMETER_ID = PluginParameter.buildId(AdamicAdarIndexPlugin.class, "community");

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

        final PluginParameter<BooleanParameterValue> communityParameter = BooleanParameterType.build(COMMUNITY_PARAMETER_ID);
        communityParameter.setName("Community Adamic-Adar Index Soundarajan-Hopcroft Score");
        communityParameter.setDescription("Only calculates score when both nodes are selected");
        communityParameter.setBooleanValue(false);
        parameters.addParameter(communityParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        final int minCommonFeatures = parameters.getParameters().get(MINIMUM_COMMON_FEATURES_PARAMETER_ID).getIntegerValue();
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);
        final boolean community = parameters.getBooleanValue(COMMUNITY_PARAMETER_ID);

        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        // map each vertex to its neighbour count
        final int vertexCount = graph.getVertexCount();
        final BitSet[] neighbours = new BitSet[vertexCount];
        final BitSet update = new BitSet(vertexCount);
        final BitSet selected = new BitSet(vertexCount);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            neighbours[vertexPosition] = new BitSet(vertexCount);
            for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < graph.getVertexNeighbourCount(vertexId); vertexNeighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                final int neighbourPosition = graph.getVertexPosition(neighbourId);

                if (vertexPosition == neighbourPosition) {
                    continue;
                }

                final int linkId = graph.getLink(vertexId, neighbourId);
                for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                    final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                    final int edgeDirection = graph.getEdgeDirection(edgeId);
                    final boolean isRequestedDirection = (treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED
                            || includeConnectionsIn && graph.getEdgeDestinationVertex(edgeId) == neighbourId
                            || includeConnectionsOut && graph.getEdgeSourceVertex(edgeId) == neighbourId);
                    if (isRequestedDirection) {
                        if (!SimilarityUtilities.checkEdgeTypes(graph, edgeId)) {
                            continue;
                        }
                        neighbours[vertexPosition].set(neighbourPosition, true);
                        update.set(vertexPosition, true);
                    }
                }
            }
            final boolean vertexSelected = graph.getBooleanValue(vertexSelectedAttributeId, vertexId);
            selected.set(vertexPosition, vertexSelected);
        }

        // calculate Adamic-Adar index for every pair of vertices on the graph
        final Map<Tuple<Integer, Integer>, Float> aaiScores = new HashMap<>();
        for (int vertexOnePosition = update.nextSetBit(0); vertexOnePosition >= 0; vertexOnePosition = update.nextSetBit(vertexOnePosition + 1)) {
            for (int vertexTwoPosition = update.nextSetBit(0); vertexTwoPosition >= 0; vertexTwoPosition = update.nextSetBit(vertexTwoPosition + 1)) {
                if (!selectedOnly || (selected.get(vertexOnePosition) || selected.get(vertexTwoPosition))) {
                    if (vertexOnePosition >= vertexTwoPosition) {
                        continue;
                    }

                    if (community && (!selected.get(vertexOnePosition) || !selected.get(vertexTwoPosition))) {
                        continue;
                    }

                    final BitSet intersection = (BitSet) neighbours[vertexOnePosition].clone();
                    intersection.and(neighbours[vertexTwoPosition]);
                    intersection.set(vertexOnePosition, false);
                    intersection.set(vertexTwoPosition, false);

                    if (intersection.cardinality() < minCommonFeatures) {
                        continue;
                    }

                    final int vertexOneId = graph.getVertex(vertexOnePosition);
                    final int vertexTwoId = graph.getVertex(vertexTwoPosition);
                    float sum = 0f;
                    for (int commonNeighbour = intersection.nextSetBit(0); commonNeighbour >= 0; commonNeighbour = intersection.nextSetBit(commonNeighbour + 1)) {
                        sum += (1f / Math.log(graph.getVertexNeighbourCount(graph.getVertex(commonNeighbour))));
                    }

                    aaiScores.put(Tuple.create(vertexOneId, vertexTwoId), sum);
                }
            }
        }

        // update the graph with Adamic-Adar index values
        SimilarityUtilities.addScoresToGraph(graph, aaiScores, ADAMIC_ADAR_INDEX_ATTRIBUTE);
        // complete with schema
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }
}
