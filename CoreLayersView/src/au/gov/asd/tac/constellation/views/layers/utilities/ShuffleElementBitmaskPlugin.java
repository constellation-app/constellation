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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;

/**
 * Updates each selected element's bitmask for viewing them on different layers
 *
 * @author aldebaran30701
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public final class ShuffleElementBitmaskPlugin extends SimpleEditPlugin {

    private final int startIndex;
    private int currentIndex;

    public ShuffleElementBitmaskPlugin(final int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        int countIndex = startIndex;
        currentIndex = startIndex;
        while (countIndex <= 64) {
            setVertices(graph);
            setTransactions(graph);
            currentIndex++;
            countIndex = (1 << currentIndex);
        }
    }

    private void setVertices(final GraphWriteMethods graph) {
        final int vertexBitmaskAttributeId = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);

            final long currentLong = graph.getLongValue(vertexBitmaskAttributeId, vertexId);
            // If the next layer is a match, set that value to then match the current startIndex
            if ((currentLong & (1 << currentIndex + 1)) != 0) {
                graph.setLongValue(vertexBitmaskAttributeId, vertexId, incrementBitmask(currentIndex, currentLong));
            }
        }
    }

    private void setTransactions(final GraphWriteMethods graph) {
        final int transactionBitmaskAttributeId = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
        final int transactionCount = graph.getTransactionCount();
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            final int transactionId = graph.getTransaction(transactionPosition);

            final long currentLong = graph.getLongValue(transactionBitmaskAttributeId, transactionId);
            // If the next layer is a match, set that value to then match the current startIndex
            if ((currentLong & (1 << currentIndex + 1)) != 0) {
                graph.setLongValue(transactionBitmaskAttributeId, transactionId, incrementBitmask(currentIndex, currentLong));
            }
        }
    }

    /**
     * Remove the next index above the current index from the current bitmask,
     * and add the current index to the current bitmask.
     *
     * Effectively removing a vertex from a higher layer and adding it to a
     * layer closer to 0.
     *
     * Protected access is required for unit tests.
     *
     * @param currentIndex The current layer (empty position) to move the layer
     * to
     * @param currentBitmask the current bitmask to increment.
     * @return the new bitmask which has shifted up a position.
     */
    protected static long incrementBitmask(final int currentIndex, final long currentBitmask) {
        final long bitmaskWithNextIndexRemoved = currentBitmask & ~(1 << (currentIndex + 1));
        return bitmaskWithNextIndexRemoved | (1 << currentIndex);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Layers View: Shuffle Element Bitmask";
    }
}
