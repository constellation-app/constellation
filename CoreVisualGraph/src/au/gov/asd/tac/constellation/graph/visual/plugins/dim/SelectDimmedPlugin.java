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
package au.gov.asd.tac.constellation.graph.visual.plugins.dim;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework for select dimmed elements
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("SelectDimmedPlugin=Select Dimmed")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectDimmedPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int vxDimAttr = VisualConcept.VertexAttribute.DIMMED.get(graph);
        final int txDimAttr = VisualConcept.TransactionAttribute.DIMMED.get(graph);

        if (vxDimAttr != Graph.NOT_FOUND) {
            final int vxSelectedAttr = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, VisualConcept.VertexAttribute.SELECTED.getName(), VisualConcept.VertexAttribute.SELECTED.getName(), false, null);
            final int vxCount = graph.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                final int vxId = graph.getVertex(position);
                final boolean isDimmed = graph.getBooleanValue(vxDimAttr, vxId);
                if (isDimmed) {
                    graph.setBooleanValue(vxSelectedAttr, vxId, true);
                }
            }
        }

        if (txDimAttr != Graph.NOT_FOUND) {
            final int txSelectedAttr = graph.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, VisualConcept.VertexAttribute.SELECTED.getName(), VisualConcept.VertexAttribute.SELECTED.getName(), false, null);

            final int txCount = graph.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = graph.getTransaction(position);
                final boolean isDimmed = graph.getBooleanValue(txDimAttr, txId);
                if (isDimmed) {
                    graph.setBooleanValue(txSelectedAttr, txId, true);
                }
            }
        }
    }
}
