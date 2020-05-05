/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.templates;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.AbstractPlugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;

/**
 * A plugin template for a plugin that naturally works within a read-query-write
 * life cycle.
 * <p>
 * During the read stage, the plugin has a read lock on the graph and can
 * extract all required information from the graph. During the query stage, the
 * plugin has no lock on the graph and typically uses information derived during
 * the read stage to perform long-running calculations or long-running queries
 * on external databases. During the write stage, the plugin has a write lock on
 * the graph and is able to update the graph based on the results of the query
 * stage.
 * <p>
 * Using the read-query-write life cycle has the advantage that the plugin
 * releases all locks on the graph during the long query stage allowing the
 * graph to remain responsive the the user and other plugins during this time.
 * This also means that the plugin must elegantly handle the case where the
 * graph has been altered by another plugin during the query stage.
 * <p>
 * This template will:
 * <ol>
 * <li>Set the graph to busy.</li>
 * <li>Start the progress bar.</li>
 * <li>Get a read lock on the graph, call the read method, then release the
 * lock.</li>
 * <li>Call the query method.</li>
 * <li>Get a write lock on the graph, call the write method, then release the
 * lock.</li>
 * </ol>
 *
 * @author sirius
 */
@Messages({
    "# {0} - graph",
    "# {1} - name",
    "MSG_Query_Failed=Action failed: {0}; {1}"
})
public abstract class SimpleQueryPlugin extends AbstractPlugin {

    private static final Logger LOGGER = Logger.getLogger(SimpleQueryPlugin.class.getName());
    
    private static final String READING_INTERACTION = "Reading...";
    private static final String QUERYING_INTERACTION = "Querying...";
    private static final String EDITING_INTERACTION = "Editing...";

    public SimpleQueryPlugin() {
    }

    public SimpleQueryPlugin(String pluginName) {
        super(pluginName);
    }

    /**
     * Returns whether this plugin performs a 'significant edit' of the graph
     *
     * A significant edit will be recorded as an individual event in the graph's
     * undo stack, allowing the user to specifically undo and redo the edit this
     * plugin made to the graph. Non-significant edits are not added to the undo
     * stack individually. Instead, the group of all non-significant edits made
     * within a fixed time window are collected and placed onto the undo stack
     * as one event.
     *
     * Most plugins perform significant edits; non-significant edits should be
     * reserved for low-level plugins that make minor visual changes to the
     * graph and are expected to run repeatedly in a small space of time.
     *
     * @return a boolean specifying whether this plugin performs a 'significant
     * edit' of the graph.
     */
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public final void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        boolean inControlOfProgress = true;

        final Graph graph = graphs.getGraph();

        // If the graph no longer exists
        if (graph == null) {
            return;
        }

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            // Make the progress bar appear nondeterminent
            try {
                boolean cancelled = false;

                Object description = null;

                ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    interaction.setProgress(0, 0, READING_INTERACTION, true);
                    read(readableGraph, interaction, parameters);
                    if (!READING_INTERACTION.equals(interaction.getCurrentMessage())) {
                        inControlOfProgress = false;
                    }
                } finally {
                    readableGraph.release();
                }

                // Wait for all plugins to finish reading and querying
                if (inControlOfProgress) {
                    interaction.setProgress(0, 0, "Waiting For Other Plugins...", true);
                }

                // Wait at gate 1 for any CascadingQueryPlugins to finish reading
                graphs.waitAtGate(1);

                if (inControlOfProgress) {
                    interaction.setProgress(0, 0, QUERYING_INTERACTION, true);
                }
                query(interaction, parameters);
                if (inControlOfProgress && !QUERYING_INTERACTION.equals(interaction.getCurrentMessage())) {
                    inControlOfProgress = false;
                }

