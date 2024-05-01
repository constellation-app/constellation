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
package au.gov.asd.tac.constellation.views.layers.shortcut;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;

/**
 * A plugin that enables a layer in the layers view
 *
 * @author formalhaut69
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class EnableLayerPlugin extends SimpleEditPlugin {

    final int layerIndex;

    public EnableLayerPlugin(final int layerIndex) {
        this.layerIndex = layerIndex;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
        if (layersViewStateAttributeId == Graph.NOT_FOUND) {
            return;
        }

        LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
        if (currentState == null) {
            currentState = new LayersViewState();
        }
        if (currentState.getVxQueriesCollection().getHighestQueryIndex() == 0
                && currentState.getTxQueriesCollection().getHighestQueryIndex() == 0) {
            currentState.getVxQueriesCollection().setDefaultQueries();
            currentState.getTxQueriesCollection().setDefaultQueries();
        }
        if (currentState.getLayerCount() >= layerIndex) {

            final BitMaskQuery vxQuery = currentState.getVxQueriesCollection().getQuery(layerIndex);
            final BitMaskQuery txQuery = currentState.getTxQueriesCollection().getQuery(layerIndex);

            if (vxQuery != null) {
                vxQuery.setVisibility(!vxQuery.isVisible());
            }
            if (txQuery != null) {
                txQuery.setVisibility(!txQuery.isVisible());
            }

            currentState.getVxQueriesCollection().add(vxQuery);
            currentState.getTxQueriesCollection().add(txQuery);
        }

        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(currentState.getVxQueriesCollection(), currentState.getTxQueriesCollection());

        final int bitmaskAttributeId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        graph.setLongValue(bitmaskAttributeId, 0, newBitmask);

        final LayersViewState newState = new LayersViewState(currentState);
        graph.setObjectValue(layersViewStateAttributeId, 0, newState);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Layers View: Enable Layer Plugin";
    }
}
