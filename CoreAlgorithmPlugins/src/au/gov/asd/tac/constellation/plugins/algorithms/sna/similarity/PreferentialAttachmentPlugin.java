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
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates preferential attachment for each pair of vertices.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("PreferentialAttachmentPlugin=Preferential Attachment")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class PreferentialAttachmentPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute PREFERENTIAL_ATTACHMENT_ATTRIBUTE = SnaConcept.TransactionAttribute.PREFERENTIAL_ATTACHMENT;

    public static final String INCLUDE_CONNECTIONS_IN_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentPlugin.class, "include_connections_in");
    public static final String INCLUDE_CONNECTIONS_OUT_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentPlugin.class, "include_connections_out");
    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentPlugin.class, "treat_undirected_bidirectional");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(PreferentialAttachmentPlugin.class, "selected_only");

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
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

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

        // calculate preferential attachment for every pair of vertices on the graph
        SimilarityUtilities.setGraphAndEnsureAttributes(graph, PREFERENTIAL_ATTACHMENT_ATTRIBUTE);
        for (int vertexOnePosition = update.nextSetBit(0); vertexOnePosition >= 0; vertexOnePosition = update.nextSetBit(vertexOnePosition + 1)) {
            for (int vertexTwoPosition = update.nextSetBit(0); vertexTwoPosition >= 0; vertexTwoPosition = update.nextSetBit(vertexTwoPosition + 1)) {
                if (!selectedOnly || (selected.get(vertexOnePosition) || selected.get(vertexTwoPosition))) {
                    if (vertexOnePosition >= vertexTwoPosition) {
                        continue;
                    }

                    final int vertexOneId = graph.getVertex(vertexOnePosition);
                    final int vertexTwoId = graph.getVertex(vertexTwoPosition);

                    final float paScore = graph.getVertexNeighbourCount(vertexOneId) * graph.getVertexNeighbourCount(vertexTwoId);

                    SimilarityUtilities.addScoreToGraph(vertexOneId, vertexTwoId, paScore);
                }
            }
        }
        // complete with schema
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }
}
