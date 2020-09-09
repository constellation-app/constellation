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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls interaction of UI to layers and filtering of nodes and transactions.
 *
 * @author aldebaran30701
 */
public class LayersViewController {

    private final LayersViewTopComponent parent;
    private final List<SchemaAttribute> listenedAttributes;
    private final BitMaskQueryCollection bitMaskCollection = new BitMaskQueryCollection(new ArrayList<>());

    public LayersViewController(final LayersViewTopComponent parent) {
        this.parent = parent;
        this.listenedAttributes = new ArrayList<>();
    }

    /**
     * Add attributes required by the Layers View for it to function
     */
    public void addAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new SimpleEditPlugin("Layers View: Add Required Attributes") {
                @Override
                public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                    LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
                    LayersConcept.GraphAttribute.LAYER_QUERIES.ensure(graph);
                    LayersConcept.GraphAttribute.LAYER_PREFERENCES.ensure(graph);
                    LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
                    LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
                    LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
                    LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
                }
            }).executeLater(activeGraph);
        }
    }

    /**
     * Update the List of listened attributes via the Layers View State.
     */
    public void updateListenedAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new SimpleReadPlugin("Layers View: Capture Listened Attributes") {
                @Override
                public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                    final int layerStateId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
                    if (layerStateId == Graph.NOT_FOUND) {
                        return;
                    }

                    final LayersViewState state = graph.getObjectValue(layerStateId, 0);
                    listenedAttributes.clear();
                    listenedAttributes.addAll(state.getLayerAttributes());
                }
            }).executeLater(activeGraph);
        }
    }

    /**
     * Get all layer queries from the Layer View and store them on the qraph.
     * Update the bitmask used to determine visibility of elements on the graph.
     */
    public void execute() {
        final LayersViewPane pane = parent.getContent();
        if (pane == null) {
            return;
        }
        final List<String> layerQueries = new ArrayList<>();
        int newBitmask = 0b0;
        for (final BitMaskQuery query : pane.getlayers()) {
            layerQueries.add(query.getQueryString());
            newBitmask |= query.getVisibility() ? (1 << query.getIndex() - 1) : 0;
        }
        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask == 0) ? 0b1 : (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Reads the graph's layers_view_state attribute and populates the Layers
     * View pane.
     */
    public void readState() {
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }
        PluginExecution.withPlugin(new LayersViewStateReader(pane))
                .executeLater(graph);

        updateListenedAttributes();
    }

    /**
     * Executes a plugin to write the current layers to the graph's
     * layers_view_state Attribute.
     */
    public void writeState() {
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }
        PluginExecution.withPlugin(new LayersViewStateWriter(pane.getlayers()))
                .executeLater(graph);
    }

    public List<SchemaAttribute> getListenedAttributes() {
        return listenedAttributes;
    }

    //
    public void updateQueries(final Graph currentGraph) {
        final LayersViewController.UpdateQueryPlugin plugin = new LayersViewController.UpdateQueryPlugin(bitMaskCollection);
        PluginExecution.withPlugin(plugin).executeLater(currentGraph);
//        final Future<?> f = PluginExecution.withPlugin(plugin).executeLater(currentGraph);
//        try {
//            f.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//            Thread.currentThread().interrupt();
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    public BitMaskQueryCollection getQueryCollection() {
        return bitMaskCollection;
    }

    /**
     * Read the current state from the graph.
     */
    private static final class LayersViewStateReader extends SimpleReadPlugin {

        private final LayersViewPane pane;

        public LayersViewStateReader(final LayersViewPane pane) {
            this.pane = pane;
        }

        @Override
        public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (graph == null) {
                return;
            }

            final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
            if (layersViewStateAttributeId == Graph.NOT_FOUND) {
                return;
            }

            final LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
            if (currentState == null || pane == null) {
                return;
            }
            pane.setLayers(currentState.getLayers());
        }

        @Override
        public String getName() {
            return "Layers View: Read State";
        }
    }

    /**
     * Write the current state to the graph.
     */
    private static final class LayersViewStateWriter extends SimpleEditPlugin {

        private final List<BitMaskQuery> layers;

        public LayersViewStateWriter(final List<BitMaskQuery> layers) {
            this.layers = layers;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (graph == null) {
                return;
            }

            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            final LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);

            final LayersViewState newState = currentState == null ? new LayersViewState() : new LayersViewState(currentState);
            newState.setLayers(layers);
            newState.extractLayerAttributes(graph);

            graph.setObjectValue(stateAttributeId, 0, newState);
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Layers View: Write State";
        }
    }

    public static class UpdateQueryPlugin extends SimpleEditPlugin {

        private final BitMaskQueryCollection bitMasks;

        public UpdateQueryPlugin(BitMaskQueryCollection bitMasks) {
            this.bitMasks = bitMasks;
        }

        @Override
        public String getName() {
            return "Update Query";
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            final int bitMaskAttributeId = graph.getAttribute(GraphElementType.VERTEX, "bitmask");
            final int bitmaskId = LayersConcept.VertexAttribute.LAYER_MASK.get(graph);
            final int bitmaskVisibilityId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(graph);
            final int visibleMaskAttributeId = graph.getAttribute(GraphElementType.VERTEX, "visibility");

            final int graphCurrentBitMaskId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
            final int currentBitmask = graph.getIntValue(graphCurrentBitMaskId, 0);
            bitMasks.updateBitMasks(graph, bitmaskId, bitmaskVisibilityId, GraphElementType.VERTEX, currentBitmask);
        }
    }
}
