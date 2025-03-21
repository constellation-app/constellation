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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.MatrixUtilities;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.ArrayList;
import org.ejml.simple.SimpleMatrix;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates effective resistance (or resistance distance) for each link. Results are stored on the transactions
 * constituting that link.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("EffectiveResistancePlugin=Effective Resistance")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class EffectiveResistancePlugin extends SimpleEditPlugin {

    private static final SchemaAttribute EFFECTIVE_RESISTANCE_ATTRIBUTE = SnaConcept.TransactionAttribute.EFFECTIVE_RESISTANCE;

    public static final String WEIGHTED_PARAMETER_ID = PluginParameter.buildId(EffectiveResistancePlugin.class, "weighted");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(EffectiveResistancePlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> weightedParameter = BooleanParameterType.build(WEIGHTED_PARAMETER_ID);
        weightedParameter.setName("Weighted");
        weightedParameter.setDescription("Account for the weight of links");
        weightedParameter.setBooleanValue(false);
        parameters.addParameter(weightedParameter);

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseByAvailableParameter.setBooleanValue(false);
        parameters.addParameter(normaliseByAvailableParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean weighted = parameters.getBooleanValue(WEIGHTED_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        final Tuple<SimpleMatrix, ArrayList<Integer>> result = MatrixUtilities.inverseLaplacianCompact(graph);
        final SimpleMatrix iL = result.getFirst();
        final ArrayList<Integer> updatedVertexIndexArray = result.getSecond();

        final int linkCount = graph.getLinkCount();
        final double[] resistances = new double[linkCount];
        double maxResistance = 0;
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
            final int linkId = graph.getLink(linkPosition);
            final int lowVertexId = graph.getLinkLowVertex(linkId);
            final int highVertexId = graph.getLinkHighVertex(linkId);

            final int i = updatedVertexIndexArray.indexOf(graph.getVertexPosition(lowVertexId));
            final int j = updatedVertexIndexArray.indexOf(graph.getVertexPosition(highVertexId));
            final double resistance;
            if (weighted) {
                final int linkTransactionCount = graph.getLinkTransactionCount(linkId);
                resistance = linkTransactionCount * (iL.get(i, i) + iL.get(j, j) - iL.get(i, j) - iL.get(j, i));
            } else {
                resistance = iL.get(i, i) + iL.get(j, j) - iL.get(i, j) - iL.get(j, i);
            }

            resistances[linkPosition] = resistance;
            maxResistance = Math.max(resistance, maxResistance);
        }

        final int effectiveResistanceAttributeId = EFFECTIVE_RESISTANCE_ATTRIBUTE.ensure(graph);
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
            final int linkId = graph.getLink(linkPosition);
            final int linkTransactionCount = graph.getLinkTransactionCount(linkId);
            for (int linkTransactionPosition = 0; linkTransactionPosition < linkTransactionCount; linkTransactionPosition++) {
                final int linkTransactionId = graph.getLinkTransaction(linkId, linkTransactionPosition);
                graph.setDoubleValue(effectiveResistanceAttributeId, linkTransactionId,
                        normaliseByAvailable && maxResistance > 0 ? resistances[linkPosition] / maxResistance : resistances[linkPosition]);
            }
        }
    }
}
