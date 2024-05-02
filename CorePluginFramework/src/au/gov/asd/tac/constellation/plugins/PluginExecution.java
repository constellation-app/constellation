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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * A PluginExecution provides a convenient alternative builder-based method for
 * executing plugins in Constellation rather than calling
 * {@link PluginEnvironment} methods directly. In many cases, most of the
 * calling options remain at their default values and this can cause direct
 * calls to {@link PluginEnvironment} to become unnecessarily verbose. By using
 * sensible defaults and a builder pattern, executing a plugin using the
 * PluginExecution becomes more concise and legible. All the functionality
 * available through the traditional calls is also available through the
 * PluginExecution interface. The {@link PluginEnvironment} methods are still
 * available if required.
 *
 * @author sirius
 * @see PluginEnvironment
 */
public class PluginExecution {

    private static final Logger LOGGER = Logger.getLogger(PluginExecution.class.getName());

    private static final PluginEnvironment DEFAULT_PLUGIN_ENVIRONMENT = PluginEnvironment.getDefault();

    private PluginEnvironment environment = DEFAULT_PLUGIN_ENVIRONMENT;

    private final Plugin plugin;

    private boolean interactive = false;
    private String disclaimer = null;
    private PluginParameters parameters = null;
    private List<Future<?>> futures = null;
    private PluginSynchronizer synchronizer = null;

    // private constructor to prevent instantiation other than with the static methods.
    private PluginExecution(final Plugin plugin) {
        this.plugin = plugin;
        LOGGER.fine(plugin.getName());
    }

    /**
     * Creates a new PluginExecution that will execute the specified plugin.
     * <p>
     * The new PluginExecution will have the following defaults:
     * <ol>
     * <li><b>interactive</b>: <code>false</code> (to modify see
     * {@link PluginExecution#interactively(boolean)}.
     * <li><b>parameters</b>: default parameters for plugin (to modify see {@link PluginExecution#withParameters(au.gov.asd.tac.constellation.plugins.parameters.PluginParameters)},
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.String)}
     * and
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.Object)}.
     * <li><b>futures</b>: <code>null</code> (to modify see
     * {@link PluginExecution#waitingFor(java.util.concurrent.Future)} and
     * {@link PluginExecution#waitingFor(java.util.List)}.
     * <li><b>synchronizer</b>: <code>null</code> (to modifiy see
     * {@link PluginExecution#synchronizingOn(au.gov.asd.tac.constellation.plugins.PluginSynchronizer)}.
     * <li><b>environment</b>: the default environment
     * {@link PluginEnvironment#getDefault()} (to modifiy see
     * {@link PluginExecution#inEnvironment(au.gov.asd.tac.constellation.plugins.PluginEnvironment)}.
     * </ol>
     *
     * A new PluginExecution can also be created using a plugin name with
     * {@link PluginExecution#withPlugin(java.lang.String)}
     *
     * @param plugin the plugin that will be executed by this PluginExecutor.
     *
     * @return a new PluginExecutor.
     */
    public static PluginExecution withPlugin(final Plugin plugin) {
        return new PluginExecution(plugin);
    }

    /**
     * Creates a new PluginExecution that will execute a plugin with the
     * specified plugin name.
     * <p>
     * The new PluginExecution will have the following defaults:
     * <ol>
     * <li><b>interactive</b>: <code>false</code> (to modify see
     * {@link PluginExecution#interactively(boolean)}.
     * <li><b>parameters</b>: default parameters for plugin (to modify see {@link PluginExecution#withParameters(au.gov.asd.tac.constellation.plugins.parameters.PluginParameters)},
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.String)}
     * and
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.Object)}.
     * <li><b>futures</b>: <code>null</code> (to modify see
     * {@link PluginExecution#waitingFor(java.util.concurrent.Future)} and
     * {@link PluginExecution#waitingFor(java.util.List)}.
     * <li><b>synchronizer</b>: <code>null</code> (to modifiy see
     * {@link PluginExecution#synchronizingOn(au.gov.asd.tac.constellation.plugins.PluginSynchronizer)}.
     * <li><b>environment</b>: the default environment
     * {@link PluginEnvironment#getDefault()} (to modifiy see
     * {@link PluginExecution#inEnvironment(au.gov.asd.tac.constellation.plugins.PluginEnvironment)}.
     * </ol>
     *
     * A new PluginExecution can also be created using a plugin directly with
     * {@link PluginExecution#withPlugin(au.gov.asd.tac.constellation.plugins.Plugin)}
     *
     * @param pluginName the name of the plugin that will be executed by this
     * PluginExecutor.
     *
     * @return a new PluginExecutor.
     */
    public static PluginExecution withPlugin(final String pluginName) {
        return new PluginExecution(PluginRegistry.get(pluginName));
    }

