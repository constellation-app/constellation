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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.triangles.TriangleUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates global clustering coefficient
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("GlobalClusteringCoefficientPlugin=Global Clustering Coefficient")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class GlobalClusteringCoefficientPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute TRIANGLE_COUNT = SnaConcept.GraphAttribute.TRIANGLE_COUNT;
    private static final SchemaAttribute TRIPLET_COUNT = SnaConcept.GraphAttribute.TRIPLET_COUNT;
    private static final SchemaAttribute CLUSTERING_COEFFICIENT = SnaConcept.GraphAttribute.CLUSTERING_COEFFICIENT;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        // calculate triangle and triple counts
        final Tuple<Float, Float> triangleTriplets = TriangleUtilities.countTrianglesTriplets(graph);
        final float triangleCount = triangleTriplets.getFirst();
        final float tripletCount = triangleTriplets.getSecond();
        final float clusteringCoefficient = ((3 * triangleCount) / (tripletCount));

        // update the graph with clustering coefficient
        final int triangleCountAttributeId = TRIANGLE_COUNT.ensure(graph);
        final int tripletCountAttributeId = TRIPLET_COUNT.ensure(graph);
        final int clusteringCoefficientAttributeId = CLUSTERING_COEFFICIENT.ensure(graph);
        graph.setFloatValue(triangleCountAttributeId, 0, triangleCount);
        graph.setFloatValue(tripletCountAttributeId, 0, tripletCount);
        graph.setFloatValue(clusteringCoefficientAttributeId, 0, clusteringCoefficient);
    }
}
