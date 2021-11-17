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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.openide.util.Lookup;

/**
 * Looks up all available data access plugins and populates a map with them
 * grouped by plugin type.
 *
 * @author formalhaunt
 */
public class LookupPluginsTask implements Supplier<Map<String, List<DataAccessPlugin>>> {
    private static final Logger LOGGER = Logger.getLogger(LookupPluginsTask.class.getName());
    
    @Override
    public Map<String, List<DataAccessPlugin>> get() {
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
                .forEach(pluginToRemoveName -> 
                    // Gets all the plugin type lists it could be in basically itself
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
     * Logs that a data access plugin has been discovered and added to the plugin map.
     * The log will indicate which type the plugin has been added under, in this case
     * it is the actual plugins type.
     *
     * @param plugin the plugin that is being added
     */
    private void logDiscoveredDataAccessPlugin(final DataAccessPlugin plugin) {
        logDiscoveredDataAccessPlugin(plugin, plugin.getType());
    }
    
    /**
     * Logs that a data access plugin has been discovered and added to the plugin map.
     * The log will indicate which type the plugin has been added under, in this case
     * it is passed type value.
     *
     * @param plugin the plugin that is being added
     * @param pluginType the plugin type that the plugin will be added under
     */
    private void logDiscoveredDataAccessPlugin(final DataAccessPlugin plugin,
                                               final String pluginType) {
        LOGGER.log(Level.INFO, String.format("Discovered data access plugin %s (%s)",
                                plugin.getName(), pluginType));
    }
    
}
