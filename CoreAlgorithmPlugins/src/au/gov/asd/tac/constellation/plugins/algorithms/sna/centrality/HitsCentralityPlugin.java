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
import java.util.Arrays;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates hyperlink-induced topic search (HITS) centrality for each vertex.
 * This centrality measure does not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("HitsCentralityPlugin=HITS Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class HitsCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute HITS_AUTHORITY_ATTRIBUTE = SnaConcept.VertexAttribute.HITS_CENTRALITY_AUTHORITY;
    private static final SchemaAttribute HITS_HUB_ATTRIBUTE = SnaConcept.VertexAttribute.HITS_CENTRALITY_HUB;

    public static final String ITERATIONS_PARAMETER_ID = PluginParameter.buildId(HitsCentralityPlugin.class, "iterations");
    public static final String EPSILON_PARAMETER_ID = PluginParameter.buildId(HitsCentralityPlugin.class, "epsilon");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(HitsCentralityPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

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
        final int iterations = parameters.getIntegerValue(ITERATIONS_PARAMETER_ID);
        final float epsilon = parameters.getFloatValue(EPSILON_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        final double[] authorities = new double[graph.getVertexCapacity()];
        Arrays.fill(authorities, 1);
        final double[] hubs = new double[graph.getVertexCapacity()];
        Arrays.fill(hubs, 1);

        final int vertexCount = graph.getVertexCount();
        for (int iteration = 0; iteration < iterations; iteration++) {
            double authorityNorm = 0;
            double authorityDelta = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final double previousAuthority = authorities[vertexPosition];
                authorities[vertexPosition] = 0;

                final int transactionCount = graph.getVertexTransactionCount(vertexId);
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getVertexTransaction(vertexId, transactionPosition);
                    final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                    final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);
                    if (sourceVertexId != destinationVertexId) {
                        final int otherVertexId = vertexId == sourceVertexId ? destinationVertexId : sourceVertexId;
                        if ((vertexId < otherVertexId && graph.getTransactionDirection(transactionId) == GraphConstants.DOWNHILL)
                                || (vertexId > otherVertexId && graph.getTransactionDirection(transactionId) == GraphConstants.UPHILL)) {
                            authorities[vertexPosition] += hubs[otherVertexId];
                        }
                    }
                    authorityDelta += Math.abs(previousAuthority - authorities[vertexPosition]);
                }
                authorityNorm += Math.pow(authorities[vertexPosition], 2);
            }
            authorityNorm = Math.sqrt(authorityNorm);

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                authorities[vertexPosition] /= authorityNorm;
            }

            double hubNorm = 0;
            double hubDelta = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final double previousHub = hubs[vertexPosition];
                hubs[vertexPosition] = 0;

                final int transactionCount = graph.getVertexTransactionCount(vertexId);
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getVertexTransaction(vertexId, transactionPosition);
                    final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                    final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);
                    if (sourceVertexId != destinationVertexId) {
                        final int otherVertexId = vertexId == sourceVertexId ? destinationVertexId : sourceVertexId;
                        if ((vertexId < otherVertexId && graph.getTransactionDirection(transactionId) == GraphConstants.UPHILL)
                                || (vertexId > otherVertexId && graph.getTransactionDirection(transactionId) == GraphConstants.DOWNHILL)) {
                            hubs[vertexPosition] += authorities[otherVertexId];
                        }
                    }
                    hubDelta += Math.abs(previousHub - hubs[vertexPosition]);
                }
                hubNorm += Math.pow(hubs[vertexPosition], 2);
            }
            hubNorm = Math.sqrt(hubNorm);

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                hubs[vertexPosition] /= hubNorm;
            }

            if (authorityDelta < epsilon || hubDelta < epsilon) {
                break;
            }
        }

        if (normaliseByAvailable) {
            double maxAuthority = 0;
            double maxHub = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                maxAuthority = Math.max(authorities[vertexPosition], maxAuthority);
                maxHub = Math.max(hubs[vertexPosition], maxHub);
            }

            if (maxAuthority != 0 && maxHub != 0) {
                for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                    authorities[vertexPosition] /= maxAuthority;
                    hubs[vertexPosition] /= maxHub;
                }
            }
        }

        // update the graph with hits values
        final int hitsAuthorityAttribute = HITS_AUTHORITY_ATTRIBUTE.ensure(graph);
        final int hitsHubAttribute = HITS_HUB_ATTRIBUTE.ensure(graph);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            graph.setFloatValue(hitsAuthorityAttribute, vertexId, (float) authorities[vertexPosition]);
            graph.setFloatValue(hitsHubAttribute, vertexId, (float) hubs[vertexPosition]);
        }
    }
}
