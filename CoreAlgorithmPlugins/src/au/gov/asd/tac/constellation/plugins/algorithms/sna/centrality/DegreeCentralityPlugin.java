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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates degree centrality for each vertex. This centrality measure does
 * not include loops.
 *
 * @author algol
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@Messages("DegreeCentralityPlugin=Degree Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class DegreeCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute DEGREE_ATTRIBUTE = SnaConcept.VertexAttribute.DEGREE_CENTRALITY;
    private static final SchemaAttribute IN_DEGREE_ATTRIBUTE = SnaConcept.VertexAttribute.IN_DEGREE_CENTRALITY;
    private static final SchemaAttribute OUT_DEGREE_ATTRIBUTE = SnaConcept.VertexAttribute.OUT_DEGREE_CENTRALITY;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(DegreeCentralityPlugin.class, "treat_undirected_bidirectional");
    public static final String NORMALISE_POSSIBLE_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "normalise_possible");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "normalise_available");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "selected_only");

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

        final PluginParameter<BooleanParameterValue> normaliseByPossibleParameter = BooleanParameterType.build(NORMALISE_POSSIBLE_PARAMETER_ID);
        normaliseByPossibleParameter.setName("Normalise By Max Possible Score");
        normaliseByPossibleParameter.setDescription("Normalise calculated scores by the maximum possible score");
        normaliseByPossibleParameter.setBooleanValue(false);
        parameters.addParameter(normaliseByPossibleParameter);

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseByAvailableParameter.setBooleanValue(false);
        parameters.addParameter(normaliseByAvailableParameter);

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
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        assert !normaliseByPossible || !normaliseByAvailable : "You should only select one method of normalisation";

        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        // calculate degree for every vertex on the graph
        int maxDegree = 0;
        final Map<Integer, Float> degrees = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);

            int degree = 0;
            for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vertexId); neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);

                if (vertexId == neighbourId) {
                    continue;
                }

                boolean isValidLink = false;
                if (!selectedOnly || graph.getBooleanValue(vertexSelectedAttributeId, neighbourId)) {
                    final int linkId = graph.getLink(vertexId, neighbourId);
                    for (int linkEdgePosition = 0; linkEdgePosition < graph.getLinkEdgeCount(linkId); linkEdgePosition++) {
                        final int edgeId = graph.getLinkEdge(linkId, linkEdgePosition);
                        final int edgeDirection = graph.getEdgeDirection(edgeId);
                        if ((treatUndirectedBidirectional && edgeDirection == GraphConstants.UNDIRECTED)
                                || (includeConnectionsIn && graph.getEdgeSourceVertex(edgeId) == neighbourId)
                                || (includeConnectionsOut && graph.getEdgeDestinationVertex(edgeId) == neighbourId)) {
                            isValidLink = true;
                            break;
                        }
                    }
                }
                if (isValidLink) {
                    degree++;
                }
            }

            degrees.put(vertexId, (float) degree);
            maxDegree = Math.max(degree, maxDegree);
        }

        // choose the correct degree attribute
        final int degreeAttribute;
        if (includeConnectionsIn && !includeConnectionsOut) {
            degreeAttribute = IN_DEGREE_ATTRIBUTE.ensure(graph);
        } else if (!includeConnectionsIn && includeConnectionsOut) {
            degreeAttribute = OUT_DEGREE_ATTRIBUTE.ensure(graph);
        } else {
            degreeAttribute = DEGREE_ATTRIBUTE.ensure(graph);
        }

        // update the graph with degree values
        for (final Entry<Integer, Float> entry : degrees.entrySet()) {
            if (normaliseByPossible) {
                graph.setFloatValue(degreeAttribute, entry.getKey(), entry.getValue() / (vertexCount - 1));
            } else if (normaliseByAvailable && maxDegree > 0) {
                graph.setFloatValue(degreeAttribute, entry.getKey(), entry.getValue() / maxDegree);
            } else {
                graph.setFloatValue(degreeAttribute, entry.getKey(), entry.getValue());
            }
        }
    }
}
