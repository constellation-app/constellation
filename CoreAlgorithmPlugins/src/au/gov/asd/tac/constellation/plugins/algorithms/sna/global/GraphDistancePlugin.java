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
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PathScoringUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates graph diameter and radius, as well as the average path distance.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("GraphDistancePlugin=Graph Distance")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class GraphDistancePlugin extends SimpleEditPlugin {

    private static final SchemaAttribute DIAMETER = SnaConcept.GraphAttribute.DIAMETER;
    private static final SchemaAttribute AVERAGE_DISTANCE = SnaConcept.GraphAttribute.AVERAGE_DISTANCE;
    private static final SchemaAttribute RADIUS = SnaConcept.GraphAttribute.RADIUS;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        // calculate eccentricities
        final Tuple<BitSet[], float[]> eccResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.ECCENTRICITY, true, true, true, false);
        final Tuple<BitSet[], float[]> disResult = PathScoringUtilities.calculateScores(graph, PathScoringUtilities.ScoreType.AVERAGE_DISTANCE, true, true, true, false);
        final float[] ecc = eccResult.getSecond();
        final float[] dis = disResult.getSecond();

        // calculate the maximum eccentricity
        float maxEccentricity = Float.MIN_VALUE;
        float minEccentricity = Float.MAX_VALUE;
        float sum = 0F;
        for (final float eccentricity : ecc) {
            if (minEccentricity > eccentricity) {
                minEccentricity = eccentricity;
            }
            if (maxEccentricity < eccentricity) {
                maxEccentricity = eccentricity;
            }
        }
        for (final float distance : dis) {
            sum += distance;
        }
        final int n = graph.getVertexCount();
        final float averageDistance = sum / (n * (n - 1));
        final int radiusAttributeId = RADIUS.ensure(graph);
        final int diameterAttributeId = DIAMETER.ensure(graph);
        final int averageDistanceAttributeId = AVERAGE_DISTANCE.ensure(graph);
        graph.setFloatValue(radiusAttributeId, 0, minEccentricity);
        graph.setFloatValue(diameterAttributeId, 0, maxEccentricity);
        graph.setFloatValue(averageDistanceAttributeId, 0, averageDistance);
    }
}
