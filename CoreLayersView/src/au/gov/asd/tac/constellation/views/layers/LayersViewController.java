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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateGraphBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateGraphQueriesPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Controls interaction of UI to layers and filtering of nodes and transactions.
 *
 * @author aldebaran30701
 */
public class LayersViewController {

    private final LayersViewTopComponent parent;
    private LayersViewPane pane = null;
    public boolean stateChanged = false;

    public LayersViewController(final LayersViewTopComponent parent) {
        this.parent = parent;
    }

    /**
     * Runs a plugin which updates the bitmask that should be used to show
     * elements.
     */
    public void execute() {
        // ensure pane is set to the content of the parent view.
        pane = parent.getContent();
        if (pane == null) {
            return;
        }
        int newBitmask = 0b0;
        for (LayerDescription layer : pane.getlayers()) {
            newBitmask |= layer.getCurrentLayerVisibility() ? (1 << layer.getLayerIndex() - 1) : 0;
        }
        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask == 0) ? 0b1 : (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(newBitmask)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Grab all queries entered into text areas and store them in the qraph's
     * queries.
     */
    public void submit() {
        // ensure pane is set to the content of the parent view.
        pane = parent.getContent();
        if (pane == null) {
            return;
        }
        final List<String> layerQueries = new ArrayList<>();
        for (LayerDescription layer : pane.getlayers()) { // TODO: This was the last change of tuesday
            layerQueries.add(StringUtils.isBlank(layer.getLayerQuery()) ? null : layer.getLayerQuery());
        }
        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(layerQueries)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Executes the plugin with parameters dependent on if the trigger was an update event.
     */
    public void updateState(final boolean wasUpdate) {
        pane = parent.getContent();
        stateChanged = true;
        PluginExecution.withPlugin(new LayersViewStateUpdater(pane, wasUpdate)).executeLater(GraphManager.getDefault().getActiveGraph());
        
    }


    /**
     * Executes a plugin to grab current Layers View selections and save them to
     * the graph's Layers View State Attribute only when it has changed.
     */
    public void writeState() {
        pane = parent.getContent();
        if(stateChanged){
            stateChanged = false;
            PluginExecution.withPlugin(new LayersViewStateWriter(pane)).executeLater(GraphManager.getDefault().getActiveGraph());
        }
    }

    /**
     * Updates the pane with current state selections, updates the state when changed.
     */
    private static final class LayersViewStateUpdater extends SimpleEditPlugin {

        private LayersViewPane pane = null;
        private boolean isUpdateCall = false;

        public LayersViewStateUpdater(final LayersViewPane pane, final boolean isUpdateCall) {
            this.pane = pane;
            this.isUpdateCall = isUpdateCall;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (pane == null) {
                return;
            }
            
            int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            currentState = (currentState == null) ? new LayersViewState() : new LayersViewState(currentState);
            if (isUpdateCall) {
                // Take a snapshot of the UI and store that state
                currentState.setLayers(pane.getlayers());
                pane.getController().writeState();
            } 
            pane.setLayers(currentState.getAllLayers());
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Layers View: Update State";
        }
    }
    
    /**
     * Write the current pane layer contents to the state attribute.
     */
    private static final class LayersViewStateWriter extends SimpleEditPlugin {

        private LayersViewPane pane = null;

        public LayersViewStateWriter(final LayersViewPane pane) {
            this.pane = pane;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (pane == null) {
                return;
            }
            int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            LayersViewState newState = graph.getObjectValue(stateAttributeId, 0);
            newState = newState == null ? new LayersViewState() : new LayersViewState(newState);
            newState.setLayers(pane.getlayers());
            graph.setObjectValue(stateAttributeId, 0, newState);
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Layers View: Update State";
        }
    }
}
