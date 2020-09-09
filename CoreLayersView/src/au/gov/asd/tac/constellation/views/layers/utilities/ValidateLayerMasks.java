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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public final class ValidateLayerMasks extends SimpleEditPlugin {

    public ValidateLayerMasks() {
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {

        final int graphCurrentBitMaskId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        final int currentBitmask = graph.getIntValue(graphCurrentBitMaskId, 0);

        final int layerQueryAttributeId = LayersConcept.GraphAttribute.LAYER_QUERIES.get(graph);
        final int layerPreferencesAttributeId = LayersConcept.GraphAttribute.LAYER_PREFERENCES.get(graph);

        final List<String> queries = CollectionUtils.isEmpty(graph.getObjectValue(layerQueryAttributeId, 0)) ? new ArrayList<>() : graph.getObjectValue(layerQueryAttributeId, 0);
        final List<Byte> preferences = CollectionUtils.isEmpty(graph.getObjectValue(layerPreferencesAttributeId, 0)) ? new ArrayList<>() : graph.getObjectValue(layerPreferencesAttributeId, 0);

        if (CollectionUtils.isEmpty(preferences)) {
            for (int i = 0; i < 31; i++) {
                preferences.add((byte) 0b0);
            }
        }

        for (int i = 0; i < queries.size(); i++) {
            // if current mask has bit set, recheck
            if ((currentBitmask & (1 << (i))) > 0) {
                if (queries.get(i) == null || queries.get(i).equals(LayerDescription.DEFAULT_QUERY_STRING)) {
                    // Used correctly as loop does not iterate the preferences list
                    preferences.remove(i);
                    preferences.add(i, (currentBitmask & (1 << (i))) > 0 ? (byte) 0b10 : (byte) 0b0);
                } else {
                    // Used correctly as loop does not iterate the preferences list
                    preferences.remove(i);
                    preferences.add(i, (currentBitmask & (1 << (i))) > 0 ? (byte) 0b11 : (byte) 0b1);
                }
            }
        }

        // VERTEX
        final int vertexLayerMaskAttributeId = LayersConcept.VertexAttribute.LAYER_MASK.get(graph);
        final int vertexLayerVisibilityAttributeId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(graph);

        if (CollectionUtils.isNotEmpty(preferences)) {
            for (int j = 0; j < queries.size(); j++) {
                // calculate bitmask for dynamic layers that are displayed
                if (j < preferences.size() && (preferences.get(j) & 0b11) == 3 && queries.get(j) != null) {
                    /**
                     * try { new SelectExpressionPlugin(GraphElementType.VERTEX,
                     * queries.get(j), j).edit(graph, interaction, parameters);
                     * } catch (InterruptedException ex) {
                     * Exceptions.printStackTrace(ex); } catch (PluginException
                     * ex) { Exceptions.printStackTrace(ex); }
                    *
                     */
                }
            }
            for (int i = 0; i < graph.getVertexCount(); i++) {
                final int bitmask;
                final int vertexId = graph.getVertex(i);
                if (vertexLayerMaskAttributeId != Graph.NOT_FOUND) {
                    bitmask = graph.getIntValue(vertexLayerMaskAttributeId, vertexId);
                    graph.setFloatValue(vertexLayerVisibilityAttributeId, vertexId, (currentBitmask & bitmask) > 0 ? 1.0f : 0.0f);
                }
            }
        }

        // TRANSACTION
        final int transactionLayerMaskAttributeId = LayersConcept.TransactionAttribute.LAYER_MASK.get(graph);
        final int transactionLayerVisibilityAttributeId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(graph);
        if (CollectionUtils.isNotEmpty(preferences)) {
            for (int j = 0; j < queries.size(); j++) {
                // calculate bitmask for dynamic layers that are displayed
                if (j < preferences.size() && (preferences.get(j) & 0b11) == 3 && queries.get(j) != null) {
                    /**
                     * try { new
                     * SelectExpressionPlugin(GraphElementType.TRANSACTION,
                     * queries.get(j), j).edit(graph, interaction, parameters);
                     * } catch (InterruptedException ex) {
                     * Exceptions.printStackTrace(ex); } catch (PluginException
                     * ex) { Exceptions.printStackTrace(ex); } //
                     * SelectExpressionPlugin.run(activeGraph,
                     * GraphElementType.TRANSACTION, queries.get(j), j);
                     */
                }
            }
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                final int transactionId = graph.getTransaction(i);
                final int bitmask;
                if (transactionLayerMaskAttributeId != Graph.NOT_FOUND) {
                    bitmask = graph.getIntValue(transactionLayerMaskAttributeId, transactionId);
                    graph.setFloatValue(transactionLayerVisibilityAttributeId, transactionId, (currentBitmask & bitmask) > 0 ? 1.0f : 0.0f);
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
        return "Layers View: Validate Layer Bitmasks";
    }
}
