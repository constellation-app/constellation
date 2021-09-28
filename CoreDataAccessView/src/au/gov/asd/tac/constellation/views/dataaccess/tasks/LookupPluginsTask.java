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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 *
 * @author formalhaunt
 */
public class LookupPluginsTask implements Supplier<Map<String, List<DataAccessPlugin>>> {
    private static final Logger LOGGER = Logger.getLogger(LookupPluginsTask.class.getName());
    
    @Override
    public Map<String, List<DataAccessPlugin>> get() {
        final Map<String, List<DataAccessPlugin>> plugins = new LinkedHashMap<>();

        // Use a pre-filled LinkedHashMap to keep the types in the correct order.
        final List<String> typeList = DataAccessPluginType.getTypes();
        typeList.stream().forEach(type -> plugins.put(type, new ArrayList<>()));

        // create the favourites category
        if (plugins.get(DataAccessPluginCoreType.FAVOURITES) == null) {
            plugins.put(DataAccessPluginCoreType.FAVOURITES, new ArrayList<>());
        }

        // Now fetch the DataAccessPlugin instances.
        final Map<String, DataAccessPlugin> pluginOverrides = new HashMap<>();
        Lookup.getDefault().lookupAll(DataAccessPlugin.class).stream()
                .forEach(plugin -> {
                    if (!plugin.isEnabled()) {
                        // If plugin is disabled, ignore the plugin.
                        LOGGER.log(Level.INFO, "Disabled data access plugin {0} ({1})",
                                new Object[]{plugin.getName(), plugin.getType()});
                    } else {
                        final String type = plugin.getType();
                        if (plugins.containsKey(type)) {
                            // If plugin type is valid, add the plugin to the Data Access View.
                            plugins.get(type).add(plugin);
                            
                            LOGGER.log(Level.INFO, "Discovered data access plugin {0} ({1})",
                                    new Object[]{plugin.getName(), plugin.getType()});
                            
                            // If plugin overrides another, record which plugin should be removed
                            // for later processing.
                            for (final String overriddenPluginName : plugin.getOverriddenPlugins()) {
                                pluginOverrides.put(overriddenPluginName, plugin);
                            }
                        } else {
                            // If a plugin type is invalid (that is, not registered as a DataAccessPluginType),
                            // ignore the plugin.
                            LOGGER.log(Level.SEVERE, "Unexpected data access plugin type '{0}' for plugin {1}",
                                    new Object[]{type, plugin.getName()});
                        }

                        // favourites
                        if (DataAccessPreferenceUtilities.isfavourite(plugin.getName(), false)) {
                            plugins.get(DataAccessPluginCoreType.FAVOURITES).add(plugin);
                            LOGGER.log(Level.INFO, "Discovered data access plugin {0} ({1})", new Object[]{plugin.getName(), DataAccessPluginCoreType.FAVOURITES});
                        }
                    }
                });

        // Remove any overridden plugins.
        pluginOverrides.forEach((pluginName, overridingPlugin) -> {
            String removeType = null;
            DataAccessPlugin removePlugin = null;
            for (String pluginType : plugins.keySet()) {
                final List<DataAccessPlugin> pluginList = plugins.get(pluginType);
                for (DataAccessPlugin plugin : pluginList) {
                    if (plugin.getName().equals(pluginName)) {
                        removeType = pluginType;
                        removePlugin = plugin;
                        break;
                    }
                }
            }

            if (removeType != null && removePlugin != null) {
                plugins.get(removeType).remove(removePlugin);
                LOGGER.log(Level.INFO, "Removed data access plugin {0} ({1}) as it is overriden by data access plugin {2} ({3})",
                        new Object[]{removePlugin.getName(), removeType, overridingPlugin.getName(), overridingPlugin.getType()});
            }
        });
        
        return plugins;
    }
    
}
