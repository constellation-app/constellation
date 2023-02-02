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
import java.util.HashSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates the connectivity degree for each vertex.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("ConnectivityDegreePlugin=Connectivity Degree")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class ConnectivityDegreePlugin extends SimpleEditPlugin {

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(ConnectivityDegreePlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(ConnectivityDegreePlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(ConnectivityDegreePlugin.class, "treat_undirected_bidirectional");
    public static final String IGNORE_SINGLETONS = PluginParameter.buildId(ConnectivityDegreePlugin.class, "ignore_singletons");
    public static final String NORMALISE = PluginParameter.buildId(ConnectivityDegreePlugin.class, "normalise");

    private static final SchemaAttribute CONNECTED_COMPONENTS = SnaConcept.VertexAttribute.CONNECTIVITY_DEGREE;
    private static final SchemaAttribute COMPONENT_SIZE = SnaConcept.VertexAttribute.COMPONENT_SIZE;

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

        final PluginParameter<BooleanParameterValue> ignoreSingletons = BooleanParameterType.build(IGNORE_SINGLETONS);
        ignoreSingletons.setName("Ignore Singletons");
        ignoreSingletons.setDescription("Singletons are not treated as graph components");
        ignoreSingletons.setBooleanValue(true);
        parameters.addParameter(ignoreSingletons);

        final PluginParameter<BooleanParameterValue> normalise = BooleanParameterType.build(NORMALISE);
        normalise.setName("Normalise Score");
        normalise.setDescription("When selected, score indicates the number of components on the graph if the vertex was deleted. Otherwise, score indicates how many components are added if the vertex was deleted.");
        normalise.setBooleanValue(true);
        parameters.addParameter(normalise);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL);
        final boolean ignoreSingletons = parameters.getBooleanValue(IGNORE_SINGLETONS);
        final boolean normalise = parameters.getBooleanValue(NORMALISE);

        // calculate eccentricities
        final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.ECCENTRICITY, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional, false);
        final BitSet[] subgraphs = scoreResult.getFirst();
        final float[] eccentricities = scoreResult.getSecond();

        // calculate the number of connected components
        float maxEccentricity = 0;
        final HashMap<BitSet, Float> maxEccentricityConnectedComponents = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        int numComponents = 0;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final float eccentricity = eccentricities[vertexPosition];
            final BitSet subgraph = subgraphs[vertexPosition];
            if (subgraph.cardinality() <= 1) {
                if (!ignoreSingletons) {
                    numComponents += 1;
                }
            } else if (!maxEccentricityConnectedComponents.containsKey(subgraph)) {
                maxEccentricityConnectedComponents.put(subgraph, eccentricity);
                maxEccentricity = Math.max(eccentricity, maxEccentricity);
            } else {
                maxEccentricityConnectedComponents.put(subgraph, Math.max(eccentricity, maxEccentricityConnectedComponents.get(subgraph)));
                maxEccentricity = Math.max(eccentricity, maxEccentricity);
            }
        }

        numComponents += maxEccentricityConnectedComponents.keySet().size();

        // update the graph with betweenness values
        final int ccAttribute = CONNECTED_COMPONENTS.ensure(graph);
        final int cSizeAttribute = COMPONENT_SIZE.ensure(graph);

        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final float eccentricity = eccentricities[vertexPosition];
            final BitSet subgraph = subgraphs[vertexPosition];
            graph.setFloatValue(cSizeAttribute, vertexId, subgraph.cardinality());
            // singleton or singleton with a loop
            if (subgraph.cardinality() <= 1) {
                if (normalise && ignoreSingletons) {
                    graph.setFloatValue(ccAttribute, vertexId, numComponents);
                } else if (normalise && !ignoreSingletons) {
                    graph.setFloatValue(ccAttribute, vertexId, numComponents - 1);
                } else {
                    graph.setFloatValue(ccAttribute, vertexId, 0);
                }
            } else if (subgraph.cardinality() == 2) {
                // subgraph just two connected nodes
                if (normalise && ignoreSingletons) {
                    graph.setFloatValue(ccAttribute, vertexId, numComponents - 1);
                } else if (normalise && !ignoreSingletons) {
                    graph.setFloatValue(ccAttribute, vertexId, numComponents);
                } else {
                    graph.setFloatValue(ccAttribute, vertexId, 0);
                }
            } else if (eccentricity == maxEccentricityConnectedComponents.get(subgraph) || graph.getVertexNeighbourCount(vertexId) == 1) {
                // if on outskirts of subnetwork, deleting won't lower the number of components
                if (normalise) {
                    graph.setFloatValue(ccAttribute, vertexId, numComponents);
                } else {
                    graph.setFloatValue(ccAttribute, vertexId, 0);
                }
            } else {
                // if not on outskirts, will need to calculate how many subgraphs get created if removed
                final BitSet temp = new BitSet(vertexCount);
                temp.or(subgraph);
                temp.set(vertexPosition, false);
                int numPendantNeighbours = 0;
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vertexId); neighbourPosition++) {
                    final int nxId = graph.getVertexNeighbour(vertexId, neighbourPosition);
                    if (graph.getVertexNeighbourCount(nxId) == 1) {
                        temp.set(graph.getVertexPosition(nxId), false);
                        numPendantNeighbours += 1;
                    }
                }
                // all of its neighbours are pendants
                if (temp.isEmpty()) {
                    if (normalise && ignoreSingletons) {
                        graph.setFloatValue(ccAttribute, vertexId, numComponents - 1);
                    } else if (normalise && !ignoreSingletons) {
                        graph.setFloatValue(ccAttribute, vertexId, numComponents + graph.getVertexNeighbourCount(vertexId) - 1);
                    } else if (ignoreSingletons) {
                        graph.setFloatValue(ccAttribute, vertexId, 0);
                    } else {
                        graph.setFloatValue(ccAttribute, vertexId, graph.getVertexNeighbourCount(vertexId));
                    }
                } else {
                    final BitSet[] newSubgraphs = PathScoringUtilities.calculateSubgraphPaths(graph, temp, includeConnectionsIn, includeConnectionsOut, treatUndirectedBidirectional);
                    final HashSet<BitSet> subgraphSet = new HashSet<>();
                    for (int vxPosition = temp.nextSetBit(0); vxPosition >= 0; vxPosition = temp.nextSetBit(vxPosition + 1)) {
                        if (newSubgraphs[vxPosition].cardinality() > 1) {
                            subgraphSet.add(newSubgraphs[vxPosition]);
                        }
                    }
                    if (ignoreSingletons && subgraphSet.size() <= 1 && normalise) {
                        graph.setFloatValue(ccAttribute, vertexId, numComponents);
                    } else if (ignoreSingletons && subgraphSet.size() <= 1 && !normalise) {
                        graph.setFloatValue(ccAttribute, vertexId, 0);
                    } else if (ignoreSingletons && normalise) {
                        graph.setFloatValue(ccAttribute, vertexId, (subgraphSet.size() + numComponents) - 1);
                    } else if (ignoreSingletons) {
                        graph.setFloatValue(ccAttribute, vertexId, subgraphSet.size() - 1);
                    } else if (normalise) {
                        graph.setFloatValue(ccAttribute, vertexId, subgraphSet.size() + numPendantNeighbours + numComponents);
                    } else {
                        graph.setFloatValue(ccAttribute, vertexId, subgraphSet.size() + numPendantNeighbours);
                    }
                }
            }
        }
    }
}
