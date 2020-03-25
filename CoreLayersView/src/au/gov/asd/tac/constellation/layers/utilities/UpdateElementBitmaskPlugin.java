/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;

/**
 * Updates each selected element's bitmask for viewing them on different layers
 *
 * @author aldebaran30701
 */
public final class UpdateElementBitmaskPlugin extends SimpleEditPlugin {

    final int targetMask;

    public UpdateElementBitmaskPlugin(final int bitmask) {
        targetMask = bitmask;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        int graphCurrentBitMask = VisualConcept.GraphAttribute.SELECTEDFILTERMASK.get(graph);

        // VERTEXES
        int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        int vxBitmaskAttributeId = VisualConcept.VertexAttribute.FILTERMASK.get(graph);

        if (vxSelectedAttr != Graph.NOT_FOUND) {
            final int vxCount = graph.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                final int vxId = graph.getVertex(position);

                if (graph.getBooleanValue(vxSelectedAttr, vxId)) {
                    graph.setIntValue(vxBitmaskAttributeId, vxId, graphCurrentBitMask == 1 ? 0b1 : graph.getIntValue(vxBitmaskAttributeId, vxId) | (1 << targetMask - 1));
                }
            }
        }

        // TRANSACTIONS
        int txSelected = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        int txBitmaskAttributeId = VisualConcept.TransactionAttribute.FILTERMASK.get(graph);

        if (txSelected != Graph.NOT_FOUND) {
            final int txCount = graph.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = graph.getTransaction(position);

                if (graph.getBooleanValue(txSelected, txId)) {
                    graph.setIntValue(txBitmaskAttributeId, txId, graphCurrentBitMask == 1 ? 0b1 : graph.getIntValue(txBitmaskAttributeId, txId) | (1 << targetMask - 1));
                }
            }
        }
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Layers View: Update Element Bitmask";
    }
}
