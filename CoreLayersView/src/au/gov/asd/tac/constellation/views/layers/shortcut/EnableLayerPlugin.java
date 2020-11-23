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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A plugin that enables a layer in the layers view
 *
 * @author formalhaut69
 */
public class EnableLayerPlugin extends SimpleEditPlugin {

    private static final Logger LOGGER = Logger.getLogger(EnableLayerPlugin.class.getName());
    final int layerIndex;

    public EnableLayerPlugin(final int layerIndex) {
        this.layerIndex = layerIndex;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        LOGGER.log(Level.WARNING, "EnableLayerPlugin");
        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
        if (layersViewStateAttributeId == Graph.NOT_FOUND) {
            return;
        }

        LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
        if (currentState == null) {
            currentState = new LayersViewState();
//            currentState.getVxQueriesCollection().setDefaultQueries();
//            currentState.getTxQueriesCollection().setDefaultQueries();
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
                vxQuery.setVisibility(!vxQuery.getVisibility());
            }
            if (txQuery != null) {
                txQuery.setVisibility(!txQuery.getVisibility());
            }

            currentState.getVxQueriesCollection().add(vxQuery);
            currentState.getTxQueriesCollection().add(txQuery);

//            LayersViewController.getDefault().getVxQueryCollection().setQueries(currentState.getVxQueriesCollection().getQueries());
//            LayersViewController.getDefault().getTxQueryCollection().setQueries(currentState.getTxQueriesCollection().getQueries());
            //graph.setObjectValue(layersViewStateAttributeId, 0, new LayersViewState(currentState));
            //final int bitmaskAttributeId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
            //final long newBitMask = graph.getLongValue(bitmaskAttributeId, 0) ^ (1 << layerIndex + 1);
            //graph.setLongValue(bitmaskAttributeId, 0, newBitMask);
        }
        LOGGER.log(Level.WARNING, "Query 1 Visible: " + currentState.getVxQueriesCollection().getQuery(1).getVisibility());
        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(currentState.getVxQueriesCollection(), currentState.getTxQueriesCollection());
        //LOGGER.log(Level.WARNING, "new bitmask: " + newBitmask);
        final int bitmaskAttributeId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        graph.setLongValue(bitmaskAttributeId, 0, newBitmask);
        //PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
        //        .executeLater(GraphManager.getDefault().getActiveGraph());

        final LayersViewState newState = new LayersViewState(currentState);
        graph.setObjectValue(layersViewStateAttributeId, 0, newState);

        //LayersViewController.getDefault().updateQueries(GraphManager.getDefault().getAllGraphs().get(graph.getId()));
    }

    @Override
    protected boolean isSignificant() {
        return false;
    }

    @Override
    public String getName() {
        return "Layers View: Enable Layer Plugin";
    }
}
