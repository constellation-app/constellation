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
package au.gov.asd.tac.constellation.views.layers.shortcut;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;

/**
 * A plugin that deselects all layers in the layers view
 *
 * @author formalhaut69
 */
public class DeselectAllLayersPlugin extends SimpleEditPlugin {

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

        currentState.getVxQueriesCollection().setVisibilityOnAll(false);
        currentState.getTxQueriesCollection().setVisibilityOnAll(false);

        final LayersViewState newState = new LayersViewState(currentState);
        graph.setObjectValue(layersViewStateAttributeId, 0, newState);

        int newBitmask = 0b0;
        for (int position = 0; position <= Math.max(currentState.getVxQueriesCollection().getHighestQueryIndex(), currentState.getTxQueriesCollection().getHighestQueryIndex()); position++) {
            final BitMaskQuery vxQuery = currentState.getVxQueriesCollection().getQuery(position);
            final BitMaskQuery txQuery = currentState.getTxQueriesCollection().getQuery(position);

            if (vxQuery != null) {// can use vx
                newBitmask |= vxQuery.getVisibility() ? (1 << vxQuery.getIndex()) : 0;
            } else if (txQuery != null) {// have to use tx
                newBitmask |= txQuery.getVisibility() ? (1 << txQuery.getIndex()) : 0;
            } else {
                // cannot use any.
            }
        }
        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask == 0) ? 0b1 : (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());
        LayersViewController.getDefault().updateQueries(GraphManager.getDefault().getAllGraphs().get(graph.getId()));
    }

    @Override
    protected boolean isSignificant() {
        return false;
    }

    @Override
    public String getName() {
        return "Layers View: Deselect All Layers Plugin";
    }
}
