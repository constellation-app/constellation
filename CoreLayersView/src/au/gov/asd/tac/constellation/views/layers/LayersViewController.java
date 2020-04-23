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
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

/**
 * Controls interaction of UI to layers and filtering of nodes and transactions.
 *
 * @author aldebaran30701
 */
public class LayersViewController {

    private final LayersViewTopComponent parent;
    private LayersViewPane pane = null;

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
        Label layerIdText = null;
        CheckBox visibilityCheckBox = null;
        for (final Node node : pane.getLayers().getChildren()) {

            if (GridPane.getRowIndex(node) > 0) { // skip layer 1
                layerIdText = GridPane.getColumnIndex(node) == 0 ? (Label) node : layerIdText;
                visibilityCheckBox = GridPane.getColumnIndex(node) == 1 ? (CheckBox) node : visibilityCheckBox;

                if (GridPane.getColumnIndex(node) == 2) {
                    // only add layer id to list when it is checked
                    newBitmask |= visibilityCheckBox.isSelected() ? (1 << Integer.parseInt(layerIdText.getText()) - 1) : 0;
                }
            }
        }

        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;

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
        for (final Node node : pane.getLayers().getChildren()) {
            if (GridPane.getRowIndex(node) > 0 && GridPane.getColumnIndex(node) == 2) {
                final TextArea queryTextArea = (TextArea) node;
                layerQueries.add(queryTextArea.getText().isBlank() ? null : queryTextArea.getText());
            }
        }
        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(layerQueries)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Executes a plugin to grab current Layers View selections and save them to
     * the graph's Layers View State Attribute.
     */
    public void updateState() {
        pane = parent.getContent();
        PluginExecution.withPlugin(new LayersViewStateUpdater(pane, true)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Executes a plugin to grab current Layers View state and load them into
     * the Layers View UI.
     */
    public void loadOrCreateState() {
        pane = parent.getContent();
        PluginExecution.withPlugin(new LayersViewStateUpdater(pane, false)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Update the display by reading and writing to/from the state attribute.
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
            // no state or on a graph update
            if (graph.getObjectValue(stateAttributeId, 0) == null || isUpdateCall) {
                // Take a snapshot of the UI and store that state
                LayersViewState newState = captureState();
                graph.setObjectValue(stateAttributeId, 0, newState);
            } else {
                // When a state exists, write it to the Layers View
                updateLayersView(graph.getObjectValue(stateAttributeId, 0));
            }
        }

        private LayersViewState captureState() {
            int layerNumber = -1;
            boolean layerSelected = false;
            String layerQuery = "";
            String layerDescription;
            LayerDescription layer;
            final LayersViewState currentState = new LayersViewState();
            for (final Node node : pane.getLayers().getChildren()) {
                // when not on a heading
                if (GridPane.getRowIndex(node) > 0) {
                    if (null != GridPane.getColumnIndex(node)) {
                        switch (GridPane.getColumnIndex(node)) {
                            case 0:
                                layerNumber = Integer.parseInt(((Label) node).getText());
                                break;
                            case 1:
                                layerSelected = ((CheckBox) node).isSelected();
                                break;
                            case 2:
                                layerQuery = ((TextArea) node).getText();
                                break;
                            case 3:
                                layerDescription = ((TextArea) node).getText();
                                layer = new LayerDescription(layerNumber, layerSelected, layerQuery, layerDescription);
                                currentState.addLayer(layer);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            return currentState;
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Layers View: Update State";
        }

        /**
         * Update the UI for Layers View to reflect the queries loaded from
         * state.
         *
         * @param newState the LayersViewState to load into the UI
         */
        private void updateLayersView(final LayersViewState newState) {
            pane.setLayers(newState);
        }
    }
}
