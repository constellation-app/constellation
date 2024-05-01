/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.DegreeCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates number of connected components for each vertex.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("AverageDegreePlugin=Average Degree")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class AverageDegreePlugin extends SimpleEditPlugin {

    private static final SchemaAttribute AVERAGE_DEGREE_ATTRIBUTE = SnaConcept.GraphAttribute.AVERAGE_DEGREE;
    private static final SchemaAttribute AVERAGE_IN_DEGREE_ATTRIBUTE = SnaConcept.GraphAttribute.AVERAGE_IN_DEGREE;
    private static final SchemaAttribute AVERAGE_OUT_DEGREE_ATTRIBUTE = SnaConcept.GraphAttribute.AVERAGE_OUT_DEGREE;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(DegreeCentralityPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL = PluginParameter.buildId(DegreeCentralityPlugin.class, "treat_undirected_bidirectional");

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

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean includeConnectionsIn = parameters.getBooleanValue(INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL);

        final int vertexCount = graph.getVertexCount();
        final BitSet includedVertices = new BitSet(vertexCount);
        float sumDegree = 0;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);

            int degree = 0;
            for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(vertexId); neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);

                if (vertexId == neighbourId) {
                    continue;
                }

                boolean isValidLink = false;
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
                if (isValidLink) {
                    degree++;
                    includedVertices.set(neighbourId);

                }
            }

            if (degree > 0) {
                includedVertices.set(vertexId);
            }

            sumDegree += degree;
        }
        final float averageDegree = sumDegree / includedVertices.cardinality();

        // choose the correct degree attribute
        final int averageDegreeAttributeId;
        if (includeConnectionsIn && !includeConnectionsOut) {
            averageDegreeAttributeId = AVERAGE_IN_DEGREE_ATTRIBUTE.ensure(graph);
        } else if (!includeConnectionsIn && includeConnectionsOut) {
            averageDegreeAttributeId = AVERAGE_OUT_DEGREE_ATTRIBUTE.ensure(graph);
        } else {
            averageDegreeAttributeId = AVERAGE_DEGREE_ATTRIBUTE.ensure(graph);
        }

        // update the graph with degree values
        graph.setFloatValue(averageDegreeAttributeId, 0, averageDegree);
    }
}
