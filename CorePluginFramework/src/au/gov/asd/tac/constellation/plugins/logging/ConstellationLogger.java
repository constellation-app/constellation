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
package au.gov.asd.tac.constellation.plugins.logging;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Properties;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * An interface for listening to the life cycle of plugins. Implementations can
 * record these details in the desired manner.
 *
 * @author sirius
 * @author cygnus_x-1
 */
public interface ConstellationLogger {

    public static ConstellationLogger getDefault() {
        return Lookup.getDefault().lookup(ConstellationLogger.class);
    }

    /**
     * Called to indicate that Constellation was started.
     */
    public void applicationStarted();

    /**
     * Called to indicate that Constellation was shut down.
     */
    public void applicationStopped();

    /**
     * Called to indicate that a Constellation View was opened.
     *
     * @param view the view that was opened.
     */
    public void viewStarted(final TopComponent view);

    /**
     * Called to indicate that a Constellation View was closed.
     *
     * @param view the view that was closed.
     */
    public void viewStopped(final TopComponent view);

    /**
     * Called to provide information about the state of a view.
     *
     * @param view the view providing the information.
     * @param info a {@link String} conveying customized information about the
     * view.
     */
    public void viewInfo(final TopComponent view, final String info);

    /**
     * Called to indicate that a plugin has started execution.
     *
     * @param plugin the plugin that is starting execution.
     * @param parameters the parameters that have been used to configure the
     * @param graph the graph that the plugin is running on (may be null).
     * plugin.
     */
    public void pluginStarted(final Plugin plugin, final PluginParameters parameters, final Graph graph);

    /**
     * Called to indicate that a plugin has stopped execution.
     *
     * @param plugin the plugin that is stopping execution.
     * @param parameters the parameters that have been used to configure the
     * plugin.
     */
    public void pluginStopped(final Plugin plugin, final PluginParameters parameters);

    /**
     * Called to provide information about a plugin execution.
     *
     * @param plugin the plugin being executed.
     * @param info a {@link String} conveying customized information about the
     * plugin.
     */
    public void pluginInfo(final Plugin plugin, final String info);

    /**
     * Called to indicate that an error occurred during a plugin execution.
     *
     * @param plugin the plugin being executed.
     * @param error a {@link Throwable} that describes the error that occurred.
     */
    public void pluginError(final Plugin plugin, final Throwable error);

    /**
     * Called to provide properties (key/value pairs) that provide more
     * information about this plugin execution. This may include information
     * such as how many elements were added to a graph, or how many elements
     * were selected. The keys are completely up to the plugin author.
     *
     * @param plugin the plugin being executed.
     * @param properties a {@link Properties} to associate with this plugin
     * execution.
     */
    public void pluginProperties(final Plugin plugin, final Properties properties);
}
