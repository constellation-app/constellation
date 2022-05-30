/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.layers.components.LayersViewPane;
import au.gov.asd.tac.constellation.views.layers.context.LayerAction;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import au.gov.asd.tac.constellation.views.layers.utilities.ShuffleElementBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateElementBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.NotifyDescriptor;

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
        if (parent == null) {
            return;
        }
        parent.removeValueHandlers(valueMonitors);
        valueMonitors.addAll(parent.setChangeListeners(List.copyOf(changeListeners)));
    }

    /**
     * Update the List of listened attributes via the Layers View State.
     */
    public void updateListenedAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new CaptureListenedAttributesPlugin(changeListeners)).executeLater(activeGraph);
        }
    }

    /**
     * Add attributes required by the Layers View for it to function
     */
    public void addAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new AddAttributesPlugin()).executeLater(activeGraph);
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

        final Future<?> f = PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());

        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Layer Selection update was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Reads the graph's layers_view_state attribute and populates the Layers
     * View pane.
     */
    public void readState() {
        LOGGER.log(Level.SEVERE, "read state in method1");
        if (parent == null) {
            return;
        }
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }
        PluginExecution.withPlugin(new LayersStateReaderPlugin(pane))
                .executeLater(graph);
    }

    public boolean hasParentVisibility() {
        return parent != null && parent.getVisibility();
    }

    public void readStateFuture() {
        LOGGER.log(Level.SEVERE, "read state in method2");
        if (parent == null) {
            return;
        }
        final LayersViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }
        final Future<?> f = PluginExecution.withPlugin(new LayersStateReaderPlugin(pane))
                .executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Layers State Reader was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Executes a plugin to write the current layers to the graph's
     * layers_view_state Attribute.
     *
     * @return a future of the plugin
     */
    public Future<?> writeState() {
        LOGGER.log(Level.SEVERE, "write state in method");
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return null;
        }

        // controller out of sync with graph...
        return PluginExecution.withPlugin(new LayersStateWriterPlugin(vxBitMaskCollection.getQueries(),
                txBitMaskCollection.getQueries()))
                .executeLater(graph);

    }

    public void updateQueries(final Graph currentGraph) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return;
        }
        PluginExecution.withPlugin(new UpdateQueryPlugin()).executeLater(graph);
    }

    public void updateQueriesFuture(final Graph currentGraph) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return;
        }
        final Future<?> f = PluginExecution.withPlugin(new UpdateQueryPlugin()).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Layers Query update was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public BitMaskQueryCollection getVxQueryCollection() {
        return vxBitMaskCollection;
    }

    public BitMaskQueryCollection getTxQueryCollection() {
        return txBitMaskCollection;
    }

    /**
     * Removes the bitmask specified by currentIndex from all elements on the
     * graph
     *
     * @param currentIndex the layer index (bitmask) to use when removing from
     * elements.
     */
    public void removeBitmaskFromElements(final int currentIndex) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return;
        }
        final Future<?> f = PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(currentIndex,
                LayerAction.REMOVE, false)).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Element Bitmask update was interrupted", ex);
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    /**
     * Move the layer from current layer to one lower. Used for deletion of
     * layers to maintain order.
     *
     * @param currentIndex the layer which is blank and needs to be shifted
     * into.
     */
    public void shuffleElementBitmasks(final int currentIndex) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            return;
        }
        final Future<?> f = PluginExecution.withPlugin(new ShuffleElementBitmaskPlugin(currentIndex)).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Element bitmask shuffler was interrupted", ex);
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    protected LayersViewTopComponent getParent() {
        return parent;
    }

    /**
     * Create a layer by writing data to the UI and saving that state
     */
    public void createLayer() {
        final int layerCount = Math.max(getTxQueryCollection().getHighestQueryIndex(), getVxQueryCollection().getHighestQueryIndex());

        if (layerCount <= BitMaskQueryCollection.MAX_QUERY_AMT) {
            Query vxQuery = new Query(GraphElementType.VERTEX, "");
            getVxQueryCollection().add(vxQuery, layerCount + 1, null);
            Query txQuery = new Query(GraphElementType.TRANSACTION, "");
            getTxQueryCollection().add(txQuery, layerCount + 1, null);
            writeState();
        } else {
            NotifyDisplayer.display("You cannot have more than " + BitMaskQueryCollection.MAX_QUERY_AMT + " layers", NotifyDescriptor.WARNING_MESSAGE);
            LOGGER.log(Level.INFO, "Layer count maximum reached. Maximum is currently: {0}", BitMaskQueryCollection.MAX_QUERY_AMT);
        }
    }
    
    /**
     * Delete the layer at the specified index
     * @param index 
     */
    public void deleteLayer(final int index) {
        if (index != 0) {
            getVxQueryCollection().removeQueryAndSort(index);
            getTxQueryCollection().removeQueryAndSort(index);
            removeBitmaskFromElements(index);
            shuffleElementBitmasks(index);
            writeState();
            execute();
        } else {
            NotifyDisplayer.display("You cannot delete the default layer!", NotifyDescriptor.WARNING_MESSAGE);
        }
    }

    /**
     * Deselect all layers in each collection
     */
    public void deselectAll() {
        getVxQueryCollection().setVisibilityOnAll(false);
        getTxQueryCollection().setVisibilityOnAll(false);
        execute();
        writeState();
    }
    
    public void changeLayerVisibility(final int index, final boolean isVisible) {
        final BitMaskQuery vxQuery = getVxQueryCollection().getQuery(index);
        final BitMaskQuery txQuery = getTxQueryCollection().getQuery(index);
        
        if (vxQuery != null) {
            vxQuery.setVisibility(isVisible);
        }
        if (txQuery != null) {
            txQuery.setVisibility(isVisible);
        }
        
        executeFuture();
        writeState();
    }

    /**
     * Update description text for the layer
     *
     * @param newString
     * @param index
     */
    public void updateDescription(final String newString, final int index) {
        final BitMaskQuery vxQuery = getVxQueryCollection().getQuery(index);
        final BitMaskQuery txQuery = getTxQueryCollection().getQuery(index);
        
        if (vxQuery != null) {
            vxQuery.setDescription(newString);
        }
        if (txQuery != null) {
            txQuery.setDescription(newString);
        }
        writeState();
    }

    /**
     * Update a query
     *
     * @param newQueryString
     * @param index
     * @param queryType
     */
    public void updateQuery(final String newQueryString, final int index, final String queryType) {
        
        final BitMaskQuery vxQuery = getVxQueryCollection().getQuery(index);
        final BitMaskQuery txQuery = getTxQueryCollection().getQuery(index);
        
        if (vxQuery != null && "Vertex Query: ".equals(queryType)) {
            vxQuery.setQueryString(newQueryString);
        }
        if (txQuery != null && "Transaction Query: ".equals(queryType)) {
            txQuery.setQueryString(newQueryString);
        }
        execute();
        writeState();
    }

    /**
     * Read the current state from the graph.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
    protected static final class LayersStateReaderPlugin extends SimpleReadPlugin {

        private final LayersViewPane pane;

        protected LayersStateReaderPlugin(final LayersViewPane pane) {
            this.pane = pane;
        }

        @Override
        public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (graph == null) {
                return;
            }
            LOGGER.log(Level.SEVERE, "Reading state in plugin");

            final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
            if (layersViewStateAttributeId == Graph.NOT_FOUND) {
                return;
            }

            final LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
            if (currentState == null || pane == null) {
                return;
            }
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
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
    protected static final class LayersStateWriterPlugin extends SimpleEditPlugin {

        private final BitMaskQuery[] vxLayers;
        private final BitMaskQuery[] txLayers;

        protected LayersStateWriterPlugin(final BitMaskQuery[] vxLayers, final BitMaskQuery[] txLayers) {
            this.vxLayers = vxLayers;
            this.txLayers = txLayers;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (graph == null) {
                return;
            }
            LOGGER.log(Level.SEVERE, "Writing state in plugin");

            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            if (currentState == null) {
                currentState = new LayersViewState();
                currentState.setVxLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES);
                currentState.setTxLayers(BitMaskQueryCollection.DEFAULT_TX_QUERIES);
            } else {
                currentState = new LayersViewState(currentState);
            }

            LOGGER.log(Level.SEVERE, "is query visible already?: " + currentState.getVxQueriesCollection().getQuery(1).isVisible());
            currentState.setVxLayers(vxLayers);
            currentState.setTxLayers(txLayers);
            if (currentState.getVxQueriesCollection().getHighestQueryIndex() == 0 && currentState.getTxQueriesCollection().getHighestQueryIndex() == 0) {
                currentState.setVxLayers(BitMaskQueryCollection.DEFAULT_VX_QUERIES);
                currentState.setTxLayers(BitMaskQueryCollection.DEFAULT_TX_QUERIES);
            }

            graph.setObjectValue(stateAttributeId, 0, currentState);
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
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
    protected static class UpdateQueryPlugin extends SimpleEditPlugin {

        protected UpdateQueryPlugin() {

        }

        @Override
        public String getName() {
            return "Update Query";
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            final int graphCurrentBitMaskAttrId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
            final long currentBitmask = graph.getLongValue(graphCurrentBitMaskAttrId, 0);

            final int vxbitmaskAttrId = LayersConcept.VertexAttribute.LAYER_MASK.get(graph);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(graph);

            final int txbitmaskAttrId = LayersConcept.TransactionAttribute.LAYER_MASK.get(graph);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(graph);

            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            final LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            if (currentState != null) {
                currentState.getVxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getVxQueriesCollection().updateBitMasks(graph, vxbitmaskAttrId, vxbitmaskVisibilityAttrId);
                currentState.getTxQueriesCollection().setActiveQueries(currentBitmask);
                currentState.getTxQueriesCollection().updateBitMasks(graph, txbitmaskAttrId, txbitmaskVisibilityAttrId);
                currentState.extractLayerAttributes(graph);
                LayersViewController.getDefault().updateListenedAttributes();
            }
        }
    }

    /**
     * Plugin to capture the listened attributes on the graph. This list allows
     * the view to update the graph when one of those attributes changes value.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    protected static class CaptureListenedAttributesPlugin extends SimpleReadPlugin {

        final List<SchemaAttribute> changeListeners;

        protected CaptureListenedAttributesPlugin(final List<SchemaAttribute> changeListeners) {
            this.changeListeners = changeListeners;
        }

        @Override
        public String getName() {
            return "Layers View: Capture Listened Attributes";
        }

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
    }

    /**
     * Plugin to add the required Layers View attributes.
     */
    @PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
    protected static class AddAttributesPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Layers View: Add Required Attributes";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
            LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
            LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
            LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
            LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
            LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
        }
    }
}
