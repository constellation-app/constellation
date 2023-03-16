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
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates ratio of reciprocity for each pair of vertices. This importance
 * measure does not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("RatioOfReciprocityPlugin=Ratio of Reciprocity")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class RatioOfReciprocityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute RATIO_OF_RECIPROCITY_ATTRIBUTE = SnaConcept.TransactionAttribute.RATIO_OF_RECIPROCITY;

    public static final String TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID = PluginParameter.buildId(RatioOfReciprocityPlugin.class, "treat_undirected_bidirectional");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(RatioOfReciprocityPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> treatUndirectedBidirectionalParameter = BooleanParameterType.build(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        treatUndirectedBidirectionalParameter.setName("Include Undirected");
        treatUndirectedBidirectionalParameter.setDescription("Treat undirected connections as bidirectional connections");
        treatUndirectedBidirectionalParameter.setBooleanValue(true);
        parameters.addParameter(treatUndirectedBidirectionalParameter);

        final PluginParameter<BooleanParameterValue> normaliseToScoresParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseToScoresParameter.setName("Normalise By Max Available Score");
        normaliseToScoresParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseToScoresParameter.setBooleanValue(false);
        parameters.addParameter(normaliseToScoresParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean treatUndirectedBidirectional = parameters.getBooleanValue(TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        // calculate ratio of reciprocity for every pair of vertices on the graph
        float maxRatioOfReciprocity = 0;
        final Map<Integer, Float> ratioOfReciprocities = new HashMap<>();
        final int linkCount = graph.getLinkCount();
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
            final int linkId = graph.getLink(linkPosition);
            final int linkLowId = graph.getLinkLowVertex(linkId);
            final int linkHighId = graph.getLinkHighVertex(linkId);
            if (linkLowId != linkHighId) {
                int transactionInCount = 0;
                int transactionOutCount = 0;
                final int transactionCount = graph.getLinkTransactionCount(linkId);
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                    final int transactionDirection = graph.getTransactionDirection(transactionId);
                    if (treatUndirectedBidirectional && transactionDirection == GraphConstants.FLAT) {
                        transactionInCount++;
                        transactionOutCount++;
                    } else if ((linkLowId < linkHighId && transactionDirection == GraphConstants.DOWNHILL)
                            || (linkLowId > linkHighId && transactionDirection == GraphConstants.UPHILL)) {
                        transactionInCount++;
                    } else if ((linkLowId < linkHighId && transactionDirection == GraphConstants.UPHILL)
                            || (linkLowId > linkHighId && transactionDirection == GraphConstants.DOWNHILL)) {
                        transactionOutCount++;
                    }
                }

                final float ratioOfReciprocity = (float) Math.min(transactionInCount, transactionOutCount) / Math.max(transactionInCount, transactionOutCount);
                ratioOfReciprocities.put(linkId, ratioOfReciprocity);
                maxRatioOfReciprocity = Math.max(ratioOfReciprocity, maxRatioOfReciprocity);
            }
        }

        // update the graph with ratio of reciprocity values
        final int ratioOfReciprocityAttribute = RATIO_OF_RECIPROCITY_ATTRIBUTE.ensure(graph);
        for (final Map.Entry<Integer, Float> entry : ratioOfReciprocities.entrySet()) {
            final int transactionCount = graph.getLinkTransactionCount(entry.getKey());
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionId = graph.getLinkTransaction(entry.getKey(), transactionPosition);
                if (normaliseByAvailable && maxRatioOfReciprocity > 0) {
                    graph.setFloatValue(ratioOfReciprocityAttribute, transactionId, entry.getValue() / maxRatioOfReciprocity);
                } else {
                    graph.setFloatValue(ratioOfReciprocityAttribute, transactionId, entry.getValue());
                }
            }
        }
    }
}
