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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The PluginExcecutor provides convenient methods to execute sequences of
 * plugins both synchronously and asynchronously.
 *
 * @author sirius
 */
public class PluginExecutor {

    /**
     * Create a new PluginExecutor that begins by executing the specified
     * plugin.
     *
     * @param pluginId the id of the plugin to begin the sequence.
     * @param interactive should the plugin be run in interactive mode?
     * @return a new PluginExecutor.
     */
    public static PluginExecutor startWith(final String pluginId, final boolean interactive) {
        PluginExecutor executor = new PluginExecutor();
        executor.followedBy(pluginId, interactive);
        return executor;
    }

    /**
     * Create a new PluginExecutor that begins by executing the specified
     * plugin.
     *
     * @param pluginId the id of the plugin to begin the sequence.
     * @return a new PluginExecutor.
     */
    public static PluginExecutor startWith(final String pluginId) {
        return startWith(pluginId, false);
    }

    /**
     * Create a new PluginExecutor that begins by executing the specified
     * plugin.
     *
     * @param plugin the plugin to begin the sequence.
     * @param interactive should the plugin be run in interactive mode?
     * @return a new PluginExecutor.
     */
    public static PluginExecutor startWith(final Plugin plugin, final boolean interactive) {
        PluginExecutor executor = new PluginExecutor();
        executor.followedBy(plugin, interactive);
        return executor;
    }

    /**
     * Create a new PluginExecutor that begins by executing the specified
     * plugin.
     *
     * @param plugin the plugin to begin the sequence.
     * @return a new PluginExecutor.
     */
    public static PluginExecutor startWith(final Plugin plugin) {
        return startWith(plugin, false);
    }

    private PluginEntry currentEntry = null;
    private final List<PluginEntry> entries = new ArrayList<>();

    /**
     * Sets a plugin parameter value on the plugin most recently added to the
     * plugin sequence.
     *
     * @param parameterId the id of the parameter to set.
     * @param parameterValue the String value to set on the parameter.
     * @return this PluginExcecutor.
     */
    public PluginExecutor set(final String parameterId, final String parameterValue) {
        currentEntry.setParameterValue(parameterId, parameterValue);
        return this;
    }

    /**
     * Sets a plugin parameter value on the plugin most recently added to the
     * plugin sequence.
     *
     * @param parameterId the id of the parameter to set.
     * @param parameterValue the Object value to set on the parameter.
     * @return this PluginExcecutor.
     */
    public PluginExecutor set(final String parameterId, final Object parameterValue) {
        currentEntry.setParameterValue(parameterId, parameterValue);
        return this;
    }

    /**
     * Sets the plugin parameters for the plugin most recently added to the
     * plugin sequence.
     *
     * @param parameters the plugin parameters.
     * @return this PluginExcecutor.
     */
    public PluginExecutor set(final PluginParameters parameters) {
        for (String parameterId : parameters.getParameters().keySet()) {
            currentEntry.setParameterValue(parameterId, parameters.getObjectValue(parameterId));
        }
        return this;
    }

    /**
     * Add another plugin to the sequence of plugins to be executed by this
     * PluginExcutor.
     *
     * @param pluginId the id of the plugin to be added.
     * @param interactive should this plugin be run in interactive mode?
     * @return this PluginExcecutor.
     */
    public PluginExecutor followedBy(final String pluginId, final boolean interactive) {
        currentEntry = new PluginEntry(pluginId, interactive);
        entries.add(currentEntry);
        return this;
    }

    /**
     * Add another plugin to the sequence of plugins to be executed by this
     * PluginExcutor.
     *
     * @param pluginId the id of the plugin to be added.
     * @return this PluginExcecutor.
     */
    public PluginExecutor followedBy(final String pluginId) {
        return followedBy(pluginId, false);
    }

    /**
     * Add another plugin to the sequence of plugins to be executed by this
     * PluginExcutor.
     *
     * @param plugin the plugin to be added.
     * @param interactive should this plugin be run in interactive mode?
     * @return this PluginExcecutor.
     */
    public PluginExecutor followedBy(final Plugin plugin, final boolean interactive) {
        currentEntry = new PluginEntry(plugin, interactive);
        entries.add(currentEntry);
        return this;
    }

