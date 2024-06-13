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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.NotificationDisplayer;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * This class holds all plugins that have been registered using the
 * ServiceProvider annotation. It is primarily used to get new instances of
 * registered plugins from their class names.
 *
 * The preferred pattern for getting a registered plugin involves storing its
 * class name as a string constant in registry a class and then calling
 * {@link #get PluginRegistry.get(SomePluginRegistry.PLUGIN_NAME)}
 *
 * @author algol
 */
public class PluginRegistry {

    private static final Logger LOGGER = Logger.getLogger(PluginRegistry.class.getName());

    private static HashMap<String, Class<? extends Plugin>> pluginsMap = null;
    private static HashMap<String, String> aliasMap = null;

    private PluginRegistry() {
    }

    private static synchronized void init() {
        if (pluginsMap != null) {
            return;
        }

        pluginsMap = new HashMap<>();
        aliasMap = new HashMap<>();
        final Map<String, Class<? extends Plugin>> pluginOverrides = new HashMap<>();
        final Result<Plugin> plugins = Lookup.getDefault().lookupResult(Plugin.class);

        final Modules modules = WindowManager.getDefault().getClass().getName().contains("org.openide.windows.DummyWindowManager") ? null : Modules.getDefault();
        for (Class<? extends Plugin> plugin : plugins.allClasses()) {
            final ModuleInfo mi = modules != null ? modules.ownerOf(plugin) : null;

            try {
                final String descr = NbBundle.getMessage(plugin, plugin.getSimpleName());
                final String msg = String.format(
                        "Discovered plugin %s (%s) in module %s (%s)",
                        plugin.getName(),
                        descr,
                        mi != null ? mi.getDisplayName() : "unknown",
                        mi != null ? mi.getCodeName() : "unknown"
                );
                LOGGER.info(msg);
            } catch (final MissingResourceException ex) {
                final String msg = String.format("Plugin class %s has no description: add a class name bundle.", plugin.getSimpleName());
                LOGGER.severe(msg);
                NotificationDisplayer.getDefault().notify("Plugin registration",
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        msg,
                        null,
                        NotificationDisplayer.Priority.HIGH
                );
            }

            // add the plugin to our registry.
            pluginsMap.put(plugin.getName(), plugin);
            aliasMap.put(getAlias(plugin.getName()), plugin.getName());

            // use reflection to limit the amount of plugins we have to instantiate to see if the plugin has been overridden
            for (final Method declaredMethod : plugin.getDeclaredMethods()) {
                if ("getOverriddenPlugins".equals(declaredMethod.getName())) {
                    pluginOverrides.put(plugin.getName(), plugin);
                }
            }
        }

        // update the maps with the overridden plugin
        pluginOverrides.forEach((pluginName, overridingPlugin) -> {
            try {
                for (final String overriddenPlugin : overridingPlugin.getDeclaredConstructor().newInstance().getOverriddenPlugins()) {

                    if (pluginsMap.containsKey(overriddenPlugin)) {
                        pluginsMap.remove(overriddenPlugin);
                        aliasMap.remove(getAlias(overriddenPlugin));
                    }

                    pluginsMap.put(overridingPlugin.getName(), overridingPlugin);
                    aliasMap.put(getAlias(overridingPlugin.getName()), overridingPlugin.getName());

                    LOGGER.log(Level.INFO, "Removed  plugin {0} as it is overriden by plugin {1})",
                            new Object[]{overriddenPlugin, overridingPlugin.getName()}
                    );
                }
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        });
    }

    /**
     * Generate a "nice" name for a plugin.
     *
     * @param name The fully qualified class name of a plugin (package.class).
     *
     * @return A "nice" name.
     */
    public static String getAlias(final String name) {
        final int ix = name.lastIndexOf('.');
        if (ix != -1) {
            String alias = name.substring(ix + 1).toUpperCase(Locale.ENGLISH);
            if (alias.endsWith("PLUGIN")) {
                alias = alias.substring(0, alias.length() - "PLUGIN".length());
            }

            return alias;
        }

        return name.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Get an instance of a registered plugin by name.
     *
     * @param name A string which is either the class name of the plugin or the
     * alias for the plugin generated by {@link #getAlias getAlias(className)}.
     *
     * @return A new instance of the named plugin.
     * @throws IllegalArgumentException If the supplied name did not correspond
     * to a registered plugin.
     */
    public static Plugin get(final String name) {
        init();

        if (pluginsMap.containsKey(name)) {
            final Class<? extends Plugin> c = pluginsMap.get(name);
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            final String alias = getAlias(name);
            if (aliasMap.containsKey(alias)) {
                return get(aliasMap.get(alias));
            }
        }

        // Throw a RunTimeException if an invalid name was passed.
        throw new IllegalArgumentException(String.format("No such plugin as %s!", name));
    }

    /**
     * Get a collection of new instances of all plugins registered as a given
     * subtype of plugin. For example, this method can be used to return a
     * collection of new instances of all registered data access plugins by
     * calling {@link #getFromLookup getFromLookup(DataAccessPlugin.class)}
     * <p>
     * Note that the subtype needs to be an abstract class which is a template
     * that concrete plugins can extend and be registered as using the
     * ServiceProvider annotation.
     *
     * @param type The class object for the subtype of plugin that is to be
     * collected (eg.
     * {@link au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin})
     * @return A Collection of new instances of all plugins registered as the
     * given subtype.
     */
    public static Collection<Plugin> getFromLookup(Class<? extends Plugin> type) {
        final Collection<Plugin> plugins = new ArrayList<>();
        for (final Plugin plugin : Lookup.getDefault().lookupResult(type).allInstances()) {
            plugins.add(get(plugin.getClass().getName()));
        }

        return plugins;
    }

    /**
     * Return a set containing all the class names of the available plugins.
     *
     * @return A set containing all the class names of the available plugins.
     */
    public static Set<String> getPluginClassNames() {
        init();
        return Collections.unmodifiableSet(pluginsMap.keySet());
    }

    /**
     * Does the named plugin exist?
     *
     * @param pluginName A string which is either the class name of the plugin
     * or the alias for the plugin generated by
     * {@link #getAlias getAlias(className)}.
     *
     * @return True if the plugin exists, false otherwise.
     */
    public static boolean exists(final String pluginName) {
        init();

        if (pluginsMap.containsKey(pluginName)) {
            return true;
        }

        return aliasMap.containsKey(getAlias(pluginName));
    }
}
