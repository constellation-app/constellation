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
package au.gov.asd.tac.constellation.plugins.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.AbstractPlugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A plugin template for plugins that edit the graph is a single efficient
 * transaction.
 * <p>
 * This template will:
 * <ol>
 * <li>Set the graph to busy.</li>
 * <li>Start the progress bar.</li>
 * <li>Get and release a write lock on the graph.</li>
 * <li>Call the edit method where developers should implement their plugin
 * logic.</li>
 * </ol>
 *
 * @author sirius
 */
public abstract class SimpleEditPlugin extends AbstractPlugin {

    private static final Logger LOGGER = Logger.getLogger(SimpleEditPlugin.class.getName());

    private static final String WAITING_INTERACTION = "Waiting...";

    protected SimpleEditPlugin() {
    }

    public SimpleEditPlugin(String pluginName) {
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
    public final void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {

        boolean inControlOfProgress = true;

        final Graph graph = graphs.getGraph();
        //graph no longer exists
        if (graph == null) {
            LOGGER.log(Level.WARNING, "Null graph not allowed in a {0}", SimpleEditPlugin.class.getSimpleName());
            return;
        }

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {
            // Make the progress bar appear nondeterminent
            interaction.setProgress(0, 0, WAITING_INTERACTION, true);

            try {
                boolean cancelled = false;
                Object description = null;
                WritableGraph writableGraph = graph.getWritableGraph(getName(), isSignificant(), this);

                try {
                    interaction.setProgress(0, 0, "Editing...", true);

                    try {
                        description = describedEdit(writableGraph, interaction, parameters);
                        if (!"Editing...".equals(interaction.getCurrentMessage())) {
                            inControlOfProgress = false;
                        }
                    } catch (final Exception ex) {
                        cancelled = true;
                        throw ex;
                    }
                } finally {
                    if (cancelled) {
                        writableGraph.rollBack();
                    } else {
                        writableGraph.commit(description);
                    }
                }
            } finally {
                interaction.setProgress(2, 1, inControlOfProgress ? "Finished" : interaction.getCurrentMessage(), true);
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
            interaction.setProgress(0, 0, WAITING_INTERACTION, true);

            try {
                edit(graph, interaction, parameters);
                if (!WAITING_INTERACTION.equals(interaction.getCurrentMessage())) {
                    inControlOfProgress = false;
                }
            } finally {
                interaction.setProgress(2, 1, "Finished", true);
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
     * @param graph the graph on which to run the plugin.
     * @param interaction a
     * {@link au.gov.asd.tac.constellation.plugins.PluginExecution} to allow
     * feedback to the user.
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
        return getName();
    }

    /**
     * Developers should implement this method to implement the logic of their
     * plugin.
     *
     * @param graph a GraphWriteMethods object representing a current write lock
     * on the graph.
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected abstract void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;
}
