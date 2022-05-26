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
import java.util.Arrays;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates eigenvector centrality for each vertex. This centrality measure
 * does not include loops.
 *
 * @author algol
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@Messages("EigenvectorCentralityPlugin=Eigenvector Centrality")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class EigenvectorCentralityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute EIGENVECTOR_ATTRIBUTE = SnaConcept.VertexAttribute.EIGENVECTOR_CENTRALITY;

    public static final String ITERATIONS_PARAMETER_ID = PluginParameter.buildId(EigenvectorCentralityPlugin.class, "iterations");
    public static final String EPSILON_PARAMETER_ID = PluginParameter.buildId(EigenvectorCentralityPlugin.class, "epsilon");
    public static final String NORMALISE_POSSIBLE_PARAMETER_ID = PluginParameter.buildId(EigenvectorCentralityPlugin.class, "normalise_possible");
    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(EigenvectorCentralityPlugin.class, "normalise_available");

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
        final int iterations = parameters.getIntegerValue(ITERATIONS_PARAMETER_ID);
        final float epsilon = parameters.getFloatValue(EPSILON_PARAMETER_ID);
        final boolean normaliseByPossible = parameters.getBooleanValue(NORMALISE_POSSIBLE_PARAMETER_ID);
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);

        assert !normaliseByPossible || !normaliseByAvailable : "You should only select one method of normalisation";

        // initialise eigenvector values
        final int vertexCount = graph.getVertexCount();
        final double[] tempEigenvectors = new double[graph.getVertexCapacity()];
        final double[] eigenvectors = new double[graph.getVertexCapacity()];
        Arrays.fill(eigenvectors, (double) 1 / vertexCount);

        // calculate eigenvector for each vertex
        for (int iteration = 0; iteration < iterations; iteration++) {
            Arrays.fill(tempEigenvectors, 0);

            double sumEigenvector = 0;
            double maxEigenvector = 0;
            double delta = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);

                final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
                for (int vertexNeighbourPosition = 0; vertexNeighbourPosition < neighbourCount; vertexNeighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(vertexId, vertexNeighbourPosition);
                    final int neighbourPosition = graph.getVertexPosition(neighbourId);
                    if (vertexId != neighbourId) {
                        tempEigenvectors[vertexPosition] += eigenvectors[neighbourPosition];
                    }
                }

                sumEigenvector += tempEigenvectors[vertexPosition];
                maxEigenvector = Math.max(tempEigenvectors[vertexPosition], maxEigenvector);
                delta += Math.abs(eigenvectors[vertexPosition] - tempEigenvectors[vertexPosition]);
            }

            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                if (normaliseByPossible) {
                    eigenvectors[vertexPosition] = tempEigenvectors[vertexPosition] / sumEigenvector;
                } else if (normaliseByAvailable && maxEigenvector > 0) {
                    eigenvectors[vertexPosition] = tempEigenvectors[vertexPosition] / maxEigenvector;
                } else {
                    eigenvectors[vertexPosition] = tempEigenvectors[vertexPosition];
                }
            }

            if (delta < epsilon) {
                break;
            }
        }

        // update the graph with eigenvector values
        final int eigenvectorAttribute = EIGENVECTOR_ATTRIBUTE.ensure(graph);
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            graph.setFloatValue(eigenvectorAttribute, vertexId, (float) eigenvectors[vertexPosition]);
        }
    }
}
