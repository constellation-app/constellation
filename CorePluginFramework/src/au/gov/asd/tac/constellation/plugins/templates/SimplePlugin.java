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
import au.gov.asd.tac.constellation.plugins.AbstractPlugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * A plugin template for plugins requiring complete control over how they read
 * and write to the graph.
 * <p>
 * This is the simplest plugin template; it should be suitable for most plugin
 * implementations.
 * <p>
 * The plugin template will set the graph to "busy" and start the progress bar.
 * The execute method is then called which has complete responsibility for
 * locking the graph.
 *
 * Developers should override the execute method to add plugin logic.
 *
 * @author sirius
 */
@NbBundle.Messages({
    "# {0} - graph",
    "# {1} - name",
    "MSG_Execute_Failed=Action failed: {0}; {1}"
})
public abstract class SimplePlugin extends AbstractPlugin {

    private static final Logger LOGGER = Logger.getLogger(SimplePlugin.class.getName());

    private static final String WAITING_INTERACTION = "Waiting...";
    private static final String FINISHED_INTERACTION = "Finished";

    public SimplePlugin() {
    }

    public SimplePlugin(String pluginName) {
        super(pluginName);
    }

    @Override
    public final void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {

        final Graph graph = graphs.getGraph();
        // Make the graph appear busy
        if (graph != null) {
            interaction.setBusy(graph.getId(), true);
        }

        try {

            // Make the progress bar appear nondeterminent
            interaction.setProgress(0, 0, WAITING_INTERACTION, true);
            try {
                execute(graphs, interaction, parameters);
            } catch (final InterruptedException e) {
                // Notify the user that the plugin was interrupted and throw the exception back to the caller for generic handling.
                interaction.notify(PluginNotificationLevel.INFO, "Plugin cancelled: " + graphs.getGraph() + SeparatorConstants.SEMICOLON + " " + getName());
                throw e;
            } catch (final PluginException e) {
                // Logging the PluginException and throwing the exception back to the caller for generic handling.
                final String msg0 = String.format("PluginException caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                LOGGER.log(Level.SEVERE, "{0}" + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + "{1}" + SeparatorConstants.NEWLINE + "{2}", new Object[]{msg0, msg, e.getMessage()});
                throw e;
            } catch (final RuntimeException e) {
                // Notify the user that there was a RuntimeException and throw the exception back to the caller for generic handling.
                final String msg0 = String.format("Unexpected non-plugin exception caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                interaction.notify(PluginNotificationLevel.ERROR, msg0 + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + msg + SeparatorConstants.NEWLINE + e.getMessage());
                throw e;
            } finally {
                interaction.setProgress(2, 1, FINISHED_INTERACTION, true);
            }

        } finally {
            if (graph != null) {
                interaction.setBusy(graph.getId(), false);
            }
        }
    }

    @Override
    public void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            // Make the progress bar appear nondeterminent
            interaction.setProgress(0, 0, WAITING_INTERACTION, true);
            try {
                read(graph, interaction, parameters);
            } catch (final InterruptedException e) {
                // Notify the user that the plugin was interrupted and throw the exception back to the caller for generic handling.
                interaction.notify(PluginNotificationLevel.INFO, "Plugin cancelled: " + graph + SeparatorConstants.SEMICOLON + " " + getName());
                throw e;
            } catch (final PluginException e) {
                // Logging the PluginException and throwing the exception back to the caller for generic handling.
                final String msg0 = String.format("PluginException caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                LOGGER.log(Level.SEVERE, "{0}" + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + "{1}" + SeparatorConstants.NEWLINE + "{2}", new Object[]{msg0, msg, e.getMessage()});
                throw e;
            } catch (final RuntimeException e) {
                // Notify the user that there was a RuntimeException and throw the exception back to the caller for generic handling.
                final String msg0 = String.format("Unexpected non-plugin exception caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                interaction.notify(PluginNotificationLevel.ERROR, msg0 + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + msg + SeparatorConstants.NEWLINE + e.getMessage());
                throw e;
            } finally {
                interaction.setProgress(2, 1, FINISHED_INTERACTION, true);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    @Override
    public void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            // Make the progress bar appear nondeterminent
            interaction.setProgress(0, 0, WAITING_INTERACTION, true);
            try {
                edit(graph, interaction, parameters);
            } catch (final InterruptedException e) {
                // Notify the user that the plugin was interrupted and throw the exception back to the caller for generic handling.
                interaction.notify(PluginNotificationLevel.INFO, "Plugin cancelled: " + graph + SeparatorConstants.SEMICOLON + " " + getName());
                throw e;
            } catch (final PluginException e) {
                // Logging the PluginException and throwing the exception back to the caller for generic handling.
                final String msg0 = String.format("PluginException caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                LOGGER.log(Level.SEVERE, "{0}" + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + "{1}" + SeparatorConstants.NEWLINE + "{2}", new Object[]{msg0, msg, e.getMessage()});
                throw e;
            } catch (final RuntimeException e) {
                // Notify the user that there was a RuntimeException and throw the exception back to the caller for generic handling.
                final String msg0 = String.format("Unexpected non-plugin exception caught in %s.run()", SimplePlugin.class.getName());
                final String msg = Bundle.MSG_Execute_Failed(graph, getName());
                interaction.notify(PluginNotificationLevel.ERROR, msg0 + SeparatorConstants.SEMICOLON + SeparatorConstants.NEWLINE + msg + SeparatorConstants.NEWLINE + e.getMessage());
                throw e;
            } finally {
                interaction.setProgress(2, 1, FINISHED_INTERACTION, true);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    /**
     * Developers should override this method to implement the logic for this
     * plugin.
     *
     * @param graphs The currently open graphs.
     * @param interaction Interact with the user.
     * @param parameters Plugin parameters.
     *
     * @throws InterruptedException if the operation is canceled.
     * @throws PluginException if an anticipated error occurs during plugin.
     */
    protected abstract void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException;

    /**
     * Developers can also override this method if they wish to support running
     * of this plugin by providing an existing read lock on the graph.
     *
     * @param graph a currently valid GraphReadMethods representing a currently
     * active read lock on the graph.
     * @param interaction a PluginInteraction object allowing the plugin to
     * interact with the UI.
     * @param parameters the parameters that configure the plugin.
     * @throws InterruptedException if the plugin is canceled.
     * @throws PluginException if an anticipated error occurs during plugin.
     * execution.
     */
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {
        throw new UnsupportedOperationException("Read mode is not supported by this plugin");
    }

    /**
     * Developers can also override this method if they wish to support running
     * of this plugin by providing an existing write lock on the graph.
     *
     * @param graph a currently valid GraphWriteMethods representing a currently
     * active write lock on the graph.
     * @param interaction a PluginInteraction object allowing the plugin to
     * interact with the UI.
     * @param parameters the parameters that configure the plugin.
     * @throws InterruptedException if the plugin is canceled.
     * @throws PluginException if an anticipated error occurs during plugin.
     * execution.
     */
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException, RuntimeException {
        throw new UnsupportedOperationException("Edit mode is not supported by this plugin");
    }
}
