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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates graph density
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("GraphDensityPlugin=Graph Density")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class GraphDensityPlugin extends SimpleEditPlugin {

    private static final SchemaAttribute DENSITY = SnaConcept.GraphAttribute.DENSITY;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final float edgeCount = graph.getEdgeCount();
        final float vertexCount = graph.getVertexCount();
        final float density = ((edgeCount) / (vertexCount * (vertexCount - 1)));

        // update the graph with density
        final int densityAttributeId = DENSITY.ensure(graph);
        graph.setFloatValue(densityAttributeId, 0, density);
    }
}
