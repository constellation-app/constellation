/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.pluginframework.logging;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import java.util.Properties;
import org.openide.util.Lookup;

/**
 * An interface for listening to the life cycle of plugins. Implementations can
 * record these details in the desired manner.
 *
 * @author sirius
 */
public interface ConstellationLogger {

    public static ConstellationLogger getDefault() {
        return Lookup.getDefault().lookup(ConstellationLogger.class);
    }

    /**
     * Called to indicate that Constellation is starting.
     */
    public void applicationStart();

    /**
     * Called to indicate that Constellation is shutting down.
     */
    public void applicationStop();

    /**
     * Called to indicate that a plugin is starting execution.
     *
     * @param graph the graph that the plugin is running on. May be null.
     * @param plugin the plugin that is starting execution.
     * @param parameters the parameters that have been used to configure the
     * plugin.
     */
    public void pluginStart(final Graph graph, final Plugin plugin, final PluginParameters parameters);

    /**
     * Called to indicate that a plugin is stopping execution.
     *
     * @param plugin the plugin that is stopping execution.
     * @param parameters the parameters that have been used to configure the
     * plugin.
     */
    public void pluginStop(final Plugin plugin, final PluginParameters parameters);

    /**
     * Called to provide customized information about the plugin.
     *
     * @param plugin the plugin.
     * @param info a string conveying customized information about the plugin.
     */
    public void pluginInfo(final Plugin plugin, final String info);

    /**
     * Called to indicate that an error occurred during plugin execution.
     *
     * @param plugin the plugin that was running when the error occurred.
     * @param error a Throwable that describes the error that occurred.
     */
    public void pluginError(final Plugin plugin, final Throwable error);

    /**
     * Called to provide properties (key/value pairs) that provide more
     * information about this plugin execution. This may include information
     * such as how many elements were added to a graph, or how many elements
     * were selected. The keys are completely up to the plugin author.
     *
     * @param plugin the plugin being executed.
     * @param properties the properties to associate with this plugin execution.
     */
    public void pluginProperties(final Plugin plugin, final Properties properties);
}
