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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.operations.SetFloatValuesOperation;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.VertexListInclusionGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * A plugin template for a query plugin that uses RecordStores for its
 * read-query-write life cycle.
 * <p>
 * This plugin template follows the same read-query-write life cycle as
 * SimpleQueryPlugin. It handles the logic of both the read and write cycles,
 * where it builds a 'query' RecordStore from the graph and adds a 'result'
 * RecordStore to the graph, respectively. This means that developers need to
 * only implement the query method which contains the logic of generating the
 * 'result' RecordStore from the 'query' RecordStore.
 * <p>
 * This template will:
 * <ol>
 * <li>Set the graph to busy.</li>
 * <li>Start the progress bar.</li>
 * <li>Create the 'query' record store from the graph.</li>
 * <li>Call the query method where developers should implement their plugin
 * logic.</li>
 * <li>Write the 'result' RecordStore to the graph</li>
 * </ol>
 *
 * @see RecordStore
 * @author sirius
 */
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
public abstract class RecordStoreQueryPlugin extends SimpleQueryPlugin {

    protected final String pluginName = getName();

    protected RecordStore queryRecordStore;
    private RecordStore result = null;
    private final List<RecordStoreValidator> validators;

    private static final String THREAD_POOL_NAME = "RecordStore Query Plugin";
    private static final ExecutorService PLUGIN_EXECUTOR = ConstellationGlobalThreadPool.getThreadPool().getCachedThreadPool();

    /**
     * Base constructor for all implementations of RecordStoreQueryPlugin
     */
    protected RecordStoreQueryPlugin() {
        this(null);
    }

    protected RecordStoreQueryPlugin(String pluginName) {
        super(pluginName);
        validators = new ArrayList<>();
    }

    /**
     * Returns the RecordStore
     *
     * This method is used to retrieve the 'result' RecordStore of a completed
     * RecordStoreQueryPlugin. The typical use case for this is examining the
     * raw results of a given query before they have been added to the graph
     * (e.g., for diagnostic purposes).
     *
     * @return the RecordStore that holds the resulting records that will be
     * added to the graph.
     */
    protected RecordStore getResult() {
        return result;
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        switch (getRecordStoreType()) {
            case GraphRecordStoreUtilities.SOURCE -> queryRecordStore = GraphRecordStoreUtilities.getSelectedVertices(graph);
            case GraphRecordStoreUtilities.TRANSACTION -> queryRecordStore = GraphRecordStoreUtilities.getSelectedTransactions(graph);
            case GraphRecordStoreUtilities.ALL -> queryRecordStore = GraphRecordStoreUtilities.getAllSelected(graph);
        }
    }

