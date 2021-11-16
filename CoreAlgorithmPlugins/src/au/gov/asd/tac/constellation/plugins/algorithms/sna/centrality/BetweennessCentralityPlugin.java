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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PathScoringUtilities.ScoreType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates betweenness centrality for each vertex. This centrality measure
 * does not include loops.
 *
 * @author cygnus_x-1
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("BetweennessCentralityPlugin=Betweenness Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class BetweennessCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute BETWEENNESS_ATTRIBUTE = SnaConcept.VertexAttribute.BETWEENNESS_CENTRALITY;
    private static final SchemaAttribute IN_BETWEENNESS_ATTRIBUTE = SnaConcept.VertexAttribute.IN_BETWEENNESS_CENTRALITY;
    private static final SchemaAttribute OUT_BETWEENNESS_ATTRIBUTE = SnaConcept.VertexAttribute.OUT_BETWEENNESS_CENTRALITY;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(BetweennessCentralityPlugin.class, "treat_undirected_bidirectional");
    public static final String NORMALISE_POSSIBLE_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "normalise_possible");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "normalise_available");
    public static final String NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "normalise_connected_components");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(BetweennessCentralityPlugin.class, "selected_only");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> includeConnectionsInParameter = BooleanParameterType.build(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        includeConnectionsInParameter.setName("Include Incoming");
        includeConnectionsInParameter.setDescription("Include incoming connections");
        includeConnectionsInParameter.setBooleanValue(false);
        parameters.addParameter(includeConnectionsInParameter);

        final PluginParameter<BooleanParameterValue> includeConnectionsOutParameter = BooleanParameterType.build(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        includeConnectionsOutParameter.setName("Include Outgoing");
        includeConnectionsOutParameter.setDescription("Include outgoing connections");
        includeConnectionsOutParameter.setBooleanValue(true);
        parameters.addParameter(includeConnectionsOutParameter);

        final PluginParameter<BooleanParameterValue> treatUndirectedBidirectionalParameter = BooleanParameterType.build(TREAT_UNDIRECTED_BIDIRECTIONAL);
        treatUndirectedBidirectionalParameter.setName("Include Undirected");
        treatUndirectedBidirectionalParameter.setDescription("Treat undirected connections as bidirectional connections");
        treatUndirectedBidirectionalParameter.setBooleanValue(true);
        parameters.addParameter(treatUndirectedBidirectionalParameter);

        final PluginParameter<BooleanParameterValue> normaliseByPossibleParameter = BooleanParameterType.build(NORMALISE_POSSIBLE_PARAMETER_ID);
        normaliseByPossibleParameter.setName("Normalise By Max Possible Score");
        normaliseByPossibleParameter.setDescription("Normalise calculated scores by the maximum possible score");
        normaliseByPossibleParameter.setBooleanValue(true);
        parameters.addParameter(normaliseByPossibleParameter);

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseByAvailableParameter.setBooleanValue(false);
        parameters.addParameter(normaliseByAvailableParameter);

        final PluginParameter<BooleanParameterValue> normaliseConnectedComponentsParameter = BooleanParameterType.build(NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID);
        normaliseConnectedComponentsParameter.setName("Normalise Connected Components");
        normaliseConnectedComponentsParameter.setDescription("Apply normalisation separately for each connected component");
        normaliseConnectedComponentsParameter.setBooleanValue(false);
        parameters.addParameter(normaliseConnectedComponentsParameter);

        final PluginParameter<BooleanParameterValue> selectedOnlyParameter = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selectedOnlyParameter.setName("Selected Only");
        selectedOnlyParameter.setDescription("Calculate using only selected elements");
        selectedOnlyParameter.setBooleanValue(false);
        parameters.addParameter(selectedOnlyParameter);

        return parameters;
    }
    
    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL);
        final boolean normaliseByPossible = parameters.getBooleanValue(NORMALISE_POSSIBLE_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final boolean normaliseConnectedComponents = parameters.getBooleanValue(NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID);
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        assert !normaliseByPossible || !normaliseByAvailable : "You should only select one method of normalisation";

        // calculate betweenness scores
        final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, ScoreType.BETWEENNESS, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);
        final BitSet[] subgraphs = scoreResult.getFirst();
        final float[] betweennesses = scoreResult.getSecond();

        // calculate the maximum betweenness
        float maxBetweenness = 0;
        final Map<BitSet, Float> maxBetweennessConnectedComponents = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final float betweenness = betweennesses[vertexPosition];
            final BitSet subgraph = subgraphs[vertexPosition];
            if (!maxBetweennessConnectedComponents.containsKey(subgraph)) {
                maxBetweennessConnectedComponents.put(subgraph, betweenness);
            } else {
                maxBetweennessConnectedComponents.put(subgraph, Math.max(betweenness, maxBetweennessConnectedComponents.get(subgraph)));
            }
            maxBetweenness = Math.max(betweenness, maxBetweenness);
        }

        // choose the correct betweenness attribute
        final int betweennessAttribute;
        if (includeConnectionsIn && !includeConnectionsOut) {
            betweennessAttribute = IN_BETWEENNESS_ATTRIBUTE.ensure(graph);
        } else if (!includeConnectionsIn && includeConnectionsOut) {
            betweennessAttribute = OUT_BETWEENNESS_ATTRIBUTE.ensure(graph);
        } else {
            betweennessAttribute = BETWEENNESS_ATTRIBUTE.ensure(graph);
        }

        // update the graph with betweenness values
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final float betweennessAttributeValue;
            if (normaliseByPossible) {
                if (normaliseConnectedComponents) {
                    final float subgraphVertexCount = subgraphs[vertexPosition].cardinality();
                    betweennessAttributeValue = betweennesses[vertexPosition] / (((subgraphVertexCount - 1) * (subgraphVertexCount - 2)) / 2);
                } else {
                    betweennessAttributeValue = betweennesses[vertexPosition] / (((vertexCount - 1) * (vertexCount - 2)) / 2f);
                }
            } else if (normaliseByAvailable && maxBetweenness > 0) {
                if (normaliseConnectedComponents) {
                    final float maxBetweennessConnectedComponent = maxBetweennessConnectedComponents.get(subgraphs[vertexPosition]);
                    betweennessAttributeValue = betweennesses[vertexPosition] / maxBetweennessConnectedComponent;
                } else {
                    betweennessAttributeValue = betweennesses[vertexPosition] / maxBetweenness;
                }
            } else {
                betweennessAttributeValue = betweennesses[vertexPosition];
            }

            graph.setFloatValue(betweennessAttribute, vertexId, Float.isNaN(betweennessAttributeValue) ? 0 : betweennessAttributeValue);
        }
    }
}
