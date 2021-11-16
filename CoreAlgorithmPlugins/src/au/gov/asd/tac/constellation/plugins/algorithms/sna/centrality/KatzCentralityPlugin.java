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
 * Calculates eigenvector centrality for each vertex. This centrality measure
 * does not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("KatzCentralityPlugin=Katz Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class KatzCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute KATZ_ATTRIBUTE = SnaConcept.VertexAttribute.KATZ_CENTRALITY;

    public static final String ALPHA_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "alpha");
    public static final String BETA_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "beta");
    public static final String ITERATIONS_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "iterations");
    public static final String EPSILON_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "epsilon");
    public static final String NORMALISE_POSSIBLE_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "normalise_possible");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(KatzCentralityPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> alphaParameter = FloatParameterType.build(ALPHA_PARAMETER_ID);
        alphaParameter.setName("Alpha");
        alphaParameter.setDescription("The attenuation factor");
        alphaParameter.setFloatValue(0.1f);
        parameters.addParameter(alphaParameter);

        final PluginParameter<FloatParameterValue> betaParameter = FloatParameterType.build(BETA_PARAMETER_ID);
        betaParameter.setName("Beta");
        betaParameter.setDescription("The weight attributed to the immediate neighbourhood");
        betaParameter.setFloatValue(1.0f);
        parameters.addParameter(betaParameter);

        final PluginParameter<IntegerParameterValue> iterationsParameter = IntegerParameterType.build(ITERATIONS_PARAMETER_ID);
        iterationsParameter.setName("Iterations");
        iterationsParameter.setDescription("The number of iterations to run before returning a result");
        iterationsParameter.setIntegerValue(100);
        parameters.addParameter(iterationsParameter);

        final PluginParameter<FloatParameterValue> epsilonParameter = FloatParameterType.build(EPSILON_PARAMETER_ID);
        epsilonParameter.setName("Epsilon");
        epsilonParameter.setDescription("The change threshold at which equilibrium can be considered reached");
        epsilonParameter.setFloatValue(1E-8f);
        parameters.addParameter(epsilonParameter);

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

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final float alpha = parameters.getFloatValue(ALPHA_PARAMETER_ID);
        final float beta = parameters.getFloatValue(BETA_PARAMETER_ID);
        final int iterations = parameters.getIntegerValue(ITERATIONS_PARAMETER_ID);
        final float epsilon = parameters.getFloatValue(EPSILON_PARAMETER_ID);
        final boolean normaliseByPossible = parameters.getBooleanValue(NORMALISE_POSSIBLE_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        assert !normaliseByPossible || !normaliseByAvailable : "You should only select one method of normalisation";

        // initialise katz values
        final int vertexCount = graph.getVertexCount();
        final double[] tempKatz = new double[graph.getVertexCapacity()];
        final double[] katz = new double[graph.getVertexCapacity()];
        Arrays.fill(katz, 1.0 / vertexCount);

        // calculate katz for each vertex
        for (int iteration = 0; iteration < iterations; iteration++) {
            Arrays.fill(tempKatz, 0);

            double sumSquaredKatz = 0;
            double maxKatz = 0;
            double delta = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);

                final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < neighbourCount; vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (vertexId != neighbourId) {
                        tempKatz[neighbourPosition] += katz[vertexPosition];
                    }
                }
            }

            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                tempKatz[vertexPosition] = (alpha * tempKatz[vertexPosition]) + beta;
                sumSquaredKatz += Math.pow(tempKatz[vertexPosition], 2);
                maxKatz = Math.max(tempKatz[vertexPosition], maxKatz);
                delta += Math.abs(katz[vertexPosition] - tempKatz[vertexPosition]);
            }

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                if (normaliseByPossible) {
                    katz[vertexPosition] = tempKatz[vertexPosition] / Math.sqrt(sumSquaredKatz);
                } else if (normaliseByAvailable && maxKatz > 0) {
                    katz[vertexPosition] = tempKatz[vertexPosition] / maxKatz;
                } else {
                    katz[vertexPosition] = tempKatz[vertexPosition];
                }
            }

            if (delta < epsilon) {
                break;
            }
        }

        // update the graph with katz values
        final int katzAttribute = KATZ_ATTRIBUTE.ensure(graph);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            graph.setFloatValue(katzAttribute, vertexId, (float) katz[vertexPosition]);
        }
    }
}