    /**
     * Sets the interactivity of this PluginExecution. When the PluginExecutor
     * executes its plugin on the {@link PluginEnvironment} it will pass this
     * value as the interactive parameter.
     *
     * @param interactive the new interactive value for this PluginExecutor.
     *
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution interactively(final boolean interactive) {
        this.interactive = interactive;
        return this;
    }
    
    /**
     * Sets the interactivity of this PluginExecution.When the PluginExecutor
     * executes its plugin on the {@link PluginEnvironment} it will pass this
     * value as the interactive parameter with the provided disclaimer.
     *
     * @param interactive the new interactive value for this PluginExecutor.
     * @param discalimer
     *
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution interactively(final boolean interactive, final String discalimer) {
        this.interactive = interactive;
        disclaimer = discalimer;
        return this;
    }

    /**
     * Replaces any parameters previously configured on this PluginExecutor with
     * new parameters. When the PluginExecutor executes its plugin on the
     * {@link PluginEnvironment} it will pass this value as the parameters
     * parameter. The values for these parameters can be further modified with
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.String)}
     * and
     * {@link PluginExecution#withParameter(java.lang.String, java.lang.Object)}.
     * If null is specified as the new parameters the plugin will be executed
     * with the plugin's default parameters.
     *
     * @param parameters the new PluginParameters for this PluginExecution.
     *
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution withParameters(final PluginParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Sets the specified plugin parameter value on the current
     * {@link PluginParameters} configured on this PluginExecution by calling
     * {@link PluginParameters#setStringValue(java.lang.String, java.lang.String)}.
     * The configured parameters may have been previously configured using
     * {@link PluginExecution#withParameters(au.gov.asd.tac.constellation.plugins.parameters.PluginParameters)}.
     * If there are currently no {@link PluginParameters} configured then a new
     * {@link PluginParameters} will be created using
     * {@link DefaultPluginParameters#getDefaultParameters(au.gov.asd.tac.constellation.plugins.Plugin)}.
     *
     * @param parameterId the id of the parameter value to be set.
     * @param parameterValue the new value for the parameter.
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution withParameter(final String parameterId, final String parameterValue) {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        parameters.setStringValue(parameterId, parameterValue);
        return this;
    }

    /**
     * Sets the specified plugin parameter value on the current
     * {@link PluginParameters} configured on this PluginExecution by calling
     * {@link PluginParameters#setObjectValue(java.lang.String, java.lang.Object)}.
     * The configured parameters may have been previously configured using
     * {@link PluginExecution#withParameters(au.gov.asd.tac.constellation.plugins.parameters.PluginParameters)}.
     * If there are currently no {@link PluginParameters} configured then a new
     * {@link PluginParameters} will be created using
     * {@link DefaultPluginParameters#getDefaultParameters(au.gov.asd.tac.constellation.plugins.Plugin)}.
     *
     * @param parameterId the id of the parameter value to be set.
     * @param parameterValue the new value for the parameter.
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution withParameter(final String parameterId, final Object parameterValue) {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        parameters.setObjectValue(parameterId, parameterValue);
        return this;
    }

    /**
     * Causes the PluginExecution to wait for the completion of the specified
     * future before executing its plugin. Any {@link Future}/s previously
     * configured on this PluginExecution will be replaced. The {@link Future}/s
     * configured on this PluginExecution when the plugin is executed will be
     * passed to the {@link PluginEnvironment} as the async parameter.
     *
     * @param future the future to wait for before executing this
     * PluginExecutors plugin.
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution waitingFor(final Future<?> future) {
        this.futures = future == null ? null : Arrays.asList(future);
        return this;
    }

    /**
     * Causes the PluginExecution to wait for the completion of the specified
     * futures before executing its plugin. Any {@link Future}/s previously
     * configured on this PluginExecution will be replaced. The {@link Future}/s
     * configured on this PluginExecution when the plugin is executed will be
     * passed to the {@link PluginEnvironment} as the async parameter.
     *
     * @param futures the futures to wait for before executing this
     * PluginExecutors plugin.
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution waitingFor(final List<Future<?>> futures) {
        this.futures = futures;
        return this;
    }

    /**
     * Causes this PluginExecution to synchronize its plugin on the specified
     * {@link PluginSynchronizer}. Any {@link PluginSynchronizer} previously
     * configured on this PluginExecution will be replaced. When the
     * PluginExecution executes its {@link Plugin} on a
     * {@link PluginEnvironment} the synchronizer will be passed as the
     * <code>synchronizer</code> parameter.
     *
     * @param synchronizer the PluginSynchronizer for this PluginExecution.
     *
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution synchronizingOn(final PluginSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
        return this;
    }

    /**
     * Sets a new {@link PluginEnvironment} that this PluginExecution will use
     * to run its plugin. In general, callers will not call this method, instead
     * using the default value which is the default plugin environment set by
     * calling
     * {@link DefaultPluginParameters#getDefaultParameters(au.gov.asd.tac.constellation.plugins.Plugin)}.
     *
     * @param environment the new {@link PluginEnvironment} to call plugin on.
     *
     * @return this to allow chaining of configuration calls.
     */
    public PluginExecution inEnvironment(final PluginEnvironment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Causes this PluginExecutor to execute its plugin on a new thread by
     * calling
     * {@link PluginEnvironment#executePluginLater(au.gov.asd.tac.constellation.graph.Graph, au.gov.asd.tac.constellation.plugins.Plugin, au.gov.asd.tac.constellation.plugins.parameters.PluginParameters, boolean, java.util.List, au.gov.asd.tac.constellation.plugins.PluginSynchronizer)}
     * on its currently configured {@link PluginEnvironment}. All parameters to
     * this call will be specified by the currently configured state of this
     * PluginExecution.
     *
     * @param graph the graph to execute the plugin on.
     * @return a {@link Future} providing access to the process running the
     * plugin.
     */
    public Future<?> executeLater(final Graph graph) {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        return environment.executePluginLater(graph, plugin, parameters, interactive, disclaimer, futures, synchronizer);
    }

    /**
     * Causes this PluginExecutor to execute its plugin on the current thread by
     * calling
     * {@link PluginEnvironment#executePluginNow(au.gov.asd.tac.constellation.graph.Graph, au.gov.asd.tac.constellation.plugins.Plugin, au.gov.asd.tac.constellation.plugins.parameters.PluginParameters, boolean)}
     * on its currently configured {@link PluginEnvironment}. All parameters to
     * this call will be specified by the currently configured state of this
     * PluginExecution.
     *
     * @param graph the graph to execute the plugin on.
     * @return the return object from the plugin's execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public Object executeNow(final Graph graph) throws InterruptedException, PluginException {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        return environment.executePluginNow(graph, plugin, parameters, interactive);
    }

    /**
     * Causes this PluginExecutor to execute its plugin on the current thread,
     * using a pre-existing write lock by calling
     * {@link PluginEnvironment#executeEditPluginNow(au.gov.asd.tac.constellation.graph.GraphWriteMethods, au.gov.asd.tac.constellation.plugins.Plugin, au.gov.asd.tac.constellation.plugins.parameters.PluginParameters, boolean)}
     * on its currently configured {@link PluginEnvironment}. All parameters to
     * this call will be specified by the currently configured state of this
     * PluginExecution.
     *
     * @param graph the graph to execute the plugin on.
     * @return the return object from the plugin's execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public Object executeNow(final GraphWriteMethods graph) throws InterruptedException, PluginException {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        return environment.executeEditPluginNow(graph, plugin, parameters, interactive);
    }

    /**
     * Causes this PluginExecutor to execute its plugin on the current thread,
     * using a pre-existing read lock by calling
     * {@link PluginEnvironment#executeReadPluginNow(au.gov.asd.tac.constellation.graph.GraphReadMethods, au.gov.asd.tac.constellation.plugins.Plugin, au.gov.asd.tac.constellation.plugins.parameters.PluginParameters, boolean)}
     * on its currently configured {@link PluginEnvironment}. All parameters to
     * this call will be specified by the currently configured state of this
     * PluginExecution.
     *
     * @param graph the
     * {@link au.gov.asd.tac.constellation.graph.GraphReadMethods} to execute
     * the plugin on.
     * @return the return object from the plugin's execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public Object executeNow(final GraphReadMethods graph) throws InterruptedException, PluginException {
        if (parameters == null) {
            parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        }
        return environment.executeReadPluginNow(graph, plugin, parameters, interactive);
    }
}
