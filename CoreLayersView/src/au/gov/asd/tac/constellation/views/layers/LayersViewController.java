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
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Controls interaction of UI to layers and filtering of nodes and transactions.
 *
 * @author aldebaran30701
 */
public class LayersViewController {

    private static final Logger LOGGER = Logger.getLogger(LayersViewController.class.getName());

    // Layers view controller instance
    private static LayersViewController instance = null;
    private LayersViewTopComponent parent;

    private final List<AttributeValueMonitor> valueMonitors;
    private final List<SchemaAttribute> changeListeners;

    private final BitMaskQueryCollection vxBitMaskCollection = new BitMaskQueryCollection(GraphElementType.VERTEX);
    private final BitMaskQueryCollection txBitMaskCollection = new BitMaskQueryCollection(GraphElementType.TRANSACTION);

    /**
     * Private constructor for singleton
     */
    private LayersViewController() {
        this.valueMonitors = new ArrayList<>();
        this.changeListeners = new ArrayList<>();
        vxBitMaskCollection.setQueries(BitMaskQueryCollection.DEFAULT_VX_QUERIES);
        txBitMaskCollection.setQueries(BitMaskQueryCollection.DEFAULT_TX_QUERIES);
    }

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized LayersViewController getDefault() {
        if (instance == null) {
            instance = new LayersViewController();
        }
        return instance;
    }

    /**
     *
     * @param parent the TopComponent which this controller controls.
     * @return the instance to allow chaining
     */
    public LayersViewController init(final LayersViewTopComponent parent) {
        this.parent = parent;
        return instance;
    }

