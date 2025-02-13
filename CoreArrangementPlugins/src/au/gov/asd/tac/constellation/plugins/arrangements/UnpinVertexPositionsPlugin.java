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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Manage the unpinning of vertex positions. A vertex with its PINNED attribute
 * set to true will be ignored (even if selected) when an arrangement plugin is
 * run. This plugin sets the value to false for selected vertexes.
 *
 * @author serpens24
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages({
    "UnpinVertexPositionsPlugin=Unpin position of selected vertexes"
})
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class UnpinVertexPositionsPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final int vxCount = graph.getVertexCount();
        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int pinnedAttr = VisualConcept.VertexAttribute.PINNED.ensure(graph);
        if (vxCount > 0) {
            for (int position = 0; position < vxCount; position++) {
                final int vxId = graph.getVertex(position);
                if (graph.getBooleanValue(selectedAttr, vxId)) {
                    graph.setBooleanValue(pinnedAttr, vxId, false);
                }
            }
        }
    }
}
