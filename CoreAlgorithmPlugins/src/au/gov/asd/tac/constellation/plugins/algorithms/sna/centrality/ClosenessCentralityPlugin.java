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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
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
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates closeness centrality for each vertex. This centrality measure does
 * not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ClosenessCentralityPlugin=Closeness Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class ClosenessCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.CLOSENESS_CENTRALITY;
    private static final SchemaAttribute IN_CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.IN_CLOSENESS_CENTRALITY;
    private static final SchemaAttribute OUT_CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.OUT_CLOSENESS_CENTRALITY;
    private static final SchemaAttribute HARMONIC_CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.HARMONIC_CLOSENESS_CENTRALITY;
    private static final SchemaAttribute IN_HARMONIC_CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.IN_HARMONIC_CLOSENESS_CENTRALITY;
    private static final SchemaAttribute OUT_HARMONIC_CLOSENESS_ATTRIBUTE = SnaConcept.VertexAttribute.OUT_HARMONIC_CLOSENESS_CENTRALITY;

    public static final String HARMONIC_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "harmonic");
    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(ClosenessCentralityPlugin.class, "treat_undirected_bidirectional");
    public static final String NORMALISE_POSSIBLE_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "normalise_possible");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "normalise_available");
    public static final String NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "normalise_connected_components");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(ClosenessCentralityPlugin.class, "selected_only");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> harmonicParameter = BooleanParameterType.build(HARMONIC_PARAMETER_ID);
        harmonicParameter.setName("Harmonic");
        harmonicParameter.setDescription("Calculate scores using the harmonic mean");
        harmonicParameter.setBooleanValue(false);
        parameters.addParameter(harmonicParameter);

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
        final boolean harmonic = parameters.getBooleanValue(HARMONIC_PARAMETER_ID);
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL);
        final boolean normaliseByPossible = parameters.getBooleanValue(NORMALISE_POSSIBLE_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final boolean normaliseConnectedComponents = parameters.getBooleanValue(NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID);
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        assert !normaliseByPossible || !normaliseByAvailable : "You should only select one method of normalisation";

        // calculate closeness scores
        final BitSet[] subgraphs;
        final float[] closenesses;
        if (harmonic) {
            final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.HARMONIC_CLOSENESS, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);
            subgraphs = scoreResult.getFirst();
            closenesses = scoreResult.getSecond();
        } else {
            final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.CLOSENESS, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, selectedOnly);
            subgraphs = scoreResult.getFirst();
            closenesses = scoreResult.getSecond();
        }

        // calculate the maximum closeness
        float maxCloseness = 0F;
        final Map<BitSet, Float> maxClosenessConnectedComponents = new HashMap<>();
        final Map<BitSet, Integer> connectedComponentSize = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final float closeness = closenesses[vertexPosition];
            final BitSet subgraph = subgraphs[vertexPosition];
            if (!maxClosenessConnectedComponents.containsKey(subgraph)) {
                maxClosenessConnectedComponents.put(subgraph, closeness);
                connectedComponentSize.put(subgraph, PathScoringUtilities.subgraphSize(graph, subgraph, selectedOnly));
            } else {
                maxClosenessConnectedComponents.put(subgraph, Math.max(closeness, maxClosenessConnectedComponents.get(subgraph)));
            }
            maxCloseness = Math.max(closeness, maxCloseness);
        }

        // choose the correct closeness attribute
        final int closenessAttribute;
        if (includeConnectionsIn && !includeConnectionsOut) {
            closenessAttribute = harmonic ? IN_HARMONIC_CLOSENESS_ATTRIBUTE.ensure(graph) 
                    : IN_CLOSENESS_ATTRIBUTE.ensure(graph);
        } else if (!includeConnectionsIn && includeConnectionsOut) {
            closenessAttribute = harmonic ? OUT_HARMONIC_CLOSENESS_ATTRIBUTE.ensure(graph) 
                    : OUT_CLOSENESS_ATTRIBUTE.ensure(graph);
        } else {
            closenessAttribute = harmonic ? HARMONIC_CLOSENESS_ATTRIBUTE.ensure(graph) 
                    : CLOSENESS_ATTRIBUTE.ensure(graph);
        }

        // update the graph with closeness values
        int graphSize = PathScoringUtilities.subgraphSize(graph, null, selectedOnly);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            if (normaliseByPossible) {
                int subgraphSize = connectedComponentSize.get(subgraphs[vertexPosition]);
                final boolean vertexSelected = graph.getBooleanValue(selectedAttributeId, vertexId);
                if (!selectedOnly || vertexSelected) {
                    subgraphSize -= 1;
                    graphSize -= 1;
                }

                if (normaliseConnectedComponents) {
                    graph.setFloatValue(closenessAttribute, vertexId, closenesses[vertexPosition] * subgraphSize);
                } else {
                    graph.setFloatValue(closenessAttribute, vertexId, closenesses[vertexPosition] * graphSize);
                }
            } else if (normaliseByAvailable && maxCloseness > 0) {
                if (normaliseConnectedComponents) {
                    final float maxClosenessConnectedComponent = maxClosenessConnectedComponents.get(subgraphs[vertexPosition]);
                    graph.setFloatValue(closenessAttribute, vertexId, closenesses[vertexPosition] / maxClosenessConnectedComponent);
                } else {
                    graph.setFloatValue(closenessAttribute, vertexId, closenesses[vertexPosition] / maxCloseness);
                }
            } else {
                graph.setFloatValue(closenessAttribute, vertexId, closenesses[vertexPosition]);
            }
        }
    }
}