    public void setListenedAttributes() {
        parent.removeValueHandlers(valueMonitors);
        valueMonitors.addAll(parent.setChangeListeners(List.copyOf(changeListeners)));
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
                    if (state != null) {
                        changeListeners.clear();
                        changeListeners.addAll(state.getLayerAttributes());
                        LayersViewController.getDefault().setListenedAttributes();
                    }
                }
            }).executeLater(activeGraph);
        }
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
                    LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
                    LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
                    LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
                    LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
                    LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
                    LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
                }
            }).executeLater(activeGraph);
        }
    }

    /**
     * Get all layer queries from the Layer View and store them on the qraph.
     * Update the bitmask used to determine visibility of elements on the graph.
     */
    public void execute() {
        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxBitMaskCollection, txBitMaskCollection);

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }

    public void executeFuture() {
        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxBitMaskCollection, txBitMaskCollection);

        Future<?> f = PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());

        try {
            f.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Reads the graph's layers_view_state attribute and populates the Layers
     * View pane.
     */
    public void readState() {
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            LOGGER.log(Level.WARNING, "Graph was found to be non-existent! - Read State");
            return;
        }
        PluginExecution.withPlugin(new LayersViewStateReader(pane))
                .executeLater(graph);
    }

    public boolean getParentVisibility() {
        return parent != null ? parent.getVisibility() : null;
    }

    public void readStateFuture() {
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            LOGGER.log(Level.WARNING, "Graph was found to be non-existent! - Read State");
            return;
        }
        Future<?> f = PluginExecution.withPlugin(new LayersViewStateReader(pane))
                .executeLater(graph);
        try {
            f.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Executes a plugin to write the current layers to the graph's
     * layers_view_state Attribute.
     *
     * @return a future of the plugin
     */
    public Future<?> writeState() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            LOGGER.log(Level.WARNING, "Graph was found to be non-existent! - Write State");
            return null;
        }

        return PluginExecution.withPlugin(new LayersViewStateWriter(vxBitMaskCollection.getQueries(), txBitMaskCollection.getQueries()))
                .executeLater(graph);

    }

    public void updateQueries(final Graph currentGraph) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            LOGGER.log(Level.WARNING, "Graph was found to be non-existent! - Write State");
            return;
        }
        // hwere is this triggerign from
        LOGGER.log(Level.WARNING, "about to update queries: layer 1: " + vxBitMaskCollection.getQuery(1).getVisibility());
        final UpdateQueryPlugin updatePlugin = new UpdateQueryPlugin(vxBitMaskCollection, txBitMaskCollection);
        PluginExecution.withPlugin(updatePlugin).executeLater(graph);
    }

    public void updateQueriesFuture(final Graph currentGraph) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            LOGGER.log(Level.WARNING, "Graph was found to be non-existent! - Write State");
            return;
        }
        LOGGER.log(Level.WARNING, "before update query future layer 1: " + vxBitMaskCollection.getQuery(1).getVisibility());
        final UpdateQueryPlugin updatePlugin = new UpdateQueryPlugin(vxBitMaskCollection, txBitMaskCollection);
        Future<?> f = PluginExecution.withPlugin(updatePlugin).executeLater(graph);
        try {
            f.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOGGER.log(Level.WARNING, "aftewr update query future");
    }

    public BitMaskQueryCollection getVxQueryCollection() {
        return vxBitMaskCollection;
    }

    public BitMaskQueryCollection getTxQueryCollection() {
        return txBitMaskCollection;
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
            LOGGER.log(Level.WARNING, "ReadSTatePlugin");
            if (graph == null) {
                //LOGGER.log(Level.WARNING, "Error Reading State!");
                return;
            }

            //LOGGER.log(Level.WARNING, "Readoing state now!");
            final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
            if (layersViewStateAttributeId == Graph.NOT_FOUND) {
                //LOGGER.log(Level.WARNING, "Error Reading State!layersViewStateAttributeId null");
                return;
            }

            final LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
            if (currentState == null || pane == null) {
                // LOGGER.log(Level.WARNING, "Error Reading State!currentStatenull");
                return;
            }
            //LOGGER.log(Level.WARNING, "Setting paneto VX: " + currentState.getVxQueriesCollection().getHighestQueryIndex() + " TX: " + currentState.getTxQueriesCollection().getHighestQueryIndex());
            pane.setLayers(currentState.getVxQueriesCollection().getQueries(), currentState.getTxQueriesCollection().getQueries());
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

        private final BitMaskQuery[] vxLayers;
        private final BitMaskQuery[] txLayers;

        public LayersViewStateWriter(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
            this.vxLayers = vxLayers;
            this.txLayers = txLayers;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            LOGGER.log(Level.WARNING, "WriteSTatePlugin");
            if (graph == null) {
                LOGGER.log(Level.WARNING, "Error writing state!");
                return;

            }

            //LOGGER.log(Level.WARNING, "Writing state now!");
            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            //LOGGER.log(Level.WARNING, currentState == null ? " null" : currentState.toString());
            if (currentState == null) {
                currentState = new LayersViewState();
                currentState.setVxLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES);
                currentState.setTxLayers(BitMaskQueryCollection.DEFAULT_TX_QUERIES);
            } else {
                currentState = new LayersViewState(currentState);
            }

            //final LayersViewState newState = currentState == null ? new LayersViewState() : new LayersViewState(currentState);
            currentState.setVxLayers(vxLayers);
            currentState.setTxLayers(txLayers);
            LOGGER.log(Level.WARNING, "Setting layers to state: Layer 1 = " + (vxLayers[1] != null ? vxLayers[1].getVisibility() : "null"));
            if (currentState.getVxQueriesCollection().getHighestQueryIndex() == 0 && currentState.getTxQueriesCollection().getHighestQueryIndex() == 0) {

                currentState.setVxLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES);
                currentState.setTxLayers(BitMaskQueryCollection.DEFAULT_TX_QUERIES);
            }

            graph.setObjectValue(stateAttributeId, 0, currentState);
            LOGGER.log(Level.WARNING, "ENDWriteSTatePlugin");
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

    /**
     * Plugin to update all bit masks relating to the queries held in both
     * vertex and transaction query collections.
     */
    public static class UpdateQueryPlugin extends SimpleEditPlugin {

        private final BitMaskQueryCollection vxBitMasks;
        private final BitMaskQueryCollection txBitMasks;

        public UpdateQueryPlugin(final BitMaskQueryCollection vxbitMasks, final BitMaskQueryCollection txbitMasks) {
            this.vxBitMasks = vxbitMasks;
            this.txBitMasks = txbitMasks;
        }

        @Override
        public String getName() {
            return "Update Query";
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            LOGGER.log(Level.WARNING, "UpdateQueryPlugin");
            final int graphCurrentBitMaskAttrId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
            final long currentBitmask = graph.getLongValue(graphCurrentBitMaskAttrId, 0);

            final int vxbitmaskAttrId = LayersConcept.VertexAttribute.LAYER_MASK.get(graph);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(graph);

            final int txbitmaskAttrId = LayersConcept.TransactionAttribute.LAYER_MASK.get(graph);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(graph);

            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            final LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            if (currentState != null) {
                //LayersViewController.getDefault().getVxQueryCollection().setActiveQueries(currentBitmask);
                //LayersViewController.getDefault().getVxQueryCollection().updateBitMasks(graph, vxbitmaskAttrId, vxbitmaskVisibilityAttrId);
                LOGGER.log(Level.WARNING, "Query 1 Visible: " + currentState.getVxQueriesCollection().getQuery(1).getVisibility() + "current butmask: " + currentBitmask);
                currentState.getVxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getVxQueriesCollection().updateBitMasks(graph, vxbitmaskAttrId, vxbitmaskVisibilityAttrId);
                currentState.getTxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getTxQueriesCollection().updateBitMasks(graph, txbitmaskAttrId, txbitmaskVisibilityAttrId);
                currentState.extractLayerAttributes(graph);
                LayersViewController.getDefault().updateListenedAttributes();
            }
            LOGGER.log(Level.WARNING, "ENDUpdateQueryPlugin");
        }
    }
}
