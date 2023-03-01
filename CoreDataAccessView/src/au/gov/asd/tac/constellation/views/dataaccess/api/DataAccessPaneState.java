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
package au.gov.asd.tac.constellation.views.dataaccess.api;

import au.gov.asd.tac.constellation.utilities.threadpool.UniversalThreadPool;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.LookupPluginsTask;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Maintains the active state of the Data Access view. It also holds a loaded copy
 * of all data access plugins that could be located at startup.
 *
 * @author formalhaunt
 */
public class DataAccessPaneState {
    /**
     * Tracks the states of the Data Access view per graph. The key is the graph ID
     * and the value is the current state for that graph.
     */
    private static final Map<String, DataAccessPaneStatePerGraph> DATA_ACCESS_PANE_STATES = new HashMap<>();
    
    /**
     * Function that will generate the {@link DataAccessPaneStatePerGraph} for a graph
     * that does not yet have an entry.
     */
    private static final Function<String, DataAccessPaneStatePerGraph> COMPUTE_IF_ABSENT =
            graphId -> new DataAccessPaneStatePerGraph();
    
    /**
     * This is the {@link Future} tracking the load of the plugins. Once this is
     * complete, the plugins can be accessed.
     */
    private static final Future<Map<String, List<DataAccessPlugin>>> PLUGIN_LOAD;
    
    /**
     * The ID of the currently active graph.
     */
    private static String currentGraphId;
    
    static {
        // As soon as the pane state is interacted with begin loading the plugins
        // in a separate thread so they are ready when requested. They do not change
        // so only need to be loaded once at initialization.
        PLUGIN_LOAD = CompletableFuture.supplyAsync(
                new LookupPluginsTask(),
                Executors.newSingleThreadExecutor()
        ).thenApply(plugins -> {
            // Sort the DataAccessPlugin lists within each type including the category type
            // so that favourites category is sorted properly.
            final DataAccessPluginComparator comparator = new DataAccessPluginComparator();
            plugins.values().forEach(pluginList -> Collections.sort(pluginList, comparator));
            return plugins;
        });
    }
    
    /**
     * Private constructor to prevent initialization.
     */
    private DataAccessPaneState() {
    }
    
    /**
     * Gets the data access plugins that have been found in the class path. If the lookup
     * has not yet completed, this will block until it is.
     * <p/>
     * The map groups the data access plugins based on their type, see {@link DataAccessPluginType}.
     *
     * @return the found data access plugins
     * @throws InterruptedException if the plugin lookup was interrupted
     * @throws ExecutionException if there was an error during the plugin lookup
     */
    public static Map<String, List<DataAccessPlugin>> getPlugins() throws InterruptedException, ExecutionException {
        return PLUGIN_LOAD.get();
    }
    
    /**
     * Gets the data access plugins that have been found in the class path. If the lookup
     * has not yet completed, this will block until it is. If the lookup takes longer
     * than the passed timeout then a {@link TimeoutException} will be thrown.
     * <p/>
     * The map groups the data access plugins based on their type, see {@link DataAccessPluginType}.
     *
     * @param timeout the time to wait for the data access plugin lookup to complete
     * @param unit the time unit that the timeout is specified in
     * @return the found data access plugins
     * @throws InterruptedException if the plugin lookup was interrupted
     * @throws ExecutionException if there was an error during the plugin lookup
     * @throws TimeoutException if the plugin lookup took longer than the specified timeout
     */
    public static Map<String, List<DataAccessPlugin>> getPlugins(final long timeout,
                                                                 final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return PLUGIN_LOAD.get(timeout, unit);
    }
    
    /**
     * Set the ID of the current graph.
     *
     * @param currentGraphId the ID of the current graph
     */
    public static synchronized void setCurrentGraphId(final String currentGraphId) {
        DataAccessPaneState.currentGraphId = currentGraphId;
    }
    
