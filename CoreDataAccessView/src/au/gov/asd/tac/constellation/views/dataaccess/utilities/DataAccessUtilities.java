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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessConcept;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessState;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author algol
 */
public class DataAccessUtilities {

    private static final Logger LOGGER = Logger.getLogger(DataAccessUtilities.class.getName());

    private DataAccessUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * A convenience method for getting the Pane used by the Data Access view.
     * <p>
     * This allows dialog boxes to be given parents, for example.
     * <p>
     * If the Data Access view is not opened, it will be.
     *
     * @return The Pane used by the Data Access view.
     */
    public static DataAccessPane getDataAccessPane() {
        if (SwingUtilities.isEventDispatchThread()) {
            return getInternalDataAccessPane();
        }

        final DataAccessPane[] panes = new DataAccessPane[1];
        try {
            SwingUtilities.invokeAndWait(() -> panes[0] = getInternalDataAccessPane());
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return panes[0];
    }

    /**
     * Load the data access graph state and update the data access view.
     * <p/>
     * Currently only looking at string parameter types and only shows the first
     * step if it exists.
     *
     * @param dataAccessPane the data access pane
     * @param graph the active graph to load the state from
     */
    public static void loadDataAccessState(final DataAccessPane dataAccessPane,
            final Graph graph) {
        if (graph != null && dataAccessPane.getDataAccessTabPane().getCurrentTab() != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();

            try {
                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.get(readableGraph);
                
                if (dataAccessStateAttribute != Graph.NOT_FOUND) {
                    final DataAccessState dataAccessState = readableGraph.getObjectValue(dataAccessStateAttribute, 0);

                    if (dataAccessState != null && !dataAccessState.getState().isEmpty()) {
                        // TODO: support multiple tabs (not just first one in state) and not
                        //       introduce memory leaks
                        final Map<String, String> tabState = dataAccessState.getState().get(0);
                        final Tab step = dataAccessPane.getDataAccessTabPane().getTabPane().getTabs().get(0);

                        DataAccessTabPane.getQueryPhasePane(step)
                                .getGlobalParametersPane().getParams().getParameters().entrySet().stream()
                                .forEach(param -> {
                                    final PluginParameter<?> pp = param.getValue();
                                    final String paramvalue = tabState.get(param.getKey());
                                    if (paramvalue != null) {
                                        pp.setStringValue(paramvalue);
                                    }
                                });
                    }
                }
            } finally {
                readableGraph.release();
            }
        }
    }

    /**
     * Build a data access state from the passed tab pane and save it to the
     * graph.
     * <p/>
     * Currently only global parameters are saved.
     *
     * @param tabs the tab pane to build the new data access state from
     * @param graph the active graph to save the state to
     */
    public static void saveDataAccessState(final TabPane tabs, final Graph graph) {
        if (graph != null) {
            // Build a new data access state from the passed tabs
            final DataAccessState dataAccessState = new DataAccessState();

            tabs.getTabs().forEach(step -> {
                dataAccessState.newTab();

                DataAccessTabPane.getQueryPhasePane(step).getGlobalParametersPane()
                        .getParams().getParameters().entrySet().stream()
                        // Don't add parameters with a null value
                        .filter(entry -> entry.getValue().getStringValue() != null)
                        // Add the non null parameter to the state
                        .forEach(entry
                                -> dataAccessState.add(
                                entry.getKey(),
                                entry.getValue().getStringValue()
                        )
                        );
            });

            // Save the state onto the graph
            WritableGraph wg = null;
            try {
                wg = graph.getWritableGraph("Update Data Access State", true);

                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.ensure(wg);

                wg.setObjectValue(dataAccessStateAttribute, 0, dataAccessState);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                Thread.currentThread().interrupt();
            } finally {
                if (wg != null) {
                    wg.commit();
                }
            }
        }
    }

    /**
     * Finds a window that is of type {@link DataAccessViewTopComponent} and
     * opens it, if it is not already open. Once open it accesses the data
     * access pane that it contains and returns it.
     *
     * @return the {@link DataAccessPane} of the current Data Access View
     */
    private static DataAccessPane getInternalDataAccessPane() {
        final TopComponent tc = WindowManager.getDefault()
                .findTopComponent(DataAccessViewTopComponent.class.getSimpleName());
        if (tc != null) {
            if (!tc.isOpened()) {
                tc.open();
            }
            tc.requestVisible();
            return ((DataAccessViewTopComponent) tc).getDataAccessPane();
        } else {
            return null;
        }
    }

    public static Map<String, List<DataAccessPlugin>> getAllPlugins() {
        // Creates a map with the key set being every available data access plugin type.
        final Map<String, List<DataAccessPlugin>> plugins = DataAccessPluginType.getTypes().stream()
                .collect(Collectors.toMap(
                        type -> type,
                        type -> new ArrayList<>(),
                        (type1, type2) -> new ArrayList<>(),
                        LinkedHashMap::new
                ));

        // Create the favourites category
        plugins.computeIfAbsent(DataAccessPluginCoreType.FAVOURITES, key -> new ArrayList<>());

        // Now fetch the DataAccessPlugin instances.
        final Multimap<String, String> pluginNameToType = ArrayListMultimap.create();
        final List<String> pluginOverrides = new ArrayList<>();
        Lookup.getDefault().lookupAll(DataAccessPlugin.class).parallelStream()
                // If plugin is disabled, ignore the plugin.
                .filter(DataAccessPlugin::isEnabled)
                // If a plugin type is invalid (that is, not registered as a DataAccessPluginType),
                // and not in the users favourites, ignore the plugin.
                .filter(plugin -> plugins.containsKey(plugin.getType())
                || DataAccessPreferenceUtilities.isfavourite(plugin.getName(), false))
                .forEach(plugin -> {
                    // If the plugin is a user's favourite, add it to the favourite list
                    if (DataAccessPreferenceUtilities.isfavourite(plugin.getName(), false)) {
                        plugins.get(DataAccessPluginCoreType.FAVOURITES).add(plugin);
                        logDiscoveredDataAccessPlugin(plugin, DataAccessPluginCoreType.FAVOURITES);

                        // Register that this plugin has been added under favourites for when
                        // overriden plugins are being dealt with
                        pluginNameToType.put(plugin.getClass().getName(), DataAccessPluginCoreType.FAVOURITES);
                    }

                    // If plugin type is valid, add the plugin to the Data Access View.
                    if (plugins.containsKey(plugin.getType())) {
                        plugins.get(plugin.getType()).add(plugin);
                        logDiscoveredDataAccessPlugin(plugin);

                        // If plugin overrides another, record which plugin should be removed
                        // for later processing. Also record name to type so that it doesn't
                        // need to loop through every type to find it
                        pluginNameToType.put(plugin.getClass().getName(), plugin.getType());
                        pluginOverrides.addAll(plugin.getOverriddenPlugins());
                    }
                });

        // Remove any overridden plugins.
        pluginOverrides.stream()
                .forEach(pluginToRemoveName
                        -> // Gets all the plugin type lists it could be in basically itself
                        // and favourites at most
                        pluginNameToType.get(pluginToRemoveName)
                        .forEach(pluginToRemoveType -> {
                            // For the given plugin name and type get the list that the plugin will be in
                            final List<DataAccessPlugin> pluginList = plugins.get(pluginToRemoveType);
                            IntStream.range(0, pluginList.size())
                                    .filter(index -> pluginList.get(index).getClass().getName()
                                    .equals(pluginToRemoveName))
                                    .findFirst()
                                    .ifPresent(index -> {
                                        // Remove the overriden plugin
                                        pluginList.remove(index);
                                        LOGGER.log(Level.INFO,
                                                String.format("Removed data access plugin %s (%s) as it is overriden.",
                                                        pluginToRemoveName, pluginToRemoveType)
                                        );
                                    });
                        })
                );

        return plugins;
    }

    /**
     * Logs that a data access plugin has been discovered and added to the
     * plugin map. The log will indicate which type the plugin has been added
     * under, in this case it is the actual plugins type.
     *
     * @param plugin the plugin that is being added
     */
    private static void logDiscoveredDataAccessPlugin(final DataAccessPlugin plugin) {
        logDiscoveredDataAccessPlugin(plugin, plugin.getType());
    }

    /**
     * Logs that a data access plugin has been discovered and added to the
     * plugin map. The log will indicate which type the plugin has been added
     * under, in this case it is passed type value.
     *
     * @param plugin the plugin that is being added
     * @param pluginType the plugin type that the plugin will be added under
     */
    private static void logDiscoveredDataAccessPlugin(final DataAccessPlugin plugin,
            final String pluginType) {
        LOGGER.log(Level.INFO, String.format("Discovered data access plugin %s (%s)",
                plugin.getName(), pluginType));
    }

}
