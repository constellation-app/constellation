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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PathScoringUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;
import java.util.HashMap;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates eccentricity for each vertex. This importance measure does not
 * include loops.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("EccentricityPlugin=Eccentricity")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class EccentricityPlugin extends SimpleEditPlugin {

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(EccentricityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(EccentricityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(EccentricityPlugin.class, "treat_undirected_bidirectional");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(EccentricityPlugin.class, "normalise_available");
    public static final String NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID = PluginParameter.buildId(EccentricityPlugin.class, "normalise_connected_components");
    private static final SchemaAttribute ECCENTRICITY_ATTRIBUTE = SnaConcept.VertexAttribute.ECCENTRICITY;

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

        final PluginParameter<BooleanParameterValue> treatUndirectedBidirectionalParameter = BooleanParameterType.build(TREAT_UNDIRECTED_BIDIRECTIONAL);
        treatUndirectedBidirectionalParameter.setName("Include Undirected");
        treatUndirectedBidirectionalParameter.setDescription("Treat undirected connections as bidirectional connections");
        treatUndirectedBidirectionalParameter.setBooleanValue(true);
        parameters.addParameter(treatUndirectedBidirectionalParameter);

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

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final boolean normaliseConnectedComponents = parameters.getBooleanValue(NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID);

        // calculate eccentricities
        final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.ECCENTRICITY, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, false);
        final BitSet[] subgraphs = scoreResult.getFirst();
        final float[] eccentricities = scoreResult.getSecond();

        // calculate the maximum eccentricity
        float maxEccentricity = 0;
        final HashMap<BitSet, Float> maxEccentricityConnectedComponents = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final float betweenness = eccentricities[vertexPosition];
            final BitSet subgraph = subgraphs[vertexPosition];
            if (!maxEccentricityConnectedComponents.containsKey(subgraph)) {
                maxEccentricityConnectedComponents.put(subgraph, betweenness);
            } else {
                maxEccentricityConnectedComponents.put(subgraph, Math.max(betweenness, maxEccentricityConnectedComponents.get(subgraph)));
            }
            maxEccentricity = Math.max(betweenness, maxEccentricity);
        }

        // update the graph with betweenness values
        final int betweennessAttribute = ECCENTRICITY_ATTRIBUTE.ensure(graph);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            if (normaliseByAvailable && maxEccentricity > 0) {
                if (normaliseConnectedComponents) {
                    final float maxBetweennessConnectedComponent = maxEccentricityConnectedComponents.get(subgraphs[vertexPosition]);
                    graph.setFloatValue(betweennessAttribute, vertexId, 1 - (eccentricities[vertexPosition] / maxBetweennessConnectedComponent));
                } else {
                    graph.setFloatValue(betweennessAttribute, vertexId, 1 - (eccentricities[vertexPosition] / maxEccentricity));
                }
            } else {
                graph.setFloatValue(betweennessAttribute, vertexId, eccentricities[vertexPosition]);
            }
        }
    }
}