    /**
     * Get the ID of the current graph.
     *
     * @return the ID of the current graph
     */
    public static synchronized String getCurrentGraphId() {
        return DataAccessPaneState.currentGraphId;
    }
    
    /**
     * Get a flag indicating if there are any queries or data access plugins
     * running for the current graph.
     *
     * @return true if there are queries running for the current graph, false otherwise
     */
    public static synchronized boolean isQueriesRunning() {
        return isQueriesRunning(currentGraphId);
    }
    
    /**
     * Get a flag indicating if there are any queries or data access plugins
     * running for the passed graph.
     *
     * @param graphId the ID of the graph to check for running plugins
     * @return true if there are queries running for the current graph, false otherwise
     */
    public static synchronized boolean isQueriesRunning(final String graphId) {
        return graphId != null && DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .isQueriesRunning();
    }
    
    /**
     * Sets the flag indicating if there are any queries or data access plugins
     * running for the current graph.
     *
     * @param isQueriesRunning true if there are queries running for the current graph,
     *     false otherwise
     */
    public static synchronized void setQueriesRunning(final boolean isQueriesRunning) {
        setQueriesRunning(currentGraphId, isQueriesRunning);
    }
    
    /**
     * Sets the flag indicating if there are any queries or data access plugins
     * running for the passed graph.
     *
     * @param graphId the ID of the graph to set the flag on
     * @param isQueriesRunning true if there are queries running for the passed graph,
     *     false otherwise
     */
    public static synchronized void setQueriesRunning(final String graphId,
                                                      final boolean isQueriesRunning) {
        if (graphId != null) {
            DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .setQueriesRunning(isQueriesRunning);
        }
    }
    
    /**
     * Add a running plugin entry to the state of the current graph.
     *
     * @param runningPlugin a {@link Future} representing the running plugin
     * @param runningPluginName the name of the running plugin
     */
    public static synchronized void addRunningPlugin(final Future<?> runningPlugin,
                                                     final String runningPluginName) {
        addRunningPlugin(currentGraphId, runningPlugin, runningPluginName);
    }
    
    /**
     * Add a running plugin entry to the state of the passed graph.
     *
     * @param graphId the ID of the graph to associate the running plugin with
     * @param runningPlugin a {@link Future} representing the running plugin
     * @param runningPluginName the name of the running plugin
     */
    public static synchronized void addRunningPlugin(final String graphId,
                                                     final Future<?> runningPlugin,
                                                     final String runningPluginName) {
        if (graphId == null) {
            throw new IllegalStateException("Cannot add running plugin. Graph ID is null.");
        }
        
        DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .getRunningPlugins().put(runningPlugin, runningPluginName);
    }
    
    /**
     * Remove all running plugins for the current graph.
     */
    public static synchronized void removeAllRunningPlugins() {
        removeAllRunningPlugins(currentGraphId);
    }
    
    /**
     * Remove all running plugins for the passed graph.
     *
     * @param graphId the ID of the graph to remove the plugins from
     */
    public static synchronized void removeAllRunningPlugins(final String graphId) {
        if (graphId != null) {
            DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .getRunningPlugins().clear();
        }
    }
    
    /**
     * Get all the running plugins for the current graph or an empty map if there
     * is not current graph. The returned map is an immutable copy of the running
     * plugin list in the state.
     *
     * @return an immutable copy of the running plugins for the current graph
     */
    public static synchronized Map<Future<?>, String> getRunningPlugins() {
        return getRunningPlugins(currentGraphId);
    }
    
    /**
     * Get all the running plugins for the passed graph or an empty map if the
     * passed graph ID is null. The returned map is an immutable copy of the running
     * plugin list in the state.
     * 
     * @param graphId the ID of the graph that the running plugins will be retrieved for
     * @return an immutable copy of the running plugins for the passed graph
     */
    public static synchronized Map<Future<?>, String> getRunningPlugins(final String graphId) {
        if (graphId != null) {
            return ImmutableMap.copyOf(
                    DATA_ACCESS_PANE_STATES
                            .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                            .getRunningPlugins()
            );
        }
        return Collections.emptyMap();
    }
    