    /**
     * Add another plugin to the sequence of plugins to be executed by this
     * PluginExcutor.
     *
     * @param plugin the plugin to be added.
     * @return this PluginExcecutor.
     */
    public PluginExecutor followedBy(final Plugin plugin) {
        return followedBy(plugin, false);
    }

    /**
     * Execute the specified sequence of plugins using a single write lock on
     * the graph. A WritableGraph will be created on the specified graph and
     * passed to each plugin in the sequence in turn. While this will happen on
     * a different thread to the calling thread, each plugin will be run
     * synchronously on the same thread. The name of this combined plugin run
     * will be the same as that of the first plugin in the sequence.
     *
     * @param graph the graph to run the plugins on.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeWriteLater(final Graph graph) {
        return executeWriteLater(graph, (Future<?>) null);
    }

    /**
     * Execute the specified sequence of plugins using a single write lock on
     * the graph. A WritableGraph will be created on the specified graph and
     * passed to each plugin in the sequence in turn. While this will happen on
     * a different thread to the calling thread, each plugin will be run
     * synchronously on the same thread.
     *
     * @param graph the graph to run the plugins on.
     * @param pluginName the name by which this combined plugin run will appear
     * in Constellation.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeWriteLater(final Graph graph, final String pluginName) {
        return executeWriteLater(graph, pluginName, null);
    }

    /**
     * Execute the specified sequence of plugins using a single write lock on
     * the graph. A WritableGraph will be created on the specified graph and
     * passed to each plugin in the sequence in turn. While this will happen on
     * a different thread to the calling thread, each plugin will be run
     * synchronously on the same thread. The name of this combined plugin run
     * will be the same as that of the first plugin in the sequence.
     *
     * @param graph the graph to run the plugins on.
     * @param future if not null then the running of the plugin sequence will
     * wait for the future to finish.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeWriteLater(final Graph graph, final Future<?> future) {
        if (entries.size() == 1) {
            return entries.get(0).executeLater(graph, future);
        } else {
            return executeWriteLater(graph, entries.get(0).plugin.getName(), future);
        }
    }

    /**
     * Execute the specified sequence of plugins using a single write lock on
     * the graph. A WritableGraph will be created on the specified graph and
     * passed to each plugin in the sequence in turn. While this will happen on
     * a different thread to the calling thread, each plugin will be run
     * synchronously on the same thread.
     *
     * @param graph the graph to run the plugins on.
     * @param pluginName the name by which this combined plugin run will appear
     * in Constellation.
     * @param future if not null then the running of the plugin sequence will
     * wait for the future to finish.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeWriteLater(final Graph graph, final String pluginName, final Future<?> future) {
        return PluginExecution.withPlugin(new SimplePlugin(pluginName) {

            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

                WritableGraph wg = graphs.getGraph().getWritableGraph(pluginName, true);
                try {
                    for (PluginEntry entry : entries) {
                        entry.executeNow(graphs.getGraph());
                    }
                } finally {
                    wg.commit();
                }
            }

        }).waitingFor(future).executeLater(graph);
    }

    /**
     * Execute the specified sequence of plugins using a single read lock on the
     * graph. A ReadableGraph will be created on the specified graph and passed
     * to each plugin in the sequence in turn. While this will happen on a
     * different thread to the calling thread, each plugin will be run
     * synchronously on the same thread.
     *
     * @param graph the graph to run the plugins on.
     * @param pluginName the name by which this combined plugin run will appear
     * in Constellation.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeReadLater(final Graph graph, final String pluginName) {
        return executeReadLater(graph, pluginName, null);
    }

    /**
     * Execute the specified sequence of plugins using a single read lock on the
     * graph. A ReadableGraph will be created on the specified graph and passed
     * to each plugin in the sequence in turn. While this will happen on a
     * different thread to the calling thread, each plugin will be run
     * synchronously on the same thread. The name of this combined plugin run
     * will be the same as that of the first plugin in the sequence.
     *
     * @param graph the graph to run the plugins on.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeReadLater(final Graph graph) {
        return executeReadLater(graph, entries.get(0).plugin.getName(), null);
    }

    /**
     * Execute the specified sequence of plugins using a single read lock on the
     * graph. A ReadableGraph will be created on the specified graph and passed
     * to each plugin in the sequence in turn. While this will happen on a
     * different thread to the calling thread, each plugin will be run
     * synchronously on the same thread. The name of this combined plugin run
     * will be the same as that of the first plugin in the sequence.
     *
     * @param graph the graph to run the plugins on.
     * @param future if not null then the running of the plugin sequence will
     * wait for the future to finish.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeReadLater(final Graph graph, Future<?> future) {
        return executeReadLater(graph, entries.get(0).plugin.getName(), future);
    }

    /**
     * Execute the specified sequence of plugins using a single read lock on the
     * graph. A ReadableGraph will be created on the specified graph and passed
     * to each plugin in the sequence in turn. While this will happen on a
     * different thread to the calling thread, each plugin will be run
     * synchronously on the same thread.
     *
     * @param graph the graph to run the plugins on.
     * @param pluginName the name by which this combined plugin run will appear
     * in Constellation.
     * @param future if not null then the running of the plugin sequence will
     * wait for the future to finish.
     * @return a Future object representing the running of this plugin sequence.
     */
    public Future<?> executeReadLater(final Graph graph, final String pluginName, final Future<?> future) {
        return PluginExecution.withPlugin(new SimplePlugin(pluginName) {

            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

                ReadableGraph rg = graphs.getGraph().getReadableGraph();
                try {
                    for (PluginEntry entry : entries) {
                        entry.executeNow(graphs.getGraph());
                    }
                } finally {
                    rg.release();
                }
            }
        }).waitingFor(future).executeLater(graph);
    }

    /**
     * Execute the specified sequence of plugins synchronously on the calling
     * thread. Each plugin will gain its own read or write lock meaning that
     * they will each appear as separate, independent plugin calls.
     *
     * @param graph the graph to run the plugin sequence on.
     *
     * @throws InterruptedException if the plugin run is canceled or
     * interrupted.
     * @throws PluginException if a well understood problem occurs during the
     * running of the plugin.
     */
    public void executeNow(final Graph graph) throws InterruptedException, PluginException {
        for (PluginEntry entry : entries) {
            entry.executeNow(graph);
        }
    }

    /**
     * Execute the specified sequence of plugins synchronously on the calling
     * thread using the specified GraphWriteMethods (write lock).
     *
     * @param graph the graph to run the plugin sequence on.
     *
     * @throws InterruptedException if the plugin run is canceled or
     * interrupted.
     * @throws PluginException if a well understood problem occurs during the
     * running of the plugin.
     */
    public void executeNow(final GraphWriteMethods graph) throws InterruptedException, PluginException {
        for (PluginEntry entry : entries) {
            entry.executeNow(graph);
        }
    }

    private static class PluginEntry {

        private final boolean interactive;
        private final Plugin plugin;
        private final PluginParameters parameters;

        public PluginEntry(final String pluginId, final boolean interactive) {
            this(PluginRegistry.get(pluginId), interactive);
        }

        public PluginEntry(final Plugin plugin, final boolean interactive) {
            this.interactive = interactive;
            this.plugin = plugin;
            this.parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }

        public void setParameterValue(final String parameterId, final Object parameterValue) {
            parameters.getParameters().get(parameterId).setObjectValue(parameterValue);
        }

        public void setParameterValue(final String parameterId, final String parameterValue) {
            parameters.getParameters().get(parameterId).setStringValue(parameterValue);
        }

        public Object executeNow(final Graph graph) throws InterruptedException, PluginException {
            return PluginEnvironment.getDefault().executePluginNow(graph, plugin, parameters, interactive);
        }

        public Object executeNow(final GraphWriteMethods graph) throws InterruptedException, PluginException {
            return PluginEnvironment.getDefault().executeEditPluginNow(graph, plugin, parameters, interactive);
        }

        public Future<?> executeLater(final Graph graph, final Future<?> future) {
            final List<Future<?>> futures = future == null ? null : Arrays.asList(future);
            return PluginEnvironment.getDefault().executePluginLater(graph, plugin, parameters, interactive, futures, null);
        }
    }
}