    @Override
    protected void query(final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        // Create a Callable to execute the query on a separate thread.
        final Callable<RecordStore> callable = () -> {
            Thread.currentThread().setName(THREAD_POOL_NAME);

            for (final RecordStoreValidator validator : getValidators()) {
                validator.validatePreQuery(this, queryRecordStore, interaction, parameters);
            }

            queryRecordStore.reset();
            final RecordStore rs = query(queryRecordStore, interaction, parameters);

            for (final RecordStoreValidator validator : getValidators()) {
                validator.validatePostQuery(this, rs, interaction, parameters);
            }

            return rs;
        };

        Future<RecordStore> future = null;

        try {
            // Execute the query on a separate thread
            // This is to guard against the possibility that the query method does not honour the
            // interrupt flag and therefore can never be cancelled. Running the query method on
            // a different thread means that an interrupt will cause the query method to interrupt
            // as far as the framework is concerned. However, the rogue thread will continue to run
            // its normal course.
            future = PLUGIN_EXECUTOR.submit(callable);
            result = future.get();

            // If the plugin thread is interrupted then attempt to interrupt the query thread. Often the query thread
            // is unable to cancel so in this case the plugin thread will cancel so that it appears to the user that
            // the plugin has cancelled while the query thread is left to finish in its own time.
        } catch (InterruptedException e) {
            if (future != null) {
                future.cancel(true);
            }
            throw e;

            // If the query thread throws an exception then cancel the plugin.
        } catch (ExecutionException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else if (cause instanceof PluginException pluginException) {
                throw pluginException;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        for (final RecordStoreValidator validator : getValidators()) {
            validator.validatePreEdit(this, result, wg, interaction, parameters);
        }

        if (result != null) {
            // TODO: try to see if its worth setting this to init with schema to true - it did cause issues with it sometimes generating vertex # nodes
            final List<Integer> newVertices = GraphRecordStoreUtilities.addRecordStoreToGraph(wg, result, false, true, null);

            wg.validateKey(GraphElementType.VERTEX, true);
            wg.validateKey(GraphElementType.TRANSACTION, true);

            // Only arrange if there are new vertices, otherwise everything will be arranged.
            if (!newVertices.isEmpty()) {
                final PluginExecutor arrangement = completionArrangement();
                if (arrangement != null) {
                    final float[] xOriginal = new float[wg.getVertexCount()];
                    final float[] yOriginal = new float[wg.getVertexCount()];
                    final float[] zOriginal = new float[wg.getVertexCount()];

                    // save original positions
                    if (wg.isRecordingEdit()) {
                        saveOriginalPositionCoordinates(wg, xOriginal, yOriginal, zOriginal);
                    }

                    // run the arrangement
                    final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(wg, Connections.NONE, newVertices);
                    arrangement.executeNow(vlGraph.getInclusionGraph());
                    vlGraph.retrieveCoords();

                    // restore the original positions
                    if (wg.isRecordingEdit()) {
                        restoreOriginalPositionCoordinates(wg, xOriginal, yOriginal, zOriginal);
                    }
                }
            }

            // Reset the view
            PluginExecutor.startWith(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(wg);
        }
    }

    /**
     * Returns the type of 'query' RecordStore that is generated from the graph
     * in this plugin's read stage. The options are RecordStoreUtilities.SOURCE,
     * which creates a RecordStore representing the graph's nodes only,
     * RecordStoreUtiltities.TRANSACTION, which creates a RecordStore
     * representing the graph's transactions only, and RecordStoreUtilities.ALL,
     * which creates a RecordStore representing the whole graph.
     *
     * Implementations should be sure to override this if their query needs to
     * work on something other than the graph's nodes only (the default).
     *
     * @return A String representing the type of 'query' RecordStore to be
     * generated from the graph.
     */
    public String getRecordStoreType() {
        return GraphRecordStoreUtilities.SOURCE;
    }

    /**
     * The arrangement to be done after the plugin completes.
     * <p>
     * Some plugins result in new nodes being created in a graph. In order to
     * make these nodes visible to the user, the new nodes should undergo a
     * default arrangement.
     * <p>
     * Be cautious when specifying no arrangement. Due to various graphics
     * card/driver quirks, we can't leave the nodes at (0,0,0). It's easy to
     * crash the display this way. Also, we don't want to crush the new nodes
     * too close together: the camera will zoom in and make them bigger and
     * slower to draw. Therefore it is highly recommended that plugins do not
     * specify no arrangement unless they know what they're doing.
     * <p>
     * Note: PluginExecutors should be arrangements. Do the sensible thing when
     * overriding.
     *
     * @return A PluginExecutor that does an arrangement, or null if no
     * arrangement is to be done.
     */
    public PluginExecutor completionArrangement() {
        return PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                .followedBy(ArrangementPluginRegistry.PENDANTS)
                .followedBy(ArrangementPluginRegistry.UNCOLLIDE);
    }

    /**
     * Developers should override this method to implement the query stage logic
     * of their plugin.
     *
     * @see SimpleQueryPluginformore information on the query stage.
     *
     * @param query The 'query' RecordStore generated from the graph in the read
     * stage. This is the input to the plugin
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     *
     * @return The 'result' RecordStore that will be added to the graph in the
     * write stage. This is the output of the plugin.
     *
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected abstract RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;

    /**
     * Add a validator to this plugin. These can be used to validate the data in
     * the RecordStores used at various stages in the read-query-write
     * life-cycle.
     *
     * @see RecordStoreValidator for more information.
     *
     * @param validator A RecordStoreValidator.
     */
    protected void addValidator(final RecordStoreValidator validator) {
        validators.add(validator);
    }

    /**
     * Remove a validator by class.
     * <p>
     * The removal explicitly checks for class equality, rather than using
     * instanceof.
     *
     * @param c A RecordStoreValidator class.
     */
    protected void removeValidator(final Class<? extends RecordStoreValidator> c) {
        // Create a new List that contains everything but instances of the given class.
        final List<RecordStoreValidator> rv = validators.stream().filter(v -> !v.getClass().equals(c)).collect(Collectors.toList());
        validators.clear();
        validators.addAll(rv);
    }

    /**
     * Get a list of all validators attached to this plugin.
     *
     * @return A List of RecordStoreValidators currently attached to this
     * plugin.
     */
    protected List<RecordStoreValidator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * Save the original position coordinates so that they can be restored.
     *
     * @param wg The graph
     * @param xOriginal Float array to store the x positions, indexed by
     * position
     * @param yOriginal Float array to store the y positions, indexed by
     * position
     * @param zOriginal Float array to store the z positions, indexed by
     * position
     */
    private void saveOriginalPositionCoordinates(final GraphWriteMethods wg, final float[] xOriginal, final float[] yOriginal, final float[] zOriginal) {
        final int xAttr = VisualConcept.VertexAttribute.X.ensure(wg);
        final int yAttr = VisualConcept.VertexAttribute.Y.ensure(wg);
        final int zAttr = VisualConcept.VertexAttribute.Z.ensure(wg);

        final int vertexCount = wg.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vxId = wg.getVertex(position);
            xOriginal[position] = wg.getFloatValue(xAttr, vxId);
            yOriginal[position] = wg.getFloatValue(yAttr, vxId);
            zOriginal[position] = wg.getFloatValue(zAttr, vxId);
        }
    }

    /**
     * Restore the original position coordinates.
     * <p>
     * Note that if the original position is 0,0,0 then its not going to be
     * restored because we don't want the graphics card to crash.
     *
     * @param wg The graph
     * @param xOriginal Float array to store the x positions, indexed by
     * position
     * @param yOriginal Float array to store the y positions, indexed by
     * position
     * @param zOriginal Float array to store the z positions, indexed by
     * position
     */
    private void restoreOriginalPositionCoordinates(final GraphWriteMethods wg, final float[] xOriginal, final float[] yOriginal, final float[] zOriginal) {
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        final SetFloatValuesOperation setXOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, xAttr);
        final SetFloatValuesOperation setYOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, yAttr);
        final SetFloatValuesOperation setZOperation = new SetFloatValuesOperation(wg, GraphElementType.VERTEX, zAttr);

        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);
            if (xOriginal[position] != 0) {
                setXOperation.setValue(vxId, xOriginal[position]);
            }
            if (yOriginal[position] != 0) {
                setYOperation.setValue(vxId, yOriginal[position]);
            }
            if (zOriginal[position] != 0) {
                setZOperation.setValue(vxId, zOriginal[position]);
            }
        }

        // restore the x,y,z float values efficiently
        wg.executeGraphOperation(setXOperation);
        wg.executeGraphOperation(setYOperation);
        wg.executeGraphOperation(setZOperation);
    }
}
