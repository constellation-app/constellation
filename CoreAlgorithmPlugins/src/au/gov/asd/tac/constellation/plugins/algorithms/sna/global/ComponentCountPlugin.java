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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PathScoringUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;
import java.util.HashSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates number of connected components for each vertex.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("ComponentCountPlugin=Component Count")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class ComponentCountPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute COMPONENT_COUNT = SnaConcept.GraphAttribute.COMPONENT_COUNT;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        // calculate eccentricities
        final Tuple<BitSet[], float[]> scoreResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.ECCENTRICITY, true, true, true, false);
        final BitSet[] subgraphs = scoreResult.getFirst();

        // calculate the maximum eccentricity
        final HashSet<BitSet> connectedComponents = new HashSet<>();
        final int vertexCount = graph.getVertexCount();
        int numComponents = 0;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final BitSet subgraph = subgraphs[vertexPosition];
            if (subgraph.cardinality() <= 1) {
                numComponents += 1;
            } else {
                connectedComponents.add(subgraph);
            }
        }

        numComponents += connectedComponents.size();

        // update the graph with number of components values
        final int componentCountAttributeId = COMPONENT_COUNT.ensure(graph);
        graph.setIntValue(componentCountAttributeId, 0, numComponents);
    }
}
