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
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
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
import java.util.HashSet;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates multiplexity for each pair of vertices. This importance measure
 * does not include loops.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("MultiplexityPlugin=Multiplexity")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class MultiplexityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute MULTIPLEXITY_ATTRIBUTE = SnaConcept.TransactionAttribute.MULTIPLEXITY;
    private static final SchemaAttribute TYPE_ATTRIBUTE = AnalyticConcept.TransactionAttribute.TYPE;

    public static final String GROUP_BY_TOP_LEVEL_TYPE = PluginParameter.buildId(MultiplexityPlugin.class, "top_level_type");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(MultiplexityPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> topLevelType = BooleanParameterType.build(GROUP_BY_TOP_LEVEL_TYPE);
        topLevelType.setName("Group by Top Level Type");
        topLevelType.setDescription("Transactions are grouped by top level type as opposed to subtype");
        topLevelType.setBooleanValue(false);
        parameters.addParameter(topLevelType);

        final PluginParameter<BooleanParameterValue> normaliseToScoresParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseToScoresParameter.setName("Normalise By Max Available Score");
        normaliseToScoresParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseToScoresParameter.setBooleanValue(false);
        parameters.addParameter(normaliseToScoresParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean topLevelType = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final int typeAttribute = TYPE_ATTRIBUTE.ensure(graph);

        // calculate multiplexity for every pair of vertices on the graph
        float maxMultiplexity = 0;
        final Map<Integer, Float> multiplexities = new HashMap<>();
        final int linkCount = graph.getLinkCount();
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
            final int linkId = graph.getLink(linkPosition);
            final int linkLowId = graph.getLinkLowVertex(linkId);
            final int linkHighId = graph.getLinkHighVertex(linkId);
            final HashSet<SchemaTransactionType> types = new HashSet<>();
            if (linkLowId != linkHighId) {
                final int transactionCount = graph.getLinkTransactionCount(linkId);
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                    SchemaTransactionType type = graph.getObjectValue(typeAttribute, transactionId);
                    if (type != null) {
                        if (topLevelType) {
                            type = (SchemaTransactionType) type.getTopLevelType();
                        }
                        types.add(type);
                    }
                }

                final float multiplexity = (float) types.size();
                multiplexities.put(linkId, multiplexity);
                maxMultiplexity = Math.max(multiplexity, maxMultiplexity);
            }
        }

        // update the graph with multiplexity values
        final int multiplexityAttribute = MULTIPLEXITY_ATTRIBUTE.ensure(graph);
        for (final Map.Entry<Integer, Float> entry : multiplexities.entrySet()) {
            final int transactionCount = graph.getLinkTransactionCount(entry.getKey());
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionId = graph.getLinkTransaction(entry.getKey(), transactionPosition);
                if (normaliseByAvailable && maxMultiplexity > 0) {
                    graph.setFloatValue(multiplexityAttribute, transactionId, entry.getValue() / maxMultiplexity);
                } else {
                    graph.setFloatValue(multiplexityAttribute, transactionId, entry.getValue());
                }
            }
        }
    }
}
