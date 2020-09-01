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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.query.QueryEvaluator;
import au.gov.asd.tac.constellation.views.layers.layer.LayerEvaluator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public final class TransactionEvaluator extends SimpleEditPlugin {

    public TransactionEvaluator() {
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        final int graphCurrentBitMaskId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        final int currentBitmask = graph.getIntValue(graphCurrentBitMaskId, 0);

        final int transactionLayerMaskAttributeId = LayersConcept.TransactionAttribute.LAYER_MASK.get(graph);
        final int transactionLayerVisibilityAttributeId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(graph);

        final int layerQueryAttributeId = LayersConcept.GraphAttribute.LAYER_QUERIES.get(graph);
        final int layerPreferencesAttributeId = LayersConcept.GraphAttribute.LAYER_PREFERENCES.get(graph);
        int bitmask = 0;

        List<String> queries = graph.getObjectValue(layerQueryAttributeId, 0);
        List<Byte> preferences = graph.getObjectValue(layerPreferencesAttributeId, 0);

        if (CollectionUtils.isEmpty(queries)) {
            queries = new ArrayList<String>();
        }

        if (CollectionUtils.isEmpty(preferences)) {
            preferences = new ArrayList<>();
            for (int i = 0; i < 31; i++) {
                preferences.add((byte) 0b0);
            }
        }

        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int transactionId = graph.getTransaction(i);
            if (transactionLayerMaskAttributeId != Graph.NOT_FOUND) {
                bitmask = graph.getIntValue(transactionLayerMaskAttributeId, transactionId);
            }
            if (CollectionUtils.isNotEmpty(preferences)) {
                for (int j = 0; j < queries.size(); j++) {
                    // calculate bitmask for dynamic layers that are displayed
                    if (j < preferences.size() && (preferences.get(j) & 0b11) == 3 && queries.get(j) != null) {
                        if (LayerEvaluator.evaluateLayerQuery(graph, GraphElementType.TRANSACTION, transactionId, QueryEvaluator.tokeniser(queries.get(j)))) {
                            bitmask = bitmask | (1 << j);
                        } else {
                            bitmask = bitmask & ~(1 << j);
                        }
                    }
                }
                // end loop for element i, and all queries. bitmask to be set
                graph.setIntValue(transactionLayerMaskAttributeId, transactionId, bitmask);
                graph.setFloatValue(transactionLayerVisibilityAttributeId, transactionId, (currentBitmask & bitmask) > 0 ? 1.0f : 0.0f);
            }
        }
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Layers View: Update Transaction Bitmask";
    }
}
