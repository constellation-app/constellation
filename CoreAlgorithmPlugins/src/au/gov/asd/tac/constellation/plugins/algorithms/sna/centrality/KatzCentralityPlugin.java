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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
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
import java.util.ArrayList;
import java.util.List;
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
        alphaParameter.setFloatValue(0.1F);
        parameters.addParameter(alphaParameter);

        final PluginParameter<FloatParameterValue> betaParameter = FloatParameterType.build(BETA_PARAMETER_ID);
        betaParameter.setName("Beta");
        betaParameter.setDescription("The weight attributed to the immediate neighbourhood");
        betaParameter.setFloatValue(1.0F);
        parameters.addParameter(betaParameter);

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

        final PluginParameter<BooleanParameterValue> normaliseByPossibleParameter = BooleanParameterType.build(NORMALISE_POSSIBLE_PARAMETER_ID);
        normaliseByPossibleParameter.setName("Normalise By Max Possible Score");
        normaliseByPossibleParameter.setDescription("Normalise calculated scores by the maximum possible score");
        normaliseByPossibleParameter.setBooleanValue(true);
        parameters.addParameter(normaliseByPossibleParameter);

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        parameters.addParameter(normaliseByAvailableParameter);
        
        parameters.addController(NORMALISE_POSSIBLE_PARAMETER_ID, (master, params, change) -> {
            if (change == ParameterChange.VALUE && master.getBooleanValue()) {
                // only one of normalise by max possible or max available can be enabled
                params.get(NORMALISE_AVAILABLE_PARAMETER_ID).setBooleanValue(false);
            }
        });
        
        parameters.addController(NORMALISE_AVAILABLE_PARAMETER_ID, (master, params, change) -> {
            if (change == ParameterChange.VALUE && master.getBooleanValue()) {
                // only one of normalise by max possible or max available can be enabled
                params.get(NORMALISE_POSSIBLE_PARAMETER_ID).setBooleanValue(false);
            }
        });

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
        final List<Double> tempKatz = new ArrayList<>();
        final List<Double> katz = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            tempKatz.add(0.0);
            katz.add(1.0 / vertexCount);
        }
        
        // calculate katz for each vertex
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int i = 0; i < vertexCount; i++) {
                tempKatz.set(i, 0.0);
            }
            
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
                        tempKatz.set(neighbourPosition, tempKatz.get(neighbourPosition) + katz.get(vertexPosition));
                    }
                }
            }

            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                tempKatz.set(vertexPosition, alpha * tempKatz.get(vertexPosition) + beta);
                sumSquaredKatz += Math.pow(tempKatz.get(vertexPosition), 2);
                maxKatz = Math.max(tempKatz.get(vertexPosition), maxKatz);
                delta += Math.abs(katz.get(vertexPosition) - tempKatz.get(vertexPosition));
            }

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                if (normaliseByPossible) {
                    katz.set(vertexPosition, tempKatz.get(vertexPosition) / Math.sqrt(sumSquaredKatz));
                } else if (normaliseByAvailable && maxKatz > 0) {
                    katz.set(vertexPosition, tempKatz.get(vertexPosition) / maxKatz);
                } else {
                    katz.set(vertexPosition, tempKatz.get(vertexPosition));
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
            graph.setFloatValue(katzAttribute, vertexId, Float.parseFloat(katz.get(vertexPosition).toString()));
        }
    }
}