    /**
     * Gets the execute buttons "Go" state for the current graph.
     * <p/>
     * The "Go" state essentially means that the button has the text "Go" and
     * is enabled.
     * <p/>
     * When it is in this state, it means 
     * <ul>
     * <li>There are currently no queries running</li>
     * <li>There are enabled and valid plugins to run</li>
     * </ul>
     *
     * @return true if the execute button for the current graph is in the "Go"
     *     state, false otherwise
     */
    public static synchronized boolean isExecuteButtonIsGo() {
        return isExecuteButtonIsGo(currentGraphId);
    }
    
    /**
     * Gets the execute buttons "Go" state for the passed graph.
     *
     * @param graphId the ID of the graph that the execute button status is being
     *     checked for
     * @return true if the execute button for the current graph is in the "Go"
     *     state, false otherwise
     * @see #isExecuteButtonIsGo() 
     */
    public static synchronized boolean isExecuteButtonIsGo(final String graphId) {
        return graphId != null && DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .isExecuteButtonIsGo();
    }
    
    /**
     * Updates the execute buttons "Go" state for the current graph.
     * 
     * @param isGo true if the execute button is in the "Go" state
     * @see #isExecuteButtonIsGo() 
     */
    public static synchronized void updateExecuteButtonIsGo(final boolean isGo) {
        updateExecuteButtonIsGo(currentGraphId, isGo);
    }
    
    /**
     * Updates the execute buttons "Go" state for the passed graph.
     * 
     * @param graphId the ID of the graph that the execute button status is being
     *     updated for
     * @param isGo true if the execute button is in the "Go" state, false otherwise
     * @see #isExecuteButtonIsGo() 
     */
    public static synchronized void updateExecuteButtonIsGo(final String graphId,
                                                            final boolean isGo) {
        if (graphId != null) {
            DATA_ACCESS_PANE_STATES
                .computeIfAbsent(graphId, COMPUTE_IF_ABSENT)
                    .setExecuteButtonIsGo(isGo);
        }
    }
    
    /**
     * Clears all the states for all the graphs. Current graph ID is set to null
     * and the graph to state map is emptied.
     */
    public static synchronized void clearState() {
        currentGraphId = null;
        DATA_ACCESS_PANE_STATES.clear();
    }
    
    /**
     * Comparator that orders a list of data access plugins based on their type.
     */
    protected static class DataAccessPluginComparator implements Comparator<DataAccessPlugin>, Serializable {
        private static final long serialVersionUID = 1;

        private final Map<String, Integer> typesWithPosition;
        
        /**
         * Initializes the data access plugin comparator getting the ordering of
         * the different plugin types from {@link DataAccessPluginType#getTypeWithPosition()}.
         *
         */
        public DataAccessPluginComparator() {
            typesWithPosition = DataAccessPluginType.getTypeWithPosition();
        }
        
        /**
         * If the plugins type positions are equal, then order is determined based
         * on the individual plugins position, otherwise the type postion is used.
         *
         * @param plugin1 the first plugin to compare
         * @param plugin2 the second plugin to compare
         * @return 0 if the plugins positions are the same, &lt; 0 if plugin1 position
         *     is less then plugin2 position and &gt; if plugin1 position is greater than
         *     plugin2 position
         */
        @Override
        public int compare(final DataAccessPlugin plugin1, final DataAccessPlugin plugin2) {
            if (typesWithPosition.get(plugin1.getType()).equals(typesWithPosition.get(plugin2.getType()))) {
                return Integer.compare(
                        plugin1.getPosition(),
                        plugin2.getPosition()
                );
            } else {
                return Integer.compare(
                        typesWithPosition.get(plugin1.getType()),
                        typesWithPosition.get(plugin2.getType())
                );
            }
        }
        
    }
}
