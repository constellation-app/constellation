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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
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
 * plugin framework supporting the de-select all nodes in the graph
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("DeselectVerticesPlugin=Remove from Selection: Nodes")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class DeselectVerticesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int vxSelectedAttrId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        if (vxSelectedAttrId != Graph.NOT_FOUND) {
            final int vxCount = wg.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                if (wg.getBooleanValue(vxSelectedAttrId, vxId)) {
                    wg.setBooleanValue(vxSelectedAttrId, vxId, false);
                }
            }
        }
    }
}
