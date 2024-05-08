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
package au.gov.asd.tac.constellation.plugins.templates;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.AbstractPlugin;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginExecutionStageConstants;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A plugin template for a plugin that naturally works within a read-query-write
 * life cycle with the additional complexity of multiple plugins running in
 * parallel.
 *
 * During the read stage, the plugin has a read lock on the graph and can
 * extract all required information from the graph. During the query stage, the
 * plugin has no lock on the graph and typically uses information derived during
 * the read stage to perform long-running calculations or long-running queries
 * on external databases. During the write stage, the plugin has a write lock on
 * the graph and is able to update the graph based on the results of the query
 * stage.
 *
 * Using the read-query-write life cycle has the advantage that the plugin
 * releases all locks on the graph during the long query stage allowing the
 * graph to remain responsive the the user and other plugins during this time.
 * This also means that the plugin must elegantly handle the case where the
 * graph has been altered by another plugin during the query stage.
 *
 * This template will:
 * <ol>
 * <li>Set the graph to busy.</li>
 * <li>Start the progress bar.</li>
 * <li>Get a read lock on the graph</li>
 * <li>Allow child plugins to read the graph. Developers should override
 * getChildPlugins</li>
 * <li>Get and release a write lock on the graph.</li>
 * <li>Call the edit method where developers should implement their plugin
 * logic.</li>
 * </ol>
 *
 * @author sirius
 */
public abstract class CascadingQueryPlugin extends AbstractPlugin {

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
        
        // Make the progress bar appear indeterminent
        final Graph graph = graphs.getGraph();
        final int totalSteps = -1;

        // If the graph no longer exists
        if (graph == null) {
            return;
        }

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            
            try {
                interaction.setExecutionStage(1, totalSteps, PluginExecutionStageConstants.RUNNING, "Executing child plugins", true);

                final Map<Plugin, PluginParameters> childPlugins = getChildPlugins(parameters);

                final PluginSynchronizer pluginSynchronizer = new PluginSynchronizer(childPlugins.size() + 1);

                for (final Entry<Plugin, PluginParameters> entry : childPlugins.entrySet()) {
                    PluginExecution.withPlugin(entry.getKey()).withParameters(entry.getValue()).synchronizingOn(pluginSynchronizer).executeLater(graph);
                }

                // Wait at gate 0 for CascadingQueryPlugin to finish reading
                graphs.waitAtGate(0);

                // Wait for child plugins at gate 1 to finish reading
                pluginSynchronizer.waitForGate(1);

                // Wait for all plugins to finish reading and querying
                interaction.setExecutionStage(2, totalSteps, PluginExecutionStageConstants.WAITING,  "Waiting For Other Plugins...", true);

                // Wait at gate 1 for any SimpleQueryPlugins to finish reading
                graphs.waitAtGate(1);

            } catch (DuplicateKeyException ex) {
                interaction.notify(PluginNotificationLevel.ERROR, ex.getMessage());
            } finally {
                interaction.setExecutionStage(3, 2, PluginExecutionStageConstants.COMPLETE, "Finished", true);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    /**
     * Developers should override this method by returning a map of the child
     * plugins which need to run in parallel.
     *
     * A typical design pattern of implementing this would be to pass the list
     * of systems that are to run and create a map of the selected plugins
     *
     * <pre>
     *   List&lt;String&gt; systems = MultiChoiceParameterType.getCheckedChoices(parameters.getParameters().get(PARAMETER_SYSTEMS));
     *   Map&lt;Plugin, PluginParameters&gt; pluginsMap = new HashMap&lt;&gt;();
     *
     *   for (Plugin plugin : plugins) {
     *       if (systems.contains(plugin.getName())) {
     *           pluginsMap.put(plugin, parameters);
     *       }
     *   }
     *
     *   return pluginsMap;
     * </pre>
     *
     * @param parameters The PluginParameters created by the plugin dynamically
     * @return A map of the Plugin and PluginParameters
     */
    protected abstract Map<Plugin, PluginParameters> getChildPlugins(final PluginParameters parameters);

    @Override
    public final void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        throw new UnsupportedOperationException("Read mode is not supported by this plugin");
    }

    @Override
    public void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int totalSteps = -1;

        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {

            // Make the progress bar appear nondeterminent
            try {
                interaction.setExecutionStage(1, totalSteps, PluginExecutionStageConstants.RUNNING, "Executing child plugins", true);

                final Map<Plugin, PluginParameters> childPlugins = getChildPlugins(parameters);

                for (final Entry<Plugin, PluginParameters> entry : childPlugins.entrySet()) {
                    PluginExecution.withPlugin(entry.getKey()).withParameters(entry.getValue()).executeNow(graph);
                }
            } finally {
                interaction.setExecutionStage(2, 1, PluginExecutionStageConstants.COMPLETE, "Finished", true);
            }

        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }
   
}