                if (inControlOfProgress) {
                    interaction.setProgress(0, 0, "Waiting to Edit Graph...", true);
                }
                WritableGraph writableGraph = graph.getWritableGraph(getName(), isSignificant(), this);
                try {
                    if (inControlOfProgress) {
                        interaction.setProgress(0, 0, EDITING_INTERACTION, true);
                    }

                    try {
                        description = describedEdit(writableGraph, interaction, parameters);
                        if (inControlOfProgress && !EDITING_INTERACTION.equals(interaction.getCurrentMessage())) {
                            inControlOfProgress = false;
                        }
                    } catch (InterruptedException e) {
                        cancelled = true;
                        interaction.notify(PluginNotificationLevel.INFO, "Action cancelled: " + graphs.getGraph() + ": " + getName());
                        throw e;
                    } catch (Exception e) {
                        cancelled = true;
                        final String msg = Bundle.MSG_Query_Failed(graph, getName());
                        LOGGER.log(Level.WARNING, msg);
                        throw e;
                    }

                } finally {
                    if (cancelled) {
                        writableGraph.rollBack();
                    } else {
                        writableGraph.commit(description);
                    }
                }
            } catch (DuplicateKeyException ex) {
                interaction.notify(PluginNotificationLevel.ERROR, ex.getMessage());
            } finally {
                interaction.setProgress(2, 1, inControlOfProgress ? "Finished" : interaction.getCurrentMessage(), false);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    @Override
    public final void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        throw new UnsupportedOperationException("Read mode is not supported by this plugin");
    }

    @Override
    public void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        boolean inControlOfProgress = true;

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            // Make the progress bar appear nondeterminent
            try {
                interaction.setProgress(0, 0, READING_INTERACTION, true);
                read(graph, interaction, parameters);
                if (!READING_INTERACTION.equals(interaction.getCurrentMessage())) {
                    inControlOfProgress = false;
                }

                if (inControlOfProgress) {
                    interaction.setProgress(0, 0, QUERYING_INTERACTION, true);
                }
                query(interaction, parameters);
                if (inControlOfProgress && !QUERYING_INTERACTION.equals(interaction.getCurrentMessage())) {
                    inControlOfProgress = false;
                }

                if (inControlOfProgress) {
                    interaction.setProgress(0, 0, EDITING_INTERACTION, true);
                }
                edit(graph, interaction, parameters);
                if (inControlOfProgress && !EDITING_INTERACTION.equals(interaction.getCurrentMessage())) {
                    inControlOfProgress = false;
                }
            } finally {
                interaction.setProgress(2, 1, inControlOfProgress ? "Finished" : interaction.getCurrentMessage(), true);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    /**
     * Runs the logic of the plugin and returns and object which describes the
     * changes made to the graph. This description object is not used by the
     * majority of plugins and is typically only used by plugins that update
     * internal attributes frequently and use the description as a way to
     * quickly flag to graph listeners that certain actions need to be taken.
     *
     * @param graph the graph on which to perform the edit.
     * @param interaction a {@link PluginInteraction} that allows feedback to
     * the user.
     * @param parameters the parameters for this plugin execution.
     *
     * @return An object that describes the changes made when the write lock is
     * released.
     *
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected Object describedEdit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        edit(graph, interaction, parameters);
        return null;
    }

    /**
     * Developers should override this method to implement the read stage of the
     * life cycle. This typically includes saving state from the graph that will
     * be needed during the query stage when the plugin no longer has access to
     * the graph.
     *
     * @param graph a GraphReadMethods representing a valid read lock on the
     * graph.
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
    }

    /**
     * Developers should override this method to implement the query stage of
     * the life cycle. This typically includes performing a long-running
     * calculation or long-running query on an external data source. As the
     * plugin does not have access to the graph during this stage, it relies on
     * information saved from the graph during the read stage.
     *
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected void query(final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
    }

    /**
     * Developers should override this method to implement the write stage of
     * the life cycle. This typically includes writing the results of the query
     * stage back to the graph using the write lock provided to this stage.
     *
     * @param graph a GraphWriteMethods representing a valid write lock on the
     * graph.
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
    }
}
