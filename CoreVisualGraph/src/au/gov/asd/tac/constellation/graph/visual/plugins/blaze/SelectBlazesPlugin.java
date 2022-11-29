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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Plugin framework to add all vertices with blazes to the current selection.
 *
 * @author elnath
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@NbBundle.Messages("SelectBlazesPlugin=Select Blazes")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectBlazesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.get(graph);

        if (blazeAttr != Graph.NOT_FOUND) {
            for (int position = 0; position < graph.getVertexCount(); position++) {
                final int vertexId = graph.getVertex(position);

                if (graph.getObjectValue(blazeAttr, vertexId) != BlazeAttributeDescription.DEFAULT_VALUE) {
                    graph.setBooleanValue(selectedAttr, vertexId, true);
                }
            }
        }
    }
}
