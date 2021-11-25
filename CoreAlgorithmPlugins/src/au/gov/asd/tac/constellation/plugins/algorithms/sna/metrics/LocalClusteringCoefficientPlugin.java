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
import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates clustering coefficient for each vertex. This importance measure
 * does not include loops.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("LocalClusteringCoefficientPlugin=Local Clustering Coefficient")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class LocalClusteringCoefficientPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute LOCAL_CLUSTERING_COEFFICIENT_ATTRIBUTE = SnaConcept.VertexAttribute.CLUSTERING_COEFFICIENT;

    public static final String NORMALISE_AVAILABLE_PARAMETER_ID = PluginParameter.buildId(LocalClusteringCoefficientPlugin.class, "normalise_available");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> normaliseByAvailableParameter = BooleanParameterType.build(NORMALISE_AVAILABLE_PARAMETER_ID);
        normaliseByAvailableParameter.setName("Normalise By Max Available Score");
        normaliseByAvailableParameter.setDescription("Normalise calculated scores by the maximum calculated score");
        normaliseByAvailableParameter.setBooleanValue(true);
        parameters.addParameter(normaliseByAvailableParameter);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean normaliseByAvailable = parameters.getBooleanValue(NORMALISE_AVAILABLE_PARAMETER_ID);
        final int localClusteringCoefficientAttribute = LOCAL_CLUSTERING_COEFFICIENT_ATTRIBUTE.ensure(graph);

        // map each vertex to its neighbours
        final Map<Integer, BitSet> neighbourMap = new HashMap<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final BitSet vertexNeighbours = new BitSet(vertexCount);

            final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
            for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);
                final int actualNeighbourPosition = graph.getVertexPosition(neighbourId);
                vertexNeighbours.set(actualNeighbourPosition);
            }

            neighbourMap.put(vertexPosition, vertexNeighbours);
        }

        // compute the local clustering coefficient for each vertex
        float maxLocalClusteringCoefficient = 0;
        final Map<Integer, Float> localClusteringCoefficients = new HashMap<>();
        for (final Map.Entry<Integer, BitSet> entry : neighbourMap.entrySet()) {
            final int vertexId = graph.getVertex(entry.getKey());
            final BitSet vertexNeighbours = entry.getValue();

            // calculate the number of connected neighbours (remembering to divide
            // by two as we will see each pair twice, once from each direction)
            int connectedNeighbourPairs = 0;
            for (int neighbourPosition = vertexNeighbours.nextSetBit(0); neighbourPosition >= 0; neighbourPosition = vertexNeighbours.nextSetBit(neighbourPosition + 1)) {
                if (neighbourPosition == entry.getKey()) {
                    continue;
                }
                final BitSet neighbourNeighbours = neighbourMap.get(neighbourPosition);
                final BitSet intersection = (BitSet) neighbourNeighbours.clone();
                intersection.and(vertexNeighbours);
                connectedNeighbourPairs += intersection.cardinality();
            }
            connectedNeighbourPairs /= 2;

            // calculate the number of neighbour pairs and then the local clustering coefficient
            final int allNeighbourPairs = numberUniquePairs(vertexNeighbours.cardinality(), 2);
            final float localClusteringCoefficient = allNeighbourPairs == 0
                    ? 0 : (float) connectedNeighbourPairs / allNeighbourPairs;
            maxLocalClusteringCoefficient = Math.max(localClusteringCoefficient, maxLocalClusteringCoefficient);
            localClusteringCoefficients.put(vertexId, localClusteringCoefficient);
        }

        // update the graph with degree values
        for (final Map.Entry<Integer, Float> entry : localClusteringCoefficients.entrySet()) {
            if (normaliseByAvailable && maxLocalClusteringCoefficient > 0) {
                graph.setFloatValue(localClusteringCoefficientAttribute, entry.getKey(), entry.getValue() / maxLocalClusteringCoefficient);
            } else {
                graph.setFloatValue(localClusteringCoefficientAttribute, entry.getKey(), entry.getValue());
            }
        }
    }

    private int numberUniquePairs(final int numberItems, final int numberPerPair) {
        return factorial(numberItems)
                .divide(factorial(numberPerPair)
                        .multiply(factorial(numberItems - numberPerPair))
                ).intValueExact();
    }

    private static BigInteger factorial(final int n) {
        BigInteger factorial = new BigInteger(String.valueOf(1));
        for (int i = 1; i <= n; i++) {
            factorial = factorial.multiply(new BigInteger(String.valueOf(i)));
        }

        return factorial;
    }
}
