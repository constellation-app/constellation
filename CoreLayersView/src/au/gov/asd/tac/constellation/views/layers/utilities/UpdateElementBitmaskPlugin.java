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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.context.LayersAddContextMenu;

/**
 * Updates each selected element's bitmask for viewing them on different layers
 *
 * @author aldebaran30701
 */
public final class UpdateElementBitmaskPlugin extends SimpleEditPlugin {

    private final long targetMask;
    private final LayersAddContextMenu.LayerAction layerAction;

    public UpdateElementBitmaskPlugin(final int bitmask, final LayersAddContextMenu.LayerAction layerAction) {
        this.targetMask = bitmask;
        this.layerAction = layerAction;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        setVertices(graph);
        setTransactions(graph);
    }

    private void setVertices(final GraphWriteMethods graph) {
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexBitmaskAttributeId = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            if (graph.getBooleanValue(vertexSelectedAttributeId, vertexId)) {
                if (layerAction.equals(LayersAddContextMenu.LayerAction.ADD)) {
                    graph.setLongValue(vertexBitmaskAttributeId, vertexId, graph.getLongValue(vertexBitmaskAttributeId, vertexId) | (1 << targetMask));
                } else if (layerAction.equals(LayersAddContextMenu.LayerAction.REMOVE)) {
                    graph.setLongValue(vertexBitmaskAttributeId, vertexId, graph.getLongValue(vertexBitmaskAttributeId, vertexId) & ~(1 << targetMask));
                }
            }
        }
    }

    private void setTransactions(final GraphWriteMethods graph) {
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        final int transactionBitmaskAttributeId = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
        final int transactionCount = graph.getTransactionCount();
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            final int transactionId = graph.getTransaction(transactionPosition);
            if (graph.getBooleanValue(transactionSelectedAttributeId, transactionId)) {
                if (layerAction.equals(LayersAddContextMenu.LayerAction.ADD)) {
                    graph.setLongValue(transactionBitmaskAttributeId, transactionId, graph.getLongValue(transactionBitmaskAttributeId, transactionId) | (1 << targetMask));
                } else if (layerAction.equals(LayersAddContextMenu.LayerAction.REMOVE)) {
                    graph.setLongValue(transactionBitmaskAttributeId, transactionId, graph.getLongValue(transactionBitmaskAttributeId, transactionId) & ~(1 << targetMask));
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
