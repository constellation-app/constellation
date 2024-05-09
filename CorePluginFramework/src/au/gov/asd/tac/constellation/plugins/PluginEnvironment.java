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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.List;
import java.util.concurrent.Future;
import org.openide.util.Lookup;

/**
 * An interface representing the environment with in Constellation where plugins
 * are run.
 *
 * @author sirius
 */
public abstract class PluginEnvironment {

    /**
     * Get the default implementation of the PluginEnvironment
     *
     * @return An instance of the default PluginEnvironment as determined by
     * lookup.
     */
    public static PluginEnvironment getDefault() {
        return Lookup.getDefault().lookup(PluginEnvironment.class);
    }
    
    /**
     * Execute a plugin asynchronously on a different thread.
     *
     * This method was the existing implementation for ExecutePluginLater() for this interface and has been overloaded to enable disclaimer 
     * messages to be provided to interactively run plugins. This method signature has been maintained in the event that external adapters or modules
     * rely on it.
     * 
     * @param graph the graph to run the plugin on.
     * @param plugin the plugin to run.
     * @param parameters the parameters that will configure this run of the
     * plugin.
     * @param interactive should the framework involve the user when running
     * this plugin or run the plugin programmatically.
     * @param async The plugin will not begin to execute until all of these
     * Futures have completed. If this Future is null, the plugin will execute
     * without waiting.
     * @param synchronizer The plugin synchronizer can synchronize the running
     * of multiple plugins running on different threads.
     * @return A Future representing the result of the asynchronous plugin.
     */
    public Future<?> executePluginLater(final Graph graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive, final List<Future<?>> async, final PluginSynchronizer synchronizer){
        return executePluginLater(graph, plugin, parameters, interactive, null, async, synchronizer); 
    }

    /**
     * Execute a plugin asynchronously on a different thread.
     *
     * @param graph the graph to run the plugin on.
     * @param plugin the plugin to run.
     * @param parameters the parameters that will configure this run of the
     * plugin.
     * @param interactive should the framework involve the user when running
     * this plugin or run the plugin programmatically.
     * @param disclaimer a optional disclaimer to the user when plugin is run interactively.
     * @param async The plugin will not begin to execute until all of these
     * Futures have completed. If this Future is null, the plugin will execute
     * without waiting.
     * @param synchronizer The plugin synchronizer can synchronize the running
     * of multiple plugins running on different threads.
     *
     * @return A Future representing the result of the asynchronous plugin.
     */
    public abstract Future<?> executePluginLater(final Graph graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive, final String disclaimer, final List<Future<?>> async, final PluginSynchronizer synchronizer);

    /**
     * Execute a plugin synchronously on the current thread.
     *
     * @param graph the graph to run the plugin on.
     * @param plugin the plugin to run.
     * @param parameters the parameters that will configure this run of the
     * plugin.
     * @param interactive should the framework involve the user when running
     * this plugin or run the plugin programmatically.
     * @return an object representing the result of this plugin run.
     * @throws InterruptedException if the plugin run is canceled or
     * interrupted.
     * @throws PluginException if a well understood problem occurs during the
     * running of the plugin.
     */
    public abstract Object executePluginNow(final Graph graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException;

    /**
     * Execute a plugin synchronously on the current thread using the supplied
     * GraphWriteMethods (write lock) and with the specified parameters.
     *
     * @param graph the graph to run the plugin on.
     * @param plugin the plugin to run.
     * @param parameters the parameters that will configure this run of the
     * plugin.
     * @param interactive should the framework involve the user when running
     * this plugin or run the plugin programmatically.
     * @return an object representing the result of this plugin run.
     * @throws InterruptedException if the plugin run is canceled or
     * interrupted.
     * @throws PluginException if a well understood problem occurs during the
     * running of the plugin.
     */
    public abstract Object executeEditPluginNow(final GraphWriteMethods graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException;

    /**
     * Execute a plugin synchronously on the current thread using the supplied
     * GraphReadMethods (read lock) and with the specified parameters.
     *
     * @param graph the GraphReadMethods to use to access the graph. This will
     * be a valid GraphReadMethods that represents a valid read lock on the
     * graph.
     * @param plugin the plugin to run.
     * @param parameters the parameters that will configure this run of the
     * plugin.
     * @param interactive should the framework involve the user when running
     * this plugin or run the plugin programmatically.
     * @return an object representing the result of this plugin run.
     * @throws InterruptedException if the plugin run is canceled or
     * interrupted.
     * @throws PluginException if a well understood problem occurs during the
     * running of the plugin.
     */
    public abstract Object executeReadPluginNow(final GraphReadMethods graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException;
}
